package oclruler.a_test;

import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import oclruler.genetics.EvaluatorOCL;
import oclruler.genetics.UnstableStateException;
import oclruler.metamodel.Attribute;
import oclruler.metamodel.Concept;
import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.MMElement;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.Model;
import oclruler.metamodel.Reference;
import oclruler.rule.MMMatch;
import oclruler.rule.PatternFactory;
import oclruler.rule.PatternFactory.PatternType;
import oclruler.rule.Program;
import oclruler.rule.patterns.A10_OppositeReferencesOneToMany;
import oclruler.rule.patterns.A13_CollectionIncludesSelf;
import oclruler.rule.patterns.A15_ReferenceIsTypeOf;
import oclruler.rule.patterns.A1_AcyclicReference;
import oclruler.rule.patterns.A7_CollectionsSameSize;
import oclruler.rule.patterns.Pattern;
import oclruler.rule.struct.Constraint;
import oclruler.rule.struct.Node;
import oclruler.rule.struct.Node.DeepMutationType;
import oclruler.rule.struct.Node.Type;
import oclruler.rule.struct.NodeFactory;
import oclruler.rule.struct.Node_DEFAULT;
import oclruler.rule.struct.Node_FO;
import oclruler.rule.struct.Node_FO.ACTION;
import oclruler.utils.Config;
import oclruler.utils.ToolBox;

/**
 * Tests related to the package oclruler.'rule'
 * @author Batot
 *
 */
public class Test_rule {
	public final static Logger LOGGER = Logger.getLogger(Test_rule.class.getName());
	
	public static void main(String[] args)  {
		LOGGER.info("Entering OCLRuler - 'rule' package oclruler.Testing - FOs diffusion\n");
		ToolBox.init();
		
		double constraintMutationMissRate = 0.0, nodeDeepMutation = 0.0;
		int loops = 500, excp = 0;
		LOGGER.info("testPruning 1");
		for (int i = 0; i < loops; i++) 
			try {	testPruning1(Level.OFF);					} catch(Exception e){e.printStackTrace();excp++;}
		LOGGER.info("testPruning 2");
		for (int i = 0; i < loops; i++)
			try {	testPruning2(Level.OFF);					} catch(Exception e){e.printStackTrace();excp++;}
		LOGGER.info("testPruning 3");
		for (int i = 0; i < loops; i++)
			try {	testPruning3(Level.OFF);					} catch(Exception e){e.printStackTrace();excp++;}
		
		LOGGER.info("testNodeMutation");
		for (int i = 0; i < loops; i++)	
			try {	testNodeMutation(Level.OFF);				} catch(Exception e){e.printStackTrace();excp++;}
		
		LOGGER.info("testNodeDeepMutation");
		for (int i = 0; i < loops; i++)	
			try {					nodeDeepMutation += testNodeDeepMutation(Level.OFF, 100, 0.5);				} catch(Exception e){e.printStackTrace();excp++;}
		nodeDeepMutation = nodeDeepMutation/loops;
		LOGGER.info("Node deep mutation miss rate: "+ToolBox.format2Decimals((float)nodeDeepMutation));
		
		LOGGER.info("testReglesEgales");
		for (int i = 0; i < loops; i++)
			try {	testReglesEgales(Level.OFF);				} catch(Exception e){e.printStackTrace();excp++;}
			
		
		LOGGER.info("testConstraintMutation");
		for (int i = 0; i < loops; i++)try {	
				constraintMutationMissRate += testConstraintMutation(Level.OFF, 100, 0.8);
			} catch(Exception e){e.printStackTrace();excp++;}

		constraintMutationMissRate = constraintMutationMissRate/loops;
		LOGGER.info("Constraint mutation miss rate: "+ToolBox.format2Decimals((float)constraintMutationMissRate));
		
		LOGGER.info("testConstraintCross");
		for (int i = 0; i < loops; i++)
			try {	testConstraintCross(Level.OFF);				} catch(Exception e){e.printStackTrace();excp++;}
		
		LOGGER.info("Exceptions : " + excp);
		LOGGER.info("nTest_rule : Exit.");
	}
	
	public static double testNodeDeepMutation(Level verbose, int loops, double maxMissedRate) throws UnstableStateException {
		Level l = LOGGER.getLevel();
		LOGGER.setLevel(verbose);
		LOGGER.info("Entering OCLRuler - 'rule' package oclruler.Testing - deep mutation ");
		
		Node n0 = NodeFactory.createEmptyNode(null, Metamodel.getConcept("M"), Type.NOT);
		Node n = NodeFactory.createRandomNode(n0, Metamodel.getConcept("M"), Type.DEFAULT, null);
		LOGGER.fine("n0:\n"+n0.printXML());
		Node nbis = n.clone();
		Node n0bis = n0.clone();
		if(!n.equals(nbis)) throw new IllegalStateException("Back to Node equals Node !\nn:\n"+n.printXML("  ")+" \n NOT EQUALS TO \nnbis:\n"+nbis.printXML("  "));
		if(!n0.equals(n0bis)) throw new IllegalStateException("Back to Node equals Node !\nn:\n"+n.printXML("  ")+" \n NOT EQUALS TO \nnbis:\n"+nbis.printXML("  "));
		LOGGER.config("1. (Test_rule.testNodeDeepMutation : line)");

		LOGGER.fine("\n"+"Mutate deep : "+n.getId());
		Constraint cst = new Constraint(n0);
		Constraint cstbis = cst.clone();
		
		n = n.mutateDeep(DeepMutationType.Specific);
		if(n.equals(nbis)) throw new IllegalStateException("Mutation not effective : \nn:\n"+n.printXML("  ")+" \n EQUALS TO \nmutant:\n"+nbis.printXML("  "));
		try {
			cst.prune();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		LOGGER.fine("Specific:\n"+cst.printXML());
		if(cst.equals(cstbis)) throw new IllegalStateException("Mutation not effective : \ncst:\n"+cst.printXML("  ")+" \n EQUALS TO \nmutant:\n"+cstbis.printXML("  "));
		nbis = n.clone();
		cstbis = cst.clone();
		LOGGER.config("2. (Test_rule.testNodeDeepMutation : line)");
		
		n = n.mutateDeep(DeepMutationType.NestInFO);
		if(n.equals(nbis)) throw new IllegalStateException("Mutation not effective : \nn:\n"+n.printXML("  ")+" \n EQUALS TO \nmutant:\n"+nbis.printXML("  "));
		try {
			cst.prune();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		LOGGER.fine("NestInFO:\n"+cst.printXML());
		if(cst.equals(cstbis)) throw new IllegalStateException("Mutation not effective : \ncst:\n"+cst.printXML("  ")+" \n EQUALS TO \nmutant:\n"+cstbis.printXML("  "));
		nbis = n.clone();
		cstbis = cst.clone();
		LOGGER.config("3. (Test_rule.testNodeDeepMutation : line)");
		
		n = n.mutateDeep(DeepMutationType.NestInNOT);
		if(n.equals(nbis)) throw new IllegalStateException("Mutation not effective : \nn:\n"+n.printXML("  ")+" \n EQUALS TO \nmutant:\n"+nbis.printXML("  "));
		try {
			cst.prune();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		LOGGER.fine("NestInNOT:\n"+cst.printXML());
		if(cst.equals(cstbis)) throw new IllegalStateException("Mutation not effective : \ncst:\n"+cst.printXML("  ")+" \n EQUALS TO \nmutant:\n"+cstbis.printXML("  "));
		nbis = n.clone();
		cstbis = cst.clone();
		LOGGER.config("4. (Test_rule.testNodeDeepMutation : line)");
		
		n = n.mutateDeep(DeepMutationType.ChangeForRandomNode);
		if(n.equals(nbis)) throw new IllegalStateException("Mutation not effective : \nn:\n"+n.printXML("  ")+" \n EQUALS TO \nmutant:\n"+nbis.printXML("  "));
		try {
			cst.prune();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		LOGGER.fine("ChangeForRandomNode:\n"+n.printXML());
		nbis = n.clone();
		cstbis = cst.clone();
		LOGGER.config("5. (Test_rule.testNodeDeepMutation : line)");
		
		if(!LOGGER.isLoggable(Level.FINE))
			LOGGER.config("n mutant:\n"+n.printXML());
		LOGGER.fine("Mutate Cst : ");
		int nbMissed = 0;
		boolean miss = false;
		for(int i = 0 ; i < loops/5 ; i ++) {
//			System.out.println("6."+i+" (Test_rule.testNodeDeepMutation : line)");
			try {
				cst.mutate();
			} catch (UnstableStateException e) {
				nbMissed++;
				e.printStackTrace();
			}
//			if(miss = cst.equals(cstbis)){
//				nbMissed++;
//				throw new IllegalStateException("Mutation not effective : \ncst:\n"+cst.printXML("  ")+" \n EQUALS TO \nmutant:\n"+cstbis.printXML("  "));
//			}
			cstbis = cst.clone();
			LOGGER.finer(i+" "+(miss?"(missed)":"")+"\n"+cst.printXML());
		}
//		cst.prune();
		LOGGER.config("AFTER "+loops+" mutations:\ncstbis:\n"+cst.prettyPrint());
		double missRate =(float)nbMissed / (float)loops;
		if(missRate > maxMissedRate) throw new IllegalStateException("Mutation not effective : missed rate is "+ToolBox.format2Decimals((float)missRate));
		LOGGER.info("Missed mutation rate : "+ToolBox.format2Decimals((float)missRate));
		
//		cst.mutate();
//		if(cst.equals(cstbis)) throw new IllegalStateException("Mutation not effective : \ncst:\n"+cst.printXML("  ")+" \n EQUALS TO \nmutant:\n"+cstbis.printXML("  "));
//		cstbis = cst.clone();
//		LOGGER.config("2:\n"+cst.printXML());
//		cst.mutate();
//		if(cst.equals(cstbis)) throw new IllegalStateException("Mutation not effective : \ncst:\n"+cst.printXML("  ")+" \n EQUALS TO \nmutant:\n"+cstbis.printXML("  "));
//		cstbis = cst.clone();
//		LOGGER.config("3:\n"+cst.printXML());
//		cst.mutate();
//		if(cst.equals(cstbis)) throw new IllegalStateException("Mutation not effective : \ncst:\n"+cst.printXML("  ")+" \n EQUALS TO \nmutant:\n"+cstbis.printXML("  "));
//		cstbis = cst.clone();
//		LOGGER.config("4:\n"+cst.printXML());
//		cst.prune();
//		LOGGER.config("4bis (pruned):\n"+cst.printXML());
		
		
		
//		System.out.println(cst.prettyPrint());
		LOGGER.config("Exit.");
		LOGGER.setLevel(l);
		return missRate;
	}
	public static double testConstraintMutation(Level verbose, int loops, double maxMissRate) throws Exception {
		Level l = LOGGER.getLevel();
		LOGGER.setLevel(verbose);
		LOGGER.config("Entering OCLRuler - 'rule' package oclruler.Testing - mutation functionalities");
		
		Constraint cst = Constraint.createRandomConstraint();
		LOGGER.config("START-1:\ncst:\n"+cst.prettyPrint());
		Constraint cstbis = cst.clone();
		LOGGER.fine("START-1:\ncst:\n"+cst.prettyPrint());
		if(!cst.equals(cstbis)) throw new IllegalStateException("Back to Node equals Node !\nn:\n"+cst.printXML("  ")+" \n NOT EQUALS TO \nnbis:\n"+cstbis.printXML("  "));
		LOGGER.fine("START:\ncst:\n"+cst.prettyPrint());
		int nbMissed = 0;
		for(int i = 0 ; i < loops ; i ++) {
			cst.mutate();
//			if(cst.equals(cstbis)) throw new IllegalStateException("Mutation not effective :\n original: "+cst.printXML("  ")+"\nmutant:\n"+cstbis.printXML("  "));
			if(cst.equals(cstbis)) 
				nbMissed ++;
			cstbis = cst.clone();
			LOGGER.finer("cstbis:\n"+cst.prettyPrint());
		}
		LOGGER.config("AFTER "+loops+" mutations:\ncstbis:\n"+cst.prettyPrint());
		double missRate = ToolBox.format2Decimals((float)nbMissed / (float)loops);
		if(missRate > maxMissRate) throw new IllegalStateException("Mutation not effective : missed rate is "+missRate);
		LOGGER.info("Missed mutation rate : "+missRate);
		
		for(int i = 0 ; i < loops ; i ++) {
			cst.mutate();
			cstbis = cst.cross(cstbis)[0];
			LOGGER.finer("cstbis:\n"+cst.prettyPrint());
		}
		LOGGER.config("AFTER "+loops+" cross and mutations:\ncstbis:\n"+cst.prettyPrint());
		
		
		LOGGER.config("\nExit.");
		LOGGER.setLevel(l);
		return missRate;
	}
	
	@SuppressWarnings("deprecation")
	public static void testNodeMutation(Level verbose) throws Exception {
		Level l = LOGGER.getLevel();
		LOGGER.setLevel(verbose);
		LOGGER.info("Entering OCLRuler - 'rule' package oclruler.Testing - mutation functionalities\n");
		
		/**
		 * Type DEFAULT
		 */
		Node nDEF = NodeFactory.createRandomNode(Type.DEFAULT);
		Node nDEFbis = nDEF.clone();
//		System.out.println(nDEF.printSimpleXML());
//		System.out.println(nDEF.printXML());
		if(!nDEF.equals(nDEFbis)) throw new IllegalStateException("Back to Node equals Node !\nn:\n"+nDEF.printXML("  ")+" \n NOT EQUALS TO \nnbis:\n"+nDEFbis.printXML("  "));
//		System.out.println("- - - Mutation - - -");
		boolean b = nDEF.mutateSimple();
		if(b && nDEF.equals(nDEFbis)) throw new IllegalStateException("Mutation not effective :\n original: "+nDEF.printXML("  ")+"\nmutant:\n"+nDEFbis.printXML("  "));
//		System.out.println(nDEF.printXML());
		
		/**
		 * Type FO
		 */
		Node_FO nFO = (Node_FO)NodeFactory.createRandomNode(Type.FO, 3);
		Node_FO nFObis = (Node_FO)nFO.clone();
//		System.out.println(n.printXML());
//		System.out.println(n.printSimpleXML());
		if(!nFO.equals(nFObis)) throw new IllegalStateException("Back to Node equals Node !\n n: "+nFO.printXML("  ")+" \n NOT EQUALS TO \nnbis:\n"+nFObis.printXML("  "));
//		System.out.println("- - - Mutation - - -");
//		if(nFO.mutateSimple() && nFO.equals(nFObis)) throw new IllegalStateException("Mutation not effective :\n original: "+nFO.printXML("  ")+"\nmutant:\n"+nFObis.printXML("  "));
		while(nFO.equals(nFObis)) //Exploring paths...
			nFO.mutateSimple();
		if(nFO.equals(nFObis)) throw new IllegalStateException("Mutation not effective :\n original: "+nFO.printXML("  ")+"\nmutant:\n"+nFObis.printXML("  "));
//		System.out.println(n.printXML());
		
		
		/**
		 * Type { AND, OR }
		 */
		Node nOR = NodeFactory.createRandomNode(Type.OR);
		Node nORbis = nOR.clone();
//		System.out.println(nOR.printSimpleXML());
//		System.out.println(nOR.printXML());
		if(!nOR.equals(nORbis)) throw new IllegalStateException("Back to Node equals Node !\nn:\n"+nOR.printXML("  ")+" \n NOT EQUALS TO \nnbis:\n"+nORbis.printXML("  "));
//		System.out.println("- - - Mutation - - -");
		 b = nOR.mutateSimple();
		if(b && nOR.equals(nORbis)) throw new IllegalStateException("Mutation not effective :\n original: "+nOR.printXML("  ")+"\nmutant:\n"+nORbis.printXML("  "));
//		System.out.println(nOR.printXML());


		/**
		 * Type IMPLIES
		 */
		Node nIMP = NodeFactory.createRandomNode(Type.IMPLIES);
		Node nIMPbis = nIMP.clone();
//		System.out.println(nIMP.printSimpleXML());
//		System.out.println(nIMP.printXML());
		if(!nIMP.equals(nIMPbis)) throw new IllegalStateException("Back to Node equals Node !\nn:\n"+nIMP.printXML("  ")+" \n NOT EQUALS TO \nnbis:\n"+nIMPbis.printXML("  "));
//		System.out.println("- - - Mutation - - -");
		 b = nIMP.mutateSimple();
		if(b && nIMP.equals(nIMPbis)) throw new IllegalStateException("Mutation not effective :\n original: "+nIMP.printXML("  ")+"\nmutant:\n"+nIMPbis.printXML("  "));
//		while(nOP.equals(nOPbis)) //Exploring paths...
//			nOP.applyMutation();
//		if(nIMP.equals(nIMPbis)) throw new IllegalStateException("Mutation not effective :\noriginal:\n"+nIMP.printXML("  ")+"\nmutant:\n"+nIMPbis.printXML("  "));
//		System.out.println(nIMP.printXML());

		
		/**
		 * Type NOT
		 */
		Node nNOT = NodeFactory.createRandomNode(Type.NOT);
		Node nNOTbis = nNOT.clone();
//		System.out.println(nNOT.printSimpleXML());
//		System.out.println(nNOT.printXML());
		if(!nNOT.equals(nNOTbis)) throw new IllegalStateException("Back to Node equals Node !\nn:\n"+nNOT.printXML("  ")+" \n NOT EQUALS TO \nnbis:\n"+nNOTbis.printXML("  "));
//		System.out.println("- - - Mutation - - -");
		 b = nNOT.mutateSimple();
		nNOT = NodeFactory.pruneNode(nNOT);
		
		if(b && nNOT.equals(nNOTbis)) throw new IllegalStateException("Mutation not effective :\n original: "+nNOT.printXML("  ")+"\nmutant:\n"+nNOTbis.printXML("  "));
//		System.out.println(nNOT.printXML());
		
		LOGGER.info("\nExit.");
		LOGGER.setLevel(l);
	}


	
	public static void testProgram2Cross(Level verbose) throws Exception {
		Program prg1 = Program.createRandomProgram();
		Program prg2 = Program.createRandomProgram();
		
		System.out.println("prg1: \n"+prg1.printXML());
		System.out.println("prg1: \n"+prg1.getOCL());
		System.out.println("prg2: \n"+prg2.printXML());
		System.out.println();System.out.println();System.out.println();
		Program[] cross = (Program[])prg1.crossover(prg2);
		System.out.println();System.out.println();System.out.println();
		System.out.println("cross[0]: \n"+cross[0].printXML());
		System.out.println("cross[1]: \n"+cross[1].printXML());
	
		try {
			System.out.println("Check prg2");
			EvaluatorOCL.check(prg2);
		} catch (Exception e) {			e.printStackTrace();		}
		try {
			System.out.println("Check prg1");
			EvaluatorOCL.check(prg1);
		} catch (Exception e) {			e.printStackTrace();		}
		try {
			System.out.println("Check prg2");
			EvaluatorOCL.check(prg2);
		} catch (Exception e) {			e.printStackTrace();		}
		
		try {
			System.out.println("Check cross[0]");
			EvaluatorOCL.check(cross[0]);
		} catch (Exception e) {			e.printStackTrace();		}
		try {
			System.out.println("Check cross[1]");
			EvaluatorOCL.check(cross[1]);
		} catch (Exception e) {			e.printStackTrace();		}
		
	}
	
	@SuppressWarnings("deprecation")
	public static void testPruning3(Level verbose) throws Exception {
		
		Level l = LOGGER.getLevel();
		LOGGER.setLevel(verbose);
		LOGGER.info("Entering OCLRuler - 'rule' package oclruler.Testing - Pruning 3 - NOT\n");
		
		
		Node n0 = NodeFactory.createEmptyNode(null, ToolBox.getRandom(Metamodel.getAllConcepts().values()), Type.NOT);
		Constraint cst = new Constraint(n0);
		
		Node n1 = NodeFactory.createEmptyNode(n0,   n0.getContext(), Type.NOT);
		int loop = 5;
		for (int i = 0; i < loop; i++) {
			n1 = NodeFactory.createEmptyNode(n1,   n1.getContext(), Type.NOT);
		}
		
		Node np = NodeFactory.createNode(n1, ToolBox.getRandom(Metamodel.getAllConcepts().values()), ToolBox.getRandom(Node.Type.valuesNot(Type.NOT, Type.TRUE)), 2, null);
		
		LOGGER.config("np_1: "+"\n"+np.prettyPrint());
		
		//NOT
		//  NOT
		//    NOT
		//     ...
		//      NOT
		//        NOT
		//          np
		
		
		LOGGER.config("\n"+n0.prettyPrint(""));
		if(n0.getChildren().contains(np))
			throw new IllegalStateException("root.children contains np: "+n0);
		LOGGER.info("!n0.children.contains(np):  "+(!n0.getChildren().contains(np)?"Ok.":"Error !"));;//getChlild(0) because n000 is a FO.
		LOGGER.info(" - - - Pruning... - - -");
		
//		System.out.println(n0.prettyPrint());
//		System.out.println("static pruning");
		n0 = NodeFactory.pruneNode(n0);
//		System.out.println(n0.prettyPrint());
		
		if(loop%2==0){
			if(n0.depth() != np.depth()){
				throw new IllegalStateException("wrong depth: "+n0);
			}
		} else if(n0.depth() != np.depth()+1){//One not left
			throw new IllegalStateException("wrong depth: "+n0);
		}
		
//		n0.prune();
		//   rootFO
		//   AND
		//np    np2
		LOGGER.config(" - - - Pruning... - - -");
		cst.prune();
		
		if( (!np.isType(Type.NOT) && !n0.getChildren().contains(np))  )
			throw new IllegalStateException("root.children does not contain np: "+n0);
		LOGGER.info("n0.children.contains(np):  "+(n0.getChildren().contains(np)?"Ok.":"Error !"));;//getChlild(0) because n000 is a FO.

		/*
		 * Second run / one more loop
		 */
		n0 = NodeFactory.createEmptyNode(null, ToolBox.getRandom(Metamodel.getAllConcepts().values()), Type.NOT);
		cst = new Constraint(n0);
		n1 = NodeFactory.createEmptyNode(n0,   n0.getContext(), Type.NOT);
		loop++;
		for (int i = 0; i < loop; i++) {
			n1 = NodeFactory.createEmptyNode(n1,   n1.getContext(), Type.NOT);
		}
		np = NodeFactory.createNode(n1, ToolBox.getRandom(Metamodel.getAllConcepts().values()), ToolBox.getRandom(Node.Type.valuesNot(Type.NOT, Type.TRUE)), 2, null);
		cst.prune();
		
//		System.out.println(n0.prettyPrint());
		cst.prune();
//		System.out.println(n0.prettyPrint());
		
		if(loop%2==0){
			if(cst.getRoot().depth() != np.depth()){
				throw new IllegalStateException("wrong depth: "+n0);
			}
		} else if(cst.getRoot().depth() != np.depth()+1){//One not left
			throw new IllegalStateException("wrong depth: "+n0);
		}
		/*
		 * END second run
		 */
		
		LOGGER.setLevel(l);
	}

	
	@SuppressWarnings("deprecation")
	public static void testPruning2(Level verbose) throws Exception {
		Level l = LOGGER.getLevel();
		LOGGER.setLevel(verbose);
		LOGGER.info("Entering OCLRuler - 'rule' package oclruler.Testing - Pruning 2\n");
		
		
		
		
		Node_FO n000 = (Node_FO)NodeFactory.createEmptyNode(null, ToolBox.getRandom(Metamodel.getAllConcepts().values()), Type.FO);
		n000.setPath(ToolBox.getRandom(n000.getContext().getSimplePaths()));
		n000.setAction(ACTION.FORALL);
		Constraint cst = new Constraint(n000);
		
		Node n00 = NodeFactory.createEmptyNode(n000, ToolBox.getRandom(Metamodel.getAllConcepts().values()), Type.AND);
		Node n0 = NodeFactory.createEmptyNode(n00, ToolBox.getRandom(Metamodel.getAllConcepts().values()), Type.AND);
		Node n2 = NodeFactory.createEmptyNode(n00, ToolBox.getRandom(Metamodel.getAllConcepts().values()), Type.AND);
		
		Node n1 = NodeFactory.createEmptyNode(n0, ToolBox.getRandom(Metamodel.getAllConcepts().values()), Type.AND);
		
		Node np_1 = NodeFactory.createNode(n0, ToolBox.getRandom(Metamodel.getAllConcepts().values()), null, 2, null);
		if(np_1.isType(Type.FO)){
			((Node_FO)np_1).setPath(ToolBox.getRandom(np_1.getContext().getSimplePaths()));
			((Node_FO)np_1).setAction(ACTION.FORALL);
		}
		np_1 = NodeFactory.pruneNode(np_1);
		Node np_2 = np_1.clone();
		n1.addChild(np_2);
		Node np_3 = np_1.clone();
		n1.addChild(np_3);
		
		Node np2_1 = NodeFactory.createNode(n2, ToolBox.getRandom(Metamodel.getAllConcepts().values()), null, 2, null);
		np2_1 = NodeFactory.pruneNode(np2_1);
		if(np2_1.isType(Type.FO)){
			((Node_FO)np2_1).setPath(ToolBox.getRandom(np2_1.getContext().getSimplePaths()));
			((Node_FO)np2_1).setAction(ACTION.FORALL);
		}
		Node np2_2 = np2_1.clone();
		n2.addChild(np2_2);
		
		LOGGER.config("np_1: \n"+np_1.prettyPrint());
		LOGGER.config("np2_1: \n"+np2_1.prettyPrint());
		
		//          n000
		//           n00            
		//   	n0         n2       
		//   n1   np   np2   np2
		//np   np
		
		
		
		LOGGER.config(n000.prettyPrint("  "));
		if(n000.getChild(0).getChildren().contains(np_1))
			throw new IllegalStateException("root.child0.children contains np");
		if(n000.getChild(0).getChildren().contains(np2_1))
			throw new IllegalStateException("root.child0.children contains np2");
		LOGGER.info("!n000.child0.children.contains(np):  "+(!n000.getChild(0).getChildren().contains(np_1)?"Ok.":"Error !"));;//getChlild(0) because n000 is a FO.
		LOGGER.info("!n000.child0.children.contains(np2): "+(!n000.getChild(0).getChildren().contains(np2_1)?"Ok.":"Error !"));;
		LOGGER.info(" - - - Pruning... - - -");
		cst.prune();
		//   rootFO
		//   AND
		//np    np2
		LOGGER.config(" \n"+n000.prettyPrint("  "));
		LOGGER.config(" - - - Pruning... - - -");
		cst.prune();
		LOGGER.config(" \n"+n000.prettyPrint("  "));
		if(!n000.getChild(0).getChildren().contains(np_1) && !n000.getChild(0).equals(np_1))
			throw new IllegalStateException("root.child0.children does not contain np: \nn000:\n"+n000+"\nnp_1:\n"+np_1);
		
		if(!n000.getChild(0).getChildren().contains(np2_1) && !n000.getChild(0).equals(np2_1))
			
			throw new IllegalStateException("root.child0.children does not contain np2: \n"+n000);
		LOGGER.info("n000.children.contains(np):  "+(n000.getChild(0).getChildren().contains(np_1)?"Ok.":"Error !"));;//getChlild(0) because n000 is a FO.
		LOGGER.info("n000.children.contains(np2): "+(n000.getChild(0).getChildren().contains(np2_1)?"Ok.":"Error !"));;

		LOGGER.setLevel(l);
	}
	
	@SuppressWarnings("deprecation")
	public static void testPruning1(Level verbose) throws Exception {
		Level l = LOGGER.getLevel();
		LOGGER.setLevel(verbose);
		LOGGER.info("Entering OCLRuler - 'rule' package oclruler.Testing - Pruning\n");
		Node n00 = NodeFactory.createEmptyNode(null, ToolBox.getRandom(Metamodel.getAllConcepts().values()), Type.AND);
		Constraint cst = new Constraint(n00);
		
		
		Node n0 = NodeFactory.createEmptyNode(n00, ToolBox.getRandom(Metamodel.getAllConcepts().values()), Type.AND);
		
		Node np_1 = NodeFactory.createNode(n0, ToolBox.getRandom(Metamodel.getAllConcepts().values()), null, 2, null);
		np_1 = NodeFactory.pruneNode(np_1);
		
		Node n1 = NodeFactory.createEmptyNode(n0, ToolBox.getRandom(Metamodel.getAllConcepts().values()), Type.AND);
		Node np_2 = np_1.clone();
		Node np_3 = np_1.clone();
		n1.addChild(np_2);
		n1.addChild(np_3);
		np_2.setParent(n1);
		np_3.setParent(n1);
		
		if(!np_1.equals(np_2))
			throw new IllegalStateException("equals : \nnp:\n"+np_1.getParent()+"\nnp_2:\n"+np_2.getParent());
		
		
		
		Node n2 = NodeFactory.createEmptyNode(n00, ToolBox.getRandom(Metamodel.getAllConcepts().values()), Type.AND);
		Node np2_1 = NodeFactory.createNode(n2, ToolBox.getRandom(Metamodel.getAllConcepts().values()), null, 2, null);
//		np2_1 = NodeFactory.pruneNode(np2_1);
		Node np2_2 = np2_1.clone();
		n2.addChild(np2_2);
		
		if(!np2_1.equals(np2_2))
			throw new IllegalStateException("equals : \nnp:\n"+np_1.getParent()+"\nnp_2:\n"+np_2.getParent());
		
		LOGGER.config("np_1"+np_1.prettyPrint());
		LOGGER.config("np2_1"+np2_1.prettyPrint());

		//             rootAND
		//       AND            OR
		//	OR      np       np2   np2
		//np  np    
		
//		n00.addChild(n0);
//		n00.addChild(n2);
//		
//		n2.addChild(np2_1);
//		n2.addChild(np2_2);
		
//		n0.addChild(np_1);
//		n0.addChild(n1);
//		n1.addChild(np_2);
//		n1.addChild(np_3);
		
		LOGGER.config(n00.printOnlyIds(""));
		if(n00.getChildren().contains(np_1))
			throw new IllegalStateException("root.children contains np");
		if(n00.getChildren().contains(np2_1))
			throw new IllegalStateException("root.children contains np2");
		LOGGER.info("!n00.children.contains(np):  "+(!n00.getChildren().contains(np_1)?"Ok.":"Error !"));;//getChlild(0) because n000 is a FO.
		LOGGER.info("!n00.children.contains(np2): "+(!n00.getChildren().contains(np2_1)?"Ok.":"Error !"));;
		LOGGER.info(" - - - Pruning... - - -");
//		cst.prune();
		//   AND
		//np    np2
//		System.out.println(n00);
//		System.out.println(np_1);
		
		LOGGER.config(n00.printOnlyIds(""));
		LOGGER.config(" - - - Pruning... - - -");
		cst.prune();
		LOGGER.config(n00.printOnlyIds(""));
		
//		
//		System.out.println();
//		System.out.println(n00);
//		System.out.println(np_1);
		
		
		if(!n00.getChildren().contains(np_1)) //in case of a fo : go to grand children.
			throw new IllegalStateException("root.children does not contain np");
		if(!n00.getChildren().contains(np2_1))
			throw new IllegalStateException("root.children does not contain np2");

		LOGGER.info("n00.children.contains(np):  "+(n00.getChildren().contains(np_1)?"Ok.":"Error !"));;
		LOGGER.info("n00.children.contains(np2): "+(n00.getChildren().contains(np2_1)?"Ok.":"Error !"));;
		LOGGER.setLevel(l);
	}

	/**
	 * @throws Exception 
	 */
	public static void testConstraintCross(Level verbose) throws Exception {
		Level l = LOGGER.getLevel();
		LOGGER.setLevel(verbose);
		LOGGER.info("Entering OCLRuler - 'rule' package oclruler.Testing - cross functionalities\n");
		
//		testMutations();
//		testPathGeneration();
		
//		for (int i = 0; i < 10; i++) {
//			Constraint c = Constraint.createRandomConstraint();
//			System.out.println(c.prettyPrint());
//		
//		}
		
		boolean printcs1 = false,
				printcs2 = false,
				printcloned1 = false,
				printcloned2 = false,
				printcrossed1 = false,
				printcrossed2 = false;
		//only cs1
		printcs1 = true;
//		printcloned1 = true;
		printcrossed1 = true;
		//only cs2
		printcs2 = true;
//		printcloned2 = true;
		printcrossed2 = true;
		
		Constraint cs1 = Constraint.createRandomConstraint();
		Constraint cs2 = Constraint.createRandomConstraint();
		if(cs1.equals(cs2))
			cs2 = Constraint.createRandomConstraint();
		
		if(printcs1)
			LOGGER.config("cs1: \n"+cs1.printXML(""));
		
		if(printcs2)
			LOGGER.config("cs2: \n"+cs2.printXML(""));
		
		Constraint cs1_2 = cs1.clone();
		
		Constraint cs2_2 = cs2.clone();
		
		if(!cs1_2.equals(cs1) && cs2_2.equals(cs2))
			throw new IllegalStateException("Cloning doesn't work !");
		
		LOGGER.config("");
		if(printcloned1)
			LOGGER.config("cs1 cloned: \n"+cs1_2.printXML("")+" ("+ (cs1_2.equals(cs1))+")");
		if(printcloned2)
			LOGGER.config("cs2 cloned: \n"+cs2_2.printXML("")+" ("+ (cs2_2.equals(cs2)) +")");
		LOGGER.config("");
		
		
		//If no 
		Constraint[] cross = cs1.cross(cs2);
		
		LOGGER.config("\n");
		if(printcs1)
			LOGGER.config("cs1-after-cross  : \n"+cs1.printXML(""));
		if(printcloned1)
			LOGGER.config("cs1 cloned a.c.: \n"+cs1_2.printXML("")+" ("+ (cs1_2.equals(cs1))+")");
		if(printcs2)
			LOGGER.config("cs2-after-cross: \n"+cs2.printXML(""));
		if(printcrossed1)
			LOGGER.config("cross[0]: \n"+cross[0].printXML(""));
		if(printcrossed2)
			LOGGER.config("cross[1]: \n"+cross[1].printXML(""));
		
		if(cs1.equals(cs2))	throw new IllegalStateException("cs1 x cs2 : true !");
		LOGGER.config("\ncs1 x cs2 :        "+cs1.equals(cs2));//false
		if(!cs1.equals(cs1)) throw new IllegalStateException("cs1 x cs1 : false !");
		LOGGER.config("cs1 x cs1 :    "+cs1.equals(cs1));//true
		LOGGER.config("");
		
		
		if(!cs1.equals(cs1_2)) throw new IllegalStateException("cs1 x cs1_2 : false !");
		LOGGER.config("cs1   x cs1-2 :    ("+cs1.getNumericId()+"|"+cs1_2.getNumericId()+") "+cs1.equals(cs1_2));//true
		if(!cs2.equals(cs2_2)) throw new IllegalStateException("cs2 x cs2_2 : false !");
		LOGGER.config("cs2   x cs2-2 :    ("+cs2.getNumericId()+"|"+cs2_2.getNumericId()+") "+cs2.equals(cs2_2));//true
		loop++;
		if(cs1.equals(cross[0])) throw new IllegalStateException(  "cs1   x cross[0] : true !\ncs1:\n"+cs1.getRoot());
		LOGGER.config("cs1   x cross[0] : ("+cs1.getNumericId()+"|"+cross[0].getNumericId()+") "+cs1.equals(cross[0]));//false
		if(cs2.equals(cross[1])) throw new IllegalStateException(  loop + " cs2   x cross[1] : true !");//\ncs2:\n"+cs2.getRoot()+"\ncross:\n"+cross[1].getRoot());
		LOGGER.config("cs2   x cross[1] : ("+cs2.getNumericId()+"|"+cross[1].getNumericId()+") "+cs2.equals(cross[1]));//false
		if(cs1_2.equals(cross[0])) throw new IllegalStateException("cs1_2 x cross[0] : true !");
		LOGGER.config("cs1-2 x cross[0] : ("+cs1_2.getNumericId()+"|"+cross[0].getNumericId()+") "+cs1_2.equals(cross[0]));//false
		if(cs2_2.equals(cross[1])) throw new IllegalStateException("cs2_2 x cross[1] : true !");
		LOGGER.config("cs2-2 x cross[1] : ("+cs2_2.getNumericId()+"|"+cross[1].getNumericId()+") "+cs2_2.equals(cross[1]));//false

		LOGGER.info("\nExit.");
		LOGGER.setLevel(l);
	}

static int loop = 1;
	@SuppressWarnings("unused")
	public static void testA15() throws Exception{
		if(!Config.METAMODEL_NAME.equals("Statemachine")) {
			throw new Exception(" Wrong metamodel : "+Config.METAMODEL_NAME+" - Expected is 'Statemachine'.");
		}
		ExampleSet ms = new ExampleSet(Config.getInstancesDirectory());
		
		
		Pattern p = new A15_ReferenceIsTypeOf(Metamodel.getConcept("Statemachine"), 
				Metamodel.getConcept("Statemachine"), 
				Metamodel.getReference("init"));
		
		 p = new A15_ReferenceIsTypeOf(Metamodel.getConcept("Transition"), 
				Metamodel.getConcept("Transition"), 
				Metamodel.getReference("target"));
		
		Model m = ExampleSet.getInstance().getExample("model_00739.xmi");
		Program prg = new Program();
		prg.addConstraint(new Constraint(new Node_DEFAULT(null, p)));
		EvaluatorOCL.execute(null, m, prg);
	}


	@SuppressWarnings("unused")
	public static void testReglesEgales(Level verbose) throws Exception{
		Level l = LOGGER.getLevel();
		LOGGER.setLevel(verbose);
		LOGGER.info("Entering.");
		PatternFactory.checkPatternsToBannish();
		
		
		LOGGER.info("Patterns enabled : ");
		for (PatternType pt : PatternType.enabledValues())
			LOGGER.info("  - "+pt);
		for (int i = 0; i < 100; i++) {
			for (PatternType pt : PatternType.enabledValues()) {
				testPatternsEquality(pt);
			}
		}
		
		if (mutationFailed > 0)
			LOGGER.warning(" mutation failed : "+mutationFailed+" times on "+mutations+".");
		
		
		if(!Config.METAMODEL_NAME.equals("Family")) {
			throw new IllegalArgumentException(" Wrong metamodel : "+Config.METAMODEL_NAME+" - Expected is 'Family'.");
		}
		Concept concept_P = Metamodel.getConcept("P");
		Concept concept_M = Metamodel.getConcept("M");
		Concept concept_F = Metamodel.getConcept("F");
		Reference ref_k = concept_M.getReference("k");
		Reference ref_k2 = concept_P.getReference("k");
		Reference ref_l = concept_F.getReference("l");
		Reference ref_d = concept_P.getReference("d");
		Reference ref_m = concept_P.getReference("m");
		Attribute att_age = concept_P.getAttribute("age");
		Attribute att_size = concept_P.getAttribute("size");
		Attribute att_test = concept_P.getAttribute("test");
		Attribute att_name = concept_P.getAttribute("name");
		
		Pattern p_1  = new A1_AcyclicReference(concept_P, ref_d);
		Pattern p_2  = new A1_AcyclicReference(concept_P, ref_d);	
		if(!p_1.equals(p_2)) throw new IllegalStateException("1. "+p_1+" NOT EQUALS "+p_2);
		
		p_1  = new A10_OppositeReferencesOneToMany(concept_P, concept_M, ref_k, ref_d);
		p_2  = new A10_OppositeReferencesOneToMany(concept_P, concept_M, ref_k, ref_d);		
		if(!p_1.equals(p_2)) throw new IllegalStateException("2. "+p_1+" NOT EQUALS "+p_2);
		
		p_1  = new A10_OppositeReferencesOneToMany(concept_P, concept_M, ref_k, ref_d);
		p_2  = new A10_OppositeReferencesOneToMany(concept_P, concept_M, ref_k2, ref_d);		
		if(p_1.equals(p_2)) throw new IllegalStateException("3. "+p_1+" EQUALS "+p_2);
		
		MMMatch m1 = ToolBox.getRandom(A13_CollectionIncludesSelf.getMatches());
		p_1 = new A13_CollectionIncludesSelf(m1);
		p_2 = new A13_CollectionIncludesSelf(m1);
		if(!p_1.equals(p_2)) throw new IllegalStateException("4. "+p_1+" NOT EQUALS "+p_2);
		
		p_1 = new A7_CollectionsSameSize(ref_k, ref_l);
		p_2 = new A7_CollectionsSameSize(ref_k2, ref_l);
		if(p_1.equals(p_2)) throw new IllegalStateException("5. "+p_1+" EQUALS "+p_2);
		
		p_1 = new A7_CollectionsSameSize(ref_k, ref_l);
		p_2 = new A7_CollectionsSameSize(ref_k, ref_l);
		if(!p_1.equals(p_2)) throw new IllegalStateException("6. "+p_1+" NOT EQUALS "+p_2);
		
		MMMatch m2 = A13_CollectionIncludesSelf.getMatches().get(1);
		m1 = A13_CollectionIncludesSelf.getMatches().get(1);
		if(!m1.equals(m2)) throw new IllegalStateException("7. "+m1+" NOT EQUALS "+m2);
		
		LOGGER.info("Exit.");
		LOGGER.setLevel(l);
	}


	public static void testPatternsEquality(PatternType pt) {
		MMMatch m1 = ToolBox.getRandom(Pattern.getMatches(pt.getInstanciationClass()));
		Pattern p_1 = Pattern.newInstance(pt.getInstanciationClass(), m1);
		Pattern p_2 = Pattern.newInstance(pt.getInstanciationClass(), m1);
		if(!p_1.equals(p_2)) throw new IllegalStateException(" "+p_1+" NOT EQUALS "+p_2);
		p_1.mutate();
		if(p_1.equals(p_2)) mutationFailed++;
		mutations++;
	}


	static int mutationFailed = 0;
	static int mutations = 0;


	public boolean testPattern(Pattern Ai, MMElement[] parametres){
		String ps = "";
		for (MMElement mmElement : parametres) 
			ps += mmElement.getName() + ", ";
		System.out.println("Main.testPattern("+Ai.getId()+", {"+ps+"})");
		boolean res = true;
		
		return res;
	}



	
	@SuppressWarnings({ "unused", "unchecked" })
	private static void testMutations(Level verbose, double maxNullMutationRate) throws CloneNotSupportedException {
		Level l = LOGGER.getLevel();
		LOGGER.setLevel(verbose);
		int error = 0;
		int loop = 0;

		
		HashMap<PatternType, Integer> errors = new HashMap<>();
		HashMap<PatternType, Integer> fines = new HashMap<>();
		for (PatternType pt : PatternType.enabledValues()) {
			errors.put(pt, 0);
			fines.put(pt, 0);
		}
		
		
		for (int j = 0; j < 100; j++) {
			
			Constraint p = Constraint.createRandomConstraint();
			Constraint pc = null;
			if(p != null){
				
				for (int i = 0; i < 100; i++) {
					pc = p.clone();
					if(!p.equals(pc)){
						error ++;
						throw new IllegalStateException(" Cloning failed : clone different from original : \n   Original : "+p+"\n   Clone : "+pc);
					}
					try {
						p.mutate();
					} catch (UnstableStateException e) {
						e.printStackTrace();
					}
					if(p.equals(pc)){
						LOGGER.config(" Mutation failed : mutant equals original : \n   Mutant : "+p+"\n   Original : "+pc);
//						System.out.println(Pattern.getMatches(p.getClass()));
//						System.out.println();
						error ++;
//						errors.put(p.getr.getType(), errors.get(p.getType())+1);
					} else {
//						fines.put(p.getType(), fines.get(p.getType())+1);
					}
					loop++;
				}
//				for (int i = 0; i < 10; i++) {
//					pc = p.clone();
//					p.mutateSimple();
//					if(p.equals(pc)){
//						LOGGER.severe(" Simple mutation failed : mutant equals original : \n   Mutant : "+p+"\n   Original : "+pc);
//						System.out.println(Pattern.getMatches(p.getClass()));
//						System.out.println();
//						error ++;
//					}
//				}
			}
		}
		int stringLength = 0;
		LOGGER.config("Patterns errors :");
		for (PatternType pt : errors.keySet()) 
			stringLength = (" - "+pt.getCompleteName()+" :\t"+errors.get(pt)).length()>stringLength?(" - "+pt.getCompleteName()+" : ").length():stringLength;
		for (PatternType pt : errors.keySet()) {
			Class<? extends Pattern> c = null;
			try {
				c = (Class<? extends Pattern>)Class.forName(pt.getCompleteName());
			} catch (ClassNotFoundException e) {
				System.err.println("Class not found : '"+pt.getCompleteName()+"'.");
			}
			float rate = ((float)errors.get(pt))/fines.get(pt);
			LOGGER.config(ToolBox.completeString(" - "+pt.getCompleteName()+" : ", stringLength) + " : "+ToolBox.format2Decimals(rate) + "\t| mSize : "+Pattern.getMatches(c).size());
		}
		double nullMutationRate = ToolBox.format2Decimals(((float)error)/loop);
		LOGGER.info("  General error rate : "+nullMutationRate);
		if(nullMutationRate > maxNullMutationRate)
			throw new IllegalStateException("null mutation rate high : "+nullMutationRate);
		LOGGER.setLevel(l);
	}

	public static void promptEnterKey(){
	   System.out.println("Press \"ENTER\" to continue...");
	   Scanner scanner = new Scanner(System.in);
	   scanner.nextLine();
	   scanner.close();
	}


	public static String printPattern(Pattern p){
		return p + "."+p.getParameters();
	}

}
