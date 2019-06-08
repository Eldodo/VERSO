package oclruler.metamodel;

import oclruler.utils.ToolBox;

public abstract class NamedEntity implements Comparable<NamedEntity>, Cloneable{
	
	/**
	 * ATTENTION Here "id" is harcoded. Like a RESERVED FIELD NAME for identification.
	 */
	public static String ID = "ID_Jess";
	public final static String ROOT_CLASS = "O";
	public final static String NAME_SEPARATOR = ".";
	private static long id_counter = (long) (ToolBox.getRandomInt(17) * ToolBox.getRandomInt(17));
	protected long numid;
	protected String name;
	
	/**
	 * Meta description of the entity (null for MMCLass)
	 */
	
	public NamedEntity(String name) {
		numid = id_counter++;
		setName(name);
	}
	
	
	public String getName() {
		return name;
	}
	
	/**
	 * If not, to be redefined in subclasses.
	 * @return
	 */
	public String getFullName() {
		return getName();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean equalNames(NamedEntity end) {
		if(end == null)
			return false;
		return this.name.equals(end.name);
	}

	public String getId() {
		return name+"_"+numid;
	}
	public long getNumericId() {
		return numid;
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && getClass() == o.getClass())
			return this.getNumericId() == ((NamedEntity)o).getNumericId();
		return false;
	}
	
	@Override
	public int compareTo(NamedEntity o) {
		if(o != null)
			return getId().compareTo(o.getId());
		return -1;
	}
	
	@Override
	public String toString() {
		return "("+getClass().getSimpleName()+":"+getId()+")";
	}

	public abstract String prettyPrint(String tab);

	public String prettyPrint() {
		return prettyPrint("");
	}

	public abstract String simplePrint();
}
