package oclruler.metamodel;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;

public class InvalidModelException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public InvalidModelException(String string) {
		super(string);
	}
	EList<Diagnostic> errors, warnings;
	public InvalidModelException(EList<Diagnostic> errors, EList<Diagnostic> warnings) {
		this.errors = errors;
		this.warnings = warnings;
	}

	
	@Override
	public String getMessage() {
		String res = super.getMessage()!= null ? super.getMessage()+"\n":"InvalidModelException : \n";
		if(errors!= null){
			for (Diagnostic diagnostic : errors) {
				res += " -E- "+diagnostic.getMessage()+"\n";
			}
		}
		if(warnings!= null){
			for (Diagnostic diagnostic : warnings) {
				res += " -W- "+diagnostic.getMessage()+"\n";
			}
		}
		return res;
	}
	
	@Override
	public String toString() {
		return getMessage();
	}
}
