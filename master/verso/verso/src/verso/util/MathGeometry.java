package verso.util;


public class MathGeometry {
	public static float[] get3DCrossProduct(float[] u, float[] v) {
		float[] uXv = {u[1]*v[2] - u[2]*v[1], u[2]*v[0] - u[0]*v[2], u[0]*v[1]-u[1]*v[0]};
		
		return uXv;
	}
	
	public static float[] getRotationParams(float x, float y, float z) {
		float[] rotationParams = new float[4];
		float[] u = {0, 1, 0};
		float[] v = {x, y, z};
		
		if (v[0] == 0 && v[2] == 0) {
			if (v[1] >= 0) {
				return null;
			}
			else {
				rotationParams[0] = 180f;
				rotationParams[1] = 0.0f;
				rotationParams[2] = 0.0f;
				rotationParams[3] = 1.0f;
			}
		}
		
		float[] uXv = get3DCrossProduct(u, v);			
		float hypotenuse = (float)Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
		float opposite = Math.abs(v[1]);
		float angleRotation = (float)Math.toDegrees(Math.asin(opposite / hypotenuse));

		if (v[1] < 0) {
			angleRotation += 90;
		}
		else  {
			angleRotation = 90 - angleRotation;
		}

		rotationParams[0] = angleRotation;
		rotationParams[1] = uXv[0];
		rotationParams[2] = uXv[1];
		rotationParams[3] = uXv[2];
		
		return rotationParams;
	}
	
	
	public static float[] get3DRotationMatrix(float angle, float x, float y, float z) {
		float[] rotationMatrix = new float[16];
		
		float vectorLength = (float)Math.sqrt(x*x + y*y + z*z);
		
		x = x/vectorLength;
		y = y/vectorLength;
		z = z/vectorLength;
		
		float c = (float)Math.cos(Math.toRadians(angle));
		float s = (float)Math.sin(Math.toRadians(angle));
		float t = 1 - c;
		
		rotationMatrix[0] = t*(x*x) + c;
		rotationMatrix[1] = t*x*y - s*z;
		rotationMatrix[2] = t*x*z + s*y;
		rotationMatrix[3] = 0;
			
		rotationMatrix[4] = t*x*y + s*z;
		rotationMatrix[5] = t*(y*y) + c;
		rotationMatrix[6] = t*y*z - s*x;
		rotationMatrix[7] = 0;
			
		rotationMatrix[8] = t*x*z - s*y;
		rotationMatrix[9] = t*y*z + s*x;
		rotationMatrix[10] = t*(z*z) + c;
		rotationMatrix[11] = 0;
					
		rotationMatrix[12] = 0;
		rotationMatrix[13] = 0;
		rotationMatrix[14] = 0;
		rotationMatrix[15] = 1;
		
		return rotationMatrix;
	}
	
	public static float[] get3DIdentityMatrix() {
		float[] identityMatrix = {1, 0, 0, 0,
								  0, 1, 0, 0,
								  0, 0, 1, 0,
								  0, 0, 0, 1};
		
		return identityMatrix;
	}
	
	public static float[] rotate3DPoint(float[] rotationMatrix, float[] point) {
		float[] rotatedPoint = new float[3];
		
		rotatedPoint[0] = rotationMatrix[0]*point[0] + rotationMatrix[1]*point[1] + rotationMatrix[2]*point[2]; //+ rotationMatrix[3]*point[3];
		rotatedPoint[1] = rotationMatrix[4]*point[0] + rotationMatrix[5]*point[1] + rotationMatrix[6]*point[2]; //+ rotationMatrix[7]*point[3];
		rotatedPoint[2] = rotationMatrix[8]*point[0] + rotationMatrix[9]*point[1] + rotationMatrix[10]*point[2]; //+ rotationMatrix[11]*point[3];
		
		return rotatedPoint;
	}
	
	public static float arcLength(float radius, float angle) {
		return (angle / 360.0f) * (2 * (float)Math.PI * radius);
	}
	
	public static float arcAngle(float radius, float arcLength) {
		return (arcLength / (2 * (float)Math.PI * radius)) * 360.0f;
	}
	
	public static float getPositiveDistance(float[] firstPoint, float[] secondPoint) {
		if (firstPoint.length != secondPoint.length) {
			System.out.println("Points de différentes dimensions");
			return -1.0f;
		}
		else {
			float distance = 0.0f;
			
			for (int i = 0; i < firstPoint.length; i++) {
				distance += (secondPoint[i] - firstPoint[i]) * (secondPoint[i] - firstPoint[i]);
			}
			
			distance = (float)Math.sqrt(distance);
			
			return distance;
		}
	}
	
	public static float[] getPointPosition(float radius, float angle) {
		float[] position = new float[2];
		
		position[0] = radius * (float)Math.cos(Math.toRadians((double)angle));
		position[1] = radius * (float)Math.sin(Math.toRadians((double)angle));
		
		return position;
	}
}
