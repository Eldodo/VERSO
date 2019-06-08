package verso.model.metric.paquetage;

import verso.model.Element;
import verso.model.Package;
import verso.model.SystemDef;
import verso.model.metric.DecimalNumberMetric;

public class PackageSVNNumberOfCommitVisitor implements PackageVisitor {

	
	public void visit(Package p) {
		double numberOfCommits = 0;
		for (Package pac : p.getSubPackages())
		{
			numberOfCommits += (Double)pac.getMetric("PackageNumberOfCommits").getValue();
		}
		for (Element e : p.getSubElements())
		{
			numberOfCommits += (Double)e.getMetric("NumberOfCommits").getValue();
		}
		p.addMetric(new DecimalNumberMetric<Double>(SystemDef.PACKAGENUMBEROFCOMMITSDESCRIPTOR, numberOfCommits));
	}

}
