package verso.view;

import java.awt.Frame;

import javax.swing.JApplet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import verso.representation.Lines.Fenetre;
//import verso.representation.Lines.FenetrePan;
import verso.representation.Lines.representationModel.ClassLineRepresentation;

public class LineViewerEditor extends EditorPart{

	private ClassLineRepresentation clr = null;
	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		VersoInput in = (VersoInput)input;
		System.out.println("init LineEditor");
		this.setSite(site);
		this.setInput(input);
		this.setTitleToolTip(null);
		
		System.out.println(this.getTitleToolTip());
		this.setPartName("VersoEditor : " + in.getName());
		this.clr = in.getClassLineRepresentation();
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createPartControl(Composite parent) 
	{
		Composite swtawtComposite = new Composite(parent,SWT.EMBEDDED);
		Frame f = SWT_AWT.new_Frame(swtawtComposite);
		JApplet ja = new JApplet();
		f.add(ja);
		//IEditorInput iei = this.getEditorInput();
		Fenetre p = new Fenetre(this.clr);
		//Fenetre p = new Fenetre();
		p.setClass(this.clr);
		ja.add(p);
		
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

}
