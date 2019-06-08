package oclruler.rule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import oclruler.metamodel.Concept;
import oclruler.rule.patterns.Pattern;
import oclruler.utils.Config;
import oclruler.utils.ToolBox;

/**
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class PatternFactory {
	private static Logger LOGGER = Logger.getLogger(PatternFactory.class.getName());
	
	public static String OCL_CHOICE_FILE_NAME = null;
	public static  File OCL_CHOICE_FILE = null;
	public static final String OCL_CHOICE_FILE_DEFAULT = "src/oclruler/utils/"+"all.oclpatterns";
	public static ArrayList<PatternType> ocl_choice_list; 
	
	
	public static void loadConfig(){
		
		if (OCL_CHOICE_FILE_NAME == null)
			OCL_CHOICE_FILE_NAME = Config.getStringParam("OCL_CHOICE_FILE");
		else {
			Config.overrideConfigFileParameter("OCL_CHOICE_FILE", OCL_CHOICE_FILE_NAME);
		}
		ocl_choice_list = new ArrayList<>();
		
		if(OCL_CHOICE_FILE_NAME != null)
			OCL_CHOICE_FILE = new File(OCL_CHOICE_FILE_NAME);
		if (OCL_CHOICE_FILE == null || !OCL_CHOICE_FILE.exists()) {
			LOGGER.warning("No OCL_PATTERNS file specified. All patterns considered.");
			
			OCL_CHOICE_FILE = new File(OCL_CHOICE_FILE_DEFAULT);
			
			if(Config.SINBAD)
				OCL_CHOICE_FILE =  new File("./config/"+"all.oclpatterns");
		}

		try {
			BufferedReader br = new BufferedReader(new FileReader(OCL_CHOICE_FILE));
			String l;
			int line = 1;
			while ((l = br.readLine()) != null) {
				l = l.trim();
				if (!l.isEmpty()) {
					boolean del = l.startsWith("-");
					if (l.startsWith("-") || l.startsWith("+"))
						l = l.substring(1).trim();
					PatternType pt;
					try {
						pt = PatternType.valueOf(l);
					} catch (Exception e) {
						LOGGER.severe("Pattern type '" + l + "' does not exist. (line:" + line + ")");
						pt = null;
					}

					if (pt != null) {
						if (del)
							bannish(pt);
						else
							ocl_choice_list.add(pt);
					}
				}
				line++;
			}
			LOGGER.config("OCL patterns considered : " + ocl_choice_list.size() + " (from '" + OCL_CHOICE_FILE.getAbsolutePath() + "')");
			LOGGER.fine("List : " + ocl_choice_list);
			LOGGER.config("Bannished : " + banishedTypes);
			br.close();
		} catch (IOException e) {
			LOGGER.warning("OCL_PATTERNS file incorrect. All patterns considered.");
			e.printStackTrace();
		}

	}
	
	/**
	 * Initialization : 
	 * <ul>
	 * 	<li>A0 rules connot be instantiated automatically, they are banned.</li>
	 * 	<li>A15 rules are unsure (OCL exceptions), they are banned.</li>
	 * </ul>
	 */
	public static void init(){
		bannish(PatternType.A0_RawText);
//		bannish(PatternType.A15_ReferenceIsTypeOf);
	}
	
	/**
	 * Creates a random instance
	 * @param c
	 * @return
	 */
	public static Pattern randomInstance(Class<? extends Pattern> c){
		if(c == null)
			throw new IllegalArgumentException("Class must NOT be null.");
		try {
			ArrayList<MMMatch> matches = Pattern.getMatches(c);
			
			if(!matches.isEmpty()){
				MMMatch match = ToolBox.getRandom(matches);
				if(LOGGER.isLoggable(Level.FINEST)){
					String log = "";
					for (Object mElt : matches) 
						log += (mElt) + " " ;
					LOGGER.finest(matches.size()+" matches found for pattern '"+c.getSimpleName()+"' : {"+log.trim()+"}");
				}	else {
					LOGGER.finer(matches.size()+" matches found for pattern '"+c.getSimpleName()+"'");
				}
				return  Pattern.newInstance(c, match);
			} else {
				LOGGER.warning("No matches found for pattern type '"+c.getSimpleName()+"' -> Pattern banished !");
				PatternType pt = PatternType.valueOf(c.getSimpleName());
				bannish(pt);
			}
			
		} catch ( IllegalArgumentException | SecurityException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	protected static HashSet<Pattern> banishedPatterns = new HashSet<>();
	public static void bannish(Pattern p){
		banishedPatterns.add(p);
	}
	
	protected static HashSet<PatternType> banishedTypes = new HashSet<>();
	public static void bannish(PatternType pt){
		banishedTypes.add(pt);
		Concept.reinitializeInstanciablePatternList();
	}
	public static void unbannish(PatternType pt){
		banishedTypes.remove(pt);
		Concept.reinitializeInstanciablePatternList();
	}
	
	public static Pattern createRandomPattern(Concept context){
		ArrayList<Pattern> instantiables = context.getInstatiablePatterns();
		Pattern p = ToolBox.getRandom(instantiables);
		
		return p != null ? p.clone() : null;
	}

	public static Pattern createRandomPattern(){
		Pattern res = createRandomPattern(ToolBox.getRandom(PatternType.enabledValues()));
		if(res != null)
			LOGGER.fine(res.toString());
		return res;
	}
	
	public static Pattern createRandomPattern(PatternType type){
		Class<? extends Pattern> c = type.getInstanciationClass();
		Pattern p = randomInstance(c);
		return p;
	}
	
	
	public enum PatternType {
		A0_RawText("A0_RawText"), 
		A1_AcyclicReference("A1_AcyclicReference"),
		A2_AutocontainerOneToMany("A2_AutocontainerOneToMany"),
		A3_UniqueIdentifierStructuralFeature("A3_UniqueIdentifierStructuralFeature"),
		A4_AutocontainerManyToMany("A4_AutocontainerManyToMany"),
		A5_CollectionIsSubset("A5_CollectionIsSubset"),
		A7_CollectionsSameSize("A7_CollectionsSameSize"),
		A8_CollectionsSize("A8_CollectionsSize"),
		A9_OppositeReferencesOneToOne("A9_OppositeReferencesOneToOne"),
		A10_OppositeReferencesOneToMany("A10_OppositeReferencesOneToMany"),
		A11_AttributeValueComparison("A11_AttributeValueComparison"),
		A12_AttributeUndefined("A12_AttributeUndefined"),
		A13_CollectionIncludesSelf("A13_CollectionIncludesSelf"),
		A14_ReferenceDifferentFromSelf("A14_ReferenceDifferentFromSelf"),
		A15_ReferenceIsTypeOf("A15_ReferenceIsTypeOf"),// NOT WORKING in OCL !!! 2017-06-06 - still true ?
		A16_BooleanProperty("A16_BooleanProperty"),
		A17_TwoNumbersComparison("A17_TwoNumbersComparison"),
		A18_SelfIsSubtype("A18_SelfIsSubtype"),
		A19_UniqueInstance("A19_UniqueInstance");
		
		private String name = "";
		Class<? extends Pattern> instantiationCLass = null;
		
		public static boolean LIST_HAS_CHANGED = true;
		// Constructeur
		@SuppressWarnings("unchecked")
		PatternType(String name) {
			this.name = name;
			try {
				instantiationCLass = (Class<? extends Pattern>)Class.forName(getCompleteName());
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Class not found : '"+getCompleteName()+"'.");
			}
		}

		public String toString() {
			if(LOGGER.isLoggable(Level.CONFIG))
				return shortName();
			else
				return name;
		}
		
		public String shortName(){
			return name.split("_")[0];
		}
		public String getCompleteName(){
			return "oclruler.rule.patterns."+name;
		}
		public String getName(){
			return name;
		}
		
		public Class<? extends Pattern> getInstanciationClass(){
			return instantiationCLass;
		}
		
		public static ArrayList<PatternType> enabledValues(){
			ArrayList<PatternType> res = new ArrayList<>();
			for (int i = 0; i < PatternType.values().length; i++) {
				if(!banishedTypes.contains( PatternType.values()[i]) )
					res.add(PatternType.values()[i]);
			}
			return res;
		}
		
		public int instantiation(){
			return ++instantiations;
		}
		public int getInstantiations() {
			return instantiations;
		}
		int instantiations = 0;
		public static PatternType get(int idx) {
			return values()[idx];
		}

		public static void enable(int idx, boolean value) {
			values()[idx].enable(value);
		}

		public void enable(boolean value) {
			if(value)
				unbannish(this);
			else
				bannish(this);
		}

		public boolean isEnabled() {
			for (PatternType pt : banishedTypes) {
				if(pt == this)
					return true;
			}
			return false;
		}
	}

	public static HashSet<PatternType> getBannishedPatternTypes() {
		return banishedTypes;
	}

	/**
	 * Tries to instantiate all types of patterns {@link #createRandomPattern(PatternType)}.
	 * If no matches found on the metamodel, the pattern is bannished  {@link #randomInstance(Class)}.
	 */
	public static void checkPatternsToBannish() {
		for (PatternType pt : PatternType.enabledValues()) {
			createRandomPattern(pt);
		}
	}

}
