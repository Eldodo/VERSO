package utils.distance;

public abstract class DistanceAlgorithm {
	String s1 , s2;
	
	public DistanceAlgorithm(String s1, String s2) {
		this.s1 = s1;
		this.s2 = s2;
	}
	
	public abstract double distance();
}
