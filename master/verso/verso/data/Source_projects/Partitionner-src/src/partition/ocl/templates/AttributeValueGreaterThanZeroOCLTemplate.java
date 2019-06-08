package partition.ocl.templates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import partition.ocl.OCLMatch;
import partition.ocl.OCLTemplate;
import utils.Utils;
/**
 * From Cadavid Thesis :
 * + MOF Structure: Figure A.11 shows the required MOF structure. Occurs for every attribute in the domain structure of a numeric type, either integer or real. The OCL expression specifies that its value must be higher than 0.
 * + OCL Expression Template:
 *	context ClassA inv AttributeValueGreaterThanZero : self.attrA > 0
 * + Parameters: ClassA, AttrA
 * 
 * In the case of a multiple attribute, it's a self.attrA->forAll(e | e >= 0).
 */
public class AttributeValueGreaterThanZeroOCLTemplate extends OCLTemplate {
	public final static Logger LOGGER = Logger.getLogger(AttributeValueGreaterThanZeroOCLTemplate.class.getName());
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
				for (EAttribute ea : ((EClass)eo).getEAllAttributes()) {
					if(ACCEPT_MANY || (!ACCEPT_MANY && !ea.isMany())){
						if(Utils.isEAttributeANumber(ea)){
							OCLMatch oclinv = buildOCLMatch(((EClass)eo), ea);
							matches.add(oclinv);
							LOGGER.fine("  * Found OCLInv : "+oclinv);
						}
					}
				}
			}
		}
		LOGGER.config("- "+matches.size()+" matches.");
		return matches;
	}


	private OCLMatch buildOCLMatch(EClass clA, EAttribute ea) {
		String classA = clA.getName();
		String eAttribute = ea.getName();
		String E = ea.getEAttributeType().getEPackage().getName()+"::"+ea.getEAttributeType().getName();
		String expr = "";
		if(ea.isMany())
			expr = "(self."+eAttribute+"->forall(temp1 : "+E+" | temp1 >= 0))";
		else
			expr = "(self."+eAttribute+" >= 0)";
		OCLMatch res = new OCLMatch("context "+classA+" inv AttributeValueGreaterThanZero: ",expr, clA);
		res.setFeaturesInvolved(ea);
		return res;
	}
	
	@Override
	public String toString() {
		return "AttributeValueGreaterThanZeroOCLTemplate";
	}
}
