package partition.ocl;

import java.util.ArrayList;

import org.eclipse.emf.ecore.resource.Resource;

public abstract class OCLTemplate {
	
	/*
	 * Faire une liste des templates ici en static ?
	 *   - les types de template sont hard-coded (inherits from this class)
	 *   
	 * Faire un Factory ?
	 *   - permet de repertorier les templates (hard-coded)
	 *   - les instantie, execute, manage...
	 *
	 */
	
	protected ArrayList<OCLMatch> matches = new ArrayList<>();
	public ArrayList<OCLMatch> getMatches() {
		return matches;
	}
	
	
	
	/**
	 * Parses the Utils.metamodelResource Ecore file and instantiates OCLInv s.
	 * @return
	 */
	public abstract ArrayList<OCLMatch> instantiate(Resource metamodelResource);
	
	
	
}
