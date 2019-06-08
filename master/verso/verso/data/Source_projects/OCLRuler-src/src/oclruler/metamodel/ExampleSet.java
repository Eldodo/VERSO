package oclruler.metamodel;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import oclruler.genetics.Oracle;
import oclruler.genetics.OraculizationException;
import oclruler.utils.Config;
import oclruler.utils.ToolBox;
import partitioner.partition.composition.FragmentSet;

/**
 * 
 * @author Edouard Batot - batotedo@iro.umontreal.ca
 *
 */
public class ExampleSet  {
	public final static Logger LOGGER = Logger.getLogger(ExampleSet.class.getName());
	public static int NEGATIVES_CONSIDERED = Integer.MAX_VALUE;
	public static int POSITIVES_CONSIDERED = Integer.MAX_VALUE;

	
	
	static int number_of_modelsset = 0;

	
	public static void loadConfig() {
		if(!Config.EXAMPLE_EDITION_MODE){
			try {
				ExampleSet.NEGATIVES_CONSIDERED = Config.getIntParam("NEGATIVES_CONSIDERED");
			} catch (Exception e) {
				LOGGER.warning("NEGATIVES_CONSIDERED = '"+Config.getStringParam("NEGATIVES_CONSIDERED")+"' (NaN) : All negatives considered. (check syntax)");
				ExampleSet.NEGATIVES_CONSIDERED = Integer.MAX_VALUE;
			}
		
			try {
				ExampleSet.POSITIVES_CONSIDERED = Config.getIntParam("POSITIVES_CONSIDERED");
			} catch (Exception e) {
				LOGGER.warning("POSITIVES_CONSIDERED = '"+Config.getStringParam("POSITIVES_CONSIDERED")+"' (NaN) : All positives considered. (check syntax)");
				ExampleSet.POSITIVES_CONSIDERED = Integer.MAX_VALUE;
			}
		}
		getInstance(true);
	}
	
	
	int number;
	boolean oraculized = false;
	File directory;
	
	private ArrayList<Model> allExamplesList;
	private ArrayList<Model> examplesBeingUsedList;
	private ArrayList<Model> positives;
	private ArrayList<Model> negatives;
	private ArrayList<Model> allPositives;
	private ArrayList<Model> allNegatives;
	
	
	
	static ExampleSet instance;
	public static ExampleSet getInstance(boolean forceNew){
		if(forceNew || instance == null){
			instance = new ExampleSet(Config.getInstancesDirectory());
		}
		return instance;
	}
	
	public static ExampleSet getInstance(){
		return getInstance(false);
	}
	public static ExampleSet newInstance() {
		instance = new ExampleSet(Config.getInstancesDirectory());
		return instance;
	}
	public ExampleSet() {
		super();
		number = number_of_modelsset++;
		allExamplesList = new ArrayList<>();
	}
	
	public ExampleSet(File directory){
		this();
		this.directory = directory;
		loadExamplesFromDirectory(directory);
	}
	
	public ArrayList<Model> loadExamplesFromDirectory(File directory) {
		Model m = null;
		if(directory == null || !directory.exists() || !directory.isDirectory()){
			throw new IllegalArgumentException("Example set: invalid directory: '"+directory.getAbsolutePath()+"'");
		}
		
		
		File[] subDir = directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() && (
						pathname.getName().equals("positives") || 
						pathname.getName().equals("negatives"));
			}
		});
		
		if(subDir.length == 2){
			LOGGER.config("Two folders : positives and negatives separated.");
			ArrayList<Model> res = loadExamplesFromDirectory(subDir[0]);
			res.addAll(loadExamplesFromDirectory(subDir[1]));
			return res;
		}
		
		
		File[] models = ToolBox.listXMIFiles(directory).clone();
		ArrayList<Model> res = new ArrayList<>(models.length);
		Arrays.sort(models, new Comparator<File>(){
			@Override
			public int compare(File f1, File f2) {
				return f1.getAbsolutePath().compareTo(f2.getAbsolutePath());
			}
		});
		
		for (File f : models) {
			if(f.isDirectory()) continue;
			try {
				m = loadModel(f.getAbsolutePath());
			} catch (InvalidModelException e) {
				LOGGER.warning("Invalid model : '"+f.getAbsolutePath()+"'\n  - "+e.getMessage()+"");
//				e.printStackTrace();
			}
			if(m != null){
				res.add(m);
				addModel(m);
			}
		}
		return res;
	}
	
	/**
	 * 
	 * @param directory or <null> to use defaut instances directory
	 * @param fileNames
	 */
	public ExampleSet(File directory, Collection<String> fileNames){
		this();
		Model m = null;
		for (String r : fileNames) {
			if(directory!=null)
				r = directory.getAbsolutePath()+File.separator + r;
			else 
				r = Config.getInstancesDirectory().getAbsolutePath() + File.separator + r;
			try {
				m = loadModel(r);
			} catch (InvalidModelException e) {
				LOGGER.warning("2Invalid model : '"+r+"'\n  - "+e.getMessage()+"");
//				e.printStackTrace();
			}
			if(m != null){
				addModel(m);
			}
		}
	}
	
	public ExampleSet(Collection<String> fileNames){
		this(null, fileNames);
	}
	
	public static Model loadNewRandomXMIModel()  {
		Model m = null;
		do {
			String n = ToolBox.getRandom(Config.getInstancesDirectory().listFiles()).getAbsolutePath();
			try {
				m = loadModel(n);
//				System.out.println("["+ idx + "]"+ " "+id+":"+m.getNbClasses());
			} catch (InvalidModelException e) {
				LOGGER.warning("Invalid model : '"+e.getMessage()+"'");
				e.printStackTrace();
			}
		} while (m == null);
		return m;
	}
	
	/**
	 * 
	 * @param filename The path from which to load the model (should be absolute).
	 * @return
	 */
	public static Model loadModel(String filename)  throws InvalidModelException {
		Model res = null;
		if(filename.endsWith("xmi") || filename.endsWith("XMI")){
			res = loadXMIModel(filename);
		} else
			LOGGER.severe("File format not recognized : "+filename+" XMI expected.");
		return res;
	}

	
	private static Model loadXMIModel(String filename) throws InvalidModelException {
			Model m = new Model(new File(filename));
			if(LOGGER.isLoggable(Level.FINER))
				LOGGER.finer("Model extracted : '" + m.getFile().getAbsolutePath()+"'");
			return m;
	}
	
	
	public boolean isOraculized() {
		return oraculized;
	}
	
	public static int getNbModels() {
		return getNbModelsInBase();
	}
	
	public File getDirectory() {
		return directory;
	}
	
	public void setOraculized() {
		oraculized = true;
	}
	
	public String getName(){
		return "MS"+number;
	}
	
	public int size() {
		return getExamplesBeingUsed().size();
	}
	
	public int sizeAll() {
		return allExamplesList.size();
	}
	
	
	public Model injectPositiveExample(){
		if(!injectionPositivePossible())
			return null;
		ExampleSet.POSITIVES_CONSIDERED++;
		return getPositives().get(getPositives().size()-1);
	}
	
	public Model injectNegativeExample(){
		if(!injectionNegativePossible())
			return null;
		ExampleSet.NEGATIVES_CONSIDERED++;
		return getNegatives().get(getNegatives().size()-1);
	}
	
	public boolean injectionPossible() {
		boolean b1 = injectionPositivePossible();
		boolean b2 = injectionNegativePossible();
		return b1 || b2;
	}
	
	public boolean injectionPositivePossible() {
		return getPositives().size() < getAllPositives().size();
	}

	public boolean injectionNegativePossible() {
		return getNegatives().size() < getAllNegatives().size();
	}

	public synchronized static ArrayList<Model> getExamplesBeingUsed() {
		if(instance.examplesBeingUsedList == null || instance.examplesBeingUsedList.size() != (POSITIVES_CONSIDERED+NEGATIVES_CONSIDERED)){
			instance.examplesBeingUsedList = new ArrayList<>(POSITIVES_CONSIDERED+NEGATIVES_CONSIDERED);
			instance.examplesBeingUsedList.addAll(instance.getPositives());
			instance.examplesBeingUsedList.addAll(instance.getNegatives());
		}
		return instance.examplesBeingUsedList;
	}
	
	public ArrayList<Model> getAllExamples() {
		return allExamplesList;
	}
	
	private void sortExampleList() {
		sortModelListByModelValidity(allExamplesList);
	}
	
	public static void sortModelListByModelValidity(ArrayList<Model> models){
		models.sort(new Comparator<Model>(){
			@Override
			public int compare(Model m1, Model m2) {
				if(m1.isValid() && !m2.isValid())
					return 1000;
				else if(!m1.isValid() && m2.isValid())
					return -1000;
				return m1.getFileName().compareTo(m2.getFileName());
			}
		});
	}
	public static void sortModelListByModelName(ArrayList<Model> models){
		models.sort(new Comparator<Model>(){
			@Override
			public int compare(Model m1, Model m2) {
				return m1.getName().compareTo(m2.getName());
			}
		});
	}
	
	/**
	 * NOT WORKING right
	 * @param models
	 */
	public static void sortModelListByModelValidity(Model[] models){
		Arrays.sort(models, new Comparator<Model>(){
			@Override
			public int compare(Model m1, Model m2) {
				if(m1 == null || m2 == null)
					return 0;
				if(m1.isValid() && !m2.isValid())
					return 1000;
				else if(!m1.isValid() && m2.isValid())
					return -1000;
				
				return m1.getFileName().compareTo(m2.getFileName());
			}
		});
	}
	
	public void setModels(ArrayList<Model> models) {
		this.allExamplesList = new ArrayList<Model>();
		for (Model model : models) {
			addModel(model);
		}
		sortExampleList();
		examplesBeingUsedList = null;
		negatives = null;
		positives = null;
	}
	
	public boolean addModel(Model m){
		if(!allExamplesList.contains(m)){
			negatives = null;
			positives = null;
			examplesBeingUsedList = null;
			boolean res = allExamplesList.add(m);
			sortExampleList();
			return res;
		} else{
			return false;
		}
	}
	
	public Model getExample(String fileName) {
		for (Model m : getAllExamples()) {
			if(m.getFileName().equals(fileName))
				return m;
		}
		return null;
	}
	
	
	/**
	 * Use before oraculization --> PROBLEM !!!
	 * @return
	 */
	public ArrayList<Model> getPositives(){
		if(positives == null || POSITIVES_CONSIDERED != positives.size()){
			positives = new ArrayList<>();
			for (Model m : getAllExamples()) {
				if(m.isValid() && positives.size() < POSITIVES_CONSIDERED)
					positives.add(m);
			}
		}
		return positives;
	}
	public ArrayList<Model> getAllPositives(){
		if(allPositives == null){
			allPositives = new ArrayList<>();
			for (Model m : allExamplesList) 
				if(m.isValid())
					allPositives.add(m);
		}
		return allPositives;
	}
	/**
	 * Return a sub list with the <howMany> firsts elements of the list of positives (modulo the list's size).
	 * @param howMany
	 * @return
	 */
	public ArrayList<Model> getPositives(int howMany){
		if(howMany >= getPositives().size()) howMany = getPositives().size();
		return new ArrayList<Model>(getPositives().subList(0, howMany));
	}

	/**
	 * Use before oraculization --> PROBLEM !!!
	 * @return
	 */
	public ArrayList<Model> getNegatives(){
		if(negatives == null || NEGATIVES_CONSIDERED != positives.size()){
			negatives = new ArrayList<>();
			for (Model m : getAllExamples()) {
				if(!m.isValid() && negatives.size() < NEGATIVES_CONSIDERED)
					negatives.add(m);
			}
		}
		return negatives;
	}
	public ArrayList<Model> getAllNegatives(){
		if(allNegatives == null){
			allNegatives = new ArrayList<>();
			for (Model m : allExamplesList) 
				if(!m.isValid())
					allNegatives.add(m);
		}
		return allNegatives;
	}
	/**
	 * Return a sub list with the <howMany> firsts elements of the list of negatives (modulo the list's size).
	 * @param howMany
	 * @return
	 */
	public ArrayList<Model> getNegatives(int howMany){
		if(howMany >= getNegatives().size()) howMany = getNegatives().size();
		return new ArrayList<Model>(getNegatives().subList(0, howMany));
	}
	

	@Override
	public String toString() {
		return "("+getName()+":"+allExamplesList.size()+" Model"+(allExamplesList.size()>1?"s":"")+")";
	}
	
	public String prettyPrint() {
		String res = this + "";
		for (Model model : allExamplesList) {
			res += "\n   - "+model.prettyPrint();
		}
		return res;
	}


	public ExampleSet clone() {
		ExampleSet ms = new ExampleSet();
		Model[] mds = new Model[allExamplesList.size()];
		allExamplesList.toArray(mds);
		for (Model model : mds) {
			ms.addModel(model.clone());
		}
		return ms;
	}
	
	
	public static int getNbModelsInBase(){
		if(Config.getInstancesDirectory() == null || Config.getInstancesDirectory().listFiles() == null)
			return 0;
		return ToolBox.listXMIFiles(Config.getInstancesDirectory()).length;
	}


	public String printOracleDecisions() {
		if(!oraculized)
			return "  ** Set has not been oraculized yet ! **";
		String res = "";
		int maxNameSize = 0;
		for (Model m : allExamplesList) 
			if(m.getFileName().length()>maxNameSize) maxNameSize = m.getFileName().length();
		
		for (Model m : allExamplesList) 
			res +=  ""+ToolBox.completeString(m.getFileName(), maxNameSize)+"  "+(m.isValid()?">VALID< ":" >NOT<  ")+" ("+m.getFireCount()+" fire"+((m.getFireCount()>1)?"s":"")+")\n";
		
		return res;
	}
	
	/**
	 * Browses example-input models, {@link Model#reload() reload} them from their XMI files, and process new {@link Oracle#oraculize(oclruler.genetics.Evaluator) oraculization}.<br/>
	 * This process may implie instability (if models are not valid anymore).
	 */
	public void reloadExamples() {
		for (Model model : getAllExamples()) {
			try {
				model.reload();
			} catch (InvalidModelException e) {
				System.out.println("Model '"+model.getFileName()+"' invalid. Execution might be instable.");
				e.printStackTrace();
			}
		}
		
		instance.examplesBeingUsedList = null;
		if(instance.oraculized)
			try {
				Oracle.getInstance().oraculize();
			} catch (OraculizationException e) {
//				e.printStackTrace();
			}
		getExamplesBeingUsed();
		
	}
	
	public void copyExamplesTo(File res) {
		for (Model m : getAllExamples()) {
			java.nio.file.Path pTarget = Paths.get(res.getAbsolutePath(), m.getFileName());
			java.nio.file.Path pSource = Paths.get(m.getFile().getAbsolutePath());
			try {
				Files.copy(pSource, pTarget, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void nullify() {
		instance = null;
		
	}

	public static void unleashConsideredExamples() {
		POSITIVES_CONSIDERED = Integer.MAX_VALUE;
		NEGATIVES_CONSIDERED = Integer.MAX_VALUE;
	}

	public double[] getCoverage() {
		if(coverage[0] < 0){
			FragmentSet fs = Metamodel.getFragmentSet();
			coverage = new double[]{
					fs.evaluateCoverage(getAllExamples()),
					fs.evaluateCoverage(getAllPositives()),
					fs.evaluateCoverage(getAllNegatives())
				};
		}
		return coverage;
	}
	double coverage[] = {-1.0f, -1.0f, -1.0f};


	public int refinedSize() {
		if(refinedSize < 0){
			refinedSize = 0;
			for (Model model : allExamplesList) {
				refinedSize += model.getNbClasses();
			}
		}
		return refinedSize;
	}
	int refinedSize = -1;
}
