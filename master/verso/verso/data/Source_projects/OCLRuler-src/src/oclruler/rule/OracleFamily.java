package oclruler.rule;

import java.io.File;

import oclruler.genetics.Oracle;
import oclruler.metamodel.Concept;
import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.Metamodel;
import oclruler.rule.patterns.A14_ReferenceDifferentFromSelf;
import oclruler.rule.patterns.A1_AcyclicReference;
import oclruler.rule.struct.Constraint;
import oclruler.rule.struct.Node_DEFAULT;
import oclruler.utils.Config;

/**
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class OracleFamily extends Oracle {
	
	String textRulesFileName = Config.getOraclesDirectory().getAbsolutePath()+File.separator+"Family.cpl";
	
	public OracleFamily(ExampleSet ms) throws IllegalArgumentException {
		super(ms);
	}


	
	
	@Override
	protected void buildPatterns(){
		Concept P = Metamodel.getConcept("P");
//		Concept M = Metamodel.getConcept("M");
//		Concept F = Metamodel.getConcept("F");
		
		addConstraint(new Constraint(new Node_DEFAULT(null, new A1_AcyclicReference(P, P.getReference("k")))));
		addConstraint(new Constraint(new Node_DEFAULT(null, new A1_AcyclicReference(P, P.getReference("d")))));
		addConstraint(new Constraint(new Node_DEFAULT(null, new A1_AcyclicReference(P, P.getReference("m")))));
		
		addConstraint(new Constraint(new Node_DEFAULT(null, new A14_ReferenceDifferentFromSelf(P, P.getReference("d")))));
		addConstraint(new Constraint(new Node_DEFAULT(null, new A14_ReferenceDifferentFromSelf(P, P.getReference("m")))));
	}
}
