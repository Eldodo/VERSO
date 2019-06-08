package verso.representation.cubeLandscape.Layout;

import verso.representation.Layout;
import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.MethodRepresentation;

public class MethodLayout extends Layout {

	public static void placeMethods(ElementRepresentation classe)
	{
		int i =0;
		int j =0;
		int numberOnRow = (int)(Math.ceil(Math.sqrt(classe.getMethods().size()/2.0)));
		for (MethodRepresentation ller : classe.getMethods())
		{
			ller.setPosX(i);
			ller.setPosZ(j);
			i++;
			if (i >= numberOnRow)
			{
				i =0;
				j++;
			}
				
			
		}
	}
	
}
