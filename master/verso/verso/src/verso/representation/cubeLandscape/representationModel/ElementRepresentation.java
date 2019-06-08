package verso.representation.cubeLandscape.representationModel;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;

import javax.media.opengl.GL;

import org.apache.commons.lang3.StringUtils;

import com.sun.opengl.util.GLUT;

import verso.graphics.VersoScene;
import verso.graphics.primitives.CubeNoCap;
import verso.graphics.primitives.Primitive;
import verso.model.Element;
import verso.model.Entity;
import verso.representation.Renderable;
import verso.representation.Lines.representationModel.ClassLineRepresentation;
import verso.representation.Lines.representationModel.LineRepresentation;
import verso.representation.cubeLandscape.representationModel.repvisitor.IRepresentationVisitor;


public abstract class ElementRepresentation extends EntityRepresentation implements Renderable{
	public Color bordersColor = Color.green;
	
	
	public final static double Length = 0.7;
	public final static double Width = 0.3;
	public static boolean showNames = false;
	protected Element element;
	protected HashMap<String,MethodRepresentation> methods;
	protected HashMap<String,AttributeRepresentation> attributes;

	// ajout Cynt
	protected HashMap<Integer, LineRepresentation> lines;
	// fin

	protected double twist;
	public static boolean render = true;
	protected double distCam = 10.0;
	protected ClassLineRepresentation clr = null;

	// meshes
	private Primitive bottom;

	public ElementRepresentation(Element element) {
		this.element = element;
		methods = new HashMap<String, MethodRepresentation>();
		attributes = new HashMap<String, AttributeRepresentation>();
		bottom = new CubeNoCap();
	}

	public ElementRepresentation(Element element, ClassLineRepresentation clr) {
		this(element);
		this.clr = clr;
	}
	
	@Override
	public float getLevel() {
		return StringUtils.countMatches(getEntity().getName(),	".")-2;
	}
	
	public ClassLineRepresentation getClassLineRepresentation() {
		return this.clr;
	}

	public void setClassLineRepresentation(ClassLineRepresentation clr) {
		this.clr = clr;
	}

	public Element getElementModel() {
		return this.element;
	}

	public Entity getEntity() {
		return this.element;
	}

	public void addMethod(MethodRepresentation metRep) {
		this.methods.put(metRep.getElement().getName(), metRep);
	}

	public MethodRepresentation getMethod(String methodName) {
		return this.methods.get(methodName);
	}

	public Collection<MethodRepresentation> getMethods() {
		return this.methods.values();
	}

	public void addAttribute(AttributeRepresentation attRep) {
		this.attributes.put(attRep.getElement().getName(), attRep);
	}

	public AttributeRepresentation getAttribute(String attributeName) {
		return this.attributes.get(attributeName);
	}

	// ajout Cynt
	public void addLine(int lineNumber, LineRepresentation lineRep) {
		this.lines.put(lineNumber, lineRep);
	}

	public LineRepresentation getLine(int lineNumber) {
		return this.lines.get(lineNumber);
	}
	// fin

	public void setTwist(double twist) {
		this.twist = twist;
	}

	public void accept(IRepresentationVisitor mv) {
		mv.visit(this);
	}

	public String getName() {
		return this.getElementModel().getName();
	}
	
	
	public void computeAbsolutePosition(int parentPosX, int parentPosZ) {
		this.absolutePosX = parentPosX + (int) this.posX;
		this.absolutePosZ = parentPosZ + (int) this.posZ;
	}
	
	public void render(GL gl) {
//		System.out.println("RENDER ELEMENT REPRESENTATION");
		
		if (hideFilteredClasses && (!this.isFiltered() && SystemRepresentation.filterState) && !this.isSelected)
			return;

		double grSize = 0;
		if (this.methods.values().size() + this.attributes.values().size() != 0)
			grSize = ElementRepresentation.Width
					/ (Math.ceil(Math.sqrt((this.methods.values().size() + this.attributes.values().size()) / 2.0)));

		gl.glLoadName(VersoScene.id);
		VersoScene.pickingEntities.put(VersoScene.id++, this);

		gl.glPushMatrix();

		gl.glTranslated((double) this.posX, 0.01, (double) this.posZ);
		


		gl.glTranslated(0.5, 0, 0.5);
		gl.glRotated(twist, 0, 1, 0);
		if (!render) {
			for (MethodRepresentation m : this.methods.values()) {
				m.setGridSize(grSize);
				m.render(gl);
			}
			for (AttributeRepresentation a : this.attributes.values()) {
				a.setGridSize(grSize);
				a.render(gl);
			}
		}

		if (this.isSelected && render) {
			System.out.println("rendering a selected element");
			this.renderBorders(gl, 0.2, this.height / 3.0, this.bordersColor);
		}

		gl.glScaled(1, this.height, 1);

		gl.glTranslated(0, 0.5, 0);

		if (showNames) {
			gl.glPushMatrix();

			if (render)
				gl.glTranslated(0, 0.51, 0.49 * ElementRepresentation.Length);
			else
				gl.glTranslated(0, -0.5, 0.49 * ElementRepresentation.Length);
			gl.glScaled(0.0005f, 1f, 0.0005f);
			gl.glRotated(-90, 1, 0, 0);
			gl.glRotated(90, 0, 0, 1);
			Color colortemp = new Color(255 - this.color.getRed(), 255 - this.color.getGreen(),
					255 - this.color.getBlue());

			GLUT glut = new GLUT();

			gl.glDisable(GL.GL_LIGHTING);
			gl.glColor3f(colortemp.getRed() / 255f, colortemp.getGreen() / 255f, colortemp.getBlue() / 255f);
			gl.glLineWidth(2.5f);
			String nameStroke = this.getElementModel().getName();
			if (nameStroke.contains("."))
				nameStroke = nameStroke.substring(nameStroke.lastIndexOf(".") + 1);
			glut.glutStrokeString(GLUT.STROKE_ROMAN, nameStroke);
			gl.glEnable(GL.GL_LIGHTING);
			
				//Commence les trucs pour les noms 2D
				gl.glRasterPos3f(0, 0, 0f);
				gl.glColor3f(1.0f, 1.0f, 1.0f);
				int size = glut.glutBitmapLength(GLUT.BITMAP_HELVETICA_18, nameStroke);
				byte[] tobuff = new byte[size*18*3];
				for (int i = 0; i < tobuff.length; i++)
					tobuff[i] = (byte)255;
				ByteBuffer bb = ByteBuffer.wrap(tobuff);
				gl.glDrawPixels(size, 18, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, bb);
				gl.glColor3f(0.0f, 0.0f, 0.0f);
				gl.glRasterPos3f(0, 0, 0f);
				glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, nameStroke);
				
				//Fini le truc pour les noms pas 2D
				
				
				gl.glPopMatrix();
			}
			this.setRatio(gl);

			
			//gl.glScaled(ElementRepresentation.Width , 1, ElementRepresentation.Length);
			/*
			if (this.isSelected)
				/*
				//gl.glColor3d(this.selectionColor.getRed() / 255.0, this.selectionColor.getGreen() / 255.0, this.selectionColor.getBlue() / 255.0);
				//gl.glColor3f(0.0f, 1.0f, 0.0f);
			//else if (!this.isFiltered() && SystemRepresentation.filterState)
			else*/ 
		if (this.isFiltered && SystemRepresentation.filterState)
			gl.glColor3f(this.unsaturatedColor.getRed() / 255.0f, this.unsaturatedColor.getGreen() / 255.0f,
					this.unsaturatedColor.getBlue() / 255.0f);
		else
			gl.glColor3f(this.color.getRed() / 255.0f, this.color.getGreen() / 255.0f, this.color.getBlue() / 255.0f);

		// System.out.println(distCam);
		if (render /* this.distCam > 10.0 */) {
			this.mesh.render(gl);
		} else {

			// gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
			gl.glTranslated(0, -0.5, 0);
			gl.glScaled(1, 1.0 / this.height, 1.0);
			gl.glScaled(1, 0.025, 1);
			gl.glTranslated(0, 0.5, 0);
			this.bottom.render(gl);
			// gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);

		}

		gl.glPopMatrix();
	}	
	
	public void setCamDist(double camX, double camY, double camZ) {
		// super.setCamDist(camX, camY, camZ);
		this.distCam = Math
				.sqrt(Math.pow((camX - this.posX), 2) + Math.pow((camY), 2) + Math.pow((camZ - this.posZ), 2));
	}

	protected abstract void setRatio(GL gl);

	protected abstract void renderBorders(GL gl, Double borderWidth, Double borderHeight, Color borderColor);

	@Override
	public boolean isElement() {
		return true;
	}
}
