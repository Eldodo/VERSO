package verso.representation.cubeLandscape.modelVisitor;

import java.awt.Color;

import verso.model.Attribute;
import verso.model.ClassDef;
import verso.model.Element;
import verso.model.Entity;
import verso.model.InterfaceDef;
import verso.model.LibDef;
import verso.model.Line;
import verso.model.Method;
import verso.model.Package;
import verso.model.SystemDef;
import verso.model.Visitor;
import verso.representation.Lines.representationModel.LineRepresentation;
import verso.representation.cubeLandscape.representationModel.AttributeRepresentation;
import verso.representation.cubeLandscape.representationModel.ClassRepresentation;
import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.InterfaceRepresentation;
import verso.representation.cubeLandscape.representationModel.LibRepresentation;
import verso.representation.cubeLandscape.representationModel.MethodRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;
import verso.representation.cubeLandscape.representationModel.SystemRepresentation;
import verso.representation.cubeLandscape.representationModel.TreemapPackageRepresentation;

public class CubeLandScapeVisitor implements Visitor{
	
	public Object visit(SystemDef system) {
		SystemRepresentation systemRep = new SystemRepresentation(system);
		for (Package p : system.getPackages()) {
			
			systemRep.addPackage((PackageRepresentation) p.accept(this));
		}
		return systemRep;
	}
	
	public Object visit(Package packagedef) {
		TreemapPackageRepresentation packageRep = new TreemapPackageRepresentation(packagedef);
		packageRep.setColor(new Color(0.7f, 0.7f, 0.7f));
		for (Package p : packagedef.getSubPackages()) {
			packageRep.addPackage((TreemapPackageRepresentation) p.accept(this));
		}
		for (Element e : packagedef.getSubElements()) {
			packageRep.addElement((ElementRepresentation) e.accept(this));
		}
		return packageRep;
	}

	public Object visit(InterfaceDef inter) {
		InterfaceRepresentation interRep = new InterfaceRepresentation(inter);
		interRep.setHeight((float) Math.random() * 2.0f + 0.75f);
		for (Method met : inter.getMethods()) {
			interRep.addMethod((MethodRepresentation) met.accept(this));
		}

		return interRep;
	}
	
	public Object visit(ClassDef classe) {
		ClassRepresentation classRep = new ClassRepresentation(classe);
		classRep.setHeight((float) Math.random() * 2.0f + 0.75f);
		for (Method met : classe.getMethods()) {
			classRep.addMethod((MethodRepresentation) met.accept(this));
		}

		return classRep;
	}
	
	public Object visit(LibDef lib) {
//		System.out.println("CubeLandScapeVisitor.visit()");
		LibRepresentation libRep = new LibRepresentation(lib);
		libRep.setHeight((float) Math.random() * 2.0f + 0.75f);
		return libRep;
	}

	public Object visit(Attribute attribute) {
		AttributeRepresentation attRep = new AttributeRepresentation(attribute);
		return attRep;
	}
	
	public Object visit(Method method) {
		MethodRepresentation metRep = new MethodRepresentation(method);
		return metRep;
	}

	public Object visit(Entity en) {
		// should not be called
		return null;
	}

	public Object visit(Line l) {
		return new LineRepresentation(l);
	}

}