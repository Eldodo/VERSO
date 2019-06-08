package oclruler.metamodel;

import java.util.ArrayList;
import java.util.WeakHashMap;

import oclruler.utils.ToolBox;

public class PathMap  {
	
	WeakHashMap<PathMapKey, ArrayList<Path>> paths;
	
	public PathMap() {
		paths = new WeakHashMap<PathMap.PathMapKey, ArrayList<Path>>();
	}
	
	
	public boolean put(Path p){
		PathMapKey pmk = null;
		for (PathMapKey pmk0 : paths.keySet()) {
			if(pmk0.start.equalNames(p.getStart()) && pmk0.end.equalNames(p.getEndType()) ){
				pmk=pmk0;
				break;
			}
		}
		if(pmk == null)
			pmk = new PathMapKey(p.getStart(), p.getEndType());
		
		if(paths.get(pmk) == null)
			paths.put(pmk, new ArrayList<Path>(3));
		
		boolean res = false;
		
		if(!paths.get(pmk).contains(p))
			res = paths.get(pmk).add(p);
		return res ;
	}
	
	public ArrayList<Path> get(Concept start, Concept end){
		PathMapKey pmk = new PathMapKey(start, end);
		return paths.get(pmk);
	}

	
	public class PathMapKey {
		Concept start;
		Concept end;
		
		public PathMapKey(Concept start, Concept end) {
			this.start = start;
			this.end = end;
		}
		
		@Override
		public boolean equals(Object o) {
			if(o == null || getClass() != o.getClass())
				return false;
			PathMapKey pmk = (PathMapKey)o;
			return this.start.equalNames(pmk.start)
				&& this.end.equalNames(pmk.end) ;
		}
		
		@Override
		public String toString() {
			return "["+start.name+">"+end.name+"]";
		}
	}


	public void putAll(ArrayList<Path> paths2) {
		for (Path path : paths2) {
			put(path);
		}
	}
	


	public int fullSize() {
		int res = 0;
		for (ArrayList<Path> ps : paths.values()) 
			res += ps.size();
		return res;
	}


	public boolean contains(Path p) {
		for (ArrayList<Path> ps : paths.values()) {
			if(ps.contains(p))
				return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "PathMap<"+paths.keySet()+"("+fullSize()+")>";
	}
	
	public String prettyPrint(){
		return prettyPrint("");
	}
	
	public String prettyPrint(String tab){
		if(paths.isEmpty())
			return tab+"<PathMap-EMPTY>";
		
		String res = tab+toString()+"{\n";
		String tab2 = tab + ToolBox.TAB_CHAR;
		for (PathMapKey pmk : paths.keySet()) {
			res += tab2 + pmk +"\n";
			for (Path p : paths.get(pmk)) {
				res += tab2 + "  "+ p.prettyPrint(tab2)+"\n";
			}
		}
		return res+"\n"+tab+"}";
	}

}
