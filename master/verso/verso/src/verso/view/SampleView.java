package verso.view;

import java.awt.Frame;

import javax.swing.JApplet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import verso.representation.cubeLandscape.PluginScene;

public class SampleView extends ViewPart{
	 Label label;
	 Canvas canvas;

	@Override
	public void createPartControl(Composite parent) {
		Composite swtawtComposite = new Composite(parent,SWT.EMBEDDED);
		Frame f = SWT_AWT.new_Frame(swtawtComposite);
		JApplet ja = new JApplet();
		f.add(ja);
		PluginScene sce = PluginScene.otherMain("Test");
		ja.add(sce.getContainner());
		sce.setShell(parent.getShell());
		
		/*
		System.out.println("adding ...");
		EmptyWizard wiz = new EmptyWizard();
		NewClassWizardPage ncwp = new NewClassWizardPage();
		
		wiz.addPage(ncwp);
		WizardDialog wd = new WizardDialog(parent.getShell(),wiz);
		
		wd.open();
		*/
		/*
		System.out.println("Opennin the view ...");
        label.setText("Loading other window ...");
        */
        //PluginScene.main(new String[0]);
        
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

}
