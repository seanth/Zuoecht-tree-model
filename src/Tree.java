import java.util.ArrayList;
import java.util.Collections;

//updating imports.
//2024-0625 STH
//import javax.media.opengl.GL2;
//import javax.media.opengl.glu.GLU;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;


//A tree, or collection of segments
public class Tree {

	Segment root; 
	VoxelGrid voxels;
	public int age; // years
	double totalStemMass; // kg/m3
	double totalMass;
	double totalLeafMass; // kg/m3
	TreeParams tParams;
	SimulationParams simParams;

	Tree(SimulationParams sParamsIn, TreeParams paramsIn, VoxelGrid voxelsIn, float x, float y) {
		simParams = sParamsIn;
		tParams = paramsIn;
		voxels = voxelsIn;
		root = new Segment(null, new Vector3(x, y, 0), new Vector3(0, 0, tParams.segmentLength()), tParams, simParams); 
		age = 0;																					
	}

	// updates everything related to the tree (can be changed to print certain statistics, if statistics are wanted on every tick)
	void update() {
		if (age >= tParams.maxAge())
			return;
		age++;
		if (age < 10) {
			tParams.branchAngleFactor = tParams.youngBranchAngleFactor();
		}
		else {
			tParams.branchAngleFactor = tParams.oldBranchAngleFactor();
		}
		float carbonBudget = root.computeCarbonBudget(voxels);
		//System.out.println("Initial Carbon Budget: " + carbonBudget);
		ArrayList<Segment> segmentsByAge = root.getDescendantsPlusThis(); //list of the segments sorted by age, allows update to update based on depth in tree rather than randomly
		for (Segment s : segmentsByAge) {
			s.resetValues();
			s.ageOneYear();
		}
		
		//for (int i = 0; carbonBudget > 0; i++) { //multiple growth cycles in one year, currently not using
			carbonBudget = root.updateSegmentWidth(voxels, carbonBudget); // updates the rootSegment and recursively updates all of its children as well
			//System.out.println("Carbon Budget after growing width " + i + ": " + carbonBudget);
			segmentsByAge = root.getDescendantsPlusThis();
			Collections.sort(segmentsByAge, new CompareSegmentAge());
			for (Segment s : segmentsByAge) {
				if (carbonBudget > 0) {
					carbonBudget = s.updateSegmentBranching(voxels, carbonBudget);
				}
				else
					break;
			}
			//System.out.println("Carbon Budget after branching " + i + ": " + carbonBudget);
		//}
		for (Segment s : segmentsByAge) {
			s.resetValues();
		}

		totalStemMass = root.computeMassWithDescendants();
		totalLeafMass = root.getMassOfLeaves();
		totalMass = totalStemMass + totalLeafMass;		
		//System.out.println(totalMass);
		//int segments = root.countDescendants();
		//int leaves = root.countLeaves();
		//if(age > 30) //prints the total mass, mass of leaves, and appropriate ratio for tree depending on its age
		//	printMtMlRelationship();
		//else
		//	printMtMlYoungRelationship();
		//printAgeHeightDiameterTotalMass();
		//printHeightDiameterRelationship();
		//System.out.println(leaves); //prints the number of leaves
		//System.out.println(segments); //prints the number of segments
	}
	
	//prints the center of mass
	void printCOM() {
		System.out.println("Center of Mass: " + root.centerOfMass() + ".");
	}
	
	//prints the wind force and the center of the wind force
	void printWindForceAndCenter() {
		System.out.println("  WindForce: " + root.getWindHorizontalForce() + ". Wind Center: " + root.getCenterOfWindForce());
	}
	
	//prints the actual mass of leaves, predicted mass of leaves (using B3Ms^a3), and ratio of predicted/actual
	void printMlMsRelationship() {
		System.out.print("Actual Mass of Leaves: " + totalLeafMass + ". ");
		float predicted = (float)(0.25 * Math.pow(totalStemMass, 0.73));
		System.out.print("Predicted Mass of Leaves: " + predicted + ".");
		System.out.print("Ratio: " + (predicted/totalLeafMass) + ".");
		System.out.print(totalLeafMass + " ");
		System.out.print(predicted + " ");
		System.out.print(predicted/totalLeafMass);
		System.out.println();
	}
	
	//prints the total mass, mass of leaves, and appropriate ratio between the two (exponent on totalMass: 0.75)
	void printMtMlRelationship() {
		System.out.print(totalMass + " ");
		System.out.print(totalLeafMass + " ");
		System.out.print(Math.pow(totalMass, 0.75)/totalLeafMass);
		System.out.println();
	}
	
	//prints the total mass, mass of leaves, and appropriate ratio between the two (exponent on totalMass: 1.0)
	void printMtMlYoungRelationship() {
		System.out.print(totalMass + " ");
		System.out.print(totalLeafMass + " ");
		System.out.print(totalMass/totalLeafMass);
		System.out.println();
	}
	
	//prints the tree height, tree diameter, and appropriate ratio between the two (Diameter exponent: 2/3)
	void printHeightDiameterRelationship() {
		float treeHeight = root.getHighestSegment(0);
		float treeDiameter = root.segWidth;
		System.out.print(age + " ");
		System.out.print(treeHeight + " ");
		System.out.print(treeDiameter + " ");
		float proportion = (float) (treeHeight / (Math.pow(treeDiameter, 2/3.0)));
		System.out.print(proportion);
		System.out.println();
	}
	
	//prints the tree age, height, diameter, and total mass 
	void printAgeHeightDiameterTotalMass() {
		float treeHeight = root.getHighestSegment(0);
		float treeDiameter = root.segWidth;
		System.out.print(age + " ");
		System.out.print(treeHeight + " ");
		System.out.print(treeDiameter + " ");
		System.out.print(totalMass);
		System.out.println();
	}
	

	// draws the tree
	void draw(GL2 gl, GLU glu) {
		root.drawSegments(gl, glu);
	}
	
	//returns totalMass of the tree
	float totalMass() {
		return (float)totalMass;
	}

	//test method: prints the ages of the segments in a list
	void printAges (ArrayList<Segment> s) {
		for (int i = 0; i < s.size(); i ++) {
			System.out.print(s.get(i).getAge() + ", ");
		}
		System.out.println();
	}
	
	//returns the amount of usable timber from a tree
	public float timberMass() {
		float mass = 0;
		ArrayList<Segment> segments = root.getDescendantsPlusThis();
		for (Segment s: segments) {
			if (s.getWidth() > simParams.timberDiameter) {
				mass += s.getSegmentMass();
			}
		}
		return mass;
	}


}
