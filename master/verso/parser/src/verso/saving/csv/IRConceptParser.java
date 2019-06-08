package verso.saving.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import verso.util.Config;
import verso.model.Concept;
import verso.model.Element;

/**
 * 
 * Classe pour parser les fichiers présents dans le IR folder 
 * Ainsi on peut ajouter aux classes des concepts utiles pour le fitrage 
 * grÃ¢ce aux IR filters.
 * 
 * 
 * IR = Information retrieval
 * 
 * Le fichier parsé se compose d'une entÃªte commenÃ§ant par le mot concept 
 * et suivi des termes qui compose ce concept
 * concept:c1;c2;c3
 * Ensuite il y a le nom des classes puis celui des méthodes et enfin le nombre de dépendances.
 * Ces fichiers sont censés se trouver dans le verso/irfolder
 * 
 * @author Maxime Gallais-Jimenez
 *
 */

public class IRConceptParser {
	
	private static final String conceptHeader = "concept:";

	public static void parse(HashMap<String,Element> classes) {
		File[] files = Config.irFolder.listFiles();
		if(files == null) {
			System.err.println("No concepts find in IR folder : "+Config.irFolder.getAbsolutePath());
			return;
		}
		for(File f : files) {
			if(f.exists() && !f.isDirectory()) {
				String line, className/*, methodName*/;
				String[] split;
				Element e;
				
				try {
					BufferedReader in = new BufferedReader(new FileReader(f));
					Concept conceptBase = new Concept();
					readHeader(in.readLine(), conceptBase);
					while((line = in.readLine()) != null) {
						Concept concept = conceptBase.cloneWords();
						split = line.split(";");
						className = split[0];
//						methodName = split[1];
						concept.setIntensity(Integer.parseInt(split[2]));
						e = classes.get(className); //Attention s'il manque le nom du projet dans le nom de la classe cela match pas
						if(e != null) e.addConcept(concept);
					}
					in.close();
				} catch(IOException ioe) {
					ioe.printStackTrace();
					System.err.println(ioe);	
				}
			}
		}
	}
	
	public static Concept getConceptFromFileName(String fileName) {
		Concept concept = new Concept();
		File file = new File(Config.irFolder.getAbsolutePath()+File.separator+fileName+".txt");
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			readHeader(in.readLine(), concept);
			in.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
			System.err.println(ioe);	
		}
		
		return concept;
	}
	
	private static void readHeader(String line, Concept concept) {
		String[] split = line.substring(conceptHeader.length()).split(";");
		for(String word : split)
			concept.addConceptWord(word);
	}
}
