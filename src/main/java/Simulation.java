import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;


//class in which an environment for the tree to grow is created, and a list of trees is created to grow in the environment
public class Simulation {
	public static Vector3 SIZE; //size of the box in which the trees will grow in meters
	public static Random randomGenerator = new Random();
	ArrayList<Tree> trees = new ArrayList<Tree>(); //list of trees
	VoxelGrid voxels; //3d index of space
	static Simulation simulation;
	static TreeCanvas canvas; //graphics
	static SimulationParams simParams; //environmental parameters and simulation parameters
	static TreeParams treeParams; //parameters for individual trees
	
	static double random() {
		return randomGenerator.nextDouble();
	}

	// Create a new simulation. Set the number of trees in the x and y directions to have equal spacings
	Simulation(SimulationParams sParams) {
		SIZE = simParams.size;
		treeParams = TreeParams.createGeneralDeciduousTreeParams();
		//treeParams = TreeParams.createAmericanSycamoreTreeParams();
		//treeParams = TreeParams.createSugarMapleTreeParams();
		voxels = new VoxelGrid(simParams);	
		float incrementX = SIZE.x / simParams.numTreesX;
		float incrementY = SIZE.y / simParams.numTreesY;
		for (int i = 0; i < simParams.numTreesX; i++) {
			for (int j = 0; j < simParams.numTreesY; j++) {
				float xCoor = (i * incrementX) + (incrementX/2);
				float yCoor = (j * incrementY) + (incrementY/2);
				trees.add(new Tree(simParams, treeParams, voxels, xCoor, yCoor));
			}
		}
		
	}

	//updates all trees in the simulation and the light
	void update() {
		for (Tree tree: trees) {
			tree.update();
		}
		if (trees.get(0).age <= treeParams.maxAge()) {
			voxels.updateLight();
		}
	}
	
	//grows one tree and continues rotating
	static void growOnceAndRotate() {
		while (true) {
        	simulation.update();
        }
	}
	
	// Grows a single tree, but always the same one.
	static void growSameTree() {
		randomGenerator = new Random(13);
		while (true) {
        	simulation.update();
        }
	}
	
	//grows a single tree and then stops simulation
	static void growOnceAndStop() {
		for (int i = 0; i < treeParams.maxAge() + 20; i++) {
			simulation.update();
		}		
	}
	
	//grows trees repeatedly
	static void growOverAndOver() {
		while (true) {
			simParams = SimulationParams.createSimulationParamsEquator();
			simulation = new Simulation(simParams);
			canvas.setSimulation(simulation);
        	simulation.growOnceAndStop();
        }
	}
	

	//runs a demo mode; repeatedly grows trees in changing conditions
	 static void demo() { 
		while (true) {
			for(int i = 0; i < 5; i++) {
				simParams = SimulationParams.createSimulationParamsEquator();
				if (i == 0) {
					simParams.cameraDistance = 1;
					simParams.roundLeaves = true;
				}
				if (i == 1) {
					simParams.cameraDistance = 1;
					simParams.roundLeaves = false;
				}
				if (i == 2) {
					simParams.cameraDistance = 1;
					simParams.roundLeaves = false;
					float[] light = {0.1f, 0.6f, 0.1f, 0.1f, 0.1f};
					simParams.startLightDirections = light;
				}
				if (i == 3) {
					simParams.cameraDistance = 1;
					simParams.roundLeaves = false;
					simParams.maxWindSpeed = 30;
				}
				if (i == 4) {
					simParams.numTreesX = 3;
					simParams.numTreesY = 3;
					simParams.roundLeaves = false;
					simParams.cameraDistance = 2;
				}
				simulation = new Simulation(simParams);
				canvas.setSimulation(simulation);
				simulation.growOnceAndStop();
			}
			
		}
	}
	 
	 //runs an experiment that grows trees at 15 different spacings, repeats 10 times, and records the total mass of all the trees
	 static void experimentMaximizeCarbonUptake() { 
		 for (int j = 0; j < 10; j++) {
			 for(int i = 1; i <= 15; i++) {
				 simParams = SimulationParams.createSimulationParamsEquator();
				 simParams.cameraDistance = 2;
				 simParams.numTreesX = i;
				 simParams.numTreesY = i;
				 simulation = new Simulation(simParams);
				 canvas.setSimulation(simulation);
				 simulation.growOnceAndStop();
				 float totalUptake = 0;
				 for (Tree tree: simulation.trees) {
					 totalUptake += tree.totalMass();
				 }
				 System.out.println(i + " " + totalUptake);
			 }
		 }
	 }
	 
	 //runs and experiment that grows trees at 15 different spacings, repeats 10 times, and records total mass of usable timber (diameter > 13 cm)
	 static void experimentMaximizeTimberProduction() { 
		 for (int j = 0; j < 10; j++) {
			 for(int i = 1; i <= 15; i++) {
				 simParams = SimulationParams.createSimulationParamsEquator();
				 simParams.cameraDistance = 2;
				 simParams.numTreesX = i;
				 simParams.numTreesY = i;
				 simulation = new Simulation(simParams);
				 canvas.setSimulation(simulation);
				 simulation.growOnceAndStop();
				 float totalTimberMass = 0;
				 for (Tree tree: simulation.trees) {
					 totalTimberMass += tree.timberMass();
				 }
				 System.out.println(i + " " + totalTimberMass);
			 }
		 }
	 }
	
	
	//will only work if there is one tree in simulation, prints the center of mass of the tree after 60 years of growth
	static void experiment30TreesPrintCOM() {
		simParams = SimulationParams.createSimulationParamsEquator();
		for(int i = 0; i < 30; i++) {
			simulation = new Simulation(simParams);
			canvas.setSimulation(simulation);
        	simulation.growOnceAndStop();
        	simulation.trees.get(0).printCOM();
        }
	}
	
	//will only work if there is one tree in simulation, prints the height/diameter relationship of 30 trees after 60 years of growth
	static void experiment30TreesPrintHeightDiameterRelationship() {
		simParams = SimulationParams.createSimulationParamsEquator();
		for(int i = 0; i < 30; i++) {
			simulation = new Simulation(simParams);
			canvas.setSimulation(simulation);
        	simulation.growOnceAndStop();
        	simulation.trees.get(0).printHeightDiameterRelationship();
        }
	}
	
	//will only work if there is one tree in simulation
	//prints the constant of proportionality between final mass of leaves and total mass
	static void experiment30TreesPrintMlMsRelationship() {
		simParams = SimulationParams.createSimulationParamsEquator();
		for(int i = 0; i < 30; i++) {
			simulation = new Simulation(simParams);
			canvas.setSimulation(simulation);
        	simulation.growOnceAndStop();
        	simulation.trees.get(0).printMlMsRelationship();
        }
	}
	
	//will only work if there is one tree in simulation, prints the relationship between total mass
	//and mass of leaves for 30 trees after 60 years of growth
	static void experiment30TreesPrintMtMlRelationship() {
		simParams = SimulationParams.createSimulationParamsEquator();
		for(int i = 0; i < 30; i++) {
			simulation = new Simulation(simParams);
			canvas.setSimulation(simulation);
        	simulation.growOnceAndStop();
        	simulation.trees.get(0).printMtMlRelationship();
        }
	}
	
	//draws the entire simulation
	void draw(GL2 gl, GLU glu) {
		for (Tree tree : trees) {
			tree.draw(gl, glu);
		}
		voxels.drawShadow(gl, glu);
	}
	
	//main function
    public final static void main(String[] args) { 
		simParams = SimulationParams.createSimulationParamsEquator();
		simulation = new Simulation(simParams);
		canvas = TreeCanvas.createCanvas(simulation);
    	//growOnceAndRotate();
    	//growOverAndOver();
		//growSameTree();
		//experimentMaximizeTimberProduction();
		demo();
		//experimentMaximizeCarbonUptake();
		//experiment30TreesPrintCOM();
		//experiment30TreesPrintMlMsRelationship();
		//experiment30TreesPrintHeightDiameterRelationship();
		//experiment30TreesPrintMtMlRelationship();
    }
}
