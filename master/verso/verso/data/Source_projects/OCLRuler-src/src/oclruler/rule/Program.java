package oclruler.rule;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import oclruler.genetics.EvaluatorOCL;
import oclruler.genetics.FitnessVector;
import oclruler.genetics.Gene;
import oclruler.genetics.GeneticIndividual;
import oclruler.genetics.Oracle;
import oclruler.genetics.UnstableStateException;
import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.MMElement;
import oclruler.metamodel.Model;
import oclruler.metrics.ProgramLoader;
import oclruler.rule.patterns.A0_RawText;
import oclruler.rule.struct.Constraint;
import oclruler.rule.struct.Node;
import oclruler.rule.struct.Node_DEFAULT;
import oclruler.utils.Config;
import oclruler.utils.ToolBox;

/**
 * 
 * @author  Edouard Batot - batotedo@iro.umontreal.ca
 *
 */
public class Program extends GeneticIndividual {
	public static Logger LOGGER = Logger.getLogger(Program.class.getName());

	/*
	 * Configured in Config.losdConfig()
	 */
	public static int[] CREATION_SIZE;
	
	/**
	 * 3 integers to ponderate possible options in list order mutation
	 * <ul>
	 *   <li>[0] ADD rate</li>
	 *   <li>[1] REMOVE rate</li>
	 *   <li>[2] EXCHANGE rate</li>
	 * </ul>
	 */
	public static int[] LIST_ORDER_MUTATION_OPTIONS_RATES;
	
	/** Proportion of mutations applied on a random contraint (opposite is a list order mutation which probability <i>p</i> = 1 - MUTATE_INSIDE_CONSTRAINT_RATE) */
	public static double LIST_ORDER_MUTATION_RATE = 0.4;;

	/**
	 * Minimum size to consider a "list mutation". If program size is inferiror, mutation will be "inside constraint".
	 */
	public static int MUTATION_MIN_SIZE = 3;
	
	/**
	 * Instances counter
	 */
	protected static int number_of_programs = 0;

	public static void loadConfig(){
		//programs' size
		CREATION_SIZE = new int[2];
		String s = Config.getStringParam("CREATION_SIZE");
		try {
			CREATION_SIZE = new int[]{Integer.decode(s.split(" ")[0]), Integer.decode(s.split(" ")[1])};
		} catch (Exception e) {
			LOGGER.severe("'CREATION_SIZE' of programs undefined : '"+s+"'\n Expected syntax is '<int> <int>' (Upper and lower bounds are required)" );
			System.exit(1);
		}

		
		LIST_ORDER_MUTATION_RATE 		= Config.getDoubleParam("LIST_ORDER_MUTATION_RATE");
		s = Config.getStringParam("LIST_ORDER_MUTATION_OPTIONS_RATES");
		try {
			LIST_ORDER_MUTATION_OPTIONS_RATES = new int[]{Integer.decode(s.split(" ")[0]), Integer.decode(s.split(" ")[1]), Integer.decode(s.split(" ")[2])};
		} catch (Exception e) {
			LOGGER.severe("'MUTATE_LIST_CHOICES_RATES' undefined : '"+s+"'\n Expected syntax is '<int> <int> <int>' (respectively ADD, REMOVE, EXCHANGE choices' rates)" );
			System.exit(1);
		}
	}
	
	protected ArrayList<Constraint> constraints;
	protected String  name;
	
	public Program() {
		setName(getClass().getSimpleName() + "_" + number_of_programs++);
		constraints = new ArrayList<Constraint>(FitnessVector.EXPECTED_NUMBER_OF_RULES[0]);
		modified = true;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Load A0 constrint (untyped) from the file given as parameter. 
	 * If the file is a directory, it will be visited reursively and load every .ocl file found.
	 * @param file
	 * @return A Program with constraints loaded from the file given as parameter
	 */
	public static Program loadProgramFromFile(File file) {
		Program res = new Program();
		ArrayList<Constraint> ns = new ArrayList<>();
		for (A0_RawText a0 : ProgramLoader.loadConstraintsFromFile(file)) {
			Constraint c = new Constraint(new Node_DEFAULT(null, a0));
			ns.add(c);
		}
		res.addAllConstraints(ns);
		return res;
	}

	public ArrayList<Constraint> getConstraints() {
		return constraints;
	}

	public boolean addConstraint(Constraint p) {
		modified = true;
		if (p == null || constraints.contains(p))
			return false;
		return constraints.add(p);
	}

	public Constraint getConstraint(int idx) {
		return constraints.get(idx);
	}

	public boolean addAllConstraints(ArrayList<? extends Constraint> ps) {
		modified = true;
		boolean res = true;
		for (Constraint pattern : ps) {
			res &= addConstraint(pattern);
		}
		return res;
	}

	public int size() {
		return constraints.size();
	}

	@Override
	public int getNumberOfLeaves() {
		int res = 0;
		for (Constraint c : constraints) {
			res += c.getNumberOfLeaves();
		}
		return res;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public ArrayList<? extends Gene> getGenes() {
		return getConstraints();
	}
	
	/**
	 * Gene g is casted into a <code>Constrint</code>.
	 */
	@Override
	public boolean addGene(Gene g) {
		return addConstraint((Constraint)g);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && getClass() == obj.getClass()) {
			Program p = (Program) obj;
			if (p.getName().equals(getName()))
				return true;
			else {
				if (p.size() == size()) {
					p.constraints.sort(null);
					constraints.sort(null);
					for (int i = 0; i < size(); i++) {
						if (!constraints.get(i).equals(p.constraints.get(i)))
							return false;
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public GeneticIndividual clone() {
		Program p = new Program();
		for (Constraint ct : constraints) {
			try {
				Constraint cClone = (Constraint) ct.clone();
				p.addConstraint(cClone);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			if (fitnessVector != null) {
				p.fitnessVector = (FitnessVector) fitnessVector.clone();
				p.modified = modified;
			}
		} catch (CloneNotSupportedException e) {
			LOGGER.config("Cloning Exception : " + e.getMessage());
		}
		return p;
	}

	public void prune() {
		for (Constraint c : constraints) {
			try {
				c.prune();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public int numberOfQUalificationMistake() {
		int res = 0;
		if (getFitnessVector() == null)
			return -1;
		for (Oracle.OracleCmp oc : getFitnessVector().getCmps().values()) {
			if (oc == Oracle.OracleCmp.NP || oc == Oracle.OracleCmp.PN)
				res++;
		}
		return res;
	}

	/* ****************************
	 * 
	 *  OCL FEATURES
	 * 
	 * 
	 **************************** */
	
	public String diagnose(Model m) {
		String res = "Diagnostic for '" + m.getFileName() + "' : ";
		HashMap<Constraint, ArrayList<String>> diagnostic = EvaluatorOCL.diagnose(m, this);
		if (diagnostic.isEmpty())
			res += "OK";
		else {
			res += "\n";
			for (Constraint p : diagnostic.keySet()) {
				res += " + " + p.getOCL_inv() + "\n";
				for (String oName : diagnostic.get(p))
					res += "   - " + oName + "\n";
			}
		}
		return res.trim();
	}

	/**
	 * Exports a program in OCL. <br/>
	 * <em>To be implemented.</em><br/>
	 * Take patterns' list, add "inv:" and context and wrap it into a String.
	 * 
	 * @return
	 */
	public String getOCL() {

		Constraint[] ps = new Constraint[constraints.size()];
		constraints.toArray(ps);

		String res = "-- " + name + "\n-- " + ps.length + " constraints\n\n";
		res += "import '" + Config.getMetamodelFile().toURI().toString() + "'\n\n";

		if (ps.length <= 0)
			return "-- " + name + " is empty";

		// package
		String pName = null;
		int i = 0;
		do {
			if (ps[i].getContext() != null)
				pName = ps[i].getContext().getPackageName();
			i++;
		} while (pName == null);
		res += "package " + ps[0].getContext().getPackageName() + "\n\n";

		// invs
		HashMap<String, ArrayList<Constraint>> orderedPatterns = new HashMap<>();
		for (Constraint p : ps) {
			String ctxt = p.getContext().getName();
			if (orderedPatterns.get(ctxt) == null)
				orderedPatterns.put(ctxt, new ArrayList<>());
			orderedPatterns.get(ctxt).add(p);
		}

		for (String s : orderedPatterns.keySet()) {
			res += "context " + s + "\n";
			for (Constraint c : orderedPatterns.get(s)) {
				res += c.getOCL_inv() + "\n";
			}
			res += "\n";
		}

		return res + "\nendpackage";
	}

	
	
	/* ****************************
	 * 
	 *  Genetics FEATURES
	 * 
	 * 
	 **************************** */
	public static Program createRandomProgram(int nbConstraints){
		Program res = new Program();
		for (int i = 0; i < nbConstraints; i++) 
			res.addConstraint(Constraint.createRandomConstraint());
		return res;
	}

	public static Program createRandomProgram(){
		return createRandomProgram(ToolBox.getRandomInt(CREATION_SIZE[0], CREATION_SIZE[1]));
	}
	
	
	public GeneticIndividual[] crossoverDeep(GeneticIndividual ge2) {
		Program p2 = (Program)ge2;
		int cutPoint1 = (size() <= 2)	?1:ToolBox.getRandomInt(1, size() - 1);
		int cutPoint2 = (p2.size() <= 2)?1:ToolBox.getRandomInt(1, p2.size() - 1);
		
//		System.out.println("sizes : list1= "+size()+", list2= "+p2.size());
//		System.out.println("cutPoints : "+cutPoint1 + ", "+cutPoint2);
		
		ArrayList<Constraint> list1 = new ArrayList<>();
		ArrayList<Constraint> list2 = new ArrayList<>();
		
		int i = 0;
		for (i = 0; i < cutPoint1; i++) 
			list1.add(constraints.get(i));				// Put first constraints from Program this into list1
		Constraint cst1 = getConstraint(cutPoint1);		// Pick the constraint on cutPoint (cst1)
		if(cst1.depth() > 1) {
//			System.out.println("1. (Program2.crossoverDeep) : ");
			Node n = cst1.pickRandomNode(2,1);			// Pick a Node to cut in cst1
//			System.out.println(n.getId());
			n = splitConstraint(cst1, n);				// Split cst1 (take the node n out of it and return a clone of n)
			list2.add(cst1);
			if(n != null ) 
				list2.add(new Constraint(n));				// Add the cut node to Progam this embedded in a Cst
		} else 
			list2.add(cst1);							// If cst1.depth == 1 No cut possible, just add cst1 to list2		
		for (i = cutPoint1+1 ; i < constraints.size() ; i++) 
			list2.add(constraints.get(i));				// Put the rest of constraints from Program this into list2
		
		
		int j = 0;
		for (j = 0; j < cutPoint2; j++) 
			list2.add(p2.constraints.get(j));
		Constraint cst2 = p2.getConstraint(cutPoint2);
		if(cst2.depth() > 1) {
//			System.out.println("2. (Program2.crossoverDeep) : ");
			Node n = cst2.pickRandomNode(2, 1);
//			System.out.println(n.getId());
			n = splitConstraint(cst2, n);
			list1.add(cst2);
			if(n != null ) 
				list1.add(new Constraint(n));
		} else 
			list1.add(cst2);
		for (j = cutPoint2+1 ; j < p2.constraints.size() ; j++) 
			list1.add(p2.constraints.get(j));
				
		Program res1 = new Program();
		res1.addAllConstraints(list1);
		Program res2 = new Program();
		res2.addAllConstraints(list2);

		return new Program[] {res1, res2};
	}
	
	/**
	 * 
	 * @param cst1 To be cut of its branch n
	 * @param n Branch to cut from cst1
	 * @return A clone of n without parent (the branch cut)
	 */
	public Node splitConstraint(Constraint cst1, Node n) {
		if(n!= null && n != cst1.getRoot()){
			Node n1 = n.cloneNoParent();
			cst1.cutNode(n);
			return n1;
		}
		return null;
	}
	
	@Override
	public GeneticIndividual[] crossover(GeneticIndividual ge2) {
		Program p2 = (Program)ge2;
		int cutPoint1 = (size() <= 2)?1:ToolBox.getRandomInt(1, size()-1);
		int cutPoint2 = (p2.size() <= 2)?1:ToolBox.getRandomInt(1, p2.size()-1);
		
		Program res1 = new Program();
		Program res2 = new Program();
		int i1 = 0, i2 = 0;
		for (Constraint p : constraints) {
			if(i1 < cutPoint1) res1.addConstraint(p);
			else res2.addConstraint(p);
			i1++;
		}
		for (Constraint p : p2.getConstraints()) {
			if(i2 < cutPoint2) res1.addConstraint(p);
			else res2.addConstraint(p);
			i2++;
		} 
		res1.prune();
		res2.prune();
		return new Program[] {res1, res2};
	}

	public boolean mutate() throws UnstableStateException {
		int k = ToolBox.getRandomDouble() < LIST_ORDER_MUTATION_RATE ? 0 : 1;
		if(constraints.isEmpty()) k = 0;
		switch (k) {
		case 0:
			mutateList();
			break;
		case 1:
			mutateInsideConstraint();
			break;
		}
		modified = true;
		return true;
	}
	
	
	public enum MutateListChoices {
		add(LIST_ORDER_MUTATION_OPTIONS_RATES[0]),
		remove(LIST_ORDER_MUTATION_OPTIONS_RATES[1]),
		exchange(LIST_ORDER_MUTATION_OPTIONS_RATES[2]);
		int pond = 0;
		MutateListChoices(int pond){
			this.pond = pond;
			initialyseListProba(this, pond);
		}
		public int getPond() {
			return pond;
		}
		void initialyseListProba(MutateListChoices mlc,int pond) {
			for (int i = 0; i < pond; i++) {
				listMutationChoice.add(mlc);
				if(mlc != MutateListChoices.remove)
					listMutationChoiceShortPrg.add(mlc);
			}
		}
		
		static MutateListChoices getRandom() {
			return ToolBox.getRandom(listMutationChoice);
		}
		static MutateListChoices getRandom(int size) {
			if(size >= MUTATION_MIN_SIZE)
				return ToolBox.getRandom(listMutationChoice);
			else
				return ToolBox.getRandom(listMutationChoiceShortPrg);
		}
		
	}
	public static ArrayList<MutateListChoices> listMutationChoice = new ArrayList<MutateListChoices>();
	public static ArrayList<MutateListChoices> listMutationChoiceShortPrg = new ArrayList<MutateListChoices>();

	private void mutateList(){
		int rnd = 0;
		MutateListChoices key = MutateListChoices.getRandom(size());
		switch (key) {
		case exchange://Remove and add
			rnd = ToolBox.getRandomIdx(constraints);
			constraints.remove(rnd); 
		case add://Add
			Constraint p = Constraint.createRandomConstraint();
			int maxTries = 0;
			while(constraints.contains(p) && maxTries++<10)
				p = Constraint.createRandomConstraint();
			constraints.add(p);
			break;
		case remove://Only remove
			rnd = ToolBox.getRandomIdx(constraints);
			constraints.remove(rnd);
			break;
		}
	}
	
	/**
	 * Pick a random constraint and call a {@link Constraint#mutate() mutation} on it.
	 * @return <code>true</code> if the constraint has changed.
	 * @throws UnstableStateException
	 */
	private boolean mutateInsideConstraint() throws UnstableStateException{
		Constraint p = ToolBox.getRandom(constraints);
		return p.mutate();
	}
	

	
	
	/* ****************************
	 * 
	 *  Printing FEATURES
	 * 
	 * 
	 **************************** */
	public String printXML() {
		String res = "<PRG name=\"" + name + "\">";
		Constraint[] cs = (Constraint[]) constraints.toArray(new Constraint[constraints.size()]);
		for (Constraint p : cs)
			res += "\n" + p.printXML(ToolBox.TAB_CHAR);
		return res + "\n</PRG>";
	}

	public String printOCL() {
		String res = "--" + name + "";
		Constraint[] cs = (Constraint[]) constraints.toArray(new Constraint[constraints.size()]);
		for (Constraint p : cs)
			res += "\n--" + p.getName() + "\n" + p.getRawOCLConstraint();
		return res + "\n";
	}

	public String printQualification(Model m) {
		if (getFitnessVector() != null && getFitnessVector().getCmp(m) != null)
			return getFitnessVector().getCmp(m).toString();
		return "[o|o]";
	}

	@Override
	public String prettyPrint(String tab) {
		String res = tab + name + ": {";
		Constraint[] cs = (Constraint[]) constraints.toArray(new Constraint[constraints.size()]);
		for (Constraint p : cs)
			res += "\n" + tab + "--" + p.getId() + "\n" + p.prettyPrint(tab + ToolBox.TAB_CHAR);
		return res + "\n}";
	}

	@Override
	public String simplePrint(String tab) {
		String res = tab + name + ": {";
		Constraint[] cs = (Constraint[]) constraints.toArray(new Constraint[constraints.size()]);
		for (Constraint p : cs)
			res += "\n" + tab + "  " + p.simplePrint();
		return res + tab + "\n}";
	}

	@Override
	public String toString() {
		return getName();
	}	


	@Override
	public String printResultPane(String tab) {
		String res = "";
		HashMap<Model, Oracle.OracleCmp> cmps = this.getFitnessVector().getCmps();
		int i = 0;
		res += "Raw: " + getFitnessVector().printExpandedStat() + "\n";

		for (Model m : ExampleSet.getExamplesBeingUsed()) {
			res += tab + i + ". " + cmps.get(m) + "\t" + m.getFileName() + "\n";
		}

		res += "Patterns:\n";
		for (Constraint pattern : constraints) {
			res += pattern.printResultPane(tab) + "\n";
		}
		return res;
	}
	
	public String printResultPaneXML(String tab) {
		String res = tab + "<program>\n";
		HashMap<Model, Oracle.OracleCmp> cmps = this.getFitnessVector().getCmps();
		res += tab + tab + "<results rawDiscrimination=\"" + getFitnessVector().printExpandedStat() + "\">\n";

		for (Model m : ExampleSet.getExamplesBeingUsed()) {
			res += tab + tab + tab + "<example file=\"" + m.getFileName() + "\">" + cmps.get(m) + "</example>\n";
		}
		res += tab + "</results>\n";
		res += tab + "<patterns>\n";
		for (Constraint pattern : constraints) {
			res += "" + pattern.printResultPane(tab + tab) + "\n";
		}
		res += tab + "<patterns>\n";
		res += "<program>\n";
		return res;
	}
	
	/*
	 * 
	 *     TEST SECTION ***
	 * 
	 */
	
	public static boolean testMutation() {
		Program p1 = createRandomProgram();
		for (int i = 0; i < 200; i++) {
			try {
				p1.mutate();
			} catch (UnstableStateException e) {
				System.out.println("Mutation " + i + " failed.");
				e.printStackTrace();
			}
		}
		return true;
	}

	public static boolean testCrossover() {
		Program p1 = createRandomProgram();
		Program p2 = createRandomProgram();
		GeneticIndividual[] cross = p1.crossover(p2);
		if (p1.size() + p2.size() != cross[0].size() + cross[1].size()) {
			System.err.println("Crossover misleading !");
			return false;
		}
		System.out.println("Program.testCrossover()");
		System.out.println("p1 : " + p1.prettyPrint());
		System.out.println("p2 : " + p2.prettyPrint());
		System.out.println("   X cross X");
		System.out.println("res1 : " + cross[0].prettyPrint());
		System.out.println("res2 : " + cross[1].prettyPrint());
		return true;
	}
	
	public Set<MMElement> getMMElements() {
		HashSet<MMElement> res = new HashSet<>();
		
		for (Constraint constraint : getConstraints()) {
//			System.out.println("\n");
//			System.out.println("  constraint:"+constraint);
			Collection<MMElement> toadd = constraint.getMMElements();
			if(toadd != null)
				res.addAll(toadd);
		}
		return res;
	}

}
