package verso.representation.cubeLandscape.representationModel.link;

import java.awt.Color;

import javax.media.opengl.GL;

import com.sun.opengl.util.GLUT;

import verso.graphics.VersoScene;
import verso.graphics.primitives.Primitive;
import verso.representation.Renderable;
import verso.representation.cubeLandscape.representationModel.EntityRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;

public class NodeRepresentation extends EntityRepresentation implements Renderable {
	public float meshSize;
	private float posXd;
	private float posYd;
	private float posZd;
	private NodeRepresentation parentNode;
	private EntityRepresentation entityRepresentation;
	
	
	//À besoins d'un lien vers l'entité qu'il représente pour savoir quelles noeuds reliés quand on veut relié
	//2 classes ensembles!!! (SAUF SI ON UTILISE LE HASHMAP DANS SystemRepresentation!!!)
	
	//private PackageRepresentation representedPackage; //???
	//private ElementRepresentation representedElement; //???
	
	//ORGANISER LES LIENS DANS UN HASHMAP OU QUELQUE CHOSE DU GENRE ET LES CLASSER SELON L'INSTANCE
	//DE ELEMENTREPRESENTATION OU PACKAGEREPRESENTATION QU'IL REPRÉSENTE, AFIN DE POUVOIR FACILEMENT RETROUVER
	//UN NOEUD SI ON CONNAÎT L'INSTANCE DE L'ÉLÉMENT QU'IL REPRÉSENTE (POUR LA CRÉATION DES LIENS).
	
	//LES NOEUDS POURRAIENT PEUT-ÊTRE ÊTRE PLACÉS DANS LES CLASSES ELEMENTREPRESENTATION ET PACKAGEREPRESENTATION,
	//MAIS PAS LES LIENS. (TROP DIFFICILE À GÉRER POUR L'AFFICHAGE DES LIENS BIDIRECTIONNELS).
	
	//Ou d'autres types d'objets pour lier les NodeRepresentation aux objets du modèle des classes (moins logique
	//selon moi!!!) ???
	
	
	//Nécessite au minimum quelque chose pour savoir avec quelle classe est lié le noeud (tout simplement pour
	//savoir entre quels NodeRepresentation faire le lien!!!) (à moins d'inclure les noeuds directement dans les
	//classes ElementRepresentation et PackageRepresentation).
	
	//Conserver les noeuds dans les classes ElementRepresentation et PackageRepresentation fonctionnerait bien,
	//car on il faut retrouver l'intance de ElementRepresentation ou de PackageRepresentation à partir du modèle
	//de toute façon (d'autres méthodes ???) pour trouver le noeud à afficher, donc dans ce cas on l'aurait déjà
	//(de toute façon, le noeud serait déjà affiché par la méthode render de la classe qui le contient).
	
	//Pour afficher un lien, de toute façon on doit commencer par trouver le modèle des 2 classes liées ensembles
	//(pour savoir quelles classes sont liées au juste) et après ça de trouver l'instance de ElementRepresentation
	//d'où part le lien (et y ajouter le lien, car tous les liens que contient la classe seront toujours TOUS affichés
	//par le fonction render).
	
	//Conserver les liens dans les classes ElementRepresentation et PackageRepresentation pourrait fonctionner seulement
	//si les liens sont unidirectionnelle, car chaque ElementRepresentation et PackageRepresentation doivent nécessairement
	//s'occuper de faire le render de leur noeud (pas trop de problème sauf qu'on pourrait difficilement customizer
	//l'apparence du noeud) et de leurs liens, donc si les liens sont bidirectionnels ils seront partagés par 2 instances
	//(les 2 entités liée) et donc ils seront affiché 2 fois (difficile de savoir si l'autres instance contenant ce lien
	//à déjà été affichée).
	
	
	
	public NodeRepresentation(EntityRepresentation e, Primitive mesh, float meshSize, float posXd, float posYd, float posZd, Color color, NodeRepresentation parentNode) {
		this.mesh = mesh;
		this.meshSize = meshSize;
		this.posXd = posXd;
		this.posYd = posYd;
		this.posZd = posZd;
		this.parentNode = parentNode;
		this.color = color;
		this.entityRepresentation = e;
		
		this.meshSize = 0.5f;
		//this.color = Color.green;
	}
	
	public EntityRepresentation getEntityRepresentation() {
		return this.entityRepresentation;
	}
	
	public void setEntityRepresentation(EntityRepresentation entityRepresentation)
	{
		this.entityRepresentation = entityRepresentation;
	}
	
	public float getMeshSize() {
		return meshSize;
	}
	
	public void setmeshSize(float meshSize) {
		this.meshSize = meshSize;
	}
	
	public float getposXd() {
		return this.posXd;
	}



	public void setposXd(float posXd) {
		this.posXd = posXd;
	}


	
	public float getposYd() {
		return this.posYd;
	}
	
	
	
	public void setposYd(float posYd) {
		this.posYd = posYd;
	}
	
	
	
	public float getposZd() {
		return posZd;
	}



	public void setposZd(float posZd) {
		this.posZd = posZd;
	}

	public NodeRepresentation getParentNode() {
		return parentNode;
	}
	
	public void setParentNode(NodeRepresentation parentNode) {
		this.parentNode = parentNode;
	}
	
	public int getNodeLevel() {
		int nodeLevel = 0;
		NodeRepresentation parentNode;
		
		parentNode = this.parentNode;
		while (parentNode != null) {
			nodeLevel++;
			parentNode = parentNode.getParentNode();
		}
		
		return nodeLevel;
	}
	
	public int hashCode()
	{
		if (entityRepresentation != null)
			return this.entityRepresentation.getName().hashCode();
		return 0;
	}
	public boolean equals(Object n)
	{
		if (this.entityRepresentation != null && ((NodeRepresentation)n).entityRepresentation != null)
			return this.entityRepresentation.getName().equals(((NodeRepresentation)n).entityRepresentation.getName());
		return false;
	}
	
	//Fonctions pour gérer les opérations arithmétiques de bases sur les noeuds.
	
	public NodeRepresentation add(NodeRepresentation node) {
		/*
		this.posXd = this.posXd + node.getposXd();
		this.posYd = this.posYd + node.getposYd();
		this.posZd = this.posZd + node.getposZd();
		
		return this;
		*/
		
		return new NodeRepresentation(this.entityRepresentation, this.mesh, this.meshSize, this.posXd + node.getposXd(), this.posYd + node.getposYd(), this.posZd + node.getposZd(), this.color, this.parentNode);
	}
	
	public NodeRepresentation add(float value) {
		/*
		this.posXd = this.posXd + value;
		this.posYd = this.posYd + value;
		this.posZd = this.posZd + value;
		
		return this;
		*/
		
		return new NodeRepresentation(this.entityRepresentation, this.mesh, this.meshSize, this.posXd + value, this.posYd + value, this.posZd + value, this.color, this.parentNode);	
	}
	
	public NodeRepresentation substract(NodeRepresentation node) {
		/*
		this.posXd = this.posXd - node.getposXd();
		this.posYd = this.posYd - node.getposYd();
		this.posZd = this.posZd - node.getposZd();
		
		return this;
		*/
		
		return new NodeRepresentation(this.entityRepresentation, this.mesh, this.meshSize, this.posXd - node.getposXd(), this.posYd - node.getposYd(), this.posZd - node.getposZd(), this.color, this.parentNode);
	}

	public NodeRepresentation substract(float value) {
		/*
		this.posXd = this.posXd - value;
		this.posYd = this.posYd - value;
		this.posZd = this.posZd - value;
		
		return this;
		*/
		
		return new NodeRepresentation(this.entityRepresentation, this.mesh, this.meshSize, this.posXd - value, this.posYd - value, this.posZd - value, this.color, this.parentNode);	
	}
	
	public NodeRepresentation multiply(NodeRepresentation node) {
		/*
		this.posXd = this.posXd * node.getposXd();
		this.posYd = this.posYd * node.getposYd();
		this.posZd = this.posZd * node.getposZd();
		
		return this;
		*/
		
		return new NodeRepresentation(this.entityRepresentation, this.mesh, this.meshSize, this.posXd * node.getposXd(), this.posYd * node.getposYd(), this.posZd * node.getposZd(), this.color, this.parentNode);
	}

	public NodeRepresentation multiply(float value) {
		/*
		this.posXd = this.posXd * value;
		this.posYd = this.posYd * value;
		this.posZd = this.posZd * value;
		
		return this;
		*/
		
		return new NodeRepresentation(this.entityRepresentation, this.mesh, this.meshSize, this.posXd * value, this.posYd * value, this.posZd * value, this.color, this.parentNode);	
	}
	
	public NodeRepresentation divide(NodeRepresentation node) {
		/*
		this.posXd = this.posXd / node.getposXd();
		this.posYd = this.posYd / node.getposYd();
		this.posZd = this.posZd / node.getposZd();
		
		return this;
		*/	
		
		return new NodeRepresentation(this.entityRepresentation, this.mesh, this.meshSize, this.posXd / node.getposXd(), this.posYd / node.getposYd(), this.posZd / node.getposZd(), this.color, this.parentNode);
	}

	public NodeRepresentation divide(float value) {
		/*
		this.posXd = this.posXd / value;
		this.posYd = this.posYd / value;
		this.posZd = this.posZd / value;
		
		return this;
		*/
		
		return new NodeRepresentation(this.entityRepresentation, this.mesh, this.meshSize, this.posXd / value, this.posYd / value, this.posZd / value, this.color, this.parentNode);	
	}
	
	//*******************************************************************************************
	
	
	public NodeRepresentation copyNode() {
		return new NodeRepresentation(this.entityRepresentation, this.mesh, this.meshSize, this.posXd, this.posYd, this.posZd, this.color, this.parentNode);
	}

	public void render(GL gl) {
		/*
		if (this.isFiltered) {
			return;
		}
		*/
		
		gl.glPushMatrix();
			gl.glLoadName(VersoScene.id);
			VersoScene.pickingEntities.put(VersoScene.id++, this);
		
			gl.glColor3f(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f);
			gl.glTranslated(posXd, posYd, posZd);
			if (mesh != null) {
				gl.glScaled(meshSize, meshSize, meshSize);
				mesh.render(gl);
			}
			else {
				GLUT glut = new GLUT();
				glut.glutSolidSphere(meshSize, 8, 8);
			}
		gl.glPopMatrix();
		
		/*
		gl.glPushMatrix();
		gl.glColor3f(this.color.getRed()/255.0f,this.color.getGreen()/255.0f,this.color.getBlue()/255.0f);
		gl.glTranslated(0, this.height + 1, 0);
		GLUT glut = new GLUT();
		//glut.glutSolidSphere(0.1, 8, 8);
		//glut.glutSolidTeapot(0.1);

		gl.glPushMatrix();
			gl.glBegin(GL.GL_QUADS);
				gl.glVertex3f(0f, 0f, -0.05f);
				gl.glVertex3f(0f, 0f, 0.05f);
				gl.glVertex3f(0f, -1f, 0.05f);
				gl.glVertex3f(0f, -1f, -0.05f);

				gl.glVertex3f(-0.05f, 0f, 0f);
				gl.glVertex3f(0.05f, 0f, 0f);
				gl.glVertex3f(0.05f, -1f, 0f);
				gl.glVertex3f(-0.05f, -1f, 0f);
			gl.glEnd();
		gl.glPopMatrix();
		
		gl.glPushMatrix();
			//gl.glLoadIdentity();
			gl.glLineWidth(5.0f);
			gl.glBegin(GL.GL_LINES);
				//gl.glColor3f(1.0f, .5f, .5f);
				gl.glVertex3f(0f, 0f, 0f);
				gl.glVertex3f(0f, -1f, 0f);
			gl.glEnd();
		gl.glPopMatrix();
		
		
		gl.glPopMatrix();
		*/
	}
	
	public static NodeRepresentation getRealParentNode(NodeRepresentation node) {
		NodeRepresentation realNode = node.getParentNode();
		
		if (realNode.getEntityRepresentation() instanceof PackageRepresentation) {
			if (((PackageRepresentation)realNode.getEntityRepresentation()).isFakePackage()) {
				realNode = getRealParentNode(realNode);
			}
		}
		
		return realNode;
	}
}
