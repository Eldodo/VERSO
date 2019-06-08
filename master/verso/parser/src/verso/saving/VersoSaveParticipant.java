package verso.saving;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.runtime.CoreException;

import verso.model.SystemManager;

public class VersoSaveParticipant implements ISaveParticipant {

	//essaie
	public void doneSaving(ISaveContext context) {
		// TODO Auto-generated method stub
		
	}


	public void prepareToSave(ISaveContext context) throws CoreException {
		// TODO Auto-generated method stub
		
	}


	public void rollback(ISaveContext context) {
		// TODO Auto-generated method stub
		
	}

	public void saving(ISaveContext context) throws CoreException {
		for (String sys : SystemManager.getProjects())
		{
			VersoProjectSaver.save(sys);
		}
		
	}

}
