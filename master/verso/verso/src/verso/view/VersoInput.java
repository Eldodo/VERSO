package verso.view;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import verso.representation.Lines.representationModel.ClassLineRepresentation;

public class VersoInput implements IEditorInput {

	ClassLineRepresentation clr = null;
	String project = "";

	public VersoInput() {

	}

	public VersoInput(String s) {
		project = s;
	}

	public String getProject() {
		return project;
	}

	public void setClassLineRepresentation(ClassLineRepresentation clr) {
		this.clr = clr;
	}

	public ClassLineRepresentation getClassLineRepresentation() {
		return this.clr;
	}

	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		if (clr != null)
			return clr.getClasse().getName();
		return "Verso Input";
	}

	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getToolTipText() {
		// TODO Auto-generated method stub
		return "Verso Input tooltip";
	}

	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

}
