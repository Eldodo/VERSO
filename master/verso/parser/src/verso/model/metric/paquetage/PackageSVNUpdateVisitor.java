package verso.model.metric.paquetage;

import java.util.Date;

import verso.model.Element;
import verso.model.Package;
import verso.model.SystemDef;
import verso.model.metric.DateMetric;

public class PackageSVNUpdateVisitor implements PackageVisitor {

	
	public void visit(Package p) 
	{
		Date currDate = null;
		Date updateDate = new Date(0);
		for (Package pac : p.getSubPackages())
		{
			currDate = (Date)pac.getMetric("PackageUpDate").getValue();
			if (updateDate.compareTo(currDate) < 0)
				updateDate = currDate;
		}
		for (Element e : p.getSubElements())
		{
			currDate = (Date)e.getMetric("LastUpdate").getValue();
			if (updateDate.compareTo(currDate) < 0)
				updateDate = currDate;
		}
		p.addMetric(new DateMetric(SystemDef.PACKAGEUPDATEDESCRIPTOR, updateDate));
	}

}
