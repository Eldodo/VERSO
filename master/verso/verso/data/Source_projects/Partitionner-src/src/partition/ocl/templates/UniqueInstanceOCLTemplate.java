package partition.ocl.templates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import partition.ocl.OCLMatch;
import partition.ocl.OCLTemplate;

/**
 * From Cadavid Thesis :
 * + MOF Structure: Figure A.1 shows the required MOF structure. The pattern applies whenever there is a class containing a reference which type is itself. Also, the upper bound of this reference has to be “many”; this is because the OCL expression invokes on this attribute the operation closure, which can only be invoked on collections.
 * + OCL Expression Template:
 * context ClassA inv AcyclicReference : attributeA ->closure(iterator: ClassA | iterator.attributeA )->excludes (self) ;
 * + Parameters: ClassA, AttributeA
 * @author batotedo
 *
 */
public class UniqueInstanceOCLTemplate extends OCLTemplate {
	public final static Logger LOGGER = Logger.getLogger(UniqueInstanceOCLTemplate.class.getName());

	
	
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
				
				OCLMatch oclmatch = buildOCLMatch(((EClass)eo));
				matches.add(oclmatch);
				LOGGER.fine("  * Found OCLMatch : "+oclmatch);
						
			}
		}
		LOGGER.config("- "+matches.size()+" matches.");
		return matches;
	}



	private OCLMatch buildOCLMatch(EClass context) {
		String classA = context.getName();
		
		String expr = classA+".allInstances()->size() = 1";
		OCLMatch res = new OCLMatch("context "+classA+" inv UniqueInstance: ", expr, context);
		return res;
	}
	
	@Override
	public String toString() {
		return "UniqueInstanceOCLTemplate";
	}
}
