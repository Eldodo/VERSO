package partition.composition;

import genetic.Entity;
import genetic.Evaluator;
import genetic.fitness.FitnessVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import models.Model;
import models.ModelSet;

public class FragmentSet implements Evaluator {
	public final static Logger LOGGER = Logger.getLogger(FragmentSet.class.getName());
	
	private ArrayList<ModelFragment> fragments;
		HashSet<ModelFragment> uncovereds;
	
	public FragmentSet() {
		fragments = new ArrayList<>();
		uncovereds = new HashSet<>();
	}
	
	
	public void cleanFragments(){
		ArrayList<ModelFragment> empties = new ArrayList<>();
		for (ModelFragment modelFragment : fragments) {
			modelFragment.cleanEmptyObjects();
			if(modelFragment.isEmpty())
				empties.add(modelFragment);
		}
		for (ModelFragment modelFragment : empties) 
			fragments.remove(modelFragment);
	}
	
	@Override
	public FitnessVector evaluateCoverage(Entity modelSet) {
		return evaluateCoverage((ModelSet)modelSet);
	}
	
	public FitnessVector evaluateCoverage(ModelSet modelSet){
		HashMap<ModelFragment, Integer> covertureMap = new HashMap<>();
		uncovereds = new HashSet<>();
		uncovereds.addAll(fragments);
		
		
		int nbFrags = fragments.size();
		ModelFragment[] mfs = (ModelFragment[]) fragments.toArray(new ModelFragment[fragments.size()]);
		
		Model[] ms = (Model[]) modelSet.getModels().toArray(new Model[modelSet.getModels().size()]);
		
		float avgSizeModels = 0;
		for (Model m : modelSet.getModels()) 
			avgSizeModels += m.size();
		avgSizeModels = (float)avgSizeModels / (float)modelSet.size();
		
		for (ModelFragment mf : mfs) {
			int nbCover = 0;
			
			for (Model m :ms) 
				nbCover += mf.coverage(m);
//			System.out.println(mf.prettyPrint()+" : "+nbCover);
			covertureMap.put(mf, nbCover);;
			if(nbCover  > 0)
				uncovereds.remove(mf);
		}
		FitnessVector fv = new FitnessVector(modelSet, covertureMap, avgSizeModels);
		modelSet.setFitnessVector(fv);
		if(!LOGGER.isLoggable(Level.FINE))
			LOGGER.config(nbFrags+" fragments evaluated : FV="+fv +" uncovered : "+uncovereds.size());
		else{
			LOGGER.fine(nbFrags+" fragments evaluated : FV="+fv +" uncovered : "+uncovereds.size()+ " : "+uncovereds);
		}
		return fv;
	}
	
	
	public int countObject(){
		int sum = 0;
		for (ModelFragment modelFragment : fragments) {
			sum += modelFragment.getObjectFragments().size();
		}
		return sum;
	}
	
	public int countProperties(){
		int sum =0;
		for (ModelFragment modelFragment : fragments) 
			sum += modelFragment.countProperties();
		return sum;
	}
	
	public ArrayList<ModelFragment> getFragments() {
		return fragments;
	}
	public void setFragments(ArrayList<ModelFragment> fragments) {
		this.fragments = fragments;
	}
	/**
	 * !!! uncovereds list is modified in evaluateCoverage() !!!
	 * @return Fragments NOT covered on last evaluation.
	 */
	public HashSet<ModelFragment> getUncovereds() {
		return uncovereds;
	}
	public boolean addFragment(ModelFragment mf) {
		if(this.fragments.contains(mf))
			return false;
		return this.fragments.add(mf);
	}
	@Override
	public String toString() {
		return "FragmentSet:MF="+getFragments().size()+"";
	}

	public String prettyPrint() {
		String res = toString()+ ":(\n";
		for (ModelFragment mf : getFragments()) {
			res += "   " +mf.prettyPrint() + ",\n";
		}
		if(res.endsWith(",\n"))
			res = res.substring(0, res.length()-2)+"\n";
		res += ")";
		return res;
	}
}
