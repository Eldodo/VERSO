package verso.model.metric.paquetage;

import java.util.Date;

import verso.model.Element;
import verso.model.Package;
import verso.model.SystemDef;
import verso.model.metric.DateMetric;

public class PackageSVNIntroDateVisitor implements PackageVisitor {

	
	public void visit(Package p) 
	{
		Date currDate = null;
		Date introDate = new Date(System.currentTimeMillis());
		for (Package pac : p.getSubPackages())
		{
			currDate = (Date)pac.getMetric("PackageIntroDate").getValue();
			if (introDate.compareTo(currDate) > 0)
				introDate = currDate;
		}
		for (Element e : p.getSubElements())
		{
			currDate = (Date)e.getMetric("DateIntro").getValue();
			if (introDate.compareTo(currDate) > 0)
				introDate = currDate;
		}
		p.addMetric(new DateMetric(SystemDef.PACKAGEDATEINTRODESCRIPTOR, introDate));
	}

}
