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
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.Model;
import oclruler.rule.Program;
import oclruler.utils.Config;
import oclruler.utils.ToolBox;

/**
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class Population {
	public final static Logger LOGGER = Logger.getLogger(Population.class.getName());

	public static int NB_ENTITIES_IN_STAT_PRINTING = 0;// *P
	/** How many program in the populations */
	public static int NB_ENTITIES_IN_POP = 0;// *P

	public static double MUTATE_RATE = 0;
	public static double CROSSING_RATE = 0;

	public static int nbPop = 0;
	private static final int EMPHASIS_RATE = 10;

	ArrayList<GeneticIndividual> entities;
	int number;
	ArrayList<Pareto> paretos;

	public static void loadConfig() {
		// Second load call
		NB_ENTITIES_IN_POP = Config.getIntParam("NB_ENTITIES_IN_POP");
		NB_ENTITIES_IN_STAT_PRINTING = Config.getIntParam("NB_ENTITIES_IN_STAT_PRINTING");

		MUTATE_RATE = Config.getDoubleParam("MUTATE_RATE");
		CROSSING_RATE = Config.getDoubleParam("CROSSING_RATE");
	}

	public Population() {
		entities = new ArrayList<>(NB_ENTITIES_IN_POP);
		paretos = new ArrayList<>(3);
		number = nbPop++;
		solutions_vs_examples = new double[entities.size()][ExampleSet.getExamplesBeingUsed().size()];
	}

	public ArrayList<GeneticIndividual> getEntities() {
		return entities;
	}

	public ArrayList<GeneticIndividual> orderEntitiesWithMonoValue() {
		Collections.sort(entities, GeneticIndividual.getMonoValueComparator());
		return entities;
	}

	public boolean addEntity(GeneticIndividual e) {
		if (entities.contains(e) || e == null || (e != null && e.isEmpty()) )
			return false;
		return entities.add(e);
	}

	public GeneticIndividual removeLastEntity() {
		if (!entities.isEmpty()) {
			return entities.remove(entities.size() - 1);
		}
		return null;
	}

	public ArrayList<GeneticIndividual> getEntities_proba_rank() {
		// System.out.println("Population.getEntities_proba_rank()");
		ArrayList<GeneticIndividual> res = new ArrayList<>();
		int maxRank = paretos.size();
		if(maxRank == 1) {
			Collections.sort(paretos, getParetoRankComparator());
		} else
			for (GeneticIndividual entity : entities) {
				res.add(entity.clone());
				for (int i = 0; i < (maxRank - entity.rank) * EMPHASIS_RATE; i++)
					res.add(entity);// .clone() ?//NO NEED : clone is done afterward when using the res;
			}
		return res;
	}

	/**
	 * Geometric redistribution of entities :
	 * last pareto : nbEntities = x(averagesizev ^2)
	 * second last : nbEntities = nbEntities + x(averagesizev ^2)
	 * @return
	 */
	public ArrayList<GeneticIndividual> getEntities_proba_rank_geometric() {
		if (paretos.size() <= 1)
			return getEntities_proba_monoObj();
		ArrayList<GeneticIndividual> res = new ArrayList<>();
		Collections.sort(paretos, getParetoRankComparator());
		int nbEnt = 0;
		float avgSize = 0;
		for (int i = 0; i < paretos.size(); i++) {
			Pareto p = paretos.get(i);
			avgSize += p.size();
		}
		avgSize = avgSize / paretos.size();

		nbEnt = paretos.get(paretos.size() - 1).size();
		for (int i = paretos.size() - 2; i >= 0; i--) {
			Pareto p = paretos.get(i);
			nbEnt += (int) Math.pow(avgSize, 2.5);
			// System.out.println(nbEnt +"\t"+p.size() +" = " +(nbEnt *
			// p.size()));
			int s = res.size();
			while (res.size() < s + nbEnt) {// res.addAll(p.getEntities());
				for (GeneticIndividual geneticEntity : p.getEntities()) {
					res.add(geneticEntity);//  .clone() ?//NO NEED : clone
											// is done afterward when using the
											// res;
				}
			}
		}
		return res;
	}

	/**
	 * 
	 * @return
	 */
	public ArrayList<GeneticIndividual> getEntities_proba_obj0() {
		
		ArrayList<GeneticIndividual> res = new ArrayList<>();
		ArrayList<Integer> toAdds = new ArrayList<>();
		int j = entities.size();
		for (GeneticIndividual entity : entities) {
			int toAdd =  (int)(entity.getFitnessVector().getValues_Considered()[0] * EMPHASIS_RATE * j);
			toAdds.add(toAdd);
			for (int i = 0; i < toAdd; i++) {
				res.add(entity);// .clone() ? //NO NEED : clone is done afterward when using the res;
			}
			j--;
		}
		return res;
	}
	
	public ArrayList<GeneticIndividual> getEntities_proba_monoObj() {
		ArrayList<GeneticIndividual> res = new ArrayList<>();
		ArrayList<Integer> toAdds = new ArrayList<>();
		int maxToAdd = 0, minToAdd = Integer.MAX_VALUE;
		for (GeneticIndividual entity : entities) {
			int toAdd =  (int)(entity.getMonoValue() * 100 );
			maxToAdd = Math.max(maxToAdd, toAdd);
			minToAdd = Math.min(minToAdd, toAdd);
		}
		
		for (GeneticIndividual entity : entities) {
			int toAdd =  (maxToAdd - (int)(entity.getMonoValue() * 100 ) + minToAdd +1)*EMPHASIS_RATE ;
//			System.out.println(toAdd + " : "+entity.getMonoValue()*100 + " :: "+(toAdd/entity.getMonoValue()*100));
			toAdds.add(toAdd);
			for (int i = 0; i < toAdd; i++) {
				res.add(entity);// .clone() ? //NO NEED : clone is done afterward when using the res;
			}
		}
		return res;
	}


	public static Population createRandomPopulation() {
		final int LOC_NB_ENTITIES_IN_POP = Population.NB_ENTITIES_IN_POP;
		Population pop = new Population();
		LOGGER.config("Creating Population " + pop.number + " : " + LOC_NB_ENTITIES_IN_POP + " sets containing ~[" + Program.CREATION_SIZE[0] + ".."+ Program.CREATION_SIZE[1] + "] models...");
		int nbRules = 0;

		for (int i = 0; i < LOC_NB_ENTITIES_IN_POP; i++) {
			Program p = Program.createRandomProgram();
			nbRules += p.size();
			pop.addEntity(p);
			LOGGER.fine("Program" + i + " : " + p );
		}
		float avgProgramSize = nbRules / (float) pop.size();
		
		LOGGER.config("\n" + pop.getEntities().size() + " entities in Population 0. \n"
				+ "\n   Average Programs' size : " + ToolBox.format2Decimals(avgProgramSize) + " rules");
		return pop;
	}
	
	public Population cutPopulationInHalf() {
		Population G1;
		G1 = new Population();
		Pareto lastPareto = null, pr;
		for (int i = 0; i < this.paretos.size(); i++) {
			pr = this.getPareto(i);
			if(G1.getEntities().size() + pr.size() < Population.NB_ENTITIES_IN_POP){
				G1.addPareto(pr);
			} else {
				lastPareto = pr;
				break;
			}
		}
		//Cutting last pareto to get POP_SIZE/2 new population.
		if(lastPareto != null) {
			Collections.sort(lastPareto.entitiesp, GeneticIndividual.getDescendantRankComparator());
			removeAfterCutpoint(lastPareto.entitiesp, (Population.NB_ENTITIES_IN_POP)-G1.getEntities().size());
			G1.addPareto(lastPareto);
		}
		solutions_vs_examples = G1.buildMatrice_solution_vs_examples();
		return G1;
	}
	
    public void removeAfterCutpoint(ArrayList<?> genes, int cutpoint) {
        int times = genes.size()-cutpoint;
        for(int i=1; i<=times; i++)
        	genes.remove(cutpoint);
    }



	public void evaluate(Evaluator ev) {
		evaluate(ev, false);
	}
	
	public void evaluate(Evaluator ev, boolean force) {
		GeneticIndividual[] ets = (GeneticIndividual[]) entities.toArray(new GeneticIndividual[entities.size()]);
		Map<GeneticIndividual, FitnessVector> fitnesses = new HashMap<>(ets.length);
		for (GeneticIndividual entity : ets) {
			FitnessVector fv = ev.evaluate(entity, force);
			if (fv == null)
				LOGGER.warning(entity.getClass().getSimpleName() + " '" + entity + "' could not be evaluated.");
			fitnesses.put(entity, fv);
		}
		//Build matrice
		solutions_vs_examples = buildMatrice_solution_vs_examples();
		
		
		
		//TODO Calculs and affects social semantic objective values
		//Affect social semantic objective values
		if(Config.TFIDF.isObjective()) {
			Map<GeneticIndividual, Double> map = crowdingDistance_tfidf(this);
			for (Entry<GeneticIndividual, Double> e : map.entrySet()) {
				fitnesses.get(e.getKey()).setTFIDFValue(e.getValue());
			}
		}
	}

	/**
	 * Population must be evaluated ! (i.e., its entities' FitnessVectors must be != null !!!
	 * @return 
	 */
	public double[][] buildMatrice_solution_vs_examples() {
		GeneticIndividual[] ets = (GeneticIndividual[]) entities.toArray(new GeneticIndividual[entities.size()]);
		Model[] ms = (Model[]) ExampleSet.getExamplesBeingUsed().toArray(new Model[ExampleSet.getExamplesBeingUsed().size()]);
		double[][] res = new double[ets.length][ms.length];
		for (int i = 0; i < ets.length; i++) {
			Program prg = (Program) ets[i];
			for (int j = 0; j < ms.length; j++) {
				Oracle.OracleCmp oc = prg.getFitnessVector().getCmps().get(ms[j]);
				if(oc != null) 
					res[i][j] = oc.isRight() ? 1.0 : 0.0;
				else //Attention, this means that if Cmp had not been evaluated yet (Not supposed to happen.)
					 //we consider an arbitrary 0.5 (MEANING NOTHING !!!)
					res[i][j] = 0.5;
			}
		}
		return res;
	}

	double[][] solutions_vs_examples ;
	
	public double[][] getSolutions_vs_examples() {
		return solutions_vs_examples;
	}
	
	/**
	 * Clone the entites - not Paretos !
	 */
	@Override
	public Population clone() {
		Population p = new Population();
		// p.number = number;

		if (paretos != null) {
			Pareto[] prs = new Pareto[paretos.size()];
			paretos.toArray(prs);
			for (Pareto pr : prs)
				p.addPareto(pr.clone());
		} else if (entities != null) {
			GeneticIndividual[] ets = new GeneticIndividual[entities.size()];
			entities.toArray(ets);
			for (GeneticIndividual e : ets)
				p.addEntity(e.clone());
		}
		p.solutions_vs_examples = solutions_vs_examples.clone();

		return p;
	}

	public String printStatistics() {
		return printStatistics(false);
	}

	public String printOneLineStatistics() {
		GeneticIndividual[] ets = (GeneticIndividual[]) entities.toArray(new GeneticIndividual[entities.size()]);
//		Arrays.sort(ets, GeneticEntity.getDescendantRankComparator());
		Arrays.sort(ets, GeneticIndividual.getMonoValueComparator());
		
		float medSize = 0.0f;
		int minSize = Integer.MAX_VALUE;
		int maxSize = -1;
		for (GeneticIndividual entity : ets) {
			medSize += entity.size();
			if(entity.size() < minSize)
				minSize = entity.size();
			if(entity.size() > maxSize)
				maxSize = entity.size();
		}
		medSize /= ets.length;
		
		String res = "Population " + number + " : " + ets.length + " programs with ["+minSize+"|"+medSize+"|"+maxSize+"]rules -";
		
		GeneticIndividual best = getBestOnObj0();
		res += " Best : " + ((best != null) ? best + ":" + best.printNumericResult(",", "", "", false) : " not affected")+"";
		return res;
		
	}
	public String printStatistics(boolean verbose) {
		int i = 0;

		
		GeneticIndividual[] ets = (GeneticIndividual[]) entities.toArray(new GeneticIndividual[entities.size()]);
//		Arrays.sort(ets, GeneticEntity.getDescendantRankComparator());
		Arrays.sort(ets, GeneticIndividual.getMonoValueComparator());
		
		
		float medSize = 0.0f;
		int minSize = Integer.MAX_VALUE;
		int maxSize = -1;
		for (GeneticIndividual entity : ets) {
			medSize += entity.size();
			if(entity.size() < minSize)
				minSize = entity.size();
			if(entity.size() > maxSize)
				maxSize = entity.size();
		}
		medSize /= ets.length;
		
		String res = "Statistics of Population " + number + " : " + ets.length + " programs with ["+minSize+"|"+medSize+"|"+maxSize+"]rules \n";
		
		GeneticIndividual best = getBestOnObj0();
		res += "   Best is " + ((best != null) ? best + ":" + best.printNumericResult(",", "", "", false) : " not affected")+"\n";
		
		
		for (GeneticIndividual entity : ets) {
			if (i++ < NB_ENTITIES_IN_STAT_PRINTING || verbose) {
				res += "   " + entity +":"+ entity.getFitnessVector();// +" "+entity.printRank();
//				res += "   " + entity.size() + "{";
//				Collections.sort(entity.getGenes(), (Gene o1, Gene o2) ->  o1.toString().compareTo(o2.toString()) );
//				
////				Collections.sort(entity.getGenes(), new Comparator<Gene>() {// Sorting model names
////							@Override
////							public int compare(Gene o1, Gene o2) {
////								return o1.toString().compareTo(o2.toString());
////							}
////						});
//				for (Gene g : entity.getGenes())
//					res += g.simplePrint() + ",";
//				if (res.endsWith(","))
//					res = res.substring(0, res.length() - 1);
//				res += "}";
				res += "\n";
			} else if (!verbose && i == NB_ENTITIES_IN_STAT_PRINTING + 1)
				res += "   ...\n";
		}
		return res;
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
		for (GeneticIndividual entity : p.getEntities()) {
			addEntity(entity);
		}
		this.paretos.add(p);
	}

	
	public ArrayList<Pareto> fastNonDominantSort() {
		paretos = new ArrayList<>(); // Re-init paretos
		ArrayList<GeneticIndividual> Sp = new ArrayList<>(NB_ENTITIES_IN_POP);
		Sp.addAll(entities);
		Pareto Fi = null;
		int front = 0;
		do {
			Fi = extractFront(Sp, front);
			Sp.removeAll(Fi.getEntities());
			if (!Fi.isEmpty()) {
				paretos.add(Fi);
				Fi.rank = front;
				for (GeneticIndividual e : Fi.getEntities())
					e.rank = front;
			}
			front++;
		} while (!Sp.isEmpty());
		return paretos;
	}

	public Pareto extractFront(ArrayList<GeneticIndividual> entities, int frontNb) {
		ArrayList<GeneticIndividual> Sp = new ArrayList<>(NB_ENTITIES_IN_POP);
		Pareto Fi = new Pareto();
		for (GeneticIndividual p : entities) {
			int np = 0;
			for (GeneticIndividual q : entities) {
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
		HashMap<GeneticIndividual, ArrayList<GeneticIndividual>> Sp = new HashMap<>();
		// ArrayList<Pareto> Fi = new ArrayList<>();
		Pareto pr = new Pareto();
		paretos.add(pr);
		for (GeneticIndividual p : entities) {
			p.rank = 0;
			Sp.put(p, new ArrayList<GeneticIndividual>());
			for (GeneticIndividual q : entities) {
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
			for (GeneticIndividual p : paretos.get(i).entitiesp) {
				for (GeneticIndividual q : Sp.get(p)) {
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

	public void crowdingDistanceAssignement2() {
		int l = this.size();
		for (GeneticIndividual e : this.getEntities())
			e.distance = 0;
		for (int i = 0; i < FitnessVector.OBJECTIVES_CONSIDERED; i++) {
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
	public void crowdingDistanceAssignement() {
		if(Config.TFIDF.isCrowdingDistance()) {
			crowdingDistanceAssignement_tfidf();
			return;
		}
		
		int l = this.size();
		for (GeneticIndividual e : this.getEntities())
			e.distance = 0;

		for (int i = 0; i < FitnessVector.OBJECTIVES_CONSIDERED; i++) {
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

	
	
	public void crowdingDistanceAssignement_tfidf() {
		Map<GeneticIndividual, Double> distancesMap = crowdingDistance_tfidf(this);
		for (GeneticIndividual geneticIndividual : entities) {
			geneticIndividual.setDistance(distancesMap.get(geneticIndividual));
		}
	}

	public static Map<GeneticIndividual, Double> crowdingDistance_tfidf(Population pop) {
		for (GeneticIndividual e : pop.getEntities())
			e.distance = 0;

		Model[] ms = (Model[]) ExampleSet.getExamplesBeingUsed().toArray(new Model[ExampleSet.getExamplesBeingUsed().size()]);

		double D = pop.entities.size();
		double[] idfi = new double[ms.length]; 
		double[] fq = new double[ms.length]; 
		for (int j = 0; j < fq.length; j++) 
			fq[j] = 0.0;
			
		for (int i = 0; i < pop.solutions_vs_examples.length; i++) {
			for (int j = 0; j < pop.solutions_vs_examples[i].length; j++) 
				fq[j] += pop.solutions_vs_examples[i][j];
		}
		for (int j = 0; j < fq.length; j++) 
			idfi[j] = Math.log10(D/fq[j]);
		
		double[] distances = new double[pop.entities.size()];
		Map<GeneticIndividual, Double> distancesMap = new HashMap<>(pop.entities.size());
		int k=0;
		for (GeneticIndividual ge : pop.entities) {
			double distance = 0.0;
			for (int i = 0; i < ms.length; i++) {
				Model m = ms[i];
				try{
					distance += (ge.getFitnessVector().getCmp(m).isRight() ? 1.0 : 0.0) * idfi[i];
				} catch( NullPointerException ex){
					LOGGER.warning("One model not instantiated properly.... " +ex.getMessage());
					
				}
			}
			distancesMap.put(ge, distance);
			ge.setDistance(distance);
			distances[k++] = distance;
		}
		return distancesMap;
	}

	/**
	 * @param idfi
	 */
	public String printArray(double[] idfi) {
		String s = "[";
		for (int i = 0; i < idfi.length; i++)
			s += String.format("%.01f, ", idfi[i]);
		return s.substring(0, s.length() - 2) + "]\n";
	}

	public class Pareto {
		ArrayList<GeneticIndividual> entitiesp;
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

		public boolean add(GeneticIndividual e) {
			if (entitiesp.contains(e))
				return false;
			return this.entitiesp.add(e);
		}

		public void clear() {
			this.entitiesp = new ArrayList<>();
		}

		public ArrayList<GeneticIndividual> getEntities() {
			return this.entitiesp;
		}

		public GeneticIndividual get(int index) {
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
			GeneticIndividual[] ets = new GeneticIndividual[entitiesp.size()];
			entitiesp.toArray(ets);
			for (GeneticIndividual entity : ets) {
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
		Collections.sort(entities, new Comparator<GeneticIndividual>() {
			@Override
			public int compare(GeneticIndividual o1, GeneticIndividual o2) {
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
	 * USING EUCLIDIAN distance to optimum values[0 et 1]!
	 *
	 * @return
	 */
	public GeneticIndividual getBestOnObj0() {
		return getBestOnObj(0);
	}
	
	public GeneticIndividual getBestOnObj(int objective) {
		GeneticIndividual max = null;
		double maxObj0 = -1;
//		System.out.println("Population.getBestOnObj " + paretos);
		
		if (paretos == null || paretos.isEmpty()) //WHY paretos would be EMPTY or null ?
			throw new IllegalStateException("'paretos' should not be null nor empty...");
		
		for (GeneticIndividual e : getFrontPareto().getEntities()) {
			double valueObj0 = e.getFitnessVector().getValue(objective);
			if(maxObj0 < valueObj0 ){
				maxObj0 = valueObj0;
				max = e;
			}
		}
//		System.out.println(" -----> "+max);
//		double dist = Double.MAX_VALUE;
//		for (GeneticEntity e : getFrontPareto().getEntities()) {
//			double distTmp = Math.sqrt(Math.pow(1 - e.getFitnessVector().getValue(0), 2) + Math.pow(1 - e.getFitnessVector().getValue(1), 2));
////			double distTmp = e.getFitnessVector().getMonoValue();
//			if (distTmp < dist) {
//				dist = distTmp;
//				max = e;
//			}
//		}
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
		for (GeneticIndividual geneticEntity : entities) {
			if(geneticEntity.getFitnessVector()!=null)
				res += "\n  + "+geneticEntity.getFitnessVector().prettyPrint();
			else res += "\n  + "+geneticEntity+" (Not evaluated yet)";
		}
		
		return res;
	}

	public void writeSolution(File solutionDir) {
		writeSolution(solutionDir, false);
	}
	
	public void writeSolution(File solutionDir, boolean firstOnly) {
		GeneticIndividual[] ets = (GeneticIndividual[]) entities.toArray(new GeneticIndividual[entities.size()]);
		if (getFrontPareto() != null)
			ets = (GeneticIndividual[]) getFrontPareto().getEntities().toArray(new GeneticIndividual[getFrontPareto().getEntities().size()]);

		Arrays.sort(ets, GeneticIndividual.getValueComparator(0));
		GeneticIndividual best = getBestOnObj0();

		if(firstOnly){
			int rate = (int) (best.getFitnessVector().getValue(0) * 100);
			File f = new File(solutionDir.getAbsolutePath() + File.separator + "bestsolution_"+rate+".ocl");
			BufferedWriter bw;
			try {
				bw = new BufferedWriter(new FileWriter(f));
				bw.write(((Program) best).getOCL());
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		
		int i = 0;
		for (GeneticIndividual ge : ets) {
			int rate = (int) (ge.getFitnessVector().getValue(0) * 100);
			File f = new File(solutionDir.getAbsolutePath() + File.separator + "solution" + i++ + "_" + rate + (ge == best ? "-best" : "") + ".ocl");
			BufferedWriter bw;
			try {
				bw = new BufferedWriter(new FileWriter(f));
				bw.write(((Program) ge).getOCL());
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
