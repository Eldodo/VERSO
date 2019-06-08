package oclruler.utils;
import java.util.logging.Logger;

import oclruler.genetics.Evaluator;
import oclruler.genetics.EvaluatorOCL;
import oclruler.genetics.FitnessVector;
import oclruler.genetics.Oracle;
import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.FireMap;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.Model;
import oclruler.metamodel.ocl.OCL_Comparator.COMPARATOR;
import oclruler.rule.Program;
import oclruler.rule.patterns.A8_CollectionsSize;
import oclruler.rule.struct.Constraint;
import oclruler.rule.struct.Node_DEFAULT;

public class OracleTool {
	public final static Logger LOGGER = Logger.getLogger(OracleTool.class.getName());
	
	public static void main(String[] args) {
		LOGGER.info("Entering OCL_Ruler - OracleTool");
		ToolBox.init();
		
		
//		for (Model model : ms.getModels()) {
//			System.out.println(model.getFileName()+ "\t -> "+res.getFires(model)+ " fires");
//		}
		//Check how many rules from the oracle are fired
		//Froeach model,
		//  -> Which rule (PatternKind)
		//  -> How many times
		
		//Order models on their fire count.
		
	}


	public static void testOnFitness(String[] args) {
		ToolBox.init();
		Oracle o = Oracle.instantiateOracle(ExampleSet.getInstance());
		System.out.println(o.getOCL());
		Evaluator eva = new EvaluatorOCL(ExampleSet.getInstance());
		
		
		Constraint cst1 = new Constraint(new Node_DEFAULT(null, new A8_CollectionsSize(Metamodel.getConcept("Final").getReference("outgoing"), COMPARATOR.EQ, 0)));
		Constraint cst2 = new Constraint(new Node_DEFAULT(null, new A8_CollectionsSize(Metamodel.getConcept("Fork").getReference("outgoing"), COMPARATOR.LT, 1)));
		System.out.println(cst1.getRoot());
		System.out.println(cst2.getRoot());
		Program prg = new Program();
		Program prg2 = new Program();
		prg.addConstraint(cst1);
		
		prg2.addConstraint(cst2);
		for (Model m : ExampleSet.getInstance().getAllExamples()) {
			FireMap fm = EvaluatorOCL.execute(null, m, prg);
			FireMap fm2 = EvaluatorOCL.execute(null, m, prg2);
			;
			boolean res = fm.getNumberOfFires(m) == 0;
			boolean res2 = fm2.getNumberOfFires(m) == 0;
			System.out.println(m.getFileName() + " : ("+m.isValid()+") "+res+ " | "+res2+" ");
		}
		
		
		 System.out.println("p.getFires : "+prg.getConstraint(0).getFires());
		
		
		
		System.out.println();
		System.out.println();
		printFitness_test(cst1);
		
		FitnessVector fv = eva.evaluate(prg);
		System.out.println("fv: "+fv);
		System.out.println(prg.getFitnessVector());
		System.out.println("\n\n");
		printFitness_test(cst2);
		eva.evaluate(prg2);
		System.out.println(prg2.getFitnessVector());
	}

	private static void printFitness_test(Constraint p) {
		int pp =0 , np=0, pn=0, nn=0;
		Program prgx = new Program();
		prgx.addConstraint(p);
		for (Model m : ExampleSet.getInstance().getAllExamples()) {
			FireMap fm = EvaluatorOCL.execute(null, m, prgx);
			boolean oValid = m.isValid();
			boolean prgValid = fm.getNumberOfFires(m) == 0;
			if (oValid) {
				if (!prgValid){
					np++; // Oracle +, Prg -
					System.out.println(m.getFileName()+ ">np : "+ oValid + " | "+prgValid);
				}else{
					pp++; // Oracle + / Prg +
					System.out.println(m.getFileName()+ ">pp : "+ oValid + " | "+prgValid);
				}
			} else {
				if (!prgValid){
					nn++; // Oracle -, Prg -
					System.out.println(m.getFileName()+ ">nn : "+ oValid + " | "+prgValid);
				} else{
					pn++; // Oracle -, Prg +
					System.out.println(m.getFileName()+ ">pn : "+ oValid + " | "+prgValid);
				}
			}
		}
		
		System.out.println("PP : "+pp);
		System.out.println("PN : "+pn);
		System.out.println("NP : "+np);
		System.out.println("NN : "+nn);
		
		float x =0, y=0;
		
		x = ((float)pp)/ (float)(pp+np);//True positive rate
		y = ((float)nn)/ (float)(nn+pn);//True negative rate
		
		
		
		
		x = (float) getMonoValue_test(x, y); //Monovalue uses values[0,1]
		y = 1;

		System.out.println("x : " + x);
		System.out.println("y : " + y);
	}
	
	private static  double getMonoValue_test(float x, float y) {
		if(x < 0){
			LOGGER.warning("Access mono computation before initialization !");
			return 0.0;
		}
		switch (FitnessVector.OBJECTIVES_CONSIDERED) {
		case 3:
			return (x*ExampleSet.POSITIVES_CONSIDERED+y*ExampleSet.NEGATIVES_CONSIDERED)
					/(ExampleSet.POSITIVES_CONSIDERED+ExampleSet.NEGATIVES_CONSIDERED);
		case 2:
		case 1:
			return (x+y)/(2);
		default:
			LOGGER.severe("OBJECTIVES_CONSIDERED out of bounds. Must be 1, 2 or 3.");
			break;
		}
		return (x+y+10000000)/(3);
	}

}
