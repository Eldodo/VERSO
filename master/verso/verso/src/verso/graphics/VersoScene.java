package verso.graphics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.StringUtils;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.BufferUtil;

import verso.graphics.camera.CameraSphere;
import verso.model.Entity;
import verso.model.metric.Metric;
import verso.representation.IPickable;
import verso.representation.Renderable;
import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.EntityRepresentation;

public class VersoScene extends JPanel
		implements GLEventListener, KeyListener, MouseMotionListener, MouseWheelListener, MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<Renderable> renderableElements;
	// protected GLJPanel glPanel;
	protected GLJPanel glPanel;
	private GLU glu;
	protected CameraSphere cam;
	private int oldMousePositionX = 0;
	private int oldMousePositionY = 0;
	protected boolean isSelecting = false;

	protected boolean isMultiSelecting = false;

	private int pickX = 0;
	private int pickY = 0;

	private int selectionWidth = 0;
	private int selectionHeight = 0;

	protected HashSet<IPickable> selectedItems = new HashSet<IPickable>();
	protected LinkedList<int[]> selectionRectangles = new LinkedList<int[]>();

	// DEMANDER � GUILLAUME � PROPOS DE �A
	// **********************************************
	protected int maxSelectedElements = 65536;
	// **********************************************

	public static Map<Integer, IPickable> pickingEntities = new HashMap<Integer, IPickable>();
	public static int id = 0;
	private RotateTest rt = new RotateTest();
	protected boolean dirtyList = true;
	private JPanel containner;
	private JLabel infoLabel;
	protected JTable metricTable;
	protected JSplitPane splitPane;
	protected JSplitPane splitPane2;
	protected JTextArea jtextarea;

	protected Animator animator;

	public VersoScene() {
		renderableElements = new ArrayList<Renderable>();

		glPanel = new GLJPanel();

		glu = new GLU();
		cam = new CameraSphere();
		
		glPanel.addGLEventListener(this);
		glPanel.setFocusable(true);
		glPanel.addKeyListener(this);
		this.setLayout(new BorderLayout());
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
		this.addMouseListener(this);
		add(glPanel);
		this.setPreferredSize(new Dimension(1200, 1200));
		this.setSize(1200, 1200);

		// Rotate Test
		rt.cam = this.cam;
		rt.glPanel = this.glPanel;

		containner = new JPanel();
		infoLabel = new JLabel(" ", JLabel.LEFT);
		containner.setLayout(new BorderLayout());
		containner.add(infoLabel, BorderLayout.NORTH);
		containner.add(this);
		metricTable = new JTable();

		JScrollPane scrollPane = new JScrollPane(metricTable);
		jtextarea = new JTextArea();
		jtextarea.setLineWrap(false);
		jtextarea.setText("");
		JScrollPane scrollPane2 = new JScrollPane(jtextarea);
		splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, scrollPane2);
		splitPane2.setDividerLocation(0.5);
		splitPane2.setMinimumSize(new Dimension(0, 0));
		metricTable.setFillsViewportHeight(true);
		metricTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		scrollPane.setPreferredSize(new Dimension(50, 400));
		scrollPane.setMinimumSize(new Dimension(0, 0));
		containner.setPreferredSize(new Dimension(800, 800));
		containner.setMinimumSize(new Dimension(0, 0));
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, containner, splitPane2);

		splitPane.setDividerLocation(0.8);
		//this.centerCam();
		// splitPane.setOneTouchExpandable(true);

	}

	public void addRenderable(Renderable element) {
		this.renderableElements.add(element);
	}

	public void clearRenderable() {
		this.renderableElements.clear();
	}

	public JPanel getContainner() {
		return this.containner;
	}

	public void setInfo(String info) {
		infoLabel.setText(info);
	}

	/* GLEventListener Methods */

	public void init(GLAutoDrawable glauto) {
		GL gl = glauto.getGL();

		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
		// gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);

		// gl.glCullFace(GL.GL_BACK);
		// gl.glEnable(GL.GL_CULL_FACE);

		// glPanel.getChosenGLCapabilities().setHardwareAccelerated(false);

		// System.out.println("HardwareAccelerated: " +
		// glPanel.getChosenGLCapabilities().getHardwareAccelerated());

		System.out.println("INIT!!!!!!!!!!!!!!!!!!!!!!!!!!");

		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);

		gl.glEnable(GL.GL_NORMALIZE);
		gl.glHint(GL.GL_POLYGON_SMOOTH_HINT, GL.GL_NICEST);

		gl.glEnable(GL.GL_LINE_SMOOTH);

		// Creation of Display Lists ...
		// Cube.createDisplayList(gl);

		gl.setSwapInterval(0);

		/*
		 * animator = new Animator(glPanel); animator.setRunAsFastAsPossible(true);
		 * animator.start();
		 */
	}

	public void generateLights(GL gl) {
		gl.glEnable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_LIGHT0);
		gl.glEnable(GL.GL_COLOR_MATERIAL);
		float[] position = { 0.0f, 4.0f, 0.0f, 0.0f };
		// float[] position = {3.0f,4.0f,2.0f,0.0f};
		float[] intensity = { 1f, 1f, 1f, 1.0f };
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, intensity, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, position, 0);

		gl.glEnable(GL.GL_LIGHT1);
		// float[] position2 = {-3.0f,4.0f,-2.0f,0.0f};
		float[] intensity2 = { 0.5f, 0.5f, 0.5f, 1.0f };
		float[] position2 = { 3.0f, 0.0f, 2.0f, 0.0f };
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, intensity2, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, position2, 0);

		gl.glEnable(GL.GL_LIGHT2);
		float[] position3 = { -3.0f, 0.0f, -2.0f, 0.0f };
		gl.glLightfv(GL.GL_LIGHT2, GL.GL_DIFFUSE, intensity2, 0);
		gl.glLightfv(GL.GL_LIGHT2, GL.GL_POSITION, position3, 0);

	}

	public void display(GLAutoDrawable glauto) {
		if (isSelecting || isMultiSelecting) {
			select(glauto.getGL());
			isSelecting = false;
			isMultiSelecting = false;
			return;
		}

		GL gl = glauto.getGL();

		//gl.glClearColor(1f, 0.89f, 0.77f, 0);
		gl.glClearColor(0.2f, 0.2f, 0.2f,0);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glMatrixMode(GL.GL_MODELVIEW);

		gl.glLoadIdentity();

		// viewing transformation
		glu.gluLookAt(cam.getX(), cam.getY(), cam.getZ(), cam.getLookAtX(), cam.getLookAtY(), cam.getLookAtZ(),
				cam.getNormalX(), cam.getNormalY(), cam.getNormalZ());

		generateLights(gl);

		// gl.glPushMatrix();
		// this.dirtyList = true;
		if (dirtyList) {
			this.selectionRectangles.clear();
			this.dirtyList = false;
		}

		render(gl);

		// gl.glPopMatrix();
		gl.glFlush();

	}

	public void select(GL gl) {
		int numOfItem = 0;
		int[] selectBuff = new int[maxSelectedElements];
		IntBuffer buffer = BufferUtil.newIntBuffer(maxSelectedElements);// IntBuffer.allocate(512);
		int[] viewport = new int[4];
		// IntBuffer view = IntBuffer.allocate(4);
		gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
		gl.glSelectBuffer(maxSelectedElements, buffer);
		gl.glRenderMode(GL.GL_SELECT);
		gl.glInitNames();
		gl.glPushName(0);
		gl.glMatrixMode(GL.GL_PROJECTION);

		gl.glPushMatrix();
		gl.glLoadIdentity();
		// System.out.println("Viewport3 :"+ (view.get(3) - pickY));
		glu.gluPickMatrix((double) pickX, (double) (viewport[3] - pickY), this.selectionWidth, this.selectionHeight,
				viewport, 0);
		glu.gluPerspective(45, (double) viewport[2] / (double) viewport[3], 0.1, 1000.0);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		render(gl);
		gl.glPopMatrix();
		gl.glFlush();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPopMatrix();

		numOfItem = gl.glRenderMode(GL.GL_RENDER);
		// System.out.println(Scene.id + "," + numOfItem);
		buffer.get(selectBuff);

		int index = 0;
		float currDepth = 0;
		float minDepth = 1000000;
		int minID = -1;

		LinkedList<Integer> selectedItemsID;
		
		this.selectedItems.clear();

		if (!this.isMultiSelecting) {
			for (int i = 0; i < numOfItem; i++) {
				index++; // On skip le nombre d'item dans la stack (tjs 1, pas de hi�rarchie)
				currDepth = selectBuff[index++];
				if (currDepth < minDepth) {
					minDepth = currDepth;
					index++; // On skip le max depth
					minID = selectBuff[index++]; // On store le nom
				} else {
					index++;
					index++; // on skip tout, c'est pas le plus petit
				}
			}
			if (minID != -1) {
				this.selectedItems.add(VersoScene.pickingEntities.get(minID));
			}
		} else {
			selectedItemsID = new LinkedList<Integer>();
			for (int i = 0; i < numOfItem; i++) {
				index++; // On skip le nombre d'item dans la stack (tjs 1, pas de hi�rarchie)
				index++; // On skip le min depth
				index++; // On skip le max depth
				selectedItemsID.add(selectBuff[index++]); // On store le nom
			}

			// this.selectedItems.clear();

			System.out.println("Picking entities size: " + VersoScene.pickingEntities.size());

			if (selectedItemsID.size() > 0) {
				for (Integer id : selectedItemsID) {
					this.selectedItems.add(VersoScene.pickingEntities.get(id));
				}
			}

			/*
			 * LinkedList<IPickable> refinedSelectedElements = new LinkedList<IPickable>();
			 * for (IPickable p : this.selectedElements) { if (p instanceof
			 * PackageRepresentation) { if (((PackageRepresentation)p).isRendered()) {
			 * refinedSelectedElements.add(p); } } else if (p instanceof
			 * ElementRepresentation) { refinedSelectedElements.add(p); } }
			 * 
			 * this.selectedElements = refinedSelectedElements;
			 */
		}
	}

	public void reshape(GLAutoDrawable glauto, int x, int y, int width, int heigth) {
		// System.out.println("reshape");

		// System.out.println("" + width + "," + heigth);
		// System.out.println("" + this.getSize().getWidth() + "," +
		// this.getSize().getHeight());
		// System.out.println("" + glPanel.getSize().getWidth() + "," +
		// glPanel.getSize().getHeight());

		GL gl = glauto.getGL();
		gl.glViewport(x, y, width, heigth);
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(45, (double) width / (double) heigth, 0.1, 1000.0);
		dirtyList = true;

		// glPanel.display();
	}

	

	
	//Permet de g�rer la multi-direction (exemple haut et gauche en m�me temps)
	protected int Zdirection = 0;
	protected int Xdirection = 0;
	
	public void keyPressed(KeyEvent ke) {
		
		switch (ke.getKeyCode()) {
//			case KeyEvent.VK_R:
//				//startRotate();
//				this.cam.addTheta(Math.PI/25.0);
//				this.cam.computeCartesianCoordinate();
//				this.glPanel.display();
//				break;
			//On gere les mouvements
			case KeyEvent.VK_LEFT:
				this.Xdirection = -1;
				break;
			case KeyEvent.VK_RIGHT:
				this.Xdirection = 1;
				break;
			case KeyEvent.VK_UP:
				this.Zdirection = -1;
				break;
			case KeyEvent.VK_DOWN:
				this.Zdirection = 1;
				break;
				//La touche espace recentre la cam�ra
			case KeyEvent.VK_SPACE:
				this.Xdirection = 0;
				this.Zdirection = 0;
				this.centerCam();
				break;
			default:
				break;
				
		}
		if(ke.getKeyCode()==KeyEvent.VK_RIGHT || ke.getKeyCode()==KeyEvent.VK_UP || ke.getKeyCode()==KeyEvent.VK_DOWN || ke.getKeyCode()==KeyEvent.VK_LEFT) {
			if(ke.isShiftDown())
				this.moveVerticalHorizontal(this.Xdirection, this.Zdirection);
			else
				this.moveCross(this.Xdirection, this.Zdirection);
		}
			
	}
	
	public void setCenterCamPos(float x, float z) {
		cam.setCenterPosition(x, z);
		centerCam();
	}
	
	private void centerCam() {
		cam.center();
		glPanel.display();
	}
	
//	private void moveCam(float x, float z) {
//		cam.move(x, z);
//		glPanel.display();
//	}
	
	/* KeyListener Methods */
	public void keyReleased(KeyEvent ke) {
		switch (ke.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			this.Xdirection = 0;
			break;
		case KeyEvent.VK_RIGHT:
			this.Xdirection = 0;
			break;
		case KeyEvent.VK_UP:
			this.Zdirection = 0;
			break;
		case KeyEvent.VK_DOWN:
			this.Zdirection = 0;
			break;
		default:
			break;
		}
	}

	private void startRotate() {

		if (rt.rotate == true)
			rt.rotate = false;
		else {
			rt.rotate = true;
			new Thread(rt).start();
		}
	}

	private void moveCross(int deltaX, int deltaY) {
		this.selectionRectangles.clear();

		cam.moveCross(deltaX, deltaY);
		glPanel.display();
	}
	
	private void moveVerticalHorizontal(int deltaX, int deltaY) {
		this.selectionRectangles.clear();

		cam.moveVerticalHorizontal(deltaX, deltaY);
		glPanel.display();
	}
	
	public void mouseMoved(MouseEvent me) {
		int deltaX = 0;
		int deltaY = 0;
		deltaX = me.getX() - oldMousePositionX;
		deltaY = me.getY() - oldMousePositionY;

		deltaX = (int) Math.ceil(deltaX / 5);
		deltaY = (int) Math.ceil(deltaY / 5);

		if (me.isShiftDown()) {
			moveVerticalHorizontal(-deltaX, -deltaY);
		} else if (me.isControlDown()) {
			moveCross(-deltaX, -deltaY);
		}

		oldMousePositionX = me.getX();
		oldMousePositionY = me.getY();
	}
	
	public void mouseDragged(MouseEvent me) {
		
		System.out.println("VersoScene.mouseDragged()");
		if (me.getButton() == MouseEvent.BUTTON3) {
			int deltaX = 0;
			int deltaY = 0;
			deltaX = me.getX() - oldMousePositionX;
			deltaY = me.getY() - oldMousePositionY;
	
			deltaX = (int) Math.ceil(deltaX / 3D);
			deltaY = (int) Math.ceil(deltaY / 3D);

			moveVerticalHorizontal(deltaX, deltaY);
			oldMousePositionX = me.getX();
			oldMousePositionY = me.getY();
		}
	}


	public void mouseWheelMoved(MouseWheelEvent e) {
		this.selectionRectangles.clear();

		cam.zoom(e.getWheelRotation());
		glPanel.display();
	}

	public void mouseClicked(MouseEvent me) {
		IPickable picked = pick(me.getX(), me.getY());
		String[][] tabModel;
		if (picked != null) {
			if(this.selectedItems.size()<=1)
				this.setInfo(picked.getName() );
			else
				this.setInfo(this.selectedItems.size()+" �l�ments");
			try {
				EntityRepresentation er = (EntityRepresentation) picked;

				tabModel = new String[er.getEntity().getMetrics().size()][2];
				// String toOutput = "";
				int i = 0;
				for (Metric m : er.getEntity().getMetrics()) {
					tabModel[i][0] = m.getName();
					tabModel[i++][1] = "" + m.getValue();
				}
				String[] columnName = { "name", "value" };
				this.metricTable.setModel(new DefaultTableModel(tabModel, columnName));
				if (picked instanceof ElementRepresentation) {
					ElementRepresentation el = (ElementRepresentation) picked;
					String s = "";
					if (el.getElementModel().getLastCommit() != null)
						s += el.getElementModel().getLastCommit().toString();
					s += "\n" + "BugList";
					s += "\n" + el.getElementModel().getBugString();
					jtextarea.setText(s);

				}
			} catch (Exception e) {
			}
		} else
			this.setInfo("Pas d'�l�ments");
	}

	public void render(GL gl) {
		/*
		 * gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE); //GLUT glut = new
		 * GLUT(); //glut.glutSolidCube(1.0f); gl.glPushMatrix(); //gl.glLoadIdentity();
		 * gl.glLineWidth(10f); gl.glBegin(GL.GL_LINES); gl.glColor3f(1f, 0.5f, 0.5f);
		 * //gl.glPointSize(10.0f); //gl.glLineWidth(500f); gl.glNormal3f(0, 1f, 0);
		 * gl.glVertex3f(0f, 0f, 0f); gl.glVertex3f(10f, 0f, 10f); gl.glVertex3f(5f, 5f,
		 * 5f); gl.glEnd(); gl.glPopMatrix(); gl.glEnd();
		 */

		/*
		 * NodeRepresentation startNode; NodeRepresentation endNode;
		 * 
		 * startNode = new NodeRepresentation(null, null, 0.0f, 0.0f, 0.0f, 0.0f, null,
		 * null);
		 * 
		 * endNode = new NodeRepresentation(null, null, 0.0f, 50.0f, 0.0f, 0.0f, null,
		 * null); DirectLinkRepresentation directLink = new
		 * DirectLinkRepresentation(startNode, endNode, 1, 0, new CubeNoCapColored(null,
		 * null), 0.05f, 1, Color.green, Color.red, false, Color.magenta);
		 * directLink.render(gl);
		 * 
		 * endNode = new NodeRepresentation(null, null, 0.0f, 0.0f, 0.0f, 50.0f, null,
		 * null); directLink = new DirectLinkRepresentation(startNode, endNode, 1, 0,
		 * new CubeNoCapColored(null, null), 0.05f, 1, Color.blue, Color.white, false,
		 * Color.magenta); directLink.render(gl);
		 * 
		 * endNode = new NodeRepresentation(null, null, 0.0f, 0.0f, 50.0f, 0.0f, null,
		 * null); directLink = new DirectLinkRepresentation(startNode, endNode, 1, 0,
		 * new CubeNoCapColored(null, null), 0.05f, 1, Color.yellow, Color.orange,
		 * false, Color.magenta); directLink.render(gl);
		 */

		// gl.glScaled(0.025, 0.025, 0.025);

		/*
		 * gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE);
		 * gl.glEnable(GL.GL_CULL_FACE);
		 * 
		 * Primitive unArc = new Arc(0.5, 45.0, 1.0, 2.0, 50); gl.glPushMatrix();
		 * gl.glColor3d(255.0, 0.0, 0.0); unArc.render(gl); gl.glPopMatrix();
		 */

		// NodeRepresentation testNode = new NodeRepresentation(null, null, 0.5, 5, 5,
		// 2, Color.white, null);
		// NodeRepresentation testNode2 = new NodeRepresentation(null, null, 0.5, 5, 5,
		// 2, Color.red, null);

		// testNode2.setposXd(testVector[0]);
		// testNode2.setposYd(testVector[1]);
		// testNode2.setposZd(testVector[2]);

		/*
		 * double x = 2.392; double y = 10.203; double z = -5.4032;
		 * 
		 * double vectorLength = Math.sqrt(x*x + y*y + z*z);
		 * 
		 * GLUT glu = new GLUT();
		 * 
		 * 
		 * gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE);
		 * 
		 * gl.glPushMatrix(); gl.glColor3f(1.0f, 1.0f, 1.0f); gl.glRotated(153.498, x,
		 * y, z); gl.glTranslated(-4.41232, 2.493, 7.4032); glu.glutSolidSphere(0.5, 8,
		 * 8); gl.glPopMatrix();
		 * 
		 * double[] testVector =
		 * EdgeBundleLinkRepresentation.evaluateRenderingPoints(null);
		 * 
		 * 
		 * System.out.println("Red Sphere: "); System.out.println("X: " + testVector[0]
		 * + "  Y: " + testVector[1] + "  Z: " + testVector[2]); System.out.println("");
		 * 
		 * 
		 * gl.glPushMatrix(); gl.glColor3f(1.0f, 0.0f, 0.0f);
		 * gl.glTranslated(testVector[0], testVector[1], testVector[2]);
		 * glu.glutSolidSphere(0.5, 8, 8); gl.glPopMatrix();
		 */

		/*
		 * int nbrePoints = 4; double linkRadius = 0.7; double pointAngle = 360.0 /
		 * nbrePoints; double[][] basePoints = new double[nbrePoints][];
		 * 
		 * for (int i = 0; i < nbrePoints; i++) { basePoints[i] = new double[3];
		 * basePoints[i][0] = linkRadius * Math.cos(Math.toRadians(pointAngle*i));
		 * basePoints[i][1] = 0.0; basePoints[i][2] = linkRadius *
		 * Math.sin(Math.toRadians(pointAngle*i)); }
		 * 
		 * 
		 * NodeRepresentation node1 = new NodeRepresentation(null, null, 0.1, 0, 0, 0,
		 * Color.red, null); NodeRepresentation node2 = new NodeRepresentation(null,
		 * null, 0.1, 2, 0, 0, Color.green, null);
		 * 
		 * node1.render(gl); node2.render(gl);
		 * 
		 * 
		 * NodeRepresentation testVector = node2.substract(node1); double[]
		 * rotationParams = MathGeometry.getRotationParams(testVector.getposXd(),
		 * testVector.getposYd(), testVector.getposZd()); double[] rotationMatrix =
		 * MathGeometry.get3DRotationMatrix(rotationParams[0], rotationParams[1],
		 * rotationParams[2], rotationParams[3]); double[] renderingPoint;
		 * 
		 * 
		 * LinkedList<double[]> currentRenderingPoints = new LinkedList<double[]>(); for
		 * (int i = 0; i < nbrePoints; i++) { renderingPoint =
		 * MathGeometry.rotate3DPoint(rotationMatrix, basePoints[i]);
		 * //renderingPoint[0] += node2.getposXd(); //renderingPoint[1] +=
		 * node2.getposYd(); //renderingPoint[2] += node2.getposZd();
		 * 
		 * currentRenderingPoints.add(renderingPoint); }
		 * 
		 * Iterator<double[]> renderingPointsItr = currentRenderingPoints.iterator();
		 * double[] currentPoint; while (renderingPointsItr.hasNext()) { currentPoint =
		 * renderingPointsItr.next();
		 * 
		 * NodeRepresentation renderingNode = new NodeRepresentation(null, null, 0.05,
		 * currentPoint[0], currentPoint[1], currentPoint[2], Color.blue, null);
		 * renderingNode.render(gl); }
		 */

		// vertex coords array
		/*
		 * int vertices[] = {1,1,1, -1,1,1, -1,-1,1, 1,-1,1, // v0-v1-v2-v3 1,1,1,
		 * 1,-1,1, 1,-1,-1, 1,1,-1, // v0-v3-v4-v5 1,1,1, 1,1,-1, -1,1,-1, -1,1,1, //
		 * v0-v5-v6-v1 -1,1,1, -1,1,-1, -1,-1,-1, -1,-1,1, // v1-v6-v7-v2 -1,-1,-1,
		 * 1,-1,-1, 1,-1,1, -1,-1,1, // v7-v4-v3-v2 1,-1,-1, -1,-1,-1, -1,1,-1, 1,1,-1};
		 * // v4-v7-v6-v5
		 * 
		 * // normal array int normals[] = {0,0,1, 0,0,1, 0,0,1, 0,0,1, // v0-v1-v2-v3
		 * 1,0,0, 1,0,0, 1,0,0, 1,0,0, // v0-v3-v4-v5 0,1,0, 0,1,0, 0,1,0, 0,1,0, //
		 * v0-v5-v6-v1 -1,0,0, -1,0,0, -1,0,0, -1,0,0, // v1-v6-v7-v2 0,-1,0, 0,-1,0,
		 * 0,-1,0, 0,-1,0, // v7-v4-v3-v2 0,0,-1, 0,0,-1, 0,0,-1, 0,0,-1}; //
		 * v4-v7-v6-v5
		 * 
		 * // color array float colors[] = {1,1,1, 1,1,0, 1,0,0, 1,0,1, // v0-v1-v2-v3
		 * 1,1,1, 1,0,1, 0,0,1, 0,1,1, // v0-v3-v4-v5 1,1,1, 0,1,1, 0,1,0, 1,1,0, //
		 * v0-v5-v6-v1 1,1,0, 0,1,0, 0,0,0, 1,0,0, // v1-v6-v7-v2 0,0,0, 0,0,1, 1,0,1,
		 * 1,0,0, // v7-v4-v3-v2 0,0,1, 0,0,0, 0,1,0, 0,1,1}; // v4-v7-v6-v5
		 * 
		 * 
		 * short indices[] = {0,1,2,3, 4,5,6,7, 8,9,10,11, 12,13,14,15, 16,17,18,19,
		 * 20,21,22,23};
		 */

		/*
		 * double vertices[] = {0.0,0.0,0.0, 0.0,0.0,-1.0, 0.0,0.0,-2.0, 0.0,0.0,-3.0,
		 * 1.0,0.0,0.0, 1.0,0.0,-1.0, 1.0,0.0,-2.0, 1.0,0.0,-3.0 };
		 * 
		 * double normals[] = {0.0,1.0,0.0, 0.0,1.0,0.0, 0.0,1.0,0.0, 0.0,1.0,0.0,
		 * 0.0,1.0,0.0, 0.0,1.0,0.0, 0.0,1.0,0.0, 0.0,1.0,0.0 };
		 * 
		 * float colors[] = {1.0f,0.0f,0.0f, 1.0f,0.0f,0.0f, 1.0f,0.0f,0.0f,
		 * 1.0f,0.0f,0.0f, 1.0f,0.0f,0.0f, 1.0f,0.0f,0.0f, 1.0f,0.0f,0.0f,
		 * 1.0f,0.0f,0.0f, };
		 */

		/*
		 * int indices[] = {0,1,5, 0,5,4, 1,2,6, 1,6,5, 2,3,7, 2,7,6, 3,0,4, 3,4,7 };
		 */

		/*
		 * for (short i = 0; i < nbrePoints; i++) { currIndices[i] = i; }
		 * 
		 * for (int i = 0; i < this.renderingPoints.size()-1; i++) { nextIndices = new
		 * short[nbrePoints]; for (short k = 0; k < nbrePoints; k++) { nextIndices[k] =
		 * (short)(currIndices[k]+4); }
		 * 
		 * for (int j = 0; j < currIndices.length; j++) { indices[currIndex++] =
		 * currIndices[j]; indices[currIndex++] = currIndices[(j + 1) %
		 * currIndices.length]; indices[currIndex++] = nextIndices[(j + 1) %
		 * nextIndices.length];
		 * 
		 * indices[currIndex++] = currIndices[j]; indices[currIndex++] =
		 * nextIndices[(j+1) % nextIndices.length]; indices[currIndex++] =
		 * nextIndices[j]; }
		 * 
		 * currIndices = nextIndices; }
		 */

		/*
		 * DoubleBuffer normalsBuffer = BufferUtil.newDoubleBuffer(normals.length);
		 * FloatBuffer colorsBuffer = BufferUtil.newFloatBuffer(colors.length);
		 * DoubleBuffer verticesBuffer = BufferUtil.newDoubleBuffer(vertices.length);
		 * IntBuffer indicesBuffer = BufferUtil.newIntBuffer(indices.length);
		 * 
		 * for (int i = 0; i < normals.length; i++) { normalsBuffer.put(normals[i]); }
		 * 
		 * for (int i = 0; i < colors.length; i++) { colorsBuffer.put(colors[i]); }
		 * 
		 * for (int i = 0; i < vertices.length; i++) { verticesBuffer.put(vertices[i]);
		 * }
		 * 
		 * for (int i = 0; i < indices.length; i++) { indicesBuffer.put(indices[i]); }
		 * 
		 * normalsBuffer.rewind(); colorsBuffer.rewind(); verticesBuffer.rewind();
		 * indicesBuffer.rewind();
		 * 
		 * 
		 * 
		 * 
		 * 
		 * // enable and specify pointers to vertex arrays
		 * gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
		 * gl.glEnableClientState(GL.GL_COLOR_ARRAY);
		 * gl.glEnableClientState(GL.GL_VERTEX_ARRAY); gl.glNormalPointer(GL.GL_DOUBLE,
		 * 0, normalsBuffer); gl.glColorPointer(3, GL.GL_FLOAT, 0, colorsBuffer);
		 * gl.glVertexPointer(3, GL.GL_DOUBLE, 0, verticesBuffer);
		 * 
		 * //gl.glPushMatrix();
		 * 
		 * gl.glDrawElements(GL.GL_TRIANGLES, indices.length, GL.GL_UNSIGNED_INT,
		 * indicesBuffer);
		 * 
		 * //gl.glPopMatrix();
		 * 
		 * gl.glDisableClientState(GL.GL_VERTEX_ARRAY); // disable vertex arrays
		 * gl.glDisableClientState(GL.GL_COLOR_ARRAY);
		 * gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
		 */

		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		for (Renderable element : renderableElements) {
			// gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE);
			gl.glPushMatrix();
			element.render(gl);
			gl.glPopMatrix();
		}

	}

	public IPickable pick(int mouseX, int mouseY) {
		this.isSelecting = true;
		pickX = mouseX;
		pickY = mouseY;
		this.selectionWidth = 1;
		this.selectionHeight = 1;

		glPanel.display();
		if (this.selectedItems.size() > 0) {
			return this.selectedItems.iterator().next();
		}
		else {
			return null;
		}
	}

	public HashSet<IPickable> pick(int mouseX, int mouseY, int selectionWidth, int selectionHeight) {
		this.isMultiSelecting = true;
		this.pickX = mouseX;
		this.pickY = mouseY;
		this.selectionWidth = selectionWidth;
		this.selectionHeight = selectionHeight;

		glPanel.display();
		return this.selectedItems;
	}

	public JPanel getContainer() {
		return this.containner;
	}

	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
		// TODO Auto-generated method stub

	}

	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void refreshScene() {
		// this.dirtyList = true;
		glPanel.display();
		// this.repaint();
	}

	public void redisplay() {
		glPanel.display();
	}
}
