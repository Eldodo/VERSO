package verso.representation.cubeLandscape.modelVisitor;

import java.awt.Color;

import verso.model.Element;
import verso.model.Package;
import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.TreemapPackageRepresentation;

public class SimonLandScapeVisitor extends CubeLandScapeVisitor {
	public Object visit(Package packagedef)
	{
		TreemapPackageRepresentation packageRep = new TreemapPackageRepresentation(packagedef);
		packageRep.setColor(new Color(0.7f,0.7f,0.7f));
		for (Package p : packagedef.getSubPackages())
		{
			packageRep.addPackage((TreemapPackageRepresentation) p.accept(this));
		}
		
		if (packagedef.getSubPackages().size() == 0) {
			for (Element e : packagedef.getSubElements())
			{
				packageRep.addElement((ElementRepresentation) e.accept(this));
			}
		}
		else {
			if (packagedef.getSubElements().size() > 0) {
				TreemapPackageRepresentation elementsPackage = new TreemapPackageRepresentation(new Package(packagedef.getName() + "_Elements"));
				elementsPackage.setFake();
				
				for (Element e : packagedef.getSubElements()) {
					elementsPackage.addElement((ElementRepresentation) e.accept(this));
				}
				
				packageRep.addPackage(elementsPackage);
			}
		}
		
		return packageRep;
	}
}
