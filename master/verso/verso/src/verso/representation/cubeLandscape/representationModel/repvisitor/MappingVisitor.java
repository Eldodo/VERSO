package verso.representation.cubeLandscape.representationModel.repvisitor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import verso.model.metric.Metric;
import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.EntityRepresentation;
import verso.representation.cubeLandscape.representationModel.MethodRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;

public class MappingVisitor implements IRepresentationVisitor{
	
	public static final float SIZE_FACTOR = 3.0f;
	public static final float SIZE_OFFSET = 0.25f;
	
	String mapName = "";
	List<Mapping> packageMapping = new ArrayList<Mapping>();
	List<Mapping> classMapping = new ArrayList<Mapping>();
	List<Mapping> methodMapping = new ArrayList<Mapping>();
	
	
	public MappingVisitor(String name) {
		mapName = name;
	}

	public String getName() {
		return this.mapName;
	}

	public List<Mapping> getPackageMapping() {
		return this.packageMapping;
	}

	public List<Mapping> getClassMapping() {
		return this.classMapping;
	}

	public List<Mapping> getMethodMapping() {
		return this.methodMapping;
	}

	public void addPackageMapping(Mapping m) {
		this.packageMapping.add(m);
	}

	public void addClassMapping(Mapping m) {
		this.classMapping.add(m);
	}

	public void addMethodMapping(Mapping m) {
		this.methodMapping.add(m);
	}

	@SuppressWarnings("unchecked")
	public void visit(PackageRepresentation p) {
		Metric<Double> met;
		for (Mapping m : packageMapping) {
			met = p.getPackage().getMetric(m.getMetric());
			mapMetric(p, met, m);
		}
		// continue visit
		for (PackageRepresentation pac : p.getPackages()) {
			pac.accept(this);
		}
		for (ElementRepresentation elem : p.getElements()) {
			elem.accept(this);
		}

	}

	@SuppressWarnings("unchecked")
	public void visit(ElementRepresentation e) {
		Metric<Double> met;
		for (Mapping m : classMapping) {
			met = e.getElementModel().getMetric(m.getMetric());
			mapMetric(e, met, m);
		}
		// continue visit
		for (MethodRepresentation mr : e.getMethods()) {
			mr.accept(this);
		}
	}

	@SuppressWarnings("unchecked")
	public void visit(MethodRepresentation method) {
		Metric<Double> met;
		for (Mapping m : methodMapping) {
			met = method.getElement().getMetric(m.getMetric());
			mapMetric(method, met, m);
		}
	}
	

	
	private void setSimonColor(EntityRepresentation p, float value) {
		if (value <= 0) {
			p.setColor(Color.blue);
		} else if (value <= 0.25) {
			p.setColor(new Color(0, 255, 150));
		} else if (value <= 0.5) {
			p.setColor(new Color(255, 255, 0));
		} else if (value <= 0.75) {
			p.setColor(new Color(255, 100, 0));
		} else {
			p.setColor(Color.yellow);
		}
	}

	private void setViascoColor(EntityRepresentation p, float value) {
		if (value <= 0) {
			p.setColor(Color.green);
		} else if (value <= 0.25) {
			p.setColor(new Color(0, 255, 150));
		} else if (value <= 0.5) {
			p.setColor(new Color(255, 255, 0));
		} else if (value <= 0.75) {
			p.setColor(new Color(255, 100, 0));
		} else {
			p.setColor(Color.red);
		}
	}
	
	
	private void mapMetric(EntityRepresentation e, Metric<Double> met, Mapping m) {
		float value;
		if (met != null) {
			value = (float) met.getNormalizedValue();
			if (m.graphicalValue.compareTo("ViascoColor") == 0) {
				setViascoColor(e, value);
			} else if (m.graphicalValue.compareTo("SimonColor") == 0) {
				setSimonColor(e, value);
			} else if (m.graphicalValue.compareTo("Color") == 0) {
				e.setColor(new Color((float) value, 0, 1.0f - (float) value));
			} else if (m.graphicalValue.compareTo("Height") == 0) {
				e.setHeight(value * SIZE_FACTOR + SIZE_OFFSET); 
			} else if (e.isElement() && m.graphicalValue.compareTo("Twist") == 0) {
				((ElementRepresentation)e).setTwist(value * -90.0);
			}
		}
	}

	
	public static MappingVisitor mvBug() {
		MappingVisitor mvBug;
		mvBug = new MappingVisitor("mvBug");
		mvBug.addPackageMapping(new Mapping("PackageMainBugProgrammer", "Color"));
		mvBug.addPackageMapping(new Mapping("PackageTotalNumberOfBugs", "Height"));
		mvBug.addClassMapping(new Mapping("MainBugProgrammer", "Color"));
		mvBug.addClassMapping(new Mapping("TotalNumberOfBugs", "Height"));
		mvBug.addClassMapping(new Mapping("NumberOfOpenBugs", "Twist"));
		return mvBug;
	}

	public static MappingVisitor mvControl() {
		MappingVisitor mvControl;
		mvControl = new MappingVisitor("mvControl");
		mvControl.addPackageMapping(new Mapping("PackageMainAuthor", "Color"));
		mvControl.addPackageMapping(new Mapping("PackageNumberOfCommits", "Height"));
		mvControl.addClassMapping(new Mapping("MainAuthor", "Color"));
		mvControl.addClassMapping(new Mapping("NumberOfCommits", "Height"));
		mvControl.addClassMapping(new Mapping("NumberOfAutors", "Twist"));
		mvControl.addMethodMapping(new Mapping("MetMainAuthor", "Color"));
		mvControl.addMethodMapping(new Mapping("MetNumberOfRevision", "Height"));
		mvControl.addMethodMapping(new Mapping("MetLatestRevision", "Twist"));
		return mvControl;
	}

	public static MappingVisitor mvViasco() {
		MappingVisitor mvViasco;
		mvViasco = new MappingVisitor("mvViasco");
		mvViasco.addPackageMapping(new Mapping("ComponentComplexity", "Height"));
		mvViasco.addPackageMapping(new Mapping("ComponentCoupling", "ViascoColor"));
		mvViasco.addClassMapping(new Mapping("CBO", "ViascoColor"));
		mvViasco.addClassMapping(new Mapping("WMC", "Height"));
		mvViasco.addClassMapping(new Mapping("DIT", "Twist"));
		return mvViasco;
	}

	public static MappingVisitor mvQuality() {
		MappingVisitor mvQuality;
		mvQuality = new MappingVisitor("mvQuality");
		mvQuality.addPackageMapping(new Mapping("PackageCoupling", "Color"));
		mvQuality.addPackageMapping(new Mapping("PackageComplexity", "Height"));
		mvQuality.addClassMapping(new Mapping("CBO", "Color"));
		mvQuality.addClassMapping(new Mapping("WMC", "Height"));
		mvQuality.addClassMapping(new Mapping("LCOM5", "Twist"));
		mvQuality.addMethodMapping(new Mapping("CouplingMethod", "Color"));
		mvQuality.addMethodMapping(new Mapping("MCMethod", "Height"));
		mvQuality.addMethodMapping(new Mapping("CohMethod", "Twist"));
		return mvQuality;
	}

}
