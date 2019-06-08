package verso.view;

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.dialogs.FilteredTypesSelectionDialog;
import org.eclipse.jdt.ui.wizards.NewElementWizardPage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import verso.util.TypeFinderUtil;

public class AddParamWizardPage extends NewElementWizardPage{

	Text textType = null;
	Text textName = null;
	public AddParamWizardPage(String name) {
		super(name);
	}

	
	public void createControl(Composite parent) {
		this.setControl(parent);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		parent.setLayout(gridLayout);
		
		Label labelType = new Label(parent, SWT.LEFT);
		labelType.setText("Parameter Type : ");
		textType = new Text(parent, SWT.LEFT);
		GridData gdt = new GridData();
		gdt.widthHint = 400;
		textType.setLayoutData(gdt);
		textType.setText("int");
		Button buttonType = new Button(parent,SWT.NONE);
		buttonType.setText("Browse");
		buttonType.addMouseListener(new BrowseListener());
		
		Label labelName = new Label(parent, SWT.LEFT);
		labelName.setText("Parameter Name : ");
		textName = new Text(parent,SWT.LEFT);
		GridData gdn = new GridData();
		gdn.widthHint = 400;
		textName.setLayoutData(gdn);
		textName.setText("param");
		
		
	}
	
	public String getType()
	{
		return this.textType.getText().trim();
	}
	
	public String getName()
	{
		return this.textName.getText().trim();
	}
	
	private class BrowseListener implements MouseListener
	{


		public void mouseDoubleClick(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

	
		public void mouseDown(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

	
		public void mouseUp(MouseEvent e) {
			IPackageFragmentRoot[] rootArray = TypeFinderUtil.getSourceFragmentRoot(true);
			IJavaSearchScope scope = SearchEngine
					.createJavaSearchScope(rootArray);
			
			FilteredTypesSelectionDialog dialog = new FilteredTypesSelectionDialog(
					getShell(), false, getWizard().getContainer(), scope,
					IJavaSearchConstants.TYPE);
			dialog.setTitle("Class Selection");
			dialog.setMessage("Please Select a Class");
			if (dialog.open() == Window.OK) {
				String elementName = ((IType) dialog.getFirstResult())
						.getElementName();
				String packageName = ((IType) dialog.getFirstResult())
						.getPackageFragment().getElementName();
				String fullName = (packageName.length() > 0) ? packageName
						+ "." + elementName : elementName;
				textType.setText(elementName);
			}
			
		}
		
	}

}
