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
 * + MOF Structure: Figure A.14 shows the required MOF structure. Occurs when a class contains a reference typed with itself. The OCL expression states that the value of this reference cannot be the same instance object that contains it.
 * + OCL Expression Template:
 * 		context ClassA inv ReferenceDifferentFromSelf : attrA <> self
 * + Parameters: ClassA, AttrA
 * 
 * If the reference is many (upperBounds > 1)
 *  + OCL Expression Template:
 * 		context ClassA inv ReferenceDifferentFromSelf : attrA->forAll(e| e <> self)
 */


public class ReferenceUnequalOCLTemplate extends OCLTemplate {
	public final static Logger LOGGER = Logger.getLogger(ReferenceUnequalOCLTemplate.class.getName());


	
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
				for (EReference bs1 : ec.getEAllReferences()) {
					if(!bs1.isMany()){
						for (EReference bs2 : ((EClass)eo).getEAllReferences()) {
							if(bs1 != bs2 
									&& bs1.getEReferenceType().equals(bs2.getEReferenceType()) 
									&& !bs2.isMany()){
								OCLMatch oclinv = buildOCLMatch(((EClass)eo), bs1, bs2);
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



	private OCLMatch buildOCLMatch(EClass clA,  EReference bs1Ref,  EReference bs2Ref) {
		String classA = clA.getName();
		String bs1 = bs1Ref.getName();
		String bs2 = bs2Ref.getName();
		
		String expr =  bs1+" <> "+bs2;
		OCLMatch res = new OCLMatch("context "+classA+" inv ReferenceUnequal: ", expr, clA);
		res.setFeaturesInvolved(new EStructuralFeature[] {bs1Ref, bs2Ref});
		return res;
	}
	
	@Override
	public String toString() {
		return "ReferenceUnequalOCLTemplate";
	}
}
