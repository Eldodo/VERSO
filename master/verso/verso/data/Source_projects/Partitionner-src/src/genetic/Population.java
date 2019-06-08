package genetic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import models.Model;
import models.ModelSet;

import org.eclipse.emf.ecore.resource.ResourceSet;

import utils.Config;
import utils.Utils;

public class Population {
	public final static Logger LOGGER = Logger.getLogger(Population.class.getName());

	public static int NB_ENTITIES_IN_STAT_PRINTING = 0;// *P
	/** How many program in the populations */
	public static int NB_ENTITIES_IN_POP = 0;// *P
	public static int NB_GENES_IN_ENTITIES = 0;// *P

	public static double MUTATE_RATE = 0;
	public static double CROSSING_RATE = 0;

	public static int nbPop = 0;
	private static final int EMPHASIS_RATE = 10;

	ArrayList<Entity> entities;
	int number;
	ArrayList<Pareto> paretos;

	public static void loadConfig() {
		// Second load call
		NB_ENTITIES_IN_POP = Config.getIntParam("NB_ENTITIES_IN_POP");
		NB_ENTITIES_IN_STAT_PRINTING = Config.getIntParam("NB_ENTITIES_IN_STAT_PRINTING");
		NB_GENES_IN_ENTITIES = Config.getIntParam("NB_GENES_IN_ENTITIES");

		MUTATE_RATE = Config.getDoubleParam("MUTATE_RATE");
		CROSSING_RATE = Config.getDoubleParam("CROSSING_RATE");
	}

	public Population() {
		entities = new ArrayList<>();
		paretos = new ArrayList<>();
		number = nbPop++;
	}

	public ArrayList<Entity> getEntities() {
		return entities;
	}

	public ArrayList<Entity> orderEntitiesWithMonoValue() {
		Collections.sort(entities, Entity.getMonoValueComparator());
		return entities;
	}

	public boolean addEntity(Entity e) {
		if (entities.contains(e) || e == null)
			return false;
		return entities.add(e);
	}

	public void removeLastEntity() {
		if (!entities.isEmpty()) {
			Entity removed = entities.remove(entities.size() - 1);
		}

	}

	public ArrayList<Entity> getEntities_proba_rank() {
		// System.out.println("Population.getEntities_proba_rank()");
		ArrayList<Entity> res = new ArrayList<>();
		int maxRank = paretos.size();
		for (Entity entity : entities) {
			res.add(entity);
			for (int i = 0; i < (maxRank - entity.rank) * EMPHASIS_RATE; i++)
				res.add(entity);
		}
		return res;
	}

	public ArrayList<Entity> getEntities_proba_rank_geometric() {// TODO GEO
																	// TRUC
		if (paretos.size() == 0)
			return getEntities_proba_rank();
		// System.out.println("Population.getEntities_proba_rank_geometric()");
		int maxRank = paretos.size();
		ArrayList<Entity> res = new ArrayList<>();
		Collections.sort(paretos, getParetoRankComparator());
		int nbEnt = 0;

		for (Pareto p : paretos) {
			nbEnt = (int) Math.pow(2, maxRank - p.getRank());
			// System.out.println("  "+p+" : "+(maxRank-p.getRank())+" -> "+nbEnt);
			for (int j = 0; j < nbEnt; j++)
				res.addAll(p.getEntities());
		}
		return res;
	}

	public ArrayList<Entity> getEntities_proba_mono() {
		ArrayList<Entity> res = new ArrayList<>();
		for (Entity entity : entities) {
			for (int i = 0; i < (int) (entity.getMonoValue() * EMPHASIS_RATE); i++) {
				res.add(entity);
			}
		}
		return res;
	}

	public static Population createRandomPopulation(ResourceSet rs) {
		final int LOC_NB_ENTITIES_IN_POP = Population.NB_ENTITIES_IN_POP / 2;
		final int LOC_NB_GENES_IN_ENTITIES = Population.NB_GENES_IN_ENTITIES;
		Population pop = new Population();
		LOGGER.config("Creating Population " + pop.number + " : " + LOC_NB_ENTITIES_IN_POP + " sets containing ~" + LOC_NB_GENES_IN_ENTITIES + " models...");
		int countModels = 0;
		int countClasses = 0;
		int countProperties = 0;
		ModelSet ms;

		for (int i = 0; i < LOC_NB_ENTITIES_IN_POP; i++) {
			ms = new ModelSet();
			int classes = 0;
			int properties = 0;
			int models = 0;
			Model m;
			for (int j = 0; j < Utils.getRandomInt(Math.max(1, LOC_NB_GENES_IN_ENTITIES / 2), LOC_NB_GENES_IN_ENTITIES); j++) {
				m = ModelSet.loadNewRandomModel(rs);

				while (!ms.addModel(m))
					// Only happen when the model set already contains the newly
					// loaded model
					m = ModelSet.loadNewRandomModel(rs);

				classes += m.getClasses().size();
				properties += m.getClassProperties().size();
				models++;
			}
			countProperties += properties;
			countClasses += classes;
			countModels += models;

			// FitnessVector fv = new FitnessVector(...); //Moved to evaluate()
			// method <= need Evaluator
			// ms.setFitnessVector(fv);
			pop.addEntity(ms);
			LOGGER.fine("ModelSet" + i + " : " + models + " models, " + classes + " classes, " + properties + " properties");

		}

		float avgModelSetSize = (float) countModels / pop.getEntities().size();
		float avgClasses = (float) countClasses / countModels;
		float avgProperties = (float) countProperties / countModels;
		LOGGER.config("\n" + pop.getEntities().size() + " entities in Population 0. \n" + countModels + " models, " + countClasses + " classes, " + countProperties + " properties"
				+ "\n   Average ModelSets size : " + ((int) avgModelSetSize) + " models" + "\n   Average Models size :    " + ((int) avgClasses) + " classes per model"
				+ "\n                            " + ((int) avgProperties) + " properties per model");
		return pop;
	}

	public void evaluate(Evaluator ev) {
		Entity[] ets = (Entity[]) entities.toArray(new Entity[entities.size()]);
		for (Entity entity : ets) {
			ev.evaluateCoverage(entity);
		}
	}

	@Override
	public Population clone() {
		Population p = new Population();
		p.number = number;

		if (entities != null) {
			Entity[] ets = new Entity[entities.size()];
			entities.toArray(ets);
			for (Entity e : ets)
				p.addEntity(e.clone());
		}

		if (paretos != null) {
			Pareto[] prs = new Pareto[paretos.size()];
			paretos.toArray(prs);
			for (Pareto pr : prs)
				p.addPareto(pr.clone());
		}

		return p;
	}

	public String printStatistics() {
		return printStatistics(false);
	}

	public String printStatistics(boolean verbose) {
		int i = 0;
		// fastNonDominantSort();
		// ArrayList<Entity> entities = getFrontPareto().getEntities();
		// Collections.sort(entities, Entity.getDescendantRankComparator());
		String res = "Statistics of Population " + number + " : " + entities.size() + " sets\n";
		Entity best = getBest();
		res += "   Best:" + ((best != null) ? getBest().printResult(",", "", "", false) : " not affected");
		Entity[] ets = (Entity[]) entities.toArray(new Entity[entities.size()]);
		Arrays.sort(ets, Entity.getDescendantRankComparator());
		for (Entity entity : ets) {
			if (i++ < NB_ENTITIES_IN_STAT_PRINTING || verbose) {
				res += "   " + entity + ":" + entity.printStats();
				// if(verbose){
				res += "\t" + "   " + entity.size() + "{";
				Collections.sort(entity.getGenes(), new Comparator<Gene>() {// Sorting
																			// model
																			// names
							@Override
							public int compare(Gene o1, Gene o2) {
								return o1.toString().compareTo(o2.toString());
							}
						});
				for (Gene g : entity.getGenes())
					res += g + ",";
				if (res.endsWith(","))
					res = res.substring(0, res.length() - 1);
				res += "}";
				// }
				res += "\n";
			} else if (!verbose && i == NB_ENTITIES_IN_STAT_PRINTING + 1)
				res += "   ...\n";
		}
		return res;
	}

	public void storeFrontParetoInFileSystem(File f, String timeStamp, boolean printJess) {
		Entity[] ets = (Entity[]) entities.toArray(new Entity[entities.size()]);
		if (getFrontPareto() != null)
			ets = (Entity[]) getFrontPareto().getEntities().toArray(new Entity[getFrontPareto().getEntities().size()]);
		Arrays.sort(ets, Entity.getDescendantRankComparator());
		Pareto p = getFrontPareto();
		File root = new File(f.getAbsolutePath() + File.separator + "res" + timeStamp);
		File jessFile = null;
		BufferedWriter bwJess = null;
		root.mkdir();
		Level lvl = LOGGER.getLevel();
		// LOGGER.setLevel(Level.FINER);
		LOGGER.finer("  Root : " + root.getAbsolutePath());
		int i = 0;
		int nbModels = 0;
		int nbModelsStored = 0;
		boolean alright = true;
		for (Entity e : p.getEntities()) {
			File folder = new File(root.getAbsolutePath() + File.separator + "solution" + i);
			folder.mkdir();

			if (printJess) {
				jessFile = new File(folder.getAbsolutePath() + File.separator + "solution" + i + ".cpl");
				try {
					bwJess = new BufferedWriter(new FileWriter(jessFile));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			
			LOGGER.finer("Folder : " + folder.getAbsolutePath());
			int numModel = 0;
			for (Gene g : e.getGenes()) {
				nbModels++;
				if (g instanceof Model) {
					LOGGER.finest("" + ((Model) g).getClassProperties());
					LOGGER.finer("\n" + ((Model) g).jessification());
				}
				if (printJess) {
					try {
						// bwJess.append(((Model) //
						// g).getClassProperties().toString());
						bwJess.append(((Model) g).jessification(numModel++ == 0));
					} catch (IOException e1) {
						LOGGER.severe("solution" + i + " : Unable to write Jess for model " + g.getResourceFileName());
					}
				}
				FileOutputStream target;
				try {
					target = new FileOutputStream(new File(folder.getAbsolutePath() + File.separator + g.getResourceFileName()));
					LOGGER.finer("Target : " + new File(folder.getAbsolutePath() + File.separator + g.getResourceFileName()).getAbsolutePath());
					Files.copy(Utils.resolveInstancePath(g.getResourceFileName()), target);
					nbModelsStored++;
				} catch (IOException e1) {
					LOGGER.severe("!!!");
					e1.printStackTrace();
					alright = false;
				}
			}
			if (printJess) {
				try {
					bwJess.close();
				} catch (IOException e1) {
					LOGGER.warning("Jess file '" + jessFile.getAbsolutePath() + "'not closed properly.");
				}
			}
			i++;
		}
		LOGGER.info("Result stored : " + (i) + " solutions (" + nbModelsStored + ((nbModelsStored != nbModels) ? "/" + nbModels : "") + " models in total)");
		if (!alright)
			LOGGER.warning((nbModels - nbModelsStored) + " models missing.");
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
		for (Entity entity : p.getEntities()) {
			addEntity(entity);
		}
		this.paretos.add(p);
	}

	public ArrayList<Pareto> fastNonDominantSort() {
		paretos = new ArrayList<>(); // Re-init paretos
		ArrayList<Entity> Sp = new ArrayList<>();
		Sp.addAll(entities);
		Pareto Fi = null;
		int front = 0;
		do {
			Fi = extractFront(Sp, front);
			Sp.removeAll(Fi.getEntities());
			if (!Fi.isEmpty()) {
				paretos.add(Fi);
				Fi.rank = front;
				for (Entity e : Fi.getEntities())
					e.rank = front;
			}
			front++;
		} while (!Sp.isEmpty());
		return paretos;
	}

	public Pareto extractFront(ArrayList<Entity> entities, int frontNb) {
		ArrayList<Entity> Sp = new ArrayList<>();
		Pareto Fi = new Pareto();
		for (Entity p : entities) {
			int np = 0;
			for (Entity q : entities) {
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
		HashMap<Entity, ArrayList<Entity>> Sp = new HashMap<>();
		// ArrayList<Pareto> Fi = new ArrayList<>();
		Pareto pr = new Pareto();
		paretos.add(pr);
		for (Entity p : entities) {
			p.rank = 0;
			Sp.put(p, new ArrayList<Entity>());
			for (Entity q : entities) {
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
			for (Entity p : paretos.get(i).entitiesp) {
				for (Entity q : Sp.get(p)) {
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

		return paretos;
	}

	public void crowdingDistanceAssignement() {
		int l = this.size();
		for (Entity e : this.getEntities())
			e.distance = 0;

		for (int i = 0; i < Config.NUMBER_OF_OBJECTIVES; i++) {
			this.sort(i);// CERTIFIED
			this.getEntities().get(0).distance = Integer.MAX_VALUE;

			if (l > 1) {
				this.getEntities().get(this.size() - 1).distance = Integer.MAX_VALUE;
				for (int j = 1; j < l - 2; j++) {
					double v = this.getEntities().get(j).getFitnessVector().getValue(i);
					v += this.getEntities().get(j + 1).getFitnessVector().getValue(i) - this.getEntities().get(j - 1).getFitnessVector().getValue(i);
					;
					this.getEntities().get(j).distance = v;
				}
			}
		}
	}

	public class Pareto {
		ArrayList<Entity> entitiesp;
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

		public boolean add(Entity e) {
			if (entitiesp.contains(e))
				return false;
			return this.entitiesp.add(e);
		}

		public void clear() {
			this.entitiesp = new ArrayList<>();
		}

		public ArrayList<Entity> getEntities() {
			return this.entitiesp;
		}

		public Entity get(int index) {
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
			Entity[] ets = new Entity[entitiesp.size()];
			entitiesp.toArray(ets);
			for (Entity entity : ets) {
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
		Collections.sort(entities, new Comparator<Entity>() {
			@Override
			public int compare(Entity o1, Entity o2) {
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

	public Entity getBest() {
		Entity max = null;
		if (Evolutioner.MONO_OBJECTIVE_ON) {
			for (Entity e : entities) {
				if (max == null || e.getMonoValue() > max.getMonoValue())
					max = e;
			}
		} else {
			double dist = Double.MAX_VALUE;
			if (paretos == null || paretos.isEmpty())
				return null;
			for (Entity e : getFrontPareto().getEntities()) {
				double distTmp = Math.sqrt(Math.pow(1 - e.getFitnessVector().getCoverage(), 2) + Math.pow(1 - e.getFitnessVector().getDissimilarity(), 2));
				if (distTmp < dist) {
					dist = distTmp;
					max = e;
				}
			}
		}
		return max;
	}

}
