package verso.reverse.main;


import java.util.Optional;

import verso.reverse.model.Relationship;
import verso.reverse.model.UMLClass;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;

/**
 * Helper provides methods for important checks and operations
 * @author rishi
 *
 */
public class UMLHelper {

	/**
	 * Checks if the {@link Type} is of {@link UMLClass} type or not.
	 * Ignores java objects and collections as we are just concentrating on the 
	 * input java classes.
	 * @param type
	 * @return
	 */
	public static boolean isUMLClassType(Type type){
		if(type instanceof PrimitiveType){
			return false;
		}else if(type instanceof ReferenceType){
			if(((ReferenceType) type).getElementType() instanceof PrimitiveType){
				return false;
			}else if(((ReferenceType) type).getElementType() instanceof ClassOrInterfaceType){
				ReferenceType refernceType = (ReferenceType) type;
				ClassOrInterfaceType actualType = (ClassOrInterfaceType) refernceType.getElementType(); 
				return isValidUMLClass(actualType.getName().asString()) || isValidUMLClass(getArrayClassName(refernceType));
			}
		}
		return true;
	}
	
	/**
	 * Checks if {@link Type} is {@link UMLClass} and array or collection at the same time.
	 * @param type
	 * @return
	 */
	public static boolean isUMLClassArray(Type type){
		if(type.toString().contains("[") || type.toString().contains("<")){
			return true;
		}
		return false;
	}
	
	/**
	 * Extracts just the name for arrays or collections of {@link UMLClass}
	 * @param type
	 * @return
	 */
	public static String getArrayClassName(Type type){
		if(type.toString().contains("[")){
			return getElementName(type.toString());
		}else if(type.toString().contains("<")){
			try {
			ReferenceType collectionType = (ReferenceType) type;
			ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType) collectionType.getElementType();
			if(classOrInterfaceType.getTypeArguments() != null){
				Optional<NodeList<Type>> args = classOrInterfaceType.getTypeArguments();
				if(args.isPresent()) {
					for(Type arg : args.get()){
						if(isUMLClassType(arg)){
							return arg.toString();
						}
					}
				}
			}
			}catch(Exception e) {return type.toString();}
		}
		return "";
	}
	
	/**
	 * Ignores [] and <> to fetch just the name
	 * @param typeStr
	 * @return
	 */
	public static String getElementName(String typeStr){
		if(typeStr.contains("[")){
			typeStr = typeStr.split("\\[")[0];
		}else if(typeStr.contains("<")){
			typeStr = typeStr.split("<")[0];
		}
		return typeStr;
	}
	
	/**
	 * We just need dependency for interfaces, this checks if one of the relatives is interface or not
	 * @param relationship
	 * @return
	 */
	public static boolean isInterfaceDependency(Relationship relationship){
		UMLClass child = relationship.getChild();
		UMLClass parent = relationship.getParent();
		if((child.isInterface() && !parent.isInterface()) || (!child.isInterface()) && parent.isInterface()){
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if the class name is not one of the Java core classes
	 * @param typeStr
	 * @return
	 */
	private static boolean isValidUMLClass(String typeStr){
		if(typeStr.equalsIgnoreCase("") || typeStr.equalsIgnoreCase("Double") || typeStr.equalsIgnoreCase("Float") || 
				typeStr.equalsIgnoreCase("Long") || typeStr.equalsIgnoreCase("Integer") || 
				typeStr.equalsIgnoreCase("Short") || typeStr.equalsIgnoreCase("Character") ||
		        typeStr.equalsIgnoreCase("Byte") || typeStr.equalsIgnoreCase("Boolean") || 
		        typeStr.equalsIgnoreCase("String") || typeStr.equalsIgnoreCase("ArrayList") ||
		        typeStr.equalsIgnoreCase("Collection") || typeStr.equalsIgnoreCase("HashSet")){
			return false;
		}
		return true;
	}
}
