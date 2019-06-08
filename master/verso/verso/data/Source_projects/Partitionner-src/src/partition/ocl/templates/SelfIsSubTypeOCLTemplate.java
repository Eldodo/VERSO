package partition.ocl.templates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import partition.ocl.OCLMatch;
import partition.ocl.OCLTemplate;
import utils.Utils;

/**
 * From Cadavid Thesis :
 * + MOF Structure: Figure A.18 shows the required MOF structure. Occurs when a class has a subclass. The OCL expression states that the concrete data type of instances of this class must be the subclass.
 * + OCL Expression Template:
 * 		context ClassA inv SelfIsSubtype : self.oclIsTypeOf( SubclassA )
 * + Parameters: ClassA, SubclassA
 */


public class SelfIsSubTypeOCLTemplate extends OCLTemplate {
	public final static Logger LOGGER = Logger.getLogger(SelfIsSubTypeOCLTemplate.class.getName());

	
	
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
				HashSet<EClass> listSubEc = Utils.getInheritage().get(ec);
				if(listSubEc != null ) 
					for (EClass subEc  : listSubEc) {
						if(!subEc.equals(ec)){//Excludes self
							OCLMatch oclinv = buildOCLMatch(ec, subEc);
							matches.add(oclinv);
							LOGGER.fine("  * Found OCLInv : "+oclinv);
						}
					}
			}
		}
		LOGGER.config("- "+matches.size()+" matches.");
		return matches;
	}



	private OCLMatch buildOCLMatch(EClass clA,  EClass subClARef) {
		String classA = clA.getName();
		String subclassA = subClARef.getName();
		
		String expr =  "self.oclIsTypeOf("+subclassA+")";
		
		OCLMatch res = new OCLMatch("context "+classA+" inv SelfIsSubType: ", expr, clA);
		res.setClassesInvolved(subClARef);
		return res;
	}
	
	@Override
	public String toString() {
		return "SelfIsSubTypeOCLTemplate";
	}
}
