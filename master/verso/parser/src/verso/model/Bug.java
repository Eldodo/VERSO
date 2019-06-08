package verso.model;

import java.util.Date;
import java.util.List;

public class Bug {
	
	private long numberID;
	private boolean status;
	private String comment;
	private Date dateIntro;
	private String firstAuthor;
	private List<Entity> entities;
	private String mainProg;
	private List<String> programmers;
	private Date closingDate;
	
	public Bug(long numberID, boolean status, 
			String comment, String firstAuthor, String mainProg,
			Date dateIntro, Date closingDate, 
			List<Entity> entities)
	{
		this.numberID = numberID;
		this.status = status;
		this.comment = comment;
		this.dateIntro = dateIntro;
		this.firstAuthor = firstAuthor;
		this.entities = entities;
		this.mainProg = mainProg;
		this.closingDate =closingDate;
	}
	
	public long getNumberID() {
		return numberID;
	}

	public boolean isOpen() {
		return status;
	}
	public void setOpen(boolean open)
	{
		this.status = open;
	}
	
	public Date getDateIntro() {
		return dateIntro;
	}

	public String getFirstAuthor() {
		return firstAuthor;
	}

	public List<Entity> getEntities() {
		return entities;
	}
	
	public void setEntities(List<Entity> entities)
	{
		this.entities = entities;
	}
	
	private String getElemStr()
	{
		String elemStr = "";
		for (Entity e : this.getEntities())
		{
			if (e instanceof Package)
				elemStr += "p_" + e.getName() + ":";
			if (e instanceof Element)
				elemStr += "e_" + e.getName() + ":";
			if (e instanceof Method)
				elemStr += "m_" + e.getName() + ":";
		}
		if (elemStr.length() > 0)
		{
			elemStr = elemStr.substring(0, elemStr.length()-1);
		}
		return elemStr;
	}

	public String getMainProg() {
		return mainProg;
	}

	public void setMainProg(String mainProg)
	{
		this.mainProg = mainProg;
	}
	
	public Date getClosingDate() {
		return closingDate;
	}
	
	public void setClosingDate(Date d)
	{
		this.closingDate = d;
	}
	
	
	public String getComment()
	{
		return this.comment;
	}
	
	public void setComment(String comment)
	{
		this.comment = comment;
	}
	
	public long getLengthMili()
	{
		if (!this.status)
			return this.closingDate.getTime() - this.dateIntro.getTime();
		else
			return System.currentTimeMillis() - this.dateIntro.getTime();
	}
	
	public String toString()
	{
		String toReturn = "";
		toReturn += this.getNumberID() + ";";
		toReturn += this.isOpen() + ";";
		toReturn += this.getComment() + ";";
		toReturn += this.getDateIntro().getTime() + ";";
		toReturn += this.getClosingDate().getTime() + ";";
		toReturn += this.getFirstAuthor() + ";";
		toReturn += this.getMainProg() + ";";
		toReturn += this.getElemStr() + "\n";
		return toReturn;
	}
	

}
