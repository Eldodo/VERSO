package genetic.fitness;

import java.util.HashMap;

import utils.Config;

public class FitnessObjectiveFactory {

	public static final String FO_TYPE_MAX = "MAX";
	public static final String FO_TYPE_MIN = "MAX";
	public static final String FO_TYPE_BRACKET = "[";

	FitnessObjectiveFactory instance;
	
	
	static HashMap<String, Double> mins = new HashMap<>();
	static HashMap<String, Double> maxs = new HashMap<>();
	
	static FitnessObjective[] objs;
	
	static FitnessObjective[] buildObjectives(String[] objectives) {
		objs = new FitnessObjective[objectives.length];
		//FitnessVector.setNumberOfObjectives(objectives.length);
//		System.out.println("FitnessObjectiveFactory.buildObjectives()");
		for (int i = 0; i < objectives.length; i++) {
			
			String name = objectives[i].substring(0, objectives[i].indexOf(","));
			String value = objectives[i].substring(name.length()+1);
			FitnessVector.setObjectiveName(i, name);
			if(value.trim().startsWith(FO_TYPE_MAX)){
				objs[i] = new FitnessObjectiveMax(name);
				mins.put(name, 1.0);
				maxs.put(name, 1.1);

				FitnessVector.setObjectiveType(i, FO_TYPE_MAX);
			} else if(value.trim().startsWith(FO_TYPE_BRACKET)){
				
				double min = Double.parseDouble(value.substring(1, value.indexOf("-")));
				double max = Double.parseDouble(value.substring(value.indexOf("-")+1, value.length()-FO_TYPE_BRACKET.length()));

				mins.put(name, min);
				maxs.put(name, max);
				
				objs[i] = new FitnessObjectiveBracket(name);
				FitnessVector.setObjectiveType(i, FO_TYPE_BRACKET);
				FitnessVector.setObjectiveBrackets(i, min, max);
			}
//			System.out.println( " - "+name+ ": "+value);
		}
		return objs;
	}

	public static void loadConfig() {
		int nbObj = Config.NUMBER_OF_OBJECTIVES;
		String objsS[] = new String[nbObj];
		for (int i = 0; i < nbObj; i++) {
			objsS[i] = Config.getStringParam("OBJ_"+i);
			
		}
		buildObjectives(objsS);
	}

	public static double getMin(String name) {
		return mins.get(name);
	}
	public static double getMax(String name) {
		return maxs.get(name);
	}

	public static FitnessObjective[] getNewVector() {
		FitnessObjective[] res = new FitnessObjective[Config.NUMBER_OF_OBJECTIVES];
		for (int i = 0; i < res.length; i++) {
//			Class.forName(objs[i].getClass().getName()).newInstance();
			try {
				res[FitnessVector.COVERAGE] = (FitnessObjective)Class.forName(objs[i].getClass().getName()).newInstance();
				res[FitnessVector.DISSIMILARITY] = (FitnessObjective)Class.forName(objs[i].getClass().getName()).newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		res[FitnessVector.COVERAGE] = new FitnessObjectiveBracket("COV");
//		res[FitnessVector.DISSIMILARITY] = new FitnessObjectiveMax("DIS");
		return res;
	}
	
}
