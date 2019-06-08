package verso.representation.cubeLandscape.representationModel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.media.opengl.GL;

import org.apache.commons.lang3.StringUtils;

import verso.model.Entity;
import verso.model.Package;
import verso.representation.Renderable;
import verso.representation.cubeLandscape.representationModel.repvisitor.IRepresentationVisitor;

public abstract class PackageRepresentation extends EntityRepresentation implements Renderable {
	public enum ORIENTATION_COLOR {
		FIRSTCOLOR(0.6f, 0.6f, 0.6f), SECONDCOLOR(0.8f, 0.8f, 0.8f), LIBRARYCOLOR(0.5f, 0.4f, 0.3f);
		float[] color = new float[3];
		
		private ORIENTATION_COLOR(float r, float g, float b) {
			color[0] = r;
			color[1] = g;
			color[2] = b;
		}
		
		float[] getColor() {
			return color;
		}
	}
	public static final float PACKAGE_HEIGHT = 1.5f;
	public ORIENTATION_COLOR orientationColor = ORIENTATION_COLOR.FIRSTCOLOR;

	
	public Color bordersColor = Color.green;
	
	//public static boolean showAllPac = false;
	public static int nowShowingPackageLevel = 0;
	public static int maxPacLevel =0;
	protected Package packagedef;
	protected boolean fakePackage = false;
	protected HashMap<String, ElementRepresentation> elements;
	protected HashMap<String, PackageRepresentation> packages;
	protected HashMap<String, Integer> linksCount;
	protected int packageLevel = 1000;
	
	//double sizeX = 0;
	//double sizeZ = 0;
	
	public static boolean pseudoRender = false;
	protected boolean render = false;
	//meshes
	
	/*
	private Primitive topBorder;
	private Primitive bottomBorder;
	private Primitive leftBorder;
	private Primitive rightBorder;
	*/
	
	protected PackageRepresentation pseudoPackage = null;
	
	@Override
	public String toString() {
		return "PackRep<"+packagedef.getName()+">";
	}
	
	public PackageRepresentation(Package packagedef) {
		this.packagedef = packagedef;
		this.elements = new HashMap<String, ElementRepresentation>();
		this.packages = new HashMap<String, PackageRepresentation>();
		this.linksCount = new HashMap<String, Integer>();

		this.height = PACKAGE_HEIGHT;
	}	
	
	@Override
	public float getLevel() {
		return StringUtils.countMatches(getEntity().getName(),	".");
	}
	
	public boolean equals(Object o) {
		if (o instanceof PackageRepresentation) {
			return ((PackageRepresentation) o).getPackage().getName().compareTo(this.getPackage().getName()) == 0;
		} else {
			return false;
		}
	}
	
	public Package getPackage() {

		return this.packagedef;
	}

	public void setFake() {
		this.fakePackage = true;
	}

	public boolean isFakePackage() {
		return this.fakePackage;
	}
	
	public int getPackageLevel() {
		return this.packageLevel;
	}

	public void setPackageLevel(int lvl) {
		this.packageLevel = lvl;
	}

	public HashMap<String, Integer> getLinkCount() {
		return this.linksCount;
	}

	public void setLinkCount(HashMap<String, Integer> lc) {
		this.linksCount = new HashMap<String, Integer>(lc);
	}

	public Entity getEntity() {
		return this.packagedef;
	}

	
	
	public void addElement(ElementRepresentation elRep) {
		this.elements.put(elRep.getElementModel().getName(), elRep);
	}

	public void addElements(Collection<ElementRepresentation> elementsRep) {
		for (ElementRepresentation elRep : elementsRep) {
			addElement(elRep);
		}
	}

	public ElementRepresentation getElement(String elementName) {
		return this.elements.get(elementName);
	}

	public void addPackage(PackageRepresentation p) {
		this.packages.put(p.getPackage().getName(), p);
	}

	public void addPackages(Collection<PackageRepresentation> packages) {
		for (PackageRepresentation p : packages) {
			this.packages.put(p.getPackage().getName(), p);
		}
	}

	public void removePackage(PackageRepresentation p) {
		this.packages.remove(p.getSimpleName());
	}

	public PackageRepresentation getPackage(String packageName) {
		return this.packages.get(packageName);
	}

	public PackageRepresentation findPackage(String pacName) {
		PackageRepresentation pac = null;
		if (this.packages.containsKey(pacName)) {
			return this.packages.get(pacName);
		} else {
			for (PackageRepresentation p : this.getPackages()) {
				pac = p.findPackage(pacName);
				if (pac != null)
					break;
			}
		}
		return pac;
	}
	
	public Collection<PackageRepresentation> getPackages() {
		return this.packages.values();
	}

	public void clearPackages() {
		this.packages.clear();
	}

	public Collection<ElementRepresentation> getElements() {
		return this.elements.values();
	}

	public void clearElements() {
		this.elements.clear();
	}
	
	/*
	public void setSizeX(double sizeX)
	{
		this.sizeX = sizeX;
	}
	
	public void setSizeZ(double sizeZ)
	{
		this.sizeZ =sizeZ;
	}
	
	public double getSizeX()
	{
		return this.sizeX;
	}
	
	public double getSizeZ()
	{
		return this.sizeZ;
	}
	*/
	
	public boolean isRendered() {
		return this.render;
	}

	public void setRender(boolean val) {
		this.render = val;
	}

	public String getName() {
		return "Package : " + this.packagedef.getName();
	}

	public String getSimpleName() {
		return this.packagedef.getName();
	}

	public void accept(IRepresentationVisitor mv) {
		mv.visit(this);
	}
	ArrayList<ElementRepresentation> allElements;
	public ArrayList<ElementRepresentation> getAllElements() {
		if(allElements == null) {
			allElements = new ArrayList<ElementRepresentation>();
			allElements.addAll(this.getElements());
			for (PackageRepresentation p : this.packages.values()) {
				allElements.addAll(p.getAllElements());
			}
		}
		return allElements;
	}

	HashMap<String, PackageRepresentation> allPackages;
	public HashMap<String, PackageRepresentation> getAllPackages() {
		if(allPackages == null) {
			allPackages = new HashMap<String, PackageRepresentation>();
			allPackages.put(this.getSimpleName(), this);
	
			for (PackageRepresentation subPack : this.getPackages()) {
				allPackages.putAll(subPack.getAllPackages());
			}
		}
		return allPackages;
	}

	public int countDescendantClasses() {
		int numberOfChildren = 0;
		for (PackageRepresentation p : this.packages.values()) {
			numberOfChildren += p.countDescendantClasses();
		}
		return numberOfChildren + this.elements.size();
	}

	public int countDescendantPackages() {
		int numberOfChildren = 0;

		if (this.packages.size() > 0) {
			numberOfChildren += this.packages.size();

			for (PackageRepresentation p : this.packages.values()) {
				numberOfChildren += p.countDescendantPackages();
			}

		}

		return numberOfChildren;
	}

	public void render(GL gl) {

	}
	

//	public void setCamDist(double camX, double camY, double camZ) {
//		for (ElementRepresentation elem : this.elements.values()) {
//			elem.setCamDist(camX, camY, camZ);
//		}
//		for (PackageRepresentation pack : this.packages.values()) {
//			pack.setCamDist(camX, camY, camZ);
//		}
//	}
	
	/*
	public void createPseudoPackage()
	{
		int maxX =0;
		int maxZ = 0;
		for (ElementRepresentation elem : this.elements.values())
		{
			if (elem.posX > maxX)
				maxX = elem.posX;
			if (elem.posZ > maxZ)
				maxZ = elem.posZ;
		}
		this.pseudoPackage = new PackageRepresentation(new Package(this.packagedef.getName()));
		this.pseudoPackage.setSizeX(maxX+1);
		this.pseudoPackage.setSizeZ(maxZ+1);
		for (TreemapPackageRepresentation pack : this.packages.values())
		{
			pack.createPseudoPackage();
		}
	}
	*/

	/*
	public int compareTo(PackageRepresentation arg0) {
		return this.countDescendantClasses() - arg0.countDescendantClasses();
	}
	*/
	
	protected boolean anyChildIsSelected()
	{
		if (this.isSelected)return true;
		for (ElementRepresentation er : this.getElements())
		{
			if (er.isSelected()) return true;
		}
		for (PackageRepresentation p : this.getPackages())
		{
			if (p.anyChildIsSelected()) return true;
		}
		return false;
	}
	
	
	public int computeMaxLevel() {
		int maxLevel = -1;
		int packLevel = -1;
		
		for (PackageRepresentation pack : this.getSubPackages()) {
			packLevel = pack.computeMaxLevel();
			
			if (packLevel > maxLevel) {
				maxLevel = packLevel;
			}
		}
		
		if (this.isFakePackage()) {
			return maxLevel;
		}
		else {
			return maxLevel+1;
		}
	}
	
	
	public int computePackageLevel()
	{
		int maxLevel = 0;
		int pacLevel =0;
		for (PackageRepresentation pac : this.getPackages())
		{
			pacLevel = pac.computePackageLevel();
			if (pacLevel > maxLevel)
			{
				maxLevel = pacLevel;
			}
		}
		if (!this.isFakePackage())
		{
			this.setPackageLevel(maxLevel+1);
			return maxLevel+1;
		}
		else
			return maxLevel+0;
		
		/*
		int currLevel = 0;
		this.setPackageLevel(currLevel);
		
		for (PackageRepresentation pack : this.getPackages()) {
			pack.computePackageLevel(currLevel+1);
		}
		*/
	}
	
	void computePackageLevel(int currLevel) {
		if (!this.isFakePackage()) {
			this.setPackageLevel(currLevel);
			currLevel++;
		} else {
			this.setPackageLevel(-1);
		}

		for (PackageRepresentation pack : this.getPackages()) {
			pack.computePackageLevel(currLevel);
		}
	}
	
	public Collection<PackageRepresentation> getSubPackages() {
		HashSet<PackageRepresentation> subPackages = new HashSet<PackageRepresentation>(); 
		for (PackageRepresentation pack : this.getPackages()) {
			if (!pack.isFakePackage()) {
				subPackages.add(pack);
			}
			else {
				subPackages.addAll(pack.getSubPackages());
			}
		}
		
		return subPackages;
	}
	
	public Collection<ElementRepresentation> getSubElements() {
		HashSet<ElementRepresentation> subElements = new HashSet<ElementRepresentation>();
		
		subElements.addAll(this.getElements());
		
		for (PackageRepresentation pack : this.getPackages()) {
			if (pack.isFakePackage()) {
				subElements.addAll(pack.getSubElements());
			}
		}
		
		return subElements;
	}
	
	@Override
	public boolean isPackage() {
		return true;
	}
	
	//public abstract PackageRepresentation copy();
}
