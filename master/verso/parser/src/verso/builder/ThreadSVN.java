package verso.builder;

import java.io.File;

import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;

import verso.model.SystemDef;

public class ThreadSVN extends Thread{

	long currentRev = 0;
	long nextRev = 0;
	ISVNClientAdapter svnc = null;
	SystemDef sys = null;
	File fragRoot = null;
	public ThreadSVN(long currentRev, long nextRev, ISVNClientAdapter svnc, SystemDef sys, File fragRoot)
	{
		this.currentRev = currentRev;
		this.nextRev = nextRev;
		this.svnc = svnc;
		this.sys = sys;
		this.fragRoot = fragRoot;
	}
	
	public void run()
	{
		try{
			//SVNMessageLogger.logMessages(currentRev, nextRev, svnc, sys); //fichier par fichier
			SVNMessageLogger.logMessages(currentRev, nextRev, svnc, fragRoot,sys); //fragRoots by fragroots
			//sys.computeSVNMetrics();
		}catch(Exception e){System.out.println(e);}
	}
}
