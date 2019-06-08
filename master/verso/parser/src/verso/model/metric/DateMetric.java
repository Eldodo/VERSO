package verso.model.metric;

import java.util.Date;

public class DateMetric extends Metric<Date> {
	
	IntervaleMetricDescriptor<Date> md;
	
	public DateMetric(IntervaleMetricDescriptor<Date> metric, Date val)
	{
		this.value = val;
		this.md = metric;
		
	}
	
	public String getTextualValue()
	{
		return "" + this.value.getTime();
	}
	
	public String getName()
	{
		return this.md.getName();
	}

	public void setName(String name)
	{
		this.md.setName(name);
	}

	public double getNormalizedValue() 
	{
		double toReturn = 0;
		if ((this.md.getMax().getTime()) != (this.md.getMin().getTime()))
			toReturn = (this.value.getTime() - this.md.getMin().getTime())/(double)((this.md.getMax().getTime() - this.md.getMin().getTime()));
		return toReturn;
	}
	
	private double getNormalizedValue(Date date)
	{
		double toReturn = 0;
		if ((this.md.getMax().getTime()) != (this.md.getMin().getTime()))
			toReturn = (date.getTime() - this.md.getMin().getTime())/(double)((this.md.getMax().getTime() - this.md.getMin().getTime()));
		return toReturn;
	}
	public Date getValeur()
	{
		return this.value;
	}

	public LegendDescriptor getLegendDescriptor() {
		LegendDescriptor ld = new LegendDescriptor(LegendDescriptor.INTERVAL);
		long minDate = md.getMin().getTime();
		long maxDate = md.getMax().getTime();
		long duree = maxDate - minDate;
		Date currDate = null;
		if (minDate == maxDate)
		{
			LegendDescriptor ld2 = new LegendDescriptor(LegendDescriptor.NOMINAL);
			ld2.addLegendItem(new LegendValue(0,this.md.getMin().toString()));
			return ld2;
		}
		ld.addLegendItem(new LegendValue(this.getNormalizedValue(md.getMin()),this.md.getMin().toString()));
		for (int i = 1; i < 10; i++)
		{
			currDate = new Date(minDate +  i * duree/10);
			ld.addLegendItem(new LegendValue(this.getNormalizedValue(currDate),currDate.toString()));
		}
		ld.addLegendItem(new LegendValue(this.getNormalizedValue(md.getMax()),this.md.getMax().toString()));
		return ld;
	}

	@Override
	public String getMaxString() {
		return "" + md.getMax().getTime();
	}

	@Override
	public String getMinString() {
		return "" + md.getMin().getTime();
	}

	@Override
	public String getTypeString() {
		return "date";
	}


	

	
	
}
