package verso.view;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;

public class AddParamWizard extends Wizard{

	AddMethodPage addMethodPage = null;
	AddParamWizardPage apwp = null;
	public AddParamWizard(Shell shell, AddMethodPage page)
	{
		super();
		apwp = new AddParamWizardPage("AddParamPage");
		apwp.createControl(shell);
		this.addPage(apwp);
		addMethodPage = page;
	}
	
	public boolean performFinish() {
		addMethodPage.addParam(apwp.getType(), apwp.getName());
		return true;
	}


}
