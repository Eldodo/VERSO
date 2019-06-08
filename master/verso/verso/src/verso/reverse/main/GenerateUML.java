package verso.reverse.main;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.github.javaparser.ast.Modifier;

import verso.reverse.enums.Modifiers;
import verso.reverse.enums.RelationType;
import verso.reverse.model.Relationship;
import verso.reverse.model.UMLClass;
import verso.reverse.model.UMLMethod;
import verso.reverse.model.UMLVariable;

import net.sourceforge.plantuml.SourceStringReader;

/**
 * Generates the UML based on the {@link UMLClass} and {@link Relationship}
 * provided by {@link Counselor}
 * @author rishi
 *
 */
public class GenerateUML {

	private Counselor counselor;
	
	//Limits
	private int limitMethods;
	private int limitParametersString;
	private int limitVariables;
	
	/**
	 * Constructor fetches instance of {@link Counselor}
	 */
	public GenerateUML() {
		limitMethods=limitParametersString=limitVariables=100000;
		counselor = Counselor.getInstance();
	}
	
	public GenerateUML(int limitmethods, int limitparameters, int limitvariables) {
		this.limitMethods=limitmethods;
		this.limitParametersString = limitparameters;
		this.limitVariables=limitvariables;
		counselor = Counselor.getInstance();
	}
	
	/**
	 * Generation of UML for class diagram is done by writing the grammar
	 * @param outputFileName
	 */
	public void createGrammar(String outputFileName){
		if(outputFileName.indexOf('.') != -1){
			outputFileName = outputFileName.split("\\.")[0];
		}
		StringBuilder umlSource = new StringBuilder();
		umlSource.append("@startuml \nskinparam classAttributeIconSize 0\n");
		for(UMLClass umlClass : counselor.getUMLClasses()){
			if(umlClass.isInterface()){
				umlSource.append("interface " + umlClass.getName() + " << interface >> {\n");
			}else {
				umlSource.append("class " + umlClass.getName() + " {\n");
			}
			
			boolean hasSetter = false;
			boolean hasGetter = false;
			String setVariable = "";
			String getVariable = "";
			UMLMethod setterMethod = null;
			UMLMethod getterMethod = null;
			List<UMLMethod> methods = umlClass.getUMLMethods();
			int limit = 0;
			//TODO Methods
			/*for(UMLMethod method : methods){
				System.out.println(method.getName()+method.getModifier());
				if(limit<this.limitMethods) {
					/*if(!isMethodPublic(method)){
						continue;
					}/
					if(method.isConstructor()){
						if(method.getParameterizedUMLString().length()>this.limitParametersString)
							umlSource.append(method.getParameterizedUMLString().substring(0, this.limitParametersString-3)+"...\n");
						else
							umlSource.append(method.getParameterizedUMLString());
					}else if(method.getName().contains("set") && method.getName().split("set").length > 1){
						hasSetter = true;
						setVariable = method.getName().split("set")[1];
						setterMethod = method;
					}else if(method.getName().contains("get") && method.getName().split("get").length > 1){
						hasGetter = true;
						getVariable = method.getName().split("get")[1];
						getterMethod = method;
					}else if(isMethodPublic(method)){
						if(method.getParameterizedUMLString().length()>this.limitParametersString)
							umlSource.append(method.getParameterizedUMLString().substring(0, this.limitParametersString-3)+"...\n");
						else
							umlSource.append(method.getParameterizedUMLString());
					}
					
				}else {umlSource.append("...\n");break;}
				limit++;
			}
			if(hasGetter && hasSetter && setVariable.equalsIgnoreCase(getVariable) && setterMethod != null){
				if(umlClass.hasVariable(getVariable)){
					counselor.updateVariableToPublic(umlClass, getVariable);
					counselor.removeSetterGetterMethod(umlClass, getterMethod, setterMethod);
				}else {
					umlSource.append(getterMethod.getParameterizedUMLString());
					umlSource.append(setterMethod.getParameterizedUMLString());
				}
			}*/
			
			List<UMLVariable> variables = umlClass.getUMLVariables();
			limit = 0;
			//TODO
			/*for(UMLVariable variable : variables){
				if(limit<this.limitVariables) {
					if(!variable.getModifier().contains(Modifier.PROTECTED) && !variable.isUMLClassType()){
						if(variable.getUMLString().length()>this.limitParametersString)
							umlSource.append(variable.getUMLString().substring(0, this.limitParametersString-3)+"...\n");
						else
							umlSource.append(variable.getUMLString());
					}
				}
				else {umlSource.append("...\n");break;}
				limit++;
			}*/
			
			umlSource.append("}\n\n");
		}
		
		for(Relationship relationship : counselor.getRelationships()){
			//if(relationship.getType() != RelationType.INNERCLASS){
				umlSource.append(relationship.getUMLString());
			//}
		}
		
		umlSource.append("hide circle \n@enduml");
		dumpGrammarToFile(umlSource.toString());
		generateUML(outputFileName, umlSource.toString());
	}
	
	/**
	 * Actually generates the PNG with provided output file name
	 * @param outputFileName
	 * @param umlSource
	 */
	private void generateUML(String outputFileName, String umlSource){
		try{
			OutputStream png = new FileOutputStream(outputFileName + ".png");
			SourceStringReader reader = new SourceStringReader(umlSource);
			reader.generateImage(png);
			System.out.println("Output UML Class diagram with name '"+outputFileName +".png' is generated in base directory");
		}
		catch (FileNotFoundException exception) {
			System.err.println("Failed to create output file " + exception.getMessage());
		}
		catch (IOException exception){
			System.err.println("Failed to write to output file " + exception.getMessage());
		}
	}
	
	/**
	 * Returns true if the method is public, checks for all combinations
	 * @param method
	 * @return
	 */
	private boolean isMethodPublic(UMLMethod method){
		if(method.getModifier().contains(Modifier.PUBLIC)){
			return true;
		}
		return false;
	}
	
	/**
	 * Dumps the grammar generated for UML Class diagram to txt file.
	 * @param grammar
	 */
	private void dumpGrammarToFile(String grammar){
	    try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("PlantUMLGrammar.txt"));
			writer.write(grammar);
		    writer.close();
		    System.out.println("PlantUML grammar is dumped in 'PlantUMLGrammar.txt' file in base directory");
		} catch (IOException e) {
			System.err.println("Failed to dump grammar to text file: "+ e.getMessage());
		}
	    
	}
}
