package partitioner.partition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import oclruler.metamodel.Metamodel;



public class PartitionModel {
	public final static Logger LOGGER = Logger.getLogger(PartitionModel.class.getName());

	
	protected ArrayList<Partition> partitions;
	protected HashSet<PropertyPartition> propertyPartitions;
	protected HashMap<String, HashSet<String>> inheritage;
	
	public PartitionModel() {
		partitions = new ArrayList<>();
		propertyPartitions = new HashSet<>();
		inheritage = new HashMap<>();
	}
	
	public ArrayList<Partition> getPartitions() {
		return partitions;
	}
	public boolean addPartition(Partition p){
		return partitions.add(p);
	}
	
	public HashSet<PropertyPartition> getPropertyPartitions() {
		return propertyPartitions;
	}
	
	public HashMap<String, HashSet<String>> getInheritage() {
		return inheritage;
	}
	

	public ArrayList<Range> getRanges() {
		ArrayList<Range> res = new ArrayList<>();
		for (Partition p : partitions) {
			res.addAll(p.getRanges());
		}
		return res;
	}

	/**
	 * Precondition : Utils must have been loaded : metamodelResource set and inheritage computed.
	 * to do: Paramtrer le passage de COV_DEFINITION (une liste de MMElement to evict from coverage computation)
	 * 
	 */
	public void extractPartition(){
//		System.out.println("PartitionModel.extractPartition()");
		TreeIterator<EObject> eAllContents = Metamodel.metamodelResource.getAllContents();
		
		while (eAllContents.hasNext()) {
			EObject eo = eAllContents.next();
			if(eo instanceof EClass) {
//				System.out.println("1."+eo);
				EClass eClass = (EClass) eo;
				for (EStructuralFeature esf : eClass.getEAllStructuralFeatures()) {
					PropertyPartition pp = new PropertyPartition(this, eClass, esf);
					propertyPartitions.add(pp);
					partitions.addAll(pp.getPartitions());
				}
			} else {
//				System.out.println("2."+eo);
			}
		}
		if(LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(prettyPrint());
		else if (LOGGER.isLoggable(Level.CONFIG))
			LOGGER.config(""+partitions.size()+" partitions extracted.");
	}
	
	
	
	public String prettyPrint() {
		String res = "::"+getClass().getSimpleName()+"::\n";
		res += " - partitions : "+partitions.size()+"\n";
		for (Partition p : partitions) {
			res += "   - "+p.prettyPrint() + "\n";
		}
		return res;
	}
	
	public String printXML() {
		String res = "<partition size=\""+partitions.size()+"\">\n";
		for (Partition p : partitions) 
			res += p.printXML("  ") + "\n";
		return res+"</partition>";
	}

}
