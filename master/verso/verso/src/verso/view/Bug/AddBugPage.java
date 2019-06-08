package verso.view.Bug;

import java.util.ArrayList;
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
import verso.model.Element;
import verso.model.Entity;
import verso.model.Method;
import verso.model.Package;
import verso.model.SystemDef;

public class AddBugPage extends NewElementWizardPage{

	SystemDef mainSys = null;
	Text idName = null;
	Combo statusCombo = null;
	Text commentText = null;
	Text authorText = null;
	Text progText = null;
	org.eclipse.swt.widgets.List elemList = null;
	public AddBugPage(String name, SystemDef sys) {
		super(name);
		mainSys = sys;
		// TODO Auto-generated constructor stub
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
		
		// Number ID
		Label label = new Label(superComposite, SWT.LEFT);
		label.setText("Bug #id : ");
		idName = new Text(superComposite, SWT.LEFT);
		idName.setText("12345");
		GridData gd = new GridData();
		gd.widthHint = 400;
		idName.setLayoutData(gd);

		// Status
		/*
		Label labelStatus = new Label(superComposite, SWT.LEFT);
		labelStatus.setText("Status : ");
		GridData gdc = new GridData();
		gdc.widthHint = 400;
		statusCombo = new Combo(superComposite, SWT.LEFT);
		statusCombo.add("true");statusCombo.add("false");
		*/

		// Comment
		Label labelComment = new Label(superComposite, SWT.LEFT);
		labelComment.setText("Comment : ");

		commentText = new Text(
				superComposite,
		        SWT.WRAP
		          | SWT.MULTI
		          | SWT.BORDER
		          | SWT.H_SCROLL
		          | SWT.V_SCROLL);
		    GridData gridData =
		      new GridData(
		        GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		    gridData.heightHint = 400;
		    gridData.widthHint = 100;
		    gridData.horizontalSpan = 3;
		    gridData.grabExcessVerticalSpace = true;
		commentText.setText("enter your comment");
		commentText.setLayoutData(gridData);
		
		
		//Author
		Label labelAuthor = new Label(superComposite, SWT.LEFT);
		labelAuthor.setText("Author : ");
		GridData gdr2 = new GridData();
		gdr2.widthHint = 400;		
		authorText = new Text(superComposite, SWT.LEFT);
		commentText.setLayoutData(gdr2);
		
		//Programmer
		Label labelProg = new Label(superComposite, SWT.LEFT);
		labelProg.setText("Main Programmer : ");
		GridData gdr3 = new GridData();
		gdr3.widthHint = 400;		
		progText = new Text(superComposite, SWT.LEFT);
		commentText.setLayoutData(gdr3);
		
		//Elements 
		Label params = new Label(superComposite, SWT.LEFT);
		params.setText("Elements : ");
		elemList = new org.eclipse.swt.widgets.List(superComposite,SWT.NONE);
		GridData gdp = new GridData();
		gdp.widthHint = 400;
		elemList.setLayoutData(gdp);
		Button addElementButton = new Button(superComposite, SWT.NONE);
		addElementButton.setText("Add ...");
		addElementButton.addMouseListener(new ElementAdder());
		superComposite.pack();
	}
	
	public long getID()
	{
		return Long.parseLong(this.idName.getText());
	}
	
	public String getComment()
	{
		return commentText.getText().replaceAll("\r", " ").replaceAll("\n", " ").replaceAll(";", " ");
	}
	
	public String getAuthor()
	{
		return authorText.getText();
	}
	
	public String getProg()
	{
		return progText.getText();
	}
	
	public String getElemStr()
	{
		String elemStr = "";
		for (int i = 0; i < elemList.getItemCount(); i++)
		{
			elemStr += elemList.getItem(i) + ":";
		}
		if (elemStr.length() > 0)
		{
			elemStr = elemStr.substring(0, elemStr.length()-1);
		}
		return elemStr;
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
				e = this.mainSys.getElement(elemRep);
				if (e != null)
				{
					toReturn.add(e);
				}
			}
			if (elemRep.startsWith("p_"))
			{
				Package p = null;
				elemRep = elemRep.substring(2);
				p = this.mainSys.getPackage(elemRep);
				if (p != null)
				{
					toReturn.add(p);
				}
			}
			if (elemRep.startsWith("m_"))
			{
				Method m = null;
				elemRep = elemRep.substring(2);
				m = this.mainSys.getMethod(elemRep);
				if (m != null)
				{
					toReturn.add(m);
				}
			}
			
				
		}
		return toReturn;
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
			TypeFinderUtil.setProjectName(mainSys.getName());
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
}
