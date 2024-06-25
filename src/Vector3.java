
import javax.vecmath.Vector3f;

//3D Vector class that extends a premade one and gives additional function
public class Vector3 extends Vector3f {
	
    /** Serial version UID, to avoid warning. */
    private static final long serialVersionUID = 1L;

    public static final Vector3 NULL_VECTOR = new Vector3(0, 0, 0);
    public static final Vector3 X_VECTOR = new Vector3(1, 0, 0);
    public static final Vector3 Y_VECTOR = new Vector3(0, 1, 0);
    public static final Vector3 Z_VECTOR = new Vector3(0, 0, 1);    
	
    public Vector3() {
    	super(0f, 0f, 0f);
    }
    
	public Vector3(Vector3f in) {
		super(in.x, in.y, in.z);
	}
	
	public Vector3(float xIn, float yIn, float zIn) {
		super(xIn, yIn, zIn);
	}
	
	public Vector3(double d, double e, double f) {
		super((float)d, (float)e, (float)f);
	}

	public Vector3 cloneVector() {
		return (Vector3) clone();
	}
	
	public Vector3 plus(Vector3f v2) {
		return new Vector3(x + v2.x, y + v2.y, z + v2.z);
	}
	
	public Vector3 minus(Vector3f v2) {
		return new Vector3(x - v2.x, y - v2.y, z - v2.z);
	}
	
	public Vector3 times(float f) {
		return new Vector3(x * f, y * f, z * f);
	}
	
	public Vector3 dividedBy(float f) {
		return times(1/f);
	}
	
	public Vector3 crossedWith(Vector3f v2) {
		Vector3 v3 = new Vector3();
		v3.cross(this, v2);
		return v3;
	}
	
	public static Vector3 zeroVector() {
		return NULL_VECTOR;
	}
	
	public static float distance(Vector3f v1, Vector3f v2) {
		return (float) Math.sqrt(distanceSquared(v1, v2));
	}
	
	public static float distanceSquared(Vector3f v1, Vector3f v2) {
		float dx = v1.x - v2.x;
		float dy = v1.y - v2.y;
		float dz = v1.z - v2.z;
		return dx * dx + dy * dy + dz * dz;
	}
	
	public Vector3 onePerpendicular() {
		Vector3 someVector = (x != 0 || y != 0 ? Z_VECTOR : X_VECTOR);
		return crossedWith(someVector);
	}
}
