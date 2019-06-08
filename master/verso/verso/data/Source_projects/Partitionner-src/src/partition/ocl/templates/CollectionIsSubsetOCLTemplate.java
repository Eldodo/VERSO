package partition.ocl.templates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
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


public class CollectionIsSubsetOCLTemplate extends OCLTemplate {
	public final static Logger LOGGER = Logger.getLogger(CollectionIsSubsetOCLTemplate.class.getName());
	private static boolean ACCEPT_SINGLE = false;
	
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
				for (EReference collectionOne : ((EClass)eo).getEReferences()) {
					if(collectionOne.isMany()){
						for (EReference collectionTwo : ((EClass)eo).getEReferences()) {
							if(ACCEPT_SINGLE || (!ACCEPT_SINGLE && collectionTwo.isMany())){
								if(collectionOne != collectionTwo && collectionOne.getEReferenceType().equals(collectionTwo.getEReferenceType())){
									OCLMatch oclinv = buildOCLInv(((EClass)eo), collectionOne.getEReferenceType(), collectionOne, collectionTwo);
									matches.add(oclinv);
									LOGGER.fine("  * Found OCLInv : "+oclinv);
								}
							}
						}
					}
				}
			}
		}
		LOGGER.config("- "+matches.size()+" matches.");
		return matches;
	}



	private OCLMatch buildOCLInv(EClass clA, EClass clB, EReference collectionOneRef, EReference collectionTwoRef) {
		String classA = clA.getName();
		String collectionOne = collectionOneRef.getName();
		String collectionTwo = collectionTwoRef.getName();
		
		String expr =  collectionOne+"->includes("+collectionTwo+")";
		if(collectionTwoRef.isMany())
			expr =  collectionOne+"->includesAll("+collectionTwo+")";
		
		OCLMatch res = new OCLMatch("context "+classA+" inv CollectionIsSubset: ", expr, clA);
		res.setClassesInvolved(clB);
		res.setFeaturesInvolved(new EStructuralFeature[] {collectionOneRef, collectionTwoRef});
		return res;
	}
	
	@Override
	public String toString() {
		return "CollectionIsSubsetOCLTemplate";
	}
}
