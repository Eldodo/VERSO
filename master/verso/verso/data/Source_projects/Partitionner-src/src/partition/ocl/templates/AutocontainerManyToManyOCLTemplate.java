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
 * + MOF Structure: Figure A.2 shows the required MOF structure. One class and two relationships are required; one pointing to the contained elements and the other to the container element; the associated WFR in this case specifies that these relationships are always opposite.
 * + OCL Expression Template: 
 *	context AutoContainerEClass inv AutocontainerOneToMany : self.contents->forAll ( a : AutoContainerEClass | a.containedIn = self )
 * + Parameters: AutoContainerEClass, Contents, ContainedIn
 */


public class AutocontainerManyToManyOCLTemplate extends OCLTemplate {
	public final static Logger LOGGER = Logger.getLogger(AutocontainerManyToManyOCLTemplate.class.getName());

	
	
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
				for (EReference erefContainedIn : ((EClass)eo).getEAllReferences()) {
					
					if(erefContainedIn.isMany()){
						
						for (EReference erefContents : ((EClass)eo).getEAllReferences()) {
							if(erefContents.isMany() && ((EClass)eo) == erefContents){
								
								OCLMatch oclinv = buildOCLMatch(((EClass)eo), erefContainedIn, erefContents);
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


	private OCLMatch buildOCLMatch(EClass context, EReference erContainedIn, EReference erContents) {
		String autoContainerEClass = context.getName();
		String containedIn = erContainedIn.getName();
		String contents = erContents.getName();
		String contentsEClass = erContents.getEReferenceType().getName();
		
		String expr =  "self."+contents+"->forAll(a : "+contentsEClass+"| "
				+ "a."+containedIn+"->includes(self))";
		OCLMatch res = new OCLMatch("context "+autoContainerEClass+" inv AutocontainerManyToMany: ", expr, context);
		res.setFeaturesInvolved(new EStructuralFeature[] {erContainedIn, erContents});
		return res;
	}
	
	@Override
	public String toString() {
		return "AutocontainerManyToManyOCLTemplate";
	}
}
