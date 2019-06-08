package verso.representation.cubeLandscape.filter;

import java.util.HashSet;
import java.util.Set;

import verso.representation.IPickable;
import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;
import verso.representation.cubeLandscape.representationModel.SystemRepresentation;
import verso.representation.cubeLandscape.representationModel.repvisitor.EntityFilterVisitorAll;

public class TargetAllFilter extends EntityFilter {

	PackageRepresentation pac = null;
	Set<String> filteredEntities = new HashSet<String>();

	public TargetAllFilter(SystemRepresentation sys) {
		super(sys);
	}

	public void setElement(IPickable er) {
		filteredEntities.clear();
		if (er instanceof PackageRepresentation) {
			PackageRepresentation e = (PackageRepresentation) er;
			this.filterable = true;
			filteredEntities.addAll(e.getPackage().getAllPackageTargets());
			filteredEntities.addAll(e.getPackage().getAllTargets());
			e.select();
			this.filter();
		} else if (er instanceof ElementRepresentation) {
			ElementRepresentation e = (ElementRepresentation) er;
			this.filterable = true;
			filteredEntities.addAll(e.getElementModel().getTargets());
			filteredEntities.addAll(e.getElementModel().getAllPackageTargets());
			e.select();
			this.filter();
		} else {
			this.filterable = false;
		}
	}

	public void filter() {
		if (filterable) {
			SystemRepresentation.filterState = true;
			for (PackageRepresentation p : sys.getPackages()) {
				p.accept(new EntityFilterVisitorAll(this.filteredEntities));
			}
		}
	}

}
