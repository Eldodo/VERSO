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
 * + MOF Structure: Figure A.9 shows the required MOF structure. It occurs when two classes are present in the domain structure, so that both contain references typed with the other. The OCL expression states that the values of these references are opposite.
 * + OCL Expression Template:
 *		context ClassA inv OppositeReferencesOneToOne : self.bs.as = self
 * + Parameters: ClassA, ClassB, As, Bs
*/

public class OppositeRefOneToManyOCLTemplate extends OCLTemplate {
	public final static Logger LOGGER = Logger.getLogger(OppositeRefOneToManyOCLTemplate.class.getName());

	
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
			EObject eoa = eAllContents.next();
			if(eoa instanceof EClass){
				for (EReference bs : ((EClass)eoa).getEAllReferences()) {
					if(bs.isMany()){
						EClass eob = (EClass)bs.getEReferenceType();
						for (EReference as : eob.getEAllReferences()) {
							if(!as.isMany()){
								OCLMatch oclmatch = buildOCLMatch(((EClass)eoa), eob, bs, as);
								matches.add(oclmatch);
								LOGGER.fine("  * Found OCLInv : "+oclmatch);
							}
						}
					}
				}
			}
		}
		LOGGER.config("- "+matches.size()+" matches.");
		return matches;
	}



	private OCLMatch buildOCLMatch(EClass clA, EClass clB,  EStructuralFeature bsRef, EStructuralFeature asRef) {
		String classA = clA.getName();
		String classB = clB.getName();
		String bs = bsRef.getName();
		String as = asRef.getName();
		
		String expr = "self."+bs+"->forAll(b : "+classB+" | b."+as+" = self)";
		
		OCLMatch res = new OCLMatch("context "+classA+" inv OppositeRefOneToMany: ", expr, clA);
		res.setClassesInvolved(clB);
		res.setFeaturesInvolved(new EStructuralFeature[] {bsRef, asRef});
		return res;
	}
	
	@Override
	public String toString() {
		return "OppositeRefOneToManyOCLTemplate";
	}
}
