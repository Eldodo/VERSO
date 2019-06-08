package utils.distance;

public class DamerauLevenshtein extends DistanceAlgorithm {
	private int[][] matrix;
	private Boolean calculated = false;

	public DamerauLevenshtein(String a, String b) {
		super(a, b);
		if ((a.length() > 0 || !a.isEmpty()) || (b.length() > 0 || !b.isEmpty())) {
			s1 = a;
			s2 = b;
		}
	}

	public int[][] getMatrix() {
		setupMatrix();
		return matrix;
	}

	@Override
	public double distance() {
		return similarity();
	}
	
	public double similarity() {
		if (!calculated)
			setupMatrix();

		return matrix[s1.length()][s2.length()];
	}

	private void setupMatrix() {
		int cost = -1;
		int del, sub, ins;

		matrix = new int[s1.length() + 1][s2.length() + 1];

		for (int i = 0; i <= s1.length(); i++) {
			matrix[i][0] = i;
		}

		for (int i = 0; i <= s2.length(); i++) {
			matrix[0][i] = i;
		}

		for (int i = 1; i <= s1.length(); i++) {
			for (int j = 1; j <= s2.length(); j++) {
				if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
					cost = 0;
				} else {
					cost = 1;
				}

				del = matrix[i - 1][j] + 1;
				ins = matrix[i][j - 1] + 1;
				sub = matrix[i - 1][j - 1] + cost;

				matrix[i][j] = minimum(del, ins, sub);

				if ((i > 1) && (j > 1) && (s1.charAt(i - 1) == s2.charAt(j - 2)) && (s1.charAt(i - 2) == s2.charAt(j - 1))) {
					matrix[i][j] = minimum(matrix[i][j], matrix[i - 2][j - 2] + cost);
				}
			}
		}

		calculated = true;
//		displayMatrix();
	}

	private void displayMatrix() {
		System.out.println(" " + s1);
		for (int y = 0; y <= s2.length(); y++) {
			if (y - 1 < 0)
				System.out.print(" ");
			else
				System.out.print(s2.charAt(y - 1));
			for (int x = 0; x <= s1.length(); x++) {
				System.out.print(matrix[x][y]);
			}
			System.out.println();
		}
	}

	private int minimum(int d, int i, int s) {
		int m = Integer.MAX_VALUE;

		if (d < m)
			m = d;
		if (i < m)
			m = i;
		if (s < m)
			m = s;

		return m;
	}

	private int minimum(int d, int t) {
		int m = Integer.MAX_VALUE;

		if (d < m)
			m = d;
		if (t < m)
			m = t;

		return m;
	}
}