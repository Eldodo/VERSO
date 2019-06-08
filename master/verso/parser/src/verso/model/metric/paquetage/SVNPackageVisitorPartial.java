package verso.model.metric.paquetage;

import verso.model.Package;
import verso.model.SystemDef;

public class SVNPackageVisitorPartial implements PackageVisitor {

	SystemDef currentSys = null;
	
	public void setSystem(SystemDef sys)
	{
		this.currentSys = sys;
	}
	
	
	public void visit(Package p) {
		
		for (Package pac : p.getSubPackages())
		{
			if (pac.isDirtyMetricSVN())
			{
				pac.accept(this);
			}
		}
		if (p.isDirtyMetricSVN())
		{
			p.accept(new PackageSVNIntroDateVisitor());
			p.accept(new PackageSVNNumberOfAuthorsVisitor());
			p.accept(new PackageSVNNumberOfCommitVisitor());
			p.accept(new PackageSVNUpdateVisitor());
			PackageSVNMainAuthorVisitor v = new PackageSVNMainAuthorVisitor();
			v.setSystemDef(currentSys);p.accept(v);
		}
		p.unsetDirtyMetricSVN();
	}

}
