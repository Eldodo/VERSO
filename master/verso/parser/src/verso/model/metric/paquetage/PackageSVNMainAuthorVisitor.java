package verso.model.metric.paquetage;

import java.util.Hashtable;
import java.util.List;

import verso.model.Package;
import verso.model.SystemDef;
import verso.model.history.Commit;
import verso.model.metric.NominaleMetric;

public class PackageSVNMainAuthorVisitor implements PackageVisitor {

	SystemDef currentSys = null;
	Hashtable<String,Integer> authors = new Hashtable<String, Integer>();
	
	public void setSystemDef(SystemDef sys)
	{
		this.currentSys = sys;
	}
	

	public void visit(Package p) {
		List<Commit> lst = p.getAllCommits();
		for (Commit c : lst)
		{
			if (authors.containsKey(c.getAuthor()))
			{
				authors.put(c.getAuthor(), authors.get(c.getAuthor()) + 1);
			}
			else
			{
				authors.put(c.getAuthor(), 1);
			}
		}
		int maxauthor = 0;
		String maxauth = "";
		for (String auth : authors.keySet())
		{
			if (authors.get(auth) > maxauthor)
			{
				maxauthor = authors.get(auth);
				maxauth = auth;
			}
		}
		
		p.addMetric(new NominaleMetric<String>(currentSys.getPackageMainAuthorDescriptor(),maxauth));
	}

}
