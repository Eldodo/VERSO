package oclruler.ui;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import oclruler.genetics.Oracle;
import oclruler.metamodel.Model;
import oclruler.rule.Program;

public class SolutionExplorer extends JPanel {
	private static final long serialVersionUID = -501527748217750865L;

	TitledBorder border ;
	OCLTextArea firesDetails;
	ExampleButtonList scrollButtons ;

	Program selectedProgram;
	Model selectedModel;
	
	

	@SuppressWarnings("serial")
	public SolutionExplorer() {
		super(new BorderLayout());
		
//		details = new JTextPane();
//		details.setEditable(false);
//		details.setContentType("text/html");
		firesDetails = new OCLTextArea();
		
		
		firesDetails.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(SwingUtilities.isRightMouseButton(e))
					toggleOracleProgramDetails();
			}
		});
			
		
		scrollButtons = new ExampleButtonList() {
			@Override
			public void selectModel(Model m) {
				setSelectedModel(m);
			}
		};
		
		JScrollPane scrollDetails = new JScrollPane(firesDetails);
		scrollDetails.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollDetails.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollDetails.setBorder(border = BorderFactory.createTitledBorder("Fires"));
		
		add(scrollButtons, BorderLayout.WEST);
		add(scrollDetails, BorderLayout.CENTER);
	}
	
	boolean isProgramShown = false;
	protected void toggleOracleProgramDetails() {
		if(isProgramShown){
			setOracleDetails(selectedModel);
			isProgramShown = false;
		} else {
			setProgramDetails(selectedProgram, selectedModel);
		}
	}

	public void setOracleDetails(Model m){
		if(m != null){
			border.setTitleColor(m.isValid()?ExampleSetExplorer.colorValid:ExampleSetExplorer.colorInvalid);
			border.setTitle("Fires : "+m.getName()+" x "+ Oracle.getInstance().getName());
			firesDetails.setProgram(Oracle.getInstance(), m);
			updateUI();
		} else {
			clearProgramDetails();
		}
	}
	
	public void setProgramDetails(Program prg, Model m){
		if(prg != null && m != null){
			border.setTitleColor(m.isValid()?ExampleSetExplorer.colorValid:ExampleSetExplorer.colorInvalid);
			border.setTitle("Fires : "+m.getName()+" x "+selectedProgram.getName() +"   "+ selectedProgram.printQualification(m));
			firesDetails.setProgram(prg, m);
			updateUI();
			isProgramShown = true;
		} else {
			clearProgramDetails();
		}
	}
	public void clearProgramDetails(){
		border.setTitle("Fires");
		firesDetails.setProgram(null);	
	}
	
		
	public void setSelectedProgram(Program program) {
		this.selectedProgram = program;
		scrollButtons.setProgram(program);
		setProgramDetails(program, selectedModel);
		scrollButtons.updateButtonsUI(selectedModel);
	}

	public void setSelectedModel(Model m) {
		selectedModel = m;
		setProgramDetails(selectedProgram, m);
		scrollButtons.updateButtonsUI(selectedModel);
	}
	
	
}
