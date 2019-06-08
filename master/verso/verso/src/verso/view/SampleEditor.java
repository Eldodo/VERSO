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

import verso.representation.cubeLandscape.PluginScene;

public class SampleEditor extends EditorPart{

	VersoInput vi = null;
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
		try {
			vi = (VersoInput) input;
		}catch(Exception e)
		{
			vi = new VersoInput("Test");
		}
		this.setSite(site);
		this.setInput(new VersoInput());
		this.setTitleToolTip(null);
		this.setTitleToolTip("allo");
		
		System.out.println(this.getTitleToolTip());
		this.setPartName("VersoEditor");
		// TODO Auto-generated method stub
		
	}
	
	public String getTitleToolTip()
	{
		return "VersoEditor";
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
			PluginScene sce = PluginScene.otherMain(vi.getProject());
			ja.add(sce.getContainner());
			sce.setShell(parent.getShell());
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

}
