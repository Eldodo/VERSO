package verso.representation.cubeLandscape;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

import verso.model.Package;
import verso.model.SystemDef;
import verso.representation.cubeLandscape.Layout.TreemapLayout;
import verso.representation.cubeLandscape.modelVisitor.CubeLandScapeVisitor;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;
import verso.representation.cubeLandscape.representationModel.SystemRepresentation;
import verso.representation.cubeLandscape.representationModel.TreemapPackageRepresentation;
import verso.saving.csv.CsvParser2;

public class SceneLandscapeWithVersionNavigation extends SceneLandscape {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	long currentVersion  =0 ;
	SystemDef mainSys = null;
	SceneLandscapeWithVersionNavigation sc = null;
	JLabel versionLabel = new JLabel();
	
	public SceneLandscapeWithVersionNavigation(SystemRepresentation sysRep, SystemDef mainSys, long version,
			String currMapping) {
		super(sysRep);
		this.currentVersion = version;
		this.mainSys = mainSys;
		this.sc = this;
		this.currMapping = currMapping;
		jpopMenu.addSeparator();
		JMenu gotoVersion = new JMenu("Go to Version");
		jpopMenu.add(gotoVersion);
		gotoVersion.add(new JMenuItem("next")).addActionListener(new PrevNextListener());
		gotoVersion.add(new JMenuItem("previous")).addActionListener(new PrevNextListener());
		GotoVersionListener vl = new GotoVersionListener();
		// gotoVersion.removeAll();
		for (long l = 0; l < this.mainSys.getVersion(); l++) {
			JMenuItem jmi = new JMenuItem("" + (l + 1));
			gotoVersion.add(jmi);
			jmi.addActionListener(vl);
		}
		versionLabel.setText(this.getVersionLabelText());
	}
	
	private String getVersionLabelText() {
		return "Current Version : " + this.currentVersion;
	}
	
	public void setNextVersion(long version, String mapping) {
		IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		IProject pro = workspace.getProject(mainSys.getName());
		SystemDef system;
		try {
			system = CsvParser2.parseFile(pro.getLocation().append(new Path("ver" + version + ".met")).toFile());
			sys = (SystemRepresentation) system.accept(new CubeLandScapeVisitor());
			TreemapPackageRepresentation pRoot = new TreemapPackageRepresentation(new Package("root"));
			for (PackageRepresentation pr : sys.getPackages()) {
				pRoot.addPackage(pr);
			}
			TreemapLayout layout = new TreemapLayout();
			layout.layout(pRoot);
			pRoot.computeAbsolutePosition(0, 0);
			pRoot.setColor(new Color(0.9f, 0.9f, 0.9f));

			sc.currentRoot = pRoot;
			// sc.setPreliminaryMapping();
			pRoot.accept(sc.getMaplst().get(mapping));

			// print(pRoot,0);
			sc.clearRenderable();
			sc.addRenderable(pRoot);
			sc.dirtyList = true;
			sc.glPanel.display();
		} catch (Exception e) {
			System.out.println("Next version not loaded.");
			e.printStackTrace();
		}

	}

	public static void start(SystemDef sys, SystemDef verSys, long version, String mapping) {
		SystemRepresentation sysrep = (SystemRepresentation) verSys.accept(new CubeLandScapeVisitor());
		SceneLandscapeWithVersionNavigation sc = new SceneLandscapeWithVersionNavigation(sysrep, sys, version, mapping);
		// sc.addRenderable(new Cartesian());
		TreemapPackageRepresentation pRoot = new TreemapPackageRepresentation(new Package("root"));
		for (PackageRepresentation pr : sysrep.getPackages()) {
			pRoot.addPackage(pr);
		}
		/*
		 * { ViascoLayout2 vl = new ViascoLayout2(); //ViascoLayout vl = new
		 * ViascoLayout(); pRoot = vl.layout(pRoot); }
		 */
		PackageRepresentation.maxPacLevel = pRoot.computePackageLevel();

		TreemapLayout layout = new TreemapLayout();
		layout.layout(pRoot);
		pRoot.computeAbsolutePosition(0, 0);
		pRoot.setColor(new Color(0.9f, 0.9f, 0.9f));

		sc.currentRoot = pRoot;
		sc.setPreliminaryMapping();
		pRoot.accept(sc.getMaplst().get(mapping));

		// print(pRoot,0);
		sc.addRenderable(pRoot);
		sc.sys = sysrep;

		JFrame jf = new JFrame();
		jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		jf.setPreferredSize(new Dimension(1000, 1000));
		jf.setSize(new Dimension(800, 600));
		jf.add(sc.getContainner());

		sc.getContainer().add(sc.versionLabel, BorderLayout.SOUTH);

		jf.setVisible(true);
		jf.addWindowListener(sc.new CloseListener());

		// glPanel.add(new Label("allo les amis, ceci est du texte"));
		// glPanel.repaint();

	}
	
	private class PrevNextListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			long version = 0;
			if (((JMenuItem) arg0.getSource()).getText().compareTo("next") == 0) {
				version = ((currentVersion + 1) % mainSys.getVersion());
			} else {
				version = ((currentVersion - 1) % mainSys.getVersion());
			}
			if (version == 0)
				version = mainSys.getVersion();
			currentVersion = version;
			versionLabel.setText(getVersionLabelText());
			setNextVersion(version, currMapping);
		}
	}

	private class GotoVersionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// should we pre-compute them all or not?
			String vNumber = ((JMenuItem) e.getSource()).getText();
			long version = Long.parseLong(vNumber);
			currentVersion = version;
			versionLabel.setText(getVersionLabelText());
			setNextVersion(version, currMapping);
		}
	}
}
