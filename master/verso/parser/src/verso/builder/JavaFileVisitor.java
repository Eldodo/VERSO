package verso.builder;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.tigris.subversion.svnclientadapter.ISVNAnnotations;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;

import verso.model.Element;
import verso.model.Line;
import verso.model.Method;
import verso.model.SystemManager;
import verso.model.metric.DateMetric;
import verso.model.metric.IntegralNumberMetric;
import verso.model.metric.IntervaleMetricDescriptor;
import verso.model.metric.NominaleMetric;
import verso.model.metric.NominaleMetricDescriptor;
import verso.util.TypeFinder;

public class JavaFileVisitor implements IResourceVisitor{

	String projectName = "";
	ISVNClientAdapter svnc = null;
	
	public boolean visit(IResource resource) throws CoreException {
		if (resource.getType() == IResource.FILE)
		{
			if (resource.getFileExtension().compareTo("java") ==0)
			{
				//System.out.println("Check fileNames : " + TypeFinder.findFileNameFromIFile((IFile)resource));
				Element currElem = SystemManager.getSystem(this.projectName).getElement(TypeFinder.findFileNameFromIFile((IFile)resource));
				if (currElem != null)
				{
					//File f = new File(TypeFinder.findFilePathfromFile((IFile)resource));
					File f = resource.getLocation().toFile();
					//System.out.println("Printing file name : " + f.toString());
					ISVNAnnotations annotations = null;
					try{
						annotations = svnc.annotate(f, SVNRevision.START, SVNRevision.HEAD);
					}catch(Exception e){System.out.println(e);}
					//readAnnotations(annotations,currElem);
				}
				//System.out.println("test");
			}
			return false;
		}
		return true;
	}
	
	public void setSVNClient(ISVNClientAdapter svnc)
	{
		this.svnc = svnc;
		//nouveau truc, est-ce que blame va le voir?
	}
	
	public void setProjectName(String projName)
	{
		this.projectName = projName;
	}
	
	
	
	public void singleFileVisit(File f, String projName, Element currElem)
	{
		ISVNClientAdapter svnc = null;
		try {
			svnc = JhlClientAdapterFactory
					.createSVNClient(JhlClientAdapterFactory
							.getPreferredSVNClientType());
		} catch (Exception e) {
			System.out.println(e);
		}
		this.setSVNClient(svnc);
		this.setProjectName(projName);
		
		ISVNAnnotations annotations = null;
		try{
			annotations = svnc.annotate(f, SVNRevision.START, SVNRevision.HEAD);
		}catch(Exception e){System.out.println(e);}
		//readAnnotations(annotations,currElem);
	}
	

}
