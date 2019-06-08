package verso.representation.cubeLandscape.filter;

import java.util.HashSet;
import java.util.Set;

import verso.representation.IFilter;
import verso.representation.IPickable;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;
import verso.representation.cubeLandscape.representationModel.SystemRepresentation;
import verso.representation.cubeLandscape.representationModel.repvisitor.EntityUnFilterVisitor;

public class EntityFilter implements IFilter {

	protected Set<String> filteredClasses = new HashSet<String>();
	protected SystemRepresentation sys;
	protected boolean filterable = false;

	public EntityFilter(SystemRepresentation sys) {
		this.sys = sys;
	}

	public void setSystem(SystemRepresentation sys) {
		this.sys = sys;
	}

	public void setElement(IPickable er) {
	}

	public void filter() {

	}

	public void unfilter() {
		SystemRepresentation.filterState = false;
		for (PackageRepresentation p : sys.getPackages()) {
			p.accept(new EntityUnFilterVisitor());
		}
	}

}
