package oclruler.ui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.Model;
import partitioner.partition.composition.ModelFragment;

public class FragmentsPane extends JPanel {
	private static final long serialVersionUID = 1L;
	
	JTextArea textArea = new JTextArea();

	Model m;
	
	private ArrayList<ModelFragment> fragmentsExSet;
	HashSet<ModelFragment> uncoveredsExSet;
	double covExSet;
	int sizeCheker = -1;
	
	
	private ArrayList<ModelFragment> fragments;
	HashSet<ModelFragment> uncovereds;
	HashSet<ModelFragment> uncoveredsDiff;
	double cov;

	public FragmentsPane() {
		super(new BorderLayout());
		textArea = new JTextArea();
		add(new JScrollPane(textArea), BorderLayout.CENTER);

		updateExampleSetCoverage();
	}

	public void updateExampleSetCoverage() {
		fragmentsExSet = new ArrayList<>();
		uncoveredsExSet = new HashSet<>();
		
		sizeCheker = ExampleSet.getExamplesBeingUsed().size();
		covExSet = Metamodel.fragmentSet.evaluateCoverage(ExampleSet.getExamplesBeingUsed());

		for (ModelFragment modelFragment : Metamodel.fragmentSet.getFragments())
			fragmentsExSet.add(modelFragment);
		for (ModelFragment modelFragment : Metamodel.fragmentSet.getUncovereds()){
			uncoveredsExSet.add(modelFragment);
		}
	}

	public void setModel(Model m) {
		this.m = m;
		fragments = new ArrayList<>();
		uncovereds = new HashSet<>();
		uncoveredsDiff = new HashSet<>();

		cov = Metamodel.fragmentSet.evaluateCoverage(m);

		for (ModelFragment mf : Metamodel.fragmentSet.getFragments())
			fragments.add(mf);
		for (ModelFragment mf : Metamodel.fragmentSet.getUncovereds()){
			uncovereds.add(mf);
			if(!uncoveredsExSet.contains(mf))
				uncoveredsDiff.add(mf);
		}
		
		updateText();
	}

	private void updateText() {
		if(ExampleSet.getExamplesBeingUsed().size() != sizeCheker)
			updateExampleSetCoverage();
		String text = "COV:" + cov + "\n";
		text += "\nUncovereds ("+uncovereds.size()+"/"+fragments.size()+"):\n";
		for (ModelFragment mf : uncovereds)
			text += " - " + mf.prettyPrint() + (!uncoveredsExSet.contains(mf)?"*":"")+"\n";
		text += "\nDiff ("+uncoveredsDiff.size()+"):\n";
		for (ModelFragment mf : uncoveredsDiff)
			text += " - " + mf.prettyPrint() + "\n";
//		text += "\nFragments ("+fragments.size()+"):\n";
//		for (ModelFragment mf : fragments)
//			text += " - " + mf.prettyPrint() + "\n";
		
//		System.out.println(text);
		textArea.setText(text);
		repaint();
	}

}
