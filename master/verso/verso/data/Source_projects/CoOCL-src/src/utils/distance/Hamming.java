package utils.distance;

import utils.Utils;

public class Hamming extends DistanceAlgorithm {

	public Hamming(String s1, String s2) {
		super(s1, s2);
	}

	
	@Override
	public double distance() {
		int length = (s1.length() >= s2.length() ? s1.length() : s2.length())  ;
		s1 = Utils.completeString(s1, length);
		s2 = Utils.completeString(s2, length);
		
        int distance = 0;
        for (int i = 0; i < s2.length(); i++) {
            if (s1.charAt(i) != s2.charAt(i)) 
                distance++;
        }
//		System.out.println(
//				" " + s1 + " |x|\n " + s2 + " |x| " +  distance);
        return distance;
	}

}
