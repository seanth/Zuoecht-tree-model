
public class TreeParams {
	// Size of random vector added to unit vector of parent segment's direction
	// to determine direction of child segment.
	public float branchAngleFactor;
	private float youngBranchAngleFactor;
	private float oldBranchAngleFactor;
	private float segmentLength; //meters
	private float woodDensity; //kg/m3
	private float dragCoefficient;
	private float massOfLeaf; //kg
	private float leafArea; //m^2
	private float fractionAbsorbed; //fraction of light absorbed by the canopy, fs
	private float efficiency; //efficiency of leaves, e
	private float rootApportion; //proportion of carbon that goes towards roots
	private int maxAge; //maximum age of tree in years
	private float woodWaterFraction; //fraction of wood that is water
	private float leafDryMass; //dry mass of a leaf
	private float stressProportion; //inverse proportion of stress that tree will grow in width
	private int maxLeafAge; //number of seasons that leaves regenerate themselves (number of layers of leaves in the tree)
	
	//constructor: private so only created by functions below
	private TreeParams () {
		
	}
	
	//treeParams for a general deciduous tree
	public static TreeParams createGeneralDeciduousTreeParams() {
		TreeParams t = new TreeParams();
		t.youngBranchAngleFactor = 0.01f;
		t.oldBranchAngleFactor = 0.05f;
		t.segmentLength = .1f;
		t.dragCoefficient = 0.5f;
		t.woodDensity = 900; //oak
		t.massOfLeaf = 0.0096f;  
		t.leafArea = 0.0032f;  
		t.fractionAbsorbed = 0.8f;
		t.efficiency = .0015f; 
		t.rootApportion = 0.4f; 
		t.maxAge = 60; //years
		t.woodWaterFraction = 0.59f; //fraction of wood that is water
		t.leafDryMass = 0.00192f; //fraction of leaf that is water: 0.8. 
		t.stressProportion = 950000000;
		t.maxLeafAge = 1;
		return t;
	}
	
	//treeParams for American Sycamore (Platanus occidentalis)
	public static TreeParams createAmericanSycamoreTreeParams() {
		TreeParams t = new TreeParams();
		t.youngBranchAngleFactor = 0.05f;
		t.oldBranchAngleFactor = 0.05f;
		t.segmentLength = .2f;
		t.dragCoefficient = 0.5f;
		t.woodDensity = 460; 
		t.massOfLeaf = 0.024f;  
		t.leafArea = 0.08f;  //8 leaves
		t.fractionAbsorbed = 0.8f;
		t.efficiency = .0015f; 
		t.rootApportion = 0.4f; 
		t.maxAge = 100; //years
		t.woodWaterFraction = 0.59f; //fraction of wood that is water
		t.leafDryMass = 0.0048f; //fraction of leaf that is water: 0.8. 
		t.stressProportion = 950000000;
		t.maxLeafAge = 2;
		return t;
	}
	
	//treeParams for Sugar Maple (Acer saccharum)
	public static TreeParams createSugarMapleTreeParams() {
		TreeParams t = new TreeParams();
		t.youngBranchAngleFactor = 0.05f;
		t.oldBranchAngleFactor = 0.05f;
		t.segmentLength = .2f;
		t.dragCoefficient = 0.5f;
		t.woodDensity = 676; 
		t.massOfLeaf = 0.012f;  
		t.leafArea = 0.04f;  //4 leaves
		t.fractionAbsorbed = 0.8f;
		t.efficiency = .0015f; 
		t.rootApportion = 0.4f; 
		t.maxAge = 100; //years
		t.woodWaterFraction = 0.59f; //fraction of wood that is water
		t.leafDryMass = 0.0024f; //fraction of leaf that is water: 0.8. 
		t.stressProportion = 950000000;
		t.maxLeafAge = 2;
		return t;
	}
	
	//accessing methods
	
	public float youngBranchAngleFactor() {
		return youngBranchAngleFactor;
	}
	
	public float oldBranchAngleFactor() {
		return oldBranchAngleFactor;
	}
	
	public float branchAngleFactor() {
		return branchAngleFactor;
	}
	
	public int maxLeafAge() {
		return maxLeafAge;
	}
	
	public float stressProportion() {
		return stressProportion;
	}
	
	public float leafDryMass() {
		return leafDryMass;
	}
	
	public float getWoodWaterFraction() {
		return woodWaterFraction;
	}
	
	public int maxAge() {
		return maxAge;
	}
	
	public float rootApportion() {
		return rootApportion;
	}
	
	public float fractionAbsorbed() {
		return fractionAbsorbed;
	}
	
	public float efficiency() {
		return efficiency;
	}
	
	public float leafArea() {
		return leafArea;
	}
	
	public float massOfLeaf() {
		return massOfLeaf;
	}
	
	public float segmentLength() {
		return segmentLength;
	}
	
	public float woodDensity() {
		return woodDensity;
	}
	
	public float dragCoefficient() {
		return dragCoefficient;
	}
}
