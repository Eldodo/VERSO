package verso.model.metric;

import java.util.ArrayList;
import java.util.List;

public class OrdinaleMetricDescriptor<Type> extends MetricDescriptor{

	List<Type> valueSet = new ArrayList<Type>();
	int currentIndex =0;
	
	
	public OrdinaleMetricDescriptor(String name, Type[] values)
	{
		super(name);
		int i = 0;
		if(valueSet.size()==0)
		{
			for (Type t : values)
			{
				valueSet.add(t);
				//System.out.println(valueSet.get(t));
			}
			currentIndex = i;
		}
	}
	
	
	public void addValueInSet(Type value)
	{
		if (!valueSet.contains(value))
			valueSet.add(value);
	}
	
	public int getNumberOfElement()
	{
		return valueSet.size();
	}
	
	public int getNumberFromType(Type t)
	{
		return valueSet.indexOf(t);
	}
	
	public String getSetString()
	{
		String toReturn = "";
		for (Type t : valueSet)
		{
			toReturn += t.toString() + ":";
		}
		if (toReturn.length() > 1)
		{
			toReturn = toReturn.substring(0, toReturn.length()-1);
		}
		return toReturn;
	}
	
	
	public List<Type> getValues()
	{
		return valueSet;
	}
	
}
