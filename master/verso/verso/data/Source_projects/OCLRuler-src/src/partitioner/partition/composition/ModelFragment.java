package partitioner.partition.composition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import oclruler.metamodel.Model;

public class ModelFragment {
	private FragmentSet set;
	private HashSet<ObjectFragment> objectFragments;
	
	public ModelFragment(FragmentSet set) {
		this.set = set;
		objectFragments = new HashSet<>();
	}
	
	public void cleanEmptyObjects(){
		ArrayList<ObjectFragment> empties = new ArrayList<>();
		for (ObjectFragment objectFragment : objectFragments) {
			if(objectFragment.isEmpty())
				empties.add(objectFragment);
		}
		for (ObjectFragment objectFragment : empties) {
			objectFragments.remove(objectFragment);
		}
	}
	
	public int countProperties(){
		int sum = 0;
		for (ObjectFragment objectFragment : objectFragments) {
			sum += objectFragment.getPropertyConstraints().size();
		}
		return sum;
	}
	
	public int coverage(Model model){
		int resInt = 0;
		for (ObjectFragment objectFragment : objectFragments) {
			resInt+=objectFragment.isCoveredBy(model);
		}
		return resInt;
	}
	
	@Deprecated
	public int coverage2(Model model){
		
		HashMap<ObjectFragment, Boolean> res = new HashMap<>();
		int resInt = 0;
		for (ObjectFragment objectFragment : objectFragments) {
			res.put(objectFragment, false);
			int tmp = 0;
			if((tmp = objectFragment.isCoveredBy(model)) > 0){
				res.put(objectFragment, true);
				resInt+= tmp;
			}
		}
		return resInt;
	}
	
	public boolean isEmpty(){
		if(! objectFragments.isEmpty())
			for (ObjectFragment objectFragment : objectFragments) {
				if(!objectFragment.isEmpty())
					return false;
			}
		return true;
	}
	
	public FragmentSet getSet() {
		return set;
	}
	public void setSet(FragmentSet set) {
		this.set = set;
	}
	public HashSet<ObjectFragment> getObjectFragments() {
		return objectFragments;
	}
	public void setObjectFragments(HashSet<ObjectFragment> objectFragments) {
		this.objectFragments = objectFragments;
	}
	public boolean addObjectFragment(ObjectFragment of){
		return this.objectFragments.add(of);
	}
	
	public String prettyPrint() {
		String res = "MF{";
		for (ObjectFragment of : getObjectFragments()) {
			res += of.prettyPrint() + ",";
		}
		if(res.endsWith(","))
			res = res.substring(0, res.length()-1);
		return res+"}";
	}
	
	@Override
	public String toString() {
		return prettyPrint();
	}

}
