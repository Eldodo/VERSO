import java.util.logging.Level;
import java.util.logging.Logger;

import oclruler.genetics.EvaluatorOCL;
import oclruler.genetics.Evolutioner;
import oclruler.genetics.Population;
import oclruler.metamodel.ExampleSet;
import oclruler.ui.Ui;
import oclruler.utils.Config;
import oclruler.utils.ToolBox;


public class OCLRuler {
	public final static Logger LOGGER = Logger.getLogger(OCLRuler.class.getName());
	
	//try a change
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
//		 Utils.printLOC();
//		 System.exit(1);
		LOGGER.info("Entering OCLRuler");
		
		
		ToolBox.loadArgs(args);
		if(LOGGER.isLoggable(Level.CONFIG))
			ToolBox.printArgs();

		if(Config.EXAMPLE_EDITION_MODE)
			LOGGER.info("Example edition mode - on\n");
		
		
		String log = ToolBox.init();
		
		if (Config.IS_RANDOM_RUN)
			Evolutioner.GENERATION_MAX = 500;
		
		boolean incrementalInjection = (ExampleSet.NEGATIVES_CONSIDERED + ExampleSet.POSITIVES_CONSIDERED) != ExampleSet.getNbModels();
		
		String resFolderName = (Config.IS_RANDOM_RUN?"rnd":"evo")+
				(Config.TFIDF.isObjective()?"_tfidf":"")+
				(Config.TFIDF.isCrowdingDistance()?"_tfidfcd":"")+
				(incrementalInjection?"_inc":"")+
				"_"+ToolBox.START_TIME;
		
		if(Config.VERBOSE_ON_FILE)
			ToolBox.touchResultsMainDirectory(resFolderName);
		Ui.init(log);
		
		Population p0 = Population.createRandomPopulation();
		LOGGER.finer(p0.toString());
		
		Evolutioner evo = new Evolutioner(EvaluatorOCL.getInstance(), p0);
		if(!Config.SINBAD )
			Ui.getInstance().setEvolutioner(evo);
		
		p0 = evo.evolutionate();
		
//		System.out.println(p0.printStatistics());
				
		System.out.println("Safe-Exit OCLRuler "+Config.METAMODEL_NAME+"'"+resFolderName+".");
	}
	

}
