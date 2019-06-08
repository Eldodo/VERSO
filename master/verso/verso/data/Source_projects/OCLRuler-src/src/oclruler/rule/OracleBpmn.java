package oclruler.rule;

import oclruler.genetics.Oracle;
import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.Metamodel;
import oclruler.rule.struct.Constraint;
import oclruler.rule.struct.Node_TRUE;
import oclruler.utils.ToolBox;

public class OracleBpmn extends Oracle {

	public OracleBpmn(ExampleSet ms) throws IllegalArgumentException {
		super(ms);
	}

	@Override
	protected void buildPatterns(){
//		Concept M = Metamodel.getConcept("M");
//		Concept F = Metamodel.getConcept("F");
		addConstraint(Constraint.createRandomConstraint());
		addConstraint(new Constraint(new Node_TRUE(null, ToolBox.getRandom(Metamodel.getAllConcepts().values()))));
		
	}

}
