package oclruler.rule;

import java.io.File;
import java.util.ArrayList;

import oclruler.genetics.Oracle;
import oclruler.metamodel.Concept;
import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.Reference;
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
public class OracleCD extends Oracle {
	
	
	public OracleCD(ExampleSet ms) throws IllegalArgumentException {
		super(ms);
	}


	
	
	@Override
	protected void buildPatterns(){
		Concept Cclassifier = 		Metamodel.getConcept("Classifier");
		@SuppressWarnings("unused")
		boolean ok = (Cclassifier == null);
		Concept Cinterface = 		Metamodel.getConcept("Interface");
		ok &= (Cinterface == null);	
		Concept Cclass = 			Metamodel.getConcept("Class");
		ok &= (Cclass == null);
		Concept Cgeneralization = 	Metamodel.getConcept("Generalization");
		ok &= (Cgeneralization == null);
		
		Reference RInterface_nest =Cinterface.getReference( "nestedClassifier");
		ok &= (RInterface_nest == null);
		Reference RClass_nest = 	Cclass.getReference( "nestedClassifier");
		ok &= (RClass_nest == null);
		
		Reference Rgeneral = 		Cgeneralization.getReference( "general" );
		ok &= (Rgeneral == null);
		Reference Rgenerals = 		Cclassifier.getReference( "ownedElement");
		ok &= (Rgenerals == null);
		
		
		
//		addPattern(new A15_ReferenceIsTypeOf(P, F, P.getReference("m")));

		for (File f : textRules) {
			ArrayList<A0_RawText> a0s = ProgramLoader.loadConstraintsFromFile(f);
			for (A0_RawText a0 : a0s) {
				Node_DEFAULT n = (Node_DEFAULT)NodeFactory.createEmptyNode(null, a0.getContext(), Type.DEFAULT);
				Constraint cst = new Constraint(n);
				boolean b = addConstraint(cst);
				if(!b)
					LOGGER.severe("Pattern not included : '"+f.getAbsolutePath()+"'");
			}
		}
		
			

		
	}
}
