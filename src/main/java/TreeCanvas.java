import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;

import javax.media.opengl.DebugGL2;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.JFrame;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;


  //A 3D Canvas to draw/display trees.
 
public class TreeCanvas extends GLCanvas implements GLEventListener, MouseMotionListener, MouseListener, GLAutoDrawable{

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The GL unit (helper class). */
    private GLU glu;

    /** The simulation to draw.  */
    private Simulation simulation;
    
    private boolean isRotating;

    /** The current camera angle. */
	double cameraRotation = 0.0f;
	
	public static Texture leafTexture; //texture for the leaf
	
	private FPSAnimator animator;

	/**
     * A new TreeCanvas
     * 
     * @param capabilities The requested GL capabilities.
     * @param width The window width.
     * @param height The window height.
     */
	
    public TreeCanvas(int width, int height, Simulation sim) {
    	simulation = sim;
    	addGLEventListener(this);
    	isRotating = true;
    	addMouseListener(this);
    }
    
    public void setSimulation(Simulation s) {
    	simulation = s;
    }

    /**
     * Sets up the screen.
     * 
     * @see javax.media.opengl.GLEventListener#init(javax.media.opengl.GLAutoDrawable)
     */
    public void init(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();
        drawable.setGL(new DebugGL2(gl));

        // Enable z- (depth) buffer for hidden surface removal. 
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);

        // Enable smooth shading and blending.
        gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL2.GL_ALPHA_TEST);
        gl.glAlphaFunc(GL.GL_GREATER, 0.1f);

        // Define "clear" color.
        gl.glClearColor(0.8f, 0.8f, 0.8f, 0f);

        // We want a nice perspective.
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);

        // Create GLU.
        glu = new GLU();
        
        try {
        	InputStream stream = getClass().getResourceAsStream("leaf128.png");
        	TextureData data = TextureIO.newTextureData(gl.getGLProfile(), stream, false, TextureIO.PNG);
        	leafTexture = TextureIO.newTexture(data);
        }
        catch (IOException e) {
        	e.printStackTrace();
        	System.exit(1);
        }
    }

    /**
     * The only method that you should implement by yourself.
     * 
     * @see javax.media.opengl.GLEventListener#display(javax.media.opengl.GLAutoDrawable)
     */
    public void display(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();

        // Clear screen.
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        // Set camera and light sources.
        setCamera(gl, glu);
        createLights(gl, glu);
        
        // Set common material properties for everything.
        gl.glMaterialf(GL.GL_FRONT, GL2.GL_SHININESS, 0.5f);

        // Draw simulation.
        simulation.draw(gl, glu);
    }
    
    /**
     * Set the camera pointing at the center of the tree growing region.
     * @param gl The GL context.
     * @param glu The GL utility.
     * @param distance The distance from the screen.
     */
    private void setCamera(GL glIn, GLU glu) {
        final GL2 gl = glIn.getGL2();
        // Change to projection matrix.
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        // Perspective.
        float widthHeightRatio = (float) getWidth() / (float) getHeight();
        glu.gluPerspective(45, widthHeightRatio, 1, 1000);
        if (isRotating) {
            cameraRotation += .03;
        }
        float halfX = Simulation.SIZE.x / 2;
        float halfY = Simulation.SIZE.y / 2;
        if (SimulationParams.cameraDistance == 1) {
            glu.gluLookAt(halfX + 8 * Math.sin(cameraRotation),
                	halfY + 8 * Math.cos(cameraRotation), 1.5,  // Eye location.
                	halfX, halfY, 2,  // Look at point.
                	0, 0, 1);  // Up direction.
        }
        else if (SimulationParams.cameraDistance == 2) {
            glu.gluLookAt(halfX + 16 * Math.sin(cameraRotation),
                	halfY + 16 * Math.cos(cameraRotation), 3,  // Eye location.
                	halfX, halfY, 2,  // Look at point.
                	0, 0, 1);  // Up direction.
        }
        else if (SimulationParams.cameraDistance == 3) {
        	glu.gluLookAt(halfX + 48 * Math.sin(cameraRotation),
                	halfY + 48 * Math.cos(cameraRotation), 15,  // Eye location.
                	halfX, halfY, 4,  // Look at point.
                	0, 0, 1);  // Up direction.
        }
        
        // Change back to model view matrix.
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }
    
    // Sets up the lighting of the tree.
    void createLights(GL2 gl, GLU glu) {
        // Prepare light parameters.
        float SHINE_ALL_DIRECTIONS = 1f;
        float[] lightPos = {10, 0, 20, SHINE_ALL_DIRECTIONS};
        float[] lightColorAmbient = {0.8f, 0.8f, 0.8f, 1f};
        float[] lightColorSpecular = {1f, 1f, 1f, 1f};

        // Set light parameters.
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPos, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightColorAmbient, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, lightColorSpecular, 0);

        // Enable lighting in GL.
        gl.glEnable(GL2.GL_LIGHT1);
        gl.glEnable(GL2.GL_LIGHTING);
    }
    
    /**
     * Resizes the screen.
     * 
     * @see javax.media.opengl.GLEventListener#reshape(javax.media.opengl.GLAutoDrawable,
     *      int, int, int, int)
     */
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        final GL gl = drawable.getGL();
        gl.glViewport(0, 0, width, height);
    }

    /**
     * Changing devices is not supported.
     * 
     * @see javax.media.opengl.GLEventListener#displayChanged(javax.media.opengl.GLAutoDrawable,
     *      boolean, boolean)
     */
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
        throw new UnsupportedOperationException("Changing display is not supported.");
    }

    /**
     * Creates and returns a TreeCanvas to draw in. 
     */
    public final static TreeCanvas createCanvas(Simulation sim) {
        TreeCanvas canvas = new TreeCanvas(800, 800, sim);
        final JFrame frame = new JFrame("Tree Simulation");
        frame.getContentPane().add(canvas, BorderLayout.CENTER);
        frame.setSize(800, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // When the window is closed, stop the application.
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
            	frame.dispose();
                System.exit(0);
            }
        });
        frame.setVisible(true);
        canvas.animator = new FPSAnimator(canvas, 10);
        canvas.animator.start();
        return canvas;
    }

	@Override
	public void dispose(GLAutoDrawable drawable) {
		
	}
	
	/**
	 * A utility function to draw a sphere of the given radius at the current origin.
	 */
	public static void drawSphere(GLU glu, float radius) {
    	GLUquadric ball = glu.gluNewQuadric();
    	glu.gluQuadricDrawStyle(ball, GLU.GLU_FILL);
    	glu.gluQuadricNormals(ball, GLU.GLU_FLAT);
    	glu.gluQuadricOrientation(ball, GLU.GLU_OUTSIDE);
    	final int detail = 4;
    	glu.gluSphere(ball, radius, detail, detail);
    	glu.gluDeleteQuadric(ball);
    }
	
	/**
	 * A utility function to draw a cylinder of the given height and radius, aligned with the Z-axis.
	 */
	public static void drawCylinder(GLU glu, float height, float radius) {
    	GLUquadric cyl = glu.gluNewQuadric();
    	glu.gluQuadricDrawStyle(cyl, GLU.GLU_FILL);
    	glu.gluQuadricNormals(cyl, GLU.GLU_FLAT);
    	glu.gluQuadricOrientation(cyl, GLU.GLU_OUTSIDE);
    	final int sides = 8;
    	glu.gluCylinder(cyl, radius, radius, height, sides, 1);
    	glu.gluDeleteQuadric(cyl);
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		isRotating = !isRotating;
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		//isRotating = false;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		//isRotating = true;
	}

	@Override
	public void mouseDragged(MouseEvent e) 
    {
    	this.mouseMoved(e);
    }
    
	@Override
    public void mouseMoved(MouseEvent e) {
    	int mx = e.getXOnScreen();
        int my = e.getYOnScreen();
	}

}