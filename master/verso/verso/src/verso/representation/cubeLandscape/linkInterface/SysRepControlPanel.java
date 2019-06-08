package verso.representation.cubeLandscape.linkInterface;

import javax.swing.JPanel;

import verso.representation.cubeLandscape.representationModel.SystemRepresentation;

public class SysRepControlPanel extends JPanel {
	protected SystemRepresentation currSysRep;
	
	public SysRepControlPanel(SystemRepresentation currSysRep) {
		super();
		this.currSysRep = currSysRep;
	}
	
    public SystemRepresentation getCurrSysRep() {
    	return this.currSysRep;
    }
    
    public void setCurrSysRep(SystemRepresentation currSysRep) {
    	this.currSysRep = currSysRep;
    } 
}
