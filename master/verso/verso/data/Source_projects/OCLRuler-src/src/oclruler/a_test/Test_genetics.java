package oclruler.a_test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import oclruler.genetics.Evaluator;
import oclruler.genetics.EvaluatorOCL;
import oclruler.genetics.Oracle;
import oclruler.genetics.Population;
import oclruler.genetics.Population.Pareto;
import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.Model;
import oclruler.rule.Program;
import oclruler.rule.struct.Constraint;
import oclruler.utils.Config;
import oclruler.utils.ToolBox;

/**
 * Tests related to the package oclruler.'genetics'
 * @author Batot
 *
 */
public class Test_genetics {
	public final static Logger LOGGER = Logger.getLogger(Test_genetics.class.getName());
	public static void main(String[] args) {
		LOGGER.info("Entering OCLRuler - 'genetics' package oclruler.Testing\n");
		ToolBox.init();
		
		
		Program prg1 = Program.createRandomProgram(3);
		Program prg2 = Program.createRandomProgram(5);
		
		System.out.println("prg1:\n"+prg1.simplePrint());
		System.out.println("prg2:\n"+prg2.simplePrint());
		
		System.out.println("\n -- - - CROSSOVER begins - - --\n");
		Program[] crossed = (Program[])prg1.crossoverDeep(prg2);
		System.out.println("\n -- - - CROSSOVER ends - - --\n");

		System.out.println("cross[0]:\n"+crossed[0].simplePrint());
		System.out.println("cross[1]:\n"+crossed[1].simplePrint());

		
		System.out.println("\nExit.");

	}

	public static void testSorting(){
		ExampleSet ms = new ExampleSet(Config.getInstancesDirectory());
		Oracle o = Oracle.instantiateOracle(ms);
		
		
		LOGGER.finer(o.prettyPrint());
		LOGGER.finer(ms.toString());
	
		if(LOGGER.isLoggable(Level.FINER)){
			LOGGER.finer("Fires :");
			int sum = 0;
			for (Constraint p : o.getConstraints()) {
				LOGGER.finer(" - " + p.getId() + " : " + p.getFires());
				sum += p.getFires();
			}
			LOGGER.finer("    -> " + sum + " fire"+(sum>1?"s":""));
		}
		
		Population p0 ;
		Evaluator eva = new EvaluatorOCL(ms);
		
		long time = System.currentTimeMillis();
		long time1 = time, time2 = time;
		int i = 0;
		Population.NB_ENTITIES_IN_POP = 20;
		
		ArrayList<Model> list = new ArrayList<>(ms.getPositives());
		list.addAll(ms.getNegatives());
		ArrayList<Collection<Model>> models = Evaluator.dichoSplit(list, Evaluator.MAX_EXEC_PER_THREAD);
		LOGGER.fine("Multi thread : "+models.size()+" threads");
		for (Collection<Model> collection : models) {
			LOGGER.fine(" - "+ collection.size()+ "   | "+collection);
		}
		
		for (i = 0; i < 10; i++) {
			p0 = Population.createRandomPopulation();
			time1 = System.currentTimeMillis();
			Evaluator.MULTI_THREAD = true;
			p0.evaluate(eva);
			time1 = System.currentTimeMillis() - time1;
			LOGGER.fine("Evaluation (multi-thread) of "+p0.size()+" programs, duration:"+ToolBox.formatMillis(time1));
			
			time2 = System.currentTimeMillis();
			Evaluator.MULTI_THREAD = false;
			p0.evaluate(eva);
			time2 = System.currentTimeMillis() - time2;
			LOGGER.fine("Evaluation (singl-thread) of "+p0.size()+" programs, duration:"+ToolBox.formatMillis(time2));
			
			LOGGER.fine("");
		}
		
		LOGGER.info("Results:");
		LOGGER.info(i+" evaluations of populations ("+Population.NB_ENTITIES_IN_POP+" programs), duration:"+ToolBox.formatMillis(System.currentTimeMillis() - time));
		LOGGER.info(" MultiThread :  "+ToolBox.formatMillis(time1)+"");
		LOGGER.info(" SingleThread : "+ToolBox.formatMillis(time2)+"");
		LOGGER.info("  -> dif : "+ToolBox.formatMillis(time2-time1)+"");
		
		
		p0 = Population.createRandomPopulation();
		p0.evaluate(eva);
		
		ArrayList<Integer> al1 = new ArrayList<>();
		ArrayList<Integer> al2 = new ArrayList<>();
		
		time = System.currentTimeMillis();
		p0.fastNonDominantSort2();
		p0.crowdingDistanceAssignement();
		i = 0;
		LOGGER.finer(""+p0.getParetos());
		LOGGER.finer("duration:"+(System.currentTimeMillis() - time));
		for (Pareto p : p0.getParetos()) 
			al1.add(p.size());
		
		LOGGER.finer("------------------------");
		
		time = System.currentTimeMillis();
		p0.fastNonDominantSort2();
		p0.crowdingDistanceAssignement();
		i = 0;
		LOGGER.finer(""+p0.getParetos());
		LOGGER.finer("duration:"+(System.currentTimeMillis() - time));
		for (Pareto p : p0.getParetos()) 
			al2.add(p.size());
		
		boolean sameRes = al1.size() == al2.size();
		for (int j = 0; j < al1.size(); j++) 
			sameRes &= al1.get(i).equals(al2.get(i));
		if(!sameRes)
			LOGGER.severe("fastNonDominantSort and fastNonDominantSort2 don't give the same result !");
		
		LOGGER.info("Exit.");
	}
	
	
	
	
}
