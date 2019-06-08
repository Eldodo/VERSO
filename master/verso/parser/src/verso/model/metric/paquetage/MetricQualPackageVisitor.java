package verso.model.metric.paquetage;

import verso.model.Package;

public class MetricQualPackageVisitor implements PackageVisitor{

	//bonjour ...
	public void visit(Package p) {
		
		//visiting children
		for (Package pac : p.getSubPackages())
		{
			pac.accept(this);
		}
		//visiting current Package
		p.accept(new PackageComplexityVisitor());
		p.accept(new PackageCouplingVisitor());
		p.accept(new PackageDITVisitor());
		p.unsetDirtyMetricQual();
	}

}
