package verso.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNLogMessageChangePath;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;

import verso.model.Element;
import verso.model.Entity;
import verso.model.SystemDef;
import verso.model.SystemManager;
import verso.model.history.Commit;
import verso.parser.Builder;
import verso.parser.ParserAppli;
import verso.saving.VersoProjectLoader;

public class IncrementalBuilder extends IncrementalProjectBuilder {

	public static int FAKE_BUILD = 50;
	public static String BUILDER_ID = "SimpleVersoParser.versoBuilder";
	private static List<VersoModificationListener> listenerList = new LinkedList<VersoModificationListener>();

	protected void startupOnInitialize() {
		System.out.println("Initializing Builder");
		VersoProjectLoader.load(getProject());
		if (SystemManager.getSystem(getProject().getName()) == null) {
			try {
				this.build(FULL_BUILD, null, null);
			} catch (Exception e) {
				System.out.println(e);
			}
		} else {
			try {
				getProject().open(null);
				SystemDef sys = SystemManager.getSystem(getProject().getName());
				sys.computeBugMetrics(this.getProject());
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		if (kind == FAKE_BUILD)
			return null;
		switch (kind) {
		case IncrementalProjectBuilder.CLEAN_BUILD:
			break;
		case IncrementalProjectBuilder.FULL_BUILD: {
			ParserAppli pa = new ParserAppli();
			pa.buildModel(getProject().getName());
			SystemDef sys = SystemManager.getSystem(getProject().getName());
			sys.computePackageMetricsQual();
			sys.setRevision0();
			SVNMessageLogger.updateCurrentVersion(sys, this.getProject());
			//SVNMessageLogger.updateAnnotationsVersion(sys, this.getProject());
			
			//sys.computeSVNMetrics();
			//this.logLines();
			sys.computeBugMetrics(this.getProject());
			// readHistory();
			//checkMylyn();
			//System.out.println("L'historique devrait être fini");
			break;
		}
		case IncrementalProjectBuilder.INCREMENTAL_BUILD:
		case IncrementalProjectBuilder.AUTO_BUILD: {
			// Quality metrics
			IResourceDelta ird = this.getDelta(getProject());
			
			ird.accept(new DeltaVisitor());
			// Should be better, but we'll recompute everything for now
			SystemDef sys = SystemManager.getSystem(getProject().getName());
			//SVNMessageLogger.updateCurrentVersion(sys, this.getProject());
			sys.computePackageMetricsQualPartial();
			// Fin de ce qu'on devrait améliorer
			//this.logLines(); // devrait se faire tout seul en prinicpe
			//checkMylyn();
			//this.updateCurrentVersion(); // c'est ça qui devrait se faire tout seul
			//sys.computeSVNMetrics();// aussi à améliorer pour ne pas tous
											// les recalculer.
			sys.computeBugMetrics(this.getProject());
			break;
		}
		}
		for (VersoModificationListener lis : listenerList) {
			lis.listen(SystemManager.getSystem(getProject().getName()));
		}
		return null;
	}

	private void readHistory(/* long startRev, long endRev */) {
		List<Long> lst = new ArrayList<Long>();
		ISVNClientAdapter svnc = null;
		File f2 = null;
		File f3 = null;
		File f4 = null;
		File f = null;
		SVNUrl projectURL = null;
		try {
			svnc = JhlClientAdapterFactory
					.createSVNClient(JhlClientAdapterFactory
							.getPreferredSVNClientType());
			f = this.getProject().getLocation().toFile();
			projectURL = svnc.getInfo(f).getRepository();
			f2 = new File(f.getAbsolutePath() + "Bis");
			// svnc.checkout(projectURL, f2, SVNRevision.getRevision("1"),
			// true);
		} catch (Exception e) {
			System.out.println(e);
		}
		// First version should be loaded here ...
		// Get all Revisions
		IProject p = null;
		try {

			f4 = new File(f2.getAbsolutePath() + File.separatorChar
					+ f.getName());
			f3 = new File(f.getAbsolutePath() + "Bis2");
		} catch (Exception e) {
			System.out.println(e);
		}

		try {
			ISVNLogMessage[] messages = svnc.getLogMessages(getProject()
					.getLocation().toFile(), SVNRevision.getRevision("0"),
					SVNRevision.HEAD);
			for (int i = 0; i < messages.length; i++) {
				if (messages[i].getChangedPaths() != null) {
					ISVNLogMessageChangePath[] paths = messages[i]
							.getChangedPaths();
					for (ISVNLogMessageChangePath cp : paths) {
						if (cp.getPath().endsWith(".java")) {
							lst.add(messages[i].getRevision().getNumber());
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		// Starting to compute metrics for each version
		try {
			for (Long l : lst) {
				if (p != null)
					p.delete(true, null);
				p = null;
				p = (IProject) ResourcesPlugin.getWorkspace().getRoot()
						.getProject(f3.getName());
				System.out.println("Je travaille sur la Révision : " + l);
				svnc.checkout(projectURL, f2, SVNRevision.getRevision("" + l),
						true);
				f4.renameTo(f3);
				if (!p.exists())
					p.create(null);
				p.open(null);
				// compute metrics on p
				SystemDef sys = SystemManager.getSystem(this.getProject()
						.getName());
				ParserAppli pa = new ParserAppli();
				Entity.setCurrentVersion(l);
				pa.buildModelVersion(l, sys, p.getName());
			}

		} catch (Exception e) {
			System.out.println(e);
		}
		Entity.setCurrentVersion(-1);// back to the currentVersion
		try {
			p.close(null);
			p.delete(true, true, null);
			this.deleteDirectory(f2);
			SystemManager.removeProject(p.getName());
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	

	private void deleteDirectory(File f) {
		if (f.isDirectory()) {
			File[] files = f.listFiles();
			for (File fi : files)
				deleteDirectory(fi);
		}
		boolean deleted = f.delete();
		if (deleted)
			System.out.println("Deleted!!!!!!!!!!!!!!");
		else
			System.out.println("Not at All Deleted !!!!!!!!!!!!1");
	}

	public static void callListeners(SystemDef sys)
	{
		for (VersoModificationListener lis : listenerList) {
			lis.listen(sys);
		}
	}

	public static void addListener(VersoModificationListener lis) {
		listenerList.add(lis);
	}

}
