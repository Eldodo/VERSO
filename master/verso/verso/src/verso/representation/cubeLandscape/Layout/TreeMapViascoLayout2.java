package verso.representation.cubeLandscape.Layout;

import java.util.ArrayList;
import java.util.List;

import verso.model.Package;
import verso.representation.Layout;
import verso.representation.cubeLandscape.representationModel.TreemapPackageRepresentation;
/**
 * Layout used to arranche packages before sending them to the Treemap layout.
 * Rearrange the the package representation so it has an inclusion hierarchy.
 * @author Guillaume
 *
 */
public class TreeMapViascoLayout2 extends Layout
{
	public TreemapPackageRepresentation layout(TreemapPackageRepresentation pRoot)
	{
		List<TreemapPackageRepresentation> table = new ArrayList<TreemapPackageRepresentation>(pRoot.getTreemapPackages());
		String otherPackageName = "";
		for (TreemapPackageRepresentation pr : pRoot.getTreemapPackages())
		{
			pr.getLinkCount().clear();
		}
		
		//compute the first part with (all outgoing links)
		for (TreemapPackageRepresentation pr : pRoot.getTreemapPackages())
		{
			for(String s : pr.getPackage().getAllTargets())
			{
				if (s.contains("."))
				{
					otherPackageName = s.substring(0, s.indexOf("."));
					if (otherPackageName.compareTo(pr.getPackage().getName())!=0)
					{
						if (pr.getLinkCount().containsKey(otherPackageName))
						{
							pr.getLinkCount().put(otherPackageName,pr.getLinkCount().get(otherPackageName)+1);
						}
						else
						{
							pr.getLinkCount().put(otherPackageName, new Integer(1));
						}
					}
				}
			}
			
		}
		//All links are computed at the first level
		while (table.size() > 1)
		{
			System.out.println(table.size());
			TreemapPackageRepresentation max1 = table.get(0);
			TreemapPackageRepresentation max2 = table.get(1);
			double maxLinks = 0.0;
			double numberOfLinks =0.0;
			for (TreemapPackageRepresentation pr1 : table)
			{
				for (TreemapPackageRepresentation pr2 : table)
				{
					if (pr1.equals(pr2)) continue;
					int linkstot = 0;
					if(pr1.getLinkCount().get(pr2.getPackage().getName())!= null)
						 linkstot += pr1.getLinkCount().get(pr2.getPackage().getName());
					if (pr2.getLinkCount().get(pr1.getPackage().getName()) != null)
						linkstot += pr2.getLinkCount().get(pr1.getPackage().getName());
					
					numberOfLinks = linkstot/(double)(pr1.countDescendantClasses()+ pr2.countDescendantClasses());
					if (numberOfLinks >= maxLinks)
					{
						maxLinks = numberOfLinks;
						max1 = pr1;
						max2 = pr2;
					}
				}
			}
			//Create new package and compute its outgoing links
			TreemapPackageRepresentation newPac = new TreemapPackageRepresentation(new Package("pac" + table.size()));
			newPac.setFake();
			newPac.setLinkCount(max1.getLinkCount());
			for (String s : max2.getLinkCount().keySet())
			{
				if (s.compareTo(max1.getPackage().getName())!= 0)
				{
					if (newPac.getLinkCount().containsKey(s))
					{
						newPac.getLinkCount().put(s, newPac.getLinkCount().get(s) + max2.getLinkCount().get(s));
					}
				}
			}
			table.remove(max1);
			table.remove(max2);
			//Adjust all the other package count
			for (TreemapPackageRepresentation pacRep : table)
			{
				int total = 0;
				if (pacRep.getLinkCount().get(max1.getPackage().getName())!= null)
				{
					total += pacRep.getLinkCount().get(max1.getPackage().getName());
					
				}
				if (pacRep.getLinkCount().get(max2.getPackage().getName())!=null)
					total += pacRep.getLinkCount().remove(max2.getPackage().getName());
				pacRep.getLinkCount().put(newPac.getPackage().getName(), new Integer(total));
			}
			newPac.addPackage(max1);
			newPac.addPackage(max2);
			table.add(newPac);
			
		}
		return table.iterator().next();
	}
}


