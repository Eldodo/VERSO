package verso.representation.cubeLandscape;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GLAutoDrawable;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;

import ca.umontreal.iro.utils.Config;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import textEditor.TextEditor;
import verso.graphics.VersoScene;
import verso.graphics.primitives.Cube;
import verso.model.ClassDef;
import verso.model.Element;
import verso.model.Method;
import verso.model.Package;
import verso.model.SystemDef;
import verso.representation.IPickable;
import verso.representation.cubeLandscape.Layout.TreeMapViascoLayout2;
import verso.representation.cubeLandscape.Layout.TreemapLayout;
import verso.representation.cubeLandscape.filter.EntityFilter;
import verso.representation.cubeLandscape.filter.IRFilter;
import verso.representation.cubeLandscape.filter.TargetAllFilter;
import verso.representation.cubeLandscape.filter.TargetClassFilter;
import verso.representation.cubeLandscape.filter.TargetMethodFilter;
import verso.representation.cubeLandscape.filter.TargetPackageFilter;
import verso.representation.cubeLandscape.modelVisitor.CubeLandScapeVisitor;
import verso.representation.cubeLandscape.representationModel.ClassRepresentation;
import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.EntityRepresentation;
import verso.representation.cubeLandscape.representationModel.MethodRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;
import verso.representation.cubeLandscape.representationModel.SystemRepresentation;
import verso.representation.cubeLandscape.representationModel.SystemRepresentation.LINK_TYPE;
import verso.representation.cubeLandscape.representationModel.TreemapPackageRepresentation;
import verso.representation.cubeLandscape.representationModel.link.EdgeBundleLinkRepresentation;
import verso.representation.cubeLandscape.representationModel.link.LinkRepresentation;
import verso.representation.cubeLandscape.representationModel.link.NodeRepresentation;
import verso.representation.cubeLandscape.representationModel.repvisitor.Mapping;
import verso.representation.cubeLandscape.representationModel.repvisitor.MappingVisitor;
import verso.representation.cubeLandscape.representationModel.repvisitor.PackageRenderHierarchicalVisitor;
import verso.traces.TracesUtil;
import verso.view.FilterView;
import verso.view.mapping.MappingModifier;

public class SceneLandscape extends VersoScene {
	
	private static final long serialVersionUID = 1L;
	private boolean displayLinks = false;
	public static boolean isRunning = false;
	protected boolean PIsDown = false;
	protected boolean QIsDown = false;
	
	protected boolean CTRLIsDown = false;
	
	protected boolean ZIsDown = false;
	protected boolean XIsDown = false;
	protected boolean CIsDown = false;
	protected boolean VIsDown = false;
	
	protected static UpdateEdgeBundles updateEdgeBundles;
	protected static boolean updateEdgeBundlesStarted = false;
	
	static Color SELECTION_LINE_COLOR = Color.red;
	static int SELECTION_RECT_FITNESS = 3;
	
	protected int selectionStartPosX;
	protected int selectionStartPosY;
	protected int selectionEndPosX;
	protected int selectionEndPosY;
	
	
	protected boolean isSelectingLinks = false;
	
	protected EntityFilter currentFilter ;
	protected boolean methodLevel = false;
	protected static int indice =0;
	protected SystemRepresentation sys;
	protected JPopupMenu jpopMenu;
	protected boolean currentlyFiltering = false;
	protected PackageRepresentation currentRoot = null;
	private Map<String,MappingVisitor> maplst = new HashMap<String,MappingVisitor>();
	JMenu mapping = null;
	MappingListener mapLis = new MappingListener();
	String currMapping = "";
	
	
	private FilterView filterView;
	private MouseListener classListener, packageListener, methodListener, noListener;
	
	public boolean isCurrentlyFiltering() {
		return this.currentlyFiltering;
	}
	
	public void targetClassFilter() {
		if(classListener==null ) classListener = new TargetClassFilterListener();
		this.addMouseListener(classListener);
		this.removeMouseListener(packageListener);
		this.removeMouseListener(methodListener);
		this.removeMouseListener(noListener);
	}
	public void targetPackageFilter() {
		if(packageListener==null ) packageListener = new TargetPackageFilterListener();
		this.addMouseListener(packageListener);
		this.removeMouseListener(classListener);
		this.removeMouseListener(methodListener);
		this.removeMouseListener(noListener);
	}
	public void targetMethodFilter() {
		if(methodListener==null ) methodListener = new TargetMethodFilterListener();
		this.addMouseListener(methodListener);
		this.removeMouseListener(packageListener);
		this.removeMouseListener(classListener);
		this.removeMouseListener(noListener);
	}
	public void noTargetFilter() {
		if(noListener==null ) noListener = new UnFilterListener();
		this.addMouseListener(noListener);
		this.removeMouseListener(packageListener);
		this.removeMouseListener(methodListener);
		this.removeMouseListener(classListener);
	}
	JMenu test = new JMenu("Entity Filters");
	private boolean leftMouseButtonDragged;
	public SceneLandscape(SystemRepresentation sysrep)
	{
		super();
		this.sys = sysrep;
		
		//On r�cup�re le package root et on set son centre � la cam�ra
		PackageRepresentation root = this.sys.getRootPackage();
		super.setCenterCamPos(root.getSizeX()/2, root.getSizeZ()/2);
		

		jpopMenu = new JPopupMenu();
		
		//Filter 
		//jpopMenu.add(new JMenuItem("No Filters")).addActionListener(new UnFilterListener());
		JMenu filter = new JMenu("Entity Filters");
		jpopMenu.add(filter);
		//filter.add(new JMenuItem("TargetClassFilter")).addActionListener(new TargetClassFilterListener());
		//filter.add(new JMenuItem("TargetPackageFilter")).addActionListener(new TargetPackageFilterListener());
		//filter.add(new JMenuItem("TargetMethodFilter")).addActionListener(new TargetMethodFilterListener());
		currentFilter = new EntityFilter(sys);
		JMenu filterLevel = new JMenu("Target at Level");
		filter.addSeparator();
		filter.add(filterLevel);
			TargetLevelFilterListener tlfl = new TargetLevelFilterListener();
			filterLevel.add(new JMenuItem("0")).addActionListener(tlfl);
			filterLevel.add(new JMenuItem("1")).addActionListener(tlfl);
			filterLevel.add(new JMenuItem("2")).addActionListener(tlfl);
			filterLevel.add(new JMenuItem("3")).addActionListener(tlfl);
			filterLevel.add(new JMenuItem("4")).addActionListener(tlfl);
			filterLevel.add(new JMenuItem("5")).addActionListener(tlfl);
			filterLevel.add(new JMenuItem("6")).addActionListener(tlfl);
			filterLevel.add(new JMenuItem("7")).addActionListener(tlfl);
			filterLevel.add(new JMenuItem("8")).addActionListener(tlfl);
			filterLevel.add(new JMenuItem("9")).addActionListener(tlfl);
			filterLevel.add(new JMenuItem("10")).addActionListener(tlfl);
		
		//IRFilter
		JMenu filterIR = new JMenu("IR Filters");
		filterIR.setEnabled(false);
		jpopMenu.add(filterIR);
		IRFilterListener irfl = new IRFilterListener();
		File irFolder = Config.irFolder;				
		File[] irs = irFolder.listFiles();
		if(!irFolder.exists())
			System.err.println(irFolder.getAbsolutePath()+ "does not exist so no ir filter available\n");
		else if(!irFolder.isDirectory())
			System.err.println(irFolder.getAbsolutePath()+" is not a directory so no ir filter available\n");
		else if(irs == null)
			System.err.println(irFolder.getAbsolutePath()+ " does not contain any ir filter\n");
		else {
			filterIR.setEnabled(true);
			for(File ir : irs) {
				if(ir.exists() && !ir.isDirectory())
					filterIR.add(new JCheckBoxMenuItem(ir.getName().substring(0, ir.getName().length()-4))).addActionListener(irfl);
			}
			filterIR.addSeparator();			
			JMenuItem allItem = new JMenuItem("All concepts");
			allItem.addActionListener(irfl);
			filterIR.add(allItem);
			
			filterIR.addSeparator();			
			JMenuItem jmi = new JMenuItem("...");
			jmi.setEnabled(false);
			filterIR.add(jmi);
		}
		jpopMenu.addSeparator();
		
		//Mapping 
		mapping = new JMenu("Mapping");
		jpopMenu.add(mapping);
//		JMenuItem quality = new JMenuItem("Quality");
		//JMenuItem vc = new JMenuItem("Version Control");
		//JMenuItem bugButton = new JMenuItem("Bug");
		JMenuItem showName = new JMenuItem("Show names");
		JMenuItem modifyMapping = new JMenuItem("Modify Mapping");
		
		showName.addActionListener(new ShowNamesListener());
		jpopMenu.addSeparator();
		jpopMenu.add(showName);
//		mapping.add(quality).addActionListener(new QualityListener());
		//mapping.add(vc).addActionListener(new ControlVersionListener());
		//mapping.add(bugButton).addActionListener(new BugListener());
		
		//Ajout filtre de recherche
		
		
		mapping.add(modifyMapping).addActionListener(new ModifyMappingListener());
		/*System.out.println("--\n--\n--\n--\n--\n--");
		Collection<ElementRepresentation> test = this.sys.getNodesElements();
		for(ElementRepresentation elem : test) {
			System.out.println(elem.getName());
		}
		System.out.println("--\n--\n--\n--\n--\n--");
		*/
		sys.filterAll();
		
		//launchFilterView();
		
		
		
	}
	


	


	
	public void setCurrentRoot(PackageRepresentation newRoot) {
		this.currentRoot = newRoot;
	}

	public SystemRepresentation getSysRep() {
		return this.sys;
	}
	
	public void setSysRep(SystemRepresentation sysrep) {
		this.sys = sysrep;
		this.currentFilter.setSystem(sysrep);
	}

	public void init(GLAutoDrawable glauto) {
		super.init(glauto);
		Cube.createDisplayList(glauto.getGL());
		glPanel.repaint();
	}
	
	
	

	
	
	
	
	
	public void checkCamPos()
	{
		System.out.println("Camera [X="+cam.getX()+" ; Y="+cam.getY()+" ; Z="+cam.getZ()+"]");
		System.out.println("LookAt [X="+cam.getLookAtX()+" ; Y="+cam.getLookAtY()+" ; Z="+cam.getLookAtZ()+"]");
		System.out.println("Normal [X="+cam.getNormalX()+" ; Y="+cam.getNormalY()+" ; Z="+cam.getNormalZ()+"]");
	}
	
	public void mousePressed(MouseEvent me) {
		if (me.getButton() == MouseEvent.BUTTON1) {
			this.selectionStartPosX = me.getX();
			this.selectionStartPosY = me.getY();
		}
	} 
	
	public void mouseDragged(MouseEvent me) {
		
		if (me.getButton() == MouseEvent.BUTTON1) {
			this.leftMouseButtonDragged = true;
			
			glPanel.display();
			
			int startX, startY;
			
			int selectionWidth = me.getX() - this.selectionStartPosX;
			if (selectionWidth < 0) {
				selectionWidth = -selectionWidth;
				startX = me.getX();
			}
			else {
				startX = this.selectionStartPosX;
			}
			
			int selectionHeight = me.getY() - this.selectionStartPosY;
			if (selectionHeight < 0) {
				selectionHeight = -selectionHeight;
				startY = me.getY();
			}
			else {
				startY = this.selectionStartPosY;
			}
			
			/*
			Color ancienneCouleur = glPanel.getGraphics().getColor();
			System.out.println("Ancienne couleur: R: " + ancienneCouleur.getRed() + "  G: " + ancienneCouleur.getGreen() + "  B: " + ancienneCouleur.getBlue());
			*/
			
			Graphics2D g2D = (Graphics2D) glPanel.getGraphics();
			
			//g2D.setColor(Color.cyan);
			
			/*
			Color nouvelleCouleur = glPanel.getGraphics().getColor();
			System.out.println("Nouvelle couleur: R: " + nouvelleCouleur.getRed() + "  G: " + nouvelleCouleur.getGreen() + "  B: " + nouvelleCouleur.getBlue());
			*/
			
			if (this.VIsDown) {
				for (int[] rectangle : this.selectionRectangles) {
					//glPanel.getGraphics().drawRect(rectangle[0] - rectangle[2]/2, rectangle[1] - rectangle[3]/2, rectangle[2], rectangle[3]);
					//g2D.drawRect(rectangle[0] - rectangle[2]/2, rectangle[1] - rectangle[3]/2, rectangle[2], rectangle[3]);
					drawThickRect(g2D, SELECTION_LINE_COLOR, SELECTION_RECT_FITNESS, rectangle[0] - rectangle[2]/2, rectangle[1] - rectangle[3]/2, rectangle[2], rectangle[3]);
				}
			}
			
			//glPanel.getGraphics().drawRect(startX, startY, selectionWidth, selectionHeight);
			//g2D.drawRect(startX, startY, selectionWidth, selectionHeight);
			
			drawThickRect(g2D, SELECTION_LINE_COLOR, SELECTION_RECT_FITNESS, startX, startY, selectionWidth, selectionHeight);
		}
		
	}
	
	
	private void drawThickRect(Graphics2D g2D, Color lineColor, int rectThickness, int startX, int startY, int selectionWidth, int selectionHeight) {
		g2D.setColor(lineColor);
		g2D.fillRect(startX, startY, selectionWidth, rectThickness);
		g2D.fillRect(startX, startY, rectThickness, selectionHeight);
		g2D.fillRect(startX, startY + selectionHeight - rectThickness, selectionWidth, rectThickness);
		g2D.fillRect(startX + selectionWidth - rectThickness, startY, rectThickness, selectionHeight);
	}
	
	
	public void mouseReleased(MouseEvent me) {
		
		if (this.leftMouseButtonDragged) {
			this.leftMouseButtonDragged = false;
			
			int startX, startY;
			int selectionWidth = me.getX() - this.selectionStartPosX;
			int selectionHeight = me.getY() - this.selectionStartPosY;
			
			if (Math.abs(selectionWidth) > 5 || Math.abs(selectionHeight) > 5) {
				startX = this.selectionStartPosX + selectionWidth/2;
				startY = this.selectionStartPosY + selectionHeight/2;
				selectionWidth = Math.abs(selectionWidth);
				selectionHeight = Math.abs(selectionHeight);
				
				int []newSelectionRectangle = {startX, startY, selectionWidth, selectionHeight};
				this.selectionRectangles.add(newSelectionRectangle);
				
				if (!this.VIsDown) {
					this.updateSelection();
					this.selectionRectangles.clear();
				}
			}
			else {
				this.mouseClicked(me);
			}
		}
	}
	
	protected void updateSelection() {
		LinkedList<IPickable> itemsToRemove = new LinkedList<IPickable>();
		HashSet<IPickable> refinedSelectedItems = new HashSet<IPickable>();
		
		for (int[] rectangle : this.selectionRectangles) {
			
			//this.dirtyList = true;
			//this.sys.setDirty();
			
			this.pick(rectangle[0], rectangle[1], rectangle[2], rectangle[3]);
			refinedSelectedItems.addAll(this.selectedItems);
		}
		
		if (!this.isSelectingLinks) {
			for (IPickable item : refinedSelectedItems) {
				if (item instanceof PackageRepresentation) {
					if (!((PackageRepresentation)item).isRendered()) {
						itemsToRemove.add(item);
					}
				}
				else if (!(item instanceof ElementRepresentation)) {
					itemsToRemove.add(item);
				}
			}
			
			for (IPickable item : itemsToRemove) {
				refinedSelectedItems.remove(item);
			}
			
			
			if (!this.CIsDown) {
				this.sys.selectedElements.clear();
			}
			
			
			for (IPickable item : refinedSelectedItems) {
				this.sys.selectedElements.add((EntityRepresentation)item);
			}
			
			
			this.sys.selectedLinks.clear();
			
			
			if (this.sys.selectedElements.size() > 0) {
				currentFilter.unfilter();
				for (IPickable element : this.sys.selectedElements) {
					currentFilter.setElement(element);
				}
				
				this.sys.filterByElements(this.sys.selectedElements);
			}
			else {
				currentFilter.unfilter();
				this.sys.unfilterAll();
			}
		}
		else {
			for (IPickable item : refinedSelectedItems) {
				if (!(item instanceof LinkRepresentation)) {
					itemsToRemove.add(item);
				}
			}
			
			for (IPickable item : itemsToRemove) {
				refinedSelectedItems.remove(item);
			}
			
			//Pas besoins de v�rifier CisDown, car on pourrait jamais ajouter d'autres liens
			//apr�s en avoir s�lectionner (on peut seulement rafiner de plus en plus la s�lection)
			
			
			//*******************************************************************************
			//SAUF POUR S�LECTION MULTIPLE (PLUSIEURS RECTANGLES/ZONES DE S�LECTION)!!!!!!!!!
			//*******************************************************************************
			
			
			
			this.sys.selectedLinks.clear();
			
			for (IPickable item : refinedSelectedItems) {
				this.sys.selectedLinks.add((LinkRepresentation)item);	
			}
			
			
			if (this.sys.selectedLinks.size() > 0) {
				this.sys.filterByLinks(this.sys.selectedLinks);
			}
			else {
				if (this.sys.selectedElements.size() > 0) {
					currentFilter.unfilter();
					for (IPickable element : this.sys.selectedElements) {
						currentFilter.setElement(element);
					}
					
					this.sys.filterByElements(this.sys.selectedElements);
				}
				else {
					currentFilter.unfilter();
					this.sys.unfilterAll();
				}
			}
			
			/*
			HashSet<NodeRepresentation> refinedSelectedNodes = new HashSet<NodeRepresentation>();
			for (LinkRepresentation link : refinedSelectedLinks) {
				refinedSelectedNodes.add(link.getStartNode());
				refinedSelectedNodes.add(link.getEndNode());
			}
			*/
		}
	
		dirtyList = true;
		this.sys.setDirty();
		glPanel.display();
	}
	
	
	
	public void mouseMoved(MouseEvent me)
	{
		super.mouseMoved(me);
		//checkCamPos();
		
	}
	
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		super.mouseWheelMoved(e);
		//checkCamPos();
	}
	
	public void mouseDoubleClick(MouseEvent me) {
		if (me.getButton() == MouseEvent.BUTTON1) {
			IPickable p = pick(me.getX(),me.getY());
			
			if (p instanceof PackageRepresentation) {
				sys.expandPackage((PackageRepresentation)p);					
				sys.updateNodesDisplayed();
				sys.updateLinksDisplayed();
				
				if (this.sys.selectedElements.contains(p)) {
					this.sys.selectedElements.remove(p);
					for (PackageRepresentation subPack : ((PackageRepresentation)p).getSubPackages()) {
						this.sys.selectedElements.add(subPack);
					}
					
					for (ElementRepresentation element : ((PackageRepresentation)p).getElements()) {
						this.sys.selectedElements.add(element);
					}
					
					sys.filterByElements(this.sys.selectedElements);
				}
				
				redisplay();
			}
		}
	}
	
	public void mouseClicked(MouseEvent me)
	{		
		if (me.getButton() == MouseEvent.BUTTON3) {
			jpopMenu.show(this, me.getX(), me.getY());
			return;
		}
		
		if (me.getClickCount() == 2) {
			this.mouseDoubleClick(me);
			return;
		}
		
		super.mouseClicked(me);
		IPickable p = pick(me.getX(),me.getY());
		
		if(me.isAltDown()) { //When you click on an class an you press CTRL it will open the code of the class
			System.out.println("ALT PRESSED - Open Java Code Window");
			if (p.isElement()) {
				EntityRepresentation entityRep = (EntityRepresentation)p;
				String path = entityRep.getPath(Config.srcFolderPath);
				TextEditor textEditor = new TextEditor(path);
				if(currentFilter instanceof IRFilter) {
					IRFilter filter = (IRFilter) currentFilter;
					textEditor.highlight(filter.getConceptsWords());
				}
			}else {
				System.err.println("Selected element is not a class");
			}
			return;
		}
		if(sys.displayTracesHeatMap)
			if(p instanceof EntityRepresentation) {
				for (EntityRepresentation e : sys.selectedElements) {
					e.unSelect();
				}
				sys.selectedElements.clear();
				sys.selectedElements.add((EntityRepresentation)p);
				((EntityRepresentation) p).select();
				dirtyList = true;
				this.sys.setDirty();
				glPanel.display();
				
				return;
			}
		
		if (!this.isDisplayLinks()) ///Guillaume's stuff
		{
			if (this.currentlyFiltering && !this.PIsDown && !this.QIsDown && !this.ZIsDown && !this.XIsDown)
			{
				currentFilter.unfilter();
				currentFilter.setElement(p);
				dirtyList = true;
				this.sys.setDirty();
				glPanel.display();
				return;
			}
		
			if(p == null) {
				System.out.println("pas d'�l�ments");
			}
			else
			{
				EntityRepresentation er = (EntityRepresentation)p;
				
				if (er.getEntity() != null) {
					System.out.println(er.getEntity().getName());
				}
				
				if (this.PIsDown) {
					if (er instanceof TreemapPackageRepresentation) {
						if (((TreemapPackageRepresentation) er).isRendered())
							((TreemapPackageRepresentation) er).setRender(false);
						else
							((TreemapPackageRepresentation) er).setRender(true);
						// dirtyList = true;
					}
					glPanel.display();
				}
				if (this.QIsDown) {
					if (er instanceof TreemapPackageRepresentation) {
						TreemapPackageRepresentation pr = ((TreemapPackageRepresentation) er);
						String packageName = pr.getPackage().getName();

						if (packageName.contains(".")) {
							String nameToSearch = packageName.substring(0, packageName.lastIndexOf("."));
							PackageRepresentation parent = this.currentRoot.findPackage(nameToSearch);
							if (parent != null)
								parent.setRender(true);
						}
					}
				}
			
			}
		}
		
		
		else if (this.isDisplayLinks()) {
			
			//QUAND NI P, NI Q, NI Z, NI X NE SONT PRESSED
			if (!this.PIsDown && !this.QIsDown && !this.ZIsDown && !this.XIsDown) {
				
				//JE NE SAIS PAS.. SI CEST EN MODE SELECTION ?
				if (!this.isSelectingLinks) {
					//ON VERIFIE QUE P EST VALIDE
					if (p != null && p instanceof EntityRepresentation) {
						
						//SI CEST UNE INSTANCE DE NODE, EN GET L'ENTITY
						if (p instanceof NodeRepresentation) {
							p = ((NodeRepresentation) p).getEntityRepresentation();
						}
						//SI P EST UN PACKAGE OU UN ELEMENT
						if (p.isPackage() || p.isElement()) {
							EntityRepresentation entity = (EntityRepresentation)p;
							
							LinkedList<EntityRepresentation> subElements = new LinkedList<EntityRepresentation>();
							//SI L'ENTITE EST UN PACKAGE, ON AJOUTE TOUTES SES DESCENDANCES
							if (entity.isPackage() && !((PackageRepresentation)entity).isRendered()) {
								System.out.println(entity.getSimpleName()+"[X:"+entity.getSizeX()+" - Z:"+entity.getSizeZ()+"]");
								subElements.addAll(this.sys.findPackageRenderedDescendants((PackageRepresentation)entity));
								subElements.addAll(this.sys.findPackageRenderedDescendantElements((PackageRepresentation)entity));
							}	
							
							//SI P N'EST PAS PRESSED
							if (!this.CIsDown) {
								//On n'efface pas si CTRL est pressed, pour pouvoir en s�l�ctionner plusieurs
								if(!this.CTRLIsDown)
									this.sys.selectedElements.clear();
								
								if (subElements.size() > 0) {
									if (this.CTRLIsDown && this.sys.selectedElements.containsAll(subElements)) {
										this.sys.selectedElements.removeAll(subElements);
									}
									else
										this.sys.selectedElements.addAll(subElements);
								}
								else {
									if(this.CTRLIsDown && this.sys.selectedElements.contains(entity)) {
										this.sys.selectedElements.remove(entity);
									}
									else this.sys.selectedElements.add(entity);
								}
							} else {
								//SI IL Y A UN SOUCIS AVEC LE CTRL C'EST ICI QUE CA SE PASSE JE PENSE!
								//ON AJOUTE TOUTE LA LISTE DE SUBELEMENT
								if (subElements.size() > 0) {
									if (this.sys.selectedElements.containsAll(subElements)) {
										this.sys.selectedElements.removeAll(subElements);
									}
									else {
										for (EntityRepresentation element : subElements) {
											if (!this.sys.selectedElements.contains(element)) {
												this.sys.selectedElements.add(element);
											}
										}
									}
								} else {
									if (this.sys.selectedElements.contains(entity)) {
										this.sys.selectedElements.remove(entity);
									}
									else {
										this.sys.selectedElements.add(entity);
									}
								}
							}
						}
					}
					else {
						this.sys.selectedElements.clear();
					}

					
					this.sys.selectedLinks.clear();
					
					
					if (this.sys.selectedElements.size() > 0) {
						currentFilter.unfilter();
						for (IPickable element : new HashSet<IPickable>(this.sys.selectedElements)) {
							currentFilter.setElement(element);
						}
						
						this.sys.filterByElements(this.sys.selectedElements);
					} else {
						currentFilter.unfilter();
						this.sys.unfilterAll();
					}
				}
				else {
					this.sys.selectedLinks.clear();
					
					if (p != null && p instanceof LinkRepresentation) {
						LinkRepresentation link = (LinkRepresentation)p;
						
						//if (!this.CIsDown) {
						//	this.selectedLinks.clear();
						//}
						
						this.sys.selectedLinks.add(link);
						
						this.sys.filterByLinks(this.sys.selectedLinks);
					}
					else {
						if (this.sys.selectedElements.size() > 0) {
							currentFilter.unfilter();
							for (IPickable element : this.sys.selectedElements) {
								currentFilter.setElement(element);
							}
							
							this.sys.filterByElements(this.sys.selectedElements);
						}
						else {
							currentFilter.unfilter();
							this.sys.unfilterAll();
						}
					}
				}
				
				//currentFilter.unfilter();
				//currentFilter.setElement(p);
				
				dirtyList = true;
				this.sys.setDirty();
				glPanel.display();
				
				return;
			}
			
			if (this.ZIsDown) {
				if (p instanceof PackageRepresentation) {
					sys.expandPackage((PackageRepresentation)p);					
					sys.updateNodesDisplayed();
					sys.updateLinksDisplayed();
					
					if (this.sys.selectedElements.contains(p)) {
						this.sys.selectedElements.remove(p);
						for (PackageRepresentation subPack : ((PackageRepresentation)p).getSubPackages()) {
							this.sys.selectedElements.add(subPack);
						}
						
						for (ElementRepresentation element : ((PackageRepresentation)p).getElements()) {
							this.sys.selectedElements.add(element);
						}
					}
					
					
					
					
					LinkedList<NodeRepresentation> selectedNodeToP = new LinkedList<NodeRepresentation>();
					LinkRepresentation link;
					Iterator<LinkRepresentation> linkItr;
					for (linkItr = this.sys.selectedLinks.iterator(); linkItr.hasNext();) {
						link = linkItr.next();
						if (link.getStartNode().getEntityRepresentation() == p) {
							selectedNodeToP.add(link.getEndNode());
							linkItr.remove();
						}
						else if (link.getEndNode().getEntityRepresentation() == p) {
							selectedNodeToP.add(link.getStartNode());
							linkItr.remove();
						}
					}

					
					HashMap<NodeRepresentation, LinkRepresentation> linksToSubEntity = new HashMap<NodeRepresentation, LinkRepresentation>();
					HashMap<NodeRepresentation, LinkRepresentation> tempLinksToSubEntity;
					for (PackageRepresentation subPack : ((PackageRepresentation)p).getSubPackages()) {
						linksToSubEntity.clear();
						
						tempLinksToSubEntity = sys.findAllLinkStartingWith(sys.getPackageNode(subPack));
						if (tempLinksToSubEntity != null) {
							linksToSubEntity.putAll(tempLinksToSubEntity);
						}
						
						tempLinksToSubEntity = sys.findAllLinkEndingWith(sys.getPackageNode(subPack));
						if (tempLinksToSubEntity != null) {
							linksToSubEntity.putAll(tempLinksToSubEntity);
						}
						
						
						for (NodeRepresentation node : selectedNodeToP) {
							link = linksToSubEntity.get(node);
							if (link != null) {
								this.sys.selectedLinks.add(link);
							}
						}
					}
					
					for (ElementRepresentation subElement : ((PackageRepresentation)p).getSubElements()) {
						linksToSubEntity.clear();
						
						tempLinksToSubEntity = sys.findAllLinkStartingWith(sys.getElementNode(subElement));
						if (tempLinksToSubEntity != null) {
							linksToSubEntity.putAll(tempLinksToSubEntity);
						}
						
						tempLinksToSubEntity = sys.findAllLinkEndingWith(sys.getElementNode(subElement));
						if (tempLinksToSubEntity != null) {
							linksToSubEntity.putAll(tempLinksToSubEntity);
						}
						
						for (NodeRepresentation node : selectedNodeToP) {
							link = linksToSubEntity.get(node);
							if (link != null) {
								this.sys.selectedLinks.add(link);
							}
						}
					}
					
					if (!this.isSelectingLinks) {
						if (this.sys.selectedElements.size() > 0) {
							currentFilter.unfilter();
							for (IPickable element : this.sys.selectedElements) {
								currentFilter.setElement(element);
							}
							
							sys.filterByElements(this.sys.selectedElements);
						}
					}
					else {
						if (this.sys.selectedLinks.size() > 0) {
							sys.filterByLinks(this.sys.selectedLinks);
						}
					}
					
					redisplay();
				}
			}
			
			if (this.XIsDown) {
				if (p instanceof PackageRepresentation || p instanceof ElementRepresentation) {
					PackageRepresentation parentPack;
					// ******************************� FAIRE ATTENTION POUR LE RADIAL!!!!******************************
					if (p instanceof PackageRepresentation && !((PackageRepresentation) p).isRendered()) {
						parentPack = (PackageRepresentation) p;
					} else {
						parentPack = this.sys.findParentPackage((EntityRepresentation) p);
					}

					if (parentPack == null)
						return;

					
					if (this.sys.selectedElements.size() > 0) {
						boolean isSelected = false;
						for (PackageRepresentation subPack : parentPack.getSubPackages()) {
							if (this.sys.selectedElements.remove(subPack)) 
								isSelected = true;
						}

						for (ElementRepresentation subElement : parentPack.getElements()) {
							if (this.sys.selectedElements.remove(subElement)) 
								isSelected = true;
						}

						if (isSelected) {
							this.sys.selectedElements.add(parentPack);
						}
					}

					if (p instanceof PackageRepresentation) {
						sys.closePackage((PackageRepresentation) p);
					} else {
						sys.closePackage((ElementRepresentation) p);
					}

					sys.updateNodesDisplayed();
					sys.updateLinksDisplayed();
					
					HashSet<NodeRepresentation> subEntityNodes = new HashSet<NodeRepresentation>();
					for (PackageRepresentation subPack : parentPack.getSubPackages()) 
						subEntityNodes.add(sys.getPackageNode(subPack));
					
					
					for (ElementRepresentation subElement : parentPack.getElements()) 
						subEntityNodes.add(sys.getElementNode(subElement));
					
					
					LinkedList<NodeRepresentation> selectedNodeToParentPack = new LinkedList<NodeRepresentation>();
					LinkRepresentation link;
					Iterator<LinkRepresentation> linkItr;
					for (linkItr = this.sys.selectedLinks.iterator(); linkItr.hasNext();) {
						link = linkItr.next();
						if (subEntityNodes.contains(link.getStartNode())) {
							selectedNodeToParentPack.add(link.getStartNode());
							linkItr.remove();
						}
						else if (subEntityNodes.contains(link.getEndNode())) {
							selectedNodeToParentPack.add(link.getEndNode());
							linkItr.remove();
						}
					}

					
					HashMap<NodeRepresentation, LinkRepresentation> linksToParentPack = new HashMap<NodeRepresentation, LinkRepresentation>();
					HashMap<NodeRepresentation, LinkRepresentation> tempLinksToParentPack;
					
					tempLinksToParentPack = sys.findAllLinkStartingWith(sys.getPackageNode(parentPack));
					if (tempLinksToParentPack != null) {
						linksToParentPack.putAll(tempLinksToParentPack);
					}
					
					tempLinksToParentPack = sys.findAllLinkEndingWith(sys.getPackageNode(parentPack));
					if (tempLinksToParentPack != null) {
						linksToParentPack.putAll(tempLinksToParentPack);
					}
					
					for (NodeRepresentation node : selectedNodeToParentPack) {
						if (linksToParentPack.get(node) != null) {
							this.sys.selectedLinks.add(linksToParentPack.get(node));
						}
					}
					
					if (!this.isSelectingLinks) {
						if (this.sys.selectedElements.size() > 0) {
							currentFilter.unfilter();
							for (IPickable element : this.sys.selectedElements) {
								currentFilter.setElement(element);
							}
							this.sys.filterByElements(this.sys.selectedElements);
						}
					} else {
						if (this.sys.selectedLinks.size() > 0) {
							sys.filterByLinks(this.sys.selectedLinks);
						}
					}

					redisplay();
				}				
			}
			
		}
	}
	
	
	public void keyPressed(KeyEvent ke) {
		super.keyPressed(ke);
		
		switch (ke.getKeyCode()) {
			case KeyEvent.VK_B:
				ElementRepresentation.render = !ElementRepresentation.render;
				redisplay();
				//System.out.println("B is pressed");
				break;
			case KeyEvent.VK_P:
				this.PIsDown = true;
				//System.out.println("P is pressed");
				break;
			case KeyEvent.VK_Q:
				this.QIsDown = true;
				//System.out.println("Q is pressed");
				break;
			case KeyEvent.VK_Z:
				this.ZIsDown = true;
				//System.out.println("Z is pressed");
				break;
			case KeyEvent.VK_X:
				this.XIsDown = true;
				//System.out.println("X is pressed");
				break;
			case KeyEvent.VK_C:
				this.CIsDown = true;
				//System.out.println("C is pressed");
				break;
			case KeyEvent.VK_V:
				this.VIsDown = true;
				//System.out.println("V is pressed");
				break;
			case KeyEvent.VK_CONTROL:
				//System.out.println("CTRL is pressed");
				this.CTRLIsDown = true;
				break;
			
		}
		
	}
	
	public void keyReleased(KeyEvent ke){
		super.keyReleased(ke);
		switch (ke.getKeyCode()) {
		case KeyEvent.VK_P:
			this.PIsDown = false;
			break;
		case KeyEvent.VK_Q:
			this.QIsDown = false;
			break;
		case KeyEvent.VK_Z:
			this.ZIsDown = false;
			break;
		case KeyEvent.VK_X:
			this.XIsDown = false;
			break;
		case KeyEvent.VK_C:
			this.CIsDown = false;
			break;
		case KeyEvent.VK_V:
			System.out.println("V released: selection rectangle update");
			this.VIsDown = false;

			if (this.selectionRectangles.size() > 0) {
				this.updateSelection();
				this.selectionRectangles.clear();
			}
			break;
		case KeyEvent.VK_N:
//			System.out.println("N released");
//			this.sys.oldRenderingPoints = !this.sys.oldRenderingPoints;
//			this.sys.setEdgeBundlesOldRenderingPoints();
//			this.refreshScene();
			break;
		case KeyEvent.VK_A:
			System.out.println("A released: refresh links");

			this.sys.displayIntraPackageLinks = !this.sys.displayIntraPackageLinks;
			this.sys.updateLinksDisplayed();
			
			redisplay();
			break;
		case KeyEvent.VK_O:
			System.out.println("O released: switch package level group");

			int pacLevel = (TreemapPackageRepresentation.nowShowingPackageLevel + 1) % (TreemapPackageRepresentation.maxPacLevel+1);
			TreemapPackageRepresentation.nowShowingPackageLevel = pacLevel;
			PackageRenderHierarchicalVisitor prv = new PackageRenderHierarchicalVisitor(pacLevel);
			this.currentRoot.accept(prv);
			redisplay();
			break;
		case KeyEvent.VK_W:
			EntityRepresentation.hideFilteredClasses = !EntityRepresentation.hideFilteredClasses;
			redisplay();
			break;
		case KeyEvent.VK_F:
			if (this.sys.linksFilterType != SystemRepresentation.LINKFILTER_TYPE.FILTER_INTRASELECTION) {
				this.sys.linksFilterType = SystemRepresentation.LINKFILTER_TYPE.FILTER_INTRASELECTION;
				this.sys.changePackageSelectionColor(Color.cyan);
			} else {
				this.sys.linksFilterType = SystemRepresentation.LINKFILTER_TYPE.NO_FILTER;
				this.sys.changePackageSelectionColor(Color.green);
			}

			if (this.sys.selectedElements.size() > 0) {
				this.sys.filterByElements(this.sys.selectedElements);
				redisplay();
			}
			break;
		case KeyEvent.VK_R:
			if (this.sys.linksFilterType != SystemRepresentation.LINKFILTER_TYPE.FILTER_EXTRASELECTION) {
				this.sys.linksFilterType = SystemRepresentation.LINKFILTER_TYPE.FILTER_EXTRASELECTION;
				this.sys.changePackageSelectionColor(new Color(255, 105, 255));
				// this.sys.changePackageSelectionColor(Color.PINK);
			} else {
				this.sys.linksFilterType = SystemRepresentation.LINKFILTER_TYPE.NO_FILTER;
				this.sys.changePackageSelectionColor(Color.green);
			}

			if (this.sys.selectedElements.size() > 0) {
				this.sys.filterByElements(this.sys.selectedElements);
				redisplay();
			}
			break;
		case KeyEvent.VK_G:
			this.isSelectingLinks = !this.isSelectingLinks;
			System.out.println("G released: selecting links "+(isSelectingLinks?"on":"off"));

			
			if (!this.isSelectingLinks) {
				if (this.sys.selectedElements.size() > 0) {
					currentFilter.unfilter();
					for (IPickable element : this.sys.selectedElements) {
						currentFilter.setElement(element);
					}

					this.sys.filterByElements(this.sys.selectedElements);
				} else {
					currentFilter.unfilter();
					this.sys.unfilterAll();
				}
			} else {
				if (this.sys.selectedLinks.size() > 0) {
					this.sys.filterByLinks(this.sys.selectedLinks);
				}
			}
			
			redisplay();
			break;
		case KeyEvent.VK_L:
			int nbrePolygonsDisplayed = this.sys.getNbreDisplayedPolygons();
			
			long beforeNbreMS = System.currentTimeMillis();
			
			for (int i = 0; i < 60; i++) {
				//this.dirtyList = true;
				//this.sys.setDirty();
				glPanel.display();
				//this.sys.render(glPanel.getGL());
			}
			
			long afterNbreMS = System.currentTimeMillis();
			System.out.println("Nombre de polygones: " + nbrePolygonsDisplayed);
			System.out.println("Temps pour 60 rendus (en ms): " + (afterNbreMS - beforeNbreMS));
			break;
		case KeyEvent.VK_Y:
			EdgeBundleLinkRepresentation.totalNanoTime = (long)0.0;
			EdgeBundleLinkRepresentation.totalGreaterThanNS = 0;
			EdgeBundleLinkRepresentation.totalGreaterNanoTime = (long)0.0;
			
			
			this.sys.updateNodesDisplayed();
			
			
			System.out.println("Edges en calcul\n");
			
			System.out.println("Nombre de EdgeBundles: " + this.sys.getNbreLinks());
			System.out.println("Nombre de EdgeBundles qui sont affich�es: " + this.sys.getNbreDisplayedLinks());
			break;
		case KeyEvent.VK_I:
			if (this.sys.linksType == SystemRepresentation.LINK_TYPE.INVOCATION) {
				this.sys.linksType = SystemRepresentation.LINK_TYPE.PARENT;
			}
			else {
				this.sys.linksType = SystemRepresentation.LINK_TYPE.INVOCATION;
			}
			
			this.sys.updateLinksDisplayed();
			
			redisplay();
			break;
		case KeyEvent.VK_U:
			if (this.sys.linksType != SystemRepresentation.LINK_TYPE.TRACES) {
				this.sys.linksType = SystemRepresentation.LINK_TYPE.TRACES;
			}
			else {
				this.sys.linksType = SystemRepresentation.LINK_TYPE.INVOCATION;
			}
			
			this.sys.updateLinksDisplayed();
			
			redisplay();
			break;
		case KeyEvent.VK_T:
			sys.displayTracesHeatMap = !sys.displayTracesHeatMap;
			updateHeatMap();
			this.sys.updateLinksDisplayed();
			redisplay();
			break;
		case KeyEvent.VK_CONTROL:
			this.checkCamPos();
			this.CTRLIsDown=false;
			break;
		default:
			break;
		}
		
	}
	
	public void updateHeatMap() {
		if(sys.displayTracesHeatMap && sys.getNumberOfCalls() != null) {
			Collection<ElementRepresentation> nodes = sys.getNodesElements();
			for(ElementRepresentation e : nodes) {
				if(e.getSavedColor()== null) {
					e.saveCurrentColor();
				}
				Double value = sys.getNumberOfCalls().get(e.getEntity().getName());
				if(value !=null){
					e.setColor(TracesUtil.getColorValueAt(sys.getFirstColor(), sys.getSecondColor(), value.doubleValue()));
				}
				else
					e.setColor(new Color(0,0,0));
			}
			sys.linksType = LINK_TYPE.NONE;
		}
		else {
			Collection<ElementRepresentation> nodes = sys.getNodesElements();
			for(ElementRepresentation e : nodes) {
				if(e.getSavedColor() != null)
					e.restoreSavedColor();
			}
			sys.linksType = LINK_TYPE.INVOCATION;
		}
	}
	
	
	public static void start(SystemDef sys) {
		SystemRepresentation sysrep = (SystemRepresentation) sys.accept(new CubeLandScapeVisitor());
		SceneLandscape sc = new SceneLandscape(sysrep);
		// sc.addRenderable(new Cartesian());
		TreemapPackageRepresentation pRoot = new TreemapPackageRepresentation(new Package("root"));
		for (PackageRepresentation pr : sysrep.getPackages()) {
			pRoot.addPackage(pr);
		}
		// Part added for Viasco should be removed when using something else.
		// PAY ATTENTION I'M PUTTING VIASCO LAYOUT 2 NOW
		{
			TreeMapViascoLayout2 vl = new TreeMapViascoLayout2();
			// ViascoLayout vl = new ViascoLayout();
			pRoot = vl.layout(pRoot);
		}
		PackageRepresentation.maxPacLevel = pRoot.computePackageLevel();

		TreemapLayout layout = new TreemapLayout();
		layout.layout(pRoot);
		pRoot.computeAbsolutePosition(0, 0);
		pRoot.setColor(new Color(0.9f, 0.9f, 0.9f));	
		
		
		sc.currentRoot = pRoot;
		sc.setPreliminaryMapping();
		pRoot.accept(sc.getMaplst().get("mvViasco"));
		sc.currMapping = "mvViasco";
		
		
		//print(pRoot,0);
		sc.addRenderable(pRoot);
		sc.sys = sysrep;
		
		
		JFrame jf = new JFrame();
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setPreferredSize(new Dimension(1000,1000));
		jf.setSize(new Dimension(1200,900));
		jf.add(sc.getContainner());
		jf.setVisible(true);
		jf.addWindowListener(sc.new CloseListener());
		
		
		
		
		//glPanel.add(new Label("allo les amis, ceci est du texte"));
		//glPanel.repaint();
	}
	
	public Map<String, MappingVisitor> getMaplst() {
		return maplst;
	}

	public void setMaplst(Map<String, MappingVisitor> maplst) {
		this.maplst = maplst;
	}

	
	public void setHouariMapping() {
		MappingVisitor mvHouari;
		mvHouari = new MappingVisitor("mvHouari");
		mvHouari.addClassMapping(new Mapping("Time", "Height"));
		mvHouari.addClassMapping(new Mapping("NbExecuted", "Color"));
		mvHouari.addClassMapping(new Mapping("NbReceivers", "Twist"));

		getMaplst().put(mvHouari.getName(), mvHouari);

		for (String s : getMaplst().keySet()) {
			JMenuItem curritem = new JMenuItem(s);
			curritem.addActionListener(this.mapLis);
			mapping.add(curritem);
		}
	}

	public void setPreliminaryMapping() {
		MappingVisitor mvQuality = MappingVisitor.mvQuality();
		MappingVisitor mvControl = MappingVisitor.mvControl();
		MappingVisitor mvViasco = MappingVisitor.mvViasco();
		MappingVisitor mvBug = MappingVisitor.mvBug();


		getMaplst().put(mvQuality.getName(), mvQuality);
		getMaplst().put(mvControl.getName(), mvControl);
		getMaplst().put(mvViasco.getName(), mvViasco);
		getMaplst().put(mvBug.getName(), mvBug);

		for (String s : getMaplst().keySet()) {
			JMenuItem curritem = new JMenuItem(s);
			curritem.addActionListener(this.mapLis);
			mapping.add(curritem);
		}

	}
	


	public void modifyMappings(Map<String, MappingVisitor> lst) {
		while (mapping.getItemCount() > 1) {
			mapping.remove(1);
		}
		for (String s : lst.keySet()) {
			JMenuItem currItem = new JMenuItem(s);
			currItem.addActionListener(this.mapLis);
			mapping.add(currItem);
		}

	}


	public static void print(TreemapPackageRepresentation pack, int level) {
		for (int i = 0; i < level; i++) {
			System.out.print("    ");
		}
		System.out.println(pack.getPackage().getName() + "  SizeX:" + pack.getSizeX() + "  SizeZ:" + pack.getSizeZ()
				+ "(" + pack.getElements().size() + ")" + "  X:" + pack.getPosX() + "  Z:" + pack.getPosZ());
		for (ElementRepresentation e : pack.getElements()) {
			System.out.println("+" + e.getElementModel().getName());
		}
		for (TreemapPackageRepresentation p : pack.getTreemapPackages()) {
			print(p, level + 1);
		}
	}

	public void redisplay() {
		this.dirtyList = true;
		this.sys.setDirty();
		this.glPanel.display();
	}

	public boolean isDisplayLinks() {
		return displayLinks;
	}

	public void setDisplayLinks(boolean displayLinks) {
		this.displayLinks = displayLinks;
	}
	
	public class TargetLevelFilterListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			JMenuItem item = (JMenuItem) arg0.getSource();
			int number = Integer.parseInt(item.getText());
			currentFilter = new TargetAllFilter(sys);
			currentlyFiltering = true;
			PackageRenderHierarchicalVisitor prhv = new PackageRenderHierarchicalVisitor(number);
			currentRoot.accept(prhv);
			if (isDisplayLinks()) {
				sys.updateNodesDisplayed();
				sys.updateLinksDisplayed();

				// sys.updateLinksPos();

				sys.selectedElements.clear();
				sys.selectedLinks.clear();
				currentFilter.unfilter();
				sys.unfilterAll();
			}
			if (number > TreemapPackageRepresentation.maxPacLevel)
				TreemapPackageRepresentation.nowShowingPackageLevel = TreemapPackageRepresentation.maxPacLevel;
			else
				TreemapPackageRepresentation.nowShowingPackageLevel = number;

			dirtyList = true;
			sys.setDirty();
			glPanel.display();
		}

	}

	public abstract class FilterMouseListener implements MouseListener {
		@Override
		public void mouseClicked(MouseEvent e) {}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}
		@Override
		public void mousePressed(MouseEvent e) {}
		
	}
	
	public class TargetClassFilterListener extends FilterMouseListener {
		@Override
		public void mouseReleased(MouseEvent e) {
			System.out.println("Listen class");
			currentFilter = new TargetClassFilter(sys);
			currentlyFiltering = true;
		}
	}

	public class TargetPackageFilterListener extends FilterMouseListener {
		@Override
		public void mouseReleased(MouseEvent e) {
			System.out.println("Listen package");
			currentFilter = new TargetPackageFilter(sys);
			currentlyFiltering = true;
		}
	}

	public class TargetMethodFilterListener extends FilterMouseListener {
		@Override
		public void mouseReleased(MouseEvent e) {
			System.out.println("Listen method");
			currentFilter = new TargetMethodFilter(sys);
			currentlyFiltering = true;
		}
	}

	public class UnFilterListener extends FilterMouseListener {
		@Override
		public void mouseReleased(MouseEvent e) {
			System.out.println("Listen unfilter");
			currentFilter.unfilter();
			if(currentlyFiltering)
				currentFilter = new EntityFilter(sys);
			currentlyFiltering = false;
			dirtyList = true;
			sys.setDirty();
			glPanel.display();
		}
	}

	public class MappingListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			JMenuItem currItem = (JMenuItem) arg0.getSource();
			String mapp = currItem.getText();
			currentRoot.accept(getMaplst().get(mapp));
			currMapping = mapp;
			dirtyList = true;
			sys.setDirty();
			glPanel.display();
		}

	}

	public class ModifyMappingListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			Iterator<Package> packageIterator = sys.getSystemDef().getPackages().iterator();
			Package p = packageIterator.hasNext() ? packageIterator.next() : null;
			Iterator<Element> elementIterator = sys.getSystemDef().getAllElements().iterator();
			Element e = elementIterator.hasNext() ? elementIterator.next() : null;
			Iterator<Method> methodIterator = sys.getSystemDef().getMethods().iterator();
			Method m = methodIterator.hasNext() ? methodIterator.next() : null;
			
			if (p != null && e != null && m != null)
				new MappingModifier(p, e, m, getMaplst(), SceneLandscape.this, mapLis);
			else {
				System.err.print("Impossible de modifier le mapping � ModifyMapping ");
				if(p == null)
					System.err.println("car pas de packages disponibles");
				if(e == null)
					System.err.println("car pas d'�l�ments disponibles");
				if(m == null)
					System.err.println("car pas de m�thodes disponibles");
			}
		}

	}

	public class ShowNamesListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			if (ElementRepresentation.showNames == false) {
				ElementRepresentation.showNames = true;
			} else {
				ElementRepresentation.showNames = false;
			}
			dirtyList = true;
			sys.setDirty();
			glPanel.display();
		}
	}
	
	public class IRFilterListener implements ActionListener {
		
		private IRFilter irFilter = new IRFilter(sys);
		
		public void actionPerformed(ActionEvent arg0) {
			if(arg0.getSource() instanceof JCheckBoxMenuItem) {//cas pour le filter sur un concept
				JCheckBoxMenuItem item = (JCheckBoxMenuItem) arg0.getSource();
				Object concept = item.getSelectedObjects();
				if (concept != null)
					irFilter.addConcept(item.getText());
				else
					irFilter.removeConcept(item.getText());
			}else if(arg0.getSource() instanceof JMenuItem) {//cas pour all concepts ou any concept
				JMenuItem item = (JMenuItem) arg0.getSource();
				if(item.getText().equals("All concepts")) {
					item.setText("Any concepts");
					JPopupMenu menu = (JPopupMenu) item.getParent();
					for(MenuElement el : menu.getSubElements()) {
						if(el instanceof JCheckBoxMenuItem) {
							irFilter.addConcept(((JCheckBoxMenuItem) el).getText());
							((JCheckBoxMenuItem)el).setSelected(true);
						}
					}
				}else if(item.getText().equals("Any concepts")) {
					item.setText("All concepts");
					irFilter.removeAllConcepts();
					JPopupMenu menu = (JPopupMenu) item.getParent();
					for(MenuElement el : menu.getSubElements()) {
						if(el instanceof JCheckBoxMenuItem) {
							((JCheckBoxMenuItem)el).setSelected(false);
						}
					}
				}else {
					System.err.println("Unexpected button clicked");
				}
			}
			currentFilter = irFilter;
			currentlyFiltering = true;
		}
	}

	public class CloseListener implements WindowListener {
		public void windowClosed(WindowEvent e) {
			isRunning = false;
		}

		public void windowActivated(WindowEvent e) {		}
		public void windowClosing(WindowEvent e) {		}
		public void windowDeactivated(WindowEvent e) {		}
		public void windowDeiconified(WindowEvent e) {		}
		public void windowIconified(WindowEvent e) {		}
		public void windowOpened(WindowEvent e) {		}

	}
	
	
	public static void createRandomPackage(PackageRepresentation parent, int level) {
		// if (level == 2)
		// return;
		TreemapPackageRepresentation pack;
		ClassRepresentation classrep;
		MethodRepresentation methodRep;
		int i = (int) (Math.random() * level);
		for (int j = 0; j <= i; j++) {
			if (level == 0)
				break;
			pack = new TreemapPackageRepresentation(new Package(parent.getPackage().getName() + j));
			pack.setColor(new Color(0.7f, 0.7f, 0.7f));
			parent.addPackage(pack);
			createRandomPackage(pack, level - 1);
		}
		// classes
		int k = (int) (Math.random() * 30);
		int m = 0;
		for (int l = 0; l <= k; l++) {
			float colorRand = (float) Math.random();
			classrep = new ClassRepresentation(new ClassDef(parent.getPackage().getName() + "class" + l));
			
			classrep.setColor(new Color(colorRand, 1.0f, 1 - colorRand));
			classrep.setHeight((float) Math.random() * 2.0f + 0.75f);
			classrep.setTwist(-90 * Math.random());
			parent.addElement(classrep);
			++indice;
			m = (int) (Math.random() * 30);
			for (int n = 0; n <= m; n++) {
				colorRand = (float) Math.random();
				methodRep = new MethodRepresentation(new Method(classrep.getElementModel().getName() + "method" + n));
				methodRep.setColor(new Color(colorRand, 0, 1 - colorRand));
				methodRep.setHeight((float) Math.random() * 2.0f + 0.25f);
				methodRep.setTwist(-90 * Math.random());
				// System.out.println(indice++);
				classrep.addMethod(methodRep);
			}

		}
	}
}