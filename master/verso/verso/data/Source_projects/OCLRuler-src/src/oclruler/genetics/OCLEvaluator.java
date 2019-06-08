package oclruler.genetics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.ocl.xtext.completeocl.CompleteOCLStandaloneSetup;
/**
 * 
 * @author Thomas Franz, BSc & Sabine Wolny, BSc
 *
 */
public class OCLEvaluator {

	private Resource ecoreResource;
	private String packageName;

	private URI mmURI;
	private URI modelURI;
	private static boolean valid;

	public OCLEvaluator(){

	} 

	/**
	 * 
	 * @param mmPath Path to the meta model
	 * @param modelPath Path to the according model
	 * @param oclPath Path to the ocl file with the constraints
	 * @param saveToFile Path to file where prepared ocl-constraints shall be saved
	 * @return Diagnostic object
	 */
	public Diagnostic evaluate(String mmPath, String modelPath, String oclPath, String saveToFile){

		File save = new File(saveToFile);

		//Initialize OCL
		CompleteOCLStandaloneSetup.doSetup();
//		OCLstdlibFactory.eINSTANCE.install();

		mmURI = URI.createFileURI(new File(mmPath).getAbsolutePath());
		modelURI = URI.createFileURI(new File(modelPath).getAbsolutePath());

		//register ecore package
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
		ecoreResource = resourceSet.getResource(mmURI, true);

		EPackage ePackage = ecoreResource.getContents().get(0).eClass().getEPackage();

		List<EPackage> pList = getPackages(ecoreResource);
		EPackage p = pList.get(0);

		resourceSet.getPackageRegistry().put(p.getNsURI(), p);

		Resource model = resourceSet.getResource(modelURI, true);
		ePackage = model.getContents().get(0).eClass().getEPackage();

		//Create prepared ocl-file
		packageName = ePackage.getName();
		try {
			createOCLFile(oclPath, save);
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

		EPackage.Registry.INSTANCE.put(p.getNsURI(), ePackage);

		//initialize OCL-Validator 
//		CompleteOCLEObjectValidator myValidator = new CompleteOCLEObjectValidator(ePackage, oclURI, Environ );

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
				Resource.Factory.Registry.DEFAULT_EXTENSION,
				new XMIResourceFactoryImpl());

		EObject rootObject = model.getContents().get(0);

//		EValidatCor.Registry.INSTANCE.put(ePackage, myValidator);

		Diagnostic diagnostics = Diagnostician.INSTANCE.validate(rootObject);

		return diagnostics;
	}

	/*
	 * load ecore resource
	 */
	@SuppressWarnings("unused")
	private void loadEcoreResource(URI mmURI){
		ResourceSet resSet = new ResourceSetImpl();
		ecoreResource = resSet.getResource(mmURI, true);
	}

	private List<EPackage> getPackages(Resource r){
		ArrayList<EPackage> pList = new ArrayList<EPackage>();
		if (r.getContents() != null)
			for (EObject obj : r.getContents()) 
				if (obj instanceof EPackage) {
					pList.add((EPackage)obj);
				}
		return pList;
	}

	/**
	 * create temporary constraints file based on the existing one. the first one will be an
	 * extended and changed version of the latter one.
	 * 
	 * @param oclPath Path of the original file with the ocl constraints
	 * @param save
	 * @throws IOException
	 */
	public void createOCLFile(String oclPath, File save) throws IOException{

		String str;
		ArrayList<String> fileContent = new ArrayList<String>();

		/*
		 * add import statement pointing to the location of the meta-model
		 * add name of package
		 */
		fileContent.add("import '" + mmURI.toString() + "'");
		fileContent.add("package oclruler." + packageName);

		int counter = 1;

		/*
		 * add specific lines from original ocl-file i.e. lines that define the context, 
		 * lines that define invariants or lines that contain specific important ocl-commands
		 * as "self" (as far as it is not a substirng of "ifself", "himself" or "herself" 
		 * which would be a hint for a comment) or ".ocl" (the latter defining specific 
		 * ocl-operations)
		 * by default constraints are assumed to be invariants
		 */
		BufferedReader in = new BufferedReader(new FileReader(oclPath));

		while((str = in.readLine()) != null){
			if(str.startsWith("context")){
				fileContent.add(str);
			}
			else if(str.startsWith("inv:")){
				String newStr = str.replace("inv:", "inv Constraint_" + counter + ":");
				counter++;
				fileContent.add(newStr);
			}
			else if((str.contains("self") && (!str.contains("tself") || str.contains("mself") || str.contains("rself"))) || str.contains(".ocl")){
				if(str.contains(":")){
					//it is assumed, that constraint (i.e. invariant) already has a name
					fileContent.add(str);
				}
				else{
					fileContent.add("inv Constraint_" + counter + ": " + str);
					counter++;
				}
			}
		}

		in.close();
		fileContent.add("endpackage");

		BufferedWriter out = new BufferedWriter(new FileWriter(save)); 

		for(String s : fileContent){
			out.write(s + "\n");
		}

		out.close();
	}

	public static void main(String[] args){

		/*
		 * if less then the four required parameters are inserted, the program prints out an
		 * error message with information about the required parameters
		 */
		if(args.length < 4){
			System.out.println("Missing Parameters! Insert them in the following order:");
			System.out.println("1. Path to Metamodel");
			System.out.println("2. Path to Model");
			System.out.println("3. Path to File with OCL-Constraints");
			System.out.println("4. Path where new, prepared File with OCL-Constraints shall be saved");
		}
		else{
			String mmPath = args[0];
			String modelPath = args[1];
			String oclPath = args[2];

			OCLEvaluator evaluator = new OCLEvaluator();

			//define file where prepared ocl-constraints shall be saved
			String saveToFile = args[3];

			Diagnostic diagnostics = evaluator.evaluate(mmPath, modelPath, oclPath, saveToFile);

			valid = true;

			if(!diagnostics.getChildren().isEmpty()){
				valid = false;
			}

			for(Object object : diagnostics.getChildren()){
				System.out.println(object.toString());
			}

			if(valid){
				System.out.println("All constraints for model are valid!");
			}
			else{
				System.out.println("ERROR: Constraints are invalid!");
			}
		}
	}
	public static boolean getValid(){
		return valid;
	}
}
