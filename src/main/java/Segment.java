import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

// A segment of the tree, has recursive functions for updating, deleting, and calculating aspects of biological model

public class Segment {
  private final TreeParams tParams;
  private final SimulationParams sParams;
  float segWidth; 
  Vector3 start; // of segment
  Vector3 end; //of segment
  Vector3 direction;  // Length and direction.
  Vector3 unit_direction;  // Unit vector in the direction.
  int age;
  Segment segParent; //the segment from which this segment sprouts
  ArrayList<Segment> children = new ArrayList<Segment>(); //all segments that sprout directly from this one
  float[] rotateToSegment; //matrix for graphics
  float massWithDescendants; //mass of this segment and all descendants of it
  float leafMassWithDescendants; //total leaf mass of this segment and all descendants of it
  Vector3 centerOfMassWithDescendants; //center of mass of everything branching from this segment (including itself)
  Vector3 centerOfWindForceWithDescendants; //center of wind force of everything branching from this segment (including itself)
  float windHorizontalForceWithDescendants; //force due to wind on this segment and its descendants
  float carbonProducedWithDescendants; //carbon budget generated from everything descending from this segment
  
  Segment( Segment parent, Vector3 segmentStart, Vector3 segmentDir, TreeParams paramsIn, SimulationParams sParamsIn) {
	tParams = paramsIn;
	sParams = sParamsIn;
    segWidth = .01f;
    start = segmentStart;
    direction = segmentDir;
    end = start.plus(direction);
    age = 0;
    segParent = parent;
    unit_direction = direction.cloneVector();
    unit_direction.normalize();
    
    // prepare for drawing: rotate the coordinate system so that Z is in the direction of the segment.
    Vector3 z = unit_direction;
    Vector3 x = z.onePerpendicular();
    x.normalize();
    Vector3 y = z.crossedWith(x);
    float[] rotateToSegment1 =
      {x.x, x.y, x.z, 0,
       y.x, y.y, y.z, 0,
       z.x, z.y, z.z, 0,
       (start.x + Simulation.SIZE.x) % Simulation.SIZE.x, (start.y + Simulation.SIZE.y) % Simulation.SIZE.y, start.z, 1f};
    rotateToSegment = rotateToSegment1;
    resetValues();
  }
  
  //resets the values, creates a "flag" so that computer knows the values have to be recalculated
  void resetValues() {
	  massWithDescendants = 0;
	  leafMassWithDescendants = 0;
	  centerOfMassWithDescendants = Vector3.NULL_VECTOR;
	  centerOfWindForceWithDescendants = Vector3.NULL_VECTOR;
	  windHorizontalForceWithDescendants = 0;
  }
  
  public void ageOneYear() {
	  age++;
  }

  //function that updates the width of the segment based on stress as long as carbon budget is still available
  //also kills a branch if it produces less carbon than it uses to grow in width
  float updateSegmentWidth(VoxelGrid voxels, float carbonBudget) {
	if (carbonBudget < 0) {
		return carbonBudget;
	}
    float cBudget = carbonBudget;
    for (int i = 0; i < children.size(); i++)
    {
    	cBudget = children.get(i).updateSegmentWidth(voxels, cBudget);
    }
    if (cBudget > 0) {
    	float previousMass = getSegmentDryMass();
    	//segWidth = Math.max(segWidth, computeWidthByStress());
    	segWidth += computeWidthByStress();
    	cBudget -= (getSegmentDryMass() - previousMass);
    }
    //kills branch if it doesn't produce as much as it used
    if (carbonProducedWithDescendants != 0 && carbonBudget-cBudget > carbonProducedWithDescendants) {
    	if (segParent != null)
    		segParent.removeChild(this);
    	ArrayList<Segment> descendants = getDescendants();
    	for (int i = 0; i < descendants.size(); i++) //record that the cells in the voxel grid are no longer full
    	{
    		voxels.setCellFull(descendants.get(i).end(), false);
    	}
    	voxels.setCellFull(end(), false);
    }
    resetValues();
    return cBudget;   
  }
  
  //branches and creates new segments according to light availability at the branching site (end of segment)
  float updateSegmentBranching(VoxelGrid voxels, float carbonBudget) {	
	  float cBudget = carbonBudget;
	  // create new child branches if has not made 3 children already, chance of branching otherwise is proportional to amount of light at end of seg.
		  if (children.size() <= 3 && (int)(Simulation.random() * (1 / voxels.getLight(end))) == 0) {
			  Vector3 randomVector = new Vector3(((float)Simulation.random() - 0.5f), 
					  ((float)Simulation.random() - 0.5f), 
					  ((float)Simulation.random() - 0.5f));
			  randomVector.scale(tParams.branchAngleFactor());
			  Vector3 newDirection = direction.plus(randomVector);
			  newDirection.normalize();
			  newDirection.scale(tParams.segmentLength());
			  Segment newSegment = new Segment(this, end(), newDirection, tParams, sParams); //new Segment branches in random direction, maximum branch angle dependent on branch angle factor
			  Vector3 point = newSegment.end();   
			  if (!(voxels.cellFull(point))) //if there not already a branch taking up the space where the end of the segment would be
			  {
				  voxels.setCellFull(point, true); //set that cell in the voxelgrid to be full
				  children.add(newSegment); //add the segment as a child
				  cBudget -= newSegment.getSegmentDryMass();
			  }
		  }
	  return cBudget;
  }
  
  //gets the amount of carbon available for growth based on incident light in one year
  float computeCarbonBudget(VoxelGrid voxels) {
	  float budget = 0.0f;
	  for (Segment child : children) {
		  budget += child.computeCarbonBudget(voxels);
	  }
	  if (isLeaf()) {
		  float leafBudget = calculateCarbonForLeaf(voxels);
		  leafBudget -= leafBudget * tParams.rootApportion();
		  leafBudget -= tParams.leafDryMass();
		  budget += leafBudget;
	  }
	  carbonProducedWithDescendants = budget;
	  return budget;
  }
  
  //returns the mass of the leaves of this segment and everything descending from it
  float getMassOfLeaves() {
	  if (leafMassWithDescendants == 0) {
		  float mass = 0;
		  float massOfLeaf = tParams.massOfLeaf(); 
		  for (Segment segment: children)
		  {
			  mass += segment.getMassOfLeaves();
		  }
		  if (isLeaf()) {
			  mass += massOfLeaf;
		  }
		  leafMassWithDescendants = mass;
	  }
	  return leafMassWithDescendants;
  }
  
  //removes a child from ArrayList of children
  void removeChild( Segment seg ) {
    children.remove(seg);
  }
  
  //uses Monteith photosynthesis equation to calculate the net biomass accumulation of the canopy at each leaf
  float calculateCarbonForLeaf(VoxelGrid voxels) {
	  float lightPerSquareMeter = sParams.totalSunlightIncident; //light incident on square meter in one year, in MJ
	  float leafArea = tParams.leafArea(); //set in TreeParams
	  float lightPerLeaf = lightPerSquareMeter * leafArea * (float)voxels.getLight(end); //amount of light incident on one leaf in a year
	  float e = tParams.efficiency(); //efficiency of leaves, e in equation, kg/MJ
	  float fs = tParams.fractionAbsorbed(); //fraction of light the is absorbed by canopy, fs in equation
	  
	  //Equation for net biomass accumulation from Monteith
	  float availableCarbon = e * fs * lightPerLeaf;
	  
	  return availableCarbon;
	  
  }
  
  //computes the width of a segment based on the stress on it from descendants
  float computeWidthByStress() {
	  //stress from gravity
	  Vector3 v = centerOfMass().minus(start); 
	  float l = calculateDistance(v);
	  float vxy = calculateXY(v); 
	  float xF = (float) ((computeMassWithDescendants() + getMassOfLeaves()) * sParams.gravity * v.z / l);
	  float zF = (float) ((computeMassWithDescendants() + getMassOfLeaves()) * sParams.gravity * vxy / l);
	  float stressG = computeStress(xF, zF, l, segWidth);
	  
	  //stress from wind
	  float windF = getWindHorizontalForce();
	  Vector3 windCenter = getCenterOfWindForce();
	  Vector3 windV = windCenter.minus(start);
	  float windL = calculateDistance(windV);
	  float windZ = windV.z;
	  float windXF = windF * calculateXY(windV) / windL;
	  float windZF = windF * windZ / windL;
	  float stressW = computeStress(windXF, windZF, windL, segWidth);  
	  
	  //compute total stress
	  float stress = stressW + stressG;
	  float width = stress / tParams.stressProportion(); 
	  return width;
  }
  
  // function that computes the total mass of this segment and everything that sprouts from it
  float computeMassWithDescendants() {
	  if (massWithDescendants == 0) {
		  massWithDescendants = getSegmentMass() + computeDescendantsMass();
	  }
	  return massWithDescendants;
  }
  
  //computes the total dry leaf mass of this segment and everythind descending from it
  float computeTotalDryLeafMass() {
	  float totalDryLeafMass = 0;
	  for (Segment child: children) {
		  totalDryLeafMass += child.computeTotalDryLeafMass();
	  }
	  if (isLeaf()) {
		  totalDryLeafMass += tParams.leafDryMass();
	  }
	  return totalDryLeafMass;
  }
  
//find the horizontal wind force in Newtons
  float getWindHorizontalForce() {
	  if (windHorizontalForceWithDescendants == 0) {
		  float totalForce = 0;
		  if (isLeaf()) {
			  float leafArea = tParams.leafArea();
			  totalForce += 0.5 * tParams.dragCoefficient() * sParams.airDensity * leafArea * sParams.maxWindSpeed * sParams.maxWindSpeed;
		  }
		  for (Segment seg: children)
		  {
			  totalForce += seg.getWindHorizontalForce();
		  }
		  windHorizontalForceWithDescendants = totalForce;
	  }
	  return windHorizontalForceWithDescendants; 
  }
  
  //returns the average location of wind force: equivalent of center of mass, but with forces
  Vector3 getCenterOfWindForce() {
	  if (centerOfWindForceWithDescendants == Vector3.NULL_VECTOR) {
		  float totalForce = 0;
		  Vector3 moment = new Vector3(0,0,0);
		  if (isLeaf()) {
			  moment.add(end.times(getWindHorizontalForce()));
			  totalForce += getWindHorizontalForce();
		  }
		  for (Segment c: children) {
			  float force = c.getWindHorizontalForce();
			  totalForce += force;
			  Vector3 childMoment = c.getCenterOfWindForce().cloneVector();
			  childMoment.scale(force);
			  moment.add(childMoment);
		  }
		  if (totalForce != 0f) {
			  moment.scale(1/totalForce);
		  }
		  centerOfWindForceWithDescendants = moment;
	  }
	  return centerOfWindForceWithDescendants;
  }
  
  //computes the stress given input forces, L and d, in Newtons/ m^2
  float computeStress(float xF, float zF, float l, float d) {
	  float partOne = (float) (1.273 / (d*d));
	  float partTwo = (float) ((8 * l * zF / d) + xF);
	  float stress = Math.abs(partOne * partTwo);
	  return stress;
  }
  
  // function that computes the mass of all descendants
  float computeDescendantsMass() {
	  float m = 0;
	  for (Segment segment : children) 
	  {
			  m += segment.computeMassWithDescendants();
	  }  
	  return m;   
  }
  
  // gets the mass of a single segment
  float getSegmentMass() {
	  float volume = (float) Math.PI * (segWidth / 2) * (segWidth / 2) * getLength();
	  float mass = volume * (900); //900 kg/m3 is the density of oak wood
	  return mass; 
  }
  
  //returns the dry mass of a single segment
  float getSegmentDryMass() {
	  return getSegmentMass() * (1 - tParams.getWoodWaterFraction());
  }
  
  //returns the height of the tree
  float getHighestSegment(float max) {
	  if (end.z > max) {
		  max = end.z;
	  }
	  for (int i = 0; i < children.size(); i++) {
		  max = children.get(i).getHighestSegment(max);
	  }
	  return max;
  }
  
  //calculates the distance of a 3d vector position from the origin
  float calculateDistance (Vector3 v) {
	  float lSquared = (v.x * v.x) + (v.y * v.y) + (v.z * v.z);
	  float l = (float)Math.sqrt(lSquared);
	  return l;
  }
  
  //calculates the hypotenuse of x and y triangle
  float calculateXY (Vector3 v) {
	  float lSquared = (v.x * v.x) + (v.y * v.y);
	  float l = (float)Math.sqrt(lSquared);
	  return l;
  }

  // function that returns an ArrayList of all descendents sprouting from this segment
  ArrayList<Segment> getDescendants() {
    ArrayList<Segment> descendants = new ArrayList<Segment>();
    for (int i = 0; i < children.size(); i++) {
    	descendants.addAll(children.get(i).getDescendantsPlusThis());
    }
    return descendants;
  }
  
  //function that returns an ArrayList of all segments descending from, and including, this one
  ArrayList<Segment> getDescendantsPlusThis() {
	  	ArrayList<Segment> descendants = new ArrayList<Segment>();
	    descendants.add(this);
	    for (int i = 0; i < children.size(); i++) {
	    	descendants.addAll(children.get(i).getDescendantsPlusThis());
	    }
	    return descendants;
 }

  // converts angle measure from degrees to radians
  float radians(float deg) {
    return deg * ((float)Math.PI / 180);
  }

  //returns true if the segment is a leaf
  boolean isLeaf() {
    return age <= tParams.maxLeafAge();
  }

  //checks whether any descendants of this segment have leaves
  boolean descendantHasLeaf() {
    if (isLeaf())
    {
      return true;
    }
    else {
      for (int i = 0; i < children.size(); i++)
      {
        if (children.get(i).descendantHasLeaf())
          return true;
      }
    }
    return false;
  }

  //counts the number of descendants that this segment has
  int countDescendants() {
    int count = 1;
    for (int i = 0; i < children.size(); i++)
    {
      count += children.get(i).countDescendants();
    }
    return count;
  }
  
  //counts the number of leaves descending from this segment
  int countLeaves() {
	  int count = 0;
	  if (isLeaf()) {
		  count = 1;
	  }
	  for (int i = 0; i < children.size(); i++)
	    {
	      count += children.get(i).countLeaves();
	    }
	    return count;
  }

  //draws a single segment, either a leaf or cylindrical wood beam
  	void drawSegment(GL2 gl, GLU glu) {
  		gl.glPushMatrix();
  		if (isLeaf()) {
  	        // Draw leaves. Set material properties.
  			if (sParams.roundLeaves) {
  				float[] rgba = {0f, 0.3f, 0f};
  				gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, rgba, 0);
  				gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, rgba, 0);
  				// Draw the sphere at the right place.
  				gl.glTranslatef((start.x + Simulation.SIZE.x) % Simulation.SIZE.x, 
  						(start.y + Simulation.SIZE.y) % Simulation.SIZE.y, start.z);
  				TreeCanvas.drawSphere(glu, .025f);
  			}
  			else {
  				float[] rgba = {1f, 1f, 1f, 1f};
  	  	        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, rgba, 0);
  	  	        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, rgba, 0);
  	  	        gl.glMultMatrixf(rotateToSegment, 0);
  	  	        
  	  	        TreeCanvas.leafTexture.enable(gl);
  	  	        TreeCanvas.leafTexture.bind(gl);
  	  	        
  	  	        gl.glBegin(GL2.GL_QUADS);
  	  	        
  	  	        gl.glTexCoord2f(0f, 0f); 
  	  	        gl.glVertex3f(-0.05f, 0f, 0f);
  	  	        gl.glTexCoord2f(1f, 0f); 
	  	        gl.glVertex3f(0.05f, 0f, 0f);
	  	        gl.glTexCoord2f(1f, 1f); 
	  	        gl.glVertex3f(0.05f, 0f, 0.15f);
	  	        gl.glTexCoord2f(0f, 1f); 
	  	        gl.glVertex3f(-0.05f, 0f, 0.15f);
	  	        
	  	        gl.glTexCoord2f(0f, 0f); 
	  	        gl.glVertex3f(0f, -0.05f, 0f);
	  	        gl.glTexCoord2f(1f, 0f); 
	  	        gl.glVertex3f(0f, 0.05f, 0f);
	  	        gl.glTexCoord2f(1f, 1f); 
	  	        gl.glVertex3f(0f, 0.05f, 0.15f);
	  	        gl.glTexCoord2f(0f, 1f); 
	  	        gl.glVertex3f(0f, -0.05f, 0.15f);
	  	        
	  	        gl.glTexCoord2f(0f, 0f); 
	  	        gl.glVertex3f(-0.05f, 0f, 0.02f);
	  	        gl.glTexCoord2f(1f, 0f); 
	  	        gl.glVertex3f(0.05f, 0f, -0.02f);
	  	        gl.glTexCoord2f(1f, 1f); 
	  	        gl.glVertex3f(0.05f, 0.15f, -0.02f);
	  	        gl.glTexCoord2f(0f, 1f); 
	  	        gl.glVertex3f(-0.05f, 0.15f, 0.02f);
	  	        
	  	        gl.glTexCoord2f(0f, 0f); 
	  	        gl.glVertex3f(-0.05f, 0f, -0.02f);
	  	        gl.glTexCoord2f(1f, 0f); 
	  	        gl.glVertex3f(0.05f, 0f, 0.02f);
	  	        gl.glTexCoord2f(1f, 1f); 
	  	        gl.glVertex3f(0.05f, -0.15f, 0.02f);
	  	        gl.glTexCoord2f(0f, 1f); 
	  	        gl.glVertex3f(-0.05f, -0.15f, -0.02f);
  	  	        
  	  	        gl.glEnd();
  	  	        
  	  	        TreeCanvas.leafTexture.disable(gl);
  			}
      	} else {
      		//Draw wood.
  	        float[] rgba = {0.3f, 0.2f, 0.075f};
  	        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, rgba, 0);
  	        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, rgba, 0);
  	        gl.glMultMatrixf(rotateToSegment, 0);
  	        TreeCanvas.drawCylinder(glu, getLength(), getRadius());
      	}
  		gl.glPopMatrix();
  	}
  
  	//draws all segments in the tree
  void drawSegments(GL2 gl, GLU glu) {
	  drawSegment(gl, glu);
	  for (int i = 0; i < children.size(); i++)
		  children.get(i).drawSegments(gl, glu);
  }
  
  //end of the segment
  Vector3 end() {
	  return end;
  }

  //age of the segment
  int getAge() {
	  return age;
  }

  //length of the segment
  float getLength() {
	  return tParams.segmentLength();
  }
  
  //returns the center of mass of this segment plus all of its descendants
  Vector3 centerOfMass() {
	  if (centerOfMassWithDescendants == Vector3.NULL_VECTOR) {
		  float totalMass = getSegmentMass();
		  Vector3 moment = start.plus(end);
		  moment.scale(0.5f*getSegmentMass());
		  for (Segment c: children) {
			  float mass = c.computeMassWithDescendants() + c.getMassOfLeaves();
			  totalMass += mass;
			  Vector3 childMoment = c.centerOfMass().cloneVector();
			  childMoment.scale(mass);
			  moment.add(childMoment);
		  }
		  moment.scale(1 / totalMass);
		  centerOfMassWithDescendants = moment;
	  }
	  return centerOfMassWithDescendants;
  }

  //width/diameter of the segment
  float getWidth() {
	  return segWidth;
  }
  
  //radius of the segment
  float getRadius() {
	  return segWidth / 2;
  }
}

