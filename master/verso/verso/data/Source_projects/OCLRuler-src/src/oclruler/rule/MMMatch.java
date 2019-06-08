package oclruler.rule;

import java.util.ArrayList;
import java.util.Iterator;

import oclruler.metamodel.MMElement;

public class MMMatch extends ArrayList<MMElement>{

	int expectedElements = 0;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MMMatch(int expectedElements) {
		super(expectedElements);
		this.expectedElements = expectedElements;
	}
	
	public boolean hasAllElements() {
		return this.size()-1 == expectedElements; // '-1' because of the context !
	}
	
	@Override
	public String toString() {
		String res = "Match:{";
		for (Iterator<MMElement> iterator = this.iterator(); iterator.hasNext();) {
			MMElement elt = (MMElement) iterator.next();
			res += elt.getName() +" " ;
		}
		return res.trim() +"}";
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null || ( getClass() != o.getClass()))
			return false;
		MMMatch m = (MMMatch)o;
		if(m.size() != size())
			return false;
		for (int i = 0; i < size(); i++) {
			if(! m.get(i).equalNames(get(i)) )
				return false;
		}
		return true;
	}
	
	@Override
	public MMMatch clone() {
		MMMatch clone = new MMMatch(expectedElements);
		for (Iterator<MMElement> iterator = this.iterator(); iterator.hasNext();) {
			MMElement elt = (MMElement) iterator.next();
			clone.add(elt);
		}
		return clone;
	}
	
}
