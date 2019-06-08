package partitioner.partition;

import java.util.ArrayList;

public abstract class Partition {
	PropertyPartition propertyPartition;
	
	public Partition(PropertyPartition propertyPartition) {
		this.propertyPartition = propertyPartition;
	}
	
	
	
	public abstract String getTypeName();
	public abstract ArrayList<? extends Range> getRanges();
	
	public String prettyPrint() {
		String res = getClass().getSimpleName()+":"+getClassName()+"."+getFeatureName()+":";
		res += "{";
		for (Range r : getRanges()) {
			res += r + ", ";
		}
		if(res.endsWith(", "))
			res = res.substring(0, res.length() - 2);
		res +="}";
		return res;
	}
	
	String getRangeTypeName(){
		if(getRanges().isEmpty()){
			return "undefined";
		} else {
			return getRanges().get(0).getTypeName();
		}
	}
	
	public String printXML(String tab) {
		String res = tab+"<fragment type=\""+getTypeName()+"\" source=\""+getClassName()+"."+getFeatureName()+"\">\n";
		res += tab+tab+"<values type=\""+getRangeTypeName()+"\">\n";
		for (Range r : getRanges()) {
			res +=  tab+tab+tab+"<value>"+r.printValue() + "</value>\n";
		}
		res += tab+tab+"</values>\n";
		return res+tab+"</fragment>";
	}

	
	@Override
	public String toString() {
		return prettyPrint();
	}

	public boolean isObjectInRange(Object o, Range r){
		return r.isContained(o);
	}

	public String getFeatureName() {
		return propertyPartition.getFeatureName();
	}

	public String getClassName() {
		return propertyPartition.getClassName();
	}
	
	public void setPropertyPartition(PropertyPartition propertyPartition) {
		this.propertyPartition = propertyPartition;
	}

	
	
	@Override
	public boolean equals(Object obj) {
//		System.out.println("Partition.equals()");
		if(obj != null && obj instanceof Partition){
			Partition p = (Partition)obj;
			boolean b = true;
			b &= getRanges().size() == p.getRanges().size();
			if(b)
				for (Range r : getRanges()) b &= p.getRanges().contains(r);
//			System.out.println(this + (b?" == ":" != ") + p);
			b &= getFeatureName().compareTo(p.getFeatureName()) == 0;
			b &= getClassName().compareTo(p.getClassName()) == 0;
			return b;
		}
//		System.out.println(" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		return false;
	}

	
}
