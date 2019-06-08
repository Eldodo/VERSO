package partition.ocl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import partition.Partition;
import partition.Range;
import partition.composition.FragmentSet;
import partition.composition.ModelFragment;
import partition.composition.ObjectFragment;
import partition.composition.PropertyConstraint;
import partition.ocl.templates.AcyclicReferenceOCLTemplate;
import partition.ocl.templates.AttributeEnumValueOCLTemplate;
import partition.ocl.templates.AttributeUndefinedOCLTemplate;
import partition.ocl.templates.AttributeValueGreaterThanZeroOCLTemplate;
import partition.ocl.templates.AutocontainerManyToManyOCLTemplate;
import partition.ocl.templates.AutocontainerOneToManyOCLTemplate;
import partition.ocl.templates.BooleanPropertyOCLTemplate;
import partition.ocl.templates.CollectionIncludeSelfOCLTemplate;
import partition.ocl.templates.CollectionIsSubsetOCLTemplate;
import partition.ocl.templates.CollectionSizeEqualsOneOCLTemplate;
import partition.ocl.templates.CollectionsSameSizeOCLTemplate;
import partition.ocl.templates.OppositeRefOneToManyOCLTemplate;
import partition.ocl.templates.OppositeRefOneToOneOCLTemplate;
import partition.ocl.templates.ReferenceDifferentFromSelfOCLTemplate;
import partition.ocl.templates.ReferenceUnequalOCLTemplate;
import partition.ocl.templates.SelfIsSubTypeOCLTemplate;
import partition.ocl.templates.TwoNumbersComparisonOCLTemplate;
import partition.ocl.templates.UniqueInstanceOCLTemplate;
import utils.Utils;
//import org.eclipse.ocl.ParserException;
//import org.eclipse.ocl.ecore.Constraint;
//import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
//import org.eclipse.ocl.ecore.OCL;
//import org.eclipse.ocl.helper.OCLHelper;
/**
 * @deprecated
 * @author batotedo
 *
 */
public class OCLEvaluator extends FragmentSet {
	public final static Logger LOGGER = Logger.getLogger(OCLEvaluator.class.getName());
	
	ArrayList<OCLTemplate> templates;
//	OCL ocl;
//	OCLHelper<EClassifier, EOperation, EStructuralFeature, Constraint> helper;
	
	
	OCLPartitionModel partitionModel;
	
	
	public OCLEvaluator() {		
		LOGGER.info("");
		templates = new ArrayList<>();
		templates.add(new AcyclicReferenceOCLTemplate());
		templates.add(new AttributeEnumValueOCLTemplate());
		templates.add(new AttributeUndefinedOCLTemplate());
		templates.add(new AttributeValueGreaterThanZeroOCLTemplate());
		templates.add(new AutocontainerOneToManyOCLTemplate());
		templates.add(new AutocontainerManyToManyOCLTemplate());
		templates.add(new BooleanPropertyOCLTemplate());
		templates.add(new CollectionIncludeSelfOCLTemplate());
		templates.add(new CollectionIsSubsetOCLTemplate());
		templates.add(new CollectionsSameSizeOCLTemplate());
		templates.add(new CollectionSizeEqualsOneOCLTemplate());
		templates.add(new OppositeRefOneToManyOCLTemplate());
		templates.add(new OppositeRefOneToOneOCLTemplate());
		templates.add(new ReferenceDifferentFromSelfOCLTemplate());
		//templates.add(new ReferenceIsTypeOfOCLTemplate());//Matching explosion
		templates.add(new ReferenceUnequalOCLTemplate());
		templates.add(new SelfIsSubTypeOCLTemplate());
		templates.add(new TwoNumbersComparisonOCLTemplate());
		templates.add(new UniqueInstanceOCLTemplate());
	
		
		partitionModel = new OCLPartitionModel();
		
//		System.out.println("OCLEvaluator.OCLEvaluator()");
		for (OCLTemplate oclTemplate : templates) {
			ArrayList<OCLMatch> list = oclTemplate.instantiate(Utils.metamodelResource);
			for (OCLMatch oclMatch : list) 
				partitionModel.addPartitions(oclMatch.getPropertyPartitions());
			
		}
		LOGGER.config(partitionModel.prettyPrint());;
		
		//AllPartitionStyle
		for (Partition p : partitionModel.getPartitions()) {
			ModelFragment mf = new ModelFragment(this);
			ObjectFragment of = new ObjectFragment(mf);
			for (Range r : p.getRanges()) {
				PropertyConstraint pc = new PropertyConstraint(r.getPartition().getClassName(), r.getPartition().getFeatureName(), of, r);
				of.addPropertyConstraint(pc);
			}
			mf.addObjectFragment(of);
			addFragment(mf);
		}
		
		
//		ocl = OCL.newInstance(EcoreEnvironmentFactory.INSTANCE);
//		helper = ocl.createOCLHelper();
	}

//	@Override
//	public FitnessVector evaluateCoverage(Entity entity) {
//		return evaluateCoverage((ModelSet)entity);
//	}
//	
//	public FitnessVector evaluateCoverage(ModelSet ms) {
//		LOGGER.config(ms.prettyPrint());
//		
//		
//		for (OCLTemplate oclTemplate : templates) { //FOREACH TEMPLATE
//			
//			for (Model m : ms.getModels()) {
//				
//				Resource r = m.getResource();
//				HashMap<EClass, HashSet<EObject>> resourceMap = new HashMap<>();
//				TreeIterator<EObject> eAllContents = r.getAllContents();
//				
//				while (eAllContents.hasNext()) {//We construct the map of concerned classes
//					EObject eo = eAllContents.next();
//					
//					for (EClass ecInherit : Utils.getInheritage().get(eo.eClass())) {
//						if(!resourceMap.containsKey(ecInherit))
//							resourceMap.put(ecInherit, new HashSet<EObject>());
//						resourceMap.get(ecInherit).add(eo);
//					}
//				}
//				
//				LOGGER.finer(printResourceMap(resourceMap));
//				
//				boolean raper = false;
//				
//				for (OCLMatch inv : oclTemplate.getMatches()) {
//					helper.setContext(inv.getContext());
//					try {
//						Constraint cst = helper.createInvariant(inv.getOCLStringExpression());
////						System.out.println(inv);
//						int i = 0;
//						int sum = 0;
//						ArrayList<EObject> listRaped = new ArrayList<>();
//						
//						HashSet<EObject> listEO = resourceMap.get(inv.getContext());
//						if(listEO != null)
//							for (EObject eo : listEO) {
//								boolean b = ocl.check(eo, cst);
////								System.out.println(b+ " : "+Utils.printEObject(eo) );
//								if(!b) {
//									listRaped.add(eo);
//									i++;
//								}
//								sum++;
//							}
//						
//						boolean constraintRaped = i != 0;
//						raper |= constraintRaped;
//						LOGGER.fine("Check "+m+" : "+(constraintRaped?"Violated : ":"Ok :")+i+"/"+sum+" objects not right : check("+inv.getHeader()+":"+inv.getOCLStringExpression()+") : "+(sum-i)+" false");
//					} catch (ParserException e) {
//						LOGGER.severe("OCL expression incorrect : '"+inv.getOCLStringExpression()+"'");
//						e.printStackTrace();
//					}
//				}
//				
//				LOGGER.config("  "+m+" : "+(raper?"!":"")+"Ok");
//			}
//		}
//		return null;
//	}

	/**
	 * @param resourceMap
	 */
	private String printResourceMap(HashMap<EClass, HashSet<EObject>> resourceMap) {
		String res = "ResourceMap :";
		for (EClass ec : resourceMap.keySet()) {
			HashSet<EObject> list = resourceMap.get(ec);
			res += "\n - ECLass : "+ec.getName();
			for (EObject eo : list) {
				res += "\n    + "+Utils.printEObject(eo);
			}
		}
		return res;
	}
}
