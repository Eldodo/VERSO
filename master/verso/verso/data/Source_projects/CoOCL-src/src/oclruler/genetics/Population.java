package oclruler.genetics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.ml.clustering.CentroidCluster;

import com.sun.swing.internal.plaf.synth.resources.synth_pt_BR;

import coocl.ocl.CollectOCLIds;
import coocl.ocl.Contrainte;
import coocl.ocl.Program;
import oclruler.genetics.FitnessVector.OBJECTIVE;
import oclruler.metamodel.Metamodel;
import utils.Config;
import utils.Utils;
import utils.distance.DoublePointProgram;

public class Population {
	public final static Logger LOGGER = Logger.getLogger(Population.class.getName());
	
	public static int NB_ENTITIES_IN_STAT_PRINTING = 0;// *P
	/** How many program in the populations */
	public static int POPULATION_SIZE = 0;// *P

	public static double MUTATE_RATE = 0;
	public static double CROSSING_RATE = 0;

	public static int nbPop = 0;
	private static final int EMPHASIS_RATE = 5;

	ArrayList<GeneticEntity> entities;
	int number;
	ArrayList<Pareto> paretos;

	public static void loadConfig() {
		// Second load call
		POPULATION_SIZE = Config.getIntParam("POPULATION_SIZE");
		NB_ENTITIES_IN_STAT_PRINTING = Config.getIntParam("NB_ENTITIES_IN_STAT_PRINTING");

		MUTATE_RATE = Config.getDoubleParam("MUTATE_RATE");
		CROSSING_RATE = Config.getDoubleParam("CROSSING_RATE");
	}

	public Population() {
		entities = new ArrayList<>(POPULATION_SIZE);
		paretos = new ArrayList<>(3);
		number = nbPop++;
	}
	
	public static Population loadFromDirectory(File directory){
		Population pop = new Population();
		for (File f : directory.listFiles()) {
			CollectOCLIds<?, ?, ?, ?, ?, ?, ?, ?, ?> collector =  CollectOCLIds.newCollectOCLId(Metamodel.getMm1()); 
			try {
				Program p = collector.load(f, false);
				pop.addEntity(p);
			} catch (Exception e) {
				System.out.println("Could not load '"+f.getAbsolutePath()+"'.");
//				e.printStackTrace();
			}
		}
		return pop;
	}

	public ArrayList<GeneticEntity> getEntities() {
		return entities;
	}

	public ArrayList<GeneticEntity> orderEntitiesWithMonoValue() {
		Collections.sort(entities, GeneticEntity.getMonoValueComparator());
		return entities;
	}

	public boolean addEntity(GeneticEntity e) {
		if (entities.contains(e) || e == null || (e != null && e.isEmpty()) )
			return false;
		return entities.add(e);
	}

	public GeneticEntity removeLastEntity() {
		if (!entities.isEmpty()) {
			return entities.remove(entities.size() - 1);
		}
		return null;
	}

	public ArrayList<GeneticEntity> getEntities_proba_rank() {
		// System.out.println("Population.getEntities_proba_rank()");
		ArrayList<GeneticEntity> res = new ArrayList<>();
		int maxRank = paretos.size();
		if(maxRank == 1) {
			Collections.sort(paretos, getParetoRankComparator());
		} else
			for (GeneticEntity entity : entities) {
				res.add(entity.clone());
				for (int i = 0; i < (maxRank - entity.rank) * EMPHASIS_RATE; i++)
					res.add(entity);//TODO .clone() ?//NO NEED : clone is done afterward when using the res;
			}
		return res;
	}

	/**
	 * Geometric redistribution of entities :
	 * last pareto : nbEntities = x(averagesize ^2)
	 * second last : nbEntities = nbEntities + x(averagesize ^2)
	 * @return
	 */
	public ArrayList<GeneticEntity> getEntities_proba_rank_geometric() {
		if (paretos.size() <= 1)
			return getEntities_proba_monoObj();
		ArrayList<GeneticEntity> res = new ArrayList<>();
		Collections.sort(paretos, getParetoRankComparator());
		int nbEnt = 0;
		float avgSize = 0;
		for (int i = 0; i < paretos.size(); i++) {
			Pareto p = paretos.get(i);
			avgSize += p.size();
		}
		avgSize = avgSize / paretos.size();

		nbEnt = paretos.get(paretos.size() - 1).size();
		// System.out.println(nbEnt);
		String log = "";
		for (int i = paretos.size() - 1; i >= 0; i--) {
			Pareto p = paretos.get(i);

			nbEnt += (int) Math.pow(avgSize, 2.5);
			// System.out.println(nbEnt +"\t"+p.size() +" = " +(nbEnt *
			// p.size()));

			int s = res.size();
			while (res.size() < s + nbEnt) {// res.addAll(p.getEntities());
				for (GeneticEntity geneticEntity : p.getEntities()) {
					res.add(geneticEntity);// TODO .clone() ?//NO NEED : clone
											// is done afterward when using the
											// res;
				}
			}
			log += " " +( res.size()-s);
			// System.out.println(i+".\t"+res.size());
			// for (int j = 0; j < nbEnt; j++)
			// res.addAll(p.getEntities());
		}
//		System.out.println("1. Population.getEntities_proba_rank_geometric:" + log);
//		Old version : squares.
//		nbEnt = 0;
//		for (Pareto p : paretos) {
//			nbEnt = (int) Math.pow(maxRank - p.getRank(), 2 );
//			// System.out.println("  "+p+" : "+(maxRank-p.getRank())+" -> "+nbEnt);
//			for (int j = 0; j < nbEnt; j++)
//				res.addAll(p.getEntities());
//		}
		return res;
	}

	/**
	 * 
	 * @return
	 */
	public ArrayList<GeneticEntity> getEntities_proba_monoObj() {
		ArrayList<GeneticEntity> res = new ArrayList<>();
		ArrayList<Integer> toAdds = new ArrayList<>();
		int maxToAdd = 0, minToAdd = Integer.MAX_VALUE;
		for (GeneticEntity entity : entities) {
			int toAdd =  (int)(entity.getMonoValue() * 100 );
			maxToAdd = Math.max(maxToAdd, toAdd);
			minToAdd = Math.min(minToAdd, toAdd);
		}
		
		for (GeneticEntity entity : entities) {
			int toAdd =  (maxToAdd - (int)(entity.getMonoValue() * 100 ) + minToAdd +1)*EMPHASIS_RATE ;
//			System.out.println(toAdd + " : "+entity.getMonoValue()*100 + " :: "+(toAdd/entity.getMonoValue()*100));
			toAdds.add(toAdd);
			for (int i = 0; i < toAdd; i++) {
				res.add(entity);//TODO .clone() ? //NO NEED : clone is done afterward when using the res;
			}
		}
		return res;
	}

	
	static int loop = 1;
	public ArrayList<GeneticEntity> getRouletteExtendedEntities(){
		ArrayList<GeneticEntity> extendFromRate = null;
		if(Evolutioner.ROULETTE_GEOMETRIC )
			extendFromRate = this.getEntities_proba_rank_geometric();
		else
			extendFromRate = this.getEntities_proba_rank();
		return extendFromRate;
	}
	
	public static boolean TIMED_VERBE = false;
	public static Population createRandomPopulation(Program seed, Evaluator eva) {
		Population p = new Population();
		LOGGER.fine(p.getName()+" with " + seed.getName());
		p.addEntity(seed);
		for (int i = 1; i < POPULATION_SIZE/2; i++) {
			Program seed2 = (Program) seed.clone();
			try {
				seed2.mutate();
			} catch (UnstableStateException e) {
				e.printStackTrace();
			}
			p.addEntity(seed2);
			seed = seed2;
			if(LOGGER.getLevel() != null && LOGGER.getLevel().equals(Level.FINEST))
				LOGGER.finest("Mutation : added : "+seed2.prettyPrint()+p.size());
			else
				LOGGER.finer("Mutation : " + seed.getName());
		}
		if(eva != null){
			p.evaluate(eva);
			int i = 0, maxTries = POPULATION_SIZE*5;
			while (p.size() < POPULATION_SIZE) {
				ArrayList<GeneticEntity> fathersRouletteExtended = p.getRouletteExtendedEntities();
				LOGGER.finer(i+".Population size: "+p.size()+" / crossover candidates: "+fathersRouletteExtended.size());
				GeneticEntity[] sons2;
				try {
					sons2 = Evolutioner.crossover(fathersRouletteExtended);
					
					
					boolean add = false;
					boolean add2 = false;
					if(i++ < maxTries) {
						add = p.addEntity(sons2[0]);
						add2 = p.addEntity(sons2[1]);
					} else {
						add = p.getEntities().add(sons2[0]);
						add2 = p.getEntities().add(sons2[1]);
					}
					
					if(add)
						eva.evaluate(sons2[0]);
					
					if(add2)
						eva.evaluate(sons2[1]);
					
				} catch (UnstableStateException e2) {
					sons2 = null;
					e2.printStackTrace();
				}
			}
		}
		return p;
	}
	
	public static Population createRandomPopulation_forSanityCheck(Program seed, int numberOfEntities) {
		Population p = new Population();
		LOGGER.fine(numberOfEntities+" entities to explore from " + seed.getName());
		p.addEntity(seed);
		for (int i = 1; i < POPULATION_SIZE/2; i++) {
			Program seed2 = (Program) seed.clone();
			try {
				seed2.mutate();
			} catch (UnstableStateException e) {
				e.printStackTrace();
			}
			p.addEntity(seed2);
			seed = seed2;
			if(LOGGER.getLevel() != null && LOGGER.getLevel().equals(Level.FINEST))
				LOGGER.finest("Mutation : added : "+seed2.prettyPrint()+p.size());
			else
				LOGGER.finer("Mutation : " + seed.getName());
		}
		Evaluator eva = new Evaluator();
		if(eva != null){
			p.evaluate(eva);
			int i = 0, maxTries = numberOfEntities*5;
			while (p.size() < numberOfEntities) {
				ArrayList<GeneticEntity> fathersRouletteExtended = p.getRouletteExtendedEntities();
				LOGGER.finer(i+".Population size: "+p.size()+" / crossover candidates: "+fathersRouletteExtended.size());
				GeneticEntity[] sons2;
				try {
					sons2 = Evolutioner.crossover(fathersRouletteExtended);
					
					
					boolean add = false;
					boolean add2 = false;
					if(i++ < maxTries) {
						add = p.addEntity(sons2[0]);
						add2 = p.addEntity(sons2[1]);
					} else {
						add = p.getEntities().add(sons2[0]);
						add2 = p.getEntities().add(sons2[1]);
					}
					
					if(add)
						eva.evaluate(sons2[0]);
					
					if(add2)
						eva.evaluate(sons2[1]);
					
				} catch (UnstableStateException e2) {
					sons2 = null;
					e2.printStackTrace();
				}
			}
		}
		return p;
	}

	public Population cutPopulationInHalf() {
		Population G1;
		G1 = new Population();
		Pareto lastPareto = null, pr;
		for (int i = 0; i < this.paretos.size(); i++) {
			pr = this.getPareto(i);
			if(G1.getEntities().size() + pr.size() < Population.POPULATION_SIZE){
				G1.addPareto(pr);
			} else {
				lastPareto = pr;
				break;
			}
		}
		//Cutting last pareto to get POP_SIZE/2 new population.
		if(lastPareto != null) {
			Collections.sort(lastPareto.entitiesp, GeneticEntity.getDescendantRankComparator());
			removeAfterCutpoint(lastPareto.entitiesp, (Population.POPULATION_SIZE)-G1.getEntities().size());
			G1.addPareto(lastPareto);
		}
		return G1;
	}
    public void removeAfterCutpoint(ArrayList<?> genes, int cutpoint) {
        int times = genes.size()-cutpoint;
        for(int i=1; i<=times; i++)
        	genes.remove(cutpoint);
    }



	public void evaluate(Evaluator ev) {
		GeneticEntity[] ets = (GeneticEntity[]) entities.toArray(new GeneticEntity[entities.size()]);
		for (GeneticEntity entity : ets) {
			FitnessVector fv = ev.evaluate(entity);
			if(fv == null)
				LOGGER.warning(entity.getClass().getSimpleName()+" '"+ entity+ "' could not be evaluated.");
			entity.setFitnessVector(fv);
		}
		fastNonDominantSort2();
		crowdingDistanceAssignement();
	}

	/**
	 * Clone the entites - not Paretos !
	 */
	@Override
	public Population clone() {
		Population p = new Population();
//		p.number = number;
		
		if (paretos != null) {
			Pareto[] prs = new Pareto[paretos.size()];
			paretos.toArray(prs);
			for (Pareto pr : prs)
				p.addPareto(pr.clone());
		} else if (entities != null) {
			GeneticEntity[] ets = new GeneticEntity[entities.size()];
			entities.toArray(ets);
			for (GeneticEntity e : ets)
				p.addEntity(e.clone());
		}

		return p;
	}

	public String printStatistics() {
		return printStatistics(false);
	}

	public String printStatistics(boolean verbose) {
		int i = 0;

		
		GeneticEntity[] ets = (GeneticEntity[]) entities.toArray(new GeneticEntity[entities.size()]);
//		Arrays.sort(ets, GeneticEntity.getDescendantRankComparator());
		Arrays.sort(ets, GeneticEntity.getMonoValueComparator());
		
//		float medSize = 0.0f;
//		int minSize = Integer.MAX_VALUE;
//		int maxSize = -1;
//		for (GeneticEntity entity : ets) {
//			medSize += entity.size();
//			if(entity.size() < minSize)
//				minSize = entity.size();
//			if(entity.size() > maxSize)
//				maxSize = entity.size();
//		}
//		medSize /= ets.length;
		
		String res = "Statistics of Population " + number + " : " + ets.length + " programs\n";
		
		GeneticEntity best = getBest();
		res += "   Best is " + ((best != null) ? best + ":" + best.printNumericResult(",", "", "", false) : "not affected")+"\n";
		
		
		int numberNoSyntaxErr = 0;
		for (GeneticEntity entity : ets) {
			if(entity.getFitnessVector().getValue(OBJECTIVE.NUMBER_OF_SYNTAX_ERRORS.getIdx()) == 0)
				numberNoSyntaxErr++;
			if (i++ < NB_ENTITIES_IN_STAT_PRINTING || verbose) {
				res += "   " + entity.getName() +": "+ entity.getFitnessVector().expandedStat()+" "+entity.printRank();
				res += "\n";
			} else if (!verbose && i == NB_ENTITIES_IN_STAT_PRINTING + 1)
				res += "   ...\n";
			else if (i >= ets.length)
				res += "   " + entity.getName() +": "+ entity.getFitnessVector().expandedStat()+" "+entity.printRank() +"\n";
		}
		res += "\nClean solutions:"+numberNoSyntaxErr+" ("+OBJECTIVE.NUMBER_OF_SYNTAX_ERRORS.getDescription()+"= 0)";
		return res;
	}

	public void storeFrontParetoInFileSystem(File f, String timeStamp) {
		GeneticEntity[] ets = (GeneticEntity[]) entities.toArray(new GeneticEntity[entities.size()]);
		if (getFrontPareto() != null)
			ets = (GeneticEntity[]) getFrontPareto().getEntities().toArray(new GeneticEntity[getFrontPareto().getEntities().size()]);
		Arrays.sort(ets, GeneticEntity.getDescendantRankComparator());
		Pareto p = getFrontPareto();
		File root = new File(f.getAbsolutePath() + File.separator + "res" + timeStamp);
		root.mkdir();
		Level lvl = LOGGER.getLevel();
		// LOGGER.setLevel(Level.FINER);
		LOGGER.finer("  Root : " + root.getAbsolutePath());
		File solutionFile;
		
		int i = 0;
		int nbEntitiess = 0;
		int nbEntitiesStored = 0;
		boolean alright = true;
		for (GeneticEntity e : p.getEntities()) {
			
			File folder = new File(root.getAbsolutePath() + File.separator + "solution" + i);
			folder.mkdir();
			solutionFile = new File(folder.getAbsolutePath() + File.separator + "solution" + i + ".cpl");
			BufferedWriter bwSOlution = null;
			try {
				bwSOlution = new BufferedWriter(new FileWriter(solutionFile));
				bwSOlution.append(e.getName());
				
			} catch (IOException e1) {
				e1.printStackTrace();
			} 
			if(bwSOlution != null)	try {	bwSOlution.close();	} catch (IOException e1) {	}
			
			LOGGER.finer("Folder : " + folder.getAbsolutePath());
			i++;
		}
		LOGGER.info("Result stored : " + (i) + " solutions (" + nbEntitiesStored + ((nbEntitiesStored != nbEntitiess) ? "/" + nbEntitiess : "") + " entities in total)");
		if (!alright)
			LOGGER.warning((nbEntitiess - nbEntitiesStored) + " patterns missing.");
		LOGGER.setLevel(lvl);
	}

	public ArrayList<Pareto> getParetos() {
		return paretos;
	}

	public Pareto getFrontPareto() {
		if (paretos == null || paretos.isEmpty())
			return null;
		return paretos.get(0);
	}

	public Pareto getPareto(int index) {
		return paretos.get(index);
	}

	public void addPareto(Pareto p) {
		for (GeneticEntity entity : p.getEntities()) {
			addEntity(entity);
		}
		this.paretos.add(p);
	}

	
	public ArrayList<Pareto> fastNonDominantSort() {
		paretos = new ArrayList<>(); // Re-init paretos
		ArrayList<GeneticEntity> Sp = new ArrayList<>(POPULATION_SIZE);
		Sp.addAll(entities);
		Pareto Fi = null;
		int front = 0;
		do {
			Fi = extractFront(Sp, front);
			Sp.removeAll(Fi.getEntities());
			if (!Fi.isEmpty()) {
				paretos.add(Fi);
				Fi.rank = front;
				for (GeneticEntity e : Fi.getEntities())
					e.rank = front;
			}
			front++;
		} while (!Sp.isEmpty());
		return paretos;
	}

	public Pareto extractFront(ArrayList<GeneticEntity> entities, int frontNb) {
		ArrayList<GeneticEntity> Sp = new ArrayList<>(POPULATION_SIZE);
		Pareto Fi = new Pareto();
		for (GeneticEntity p : entities) {
			int np = 0;
			for (GeneticEntity q : entities) {
				if (p.dominates(q)) // if p dominates q then
					Sp.add(q); // then SP.add q
				else if (q.dominates(p)) // if p is dominated by q
					np++; // increment np
			}
			if (np == 0) { // No solution dominates p
				Fi.add(p); // p is a memeber of the first front
				p.rank = frontNb;
			}
		}
		return Fi;
	}

	public ArrayList<Pareto> fastNonDominantSort2() {
		paretos = new ArrayList<>(); // Re-init paretos
		HashMap<GeneticEntity, ArrayList<GeneticEntity>> Sp = new HashMap<>();
		// ArrayList<Pareto> Fi = new ArrayList<>();
		Pareto pr = new Pareto();
		paretos.add(pr);
		for (GeneticEntity p : entities) {
			p.rank = 0;
			Sp.put(p, new ArrayList<GeneticEntity>());
			for (GeneticEntity q : entities) {
				if (p.dominates(q)) // if p dominates q then
					Sp.get(p).add(q); // then SP.add q
				else if (q.dominates(p)) // if p is dominated by q
					p.rank++; // increment np
			}
			if (p.rank == 0) { // No solution dominates p
				pr.add(p); // p is a memeber of the first front
				pr.rank = 0;
				p.rank = 0;
			}
		}
		int i = 0;

		while (!paretos.get(i).isEmpty()) {
			pr = new Pareto();
			for (GeneticEntity p : paretos.get(i).entitiesp) {
				for (GeneticEntity q : Sp.get(p)) {
					q.rank--;
					if (q.rank == 0) {
						pr.add(q);
						p.rank = i;
					}
				}
			}
			i++;
			paretos.add(pr);
			pr.rank = i;
		}
		if(paretos.get(paretos.size()-1).isEmpty())
			paretos.remove(paretos.size()-1);

		return paretos;
	}

	public void crowdingDistanceAssignement() {
		int l = this.size();
		for (GeneticEntity e : this.getEntities())
			e.distance = 0;

		for (int i = 0; i < FitnessVector.NUMBER_OF_OBJECTIVES; i++) {
			this.sort(i);// CERTIFIED
			this.getEntities().get(0).distance = Integer.MAX_VALUE;

			if (l > 1) {
				this.getEntities().get(this.size() - 1).distance = Integer.MAX_VALUE;
				for (int j = 1; j < l - 2; j++) {
					double v = this.getEntities().get(j).getFitnessVector().getValue(i);
					v += this.getEntities().get(j + 1).getFitnessVector().getValue(i) - this.getEntities().get(j - 1).getFitnessVector().getValue(i);
					this.getEntities().get(j).distance = v;
				}
			}
		}
	}

	public class Pareto {
		ArrayList<GeneticEntity> entitiesp;
		int rank = -1;

		public Pareto() {
			this.entitiesp = new ArrayList<>();
		}

		public int size() {
			return entitiesp.size();
		}

		public boolean isEmpty() {
			return entitiesp.isEmpty();
		}

		public boolean add(GeneticEntity e) {
			if (entitiesp.contains(e))
				return false;
			return this.entitiesp.add(e);
		}

		public void clear() {
			this.entitiesp = new ArrayList<>();
		}

		public ArrayList<GeneticEntity> getEntities() {
			return this.entitiesp;
		}

		public GeneticEntity get(int index) {
			return this.entitiesp.get(index);
		}

		public int getRank() {
			return rank;
		}

		@Override
		public String toString() {
			String res = "Pareto(" + rank + ") : " + entitiesp.size();
			return res;
		}

		@Override
		public Pareto clone() {
			Pareto p = new Pareto();
			GeneticEntity[] ets = new GeneticEntity[entitiesp.size()];
			entitiesp.toArray(ets);
			for (GeneticEntity entity : ets) {
				p.add(entity.clone());
			}
			return p;
		}

	}

	public static Comparator<Pareto> getParetoRankComparator() {
		return new Comparator<Pareto>() {
			@Override
			public int compare(Pareto o1, Pareto o2) {
				return Integer.compare(o2.getRank(), o1.getRank());
			}
		};
	}

	/*
	 * 
	 */
	public void sort(final int obj) {
		Collections.sort(entities, new Comparator<GeneticEntity>() {
			@Override
			public int compare(GeneticEntity o1, GeneticEntity o2) {
				return Double.compare(o2.getFitnessVector().getValue(obj), o1.getFitnessVector().getValue(obj));
			}
		});
	}

	public int size() {
		return entities.size();
	}

	public void clean() {
		entities = null;
		paretos = null;
	}

	/**
	 * USING FV.monoValue
	 *
	 * @return
	 */
	public GeneticEntity getBest() {
		GeneticEntity max = null;
		double dist = Double.MAX_VALUE;
		if (paretos == null || paretos.isEmpty())
			return null;
//		for (GeneticEntity e : getFrontPareto().getEntities()) {
//			double distTmp = Math.sqrt(Math.pow(1 - e.getFitnessVector().getValue(0), 2) + Math.pow(1 - e.getFitnessVector().getValue(1), 2) + Math.pow(1 - e.getFitnessVector().getValue(2), 2));
////			double distTmp = e.getFitnessVector().getMonoValue();
//			if (distTmp < dist) {
//				dist = distTmp;
//				max = e;
//			}
//		}
			for (GeneticEntity e : getFrontPareto().getEntities()) {
				double distTmp = e.getMonoValue();
				if (distTmp < dist) {
					dist = distTmp;
					max = e;
				}
			}
		return max;
	}
	
	@Override
	public String toString() {
		return getName()+": "+entities.size()+" entities";
	}

	public String getName(){
		return "Pop_"+number;
	}
	
	public String prettyPrint() {
		String res = "Pop_"+number+": "+entities.size()+" entities";
		for (GeneticEntity geneticEntity : entities) {
			res += "\n  + "+geneticEntity.getName()+" : ";
			if(geneticEntity.getFitnessVector()!=null)
				res += geneticEntity.getFitnessVector().prettyPrint();
			else res += " (Not evaluated yet)";
		}
		
		return res;
	}

	public void forceEvaluation() {
		
	}
	
	public boolean clustersCentersContainsSolution(){
		List<CentroidCluster<DoublePointProgram>> clustersKM = Evolutioner.clusterFrontParetoFromPopulation(this);
		// On recupere les centres des clusters
		for (CentroidCluster<DoublePointProgram> clusterKM : clustersKM) {
			// For each cluster check if solution and compute distance to
			// GroundTruth
			DoublePointProgram center = Evolutioner.extractCenterFromCluster(clusterKM);
			if(center.equals(Program.getExpectedSolution()))
				return true;
		}
		return false;
	}
	
	public String printStatisticsLine() {
		String res = "";
		//Present in pareto ?
		res += ((Evolutioner.checkPresenceOfGroundTruthInPopulation(this, Program.getExpectedSolution()) != null) ? 1 : 0) + ";";
		double minDist = Double.MAX_VALUE, avgDist = 0.0;
		Program closest = null;
		for (GeneticEntity ge : getFrontPareto().getEntities()) {
			Program p = (Program) ge;
			double dist = p.getDistance(Program.getExpectedSolution());
			if( dist < minDist){
				minDist = p.getDistance(Program.getExpectedSolution());
				closest = p;
			}
			avgDist += dist;
		}
		int numberOfConstraintsFound = 0;
		for (double dist : closest.computeDamerauLevensteinDistances(Program.getExpectedSolution())) {
			if(dist == 0.0)
				numberOfConstraintsFound++;
		}
		
		//mininum and average distance in pareto (to ground truth)
		res += numberOfConstraintsFound+";"+minDist + ";" + String.format ("%.03f",avgDist/getFrontPareto().size())+";";

		double avgDistanceToGT = 0.0;
		double minDistanceToGT = Double.MAX_VALUE;
		List<CentroidCluster<DoublePointProgram>> clustersKM = Evolutioner.clusterFrontParetoFromPopulation(this);
		// On recupere les centres des clusters
		boolean trouve = false;
		for (CentroidCluster<DoublePointProgram> clusterKM : clustersKM) {
			// For each cluster check if solution and compute distance to
			// GroundTruth
			DoublePointProgram center = Evolutioner.extractCenterFromCluster(clusterKM);
			double distance = center.getProgram().computeDamerauLevensteinDistanceSum(Program.getExpectedSolution());
			avgDistanceToGT += distance;
			if(distance < minDistanceToGT) {
				minDistanceToGT = distance;
				closest = center.getProgram();
			}
			
			if(center.equals(Program.getExpectedSolution()))
				trouve = true;
		}
		numberOfConstraintsFound = 0;
		for (double dist : closest.computeDamerauLevensteinDistances(Program.getExpectedSolution())) {
			if(dist == 0.0)
				numberOfConstraintsFound++;
		}
		avgDistanceToGT = avgDistanceToGT / clustersKM.size();
		//mininum and average distance in centroids (to ground truth)
		res += Evolutioner.NUMBER_OF_CLUSTERS + ";" + (trouve ? 1 : 0) + ";"+numberOfConstraintsFound+";"+ String.format ("%.03f",minDistanceToGT)+ ";"+ String.format ("%.03f",avgDistanceToGT);
		return res;
	}
}
