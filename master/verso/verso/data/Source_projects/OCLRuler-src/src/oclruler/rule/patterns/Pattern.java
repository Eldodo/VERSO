package oclruler.rule.patterns;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import oclruler.genetics.Gene;
import oclruler.metamodel.Concept;
import oclruler.metamodel.MMElement;
import oclruler.metamodel.Model;
import oclruler.metamodel.NamedEntity;
import oclruler.rule.MMMatch;
import oclruler.rule.PatternFactory;
import oclruler.rule.PatternFactory.PatternType;
import oclruler.rule.struct.Node_FO;
import oclruler.utils.ToolBox;

/**
 * 
 * This is an "atomic" constraint when instantiated !
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public abstract class Pattern extends NamedEntity implements Gene {
	public static Logger LOGGER = Logger.getLogger(Pattern.class.getName());

	public static boolean FIRE_COUNT = true;
	public static int randomXmax_A11b = 3;

	public static double SIMPLE_MUTATION_RATE = 0.5;

	private static int instances = 0;

	/**
	 * Type of the pattern {@link PatternType}
	 */
	private PatternType type;
	/**
	 * Context used to instantiate constraint from this pattern
	 */
	protected Concept context;
	/**
	 * Parameters
	 */
	protected MMMatch parameters;

	/**
	 * Contains the "link" to subreferences (when embedded in a FO {@link Node_FO first order node}
	 */
	String selfOrSubname = "self";

	/**
	 * Number of fires found by the constraint when applied on models (this value is general and must be used as a
	 * buffer!)<br/>
	 * <ol>
	 * <li>Clean fires</li>
	 * <li>Execute the pattern/constraint on a model (or more than one)</li>
	 * <li>Get the resulting number of fires</li>
	 * <li>Use it as your own</li>
	 * <li>Clean back fires</li>
	 * </ol>
	 * 
	 */
	private int fires = 0;

	/**
	 * Indicate if the buffer has been modified.<br/>
	 * This is used to avoid rebuilding the OCL string every time the pattern is used to evaluate a model. The string is
	 * only rebuilt when pattern has been <em>modified</em> (i.e., most probably when a mutation happen.
	 */
	protected boolean modified = true;

	/**
	 * Calls the constructor of the Class given as parameter with as parameter the match given as parameter
	 * 
	 * @param c
	 *            A class instanceof Pattern
	 * @param m
	 *            A MMMatch
	 * @return an object of type c instantiated with match m.
	 */
	public static Pattern newInstance(Class<? extends Pattern> c, MMMatch m) {
		LOGGER.finest(c.getSimpleName());
		try {
			return (Pattern) c.getDeclaredConstructor(MMMatch.class).newInstance(m);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException
				| NoSuchMethodException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	public Pattern(String name) {
		super(name);
		parameters = new MMMatch(0);
		type = PatternType.valueOf(getClass().getSimpleName());
		instances++;
		type.instantiation();
	}

	public Pattern(String name, Concept context) {
		this(name);
		this.context = context;
		if (context != null)
			parameters.add(context);
	}

	public static int numberOfInstances() {
		return instances;
	}

	public void setSelfOrSubname(String name) {
		selfOrSubname = name;
	}

	public boolean addParameter(MMElement ne) {
		return parameters.add(ne);
	}

	/**
	 * 
	 * @param idx
	 *            Index of the parameter [0..n]
	 * @return MMElement at the specified index
	 */
	protected MMElement getParameter(int idx) {
		return parameters.get(idx + 1); // '+1' because the first parameter is the context.
	}

	/**
	 * 
	 * @param idx
	 *            Index where to put the MMElement [0..n]
	 * @param mme
	 * @return the element previously at the specified index
	 */
	protected MMElement setParameter(int idx, MMElement mme) {
		return parameters.set(idx + 1, mme); // '+1' because the first parameter is the context.
	}

	public MMMatch getParameters() {
		return parameters;
	}

	/**
	 * Return <code>true</code> if the match <code>m</code> differs from the current one (actually in use) :
	 * <ul>
	 * <li>tests the equality of respective context</li>
	 * <li>tests the equality of each parameter of the MMMatch with the current parameters.</li>
	 * 
	 * @param m
	 *            MMMatch to compare with the current one.
	 * @return <code>true</code> if the Match differs from the current one (actually in use).
	 */
	private boolean matchIsNew(MMMatch m) {
		boolean res = true;
		if (!context.equals(m.get(0)))
			return true;

		// if(parameters.size() != m.size())
		// return true;

		for (int i = 0; i < parameters.size() && i < m.size(); i++)
			res |= !parameters.get(i).equals(m.get(i));
		return res;
	}

	/**
	 * Modify the list of parametter (only if {@link #matchIsNew(MMMatch) the MMMatch is new})
	 * 
	 * @param m
	 * @return
	 */
	protected boolean modifyParameters(MMMatch m) {
		if (!matchIsNew(m))
			return false;
		this.context = (Concept) m.get(0);
		if (m.size() == parameters.size())
			this.parameters = m;
		else {
			for (int i = 0; i < m.size(); i++) {
				parameters.set(i, m.get(i));
			}
		}
		modified = true;
		return true;
	}

	public Concept getContext() {
		return context;
	}

	/**
	 * Mutation : try to find <code>different</code> matches with the <code>same</code> context. If there is not,
	 * {@link #mutateSimple() mutateSimple} is called.
	 * 
	 * @return <code>true</code> if the mutation has been effective (if the pattern has changed, <i>i.e.</i> result =
	 *         <code>!p.equals(mutant)</code> )
	 */
	@Override
	public boolean mutate() {
		ArrayList<MMMatch> matches = getMatches(this.getClass());
		MMMatch[] mmms = new MMMatch[matches.size()];
		// System.out.println("1."+matches); // matches of the patterns
		matches.toArray(mmms);
		for (MMMatch mmMatch : mmms) {
			if (!mmMatch.get(0).getName().equals(context.getName())) { // remove matches of same context
				matches.remove(mmMatch);
			}
		}
		// System.out.println("2."+matches);

		mmms = new MMMatch[matches.size()];
		matches.toArray(mmms);
		for (MMMatch mmMatch : mmms) {
			boolean drop = true;
			for (int i = 0; i < mmMatch.size() && drop; i++)
				drop &= mmMatch.get(i).equals(parameters.get(i));
			if (drop)
				matches.remove(mmMatch); // remove matches with same parameters
		}
		// System.out.println("3."+matches);

		if (matches.isEmpty() || ToolBox.getRandomDouble() > SIMPLE_MUTATION_RATE) { // There is no matches different with
																					// same context
			boolean b = mutateSimple();
			if (!b && !matches.isEmpty())
				return modifyParameters(ToolBox.getRandom(matches));
			else
				return false;
		} else {
			if (!modifyParameters(ToolBox.getRandom(matches))) {
				System.out.println(">--------" + this + "----------<");
				return mutateSimple();
			} else
				return false;
		}
	}

	/**
	 * Mutation of the pattern : changes the match of the pattern.
	 * 
	 * @return <code>true
	 */
	public boolean mutateSimple() {
		ArrayList<MMMatch> matches = getMatches(this.getClass(), context);
		MMMatch m = ToolBox.getRandom(matches);
		return m != null && modifyParameters(m);
	}

	/**
	 * @return Matches to the specified type of pattern
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<MMMatch> getMatches(Class<? extends Pattern> c) {
		try {
			return (ArrayList<MMMatch>) c.getMethod("getMatches").invoke(null, (Object[]) null);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return Matches to the specified type of pattern with Context
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<MMMatch> getMatches(Class<? extends Pattern> c, Concept context) {
		try {
			return (ArrayList<MMMatch>) c.getMethod("getMatches", Concept.class).invoke(null, context);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	public int getFires() {
		return fires;
	}

	public void setFires(int randomInt) {
		fires = randomInt;
	}

	public int addFires(int fires) {
		this.fires += fires;
		return this.fires;
	}

	public int addFire() {
		fires++;
		return fires;
	}

	public void cleanFires() {
		fires = 0;
	}

	public String getOCL_inv() {
		String res = " inv " + getId() + " : \n" + getRawOCLConstraint();
		return res;
	}

	public abstract String getRawOCLConstraint();

	/**
	 * Required to be equal :
	 * <ul>
	 * <li>Same class</li>
	 * <li>Same context</li>
	 * <li>Same number of parameters</li>
	 * <li>For each parameter, all equal, in order</li>
	 * </ul>
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null || (getClass() != o.getClass()))
			return false;
		Pattern p = (Pattern) o;
		if (getParameters().size() != p.getParameters().size())
			return false;
		for (int i = 0; i < getParameters().size(); i++) {
			// System.out.println(getParameter(i-1) + " | "+p.getParameter(i-1)+ " ->
			// "+getParameter(i-1).equals(p.getParameter(i-1)));
			if (!getParameter(i - 1).equals(p.getParameter(i - 1))) // getParameter(-1) is the context
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return simplePrint();
	}

	/*
	 * * ******************* GENE ZONE **************** *
	 */

	/**
	 * Tentative, do not polymorphes well {@link A0_RawText#clone() specific redeclarations for each Patterns}
	 */
	public Pattern clone() {
		Pattern clone = Pattern.newInstance(this.getClass(), parameters.clone());
		return clone;
	}

	/**
	 * Rules don't have a size.
	 */
	@Override
	public int size() {
		return 0;
	}

	public PatternType getType() {
		return type;
	}

	@Override
	public String simplePrint() {
		String res = getName() + ":{";
		for (MMElement elt : getParameters())
			res += elt.simplePrint() + ", ";
		res = res.substring(0, res.length() - 2) + "}";
		return res;
	}

	@Override
	public String prettyPrint(String tab) {
		String res = tab + getName() + ":{";
		for (MMElement elt : getParameters())
			res += elt.prettyPrint() + ", ";
		res = res.substring(0, res.length() - 2) + "}";
		return res;
	}

	/*
	 * * ******************* TEST ZONE **************** *
	 */

	// public static void main(String[] args) {
	// Utils.init();
	// testPatternCreation();
	// }

	public static boolean testPatternCreation() {
		for (int i = 0; i < 17; i++) {
			PatternFactory.createRandomPattern();
		}
		return false;
	}

	@Override
	public String printResultPane(String tab) {
		String res = getName() + "\n" + tab + "{";
		for (MMElement elt : getParameters())
			res += elt.simplePrint() + ", ";
		res = res.substring(0, res.length() - 2) + "}";
		return res;
	}

	/**
	 * Not implemented. Requires <code>Model</code>s to be filled with content - and not only a Jess executable
	 * string.<br/>
	 * Requires model files to be parsed.
	 * 
	 * @param m
	 * @return
	 */
	public int check(Model m) {
		int res = 0;
		return res;
	}
	
	/**
	 * NOT FOR A0 ATTENTION !
	 * @return
	 */
	public Collection<MMElement> getMMElements() {
		return getParameters();
	}
}
