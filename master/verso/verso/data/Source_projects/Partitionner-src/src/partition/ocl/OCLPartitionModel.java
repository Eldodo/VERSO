package partition.ocl;

import java.util.ArrayList;
import java.util.logging.Level;

import partition.Partition;
import partition.PartitionModel;
import partition.PropertyPartition;
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
import partition.ocl.templates.ReferenceIsTypeOfOCLTemplate;
import partition.ocl.templates.ReferenceUnequalOCLTemplate;
import partition.ocl.templates.SelfIsSubTypeOCLTemplate;
import partition.ocl.templates.TwoNumbersComparisonOCLTemplate;
import partition.ocl.templates.UniqueInstanceOCLTemplate;
import utils.Config;
import utils.Utils;




public class OCLPartitionModel extends PartitionModel {
	static boolean 	use_AcyclicReferenceOCLTemplate	=true,
			use_AttributeEnumValueOCLTemplate		=true,
			use_AttributeUndefinedOCLTemplate		=true,
			use_AttributeValueGreaterThanZeroOCLTemplate	=true,
			use_AutocontainerManyToManyOCLTemplate	=true,
			use_AutocontainerOneToManyOCLTemplate	=true,
			use_BooleanPropertyOCLTemplate			=true,
			use_CollectionIncludeSelfOCLTemplate	=true,
			use_CollectionIsSubsetOCLTemplate		=true,
			use_CollectionsSameSizeOCLTemplate		=true,
			use_CollectionSizeEqualsOneOCLTemplate	=true,
			use_OppositeRefOneToManyOCLTemplate		=true,
			use_OppositeRefOneToOneOCLTemplate		=true,
			use_ReferenceDifferentFromSelfOCLTemplate=true,
			use_ReferenceIsTypeOfOCLTemplate		=true,
			use_ReferenceUnequalOCLTemplate			=true,
			use_SelfIsSubTypeOCLTemplate			=true,
			use_TwoNumbersComparisonOCLTemplate		=true,
			use_UniqueInstanceOCLTemplate			=true;

	public static void loadConfig(){
		//Fourth load call;
		use_AcyclicReferenceOCLTemplate					= Config.getBooleanParam("AcyclicReferenceOCLTemplate");
		use_AttributeEnumValueOCLTemplate				= Config.getBooleanParam("AttributeEnumValueOCLTemplate");
		use_AttributeUndefinedOCLTemplate				= Config.getBooleanParam("AttributeUndefinedOCLTemplate");
		use_AttributeValueGreaterThanZeroOCLTemplate	= Config.getBooleanParam("AttributeValueGreaterThanZeroOCLTemplate");
		use_AutocontainerManyToManyOCLTemplate			= Config.getBooleanParam("AutocontainerManyToManyOCLTemplate");
		use_AutocontainerOneToManyOCLTemplate			= Config.getBooleanParam("AutocontainerOneToManyOCLTemplate");
		use_BooleanPropertyOCLTemplate					= Config.getBooleanParam("BooleanPropertyOCLTemplate");
		use_CollectionIncludeSelfOCLTemplate			= Config.getBooleanParam("CollectionIncludeSelfOCLTemplate");
		use_CollectionIsSubsetOCLTemplate				= Config.getBooleanParam("CollectionIsSubsetOCLTemplate");
		use_CollectionsSameSizeOCLTemplate				= Config.getBooleanParam("CollectionsSameSizeOCLTemplate");
		use_CollectionSizeEqualsOneOCLTemplate			= Config.getBooleanParam("CollectionSizeEqualsOneOCLTemplate");
		use_OppositeRefOneToManyOCLTemplate				= Config.getBooleanParam("OppositeRefOneToManyOCLTemplate");
		use_OppositeRefOneToOneOCLTemplate				= Config.getBooleanParam("OppositeRefOneToOneOCLTemplate");
		use_ReferenceDifferentFromSelfOCLTemplate 		= Config.getBooleanParam("ReferenceDifferentFromSelfOCLTemplate");
		use_ReferenceIsTypeOfOCLTemplate				= Config.getBooleanParam("ReferenceIsTypeOfOCLTemplate");
		use_ReferenceUnequalOCLTemplate					= Config.getBooleanParam("ReferenceUnequalOCLTemplate");
		use_SelfIsSubTypeOCLTemplate					= Config.getBooleanParam("SelfIsSubTypeOCLTemplate");
		use_TwoNumbersComparisonOCLTemplate				= Config.getBooleanParam("TwoNumbersComparisonOCLTemplate");
		use_UniqueInstanceOCLTemplate					= Config.getBooleanParam("UniqueInstanceOCLTemplate");
	}

	
	ArrayList<OCLTemplate> templates;
	public OCLPartitionModel() {
		super();
		templates = new ArrayList<>();
		if(use_AcyclicReferenceOCLTemplate)
			templates.add(new AcyclicReferenceOCLTemplate());
		if(use_AttributeEnumValueOCLTemplate)
			templates.add(new AttributeEnumValueOCLTemplate());
		if(use_AttributeUndefinedOCLTemplate)
			templates.add(new AttributeUndefinedOCLTemplate());
		if(use_AttributeValueGreaterThanZeroOCLTemplate)
			templates.add(new AttributeValueGreaterThanZeroOCLTemplate());
		if(use_AutocontainerOneToManyOCLTemplate)
			templates.add(new AutocontainerOneToManyOCLTemplate());
		if(use_AutocontainerManyToManyOCLTemplate)
			templates.add(new AutocontainerManyToManyOCLTemplate());
		if(use_BooleanPropertyOCLTemplate)
			templates.add(new BooleanPropertyOCLTemplate());
		if(use_CollectionIncludeSelfOCLTemplate)
			templates.add(new CollectionIncludeSelfOCLTemplate());
		if(use_CollectionIsSubsetOCLTemplate)
			templates.add(new CollectionIsSubsetOCLTemplate());
		if(use_CollectionsSameSizeOCLTemplate)
			templates.add(new CollectionsSameSizeOCLTemplate());
		if(use_CollectionSizeEqualsOneOCLTemplate)
			templates.add(new CollectionSizeEqualsOneOCLTemplate());
		if(use_OppositeRefOneToManyOCLTemplate)
			templates.add(new OppositeRefOneToManyOCLTemplate());
		if(use_OppositeRefOneToOneOCLTemplate)
			templates.add(new OppositeRefOneToOneOCLTemplate());
		if(use_ReferenceDifferentFromSelfOCLTemplate)
			templates.add(new ReferenceDifferentFromSelfOCLTemplate());
		if(use_ReferenceIsTypeOfOCLTemplate)
			templates.add(new ReferenceIsTypeOfOCLTemplate());//Matching explosion
		if(use_ReferenceUnequalOCLTemplate)
			templates.add(new ReferenceUnequalOCLTemplate());
		if(use_SelfIsSubTypeOCLTemplate)
			templates.add(new SelfIsSubTypeOCLTemplate());
		if(use_TwoNumbersComparisonOCLTemplate)
			templates.add(new TwoNumbersComparisonOCLTemplate());
		if(use_UniqueInstanceOCLTemplate)
			templates.add(new UniqueInstanceOCLTemplate());



		
//		System.out.println("OCLEvaluator.OCLEvaluator()");
//		for (OCLTemplate oclTemplate : templates) {
//			ArrayList<OCLMatch> list = oclTemplate.instantiate(Utils.metamodelResource);
//			for (OCLMatch oclMatch : list) 
//				addPartitions(oclMatch.getPropertyPartitions());
//			
//		}
		
//		//AllPartitionStyle
//		for (Partition p : getPartitions()) {
//			ModelFragment mf = new ModelFragment();
//			ObjectFragment of = new ObjectFragment(mf);
//			for (Range r : p.getRanges()) {
//				PropertyConstraint pc = new PropertyConstraint(r.getPartition().getClassName(), r.getPartition().getFeatureName(), of, r);
//				of.addPropertyConstraint(pc);
//			}
//			mf.addObjectFragment(of);
//			addFragment(mf);
//		}
		

	}

	/**
	 * Instantiates OCLTemplates declared in constructor
	 */
	public void extractPartition(){
		for (OCLTemplate oclTemplate : templates) {
			ArrayList<OCLMatch> list = oclTemplate.instantiate(Utils.metamodelResource);
			for (OCLMatch oclMatch : list) 
				addPartitions(oclMatch.getPropertyPartitions());
			
		}
//		TreeIterator<EObject> eAllContents = Utils.metamodelResource.getAllContents();
//		
//		while (eAllContents.hasNext()) {
//			EObject eo = eAllContents.next();
//			if(eo instanceof EClass) {
//				EClass eClass = (EClass) eo;
//				for (EStructuralFeature esf : eClass.getEStructuralFeatures()) {
//					PropertyPartition pp = new PropertyPartition(this, eClass, esf);
//					propertyPartitions.add(pp);
//					partitions.addAll(pp.getPartitions());
//				}
//			}
//		}
		if(LOGGER.isLoggable(Level.CONFIG))
			LOGGER.config(prettyPrint());
		else if (LOGGER.isLoggable(Level.INFO))
			LOGGER.info(""+partitions.size()+" partitions extracted.");
	}

	public void addPartitions(ArrayList<PropertyPartition> propertyPartitions) {
		for (PropertyPartition propertyPartition : propertyPartitions) {
			propertyPartition.setPartitionModel(this);
			for (Partition p : propertyPartition.getPartitions()) {
				if(!partitions.contains(p))
					partitions.add(p);
			}
		}
	}
	
}
