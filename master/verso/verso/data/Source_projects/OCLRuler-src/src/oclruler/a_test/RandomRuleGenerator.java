package oclruler.a_test;

import java.util.logging.Logger;

import oclruler.rule.Program;
import oclruler.utils.ToolBox;

public class RandomRuleGenerator {
	public final static Logger LOGGER = Logger.getLogger(RandomRuleGenerator.class.getName());
	
	public static void main(String[] args) {
		LOGGER.info("Entering OCLRuler - Random rule generator\n");
		ToolBox.init();
		
		
		int numberConstraintPerProgram = 5;
		
		Program prg = Program.createRandomProgram(numberConstraintPerProgram);
		System.out.println(prg.prettyPrint());
		
	}
	
}
