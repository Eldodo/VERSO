package verso.reverse.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import verso.reverse.enums.Modifiers;
import verso.reverse.model.UMLClass;
import verso.reverse.model.UMLMethod;
import verso.reverse.model.UMLVariable;

/**
 * Parses Java classes to create {@link UMLClass} for each input class.
 * Deals with variables, constructors and methods.
 * Also, calls {@link Counselor} for creating relationships if any
 * @author rishi
 *
 */
public class ParseJava {
	private Counselor counselor;
	
	/**
	 * Constructor for class
	 */
	public ParseJava() {
		counselor = Counselor.getInstance();
	}
	
	
	/**
	 * Parsing begins here for each input file
	 * @param files
	 */
	public void parseFiles(List<File> files){
		try{
			for(File file : files){
				System.out.println("Parsing " + file.getAbsolutePath() + " file...");
				CompilationUnit compliationUnit = JavaParser.parse(file);
				compliationUnit.accept(new VoidVisitorAdapter<Void>() {
		            @Override
		            public void visit(ClassOrInterfaceDeclaration n, Void arg) {
		            	//System.out.println(n.getName());
		            	UMLClass newUMLClass = new UMLClass();
		        		newUMLClass.setName(n.getName().asString());
		            	counselor.addUMLClass(newUMLClass);
		                super.visit(n, arg);
		            }
		        }, null);
				createUMLClass(compliationUnit);
			}
			//counselor.removeUnneccessaryMethods();
		}catch(FileNotFoundException ex){
			System.err.println("Error: File not found. Trace: "+ ex.getMessage());
		}
	}
	
	/**
	 * Creates {@link UMLClass} for input Java Class.
	 * @param compliationUnit
	 */
	private void createUMLClass(CompilationUnit compliationUnit){
		List<TypeDeclaration<?>> types = compliationUnit.getTypes();
		//TODO surement à corriger 
		for(TypeDeclaration type : types){
			List<BodyDeclaration> bodyDeclarations = type.getMembers();
			boolean isInterface = ((ClassOrInterfaceDeclaration) type).isInterface();
			
			UMLClass umlClass = counselor.getUMLClass(type.getName().asString());
			if(umlClass!=null) {
				umlClass.setInterface(isInterface);
				
				counselor.checkForRelatives(umlClass, type);
				
				for(BodyDeclaration body : bodyDeclarations){
					if(body instanceof FieldDeclaration){
						createUMLVariables(umlClass, (FieldDeclaration) body);
					}else if(body instanceof MethodDeclaration){
						createUMLMethods(umlClass, (MethodDeclaration) body, false);
					}else if(body instanceof ConstructorDeclaration){
						createUMLMethods(umlClass, (ConstructorDeclaration) body, true);
					}else if(body instanceof ClassOrInterfaceDeclaration){
						UMLClass inner = counselor.getUMLClass(((ClassOrInterfaceDeclaration)body).getName().asString());
						if(inner!=null)
							counselor.getUMLClasses().remove(inner);
							//createUMLClass(umlClass, inner);
						//System.out.println("Il y a quelque chose qu'on ignore : "+((ClassOrInterfaceDeclaration)body).getName());
					}
				}
				//System.out.println("Add1 : "+umlClass.getName());
				counselor.addUMLClass(umlClass);
			}
		}
	}
	
	private void createUMLClass(UMLClass umlClass, UMLClass classOrInterface) {
		counselor.declareAsInner(umlClass, classOrInterface);
	}
	
	/**
	 * All instance variables are parsed here
	 * @param umlClass
	 * @param field
	 */
	private void createUMLVariables(UMLClass umlClass, FieldDeclaration field){
		List<VariableDeclarator> variables = field.getVariables();
		for(VariableDeclarator variable : variables){
			UMLVariable umlVariable = new UMLVariable();
			umlVariable.setModifier(field.getModifiers());
			umlVariable.setName(variable.getName().asString());
			String initialValue;
			if(variable.getInitializer() == null)
				initialValue = "";
			else if(variable.getInitializer().toString().length()<30)
				initialValue =  " = " + variable.getInitializer().toString();
			else initialValue =  " = " + variable.getInitializer().toString().substring(0, 27)+"...";
			umlVariable.setInitialValue(initialValue);
			//System.out.println("value: "+umlVariable.getInitialValue());
			umlVariable.setUMLClassType(UMLHelper.isUMLClassType(field.getElementType()));
			umlVariable.setType(field.getElementType());
			umlClass.getUMLVariables().add(umlVariable);
			counselor.checkForRelatives(umlClass, umlVariable);
		}
	}
	
	/**
	 * All the methods including constructors are parsed here
	 * @param umlClass
	 * @param body
	 * @param isConstructor
	 */
	private void createUMLMethods(UMLClass umlClass, BodyDeclaration body, boolean isConstructor){
		UMLMethod umlMethod = new UMLMethod();
		if(isConstructor){
			ConstructorDeclaration constructor = (ConstructorDeclaration) body;
			umlMethod.setConstructor(true);
			umlMethod.setModifier(constructor.getModifiers());
			umlMethod.setName(constructor.getName().asString());
			umlMethod.setParameters(constructor.getParameters());
			parseMethodBody(umlClass, constructor.getBody());
		}else {
			MethodDeclaration method = (MethodDeclaration) body;
			umlMethod.setConstructor(false);
			umlMethod.setModifier(umlClass.isInterface() ? EnumSet.of(Modifier.PUBLIC, Modifier.ABSTRACT) : method.getModifiers());
			umlMethod.setName(method.getName().asString());
			umlMethod.setParameters(method.getParameters());
			umlMethod.setType(method.getType());
			//System.out.println(umlMethod.getName() + umlMethod.getModifier());
			
			parseMethodBody(umlClass, method.getBody());
		}
		umlClass.getUMLMethods().add(umlMethod);
		counselor.checkForRelatives(umlClass, umlMethod);		
	}
	
	private void parseMethodBody(UMLClass umlClass, BlockStmt body) {
		for(Statement statement : body.getStatements()){
			if(statement instanceof ExpressionStmt && ((ExpressionStmt) statement).getExpression() instanceof VariableDeclarationExpr){
				VariableDeclarationExpr expression = (VariableDeclarationExpr) (((ExpressionStmt) statement).getExpression());
				counselor.checkForRelatives(umlClass, expression);
			}
		}
		
	}


	/**
	 * Method body parsing
	 * @param umlClass
	 * @param optional
	 */
	private void parseMethodBody(UMLClass umlClass, Optional<BlockStmt> optional){
		if(optional == null || !optional.isPresent()){
			return;
		}
		BlockStmt methodStmts = optional.get();
		for(Statement statement : methodStmts.getStatements()){
			if(statement instanceof ExpressionStmt && ((ExpressionStmt) statement).getExpression() instanceof VariableDeclarationExpr){
				VariableDeclarationExpr expression = (VariableDeclarationExpr) (((ExpressionStmt) statement).getExpression());
				counselor.checkForRelatives(umlClass, expression);
			}
		}
	}
}