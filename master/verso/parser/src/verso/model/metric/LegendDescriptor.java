package verso.model.metric;

import java.util.ArrayList;
import java.util.List;

public class LegendDescriptor {
	public static final int INTERVAL = 1;
	public static final int NOMINAL = 2;
	private List<LegendValue> values = new ArrayList<LegendValue>();
	private int type;
	
	public LegendDescriptor(int type)
	{
		this.type = type;
	}
	
	public void addLegendItem(LegendValue item)
	{
		this.values.add(item);
	}
	
	public List<LegendValue> getList()
	{
		return values;
	}
	
	public int getType()
	{
		return type;
	}

}
