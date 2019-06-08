package verso.representation.cubeLandscape.Layout;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

import verso.model.Package;
import verso.representation.Layout;
import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.TreemapPackageRepresentation;

public class TreeMapPierreLayout extends Layout 
{
	public TreemapPackageRepresentation layout(TreemapPackageRepresentation pRoot) {
		for (TreemapPackageRepresentation childPack : pRoot.getTreemapPackages()) {
			this.layout(childPack);
		}
		
		
		if (pRoot.getElements().size() > 0 && pRoot.getPackages().size() > 0) {
			//TreemapPackageRepresentation elementsPack = new TreemapPackageRepresentation(new Package(pRoot.getSimpleName() + ".elementsPack"));
			TreemapPackageRepresentation elementsPack = new TreemapPackageRepresentation(new Package(pRoot.getSimpleName() + "_elements"));
			elementsPack.setFake();
			
			for (ElementRepresentation e : pRoot.getElements()) {
				elementsPack.addElement(e);
			}
			
			pRoot.clearElements();
			pRoot.addPackage(elementsPack);
		}
		
		
		if (pRoot.getTreemapPackages().size() > 2) {
			PriorityQueue<TreemapPackageRepresentation> heap = new PriorityQueue<TreemapPackageRepresentation>(11, new TreemapInverseComparator());
			
			Iterator<TreemapPackageRepresentation> childPackagesItr = pRoot.getTreemapPackages().iterator();
			while (childPackagesItr.hasNext()) {
				heap.add(childPackagesItr.next());
			}
			
			LinkedList<TreemapPackageRepresentation> leftPart = new LinkedList<TreemapPackageRepresentation>();
			LinkedList<TreemapPackageRepresentation> rightPart = new LinkedList<TreemapPackageRepresentation>();
			int leftPartTotalDescendantClasses = 0;
			int rightPartTotalDescendantClasses = 0;

			while (heap.size() > 0) {
				if (leftPartTotalDescendantClasses < rightPartTotalDescendantClasses) {
					leftPartTotalDescendantClasses += heap.peek().countDescendantClasses();					
					leftPart.add(heap.poll());
				}
				else {				
					rightPartTotalDescendantClasses += heap.peek().countDescendantClasses();
					rightPart.add(heap.poll());
				}
			}
			
			TreemapPackageRepresentation newPackLeft = new TreemapPackageRepresentation(new Package(pRoot.getSimpleName() + ".leftPack"));
			TreemapPackageRepresentation newPackRight = new TreemapPackageRepresentation(new Package(pRoot.getSimpleName() + ".rightPack"));
			
			newPackLeft.setFake();
			newPackRight.setFake();
			
			childPackagesItr = leftPart.iterator();
			while (childPackagesItr.hasNext()) {
				newPackLeft.addPackage(childPackagesItr.next());
			}
			
			childPackagesItr = rightPart.iterator();
			while (childPackagesItr.hasNext()) {
				newPackRight.addPackage(childPackagesItr.next());
			}
			
			pRoot.clearPackages();
			
			if (false && newPackLeft.getPackages().size() == 1) {
				TreemapPackageRepresentation tempPack = newPackLeft.getTreemapPackages().iterator().next(); 
				pRoot.addPackage(tempPack);
				this.layout(tempPack);
			}
			else {
				pRoot.addPackage(newPackLeft);
				this.layout(newPackLeft);
			}
			
			if (false && newPackRight.getPackages().size() == 1) {
				TreemapPackageRepresentation tempPack = newPackRight.getTreemapPackages().iterator().next(); 
				pRoot.addPackage(tempPack);
				this.layout(tempPack);
			}
			else {
				pRoot.addPackage(newPackRight);
				
				this.layout(newPackRight);
			}
			
		}
		
		
		return pRoot;
	}
	
	
	private class TreemapInverseComparator implements Comparator<TreemapPackageRepresentation> {
		public int compare(TreemapPackageRepresentation o1, TreemapPackageRepresentation o2) {
			return -1 * o1.compareTo(o2);
		}
	}
}
