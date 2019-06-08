package oclruler.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;

import oclruler.genetics.Oracle;
import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.Model;
import oclruler.rule.Program;

public abstract class ExampleButtonList extends JScrollPane {
	private static final long serialVersionUID = 3309897545740286461L;
	
	TitledBorder border; 
	
	ExampleButton[] buttons;
	/**
	 * If a OCL program is selected, it is used to color button (greenish for positives, redish for negatives)
	 */
	private Program program;
	
	
	JPanel buttonsPane;
	
	public ExampleButtonList() {
		buttons = new ExampleButton[ExampleSet.getInstance().getAllExamples().size()];
		
		updateButtons();
		
		setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//		scrollButtons.setSize(new Dimension(100,300));
		border = BorderFactory.createTitledBorder("Examples"); 
		setBorder(border);
	}
	
	public void setTitle(String title) {
		border.setTitle(title);
	}
	

	public void updateButtons() {
		buttonsPane = new JPanel(new GridLayout(ExampleSet.getInstance().getAllExamples().size(), 1));
		ArrayList<Model> ms1 = new ArrayList<>(ExampleSet.getInstance().getAllPositives());
		ArrayList<Model> ms2 = new ArrayList<>(ExampleSet.getInstance().getAllNegatives());
		ExampleSet.sortModelListByModelValidity(ms1);
		ExampleSet.sortModelListByModelValidity(ms2);
		ArrayList<Model> ms = new ArrayList<>();
		ms.addAll(ms1);
		ms.addAll(ms2);
		int i = 0;
		for (Model m : ms) {
			ExampleButton jb = new ExampleButton(m);
			jb.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					selectModel(m);
					updateButtonsUI(m);
				}
			});
			buttons[i++] = jb;
			buttonsPane.add(jb);
		}
		setViewportView(buttonsPane);
	}
	
	public abstract void selectModel(Model m) ;
	
	public void setProgram(Program program){
		this.program = program;
	}
	
	/**
	 * Updates ui with the specified selected model.
	 * @param selectedModel Model
	 */
	public void updateButtonsUI(Model selectedModel){
//		System.out.println("ExampleButtonList.updateButtons()"+selectedModel);
		for (ExampleButton exampleButton : buttons) {
			String color = exampleButton.m.isValid()?ExampleSetExplorer.colorValidStr:ExampleSetExplorer.colorInvalidStr;
			String isRight = "";
			String underline = " ";
			if(program != null && program.getFitnessVector() != null){
				HashMap<Model, Oracle.OracleCmp> cmps = program.getFitnessVector().getCmps();
				if (cmps.get(exampleButton.m) != null) {
					for (Model model : ExampleSet.getExamplesBeingUsed()) {
						if (model.equals(exampleButton.m)) {
							isRight = cmps.get(exampleButton.m).isRight() ? ExampleSetExplorer.colorRightStr : ExampleSetExplorer.colorWrongStr;
							break;
						}
					}
				}
			}
			
			if(exampleButton.m.equals(selectedModel))
				underline = "underline";
			exampleButton.setText("<p style=\"margin:0px; padding:0px; font-family: Courier, Helvetica, sans-serif; font-size:90%; background-color:"+isRight+"; text-decoration:"+underline+"; color:"+color+";\"> "  + exampleButton.m.getName() + "</span><br/>\n");
		}
	}
	
	public ExampleButton getButton(Model m) {
		for (ExampleButton exampleButton : buttons) {
			if(exampleButton.m.equals(m))
				return exampleButton;
		}
		return null;
	}
	
	class ExampleButton extends JTextPane {
		private static final long serialVersionUID = 1837037396452041066L;
		static final int height = 20;
		Model m;

		public ExampleButton(Model m) {
			super();
			
			setEditable(false);
			setContentType("text/html");
			// setSize(new Dimension(100,height));
			setMinimumSize(new Dimension(100, height));
			setPreferredSize(new Dimension(100, height));

			this.m = m;
			String color = m.isValid() ? ExampleSetExplorer.colorValidStr : ExampleSetExplorer.colorInvalidStr;
			setText("<p style=\"margin:0px; padding:0px;font-family: Courier, Helvetica, sans-serif; font-size:90%; color:" + color + ";\"> " + m.getName() + "</span><br/>\n");
			
//			for (ComponentListener cl : getComponentListeners()) {
//				removeComponentListener(cl);
//			}
//			for (ContainerListener cl : getContainerListeners()) {
//				removeContainerListener(cl);
//			}
//			for (PropertyChangeListener cl : getPropertyChangeListeners()) {
//				removePropertyChangeListener(cl);
//			}
//			for (CaretListener cl : getCaretListeners()) {
//				removeCaretListener(cl);
//			}
			
		}
		
		@Override
		public void setText(String t) {
			
			super.setText(t);
		}

	}


}
