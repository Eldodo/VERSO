package oclruler.genetics;


public interface Gene   {
	
	Object clone() throws CloneNotSupportedException;
	
	public boolean mutate() throws UnstableStateException;
	
	public int size();
	
	public String prettyPrint();
	
	public String getName();
	public abstract String printResultPane(String tab) ;
	/**
	 * One line pretty print.
	 * @return
	 */
	String simplePrint();
}
