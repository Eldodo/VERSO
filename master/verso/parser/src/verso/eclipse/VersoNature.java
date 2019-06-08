package verso.eclipse;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class VersoNature implements IProjectNature{

	private IProject proj = null;
	
	public void configure() throws CoreException {
		final String BUILDER_ID = "SimpleVersoParser.versoBuilder";
		   IProjectDescription desc = this.getProject().getDescription();
		   ICommand[] commands = desc.getBuildSpec();
		   boolean found = false;

		   for (int i = 0; i < commands.length; ++i) {
		      if (commands[i].getBuilderName().equals(BUILDER_ID)) {
		         found = true;
		         break;
		      }
		   }
		   if (!found) { 
		      //add builder to project
		      ICommand command = desc.newCommand();
		      command.setBuilderName(BUILDER_ID);
		      ICommand[] newCommands = new ICommand[commands.length + 1];

		      // Add it after other builders.
		      System.arraycopy(commands, 0, newCommands, 0, commands.length);
		      newCommands[newCommands.length-1] = command;
		      desc.setBuildSpec(newCommands);
		      this.getProject().setDescription(desc, null);
		   }

		
	}

	public void deconfigure() throws CoreException {
		// TODO Auto-generated method stub
		
	}

	public IProject getProject() {
		
		return proj;
	}

	public void setProject(IProject project) {
		this.proj = project;
		
	}

}
