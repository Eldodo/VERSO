package partition.composition;

import java.util.ArrayList;

import models.Model;

public class ObjectFragment {
	private ModelFragment modelFragment;
	private ArrayList<PropertyConstraint> constraints;
	
	public ObjectFragment(ModelFragment model) {
		this.modelFragment = model;
		constraints = new ArrayList<>();
	}
	
	public boolean isEmpty(){
		return constraints.isEmpty();
	}
	
	public ModelFragment getModelFragment() {
		return modelFragment;
	}
	public void setModelFragment(ModelFragment modelFragment) {
		this.modelFragment = modelFragment;
	}
	
	public ArrayList<PropertyConstraint> getPropertyConstraints() {
		return constraints;
	}
	public void setPropertyConstraints(ArrayList<PropertyConstraint> constraints) {
		this.constraints = constraints;
	}
	public boolean addPropertyConstraint(PropertyConstraint pc) {
		return this.constraints.add(pc);
	}
	
	public String prettyPrint() {
		String res = "";
		for (PropertyConstraint pc : getPropertyConstraints()) {
			res += pc.prettyPrint() + ", ";
		}
		if(res.endsWith(", "))
			res = res.substring(0, res.length()-2);
		return res;
	}

	public int isCoveredBy(Model m) {
		int res = 0;
		for (PropertyConstraint pc : constraints) {
			res += pc.isCoveredBy(m);
		}
//		System.out.println("ObjectFragment.isCoveredBy()"+constraints+":  "+m+" : "+res);
		return res;
	}

 }
