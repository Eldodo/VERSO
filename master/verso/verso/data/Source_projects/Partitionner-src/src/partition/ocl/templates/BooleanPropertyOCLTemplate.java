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
 * + MOF Structure: Figure A.2 shows the required MOF structure. One class and two relationships are required; one pointing to the contained elements and the other to the container element; the associated WFR in this case specifies that these relationships are always opposite.
 * + OCL Expression Template: 
 *	context AutoContainerEClass inv AutocontainerOneToMany : self.contents->forAll ( a : AutoContainerEClass | a.containedIn = self )
 * + Parameters: AutoContainerEClass, Contents, ContainedIn
 */


public class BooleanPropertyOCLTemplate extends OCLTemplate {
	public final static Logger LOGGER = Logger.getLogger(BooleanPropertyOCLTemplate.class.getName());
	private static  boolean ACCEPT_MANY = false;

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
						if(Utils.isEAttributeABoolean(ea)){
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


	private OCLMatch buildOCLMatch(EClass context, EAttribute eaRef) {
		String autoContainerEClass = context.getName();
		String attra = eaRef.getName();
		String expr =  "self."+attra;
		OCLMatch res = new OCLMatch("context "+autoContainerEClass+" inv BooleanProperty: ", expr, context);
		res.setFeaturesInvolved(eaRef);
		return res;
	}

	
	@Override
	public String toString() {
		return "BooleanPropertyOCLTemplate";
	}
}
