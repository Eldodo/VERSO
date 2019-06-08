package verso.builder;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.tigris.subversion.svnclientadapter.ISVNAnnotations;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNLogMessageChangePath;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;

import verso.model.Element;
import verso.model.Line;
import verso.model.Method;
import verso.model.SystemDef;
import verso.model.SystemManager;
import verso.model.history.Commit;
import verso.model.metric.DateMetric;
import verso.model.metric.DecimalNumberMetric;
import verso.model.metric.IntegralNumberMetric;
import verso.model.metric.IntervaleMetricDescriptor;
import verso.model.metric.NominaleMetric;
import verso.model.metric.NominaleMetricDescriptor;
import verso.util.TypeFinder;


public class SVNMessageLogger {
	
	public static void logMessages(Element e, File elemLocation, SystemDef sys)
	{
		e.setFileLocation(elemLocation);
		ISVNClientAdapter svnc = null;
		try{
		svnc = JhlClientAdapterFactory
		.createSVNClient(JhlClientAdapterFactory.getPreferredSVNClientType());
		ISVNInfo inf = svnc.getInfo(elemLocation);
		long endRev = inf.getRevision().getNumber();
		logMessages(0,endRev,svnc,e,sys);
		}catch(Exception ex){System.out.println(e);};
		
		
	}
	
	public static void logMessages(long startRev, long endRev, ISVNClientAdapter svnc, File f, SystemDef sys)
	{
		String currentMessage = "";
		ISVNLogMessage[] messages = null;
		System.out.println("Parsing SVN of " + f.getAbsolutePath());
		if (f != null)
		{
			try{

			messages = svnc.getLogMessages(f,
					SVNRevision.getRevision("" + (startRev + 1)), 
					//SVNRevision.getRevision("" + endRev)
					SVNRevision.BASE);
			}catch(Exception ex){System.out.println(ex);}
			sys.removeCommits();
			for (int i = 0; i < messages.length; i++) 
			{
				currentMessage = messages[i].getMessage();
				ISVNLogMessageChangePath[] paths = messages[i].getChangedPaths();
				SystemManager.getSystem(sys.getName()).addAuthor(messages[i].getAuthor());
				for (int j = 0; j < paths.length; j++)
				{
					String realPath = paths[j].getPath();
					if (realPath.endsWith(".java"))
					{
						Element e = TypeFinder.findElementFromChangePath(realPath, sys);
						if (e != null)
							e.addCommit(new Commit(messages[i].getRevision().getNumber(), messages[i].getAuthor(),messages[i].getDate(),currentMessage));
					}
				}		
			}
		}
	}
	
	public static void logMessages(long startRev, long endRev, ISVNClientAdapter svnc, Element e, SystemDef sys)
	{
		//à vérifier si on a du temps seulement
		//Abandonner cette idée, remplacer par un refresh svnInfo ... l'expérience est bientôt
		//((SVNTeamProvider)RepositoryProvider.getProvider((IProject) ResourcesPlugin.getWorkspace().getRoot().getProject(sys.getName()))).getSVNWorkspaceRoot().getRepository().getSVNClient().addNotifyListener(arg0);
		String currentMessage = "";
		ISVNLogMessage[] messages = null;
		System.out.println("Parsing SVN of " + e.getName());
		if (e.getFileLocation() != null)
		{
			try{
				
			messages = svnc.getLogMessages(e.getFileLocation(),
					SVNRevision.getRevision("" + (startRev + 1)), 
					//SVNRevision.getRevision("" + endRev)
					SVNRevision.BASE);
			
			for (int i = 0; i < messages.length; i++) 
			{
				currentMessage = messages[i].getMessage();
				ISVNLogMessageChangePath[] paths = messages[i].getChangedPaths();
				SystemManager.getSystem(sys.getName()).addAuthor(messages[i].getAuthor());
				if (paths.length > 0)
					e.addCommit(new Commit(messages[i].getRevision().getNumber(),/*paths[0].getAction() ,*/ messages[i].getAuthor(),messages[i].getDate(),currentMessage));
			}
			e.computeSVNMetrics(sys);
			sys.setDirtyPackages(e.getPackage(), SystemDef.SVNMETRIC);
			}catch(Exception ex){System.out.println(ex);}
			ISVNAnnotations annotations = null;
			try{
				annotations = svnc.annotate(e.getFileLocation(), SVNRevision.START, SVNRevision.BASE);
			}catch(Exception ex){System.out.println(ex);}
			readAnnotations(annotations,e, endRev);
		}
	}
	
	public static void logAnnotations(long startRev, long endRev, ISVNClientAdapter svnc, SystemDef sys)
	{
		for (Element e : sys.getAllElements())
		{
			System.out.println("Reading annotation of :" + e.getName());
			ISVNAnnotations annotations = null;
			try{
				annotations = svnc.annotate(e.getFileLocation(), SVNRevision.START, SVNRevision.BASE);
				ISVNInfo inf = svnc.getInfo(e.getFileLocation());// le faire plutôt par package fragments
				endRev = inf.getRevision().getNumber();
			}catch(Exception ex){System.out.println(ex);}
			readAnnotations(annotations,e, endRev);
			
		}
	}

	public static void logMessages(long startRev, long endRev, ISVNClientAdapter svnc, SystemDef sys) 
	{
		// start a thread ... or not??S
		//Element by element solution
		for (Element e : sys.getAllElements())
		{
			logMessages(startRev, endRev, svnc, e, sys);
		}
	}
	
	public static void logMessages(long startRev, long endRev, ISVNClientAdapter svnc, SystemDef sys, List<File> fragRoots) 
	{
		for (File f : fragRoots)
		{
			logMessages(startRev, endRev, svnc, f, sys);
		}
	}
	
	private static void readAnnotations(ISVNAnnotations annotations,Element elem, long endRev)
	{
		if (annotations == null)
			return;
		//System.out.println("computing some metrics");
		// Création intervale pour Date
		long firstDate = annotations.getChanged(0).getTime();
		long lastDate = annotations.getChanged(0).getTime();
		int maximum = elem.getLines().size();
		if (annotations.numberOfLines() < maximum)
			maximum = annotations.numberOfLines();
		for (int i = 0; i<maximum; i++)
		{
			if (annotations.getChanged(i).getTime()<firstDate)
				firstDate = annotations.getChanged(i).getTime();
			if (annotations.getChanged(i).getTime()>lastDate)
				lastDate = annotations.getChanged(i).getTime();
		}
		Date first = new Date(firstDate);
		Date last = new Date(lastDate);
		
		
		// Création Collection d'auteurs
		Set<String> authorSet = new TreeSet();
		
		for (int i = 0; i<annotations.numberOfLines()/*elem.getLines().size()*/; i++)
		{
			authorSet.add(annotations.getAuthor(i));
		}
		
		// Création intervale pour Revision
		
		long min = 0;//annotations.getRevision(0);
		long max = endRev;//annotations.getRevision(0);
		/*
		for (int i = 0 ; i<elem.getLines().size(); i++)
		{
			
			if (annotations.getRevision(i)<min)
				min = annotations.getRevision(i);
			if (annotations.getRevision(i)>max)
				max = annotations.getRevision(i);
		}
		*/
		for(int i = 0; i<elem.getLines().size(); i++)
		{
			elem.getLine(i).addMetric(new NominaleMetric<String>(new NominaleMetricDescriptor<String>("Author", authorSet),annotations.getAuthor(i)));
			elem.getLine(i).addMetric(new IntegralNumberMetric<Long>(new IntervaleMetricDescriptor<Long>("Revision", min, max), annotations.getRevision(i)));
			elem.getLine(i).addMetric(new DateMetric(new IntervaleMetricDescriptor<Date>("LastChangeDate", first, last), annotations.getChanged(i)));
		}
		for (Method m : elem.getMethods())
		{
			HashMap<String,Integer> mainAuthorMap = new HashMap<String,Integer>();
			int mainAuthorNum = 0;
			String mainAuthor = "";
			String lastAuthor = "";
			long latestRevision = -1;
			Date latestDate = new Date(0);
			Set<Long> allRevision = new HashSet<Long>();
			//if (m.getLines().size() == 0)
				//System.out.println("WTF ... hummm vérifier ceci");
			for (Line l : m.getLines())
			{
				allRevision.add((Long)l.getMetric("Revision").getValue());
				if (!mainAuthorMap.containsKey((String)l.getMetric("Author").getValue()))
				{
					mainAuthorMap.put((String)l.getMetric("Author").getValue(), new Integer(1));
				}
				else
				{
					String aut = (String)l.getMetric("Author").getValue();
					mainAuthorMap.put(aut, mainAuthorMap.get(aut)+1);
				}
				if (latestRevision < (Long)l.getMetric("Revision").getValue())
				{
					latestRevision = (Long)l.getMetric("Revision").getValue();
					latestDate = (Date)l.getMetric("LastChangeDate").getValue();
					lastAuthor = (String)l.getMetric("Author").getValue();
				}
			}
			for(String s : mainAuthorMap.keySet())
			{
				if (mainAuthorMap.get(s) > mainAuthorNum)
				{
					mainAuthor = s;
					mainAuthorNum = mainAuthorMap.get(s);
				}
			}
			if (elem.getLines().size() > 0)
			{
				m.addMetric(new NominaleMetric<String>(new NominaleMetricDescriptor<String>("MetMainAuthor", authorSet),mainAuthor));
				m.addMetric(new NominaleMetric<String>(new NominaleMetricDescriptor<String>("MetLastAuthor", authorSet),lastAuthor));
				m.addMetric(new DecimalNumberMetric<Double>(new IntervaleMetricDescriptor<Double>("MetLatestRevision", (double)min, (double)max), (double)latestRevision));
				m.addMetric(new DateMetric(new IntervaleMetricDescriptor<Date>("MetLastChangeDate", first, last), latestDate));
				m.addMetric(new DecimalNumberMetric<Double>(new IntervaleMetricDescriptor<Double>("MetNumberOfRevision", (double)0, (double)10), (double)allRevision.size()));
			}
		}
	}
	
	public static void updateCurrentVersion(SystemDef sys, IProject p) {
		ISVNClientAdapter svnc = null;
		//SystemDef sys = SystemManager.getSystem(getProject().getName());
		
		try {
			svnc = JhlClientAdapterFactory
					.createSVNClient(JhlClientAdapterFactory
							.getPreferredSVNClientType());
			
			ThreadAllSVN tas = new ThreadAllSVN(svnc, sys);
			tas.run();
			
			
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void updateAnnotationsVersion(SystemDef sys, IProject p) {
		ISVNClientAdapter svnc = null;
		//SystemDef sys = SystemManager.getSystem(getProject().getName());
		
		try {
			svnc = JhlClientAdapterFactory
					.createSVNClient(JhlClientAdapterFactory
							.getPreferredSVNClientType());
			
			ThreadAnnotation ta = new ThreadAnnotation(0, 0, svnc, sys);
			ta.start();
			
			
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
