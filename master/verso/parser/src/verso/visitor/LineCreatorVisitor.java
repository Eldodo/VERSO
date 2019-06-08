package verso.visitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

import verso.model.Line;

public class LineCreatorVisitor extends ASTVisitor{
	
	List<Line> lst = new ArrayList<Line>();
	
	public boolean visit(CompilationUnit cu)
	{
		int currentStartPos = 0;
		int currentEndPos = 0;
		int currentLine = 1;
		int startPosition = cu.getStartPosition();
		int lenght = cu.getLength();
		
		int i = startPosition;
		
		
		BufferedReader br = null;
		IFile f = (IFile)cu.getJavaElement().getResource();
		try{
		InputStreamReader isr = new InputStreamReader(f.getContents());
		br = new BufferedReader(isr);
		
		
		for (; i < lenght; i++)
		{
			if (cu.getLineNumber(i)>currentLine)
			{
				//System.out.println("Line " + (cu.getLineNumber(i)-1) + " : StartPos = " + currentStartPos  + " EndPos = " + (currentEndPos-1));
				String lineText = br.readLine().replaceAll("\t", "    ");
				lst.add(new Line("Line" + (cu.getLineNumber(i)-1),currentStartPos, lineText.length(),lineText));
				currentStartPos = i;
				currentLine = cu.getLineNumber(i);
				
				
				
			}
			currentEndPos++;
			//System.out.println("Line : " + cu.getLineNumber(i));
		}
		lst.add(new Line("Line" + currentLine, currentStartPos, i-1-currentStartPos,br.readLine()));
		br.close();
		}catch(Exception e){System.out.println(e);}
		return true;
	}
	
	public List<Line> getLines()
	{
		return this.lst;
	}

}
