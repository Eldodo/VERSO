package oclruler.genetics;


public interface Gene   {
	
	
	public boolean mutate() throws UnstableStateException;
	
	public int size();
	
	public String prettyPrint();
	
	public String getName();
	String printResultPane();
	/**
	 * One line pretty print.
	 * @return
	 */
	String simplePrint();
}
