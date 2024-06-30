//stores parameters for a simulation, including environmental factors and graphics preferences
public class SimulationParams {
	
	public static float maxWindSpeed; //m/s
	public static float gravity; //m/s^2
	public static float totalSunlightIncident; //on a cubic meter in one year, in MJ
	public static float airDensity; //kg/m3
	public static float[] startLightDirections = {0.6f, 0.1f, 0.1f, 0.1f, 0.1f}; //proportion of light coming from each direction (DNSEW)
	public static int cameraDistance;
	//1: close up (for single deciduous trees)
	//2: middle range (for forests of normal deciduous trees)
	//3: far away (for very large trees, ex. American Sycamore)
	public static boolean roundLeaves; //true if round, false if pretty
	public static int numTreesX; //number of trees in x direction
	public static int numTreesY; //number of trees in y direction
	public static float timberDiameter; //minimum diameter of wood to use for timber
	public Vector3 size; //size of simulation
	public float vGridCellSize; //size of cells in voxel grid

	private SimulationParams () {
		
	}
	
	public static SimulationParams createSimulationParamsEquator() {
		SimulationParams s = new SimulationParams();
		s.size = new Vector3(10,10,7);
		s.vGridCellSize = .08f;
		float[] light = {0.6f, 0.1f, 0.1f, 0.1f, 0.1f}; 
		// s.maxWindSpeed = 20.0f; 
		// s.gravity = 9.8f; 
		// s.totalSunlightIncident = 6636f; 
		// s.airDensity = 1.2f; 
		// s.numTreesX = 1; 	
		// s.numTreesY = 1; 
		// s.roundLeaves = false; 
		// s.cameraDistance = 1; 
		// s.startLightDirections = light;
		// s.timberDiameter = .13f;
		// static field should be accessed in a static way. STH 0630-2024
		SimulationParams.maxWindSpeed = 20.0f; 
		SimulationParams.gravity = 9.8f; 
		SimulationParams.totalSunlightIncident = 6636f; 
		SimulationParams.airDensity = 1.2f; 
		SimulationParams.numTreesX = 1; 	
		SimulationParams.numTreesY = 1; 
		SimulationParams.roundLeaves = false; 
		SimulationParams.cameraDistance = 1; 
		SimulationParams.startLightDirections = light;
		SimulationParams.timberDiameter = .13f;


		return s;
	}

}
