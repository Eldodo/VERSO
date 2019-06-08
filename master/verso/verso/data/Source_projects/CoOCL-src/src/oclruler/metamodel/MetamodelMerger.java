package oclruler.metamodel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.utilities.TypedElement;

import coocl.ocl.CollectOCLIds;
import coocl.ocl.Program;

public class MetamodelMerger {
	static Logger LOGGER = Logger.getLogger(MetamodelMerger.class.getName()) ;
	
	
	Metamodel mm1, mm2;
	CollectOCLIds<?, ?, ?, ?, ?, ?, ?, ?, ?> collector;
	File oclFile;
	
	HashMap<DIFF_TYPE, ArrayList<Concept>> diffConcepts;
	HashMap<DIFF_TYPE, ArrayList<StructuralFeature>> diffStructuralFeatures;
	
	
	static MetamodelMerger instance;
	public static MetamodelMerger getInstance() {
		if(instance == null)
			LOGGER.severe("No instance yet instantiated !");
		return instance;
	}
	
	public static void setInstance(MetamodelMerger instance) {
		MetamodelMerger.instance = instance;
	}
	
	private CollectOCLIds<?, ?, ?, ?, ?, ?, ?, ?, ?> getCollector() {
		return collector;
	}
	
	/*
	 * StructuralFeatures
	 */
	
	public HashMap<DIFF_TYPE, ArrayList<StructuralFeature>> getDiffStructuralFeatures() {
		return diffStructuralFeatures;
	}
	public ArrayList<StructuralFeature> getRemovedStructuralFeatures() {
		return diffStructuralFeatures.get(DIFF_TYPE.REMOVE);
	}
	public ArrayList<StructuralFeature> getAddedStructuralFeatures() {
		return diffStructuralFeatures.get(DIFF_TYPE.ADD);
	}
	public ArrayList<StructuralFeature> getDiffStructuralFeatures(DIFF_TYPE dt) {
		return diffStructuralFeatures.get(dt);
	}
	
	
	public ArrayList<StructuralFeature> getDiffStructuralFeatures(DIFF_TYPE... dts) {
		ArrayList<StructuralFeature> res = new ArrayList<>();
		for (DIFF_TYPE diff_TYPE : dts) {
			res.addAll(diffStructuralFeatures.get(diff_TYPE));
		}
		return res;
	}


	public ArrayList<StructuralFeature> getAllDiffStructuralFeatures() {
		ArrayList<StructuralFeature> res = new ArrayList<>();
		for (DIFF_TYPE dt : DIFF_TYPE.values()) {
			for (StructuralFeature sf : getDiffStructuralFeatures(dt)) {
				if(!res.contains(sf))
					res.add(sf);
			}
		}
		return res;
	}

	
	public HashMap<StructuralFeature, ArrayList<TypedElement<?>>> getStructuralFeaturesAffected() {
		return  collector.getSfsAffected();
	}
	public ArrayList<TypedElement<?>> getAffectedOCLElement(StructuralFeature sf) {
		return  collector.getElementForEsf(sf);
	}
	public ArrayList<TypedElement<?>> getAffectedOCLElements_byRemovedStructuralFeatures() {
		ArrayList<TypedElement<?>> res = new ArrayList<>();
		for (StructuralFeature sf : getRemovedStructuralFeatures()) {
			ArrayList<TypedElement<?>> tes = getAffectedOCLElement(sf);
			if(tes != null)
				for (TypedElement<?> te : tes) {
					if(te != null && ! res.contains(te))
						res.add(te);
				} 
		}
		return res;
	}
	
	/*
	 * Concepts
	 */
	
	public HashMap<DIFF_TYPE, ArrayList<Concept>> getDiffConcepts() {
		return diffConcepts;
	}
	public ArrayList<Concept> getDiffConcepts(DIFF_TYPE dt) {
		return diffConcepts.get(dt);
	}
	
	public ArrayList<Concept> getAllDiffConcepts() {
		ArrayList<Concept> res = new ArrayList<>();
		for (DIFF_TYPE dt : DIFF_TYPE.values()) {
			for (Concept c : getDiffConcepts(dt)) {
				if(!res.contains(c))
					res.add(c);
			}
		}
		return res;
	}
	public ArrayList<MMElement> getAllDiffMMELements() {
		ArrayList<MMElement> res = new ArrayList<>();
		for (DIFF_TYPE dt : DIFF_TYPE.values()) {
			for (Concept c : getDiffConcepts(dt)) {
				if(!res.contains(c))
					res.add(c);
			}
			for (StructuralFeature c : getDiffStructuralFeatures(dt)) {
				if(!res.contains(c))
					res.add(c);
			}
		}
		return res;
	}
	public ArrayList<MMElement> getDiffMMELements(DIFF_TYPE dt) {
		ArrayList<MMElement> res = new ArrayList<>();
		for (Concept c : getDiffConcepts(dt)) {
			if(!res.contains(c))
				res.add(c);
		}
		for (StructuralFeature c : getDiffStructuralFeatures(dt)) {
			if(!res.contains(c))
				res.add(c);
		}
		return res;
	}
	
//	public ArrayList<MMElement> getDiffMMElements() {
//		ArrayList<MMElement> res = new ArrayList<>();
//		for (DIFF_TYPE dt : dts) {
//			res.addAll(getDiffMMElements());
//		}
//		return res;
//	}

	public ArrayList<Concept> getRemovedConcepts() {
		return diffConcepts.get(DIFF_TYPE.REMOVE);
	}
	
	public HashMap<Concept, ArrayList<TypedElement<?>>> getConceptsAffected() {
		return collector.getConceptsAffected();
	}
	public ArrayList<TypedElement<?>> getAffectedOCLElement(Concept c) {
		return  collector.getElementForConcept(c);
	}
	public ArrayList<TypedElement<?>> getAffectedOCLElements_byRemovedConcepts() {
		ArrayList<TypedElement<?>> res = new ArrayList<>();
		for (Concept sf : getRemovedConcepts()) {
			ArrayList<TypedElement<?>> tes = getAffectedOCLElement(sf);
			if(tes != null)
				for (TypedElement<?> te : tes) {
					if(te != null && ! res.contains(te))
						res.add(te);
				}
		}
		return res;
	}
	
	
	
	public ArrayList<MMElement> getAllDiffMMElements(){
		ArrayList<MMElement> res = new ArrayList<>();
		res.addAll(getAllDiffConcepts());
		res.addAll(getAllDiffStructuralFeatures());
		return res;
	}
	
	/*
	 * Constraints
	 */
	
	public HashMap<String, Constraint> getConstraints() {
		return collector.getConstraints();
	}
	
	public Constraint getConstraint(String name){
		return getConstraints().get(name);
	}
	
	
	
	/*
	 * 
	 */
	
	public MetamodelMerger(Metamodel metamodel1, Metamodel metamodel2, File oclFile) {
		this.mm1 = metamodel1;
		this.mm2 = metamodel2;
		this.oclFile = oclFile;
		
		collector = CollectOCLIds.newCollectOCLId(mm1);
		
		diffConcepts = mm1.diffConcepts(mm2);
		diffStructuralFeatures = mm1.diffStructuralReferences(mm2);
		
//		for (DIFF_TYPE dt : DIFF_TYPE.values()) {
//			for (Concept c : diffConcepts.get(dt)) {
//				for (StructuralFeature sf : c.getStructuralFeatures()) {
//					if(!diffStructuralFeatures.get(dt).contains(sf))
//						diffStructuralFeatures.get(dt).add(sf);
//				}
//			}
//		}
		
		for (Concept c : diffConcepts.get(DIFF_TYPE.ADD)) {
			for (StructuralFeature sf : c.getStructuralFeatures()) {
				if(!diffStructuralFeatures.get(DIFF_TYPE.ADD).contains(sf))
					diffStructuralFeatures.get(DIFF_TYPE.ADD).add(sf);
			}
		}
		
		
		LOGGER.fine(printMetamodelsDiff());
		
		processOCLFile(oclFile);
		
		LOGGER.fine(printOCLFootprint());

		
	}
	
	
	
	protected void processOCLFile(File file) {
		LOGGER.fine(file.getName());
		try {
			collector.load(file);
		} catch (IOException e) {
			throw new IllegalArgumentException("OCL file not found: '"+file.getAbsolutePath()+"'\n OCL_FILE must be changed in config.properties.");
		}
		collector.computeRemovedContexts(mm2);
	}
	
	public enum DIFF_TYPE {
		ADD, REMOVE, CARDINALITY_UP, CARDINALITY_DOWN;
	}

	public void swapAffectedElements(HashMap<TypedElement<?>, TypedElement<?>> swaps) {
		collector.swapAffectedElements( swaps);
	}
	
	public String printMetamodelsDiff(){
		String res = "";
		//++ LOG metamodels' diffs
		res += "diff on concepts: " + "\n";
		for (DIFF_TYPE dt : DIFF_TYPE.values()) {
			if(!diffConcepts.get(dt).isEmpty()){
				res += " + "+dt + "\n";
				for (Concept c : diffConcepts.get(dt)) 
					res += "   - "+c + "\n";
			} else 
				res += " - No "+dt + "\n";
		}
		
		res += "diff on strucutral features: " + "\n";
		for (DIFF_TYPE dt : DIFF_TYPE.values()) {
			if(!diffStructuralFeatures.get(dt).isEmpty()){
				res += " + "+dt + "\n";
				for (StructuralFeature sf : diffStructuralFeatures.get(dt)) 
					res += "   - "+sf + "\n";
			} else 
				res += " - No "+dt + "\n";
		}
		//--end LOG metamodels' diffs
		return res;
	}
	public String printOCLFootprint(){
		String res = "";	
		//++ LOG OCL ids 
		res += "Constraints with context affected :" + "\n";
		for (Constraint cst : collector.getRemovedConstraints()) {
			res += " - "+cst.getName()+" -> "+cst.getSpecification().getContextVariable().getType().getName() + "\n";
		}
		
		Set<StructuralFeature> sfs = collector.getSfsAffected().keySet();
		res += "OCL SF ids :" + "\n";
		for (StructuralFeature sf : sfs) {
			String remove = "";
			if(diffStructuralFeatures.get(DIFF_TYPE.REMOVE).contains(sf))
				remove += " -> SF removed";
			if( diffConcepts.get(DIFF_TYPE.REMOVE).contains(sf.getSourceConcept()))
				remove += " -> Source ("+sf.getSourceClassName()+") removed";
			if( diffConcepts.get(DIFF_TYPE.REMOVE).contains(sf.getType()))
				remove += " -> Type ("+sf.getSourceClassName()+") removed";
			
			res += " - "+sf+" \t"+remove + "\n";
		}
		
		Set<Concept> concepts = collector.getConceptsAffected().keySet();
		res += "OCL Type ids :" + "\n";
		for (Concept c : concepts) {
			res += " - "+c.getName()+": " + "\n";
			for (TypedElement<?> te : collector.getConceptsAffected().get(c)) {
				res += "   - "+te + "\n";
			}
			
		}
		//-- LOG OCL ids 
		return res;
	}



}
