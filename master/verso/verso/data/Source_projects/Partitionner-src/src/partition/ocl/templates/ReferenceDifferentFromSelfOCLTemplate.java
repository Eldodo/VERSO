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
 * + MOF Structure: Figure A.14 shows the required MOF structure. Occurs when a class contains a reference typed with itself. The OCL expression states that the value of this reference cannot be the same instance object that contains it.
 * + OCL Expression Template:
 * 		context ClassA inv ReferenceDifferentFromSelf : attrA <> self
 * + Parameters: ClassA, AttrA
 * 
 * If the reference is many (upperBounds > 1)
 *  + OCL Expression Template:
 * 		context ClassA inv ReferenceDifferentFromSelf : attrA->forAll(e| e <> self)
 */


public class ReferenceDifferentFromSelfOCLTemplate extends OCLTemplate {
	public final static Logger LOGGER = Logger.getLogger(ReferenceDifferentFromSelfOCLTemplate.class.getName());

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
				EClass ec = (EClass)eo;
				for (EReference collectionOne : ec.getEAllReferences()) {
					if(ACCEPT_MANY || (!ACCEPT_MANY && !collectionOne.isMany())){
						if(collectionOne.getEReferenceType().equals(ec)){
							OCLMatch oclinv = buildOCLInv(ec, collectionOne);
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



	private OCLMatch buildOCLInv(EClass clA,  EReference collectionOneRef) {
		String classA = clA.getName();
		String collectionOne = collectionOneRef.getName();
		
		String expr =  collectionOne+" <> self";
		if(collectionOneRef.isMany())
			expr = collectionOne+"->forAll(e | e <> self)";
		OCLMatch res = new OCLMatch("context "+classA+" inv ReferenceDifferentFromSelf: ", expr, clA);
		res.setFeaturesInvolved(collectionOneRef);
		return res;
	}
	
	@Override
	public String toString() {
		return "ReferenceDifferentFromSelfOCLTemplate";
	}
}
