package oclruler.metamodel;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.emf.ecore.EObject;

import oclruler.rule.struct.Constraint;

/**
 * For a given model, get fired constraints (how many time).
 * All packed up in a Map.
 * @author batotedo
 *
 */
public class FireMap extends HashMap<Model, HashMap<Constraint, Integer>> {
	private static final long serialVersionUID = 1L;
	HashMap<String, HashMap<Model, Integer>> reverseMap;
	private HashMap<KeyValue<Model, Constraint>, ArrayList<EObject>> firedObjects;

	public FireMap() {
		super();
		firedObjects = new HashMap<>();
	}

	public void addFire(Model m, Constraint p, int fires) {
		p.addFires(fires);
		addFire(m, p);
	}
	
	public void addFire(Model m, Constraint p) {
		HashMap<Constraint, Integer> mFires =  get(m);
		if(mFires == null)
			put(m, mFires = new HashMap<Constraint, Integer>());
		
		
		if(mFires.get(p) == null)
			mFires.put(p, 0);
		
		mFires.put(p, mFires.get(p)+p.getFires());		
	}
	
	
	/**
	 * 
	 * @param fm
	 * @return
	 */
	public void merge(FireMap fm){
		for (Model m : fm.keySet()) {
			if(get(m) == null)
				put(m, new HashMap<>());
			get(m).putAll(fm.get(m));
		}
		for (KeyValue<Model, Constraint> kv : fm.getFiredObjects().keySet()) {
			if(firedObjects.get(kv) == null)
				firedObjects.put(kv, new ArrayList<>());
			
			firedObjects.get(kv).addAll(fm.getFiredObjects(kv));
		}
	}
	
	public int getNumberOfFires(Model m){
		int res = 0;
		if(get(m) != null)
			for (Integer i : get(m).values()) 
				res += i;
		return res;
	}
	public int getNumberFires(Model m, Constraint pt){
		if(get(m) != null && get(m).get(pt) != null)
			return get(m).get(pt);
		return 0;
	}
	

	public void addFiredObject(Model m, EObject eo, Constraint ct) {
		KeyValue<Model, Constraint> kv = new KeyValue<>(m, ct);
		if(firedObjects.get(kv) == null)
			firedObjects.put(kv, new ArrayList<>(1));
		firedObjects.get(kv).add(eo);
	}
	
	
	public class KeyValue<K extends NamedEntity, V extends NamedEntity>  {
		K m;
		V ct;
		public KeyValue(K m, V ct) {
			this.m = m;
			this.ct = ct;
		}
		
		@Override
		public boolean equals(Object o) {
			if(o == null || (getClass() != o.getClass()))
				return false;
			return ((KeyValue<?, ?>)o).m.equals(m) && ((KeyValue<?,?>)o).ct.equals(ct);
		}
		
		@Override
		public int hashCode() {
			return m.hashCode()+ct.hashCode();
		}
		
		@Override
		public String toString() {
			return "("+m.getName()+", "+ct.getName()+")";
		}
	}
	
	public HashMap<KeyValue<Model, Constraint>, ArrayList<EObject>> getFiredObjects() {
		return firedObjects;
	}
	public ArrayList<EObject> getFiredObjects(Model m) {
		 ArrayList<EObject> res = new ArrayList<>();
		for (KeyValue<Model, Constraint> kv : firedObjects.keySet()) {
			if(kv.m.equals(m)){
				for (EObject eObject : firedObjects.get(kv)) {
					if(!res.contains(eObject))
						res.add(eObject);
				}
			}
		}
		return res;
	}
	
	public ArrayList<EObject> getFiredObjects(KeyValue<Model, Constraint> kv) {
		return firedObjects.get(kv);
	}
	
	public ArrayList<EObject> getFiredObjects(Model m, Constraint ct) {
		ArrayList<EObject> res = firedObjects.get(new KeyValue<>(m, ct));
		return res != null ? res : new ArrayList<EObject>(0);
	}
	
	
	@Override
	public String toString() {
		String log = "[";
		for (Model m : keySet()) {
			log += "\n    + "+m+" -> {";
			HashMap<Constraint, Integer> hm = get(m);
			short i =0;
			for (Constraint pt : hm.keySet()) {
				if(hm.get(pt)>0){
					log += pt + " "+hm.get(pt)+", ";
					i++;
				}
			}
			if(i>0)
				log = log.substring(0, log.length()-2);
			log += "}";
		}
		return log+"]";
	}

}
