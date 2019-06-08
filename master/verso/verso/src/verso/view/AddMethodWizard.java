package verso.view;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;

import verso.util.TypeFinderUtil;

public class AddMethodWizard extends Wizard{

	AddMethodPage amwp;
	
	public AddMethodWizard(Shell shell)
	{
		super();
		amwp = new AddMethodPage("AddMethodPage");
		amwp.createControl(shell);
		this.addPage(amwp);
	}
	@Override
	public boolean performFinish() {
		System.out.println("ça vas tu marcher?");
		String className = amwp.getClassName();
		IType type = TypeFinderUtil.findTypeFromString(className);
		String methodTexte = createMethod();
		try
		{
		type.createMethod(methodTexte, null, false, null);
		}catch(Exception e){System.out.println(e);}
		return true;
	}
	
	private String createMethod()
	{
		String toReturn = "";
		toReturn += "public ";
		toReturn += amwp.getReturnType() + " ";	
		toReturn += amwp.getMethodName();
		toReturn += "(";
		String params = "";
		for (String s : amwp.getParams())
		{
			params += s + ",";
		}
		if (params.length() > 1)
			params = params.substring(0, params.length()-1);
		toReturn += params;
		toReturn += ")\n";
		toReturn += "{\n";
		toReturn += "\t//Add your code here\n";
		toReturn += "}";	
		
		return toReturn;
	}
	
	public void setElement(String element)
	{
		this.amwp.setClassText(element);
	}
	
	

}
