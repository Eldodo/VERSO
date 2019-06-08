package utils;


import java.util.logging.Logger;

public class ModelsRepositoryStatistics {
	public final static Logger LOGGER = Logger.getLogger(ModelsRepositoryStatistics.class.getName());
	public static void main(String[] args) throws Exception{
		LOGGER.info("");


		Utils.init();	
//		for (int i = 0; i < 200; i++) {
//			System.out.println("<items xsi:type=\"TestModel:Item\" name=\""+genChars(Utils.getRandomInt(3, 6))+"\" type=\""+genChars(Utils.getRandomInt(1, 3))+"\" price=\""+Utils.getRandomInt(2,500)+"\">");
//		}

		String res = Utils.buildModelRepositoryStatistics();
		System.out.println(res);
		
		System.out.println("Done !");
	}
	
	static char[] al = "qwertyuiopasdfghjklzxcvbnQWERTYUIOPASDFGHJKLZXCVBNM".toCharArray();
	public static String genChars(int length){
		String res = "";
		for (int i = 0; i < length; i++) {
			res += al[Utils.getRandomInt(al.length)];			
		}
		return res;
	}
	
	
	
	public String[] genItems(int length) {
		String[] res = new String[length];
		for (int i = 0; i < length; i++) {
			res[i] = "<items xsi:type=\"TestModel:Item\" name=\""+genChars(Utils.getRandomInt(3, 6))+"\" type=\""+genChars(Utils.getRandomInt(1, 3))+"\" price=\""+Utils.getRandomInt(2,500)+"\">";
		}
		return res;
	}
	
}
