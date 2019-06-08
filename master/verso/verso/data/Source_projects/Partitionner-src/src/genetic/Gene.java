package genetic;


public interface Gene   {
	public Gene clone();
	public int size();
	
	public String prettyPrint();
	public String getResourceFileName();
}
