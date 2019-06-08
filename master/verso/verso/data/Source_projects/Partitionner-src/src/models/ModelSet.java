package models;

import genetic.Entity;
import genetic.Evaluator;
import genetic.Evolutioner;
import genetic.Gene;
import genetic.fitness.FitnessVector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections4.KeyValue;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import utils.Config;
import utils.Utils;

/*
 * Pick randomly from a repository.
 * 
 */
public class ModelSet extends Entity {
	
	private static final int MINIMUM_MODELS_IN_MS = 1;
	
	public  static int MIN_SIZE_MODEL = 0;
	public  static int MAX_SIZE_MODEL = Integer.MAX_VALUE;
	
	static Resource[] resources;

	
	private ArrayList<Model> models;
	static int mscount = 0;
	int number;
	static File instancesDirectory = new File(Config.DIR_INSTANCES+Config.METAMODEL_NAME);
	
	public static int getNbModels() {
		return getNbModelsInBase();
	}
	public static File getInstancesDirectory() {
		return instancesDirectory;
	}
	
	public ModelSet(Collection<String> resources){
		this();
		Model m;
		for (String r : resources) {
			m = loadModel(r, Utils.resourceSet);
			if(m != null){
				addModel(m);
			}
		}
	}
	public ModelSet(File directory){
		this();
		Model m;
		File[] models = directory.listFiles().clone();
		Arrays.sort(models, new Comparator<File>(){
			@Override
			public int compare(File f1, File f2) {
				return f1.getAbsolutePath().compareTo(f2.getAbsolutePath());
			}
			
		});
		for (File f : models) {
			m = loadModel(f.getAbsolutePath(), Utils.resourceSet);
			if(m != null){
				addModel(m);
			}
		}
	}

	
	public ModelSet() {
		super();
		number = mscount++;
		models = new ArrayList<>();
	}
	
	public static void loadConfig(){
		try{
			buildInstanceRepositoryIndex();//Builds table modelBase[][]
		} catch(Exception e){
			Model.LOGGER.warning("Model repository index incorect.");
			e.printStackTrace();
		} finally {
			if(!modelBaseIndexReady)
				return;
		}
		
		
		try {
			MIN_SIZE_MODEL = 	Config.getIntParam("MIN_SIZE_MODEL");
		} catch (Exception e1) {
			MIN_SIZE_MODEL = 0;
		}
		
		try {
			MAX_SIZE_MODEL = 	Config.getIntParam("MAX_SIZE_MODEL");
		} catch (Exception e) {
			MAX_SIZE_MODEL = Integer.MAX_VALUE;
		}
		
		indexMin = -1;
		for (int i = 0; i < modelBase.length; i++) {
			if(indexMin < 0 && modelBase[i][1] >= MIN_SIZE_MODEL)
				indexMin = i;
			
			if(modelBase[i][1] > MAX_SIZE_MODEL){
				indexMax = i;
				break;
			}
		}
		if(MIN_SIZE_MODEL<modelBase[0][1]){
			MIN_SIZE_MODEL = modelBase[0][1];
			indexMin = 0;
		}
		if(MAX_SIZE_MODEL>modelBase[modelBase.length-1][1]){
			MAX_SIZE_MODEL = modelBase[modelBase.length-1][1];
			indexMax = modelBase.length-1;
		}
		
		//WATCH OUT !!! ORDER DEPENDANT modelBase.length getNbModelInBase uses MIN_/MAX_ sizes. 
		resources = new Resource[modelBase.length];
//		System.out.println("ModelSet.loadConfig()"+resources.length+" : "+new File(Config.DIR_INSTANCES+Config.METAMODEL_NAME).listFiles().length);
	}
	public static Model loadNewRandomModel() {
		return loadNewRandomModel(Utils.resourceSet);
	}
	public static Model loadNewRandomModel(ResourceSet rs) {
		
		Model m = null;
		
		do {
			int id = -1;
			if(!modelBaseIndexReady){
				id = Utils.getRandomInt(getNbModelsInBase() -1);
			}else{
				int idx = Utils.getRandomInt(indexMin, indexMax);
				id = modelBase[idx][0];
			}
			m = loadModel(id, rs);
//			System.out.println("["+ idx + "]"+ " "+id+":"+m.getNbClasses());
			if(m == null)
				Logger.getGlobal().severe("Model '"+id+"' is not valid.");
		} while (m == null);
		return m;
	}
	
	static public boolean saveResources = false;//On true, with large modelBase -> Memory OVERLOAD !!!
	
	/**
	 * Load a model within the main resourceset - ONLY FOR TESTING : Utils.resourceSet get overload otherwise.
	 * @param id:int : id of 'model_00123.xmi' is 123.
	 * @return Model contained in file 'model_00123.xmi'
	 */
	public static Model loadModel(int id){
		return loadModel(id, Utils.resourceSet);
	}
	
	/**
	 * Load a model from its id.
	 * @param id:int : id of 'model_00123.xmi' is 123.
	 * @return Model contained in file 'model_00123.xmi'
	 */
	public static Model loadModel(int id, ResourceSet rs){
		String filename = instancesDirectory.getAbsolutePath()+File.separator+"model_";
		filename += (((id<10)?"0000":(id<100)?"000":(id<1000)?"00":(id<10000)?"0":"") +id);
		filename += ".xmi";
		try{
			URI fileURIm = URI.createFileURI(filename);
			
			Resource resource = null; 
			Model m = null;
			
			if(id >= resources.length){
				Logger.getLogger(Model.class.getName()).warning("Resource id incorrect: '" + filename +"' (only "+resources.length+" resources found).");
				return null;
			}
			
			if(resources[id] != null){
				resource = resources[id];
//				System.out.println("ModelSet.loadModel("+id+") Got it :"+m+" with "+m.getNbClasses()+" classes.");
			}else {
				resource = rs.createResource(fileURIm);
				if(saveResources)
					resources[id] = resource;
			}
			m = new Model(resource);
			if(Model.LOGGER.isLoggable(Level.FINER))
				Model.LOGGER.finer("Model extracted : '" + m +"'");
			return m;
		} catch (IOException e){
			Model.LOGGER.warning("File couldn't be loaded :" + filename+"'");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 
	 * @param filename The path from which to load the model (should be absolute).
	 * @return
	 */
	public static Model loadModel(String filename, ResourceSet rs){
		try{
			URI fileURIm = URI.createFileURI(filename);
			
			Resource resource =  rs.createResource(fileURIm);
			
			Model m = new Model(resource);
			
			if(Model.LOGGER.isLoggable(Level.FINER))
				Model.LOGGER.finer("Model extracted : '" + resource.getURI().path()+"'");
			return m;
		} catch (Exception e){
			Model.LOGGER.warning("File couldn't be loaded :" + filename+"'");
			//e.printStackTrace();
		}
		return null;
	}

	
	@Override
	public Entity[] crossover(Entity e) {
		ArrayList<Model> a = getModels();
		ArrayList<Model> b = ((ModelSet)e).getModels();
		
		int saveda = a.size()/10;
		int cuta = Utils.getRandomInt(saveda, a.size()-saveda);
		int savedb = b.size()/10;
		int cutb = Utils.getRandomInt(savedb, b.size()-savedb);
		
		
		ArrayList<Model> a_1 = new ArrayList<>(cuta+cutb);
		ArrayList<Model> a_2 = new ArrayList<>(b.size()-cutb + a.size()-cuta);
		ArrayList<Model> b_1 = new ArrayList<>(cutb);
		ArrayList<Model> b_2 = new ArrayList<>(b.size()-cutb);
		
		for (int i = 0; i < cuta; i++) 
			a_1.add(a.get(i));
		for (int i = cuta; i < a.size(); i++) 
			a_2.add(a.get(i));
		
		for (int i = 0; i < cutb; i++) 
			b_1.add(b.get(i));
		for (int i = cutb; i < b.size(); i++) 
			b_2.add(b.get(i));
		
		a_1.addAll(b_1);
		a_2.addAll(b_2);
		
		
		// Combler les trous : modeles aléatoires réinjectés.
		while(a_1.size() < MINIMUM_MODELS_IN_MS){
			a_1.add(loadNewRandomModel(Evolutioner.getRsGeneration()));
			//System.out.println("ModelSet.crossover(X) "+a_1.size());
		}
		
		while(a_2.size() < MINIMUM_MODELS_IN_MS){
			a_2.add(loadNewRandomModel(Evolutioner.getRsGeneration()));
			//System.out.println("ModelSet.crossover(0) "+a_2.size());
		}
		
		
		ModelSet ms1 = new ModelSet();
		ms1.setModels(a_1);
		
		ModelSet ms2 = new ModelSet();
		ms2.setModels(a_2);
		
		return new Entity[]{ ms1, ms2 };
	}

	@Override
	public boolean mutate() {
		//Add new model or Exchange one model for a new one or Remove one model.
		int type = Utils.getRandomInt(2);
		if(models.size() <= MINIMUM_MODELS_IN_MS)
			type = Math.max(type, 1);//Don't make the set empty !
		boolean res = true;
		switch (type) {
			case 0:
				mut_newModel();
				break;
			case 1:
				mut_removeModel();
				mut_newModel();
				break;
			case 2:
				mut_removeModel();
				break;
			default:
				res = false;
				break;
			}
		return res;
	}
	public void mut_newModel(){
		Model m = loadNewRandomModel(Evolutioner.getRsGeneration());
		addModel(m);
	}
	public void mut_removeModel(){
		if(models.size()>1){
			int id = Utils.getRandomInt(models.size());
			models.remove(id);
		}
	}
	
	@Override
	public FitnessVector evaluate(Evaluator ev) {
		return ev.evaluateCoverage(this);
	}


	@Override
	public ArrayList<? extends Gene> getGenes() {
		return models;
	}
	public ArrayList<Model> getModels() {
		return models;
	}
	public void setModels(ArrayList<Model> models) {
		this.models = new ArrayList<Model>();
		for (Model model : models) {
			addModel(model);
		}
	}
	
	public boolean addModel(Model m){
		if(!models.contains(m))
			return models.add(m);
		else
			return false;
	}
	
	@Override
	public String toString() {
		return "("+getName()+":"+models.size()+" Models)";
	}
	
	public String getName(){
		return "MS"+number;
	}
	
	public String prettyPrint() {
		String res = this + "";
		for (Model model : models) {
			res += "\n   - "+model.getResourceFileName();
		}
		return res;
	}

	/**
	 * Print result [time +separator ] COV + separator + DIS + separator + nbModels [endChar]
	 * @param separator
	 * @param endChar 
	 * @param timeStamp
	 * @return
	 */
	@Override
	public String printResult(String separator, String timeStamp, String elapsedTime, boolean formatDecimals){
		String res = "";
		if(fitnessVector == null){
			Model.LOGGER.warning("Printing result : No results yet.");
			return "";
		}
		if(timeStamp != null && !timeStamp.isEmpty())
			res = timeStamp+separator;
		
		if(formatDecimals)
			res += Utils.format2Decimals((float)fitnessVector.getCoverage())+separator+
				   Utils.format2Decimals((float)fitnessVector.getDissimilarity());
		else
			res += fitnessVector.getCoverage()+separator+
				   fitnessVector.getDissimilarity();
		
		res += separator+size();
		res += separator+elapsedTime;
		
		
		int totalClasses = 0, totalProperties = 0;
		ArrayList<Model> ms = new ArrayList<>(getModels());
		for (Model m : ms) {
			totalClasses += m.getNbClasses();
			totalProperties += m.getNbProperties();
		}
		float avgClasses = (float)totalClasses / size();
		float avgProperties = (float)totalProperties / size();
		
		if(formatDecimals)
			res += separator+Utils.format2Decimals(avgClasses)+separator+
				   Utils.format2Decimals(avgProperties);
		else
			res +=  separator+avgClasses+separator+
					avgProperties;
		
		return res;
	}
	public String printResultHeader(String separator) {
		return "TimeStamp;COV;DIS;elapsedTime;nbModels;avgNbObjects;avgNbProperties\n";
	}

	public ModelSet clone() {
		
		ModelSet ms = new ModelSet();
		
		if(fitnessVector != null)
			ms.setFitnessVector(fitnessVector.clone());
		
		
		Model[] mds = new Model[models.size()];
		models.toArray(mds);
		for (Model model : mds) {
			ms.addModel(model.clone());
		}
		ms.setRank(getRank());
		ms.setDistance(getDistance());
		return ms;
	}
	
	public void nullify(){
		models = null;
		super.nullify();
	}
	@Override
	public int sizeRefined() {
		int s = 0;
		for (Model m : models) 
			s+= m.size();
		return s;
	}

	@Override
	public int size() {
		return models.size();
	}
	
	static int indexMin = 0, indexMax =0;
	static int instanceDirectoryLength = -1;
	public static int getNbModelsInBase(){
		if(modelBaseIndexReady)	return indexMax - indexMin;
		else {
			if(instanceDirectoryLength < 0)
				instanceDirectoryLength = instancesDirectory.listFiles().length;
			return instanceDirectoryLength;
		}
	}
	static Integer[][] modelBase;
	static boolean modelBaseIndexReady = false;
	public static void buildInstanceRepositoryIndex() {
		modelBaseIndexReady = false;
		File f = new File(Config.DIR_INSTANCES+Config.METAMODEL_NAME+"_RepositoryStatistics.log");
		if(!f.exists()){
				Logger.getLogger(Model.class.getName()).warning("Model repository index does not exist. Execution might be slow.");
				modelBaseIndexReady = false;
				return;
		}

		modelBase = new Integer[new File(Config.DIR_INSTANCES+Config.METAMODEL_NAME).listFiles().length][2];
		Scanner s;
		try {
			s = new Scanner(f);
			String l = s.next();//HEADER
			int i = 0;
			while(s.hasNext()){
				l = s.next();
				int numModel = Integer.parseInt(l.substring("model_".length(), "model_00000".length()));
				int nbClasse = Integer.parseInt(l.substring("model_00000.xmi;".length(), l.lastIndexOf(";")));
				modelBase[i][0] = numModel;
				modelBase[i][1] = nbClasse;
				i++;
//				System.out.println(i+":"+l+ " : " + numModel + " : "+nbClasse);
			}
			if(i != modelBase.length){
				s.close();
				throw new IllegalStateException("Statistics files incorrect : "+modelBase.length +" files in repository and "+i+" models in statistics.");
			}
//			System.out.println("i:"+i);
			Arrays.sort(modelBase, new Comparator<Integer[]>() {
				public int compare(Integer[] o1, Integer[] o2) {
					return ((Integer[]) o1)[1].compareTo(((Integer[]) o2)[1]);
				}
			});
			modelBaseIndexReady = true;
			
			s.close();
		} catch (FileNotFoundException e) {
			Model.LOGGER.warning("Model repository index could not be loaded : '"+f.getAbsolutePath()+"'");
		} catch (Exception e){
			Model.LOGGER.warning("Model repository index could not be loaded : '"+f.getAbsolutePath()+"'");
			e.printStackTrace();
		} finally {
			
		}
	}

}
