package verso.representation;


public interface IPickable {

	public String getName();
	public String getSimpleName();
	
	public default boolean isElement() {return false;}
	public default boolean isPackage() {return false;}
}
