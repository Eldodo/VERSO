package verso.model.metric.paquetage;

import verso.model.Element;
import verso.model.Package;
import verso.model.SystemDef;
import verso.model.metric.DecimalNumberMetric;

public class PackageComplexityVisitor implements PackageVisitor {

	
	public void visit(Package p) {
		double wmc = 0;
		for (Element e : p.getSubElements()) {
			wmc += (Double) e.getMetric("WMC").getValue();
		}
		for (Package pac : p.getSubPackages()) {
			wmc += (Double) pac.getMetric("PackageComplexity").getValue();
		}
		p.addMetric(new DecimalNumberMetric<Double>(SystemDef.PACKAGECOMPLEXITYDESCRIPTOR, wmc));
	}

}
