package utils.distance;

import java.util.Arrays;

import org.apache.commons.math3.ml.clustering.DoublePoint;

import coocl.ocl.Program;

public class DoublePointProgram extends DoublePoint {
	private static final long serialVersionUID = 1L;

	Program program1, program2;
	public DoublePointProgram(double[] point, Program p1) {
		super(point);
		this.program1 = p1;
	}
	
	public Program getProgram() {
		return program1;
	}
	
	 /** {@inheritDoc} */
    @Override
    public String toString() {
        return program1.getName()+Arrays.toString(getPoint());
    }
    
    public double euclidianDistanceToVector(double[] v){
    	double res = 0.0;
    	for (int i = 0; i < v.length; i++) {
			res += (v[i] - getPoint()[i]) * (v[i] - getPoint()[i]);
		}
    	res = Math.sqrt(res);
		return res;
    }

}
