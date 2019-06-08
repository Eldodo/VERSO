package genetic;

import genetic.fitness.FitnessVector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import utils.Config;
import utils.Utils;

public class ParetoListenerImpl implements ParetoListener {
	public final static Logger LOGGER = Logger.getLogger(Evolutioner.class.getName());
	private File outFile, outDataFile;
	Evolutioner evo;
	
	BufferedWriter bw, bwData;
	public ParetoListenerImpl(Evolutioner evo) {
		this.evo = evo;
		if(Evolutioner.CHECK_POINT_TIME < 0 )
			return;
		
		
		String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmm", Locale.FRANCE).format( new Date() );
    	String timeStamp2 = timeStamp;
    	String ev = evo.getEvaluator().getClass().getSimpleName();
    	if(ev.endsWith("FragmentSet")) ev = ev.substring(0, ev.indexOf("FragmentSet"));
    	String fileOutName = Config.DIR_OUT+"log_t_"+Config.METAMODEL_NAME+"_"+ev+"_"+Evolutioner.GENERATION_MAX+"_"+Population.NB_ENTITIES_IN_POP+"_"+Population.NB_GENES_IN_ENTITIES+"_"+timeStamp+".log";
    	String fileOutDataName = Config.DIR_OUT+"log_t_"+Config.METAMODEL_NAME+"_"+ev+"_"+Evolutioner.GENERATION_MAX+"_"+Population.NB_ENTITIES_IN_POP+"_"+Population.NB_GENES_IN_ENTITIES+"_"+timeStamp+".data.log";
    	int itmp = 1;
    	while(new File(fileOutName).exists() ||new File(fileOutDataName).exists()){
    		timeStamp2 = timeStamp+"-"+(++itmp);
    		fileOutName = Config.DIR_OUT+"log_t_"+Config.METAMODEL_NAME+"_"+ev+"_"+Evolutioner.GENERATION_MAX+"_"+Population.NB_ENTITIES_IN_POP+"_"+Population.NB_GENES_IN_ENTITIES+"_"+timeStamp2+".log";
        	fileOutDataName = Config.DIR_OUT+"log_t_"+Config.METAMODEL_NAME+"_"+ev+"_"+Evolutioner.GENERATION_MAX+"_"+Population.NB_ENTITIES_IN_POP+"_"+Population.NB_GENES_IN_ENTITIES+"_"+timeStamp2+".data.log";
    	}
    	timeStamp = timeStamp2;
    	outFile = new File(fileOutName);
    	outDataFile = new File(fileOutDataName);
		
		if(!evo.isSuscribedParetoListener(this))
			evo.subscribeParetoListener(this);
		File fo = new File(Config.DIR_OUT);
	   	if(!fo.exists())
	   		fo.mkdir();

	   	if(Config.VERBOSE_ON_FILE){
	   		try {
	   			bw = new BufferedWriter(new FileWriter(new File(fileOutName)));
	   		} catch (IOException e1) {
	   			LOGGER.severe("Couldn't create file : '"+fileOutName+"'.");
	   			e1.printStackTrace();
	   		}
	   		try {				
	   			bwData = new BufferedWriter(new FileWriter(new File(fileOutDataName)));
	   			bwData.write("generation;"+FitnessVector.csvHeader+";elapsedTime;nbModelsBest;nbModelsAvg;sizeModelAvg\n");
	   		} catch (IOException e1) {
	   			LOGGER.severe("Couldn't create file : '"+fileOutDataName+"'.");
	   			e1.printStackTrace();
	   		}
	   	}
	}
	@Override
	public void notify(Population p, int generation, float avgGenes, float avgGeneSize, long timePassed) {
		LOGGER.config("ParetoListenerImpl.notify("+generation+", "+Utils.formatMillis(timePassed)+")"+p.printStatistics(false));
		if(Evolutioner.CHECK_POINT_TIME > 0 && p.getBest() != null)
			Evolutioner.logG_OnFile(bwData, generation, timePassed, p.getBest(), avgGenes, avgGeneSize);
	}

	@Override
	public void notifyEnd(Population currentPopulation, int currentGeneration, float avgGenes, float avgGeneSize, long timePassed) {
		LOGGER.config("ParetoListenerImpl.notifyEnd("+currentGeneration+", "+Utils.formatMillis(timePassed)+")"+currentPopulation.printStatistics(false));
		if(Evolutioner.CHECK_POINT_TIME > 0 ){
			Evolutioner.logG_OnFile(bwData, currentGeneration, timePassed, currentPopulation.getBest(), avgGenes, avgGeneSize);
			Evolutioner.printResult(bw, evo.getEvaluator(), currentPopulation, avgGeneSize, timePassed);
			
			try {
				bw.close();
				bwData.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
