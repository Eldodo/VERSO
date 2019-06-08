package verso.model.metric;

@SuppressWarnings("rawtypes")
public class NullMetric extends Metric {

	
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public double getNormalizedValue() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public String getTextualValue()
	{
		return "" + (-1);
	}

	
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}
	public int getValeur()
	{
		return 0;
	}
	
	public LegendDescriptor getLegendDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getMaxString() {
		return null;
	}


	@Override
	public String getMinString() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getTypeString() {
		// TODO Auto-generated method stub
		return null;
	}

	

}
