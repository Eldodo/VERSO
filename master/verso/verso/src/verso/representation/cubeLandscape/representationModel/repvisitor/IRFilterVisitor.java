package verso.representation.cubeLandscape.representationModel.repvisitor;

import java.util.HashSet;
import java.util.Set;

import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.MethodRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;

/**
 * Visite tous les éléments sélectionné pour filtrer les éléments
 * présents dans filteredEntities.
 * Ces éléments seront ensuite mis en surbrillance
 * 
 * @author Maxime Gallais-Jimenez
 *
 */
public class IRFilterVisitor implements IRepresentationVisitor {

	Set<String> filteredEntities = new HashSet<String>();
	
	public IRFilterVisitor(Set<String> filteredEntities) {
		this.filteredEntities = filteredEntities;
	}
	
	@Override
	public void visit(PackageRepresentation p) {
		for(PackageRepresentation pck : p.getPackages()) {
			pck.accept(this);
		}
		for(ElementRepresentation e : p.getElements()) {
			e.accept(this);
		}

	}

	@Override
	public void visit(ElementRepresentation e) {
		if (this.filteredEntities.contains(e.getElementModel().getName())) {
			e.setFiltered();
			e.select();
		}else {
			e.setUnFiltered();
			e.unSelect();
		}
		for (MethodRepresentation m : e.getMethods()) {
			m.accept(this);
		}

	}

	@Override
	public void visit(MethodRepresentation method) {
		if (this.filteredEntities.contains(method.getElement().getName())) {
			method.setFiltered();
			method.select();
		}

	}

}
