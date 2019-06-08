package verso.model.metric.paquetage;

import java.util.Set;

import verso.model.Package;
import verso.model.SystemDef;
import verso.model.metric.DecimalNumberMetric;

public class PackageCouplingVisitor implements PackageVisitor {

	
	public void visit(Package p) {
		Set<String> targets = p.getAllTargets();
		int coupling = 0;
		for (String tar : targets)
		{
			if (!tar.contains(p.getName()))
			{
				coupling++;
			}
		}
		p.addMetric(new DecimalNumberMetric<Double>(SystemDef.PACKAGECOUPLINGDESCRIPTOR, (double)coupling));
	}

}
