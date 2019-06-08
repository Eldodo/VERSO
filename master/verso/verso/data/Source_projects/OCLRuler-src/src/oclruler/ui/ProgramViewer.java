package oclruler.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import oclruler.genetics.Evolutioner;
import oclruler.genetics.Oracle;
import oclruler.metamodel.Model;
import oclruler.rule.Program;

public class ProgramViewer extends JPanel {
	private static final long serialVersionUID = -2282998802853313235L;

	Program program;

	OCLTextArea jTextOCL;

	SolutionExplorer solutionExplorer;

	JButton addPositive, addNegative, addRandom;

	Font font = new Font("Courier", Font.PLAIN, 12);

	private Evolutioner evolutioner;

	public ProgramViewer() {
		super(new BorderLayout());
		setBorder(new TitledBorder("OCL - Pick a program"));

		JSplitPane centerPane = new JSplitPane();
		centerPane.setOneTouchExpandable(false);
		centerPane.setContinuousLayout(true);
		centerPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		centerPane.setDividerSize(3);
		centerPane.setDividerLocation(250);

		jTextOCL = new OCLTextArea();
		jTextOCL.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)){
					toggleProgramOracle();
				}
			}
		});
		solutionExplorer = new SolutionExplorer();

		JScrollPane scrollOCL = new JScrollPane(jTextOCL, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		centerPane.setLeftComponent(scrollOCL);
		centerPane.setRightComponent(solutionExplorer);

		add(centerPane, BorderLayout.CENTER);
		add(initializeButtons(), BorderLayout.SOUTH);
		
	}

	boolean isProgramShown = true;

	protected void toggleProgramOracle() {
		String itStr = iteration >0 ? " (at " + iteration + " g.)":"";
		if (isProgramShown) {
			setTitle(Oracle.getInstance().getName()+itStr);
			jTextOCL.setProgram(Oracle.getInstance());
			isProgramShown = false;
		} else {
			if (program != null) {
				jTextOCL.setProgram(program);
				setTitle(program.getName()+itStr);
				isProgramShown = true;
			} else {
				isProgramShown = false;
			}
		}
	}

	private JPanel initializeButtons() {
		addPositive = new JButton("+");
		addNegative = new JButton("-");
		addRandom = new JButton("+/-");

		addPositive.setEnabled(false);
		addNegative.setEnabled(false);
		addRandom.setEnabled(false);

		addPositive.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (evolutioner.getModelSet().injectionPositivePossible())
					evolutioner.injectPositiveExample();
				addPositive.setEnabled(evolutioner.getModelSet().injectionPositivePossible());
				addRandom.setEnabled(evolutioner.getModelSet().injectionPossible());
			}
		});
		addNegative.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (evolutioner.getModelSet().injectionPositivePossible())
					evolutioner.injectNegativeExample();
				addNegative.setEnabled(evolutioner.getModelSet().injectionNegativePossible());
				addRandom.setEnabled(evolutioner.getModelSet().injectionPossible());
			}
		});
		addRandom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (evolutioner.getModelSet().injectionPossible())
					evolutioner.injectExample();
				addRandom.setEnabled(evolutioner.getModelSet().injectionPossible());
				addPositive.setEnabled(evolutioner.getModelSet().injectionPositivePossible());
				addNegative.setEnabled(evolutioner.getModelSet().injectionNegativePossible());
			}
		});

		JPanel jp = new JPanel(new BorderLayout(2, 2));
		jp.add(addPositive, BorderLayout.WEST);
		jp.add(addRandom, BorderLayout.CENTER);
		jp.add(addNegative, BorderLayout.EAST);
		return jp;
	}
	
	
	int iteration = -1;
	public void setProgram(Program program, Model m, int idxInPop, int iterationInEvolution) {
		this.program = program;
		this.iteration = iterationInEvolution;
		this.solutionExplorer.setSelectedModel(m);
		this.solutionExplorer.setSelectedProgram(program);
		
		setTitle(program.getName() + " (at " + iteration + " g.)");
		jTextOCL.setText(program.printOCL());
		jTextOCL.setCaretPosition(0);
		isProgramShown = program == Oracle.getInstance();
		updateUI();
	}

	public void setTitle(String title) {
		((TitledBorder)getBorder()).setTitle(title);
		updateUI();
	}
	

	public void setEvolutioner(Evolutioner evo) {
		this.evolutioner = evo;
		addPositive.setEnabled(evolutioner.getModelSet().injectionPositivePossible());
		addNegative.setEnabled(evolutioner.getModelSet().injectionNegativePossible());
		addRandom.setEnabled(evolutioner.getModelSet().injectionPossible());
	}

}
