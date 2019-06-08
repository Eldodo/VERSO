package verso.model.metric;

public class IntegralNumberMetric<Type extends Number> extends NumberMetric<Type>{

	
	
	public IntegralNumberMetric(IntervaleMetricDescriptor<Type> metric, Type val) {
		super(metric, val);
		// TODO Auto-generated constructor stub
	}
	
	public LegendDescriptor getLegendDescriptor() {
		LegendDescriptor ld = new LegendDescriptor(LegendDescriptor.INTERVAL);
		long min = md.getMin().longValue();
		long max = md.getMax().longValue();
		long difference = max - min;
		long currValue = 0;
		long maxIndice = max - min;
		if (maxIndice > 10)
			maxIndice = 10;
		if (min == max)
		{
			LegendDescriptor ld2 = new LegendDescriptor(LegendDescriptor.NOMINAL);
			ld2.addLegendItem(new LegendValue(1.0,this.md.getMax().toString()));
			return ld2;
		}
		ld.addLegendItem(new LegendValue(this.getNormalizedValue(md.getMin()),this.md.getMin().toString()));
		for (int i = 1; i < maxIndice; i++)
		{
			currValue = min +  i * difference/maxIndice;
			ld.addLegendItem(new LegendValue(this.getNormalizedValue(currValue), String.valueOf(currValue)));
		}
		ld.addLegendItem(new LegendValue(this.getNormalizedValue(md.getMax()),this.md.getMax().toString()));
		return ld;
	}

	
	public double getNormalizedValue(Number value) {
		// TODO Auto-generated method stub
		if (this.md.getMin() == this.md.getMax())
			return 1.0;
		return Math.min(1.0,(value.longValue() - this.md.getMin().longValue())/(double)(this.md.getMax().longValue() - this.md.getMin().longValue()));
	}

	public String getMaxString() {
		
		return "" + md.getMax();
	}

	public String getMinString() {
		return "" + md.getMin();
	}

	public String getTypeString() {
		return "double";
	}

}
