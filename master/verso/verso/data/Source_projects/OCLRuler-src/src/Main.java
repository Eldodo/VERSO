import java.util.logging.Level;
import java.util.logging.Logger;

import oclruler.genetics.Evaluator;
import oclruler.genetics.EvaluatorOCL;
import oclruler.genetics.Evolutioner;
import oclruler.genetics.Oracle;
import oclruler.genetics.Population;
import oclruler.metamodel.ExampleSet;
import oclruler.rule.struct.Constraint;
import oclruler.ui.Ui;
import oclruler.utils.ToolBox;


public class Main {
	public final static Logger LOGGER = Logger.getLogger(Main.class.getName());
	
	
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
//		Utils.printLOC();
//		System.exit(1);
		
		LOGGER.info("Entering OCLRuler\n");
		ToolBox.init();
		
		ExampleSet ms = ExampleSet.getInstance();
		Evaluator eva = new EvaluatorOCL(ms);
		Oracle o = Oracle.getInstance();
		
		long t = System.currentTimeMillis();
		o.oraculize(eva);
		LOGGER.finer("Oraculization lasted "+(System.currentTimeMillis() - t)+"ms.");
		
		
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
		
		Population p0 = Population.createRandomPopulation();
		LOGGER.fine(p0.toString());
		Evolutioner evo = new Evolutioner(eva, p0);
		Ui.getInstance().setEvolutioner(evo);
		
		Population pN = evo.evolutionate();
		
		

				
		System.out.println("Exit OCLRuler.");
	}

	

}
