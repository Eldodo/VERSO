package oclruler.metamodel;

import java.util.HashSet;

import org.eclipse.emf.ecore.EClassifier;

public class Enum extends DataType {
	HashSet<EnumLit> lits = new HashSet<>();
	
	public Enum(Metamodel metamodel, String name, EClassifier ec) {
		super(metamodel, name, ec);
	}
	
	public void addLit(EnumLit lit){
		lits.add(lit);
	}
	
	public HashSet<EnumLit> values(){
		return lits;
	}
	
	public HashSet<String> literalValues(){
		HashSet<String> res = new HashSet<>(lits.size());
		for (EnumLit el : lits) 
			res.add(el.getName());
		return res;
	}
	
	@Override
	public boolean isEnum(){
		return true;
	}

}
