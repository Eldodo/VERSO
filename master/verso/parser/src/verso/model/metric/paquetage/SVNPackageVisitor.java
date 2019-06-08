package verso.model.metric.paquetage;

import verso.model.Package;
import verso.model.SystemDef;

public class SVNPackageVisitor implements PackageVisitor {

	SystemDef currentSys = null;
	
	public void setSystem(SystemDef sys)
	{
		this.currentSys = sys;
	}
	
	
	public void visit(Package p) {
		
		for (Package pac : p.getSubPackages())
		{
			pac.accept(this);
		}
		p.accept(new PackageSVNIntroDateVisitor());
		p.accept(new PackageSVNNumberOfAuthorsVisitor());
		p.accept(new PackageSVNNumberOfCommitVisitor());
		p.accept(new PackageSVNUpdateVisitor());
		PackageSVNMainAuthorVisitor v = new PackageSVNMainAuthorVisitor();
		v.setSystemDef(currentSys);p.accept(v);	
		p.unsetDirtyMetricSVN();
	}

}
