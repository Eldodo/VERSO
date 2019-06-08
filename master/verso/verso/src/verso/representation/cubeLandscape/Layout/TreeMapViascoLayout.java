package verso.representation.cubeLandscape.Layout;

import java.util.PriorityQueue;

import verso.model.Package;
import verso.representation.Layout;
import verso.representation.cubeLandscape.representationModel.TreemapPackageRepresentation;
/**
 * Layout used to arranche packages before sending them to the Treemap layout.
 * Rearrange the the package representation so it has an inclusion hierarchy.
 * @author Guillaume
 *
 */
public class TreeMapViascoLayout extends Layout
{
	public TreemapPackageRepresentation layout(TreemapPackageRepresentation pRoot)
	{
		TreemapPackageRepresentation currPac = null;
		PriorityQueue<TreemapPackageRepresentation> heap = new PriorityQueue<TreemapPackageRepresentation>(pRoot.getTreemapPackages());
		while (heap.size() > 1)
		{
			currPac = new TreemapPackageRepresentation(new Package("pac" + heap.size()));
			currPac.addPackage(heap.poll());
			currPac.addPackage(heap.poll());
			currPac.setFake();
			heap.add(currPac);
		}
		return heap.poll();
	}
}


