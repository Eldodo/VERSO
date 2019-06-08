package verso.view.Bug;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.wizards.NewElementWizardPage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import verso.util.TypeFinderUtil;
import verso.model.Bug;
import verso.model.Element;
import verso.model.Entity;
import verso.model.Method;
import verso.model.Package;
import verso.model.SystemDef;

public class ModifySelectBugPage extends NewElementWizardPage{

	org.eclipse.swt.widgets.List bugList = null;
	SystemDef sys = null;
	Combo comboStatus = null;
	Text textComment = null;
	Text textProg = null;
	org.eclipse.swt.widgets.List elemList = null;
	
	public ModifySelectBugPage(String name, SystemDef sys) {
		super(name);
		this.sys = sys;
		// TODO Auto-generated constructor stub
	}
	public List<Entity> getElemLst()
	{
		List<Entity> toReturn = new ArrayList<Entity>();
		for (int i = 0; i < elemList.getItemCount(); i++)
		{
			String elemRep = elemList.getItem(i);
			
			if (elemRep.startsWith("e_"))
			{
				Element e = null;
				elemRep = elemRep.substring(2);
				e = this.sys.getElement(elemRep);
				if (e != null)
				{
					toReturn.add(e);
				}
			}
			if (elemRep.startsWith("p_"))
			{
				Package p = null;
				elemRep = elemRep.substring(2);
				p = this.sys.getPackage(elemRep);
				if (p != null)
				{
					toReturn.add(p);
				}
			}
			if (elemRep.startsWith("m_"))
			{
				Method m = null;
				elemRep = elemRep.substring(2);
				m = this.sys.getMethod(elemRep);
				if (m != null)
				{
					toReturn.add(m);
				}
			}
			
				
		}
		return toReturn;
	}
	
	public void createControl(Composite parent) {
		
		
		this.setControl(parent);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		parent.setLayout(gridLayout);
		Composite superComposite = new Composite(parent,SWT.NONE);
		GridLayout gridInnerLayout = new GridLayout();
		gridInnerLayout.numColumns = 2;
		superComposite.setLayout(gridInnerLayout);


		//
		Label label = new Label(superComposite, SWT.LEFT);
		label.setText("Bug");
		bugList = new org.eclipse.swt.widgets.List(superComposite,SWT.NONE);
		
		GridData gdp3 = new GridData();
		gdp3.widthHint = 400;
		
		
		for (Bug b : sys.getAllBugs())
		{
			bugList.add("" + b.getNumberID());
		}
		bugList.setLayoutData(gdp3);
		bugList.addSelectionListener(new BugSelectionListener());
		
		//InfoBug
		Label statusLabel = new Label(superComposite, SWT.LEFT);
		statusLabel.setText("Open Status");
		comboStatus = new Combo(superComposite, SWT.NONE);
		comboStatus.add("true"); comboStatus.add("false");
		//comboStatus.setLayoutData(gdp);
		
		GridData gdp = new GridData();
		gdp.widthHint = 400;
		Label labelComment = new Label(superComposite, SWT.LEFT);
		labelComment.setText("Comment");
		textComment = new Text(superComposite,SWT.MULTI | SWT.LEFT);
		textComment.setLayoutData(gdp);
		
		Label labelProg = new Label(superComposite, SWT.LEFT);
		labelProg.setText("Main Programmer");
		textProg = new Text(superComposite, SWT.LEFT);
		textProg.setLayoutData(gdp);
		
		Label params = new Label(superComposite, SWT.LEFT);
		params.setText("Elements : ");
		elemList = new org.eclipse.swt.widgets.List(superComposite,SWT.NONE);
		//elemList.addSelectionListener(new BugSelectionListener());
		//elemList.addMouseListener(new BugSelectionListener());
		GridData gdp1 = new GridData();
		gdp1.widthHint = 400;
		elemList.setLayoutData(gdp1);
		
		
		Button addElementButton = new Button(superComposite, SWT.NONE);
		addElementButton.setText("Add ...");
		addElementButton.addMouseListener(new ElementAdder());
		Button removeElementButton = new Button(superComposite, SWT.NONE);
		removeElementButton.setText("Remove ...");
		removeElementButton.addMouseListener(new ElementRemover());
		Button buttonValidate = new Button(superComposite, SWT.NONE);
		buttonValidate.setText("Validate ...");
		buttonValidate.addMouseListener(new BugValidator());
		
	}
	
	
	private class ElementRemover implements MouseListener
	{

		public void mouseDoubleClick(MouseEvent e) {
		}

		public void mouseDown(MouseEvent e) {
		}

		
		public void mouseUp(MouseEvent e) {
			elemList.remove(elemList.getSelectionIndex());
			elemList.update();
		}	
	}
	private class ElementAdder implements MouseListener
	{

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseDown(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseUp(MouseEvent e) {
			TypeFinderUtil.setProjectName(sys.getName());
			IPackageFragmentRoot[] rootArray = TypeFinderUtil.getSourceFragmentRoot(false);
			
			IJavaSearchScope scope = SearchEngine
					.createJavaSearchScope(rootArray);
			/*
			FilteredItemsSelectionDialog dialog = new FilteredItemsSelectionDialog(
					getShell(), false, getWizard().getContainer(), scope,
					IJavaSearchConstants.TYPE);
			*/
			
			CheckedTreeSelectionDialog dialog = new CheckedTreeSelectionDialog(
					getShell(),new WorkbenchLabelProvider(), new BaseWorkbenchContentProvider());
			dialog.setInput(rootArray[0]);
			
			dialog.setTitle("Element Selection");
			dialog.setMessage("Please Select a Class");
			
			if (dialog.open() == Window.OK) {
				for (Object o : dialog.getResult())
				{
					if (o instanceof IType)
					{
						String elementName = ((IType)o)
						.getElementName();
						String packageName = ((IType) o)
						.getPackageFragment().getElementName();
						String fullName = (packageName.length() > 0) ? packageName
						+ "." + elementName : elementName;
						elemList.add("e_" + fullName);
					}
					if (o instanceof IPackageFragment)
					{
						String elementName = ((IPackageFragment)o)
						.getElementName();
						elemList.add("p_" + elementName);
					}
					/*
					if (o  instanceof IMethod)
					{
						IMethod m = ((IMethod)o);
						//elemList.add("m_" + elementName);
					
						String methodRepresentant = "";
						methodRepresentant = m.getDeclaringType().getFullyQualifiedName() + "." + m.getElementName() + "(";
						ITypeParameter[] params = null;
						try{
							params = m.getTypeParameters();
						}catch(Exception ex){System.out.println(ex);}
						for (int i = 0; i < params.length;i++)
						{
							methodRepresentant += params[i].getElementName();
							params[i].getElementName();
							if (i < params.length -1)
								methodRepresentant += ",";
						}
						methodRepresentant += ")";
						elemList.add("m_" + methodRepresentant);
					}
					*/
				}
			}	
		}		
	}
	
	private class BugValidator implements MouseListener
	{
		public void mouseDoubleClick(MouseEvent e) {}

		public void mouseDown(MouseEvent e) {}

		public void mouseUp(MouseEvent e) 
		{
			String elemStr = bugList.getItem(bugList.getSelectionIndex());
			long numberID = Long.parseLong(elemStr);
			Bug b = sys.getBugMap().get(numberID);
			if (comboStatus.getText().compareTo("true") == 0)
			{
				b.setOpen(true);
			}
			else
			{
				if (b.isOpen())
				{
					b.setOpen(false);
					b.setClosingDate(new Date(System.currentTimeMillis()));
				}
			}
			b.setComment(textComment.getText().replaceAll("\r", " ").replaceAll("\n", " ").replaceAll(";", " "));
			b.setMainProg(textProg.getText());
			b.setEntities(getElemLst());
		}
	}
	
	private class BugSelectionListener implements SelectionListener,MouseListener
	{
		
		public void widgetDefaultSelected(SelectionEvent e) {
			/*
			System.out.println("Click");
			String elemStr = elemList.getItem(elemList.getSelectionIndex());
			long numberID = Long.parseLong(elemStr);
			Bug b = sys.getBugMap().get(numberID);
			if (b.isOpen())
			{
				comboStatus.setText("true");
			}
			else 
			{
				comboStatus.setText("false");
			}
			textComment.setText(b.getComment());
			textProg.setText(b.getMainProg());
			elemList.removeAll();
			for (Element elem : b.getEntities())
			{
				elemList.add(elem.getName());
			}
			*/
		}

		public void widgetSelected(SelectionEvent e) {
			System.out.println("Click");
			String elemStr = bugList.getItem(bugList.getSelectionIndex());
			long numberID = Long.parseLong(elemStr);
			Bug b = sys.getBugMap().get(numberID);
			if (b.isOpen())
			{
				comboStatus.setText("true");
			}
			else 
			{
				comboStatus.setText("false");
			}
			textComment.setText(b.getComment());
			textProg.setText(b.getMainProg());
			elemList.removeAll();
			for (Entity elem : b.getEntities())
			{
				if (elem instanceof Package)
					elemList.add("p_" + elem.getName());
				if (elem instanceof Element)
					elemList.add("e_" + elem.getName());
				if (elem instanceof Method)
					elemList.add("m_" + elem.getName());
			}
		}
		
		public void mouseDoubleClick(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void mouseDown(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void mouseUp(MouseEvent e) {
			System.out.println("click");
			
		}
	}
}
