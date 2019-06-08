package partition.ocl;

import java.util.ArrayList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import partition.PropertyPartition;

public class OCLMatch {
	
	String header;
	String expression;
	EClass context;
	EClass[] classesInvolved;
	EStructuralFeature[] featuresInvolved;

	
	public OCLMatch(String header, String expr, EClass context) {
		this.header = header;
		this.expression = expr;
		this.context = context;
	}
	
	public String getHeader() {
		return header;
	}
	public EClass getContext() {
		return context;
	}
	public String getOCLStringExpression() {
		return expression;
	}
	public void setClassesInvolved(ArrayList<EClass> classesInvolved) {
		this.classesInvolved = (EClass[])classesInvolved.toArray();
	}
	public void setClassesInvolved(EClass classInvolved) {
		this.classesInvolved = new EClass[] {classInvolved};
	}
	public void setClassesInvolved(EClass[] classesInvolved) {
		this.classesInvolved = classesInvolved;
	}

	public void setFeaturesInvolved(ArrayList<EStructuralFeature> featuresInvolved) {
		this.featuresInvolved = (EStructuralFeature[])featuresInvolved.toArray();
	}
	public void setFeaturesInvolved(EStructuralFeature[] featuresInvolved) {
		this.featuresInvolved = featuresInvolved;
	}
	public void setFeaturesInvolved(EStructuralFeature featureInvolved) {
		this.featuresInvolved = new EStructuralFeature[] {featureInvolved};
	}
	
	
	/**
	 * Return the PropertyPartitions made from the involved classes (including context) and the involved features.
	 * 
	 * @return
	 */
	public ArrayList<PropertyPartition> getPropertyPartitions(){
		ArrayList<PropertyPartition> res = new ArrayList<>();
		//Connections to context
		if(featuresInvolved != null){
			for (EStructuralFeature esf : context.getEStructuralFeatures()) {
				for (EStructuralFeature esf2 : featuresInvolved) {
					if(esf.equals(esf2)){
						PropertyPartition pp = new PropertyPartition(context, esf);
						res.add(pp);
					}
				}
			}
		} else {
			for (EStructuralFeature esf : context.getEStructuralFeatures()) {
				PropertyPartition pp = new PropertyPartition(context, esf);
				res.add(pp);
			}
		}
		
		//Connections to involvedClasses
		if(classesInvolved != null){
			for (EClass ec : classesInvolved) {
				for (EStructuralFeature esf : ec.getEStructuralFeatures()) {
						PropertyPartition pp = new PropertyPartition(ec, esf);
						res.add(pp);
				}
			}
			
			
//			for (EStructuralFeature esf : featuresInvolved) {
//				for (EClass ec : classesInvolved) {
//					if(ec.getEStructuralFeatures().contains(esf) && ec.eIsSet(esf)){
//						PropertyPartition pp = new PropertyPartition(ec, esf);
//						res.add(pp);
//					}
//				}
//			}
		}
//		System.out.println(this+".getPropertyPartitions : "+res);
		return res;
	}

	
	
	@Override
	public String toString() {
		return "OCLMatch("+context.getName()+", '"+expression+"')";
	}
}
