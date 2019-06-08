package partition.ocl.templates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;

import partition.ocl.OCLMatch;
import partition.ocl.OCLTemplate;
/**
 * From Cadavid Thesis :
 * + MOF Structure: Figure A.5 shows the required MOF structure. It occurs when a class in a domain structure contains two equally typed collections. The OCL expression states that one of the collections is a subset from the other.
 * + OCL Expression Template:
 * context ClassA inv CollectionIsSubset :collectionOne->includesAll( collectionTwo )
 * + Parameters: ClassA, ClassB, CollectionOne, CollectionTwo */


public class CollectionSizeEqualsOneOCLTemplate extends OCLTemplate {
	public final static Logger LOGGER = Logger.getLogger(CollectionSizeEqualsOneOCLTemplate.class.getName());

	
	
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
				for (EStructuralFeature collectionOne : ((EClass)eo).getEAllStructuralFeatures()) {
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



	private OCLMatch buildOCLInv(EClass clA,  EStructuralFeature collectionOneRef) {
		String classA = clA.getName();
		String collectionOne = collectionOneRef.getName();
		
		String expr = collectionOne+"->size() = 1";
		OCLMatch res = new OCLMatch("context "+classA+" inv CollectionSizeEqualsOne: ", expr, clA);
		res.setFeaturesInvolved(new EStructuralFeature[] {collectionOneRef});
		return res;
	}
	
	@Override
	public String toString() {
		return "CollectionSizeEqualsOneOCLTemplate";
	}
}
