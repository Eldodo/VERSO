package coocl.ocl;

import java.util.ArrayList;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.ocl.utilities.TypedElement;

import oclruler.metamodel.StructuralFeature;

public class Mutation {
	
	public static int MUTATION_CHANGE_COST_RENAME = 1;
	public static int MUTATION_CHANGE_COST_COLLAPSE = 2;
	public static int MUTATION_CHANGE_COST_CONTEXT_CHANGE = 2;
	public static int MUTATION_CHANGE_COST_ENUMUT = 1;
	public static int MUTATION_CHANGE_COST_MOVE = 1;
	
	
	
	enum MutationType {
		RENAME(MUTATION_CHANGE_COST_RENAME), 
		COLLAPSE(MUTATION_CHANGE_COST_COLLAPSE), 
		CONTEXT_CHANGE(MUTATION_CHANGE_COST_CONTEXT_CHANGE),
		ENUMUT(MUTATION_CHANGE_COST_ENUMUT),
		MOVE(MUTATION_CHANGE_COST_MOVE);
		
		int changeCost = 1;
		private MutationType(int changeCost) {
			this.changeCost = changeCost;
		}
		
		public int changeCost() {
			return changeCost;
		}
	}


	protected MutationType type;
	protected EObject affected;
	protected ArrayList<EObject> parameters;
	
	private Mutation(MutationType type) {
		this.type = type;
		this.parameters 	= new ArrayList<>();
	}
	
	public Mutation(MutationType type, EObject before, EObject... parameters) {
		this(type);
		this.affected = before;
		if(parameters != null)
			for (EObject eObject : parameters) 
				this.parameters.add(eObject);
			
	}

	@Override
	public String toString() {
		return prettyPrint();
	}
	public String prettyPrint() {
		
		String res = "";
		switch (type) {
		case COLLAPSE:
			res = type.name()+":"+affected;
			break;
		case RENAME:
			EStructuralFeature esf = (EStructuralFeature)parameters.get(0);
			EStructuralFeature esf1 = (EStructuralFeature)parameters.get(1);
			res = type.name()+":"+affected+":"+esf1.getName()+">"+esf.getName();
			break;
		case CONTEXT_CHANGE:
			res = type.name()+":"+affected+":"+parameters;
			break;
		case ENUMUT:
			res = type.name();
			break;
		case MOVE:
			res = type.name();
			break;
		default:
			break;
		}
		
		return "("+res+")";
	}
	
	@Override
	protected Mutation clone() throws CloneNotSupportedException {
		Mutation clone = new Mutation(type);
		clone.affected = affected;
		for (EObject eObject : parameters) {
			clone.parameters.add(eObject);
		}
		return clone;
	}
}
