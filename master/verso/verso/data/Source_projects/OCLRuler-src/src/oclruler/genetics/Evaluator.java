package oclruler.genetics;


import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.FireMap;
import oclruler.metamodel.Model;
import oclruler.rule.Program;
import oclruler.rule.struct.Constraint;
import oclruler.utils.Config;
import oclruler.utils.ToolBox;

/**
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public abstract class Evaluator {
	public final static Logger LOGGER = Logger.getLogger(Evaluator.class.getName());

	static Evaluator instance;
	
	protected long numberOfEvaluations = 0;
	long evaluationTime = 0;

	/**
	 * For testing purpose.
	 */
	Evolutioner evo;
	public void setEvo(Evolutioner evo) {
		this.evo = evo;
	}
	
	
	ExampleSet exampleSet;
	public static int MAX_EXEC_PER_THREAD = 3;
	public static boolean MULTI_THREAD = true;
	public static GRAIN EXECUTION_GRAIN = GRAIN.RAW;
	
	public enum GRAIN {
		RAW, FINE;

		public boolean isRaw() {
			return this == RAW;
		}
		public boolean isFine() {
			return this == FINE;
		}
	}
	
	/**
	 * 
	 * @param ms : <code>Oraculized</code> model set.
	 */
	public Evaluator(ExampleSet ms) {
		this.exampleSet = ms;
	}
	
	public ExampleSet getExampleSet() {
		return exampleSet;
	}
	
	public FitnessVector evaluate(GeneticIndividual e) {
		return evaluate(e, false);
	}

	public FitnessVector evaluate(GeneticIndividual e, boolean force) {
		FitnessVector fv = e.getFitnessVector();
		if (force || fv == null || e.isModified()) {
			synchronized (e) {
				if (MULTI_THREAD) {
					fv = evaluateMultiThread(e);
					if (LOGGER.isLoggable(Level.FINER))
						LOGGER.finer(e + " (" + e.size() + ") Multi thread evaluation : " + fv);
				} else {
					fv = evaluateMonoThread(e);
					if (LOGGER.isLoggable(Level.FINER))
						LOGGER.finer(e + " (" + e.size() + ") Single thread evaluation : " + fv);
				}
				e.setFitnessVector(fv);
			}
		}
		return fv;
	}
	
	/**
	 * Simulate an evaluation : but JESS is off and avg (0.5/1) values are given.<br/>
	 * Evolutioner 'evo' must be set !
	 * @param e
	 * @return
	 */
	protected FitnessVector evaluateFAKE(GeneticIndividual e) {
		if (evo == null) {
			LOGGER.severe("An evolutioner must be set to run FAKE evaluation.");
			System.exit(1);
		}
		try {
			LOGGER.finer("Fake evaluation.");
			Thread.sleep(5);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		Program prg = (Program) e;
		FitnessVector fv = new FitnessVector(prg);
		ArrayList<Model> list = new ArrayList<>(exampleSet.getPositives());
		list.addAll(exampleSet.getNegatives());
		for (int j = 0; j < list.size(); j++) {
			Model m = list.get(j);
			for (Constraint p : prg.getConstraints()) {
				p.check(m);
			}
			//// JessExecutor.executeJess(engine, m, prg);
			for (Constraint p : prg.getConstraints()) {
				p.setFires(ToolBox.getRandomInt(5));
				fv.addFire(m, p);
				p.cleanFires();
			}
		}
		fv.update();
		prg.setFitnessVector(fv);
		return fv;
	}

	public static <T> ArrayList<Collection<T>> dichoSplit(Collection<T> c, int max_size){
		int l = c.size();
		ArrayList<Collection<T>> res = new ArrayList<>();
		if(l <= max_size) {
			res.add(c);
			return res;
		} else {
			res.addAll(dichoSplit(ToolBox.split(c).get(0), max_size));
			res.addAll(dichoSplit(ToolBox.split(c).get(1), max_size));
			return res;
		}
	}
	
	/**
	 * To be distributed on more than one CPU !
	 * @param e MUST BE A <code>Program</code>
	 * @return
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public FitnessVector evaluateMultiThread(GeneticIndividual e) {
		long timeBefore = System.currentTimeMillis();
		Program prg = (Program) e;
		FitnessVector fv = new FitnessVector(prg);

		
		ArrayList<Model> list = new ArrayList<>(exampleSet.getPositives());
		list.addAll(exampleSet.getNegatives());
		ArrayList<Collection<Model>> models = dichoSplit(list, MAX_EXEC_PER_THREAD);
		
		ArrayList<Callable<FireMap>> execs = new ArrayList<>(models.size());
		for (Collection<Model> collection : models)
			execs.add( new Caller(collection, prg));
		
		// Pool with 4 threads
		ArrayList<Future<FireMap>> futures = new ArrayList<>(models.size());
		ExecutorService pool = Executors.newFixedThreadPool(execs.size());
		for (Callable<FireMap> call : execs) 
			futures.add(pool.submit(call));
		
		
		
		pool.shutdown();
		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
			
			if(LOGGER.isLoggable(Level.FINEST))
				LOGGER.finest("Evaluation's duration: "+ToolBox.formatMillis(System.currentTimeMillis()-timeBefore));
		
			// Get results 
			FireMap resultingFireMap = new FireMap();
			for (Future<FireMap> future : futures)
				try {
					resultingFireMap.merge(future.get(Long.MAX_VALUE, TimeUnit.MILLISECONDS));
				} catch (TimeoutException e1) {
					e1.printStackTrace();
				}
			
			fv.setFires(resultingFireMap);

		} catch (InterruptedException | ExecutionException e1) {
			e1.printStackTrace();
		}
		
		fv.update();

		prg.setFitnessVector(fv);
		return fv;
	}

	public class Caller implements Callable<FireMap> {
		Program prg; Collection<Model> modelList;
		@Override
		public FireMap call() throws Exception {
			long t = System.currentTimeMillis();
			FireMap res = execute(modelList, prg);
			if(LOGGER.isLoggable(Level.FINEST))
				LOGGER.finest("  - executeOCL() duration: "+ToolBox.formatMillis(System.currentTimeMillis()-t));
			return res;
		}

		public Caller(Collection<Model> ms, Program prg) {
			super();
			this.prg = prg;
			this.modelList = ms;
		}
	}

	protected abstract FireMap execute(Collection<Model> modelList, Program prg);
	
	
	
	
	/**
	 * To be distributed on more than one CPU !
	 * @param e MUST BE A <code>Program</code>
	 * @return
	 */
	public FitnessVector evaluateMonoThread(GeneticIndividual e){
		long timeBefore = System.currentTimeMillis();
		Program prg = (Program)e;
		FitnessVector fv = new FitnessVector(prg);
		
		FireMap fm = execute(exampleSet.getPositives(), prg);
		fm.merge(execute(exampleSet.getNegatives(), prg));
		if(LOGGER.isLoggable(Level.FINEST))
			LOGGER.finest("Evaluation's duration: "+ToolBox.formatMillis(System.currentTimeMillis()-timeBefore));


		fv.setFires(fm);
		fv.update();
		prg.setFitnessVector(fv);
		return fv;
	}

	public static void loadConfig() {
		MULTI_THREAD 		= Config.getBooleanParam("MULTI_THREAD");
		MAX_EXEC_PER_THREAD = Config.getIntParam("MAX_EXEC_PER_THREAD");
				
		try {
			String exec_grain = Config.getStringParam("EXECUTION_GRAIN");
			switch(exec_grain.toLowerCase()){
			case "raw" : 	EXECUTION_GRAIN = GRAIN.RAW;  break;
			case "fine" :	EXECUTION_GRAIN = GRAIN.FINE; LOGGER.warning("EXECUTION_GRAIN = 'Fine' but the feature is not implemented."); break;
			default : 		EXECUTION_GRAIN = GRAIN.RAW;  break;
			}
		} catch (Exception e) {
			LOGGER.warning("EXECUTION_GRAIN not set : RAW selected.");
			EXECUTION_GRAIN = GRAIN.RAW;
		}
	
	}
}
