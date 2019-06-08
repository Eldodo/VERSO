package verso.representation.cubeLandscape.filter;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;

import verso.model.Concept;
import verso.representation.IPickable;
import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;
import verso.representation.cubeLandscape.representationModel.SystemRepresentation;
import verso.representation.cubeLandscape.representationModel.repvisitor.IRFilterVisitor;
import verso.saving.csv.IRConceptParser;

/**
 * 
 * @author Maxime Gallais-Jimenez
 * 
 * Filtre pour mettre en valeur les classes et méthodes 
 * concernées par les concepts choisis parmi les IR filters disponibles
 *
 */
public class IRFilter extends EntityFilter {

	HashMap<String, HashSet<Concept>> filteredEntities = new HashMap<String, HashSet<Concept>>();
	HashSet<Concept> concepts = new HashSet<Concept>();
	
	public IRFilter(SystemRepresentation sys) {
		super(sys);
		if(sys == null)
			System.err.println("Le IRFilter a été initialisé avec un SystemRepresentation null attention...");
	}
	
	/**
	 * @Override
	 * Si un élément possede un des concepts sélectionnés il est sélectionné
	 * Sinon il est retiré des éléments sélectionnés
	 */
	public void setElement(IPickable er) {
		System.out.println("Je suis là");
		if (er instanceof ElementRepresentation) {
			ElementRepresentation e = (ElementRepresentation) er;
			this.filterable = true;
			for(Concept concept : e.getElementModel().getConcepts()) {
				for(Concept c : concepts) { /*concepts.contains(concept) ne fonctionne malgré la surcharge de Concept.equals*/
					if(c.equals(concept)) {
						HashSet<Concept> entityConcepts = filteredEntities.get(e.getElementModel().getName());
						if(entityConcepts != null)
							entityConcepts.add(concept);
						else {
							entityConcepts = new HashSet<Concept>();
							entityConcepts.add(concept);
							filteredEntities.put(e.getElementModel().getName(), entityConcepts);
						}
	//					e.setColor(new Color(0.9f, 0.2f, 0.2f));//setter la couleur et la taille en fonction de l'intensité du concept
						e.select();
						this.filter();
						return;
					}
				}
			}
		} else {
			this.filterable = false;
		}
		sys.selectedElements.remove(er);
	}
	
	@Override
	public void filter() {
		if(filterable) {
			SystemRepresentation.filterState = true;
			for(PackageRepresentation p : sys.getPackages()) {
				p.accept(new IRFilterVisitor(filteredEntities.keySet()));
			}
		}
	}
	
	@Override
	public void unfilter() {
		super.unfilter();
		filteredEntities.clear();
	}

	private boolean addConcept(Concept concept) {
		return concepts.add(concept);
	}
	
	public boolean addConcept(String fileName) {
		return addConcept(IRConceptParser.getConceptFromFileName(fileName));
	}
	
	private boolean removeConcept(Concept concept) {
		boolean remove = concepts.remove(concept);
		for(String entity : new HashSet<String>(filteredEntities.keySet())) {
			HashSet<Concept> entityConcepts = filteredEntities.get(entity);
			if(entityConcepts.size() == 1 && entityConcepts.contains(concept))
				filteredEntities.remove(entity);
		}
		return remove;
	}
	
	public boolean removeConcept(String fileName) {
		return removeConcept(IRConceptParser.getConceptFromFileName(fileName));
	}
	
	public void removeAllConcepts() {
		resetConcepts();
		unfilter();
	}
	
	public void resetConcepts() {
		concepts.clear();
	}
	
	public HashSet<String> getConceptsWords() {
		HashSet<String> keywords = new HashSet<String>();
		for(Concept concept : concepts) {
			keywords.addAll(concept.getConceptWords());
		}
		return keywords;
	}
}
