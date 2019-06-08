package verso.model.metric;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class NominaleMetricDescriptor<Type> extends MetricDescriptor
{
	Map<Type,Integer> valueSet = new TreeMap<Type,Integer>();
	int currentIndex =0;
	public NominaleMetricDescriptor(String name, Set<Type> values)
	{
		super(name);
		int i = 1;
		for (Type t : values)
		{
			if (!valueSet.containsKey(t))
				valueSet.put(t, i++);
		}
		currentIndex = i;
	}
	
	public void addValueInSet(Type value)
	{
		if (!valueSet.containsKey(value))
			valueSet.put(value, currentIndex++);
	}
	
	public int getNumberOfElement()
	{
		return valueSet.size();
	}
	
	public int getNumberFromType(Type t)
	{
		if (valueSet.containsKey(t))
			return valueSet.get(t);
		else
			return 0;
	}
	
	public String getSetString()
	{
		String toReturn = "";
		for (Type t : valueSet.keySet())
		{
			toReturn += t.toString() + ":";
		}
		if (toReturn.length() > 1)
		{
			toReturn = toReturn.substring(0, toReturn.length()-1);
		}
		return toReturn;
	}
	
	public List<Type> getValues(){
		List<Type> lst = new ArrayList<Type>();
		lst.addAll(this.valueSet.keySet());
		return lst;
	}
	

}
