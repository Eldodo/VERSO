package oclruler.rule;

import java.io.File;
import java.util.ArrayList;

import oclruler.genetics.Oracle;
import oclruler.metamodel.ExampleSet;
import oclruler.metrics.ProgramLoader;
import oclruler.rule.patterns.A0_RawText;
import oclruler.rule.struct.Constraint;
import oclruler.rule.struct.Node.Type;
import oclruler.rule.struct.NodeFactory;
import oclruler.rule.struct.Node_DEFAULT;

/**
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class OracleStatemachine extends Oracle {
	
	public OracleStatemachine(ExampleSet ms) throws IllegalArgumentException {
		super(ms);
	}

	@Override
	protected void buildPatterns(){
//		Concept P = Metamodel.getConcept("Statemachine");
//		Concept M = Metamodel.getConcept("Vertex");
//		Concept F = Metamodel.getConcept("Transition");
		
//		addPattern(new A15_ReferenceIsTypeOf(P, M, P.getReference("d")));
//		addPattern(new A15_ReferenceIsTypeOf(P, F, P.getReference("m")));

		for (File f : textRules) {
			ArrayList<A0_RawText> a0s = ProgramLoader.loadConstraintsFromFile(f);
			for (A0_RawText a0 : a0s) {
				Node_DEFAULT n = (Node_DEFAULT)NodeFactory.createEmptyNode(null, a0.getContext(), Type.DEFAULT);
				n.setPattern(a0);
				Constraint cst = new Constraint(n);
				cst.setName(a0.getFullName());
				boolean b = addConstraint(cst);
				if(!b)
					LOGGER.severe("Pattern not included : '"+f.getAbsolutePath()+"'");
			}
		}
	}
	
}
