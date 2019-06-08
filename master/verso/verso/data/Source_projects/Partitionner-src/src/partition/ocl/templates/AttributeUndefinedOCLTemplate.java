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
/**
 * From Cadavid Thesis :
 * + MOF Structure: Figure A.12 shows the required MOF structure. Occurs for every attribute in the domain structure, regardless of its type. The OCL expression states that this attribute’s value is undefined.
 * + OCL Expression Template:
 *		context ClassA inv AttributeUndefined : self.attrA.oclIsUndefined ( )
 * + Parameters: ClassA, AttrA
 */
public class AttributeUndefinedOCLTemplate extends OCLTemplate {
	public final static Logger LOGGER = Logger.getLogger(AttributeUndefinedOCLTemplate.class.getName());
	
	
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
					OCLMatch oclinv = buildOCLMatch(((EClass)eo), ea);
					matches.add(oclinv);
					LOGGER.fine("  * Found OCLInv : "+oclinv);
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
		String expr = "self."+eAttribute+".oclIsUndefined()";
		OCLMatch res = new OCLMatch("context "+classA+" inv AttributeUndefined: ",expr, clA);
		res.setFeaturesInvolved(ea);
		return res;
	}
	
	@Override
	public String toString() {
		return "AttributeUndefinedOCLTemplate";
	}
}
