package verso.view;

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.dialogs.FilteredTypesSelectionDialog;
import org.eclipse.jdt.ui.wizards.NewElementWizardPage;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import verso.util.TypeFinderUtil;

public class AddMethodPage extends NewElementWizardPage {

	Text methodName = null;
	Text className = null;
	Text returnName = null;
	String classNameText = "";
	org.eclipse.swt.widgets.List lstparams = null;
	AddMethodPage thispage = null;
	Button checkPublic = null;
	Button checkProtected = null;
	Button checkPrivate = null;

	public AddMethodPage(String name) {
		super(name);
		thispage = this;
		// TODO Auto-generated constructor stub
	}

	
	public void createControl(Composite parent) {
		this.setControl(parent);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		parent.setLayout(gridLayout);
		Composite superComposite = new Composite(parent,SWT.NONE);
		GridLayout gridInnerLayout = new GridLayout();
		gridInnerLayout.numColumns = 3;
		superComposite.setLayout(gridInnerLayout);
		
		// Method Name
		Label label = new Label(superComposite, SWT.LEFT);
		label.setText("Method Name : ");
		methodName = new Text(superComposite, SWT.LEFT);
		methodName.setText("newMethod");
		GridData gd = new GridData();
		gd.widthHint = 400;
		Button invisibleButton = new Button(superComposite, SWT.NONE);
		invisibleButton.setVisible(false);
		methodName.setLayoutData(gd);

		// Class Name
		Label labelClass = new Label(superComposite, SWT.LEFT);
		labelClass.setText("Class Name : ");
		GridData gdc = new GridData();
		gdc.widthHint = 400;
		className = new Text(superComposite, SWT.LEFT);
		className.setText(classNameText);
		className.setLayoutData(gdc);
		Button classButton = new Button(superComposite, SWT.NONE);
		classButton.setText("Browse");
		classButton.addMouseListener(new ClassListener());

		// Return Type
		Label labelReturn = new Label(superComposite, SWT.LEFT);
		labelReturn.setText("Return Type : ");
		GridData gdr = new GridData();
		gdr.widthHint = 400;
		returnName = new Text(superComposite, SWT.LEFT);
		returnName.setText("void");
		returnName.setLayoutData(gdr);
		Button returnButton = new Button(superComposite, SWT.NONE);
		returnButton.setText("Browse");
		returnButton.addMouseListener(new ReturnTypeListener());
		
		//Parameters 
		Label params = new Label(superComposite, SWT.LEFT);
		params.setText("Parameters : ");
		lstparams = new org.eclipse.swt.widgets.List(superComposite,SWT.NONE);
		GridData gdp = new GridData();
		gdp.widthHint = 400;
		lstparams.setLayoutData(gdp);
		Button addParamButton = new Button(superComposite, SWT.NONE);
		addParamButton.setText("Add ...");
		addParamButton.addMouseListener(new ParamAdder());
		
		//Visibility
		Composite superVis = new Composite(parent,SWT.NONE);
		FillLayout fillLayout = new FillLayout();
		superVis.setLayout(fillLayout);
		checkPublic = new Button(superVis, SWT.RADIO);
		checkPublic.setText("Public");
		checkProtected = new Button(superVis, SWT.RADIO);
		checkProtected.setText("Protected");
		checkPrivate = new Button(superVis, SWT.RADIO);
		checkPrivate.setText("Private");
	}

	public String getClassName() {
		return className.getText();
	}

	public String getMethodName() {
		return methodName.getText();
	}
	
	public String getReturnType()
	{
		return returnName.getText();
	}
	
	public String[] getParams()
	{
		return lstparams.getItems();
	}
	
	public String getVisibility()
	{
		if (this.checkPrivate.getSelection())
		{
			return "private";
		}
		if (this.checkProtected.getSelection())
		{
			return "protected";
		}
		if (this.checkPublic.getSelection())
		{
			return "public";
		}
		return "public";
	}

	public void setClassText(String name) {
		this.classNameText = name;
		this.className.setText(name);
	}
	
	public void addParam(String type, String name)
	{
		this.lstparams.add(type + " " + name);
	}
	

	private class ClassListener implements MouseListener {

		
		public void mouseDoubleClick(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		
		public void mouseDown(MouseEvent e) {
			// TODO Auto-generated method stub

		}

	
		public void mouseUp(MouseEvent e) {
			
			IPackageFragmentRoot[] rootArray = TypeFinderUtil.getSourceFragmentRoot(false);
			IJavaSearchScope scope = SearchEngine
					.createJavaSearchScope(rootArray);

			FilteredTypesSelectionDialog dialog = new FilteredTypesSelectionDialog(
					getShell(), false, getWizard().getContainer(), scope,
					IJavaSearchConstants.TYPE);
			dialog.setTitle("Class Selection");
			dialog.setMessage("Please Select a Class");
			// dialog.setInitialPattern(Signature.getSimpleName(getEnclosingTypeText()));

			if (dialog.open() == Window.OK) {
				String elementName = ((IType) dialog.getFirstResult())
						.getElementName();
				String packageName = ((IType) dialog.getFirstResult())
						.getPackageFragment().getElementName();
				String fullName = (packageName.length() > 0) ? packageName
						+ "." + elementName : elementName;
				className.setText(fullName);
			}
			return;
		}

	}
	
	private class ReturnTypeListener implements MouseListener{

	
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
				className.setText(fullName);
			}
			
		}
		
	}
	
	private class ParamAdder implements MouseListener
	{

	
		public void mouseDoubleClick(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}


		public void mouseDown(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		
		public void mouseUp(MouseEvent e) 
		{
			AddParamWizard apw = new AddParamWizard(getShell(),thispage);
			WizardDialog wd = new WizardDialog(getShell(),apw);
			wd.open();
		}
		
	}

}
