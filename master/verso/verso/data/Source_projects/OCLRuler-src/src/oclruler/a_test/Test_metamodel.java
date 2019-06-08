package oclruler.a_test;

import java.util.ArrayList;
import java.util.logging.Logger;

import oclruler.metamodel.Concept;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.Path;
import oclruler.metamodel.PathMap;
import oclruler.rule.MMMatch;
import oclruler.rule.PatternFactory.PatternType;
import oclruler.rule.patterns.Pattern;
import oclruler.utils.Config;
import oclruler.utils.ToolBox;

/**
 * Tests	public final static Logger LOGGER = Logger.getLogger(MainRawTesting.class.getName());
 related to the package oclruler.'metamodel'
 * @author Batot
 *
 */
public class Test_metamodel {
	
	public final static Logger LOGGER = Logger.getLogger(Test_metamodel.class.getName());
	
	
	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
		LOGGER.info("Entering OCLRuler - 'metamodel' package oclruler.Testing\n");

		ToolBox.init();
//		testPaths_equals();
		
		Concept P = Metamodel.getConcept("P");
		Concept M = Metamodel.getConcept("M");
		Concept D = Metamodel.getConcept("D");
		Concept DD = Metamodel.getConcept("DD");
		
		PathMap pm = P.getPaths();
		
		System.out.println("1. "+pm.prettyPrint());
		
		System.out.println("2. "+M.getPaths(D));
		Path p = ToolBox.getRandom(M.getPaths(D));
		System.out.println(p);
		pm.put( p);
		pm.put( p);
		
		System.out.println(pm.prettyPrint());
		
		System.out.println("\nExit.");

	}

	@SuppressWarnings("unused")
	public static void testPaths_equals() throws Exception {
		if(!Config.METAMODEL_NAME.equals("F")) {
			throw new Exception(" Wrong metamodel : "+Config.METAMODEL_NAME+" - Expected is 'F'.");
		}
		LOGGER.config("metamodel : "+Config.METAMODEL_NAME);
		LOGGER.config(Metamodel.getAllConcepts().toString());
		Concept P = Metamodel.getConcept("P");
		Concept M = Metamodel.getConcept("M");
		Concept D = Metamodel.getConcept("D");
		Concept DD = Metamodel.getConcept("DD");
		
		ArrayList<Path> ps = P.getPaths(D);
		System.out.println(ps.size()+ " paths from P to D : ");
		for (Path path : ps) {
			System.out.println(path);
		}
		
		PathMap pm = P.getPaths();
		System.out.println(pm);
	}

	/**
	 */
	public static void testPaths_generation() throws Exception {
		if(!Config.METAMODEL_NAME.equals("F")) {
			throw new Exception(" Wrong metamodel : "+Config.METAMODEL_NAME+" - Expected is 'F'.");
		}
		LOGGER.config("metamodel : "+Config.METAMODEL_NAME);
		LOGGER.config(Metamodel.getAllConcepts().toString());
		Concept P = Metamodel.getConcept("P");
		Concept M = Metamodel.getConcept("M");
		Concept D = Metamodel.getConcept("D");
		Concept DD = Metamodel.getConcept("DD");
		
		ArrayList<Path> ps = P.getPaths(D, 1);
		if(ps.size() != 1) {
			throw new Exception(" Wrong result : "+ps.size()+" - Expected is 1.");
		}
		
		ps = P.getPaths( M, 2);
		if(ps.size() != 4) {
			throw new Exception(" Wrong result : "+ps.size()+" - Expected is 4.");
		}
		
		ps = P.getPaths( D, 3);
		if(ps.size() != 16) {
			throw new Exception(" Wrong result : "+ps.size()+" - Expected is 16.");
		}
	
		ps = P.getPaths( M, 4);
		if(ps.size() != 64) {
			throw new Exception(" Wrong result : "+ps.size()+" - Expected is 64.");
		}
		
		ps = P.getPaths( DD, 4);
		if(ps.size() != 0) {
			throw new Exception(" Wrong result : "+ps.size()+" - Expected is 0.");
		}
		
		if(P.getPaths().fullSize() != (1+4+16+64)) {
			throw new Exception(" Wrong result : "+ps.size()+" - Expected is 64.");
		}
		
		LOGGER.config("Paths : ");
		LOGGER.config(P.getPaths().prettyPrint());
	}



	public static void testGetMatches_Metamodel_F() throws Exception {
			if(!Config.METAMODEL_NAME.equals("F")) {
				throw new Exception(" Wrong metamodel : "+Config.METAMODEL_NAME+" - Expected is 'F'.");
			}
	//		LOGGER.config("metamodel : "+Config.METAMODEL_NAME);
	//		LOGGER.config(Metamodel.getConcepts().toString());
			
			PatternType pt = PatternType.A8_CollectionsSize;
			testGetMatches(pt, null, 8);
			testGetMatches(pt, Metamodel.getConcept("P"), 2);
			testGetMatches(pt, Metamodel.getConcept("M"), 2);
			testGetMatches(pt, Metamodel.getConcept("D"), 2);
			testGetMatches(pt, Metamodel.getConcept("DD"), 2);
			
			pt = PatternType.A1_AcyclicReference;
			testGetMatches(pt, null, 2);
			testGetMatches(pt, Metamodel.getConcept("P"), 2);
			testGetMatches(pt, Metamodel.getConcept("M"), 0);
			testGetMatches(pt, Metamodel.getConcept("D"), 0);
			testGetMatches(pt, Metamodel.getConcept("DD"), 0);
	
			pt = PatternType.A2_AutocontainerOneToMany;
			testGetMatches(pt, null, 4);
			testGetMatches(pt, Metamodel.getConcept("P"), 4);
			testGetMatches(pt, Metamodel.getConcept("M"), 0);
			testGetMatches(pt, Metamodel.getConcept("D"), 0);
			testGetMatches(pt, Metamodel.getConcept("DD"), 0);
	
			pt = PatternType.A3_UniqueIdentifierStructuralFeature;
			testGetMatches(pt, null, 8);
			testGetMatches(pt, Metamodel.getConcept("P"), 4);
			testGetMatches(pt, Metamodel.getConcept("M"), 2);
			testGetMatches(pt, Metamodel.getConcept("D"), 2);
			testGetMatches(pt, Metamodel.getConcept("DD"), 0);
	
			pt = PatternType.A4_AutocontainerManyToMany;
			testGetMatches(pt, null, 4);
			testGetMatches(pt, Metamodel.getConcept("P"), 4);
			testGetMatches(pt, Metamodel.getConcept("M"), 0);
			testGetMatches(pt, Metamodel.getConcept("D"), 0);
			testGetMatches(pt, Metamodel.getConcept("DD"), 0);
	
			pt = PatternType.A5_CollectionIsSubset;
			testGetMatches(pt, null, 4);
			testGetMatches(pt, Metamodel.getConcept("P"), 4);
			testGetMatches(pt, Metamodel.getConcept("M"), 0);
			testGetMatches(pt, Metamodel.getConcept("D"), 0);
			testGetMatches(pt, Metamodel.getConcept("DD"), 0);
	
			pt = PatternType.A7_CollectionsSameSize;
			testGetMatches(pt, null, 8);
			testGetMatches(pt, Metamodel.getConcept("P"), 2);
			testGetMatches(pt, Metamodel.getConcept("M"), 2);
			testGetMatches(pt, Metamodel.getConcept("D"), 2);
			testGetMatches(pt, Metamodel.getConcept("DD"), 2);
	
			pt = PatternType.A8_CollectionsSize;
			testGetMatches(pt, null, 8);
			testGetMatches(pt, Metamodel.getConcept("P"), 2);
			testGetMatches(pt, Metamodel.getConcept("M"), 2);
			testGetMatches(pt, Metamodel.getConcept("D"), 2);
			testGetMatches(pt, Metamodel.getConcept("DD"), 2);
	
			pt = PatternType.A9_OppositeReferencesOneToOne;
			testGetMatches(pt, null, 8);
			testGetMatches(pt, Metamodel.getConcept("P"), 4);
			testGetMatches(pt, Metamodel.getConcept("M"), 2);
			testGetMatches(pt, Metamodel.getConcept("D"), 2);
			testGetMatches(pt, Metamodel.getConcept("DD"), 0);
	
			pt = PatternType.A10_OppositeReferencesOneToMany;
			testGetMatches(pt, null, 8);
			testGetMatches(pt, Metamodel.getConcept("P"), 4);
			testGetMatches(pt, Metamodel.getConcept("M"), 2);
			testGetMatches(pt, Metamodel.getConcept("D"), 2);
			testGetMatches(pt, Metamodel.getConcept("DD"), 0);
	
			pt = PatternType.A11_AttributeValueComparison;
			testGetMatches(pt, null, 8);
			testGetMatches(pt, Metamodel.getConcept("P"), 2);
			testGetMatches(pt, Metamodel.getConcept("M"), 2);
			testGetMatches(pt, Metamodel.getConcept("D"), 2);
			testGetMatches(pt, Metamodel.getConcept("DD"), 2);
	
			pt = PatternType.A12_AttributeUndefined;
			testGetMatches(pt, null, 16);
			testGetMatches(pt, Metamodel.getConcept("P"), 4);
			testGetMatches(pt, Metamodel.getConcept("M"), 4);
			testGetMatches(pt, Metamodel.getConcept("D"), 4);
			testGetMatches(pt, Metamodel.getConcept("DD"), 4);
	
			pt = PatternType.A13_CollectionIncludesSelf;
			testGetMatches(pt, null, 6);
			testGetMatches(pt, Metamodel.getConcept("P"), 4);
			testGetMatches(pt, Metamodel.getConcept("M"), 1);
			testGetMatches(pt, Metamodel.getConcept("D"), 1);
			testGetMatches(pt, Metamodel.getConcept("DD"), 0);
	
			pt = PatternType.A14_ReferenceDifferentFromSelf;
			testGetMatches(pt, null, 4);
			testGetMatches(pt, Metamodel.getConcept("P"), 2);
			testGetMatches(pt, Metamodel.getConcept("M"), 1);
			testGetMatches(pt, Metamodel.getConcept("D"), 1);
			testGetMatches(pt, Metamodel.getConcept("DD"), 0);
	
			pt = PatternType.A15_ReferenceIsTypeOf;
			testGetMatches(pt, null, 16);
			testGetMatches(pt, Metamodel.getConcept("P"), 8);
			testGetMatches(pt, Metamodel.getConcept("M"), 2);
			testGetMatches(pt, Metamodel.getConcept("D"), 4);
			testGetMatches(pt, Metamodel.getConcept("DD"), 2);
	
			pt = PatternType.A16_BooleanProperty;
			testGetMatches(pt, null, 4);
			testGetMatches(pt, Metamodel.getConcept("P"), 1);
			testGetMatches(pt, Metamodel.getConcept("M"), 1);
			testGetMatches(pt, Metamodel.getConcept("D"), 1);
			testGetMatches(pt, Metamodel.getConcept("DD"), 1);
	
			pt = PatternType.A17_TwoNumbersComparison;
			testGetMatches(pt, null, 8);
			testGetMatches(pt, Metamodel.getConcept("P"), 2);
			testGetMatches(pt, Metamodel.getConcept("M"), 2);
			testGetMatches(pt, Metamodel.getConcept("D"), 2);
			testGetMatches(pt, Metamodel.getConcept("DD"), 2);
	
			pt = PatternType.A18_SelfIsSubtype;
			testGetMatches(pt, null, 4);
			testGetMatches(pt, Metamodel.getConcept("P"), 0);
			testGetMatches(pt, Metamodel.getConcept("M"), 1);
			testGetMatches(pt, Metamodel.getConcept("D"), 1);
			testGetMatches(pt, Metamodel.getConcept("DD"), 2);
	
			pt = PatternType.A19_UniqueInstance;
			testGetMatches(pt, null, 4);
			testGetMatches(pt, Metamodel.getConcept("P"), 1);
			testGetMatches(pt, Metamodel.getConcept("M"), 1);
			testGetMatches(pt, Metamodel.getConcept("D"), 1);
			testGetMatches(pt, Metamodel.getConcept("DD"), 1);
		}

	public static void testGetMatches(PatternType pt, Concept c, int test) throws Exception {
		if(c == null){
			if(Pattern.getMatches(pt.getInstanciationClass()).size() != test){
				printAllConceptMatches(pt);
				throw new Exception("Erreur dans "+pt.shortName()+".getMatches(Concept) ");
			}
		} else{
			if(Pattern.getMatches(pt.getInstanciationClass(), c).size() != test){
				printAllConceptMatches(pt);
				throw new Exception("Erreur dans "+pt.shortName()+".getMatches(Concept) ");
			}
		}
	}

	public static void printAllConceptMatches(PatternType pt) {
		System.out.println(pt);
		for (Concept c : Metamodel.getAllConcepts().values()) {
			System.out.println(c.getName());
			for (MMMatch match : Pattern.getMatches(pt.getInstanciationClass(), c) ) {
				System.out.println("    "+ match);
			}
		}
	}
	
	
	
	
}
