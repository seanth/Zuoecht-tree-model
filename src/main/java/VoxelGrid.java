import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

//3D index of space that is used for light propagation and prevention of intersections
public class VoxelGrid {
	final int DOWN = 0;
	final int NORTH = 1;
	final int SOUTH = 2;
	final int EAST = 3;
	final int WEST = 4;
	final boolean DIRECTIONAL = true; //true if directional, false if nondirectional
	final float[] startLight = SimulationParams.startLightDirections;

	Vector3 size; //number of cells in the voxelgrid in each dimension
	float CELL_SIZE; //meters
	float lightFactor = 0.15f;//used in nondirectional
	int maxZ = 1; //maximum z coordinate that light needs to be updated from

	//pixel of volume in the voxel grid
	private class Voxel {
		boolean occupied = false;
		float lightIn = 0.0f;
		float lightOut = 0.0f;
		float directionalLightIn[];
		float directionalLightOut[];
		Voxel() {
			if (DIRECTIONAL)
			{
				directionalLightIn = new float[5];
				directionalLightOut = new float[5];
			}
		}
	}

	private Voxel[][][] voxels;

	//creates a voxel grid
	VoxelGrid(SimulationParams sParams) {
		CELL_SIZE = sParams.vGridCellSize;
		size = Simulation.SIZE.dividedBy(CELL_SIZE);
		voxels = new Voxel[(int)size.x + 1][(int)size.y + 1][(int)size.z + 1];
		for (int i = 0; i < size.x; i++)
		{
			for (int j = 0; j < size.y; j++)
			{
				for (int k = 0; k < size.z; k++)
				{  
					voxels[i][j][k] = new Voxel();
					
				}
			}
		}
		//initially sets light to be available at all unupdated points to prevent problems with multiple growth cycles
		maxZ = (int)size.z - 2;
		updateLight();
		maxZ = 1;
	}

	//given a point in 3D space, find the cell in which that point is found
	Vector3 findCell(Vector3 point) {
		Vector3 cell = point.cloneVector();
		cell.scale(1 / CELL_SIZE);
		float x = (cell.x + size.x) % size.x;
		float y = (cell.y + size.y) % size.y;
		return new Vector3(x,y,cell.z);
	}

	//given a cell, return a point that is found in that cell
	Vector3 cellToPoint(Vector3 cell)
	{
		Vector3 point = cell.times(CELL_SIZE);
		return point;
	}

	//updates the light by setting light in top layer to environmental conditions and hen propagating it through canopy
	void updateLight() {
		for (int i = 0; i < size.x; i++) //initialize light going in and out of top layer to 1
		{
			for (int j = 0; j < size.y; j++)
			{
				if (DIRECTIONAL) {
					for (int k = 0; k < 5; k++) {
						voxels[i][j][maxZ + 1].directionalLightIn[k] = startLight[k];
						voxels[i][j][maxZ + 1].directionalLightOut[k] = startLight[k];
					}
				}
				else {
					voxels[i][j][maxZ + 1].lightIn = 1.0f;
					voxels[i][j][maxZ + 1].lightOut = 1.0f;
				}
			}
		}
		//propagate the light downward
		for (int q = maxZ; q >= 0; q--)
		{
			for (int r = 0; r < size.x; r++)
			{
				for (int s = 0; s < size.y; s++)
				{
					Voxel v = voxels[r][s][q];	
					float lightHere;
					float lightOutOfHere;
					//light straight down, light in cell is function of the light in 5 cells above
					if (!DIRECTIONAL) {
						float LA = voxels[r][s][q+1].lightOut; //light directly above
						float LAR = voxels[(r+1) % ((int)(size.x))][s][q+1].lightOut; //light above and to the right
						float LAL = voxels[(r-1+((int)(size.x))) % ((int)(size.x))][s][q+1].lightOut; //light above and to the left
						float LAB = voxels[r][(s+1) % ((int)(size.y))][q+1].lightOut; //light above and behind
						float LAF = voxels[r][(s-1+((int)(size.y))) % ((int)(size.y))][q+1].lightOut; //light above and in front of
						lightHere = (LA * (1-(4*lightFactor))) + (lightFactor * LAR) + (lightFactor * LAL) + (lightFactor * LAB) +(lightFactor * LAF);
					}        
					//directional light
					else {
						v.directionalLightIn[DOWN] = voxels[r][s][q+1].directionalLightOut[DOWN];
						v.directionalLightIn[NORTH] = voxels[r][(s+1) % ((int)(size.y))][q+1].directionalLightOut[NORTH];
						v.directionalLightIn[SOUTH] = voxels[r][(s-1+((int)(size.y))) % ((int)(size.y))][q+1].directionalLightOut[SOUTH];
						v.directionalLightIn[EAST] = voxels[(r+1) % ((int)(size.x))][s][q+1].directionalLightOut[EAST];
						v.directionalLightIn[WEST] = voxels[(r-1+((int)(size.x))) % ((int)(size.x))][s][q+1].directionalLightOut[WEST];
					}

					if (v.occupied)
					{
						if (DIRECTIONAL) {
							for (int w = 0; w < 5; w++) {
								v.directionalLightOut[w] = (float)(.2 * v.directionalLightIn[w]);
							}
						}
						else {
							lightOutOfHere = (float)(.2 * lightHere); //if the cell here is full, only let some of the light through the canopy (other half
							//will be absorbed and/or used in photosynthesis)
						}
					}
					else
					{
						if (DIRECTIONAL) {
							for (int w = 0; w < 5; w++) {
								v.directionalLightOut[w] = v.directionalLightIn[w];
							}
						}
						else {
							lightOutOfHere = lightHere;    //if the cell is not taken, then nothing is absorbing light so all light will exit cell       
						}
					}
					if (!DIRECTIONAL) {
						voxels[r][s][q].lightIn = lightHere;
						voxels[r][s][q].lightOut = lightOutOfHere;
					}
					else {
						float lightI = 0;
						for (int h = 0; h < 5; h++) {
							lightI += v.directionalLightIn[h];
						}
						v.lightIn = lightI;
						float lightO = 0;
						for (int h = 0; h < 5; h++) {
							lightO += v.directionalLightOut[h];
						}
						v.lightOut = lightO;
					}
				}
			}
		}
	}

//get the amount of light in a given cell
double getLight (Vector3 point) {
  Vector3 cell = findCell(point);
  return voxels[(int)cell.x][(int)cell.y][(int)cell.z].lightIn;
}

//check if the cell at given point is full
boolean cellFull( Vector3 point ) {
  Vector3 cell = findCell(point);
  if (cell.z >= size.z || cell.z <= 0)
    return true;
  return voxels[(int)cell.x][(int)cell.y][(int)cell.z].occupied;
}

//set the cell in voxel grid to be full/empty
void setCellFull(Vector3 point, boolean full ) {
  Vector3 cell = findCell(point);
  voxels[(int)cell.x][(int)cell.y][(int)cell.z].occupied = full;
    maxZ = Math.max(maxZ, (int)cell.z);
}

//draw the shadow of the tree on the ground based on the amount of light left at the ground
void drawShadow(GL2 gl, GLU glu) {
	gl.glDisable(GL2.GL_LIGHTING);
	for (int i = 0; i < size.x; i++)
	{
		for (int j = 0; j < size.y; j++)
		{
			gl.glPushMatrix();
			float lightHere = voxels[i][j][0].lightIn;
	        gl.glColor3f(lightHere, lightHere, lightHere);
	        Vector3 corner = cellToPoint(new Vector3(i, j, 0));
	        gl.glTranslatef(corner.x, corner.y, corner.z);
	        gl.glBegin(GL2.GL_QUADS);
	        gl.glVertex3f(0f, 0f, 0f);
	        gl.glVertex3f(0f, CELL_SIZE, 0f);
	        gl.glVertex3f(CELL_SIZE, CELL_SIZE, 0f);
	        gl.glVertex3f(CELL_SIZE, 0f, 0f);
	        gl.glEnd();
			gl.glPopMatrix();
		}
	}
}

}