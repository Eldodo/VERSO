package verso.model.metric;


public class DecimalNumberMetric<Type extends Number> extends NumberMetric<Type>{

	
	public DecimalNumberMetric(IntervaleMetricDescriptor<Type> metric, Type val) {
		super(metric, val);
		// TODO Auto-generated constructor stub
	}
	
	
	public LegendDescriptor getLegendDescriptor() {
		LegendDescriptor ld = new LegendDescriptor(LegendDescriptor.INTERVAL);
		double min = md.getMin().doubleValue();
		double max = md.getMax().doubleValue();
		double difference = max - min;
		double currValue = 0;
		if (min == max)
		{
			LegendDescriptor ld2 = new LegendDescriptor(LegendDescriptor.NOMINAL);
			ld2.addLegendItem(new LegendValue(0,this.md.getMin().toString()));
			return ld2;
		}
		ld.addLegendItem(new LegendValue(this.getNormalizedValue(md.getMin()),this.md.getMin().toString()));
		for (int i = 1; i < 10; i++)
		{
			currValue = min +  i * difference/10;
			ld.addLegendItem(new LegendValue(this.getNormalizedValue(currValue), String.valueOf(currValue)));
		}
		ld.addLegendItem(new LegendValue(this.getNormalizedValue(md.getMax()),this.md.getMax().toString()));
		return ld;
	}


	
	public double getNormalizedValue(Number value) {
		// TODO Auto-generated method stub
		return Math.min(1.0,(value.doubleValue() - this.md.getMin().doubleValue())/(double)(this.md.getMax().doubleValue() - this.md.getMin().doubleValue()));
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
