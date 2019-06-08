package verso.model;

import java.util.HashSet;

/**
 * 
 * Classe pour représenter un concept extrait grÃ¢ce Ã  l'information retrieval
 * Ces concepts seront utilisés dans les IR filters 
 * 
 * @author Maxime Gallais-Jimenez
 *
 */

public class Concept {

	private HashSet<String> conceptWords;
	private int intensity;
	
	public Concept() {
		this(new HashSet<String>());
	}
	
	public Concept(HashSet<String> words) {
		this(words, 1);
	}
	
	public Concept(HashSet<String> words, int intensity) {
		conceptWords = words;
		this.intensity = intensity;
	}
	
	public Concept cloneWords() {
		return new Concept(conceptWords);
	}
	
	public HashSet<String> getConceptWords() {
		return conceptWords;
	}
	
	public int getIntensity() {
		return intensity;
	}
	
	public boolean addConceptWord(String word) {
		return conceptWords.add(word);
	}
	
	public boolean setIntensity(int intensity) {
		if(intensity >= 0) {
			this.intensity = intensity;
			return true;
		}
		return false;
	}
	
	public boolean equals(Object o) {
		Concept c = (Concept) o;
		if(conceptWords.size() != c.conceptWords.size())
			return false;
		for(String word : conceptWords) {
			if(!c.conceptWords.contains(word))
				return false;
		}
		return true;
	}
	
	public String toString() {
		String concept = conceptWords.toString()+"\n";
		concept += "Intensity: "+intensity;
		return concept;
	}
}
