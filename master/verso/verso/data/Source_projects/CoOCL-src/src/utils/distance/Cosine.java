package utils.distance;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import utils.Config;

/**
 * The similarity between the two strings is the cosine of the angle between
 * these two vectors representation. It is computed as V1 . V2 / (|V1| * |V2|)
 * The cosine distance is computed as 1 - cosine similarity.
 *
 * @author Thibault Debatty
 */
public class Cosine extends DistanceAlgorithm {

	public static int DEFAULT_K = 3;
	private final int k;

	public int getK() {
		return k;
	}

	/**
	 * Implements Cosine Similarity between strings. The strings are first
	 * transformed in vectors of occurrences of k-shingles (sequences of k
	 * characters). In this n-dimensional space, the similarity between the two
	 * strings is the cosine of their respective vectors.
	 *
	 * @param k
	 */
	public Cosine(String s1, String s2, final int k) {
		super(s1, s2);
		if (k <= 0) {
			throw new IllegalArgumentException("k should be positive!");
		}
		this.k = k;
	}

	/**
	 * Implements Cosine Similarity between strings. The strings are first
	 * transformed in vectors of occurrences of k-shingles (sequences of k
	 * characters). In this n-dimensional space, the similarity between the two
	 * strings is the cosine of their respective vectors. Default k is 3.
	 */
	public Cosine(String s1, String s2) {
		this(s1, s2, DEFAULT_K);
	}

	/**
	 * Compute the cosine similarity between strings.
	 * 
	 * @param s1
	 *            The first string to compare.
	 * @param s2
	 *            The second string to compare.
	 * @return The cosine similarity in the range [0, 1]
	 * @throws NullPointerException
	 *             if s1 or s2 is null.
	 */
	public final double similarity() {
		if (s1.equals(s2)) {
			return 1;
		}

		if (s1.length() < getK() || s2.length() < getK()) {
			return 0;
		}

		Map<String, Integer> profile1 = getProfile(s1);
		Map<String, Integer> profile2 = getProfile(s2);

		return dotProduct(profile1, profile2) / (norm(profile1) * norm(profile2));
	}


	public final Map<String, Integer> getProfile(final String string) {
		HashMap<String, Integer> shingles = new HashMap<String, Integer>();
		Pattern SPACE_REG = Pattern.compile("\\s+");
		String string_no_space = SPACE_REG.matcher(string).replaceAll(" ");
		for (int i = 0; i < (string_no_space.length() - k + 1); i++) {
			String shingle = string_no_space.substring(i, i + k);
			Integer old = shingles.get(shingle);
			if (old != null) {
				shingles.put(shingle, old + 1);
			} else {
				shingles.put(shingle, 1);
			}
		}

		return Collections.unmodifiableMap(shingles);
	}

	/**
	 * Compute the norm L2 : sqrt(Sum_i( v_i²)).
	 *
	 * @param profile
	 * @return L2 norm
	 */
	private static double norm(final Map<String, Integer> profile) {
		double agg = 0;

		for (Map.Entry<String, Integer> entry : profile.entrySet()) {
			agg += 1.0 * entry.getValue() * entry.getValue();
		}

		return Math.sqrt(agg);
	}

	private static double dotProduct(final Map<String, Integer> profile1, final Map<String, Integer> profile2) {

		// Loop over the smallest map
		Map<String, Integer> small_profile = profile2;
		Map<String, Integer> large_profile = profile1;
		if (profile1.size() < profile2.size()) {
			small_profile = profile1;
			large_profile = profile2;
		}

		double agg = 0;
		for (Map.Entry<String, Integer> entry : small_profile.entrySet()) {
			Integer i = large_profile.get(entry.getKey());
			if (i == null) {
				continue;
			}
			agg += 1.0 * entry.getValue() * i;
		}

		return agg;
	}

	/**
	 * Return 1.0 - similarity.
	 * 
	 * @param s1
	 *            The first string to compare.
	 * @param s2
	 *            The second string to compare.
	 * @return 1.0 - the cosine similarity in the range [0, 1]
	 * @throws NullPointerException
	 *             if s1 or s2 is null.
	 */
	public final double distance() {
		return 1.0 - similarity();
	}

	protected double similarity(final Map<String, Integer> profile1, final Map<String, Integer> profile2) {
		return dotProduct(profile1, profile2) / (norm(profile1) * norm(profile2));
	}

	public static void loadConfig() {
		DEFAULT_K = Config.getIntParam("COSINE_DISTANCE_K");
	}

}