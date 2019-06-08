package partition.ocl.templates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;

import partition.ocl.OCLMatch;
import partition.ocl.OCLTemplate;
/**
 * From Cadavid Thesis :
 * + MOF Structure: Figure A.13 shows the required MOF structure. Occurs when a class contains a collection typed with itself. The OCL expression states that aninstance of this class also makes part of this contained collection.
 * + OCL Expression Template:
 *		context ClassA inv CollectionIncludesSelf : attrA->includes( self )
 * + Parameters: ClassA, AttrA
 */

public class CollectionIncludeSelfOCLTemplate extends OCLTemplate {
	public final static Logger LOGGER = Logger.getLogger(CollectionIncludeSelfOCLTemplate.class.getName());

	
	
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
				for (EReference collectionOne : ((EClass)eo).getEAllReferences()) {
					if(collectionOne.isMany()){
							OCLMatch oclinv = buildOCLInv(((EClass)eo), collectionOne);
							matches.add(oclinv);
							LOGGER.fine("  * Found OCLInv : "+oclinv);
					}
				}
			}
		}
		LOGGER.config("- "+matches.size()+" matches.");
		return matches;
	}



	private OCLMatch buildOCLInv(EClass clA,  EReference collectionOneRef) {
		String classA = clA.getName();
		String collectionOne = collectionOneRef.getName();
		
		String expr =  collectionOne+"->includes(self)";
		
		OCLMatch res = new OCLMatch("context "+classA+" inv CollectionIncludeSelf: ", expr, clA);
		res.setFeaturesInvolved(collectionOneRef);
		return res;
	}
	
	@Override
	public String toString() {
		return "CollectionIncludeSelfOCLTemplate";
	}
}
