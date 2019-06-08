package partition.ocl.templates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import partition.ocl.OCLMatch;
import partition.ocl.OCLTemplate;
/**
 * From Cadavid Thesis :
 * + MOF Structure: Figure A.6 shows the required MOF structure. It appears in domain structures containing an attribute typed with an enumeration, and it is matched for each one of its literals. The OCL expression for every match specifies that the value of the attribute is this match’s literal.
 * + OCL Expression Template:
 * context A inv AttributeEnumValue : enumAttribute = E :: EnumLit
 * + Parameters: A, EnumAttribute, E, EnumLit
*/


public class AttributeEnumValueOCLTemplate extends OCLTemplate {
	public final static Logger LOGGER = Logger.getLogger(AttributeEnumValueOCLTemplate.class.getName());
	
	private static boolean ACCEPT_MANY = false;
	
	@Override
	public ArrayList<OCLMatch> instantiate(Resource metamodelResource) {
		URI fileURI = metamodelResource.getURI();
		if(!metamodelResource.isLoaded()){
			try {
				metamodelResource.load(null);
				LOGGER.config("Loading metamodel : "+fileURI+"... Loaded.");
			} catch (IOException e) {
				LOGGER.severe(" !! Loading metamodel : "+fileURI+"... Failure ! Unable to load file.  !!");
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		matches = new ArrayList<>();
		TreeIterator<EObject> eAllContents = metamodelResource.getAllContents();
		while (eAllContents.hasNext()) {
			EObject eo = eAllContents.next();
			if(eo instanceof EClass){
				for (EAttribute enumAttribute : ((EClass)eo).getEAllAttributes()) {
					if(ACCEPT_MANY || (!ACCEPT_MANY && !enumAttribute.isMany())){
						if(enumAttribute.getEAttributeType() instanceof EEnum){
							for (EEnumLiteral eel : ((EEnum)enumAttribute.getEAttributeType()).getELiterals()) {
								OCLMatch oclinv = buildOCLMatch(((EClass)eo), enumAttribute, eel);
								matches.add(oclinv);
							LOGGER.fine("  * Found OCLInv : "+oclinv);
							}
						}
					}
				}
			}
		}
		LOGGER.config("- "+matches.size()+" matches.");
		return matches;
	}



	private OCLMatch buildOCLMatch(EClass clA, EAttribute ea, EEnumLiteral eel) {
		String classA = clA.getName();
		String enumAttribute = ea.getName();
		String E = ea.getEAttributeType().getEPackage().getName()+"::"+ea.getEAttributeType().getName();
		String enumLit = eel.getLiteral();
		String expr = "";
		if(ea.isMany())
			expr = "(self."+enumAttribute+"->exists(temp1 : "+E+" | temp1 = "+E+"::"+enumLit+"))";
		else
			expr = "(self."+enumAttribute+" = "+E+"::"+enumLit+")";
		OCLMatch res = new OCLMatch("context "+classA+" inv AttributeEnumValue: ",expr, clA);
		res.setFeaturesInvolved(ea);//TODO what about EnumLiteral ?
		return res;
	}
	
	@Override
	public String toString() {
		return "AttributeEnumValueOCLTemplate";
	}
}
