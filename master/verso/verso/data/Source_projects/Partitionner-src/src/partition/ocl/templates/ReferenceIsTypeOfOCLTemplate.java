package partition.ocl.templates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;

import partition.ocl.OCLMatch;
import partition.ocl.OCLTemplate;
import utils.Utils;

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


public class ReferenceIsTypeOfOCLTemplate extends OCLTemplate {
	public final static Logger LOGGER = Logger.getLogger(ReferenceIsTypeOfOCLTemplate.class.getName());

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
				EClass clb = (EClass)eo;
				for (EReference asRef : clb.getEAllReferences()) {
					if(ACCEPT_MANY || (!ACCEPT_MANY && !asRef.isMany())){
						EClass cla = asRef.getEReferenceType();
						HashSet<EClass> listSubEc = Utils.getInheritage().get(cla);
						if(listSubEc != null ) // At minimum there is itself in the list.
							for (EClass subcla  : listSubEc) {
								if(!subcla.equals(clb)){
									OCLMatch oclinv = buildOCLMatch(cla, clb, subcla, asRef);
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


//TODO check classes involved : clA or not ?
	private OCLMatch buildOCLMatch(EClass clA, EClass clB, EClass subARef, EReference asRef) {
		String classA = clA.getName();
		String classB = clB.getName();
		String subclassA = subARef.getName();
		String as = asRef.getName();
		
		String expr =  "self."+as+".oclIsTypeOf("+subclassA+")";
		if(asRef.isMany())
			expr = "self."+as+"->forAll(e | e.oclIsTypeOf("+subclassA+"))";
		OCLMatch res = new OCLMatch("context "+classB+" inv ReferenceIsTypeOf: ", expr, clB);
		res.setClassesInvolved(new EClass[] {subARef, clA});
		res.setFeaturesInvolved(asRef);
		return res;
	}
	
	@Override
	public String toString() {
		return "ReferenceIsTypeOfOCLTemplate";
	}
}
