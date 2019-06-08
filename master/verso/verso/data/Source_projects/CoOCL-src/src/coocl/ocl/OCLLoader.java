package coocl.ocl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import oclruler.metamodel.Metamodel;
import utils.Config;
import utils.Utils;

public class OCLLoader {
	public static Logger LOGGER = Logger.getLogger(OCLLoader.class.getName());

	public static void main(String[] args) {
		LOGGER.info("Entering OCLRuler - OCL tests");
		Utils.init();
		Metamodel metamodel1 = Metamodel.getMm1();
		Metamodel metamodel2 = Metamodel.getMm2();
		
		System.out.println(metamodel1.ePackages());
		
		
		
		System.out.println("** Load 1");
		try {
			InputStream in = new FileInputStream(new File(Config.DIR_TESTS + "/ocl/test_Statemachine_1.ocl"));
//			new Program(metamodel1).load(metamodel1, in);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println("\n\n** Random 1");
		File f = new File(Config.DIR_TESTS + "/ocl/test_Statemachine_1.ocl");
		Program prg;
			try {
				prg = CollectOCLIds.newCollectOCLId(metamodel1).load(f);
			} catch (IOException e) {
				throw new IllegalArgumentException("OCL file not found: '"+f.getAbsolutePath()+"'\n OCL_FILE must be changed in config.properties.");
			}
//					new Program(metamodel1, new File(Config.DIR_TESTS + "/ocl/test_Statemachine_1.ocl"));
			String rndOCLPrg = prg.printExecutableOCL();
			System.out.println(rndOCLPrg);
		
	}

}
/*
 *
 *  Methods to override while visiting OCL AST
 *  
 *  
visitAssociationClassCallExp(AssociationClassCallExp<C, P>)
visitBooleanLiteralExp(BooleanLiteralExp<C>)
visitCollectionItem(CollectionItem<C>)
visitCollectionLiteralExp(CollectionLiteralExp<C>)
visitCollectionRange(CollectionRange<C>)
visitConstraint(CT)
visitEnumLiteralExp(EnumLiteralExp<C, EL>)
visitExpressionInOCL(ExpressionInOCL<C, PM>)
visitIfExp(IfExp<C>)
visitIntegerLiteralExp(IntegerLiteralExp<C>)
visitInvalidLiteralExp(InvalidLiteralExp<C>)
visitIterateExp(IterateExp<C, PM>)
visitIteratorExp(IteratorExp<C, PM>)
visitLetExp(LetExp<C, PM>)
visitMessageExp(MessageExp<C, COA, SSA>)
visitNullLiteralExp(NullLiteralExp<C>)
visitOperationCallExp(OperationCallExp<C, O>)
visitPropertyCallExp(PropertyCallExp<C, P>)
visitRealLiteralExp(RealLiteralExp<C>)
visitStateExp(StateExp<C, S>)
visitStringLiteralExp(StringLiteralExp<C>)
visitTupleLiteralExp(TupleLiteralExp<C, P>)
visitTupleLiteralPart(TupleLiteralPart<C, P>)
visitTypeExp(TypeExp<C>)
visitUnlimitedNaturalLiteralExp(UnlimitedNaturalLiteralExp<C>)
visitUnspecifiedValueExp(UnspecifiedValueExp<C>)
visitVariable(Variable<C, PM>)
visitVariableExp(VariableExp<C, PM>)
 * 
*/

