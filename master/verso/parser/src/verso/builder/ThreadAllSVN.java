package verso.builder;

import java.io.File;

import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;

import verso.model.SystemDef;

public class ThreadAllSVN extends Thread{
	
	ISVNClientAdapter svnc = null;
	SystemDef sys = null;
	
	public ThreadAllSVN(ISVNClientAdapter svnc, SystemDef sys)
	{
		this.svnc = svnc;
		this.sys = sys;
	}
	
	public void run()
	{
		try{
			long currentRev = 0;
			long nextRev = 0;
			for (File f : sys.getRoots())
			{
				sys.setRevision0();
				currentRev = sys.getRevision(f);
				nextRev = 0;
				try{
				ISVNInfo inf = svnc.getInfo(f);// le faire plutôt par package fragments
				nextRev = inf.getRevision().getNumber();
				if (nextRev > currentRev) {
					SVNMessageLogger.logMessages(currentRev, nextRev, svnc, f,sys); //fragRoots by fragroots
				}
				sys.setRevision(nextRev,f);
				}catch(Exception e){System.out.println(e);}
			}
			sys.computeSVNMetrics();
			System.out.println("Done computing SVN Metrics(not annotations) for " + sys.getRoots());
			//ThreadAnnotation ta = new ThreadAnnotation(currentRev, nextRev, svnc, sys);
			//ta.start();
		}catch(Exception e){System.out.println(e);}
	}
}
