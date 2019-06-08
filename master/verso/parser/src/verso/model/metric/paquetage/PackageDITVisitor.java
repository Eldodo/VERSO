package verso.model.metric.paquetage;

import verso.model.Element;
import verso.model.Package;
import verso.model.SystemDef;
import verso.model.metric.DecimalNumberMetric;

public class PackageDITVisitor implements PackageVisitor{

	
	public void visit(Package p) {
		int totalClasses = 0;
		double totalDit = 0;
		double dit;
		for (Element e : p.getSubElements())
		{
			totalClasses++;
			totalDit += (Double)e.getMetric("DIT").getValue();
		}
		for (Package pac : p.getSubPackages())
		{
			int numbClass = pac.getNumberOfClasses();
			totalClasses += numbClass;
			totalDit = (Double)pac.getMetric("PackageDIT").getValue() * (double)numbClass;
		}
		dit = totalDit / (double) totalClasses;
		p.addMetric(new DecimalNumberMetric<Double>(SystemDef.PACKAGEDITDESCRIPTOR, dit));
	}

	
}
