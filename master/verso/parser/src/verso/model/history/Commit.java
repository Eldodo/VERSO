package verso.model.history;
import java.util.Date;





public class Commit {
	private long name =0;
	private String author ="";
	private Date date = null;
	//private char action = ' ';
	private String comment;
	
	public Commit(long name, /*char action,*/ String author, Date date, String comment)
	{
		this.name = name;
		this.author = author;
		this.date = date;
		//this.action = action;
		this.comment = comment;
	}
	
	public long getName()
	{
		return this.name;
	}
	
	public String getAuthor()
	{
		return this.author;
	}
	
	public Date getDate()
	{
		return this.date;
	}
	
	public String getTextualDate()
	{
		return "" + this.date.getTime();
	}
	/*
	public char getAction()
	{
		return action;
	}
	*/
	public String getComment()
	{
		if (this.comment.compareTo("") == 0)
			return "no comment";
		return this.comment;
	}
	
	public String toString()
	{
		return "Commit : " + name + " , "  + author + " , " + date + " , " + comment;
	}
}
