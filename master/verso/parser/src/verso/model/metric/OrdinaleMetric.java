package verso.model.metric;

import java.util.List;

public class OrdinaleMetric<Type> extends Metric<Type>{
	
	OrdinaleMetricDescriptor<Type> md;
	
	public OrdinaleMetric(OrdinaleMetricDescriptor<Type> metric, Type val)
	{
		this.value = val;
		this.md = metric;
		
	}
	
	public String getName()
	{
		return this.md.getName();
	}

	public void setName(String name)
	{
		this.md.setName(name);
	}
	
	public double getNormalizedValue(Type t)
	{
		return this.md.getNumberFromType(t)/((double)(this.md.getNumberOfElement()-1));
	}

	public double getNormalizedValue() 
	{
		if (md.getValues().size() == 1)
			return 1.0;
		return this.md.getNumberFromType(this.value)/((double)(this.md.getNumberOfElement()-1));
	}
	
	public int getValeur()
	{
		return this.md.getNumberFromType(this.value);
	}


	public LegendDescriptor getLegendDescriptor() {
		// TODO Auto-generated method stub
		LegendDescriptor ld = new LegendDescriptor(LegendDescriptor.NOMINAL);
		List <Type> allValues = md.getValues();
		for (Type t : allValues)
		{
			ld.addLegendItem(new LegendValue(this.getNormalizedValue(t) ,t.toString()));
		}
		return ld;
	}

	public String getMaxString() {
		return md.getSetString();
	}

	public String getMinString() {
		return "";
	}

	public String getTypeString() {
		return "string";
	}

}
