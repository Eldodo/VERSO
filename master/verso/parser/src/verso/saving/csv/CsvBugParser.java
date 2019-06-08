package verso.saving.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import verso.model.Bug;
import verso.model.Element;
import verso.model.Entity;
import verso.model.Package;
import verso.model.SystemDef;

public class CsvBugParser {
	
	public static void parseBugFile(String fileName, SystemDef sys)
	{
		sys.clearBugs();
		String line = null;
		try
		{
			File f = new File(fileName);
			BufferedReader input =  new BufferedReader(new FileReader(f));
			
			while((line = input.readLine()) != null )
			{
				processLine(line,sys);
			}
			input.close();
		}catch(Exception e){System.out.println(e);}
	}
	
	private static void processLine(String line, SystemDef sys)
	{
		if (line.endsWith(";"))
			line += " ";
		int tokenIndex = 0;
		String[] tokens = line.split("[;]");
		long numberId = Long.parseLong(tokens[tokenIndex++]);
		boolean status = Boolean.parseBoolean(tokens[tokenIndex++]);
		String comment = tokens[tokenIndex++];
		Date dateIntro = new Date(Long.parseLong(tokens[tokenIndex++]));
		Date dateFermeture = new Date(Long.parseLong(tokens[tokenIndex++]));
		String firstAuthor = tokens[tokenIndex++];
		String prog = tokens[tokenIndex++];
		List<Entity> elemList = new ArrayList<Entity>();
		try{
		String elemString = tokens[tokenIndex++];
		String[] elems = elemString.split("[:]");
		
		for (int i = 0; i < elems.length; i++)
		{
			String elemRep = elems[i];
			
			if (elemRep.startsWith("e_"))
			{
				Element e = null;
				elemRep = elemRep.substring(2);
				e = sys.getElement(elemRep);
				if (e != null)
				{
					elemList.add(e);
				}
			}
			if (elemRep.startsWith("p_"))
			{
				Package p = null;
				elemRep = elemRep.substring(2);
				p = sys.getPackage(elemRep);
				if (p != null)
				{
					elemList.add(p);
				}
			}
			/*
			if (elemRep.startsWith("m_"))
			{
				Method m = null;
			}
			*/
		}
		}catch(Exception e){System.out.println(e);}
		//sys.addBugAuthor(firstAuthor);
		//sys.addBugProgrammer(prog);
		Bug b = new Bug(numberId,status, comment, firstAuthor, prog, dateIntro, dateFermeture, elemList);
		for (Entity el : elemList)
		{
			el.addBug(b);
		}
		sys.addBug(b);
	}

}
