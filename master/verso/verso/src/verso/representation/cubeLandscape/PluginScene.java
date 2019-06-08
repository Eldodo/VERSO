package verso.representation.cubeLandscape;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.actions.OpenAction;
import org.eclipse.jdt.ui.actions.OpenNewClassWizardAction;
import org.eclipse.jdt.ui.actions.OpenNewInterfaceWizardAction;
import org.eclipse.jdt.ui.actions.OpenNewPackageWizardAction;
import org.eclipse.jdt.ui.actions.ShowInPackageViewAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.OpenFileAction;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import verso.util.TypeFinderUtil;
import verso.builder.SVNMessageLogger;
import verso.builder.VersoModificationListener;
import verso.model.Element;
import verso.model.Line;
import verso.model.Method;
import verso.model.Package;
import verso.model.SystemDef;
import verso.model.SystemManager;
import verso.parser.Activator;
import verso.representation.IPickable;
import verso.representation.Lines.representationModel.ClassLineRepresentation;
import verso.representation.Lines.representationModel.LineRepresentation;
import verso.representation.cubeLandscape.Layout.TreemapLayout;
import verso.representation.cubeLandscape.modelVisitor.CubeLandScapeVisitor;
import verso.representation.cubeLandscape.representationModel.MethodRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;
import verso.representation.cubeLandscape.representationModel.SystemRepresentation;
import verso.representation.cubeLandscape.representationModel.TreemapPackageRepresentation;
import verso.saving.csv.CsvParser2;
import verso.saving.csv.CsvWriter;
import verso.view.AddMethodWizard;
import verso.view.VersoInput;
import verso.view.Bug.AddBugWizard;
import verso.view.Bug.ModifyBugWizard;
import verso.visitor.AppelMethodeVisitor;
import verso.visitor.CompilationUnitExtractor;
import verso.visitor.CynthiaVisitor;
import verso.visitor.DepthVisitor;
import verso.visitor.LineCreatorVisitor;
import verso.visitor.LineTypeVisitor;

@SuppressWarnings("deprecation")
public class PluginScene extends SceneLandscape implements VersoModificationListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public static PluginScene sce = null;
	private Shell shell = null;
	private IPickable lastRightClickItem = null;
	private JMenuItem adding = null;
	private JMenuItem addInter = null;
	private JMenuItem addPackage = null;
	private JMenuItem addMethod = null;
	private JMenuItem showInEditor = null;
	private JMenuItem showInCoolEditor = null;
	private JMenuItem showInLineEditor = null;
	private JMenuItem addBug = null;
	private JMenuItem modifyBug = null;
	private JMenuItem markAsVersion = null;
	private JMenuItem gotoVersion = null;
	private JMenuItem refreshSVNInfo = null;
	private JMenuItem packageOpenClose = null;
	private SystemDef mainSystem;
	static private String nextProject = "Test";
	Thread displayThread = null;

	public PluginScene(SystemRepresentation sysrep)
	{	
		super(sysrep);
		mainSystem = sysrep.getSystemDef();
		jpopMenu.setLightWeightPopupEnabled(false);
		adding = new JMenuItem("Add Class");
		addInter = new JMenuItem("Add Interface");
		addPackage = new JMenuItem("Add Package");
		addMethod = new JMenuItem("Add Method");
		showInEditor = new JMenuItem("Show Window In Editor");
		showInCoolEditor = new JMenuItem("Show in Text Editor");
		showInLineEditor = new JMenuItem("Show in Line Editor");
		addBug = new JMenuItem("add a new Bug");
		modifyBug = new JMenuItem("modify a Bug");
		markAsVersion = new JMenuItem("Mark As Version");
		gotoVersion = new JMenu("Go to Version");
		refreshSVNInfo = new JMenuItem("Refresh SVN Info");
		packageOpenClose = new JMenuItem("PackageSelectionMode");
		//gotoVersion.addActionListener(new GotoVersionAdderListener());
		
		
		AddClassListener adl = new AddClassListener();
		adding.addActionListener(adl);
		addInter.addActionListener(adl);
		addPackage.addActionListener(adl);
		addMethod.addActionListener(adl);
		showInEditor.addActionListener(new ShowInEditorListener());
		showInCoolEditor.addActionListener(new ShowInCoolEditorListener());
		showInLineEditor.addActionListener(new ShowInLineEditorListener());
		addBug.addActionListener(new AddBugListener());
		modifyBug.addActionListener(new ModifyBugListener());
		markAsVersion.addActionListener(new MarkAsVersionListener());
		refreshSVNInfo.addActionListener(new RefreshSVNInfoListener());
		packageOpenClose.addActionListener(new PackageOpenCloseMenuListener());
		
		jpopMenu.add(packageOpenClose);
		jpopMenu.addSeparator();
		jpopMenu.add(adding);
		jpopMenu.add(addInter);
		jpopMenu.add(addPackage);
		jpopMenu.add(addMethod);
		jpopMenu.addSeparator();
		jpopMenu.add(addBug);
		jpopMenu.add(modifyBug);
		jpopMenu.addSeparator();
		jpopMenu.add(showInEditor);
		jpopMenu.add(showInCoolEditor);
		jpopMenu.add(showInLineEditor);
		jpopMenu.addSeparator();
		jpopMenu.add(gotoVersion);
		jpopMenu.add(markAsVersion);
		jpopMenu.addSeparator();
		jpopMenu.add(refreshSVNInfo);
		this.setVersionMenu();
		displayThread = Thread.currentThread();
		
	}
	
	/**
	 * NOT WORKING as it is.
	 * @param args
	 */
	@Deprecated
	public static void main(String[] args)
	{
		SystemRepresentation sysrep =null;
	
		SceneLandscape.isRunning = true;
		
		Activator activator = Activator.getDefault();
		
		SystemDef sys = activator.getSystem(nextProject);
		if (sys == null)
			sys = new SystemDef("TestFail");
		sysrep = (SystemRepresentation)sys.accept(new CubeLandScapeVisitor());
		PluginScene sc = new PluginScene(sysrep);
		Activator.getDefault().addModificationListnener(sc);
		//sc.addRenderable(new Cartesian());
		TreemapPackageRepresentation pRoot = new TreemapPackageRepresentation(new Package("root"));
		for (PackageRepresentation pr : sysrep.getPackages())
		{
			pRoot.addPackage(pr);
		}
		TreemapLayout layout = new TreemapLayout();
		layout.layout(pRoot);
		TreemapPackageRepresentation.maxPacLevel = pRoot.computePackageLevel();
		pRoot.setColor(new Color(0.7f, 0.7f, 0.7f));
		pRoot.computeAbsolutePosition(0, 0);
		
		sc.currentRoot = pRoot;
		sc.setPreliminaryMapping();
		/*
		sc.mvQuality = new MappingVisitor();
		sc.mvQuality.addPackageMapping(new Mapping("PackageCoupling", "Color"));
		sc.mvQuality.addPackageMapping(new Mapping("PackageComplexity", "Height"));
		sc.mvQuality.addClassMapping(new Mapping("CBO", "Color"));
		sc.mvQuality.addClassMapping(new Mapping("WMC", "Height"));
		sc.mvQuality.addClassMapping(new Mapping("LCOM5", "Twist"));
		sc.mvQuality.addMethodMapping(new Mapping("CouplingMethod", "Color"));
		sc.mvQuality.addMethodMapping(new Mapping("MCMethod", "Height"));
		sc.mvQuality.addMethodMapping(new Mapping("CohMethod", "Twist"));
		
		
		sc.mvControl = new MappingVisitor();
		sc.mvControl.addPackageMapping(new Mapping("MainAuthor", "Color"));
		sc.mvControl.addPackageMapping(new Mapping("NumberOfCommit", "Height"));
		sc.mvControl.addClassMapping(new Mapping("MainAuthor", "Color"));
		sc.mvControl.addClassMapping(new Mapping("NumberOfCommit", "Height"));
		sc.mvControl.addClassMapping(new Mapping("NumberOfAuthor", "Twist"));
		*/
		
		pRoot.accept(sc.getMaplst().get("mvQuality"));
		sc.currMapping = "mvQuality";
		
		
		//print(pRoot,0);
		sc.addRenderable(pRoot);
		sc.sys = sysrep;
		
		sce = sc;
		sce.mainSystem = sys;
		
	}
	public static PluginScene otherMain(String project)
	{
		nextProject = project;
		PluginScene.main(new String[0]);
		return PluginScene.sce;
	}
	
	
	public void listen(SystemDef sys) {
		mainSystem = sys;
		
		System.out.println("listening");
		if (sys == null)
			return;
		this.clearRenderable();
		SystemRepresentation sysrep = (SystemRepresentation)sys.accept(new CubeLandScapeVisitor());
		
		//this.addRenderable(new Cartesian());
		TreemapPackageRepresentation pRoot = new TreemapPackageRepresentation(new Package("root"));
		for (PackageRepresentation pr : sysrep.getPackages())
		{
			pRoot.addPackage(pr);
		}
		TreemapLayout layout = new TreemapLayout();
		layout.layout(pRoot);
		pRoot.computeAbsolutePosition(0, 0);
		pRoot.setColor(new Color(0.7f, 0.7f, 0.7f));
		pRoot.accept(this.getMaplst().get(this.currMapping));
		
		//print(pRoot,0);
		this.currentRoot = pRoot;
		this.addRenderable(pRoot);
		this.sys = sysrep;
		this.dirtyList = true;
		this.glPanel.display();
		setVersionMenu();
			
		
	}
	public void setVersionMenu()
	{
		GotoVersionListener vl = new GotoVersionListener();
		gotoVersion.removeAll();
		for (long l = 0; l < this.mainSystem.getVersion(); l++)
		{
			JMenuItem jmi = new JMenuItem("" + (l + 1));
			gotoVersion.add(jmi);
			jmi.addActionListener(vl);
		}
	}
	public void setShell(Shell s)
	{
		shell = s;
	}
	
	public void mouseClicked(MouseEvent me)
	{
		if (me.getButton() == MouseEvent.BUTTON3)
		{
			this.lastRightClickItem = this.pick(me.getX(), me.getY());
		}
//		IViewDescriptor[] views = PlatformUI.getWorkbench().getViewRegistry().getViews();
		/*
		 * System.out.println("Start of the registry"); for (IViewDescriptor v : views)
		 * { System.out.println(v.getId()); }
		 */
		if (me.getButton() == MouseEvent.BUTTON1)
		{
			IPickable pic = this.pick(me.getX(), me.getY());
			ClickRunner cr = null;
			if (pic != null)
			{
				cr = new ClickRunner(pic);
				if (me.getClickCount() == 2)
				{
					cr.setOpenEdit(true);
				}
			}
			shell.getDisplay().asyncExec(cr);
		}
		super.mouseClicked(me);
	}
	
	public class ClickRunner implements Runnable {
		private String elemtxt = "";
		private IPickable element = null;
		private boolean openEdit = false;

		public void setOpenEdit(boolean edit) {
			this.openEdit = edit;
		}

		public ClickRunner(IPickable s) {
			element = s;
			elemtxt = element.getSimpleName();
		}

//		private String[] getParams(String paramtxt) {
//			String[] toReturn = null;
//			List<String> lst = new ArrayList<String>();
//			if (paramtxt.trim().length() > 0) {
//				String[] splitted = paramtxt.split(",");
//				for (String par : splitted) {
//					if (par.contains(".")) {
//						par = par.substring(par.lastIndexOf(".") + 1);
//						par = par.trim();
//					}
//					lst.add(par);
//				}
//			}
//			toReturn = new String[lst.size()];
//			for (int i = 0; i < lst.size(); i++) {
//				toReturn[i] = lst.get(i);
//			}
//			return toReturn;
//		}
		
		
		public void run() {
			IWorkbenchPage p = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			try {
				p.showView("org.eclipse.jdt.ui.PackageExplorer");
			} catch (Exception e) {
				System.out.println(e);
			}
			IWorkbenchPartSite s = p.getActivePart().getSite();
			ShowInPackageViewAction showaction = new ShowInPackageViewAction(s);
			// if the element is a method
			IJavaElement elem = null;
			if (elemtxt.contains("(")) {
				String className = elemtxt.substring(0, elemtxt.indexOf("("));
				className = className.substring(0, className.lastIndexOf("."));
				IType t = TypeFinderUtil.findTypeFromString(className);
				String methodSig = ((Method) ((MethodRepresentation) element).getElement()).getSignature();
				methodSig = methodSig.replaceAll("[']", ";");
				String methodName = elemtxt.substring(0, elemtxt.indexOf("("));
				methodName = methodName.substring(methodName.lastIndexOf(".") + 1);
				try {
					t.getOpenable().open(null);
					elem = t.getMethod(methodName, Signature.getParameterTypes(methodSig));
				} catch (Exception e) {
					System.out.println(e);
				}
				// System.out.println(methodSig);
			}
			if (elem == null) {
				TypeFinderUtil.setProjectName(mainSystem.getName());
				elem = TypeFinderUtil.findJavaElementFromString(elemtxt);
			}
			if (elem == null) {
				elem = TypeFinderUtil.findTypeFromString(elemtxt);
				if (elem != null)
					elem = elem.getParent();
			}
			showaction.run(elem);
			if (this.openEdit) {
				OpenAction oa = new OpenAction(s);
				oa.run();
			}
		}

	}
	
	public class ShowInCoolEditorListener implements ActionListener, Runnable {

		public void run() {
			try {
				IWorkbenchPage p = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IViewPart vp = p.findView("org.eclipse.jdt.ui.PackageExplorer");
				ISelection s = vp.getSite().getSelectionProvider().getSelection();
				IResource r = ((ICompilationUnit) ((IStructuredSelection) s).getFirstElement()).getResource();
				if (r != null) {
					if (r.getType() == IResource.FILE) {
						IDE.openEditor(p, new FileEditorInput((IFile) r), "verso.versoTextEditor", true);

					} else
						System.out.println("pas de fichier vraiment sélectionné");
				}
			} catch (Exception e) {
				System.out.println(e);
			}

		}

		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			shell.getDisplay().syncExec(this);
		}

	}

	public class ShowInEditorListener implements ActionListener, Runnable {
		OpenFileAction ofa = null;
		IWorkbenchPage p = null;

		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Vive le nouveau truc vraiment cool d'éditeur j'espère que ça marche ... "
					+ PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor("allo.java"));
			// System.out.println("Start of listing Editors");

			// IWorkbenchPage p =
			// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IWorkbench wb = PlatformUI.getWorkbench();
			IWorkbenchWindow[] iwbws = wb.getWorkbenchWindows();
			p = null;

			for (int i = 0; i < iwbws.length; i++) {
				System.out.println(iwbws[i]);
				p = iwbws[i].getActivePage();
			}

			shell.getDisplay().syncExec(this);
		}

		public void run() {
			try {
				IDE.openEditor(p, new VersoInput(), "verso.versoEditor", true);

			} catch (Exception e) {
				System.out.println(e);
			}
		}

	}
	
	public class ShowInLineEditorListener implements ActionListener, Runnable {
		public void actionPerformed(ActionEvent e) {
			shell.getDisplay().syncExec(this);
		}

		public void run() {
			ClassLineRepresentation clr = null;
			try {
				IWorkbenchPage p = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IViewPart vp = p.findView("org.eclipse.jdt.ui.PackageExplorer");
				ISelection s = vp.getSite().getSelectionProvider().getSelection();
				ICompilationUnit r = ((ICompilationUnit) ((IStructuredSelection) s).getFirstElement());
				String className = "";
				if (r != null) {
					className = r.getAllTypes()[0].getFullyQualifiedName();
					if (className.indexOf(".") == -1)
						className = "default." + className;
					Element elem = SystemManager.getSystem(mainSystem.getName()).getElement(className);
					// parsing for the file

					ASTParser parser = ASTParser.newParser(AST.JLS3);
					parser.setResolveBindings(true);
					parser.setSource(r);
					ASTNode node = parser.createAST(null);
					CompilationUnitExtractor cue = new CompilationUnitExtractor();
					node.accept(cue);

					CompilationUnit comp = cue.getCompilationUnit();
					LineCreatorVisitor lcv = new LineCreatorVisitor();
					comp.accept(lcv);
					elem.setLines(lcv.getLines());

					DepthVisitor dv = new DepthVisitor(elem);
					comp.accept(dv);
					AppelMethodeVisitor amv = new AppelMethodeVisitor(elem);
					comp.accept(amv);
					CynthiaVisitor cv1 = new CynthiaVisitor(comp);
					cv1.setCurrentClass(elem);
					comp.accept(cv1);
					LineTypeVisitor ltv = new LineTypeVisitor(elem);
					comp.accept(ltv);

					File elemLocation = TypeFinderUtil.findTypeFromString(elem.getName()).getResource().getLocation()
							.toFile();
					SVNMessageLogger.logMessages(elem, elemLocation, sys.getSystemDef());

					clr = new ClassLineRepresentation(elem);
					for (Line l : elem.getLines()) {
						clr.addLine(new LineRepresentation(l));
					}
					System.out.println("J'ouvre le Frame avec les lignes");
					// Fenetre f = new Fenetre();
					// f.setClass(clr);
					VersoInput vi = new VersoInput();
					vi.setClassLineRepresentation(clr);
					try {
						IDE.openEditor(p, vi, "verso.versoLineEditor", true);
					} catch (Exception e) {
						System.out.println(e);
					}
				}
			} catch (Exception ex) {
				System.out.println(ex);
			}

		}
	}

	public class GotoVersionListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			String vNumber = ((JMenuItem) arg0.getSource()).getText();
			long version = Long.parseLong(vNumber);
			IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
			IProject pro = workspace.getProject(mainSystem.getName());
			SystemDef sys = null;
			try {
				sys = CsvParser2.parseFile(pro.getLocation().append(new Path("ver" + version + ".met")).toFile());
			} catch (Exception e) {
				e.printStackTrace();
			}
			// listen(sys);
			SceneLandscapeWithVersionNavigation.start(mainSystem, sys, version, currMapping);
		}
	}

	public class MarkAsVersionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(mainSystem.getName());
			IPath pat = p.getLocation().append(new Path("ver" + (mainSystem.getVersion() + 1) + ".met"));
			mainSystem.setVersion(mainSystem.getVersion() + 1);
			CsvWriter cw = new CsvWriter(mainSystem, pat.toString());
			cw.print();
			setVersionMenu();
		}
	}

	public class RefreshSVNInfoListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			SVNMessageLogger.updateCurrentVersion(sys.getSystemDef(),
					ResourcesPlugin.getWorkspace().getRoot().getProject(sys.getSystemDef().getName()));

		}
	}
	
	public class PackageOpenCloseMenuListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			PIsDown = !PIsDown;
		}
	}
	
	public class GotoVersionAdderListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.out.println("GotoVersionAdderListener not implemented in this version of Verso");
		}

	}

	public class AddBugListener implements ActionListener, Runnable {
		public void actionPerformed(ActionEvent arg0) {
			shell.getDisplay().syncExec(this);
		}

		public void run() {
			AddBugWizard abw = new AddBugWizard(shell, mainSystem, displayThread);
			WizardDialog w = new WizardDialog(shell, abw);
			abw.setWindowTitle("Adding a bug");
			w.open();
		}

	}
	
	public class ModifyBugListener implements ActionListener, Runnable {
		public void actionPerformed(ActionEvent arg0) {
			shell.getDisplay().syncExec(this);
		}

		public void run() {
			ModifyBugWizard abw = new ModifyBugWizard(shell, mainSystem);
			WizardDialog w = new WizardDialog(shell, abw);
			abw.setWindowTitle("Modify a bug");
			w.open();
		}
	}
	
	public class AddClassListener implements ActionListener, Runnable {
		private static final int CLASS = 1;
		private static final int INTERFACE = 2;
		private static final int PACKAGE = 3;
		private static final int METHOD = 4;
		IResource selectedRes = null;
		int currentlyAddindType = 0;

		public void actionPerformed(ActionEvent arg0) {
			if (lastRightClickItem != null) {
				selectedRes = TypeFinderUtil.findIResourcefromString(lastRightClickItem.getSimpleName());
			}
			System.out.println("AddClassListener.adding ...");
			if (arg0.getSource().equals(adding)) {
				this.currentlyAddindType = CLASS;
			}
			if (arg0.getSource().equals(addInter)) {
				this.currentlyAddindType = INTERFACE;
			}
			if (arg0.getSource().equals(addPackage)) {
				this.currentlyAddindType = PACKAGE;
			}
			if (arg0.getSource().equals(addMethod)) {
				this.currentlyAddindType = METHOD;
			}
			shell.getDisplay().syncExec(this);

		}

		
		public void run() {
			if (this.currentlyAddindType == CLASS) {
				OpenNewClassWizardAction oncwa = new OpenNewClassWizardAction();
				if (selectedRes != null)
					oncwa.setSelection(new StructuredSelection(selectedRes));
				oncwa.run();
			}
			if (this.currentlyAddindType == INTERFACE) {
				OpenNewInterfaceWizardAction oniwa = new OpenNewInterfaceWizardAction();
				if (selectedRes != null)
					oniwa.setSelection(new StructuredSelection(selectedRes));
				oniwa.run();
			}
			if (this.currentlyAddindType == PACKAGE) {
				OpenNewPackageWizardAction onpwa = new OpenNewPackageWizardAction();
				if (selectedRes != null)
					onpwa.setSelection(new StructuredSelection(selectedRes));
				onpwa.run();
			}

			if (this.currentlyAddindType == METHOD) {
				String elemName = "";
				AddMethodWizard ew = new AddMethodWizard(shell);
				WizardDialog w = new WizardDialog(shell, ew);
				ew.setWindowTitle("Adding a method");
				if (lastRightClickItem != null) {
					if (lastRightClickItem.getName().startsWith("Package : ")) {
						elemName = lastRightClickItem.getName().substring("Package : ".length());
						ew.setElement(elemName);
					}
					if (lastRightClickItem.getName().startsWith("Class :")
							|| lastRightClickItem.getName().startsWith("Interface :")) {
						String name = lastRightClickItem.getName();
						name = name.substring(name.indexOf(":") + 2);
						ew.setElement(name);
					}
				}

				w.open();

			}

		}
	}
}
