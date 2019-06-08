package verso.model.metric.paquetage;

import verso.model.Package;

public class MetricQualPackageVisitorPartial implements PackageVisitor{

	public String packageToVisit = "";
	
	
	public void visit(Package p) {
		for (Package pac : p.getSubPackages())
		{
			if (pac.isDirtyMetricQual())
			{
				pac.accept(this);
			}
		}
		if (p.isDirtyMetricQual())
		{
			p.accept(new PackageComplexityVisitor());
			p.accept(new PackageCouplingVisitor());
			p.accept(new PackageDITVisitor());
		}
		p.unsetDirtyMetricQual();
	}

}
