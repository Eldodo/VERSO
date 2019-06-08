package verso.model.metric;

import java.util.List;


public class NominaleMetric<Type> extends Metric<Type>
{

	NominaleMetricDescriptor<Type> md;
	
	public NominaleMetric(NominaleMetricDescriptor<Type> metric, Type val)
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
		return this.md.getNumberFromType(t)/((double)this.md.getNumberOfElement());
	}

	public double getNormalizedValue() 
	{
		if (/*this.md.getNumberOfElement() == 1 ||*/ this.md.getNumberOfElement() == 0)
			return 1.0;
		return this.md.getNumberFromType(this.value)/((double)this.md.getNumberOfElement());
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

	@Override
	public String getMaxString() {
		return md.getSetString();
	}

	@Override
	public String getMinString() {
		return "";
	}

	@Override
	public String getTypeString() {
		return "string";
	}

	
}
