package partition;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;


public class PropertyPartition {

	private PartitionModel partitionModel;

	private String featureName;
	private String className;

	private ValuePartition valuePartition;
	private MultiplicityPartition multiplicityPartition;

	public PropertyPartition(EClass eclass, EStructuralFeature esf) {
		this.className = 	  eclass.getName();
		this.featureName = 	  esf.getName();
		
		int lower = esf.getLowerBound();
		int upper = esf.getUpperBound();
		
		if(esf instanceof EAttribute){
			EAttribute ea = (EAttribute) esf;
			valuePartition = new ValuePartition(this, ea.getEAttributeType());
			if(lower == upper && lower != 1)
				multiplicityPartition = new MultiplicityPartition(this, lower, upper);
		} else{
			multiplicityPartition = new MultiplicityPartition(this, lower, upper);
		}
	}
	
	public PropertyPartition(PartitionModel partitionModel, EClass eclass, EStructuralFeature esf) {
		this(eclass, esf);
		this.partitionModel = partitionModel;
	}

	public PartitionModel getPartitionModel() {
		return partitionModel;
	}
	
	public String getFeatureName() {
		return featureName;
	}
	public String getClassName() {
		return className;
	}

	public ValuePartition getValuePartition() {
		return valuePartition;
	}
	public MultiplicityPartition getMultiplicityPartition() {
		return multiplicityPartition;
	}

	public void setPartitionModel(PartitionModel partitionModel) {
		this.partitionModel = partitionModel;
	}
	public void setValuePartition(ValuePartition valuePartition) {
		this.valuePartition = valuePartition;
	}
	public void setMultiplicityPartition(MultiplicityPartition multiplicityPartition) {
		this.multiplicityPartition = multiplicityPartition;
	}
	@Override
	public String toString() {
		return "PP:("+className+"."+featureName+")";
	}
	
	public String prettyPrint(){
		String res = "";
		for (Partition p : getPartitions()) 
			res += p.prettyPrint();
		return res;
	}

	public Collection<Partition> getPartitions() {
		ArrayList<Partition> res = new ArrayList<>();
		if(multiplicityPartition != null)
			res.add(multiplicityPartition);
		if(valuePartition != null)
			res.add(valuePartition);
		return res;
	}
}
