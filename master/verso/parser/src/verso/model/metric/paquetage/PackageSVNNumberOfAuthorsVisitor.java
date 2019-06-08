package verso.model.metric.paquetage;

import verso.model.Package;
import verso.model.SystemDef;
import verso.model.metric.DecimalNumberMetric;

public class PackageSVNNumberOfAuthorsVisitor implements PackageVisitor{

	
	public void visit(Package p) {
		int nbAuthors = p.getAllAuthors().size();
		p.addMetric(new DecimalNumberMetric<Double>(SystemDef.PACKAGENUMBEROFAUTHORSDESCRIPTOR, (double)nbAuthors));
		
	}

}
