package verso.graphics.camera;

public class CameraSphere extends Camera{

	private double phi = Math.PI/4.0;
	private double roh = 15;
	private double theta = 0;//-Math.PI/2.0;
	private boolean atTop = false;
	
	
	// TODO AIE - HARD CODED values !!!
	private float initialX = 0;
	private float initialZ = 0;
	
	public CameraSphere() {
		this.lookAtX = 0;
		this.lookAtZ = 0;
		this.posX = 1000;
		this.posZ = 1000;

		computeCartesianCoordinate();
	}
	
	public void setCenterPosition(float x, float z) {
		initialX = x;
		initialZ = z;
	}

	public void setPhi(double phi) {
		this.phi = phi;
	}

	public void setRoh(double roh) {
		this.roh = roh;
	}

	public void setTheta(double theta) {
		this.theta = theta;
	}
	
	public void computeCartesianCoordinate() {
		// System.out.println(phi);

		this.posZ = roh * Math.cos(theta) * Math.sin(phi);
		this.posX = roh * Math.sin(theta) * Math.sin(phi);
		this.posY = roh * Math.cos(phi);
		// System.out.println(this.posX + "," + this.posY + "," + this.posZ);

		this.posX += this.lookAtX;
		this.posY += this.lookAtY;
		this.posZ += this.lookAtZ;
		// System.out.println(this.posX + "," + this.posY + "," + this.posZ);

		if ((posX - this.lookAtX) == 0 && (posZ - this.lookAtZ) == 0) {
			this.normalZ = -Math.cos(theta);
			this.normalX = -Math.sin(theta);
			this.normalY = 0;
			atTop = true;
		} else {
			this.normalZ = 0;
			this.normalY = 1;
			this.normalX = 0;
			atTop = false;
		}
	}
	
	
	public void moveVerticalHorizontal(int x, int y) {
		this.phi += y * 0.02 * Math.PI;
		this.theta += x * 0.02 * Math.PI;

		if (this.theta > 2 * Math.PI)
			this.theta = 0;
		if (this.phi < 0)
			this.phi = 0;
		if (this.phi > Math.PI / 2.0)
			this.phi = Math.PI / 2.0;
		computeCartesianCoordinate();
	}
	
	public void moveCross(int x, int y) {

		double fowardX = this.posX - this.lookAtX;
		double fowardZ = this.posZ - this.lookAtZ;
		double sqrtFow = Math.sqrt((fowardX * fowardX) + (fowardZ * fowardZ));
		double foX = fowardX / sqrtFow;
		double foZ = fowardZ / sqrtFow;

		double straffX = this.posZ - this.lookAtZ;
		double straffZ = -(this.posX - this.lookAtX);
		double sqrtStraff = Math.sqrt((straffX * straffX) + (straffZ * straffZ));
		double stX = straffX / sqrtStraff;
		double stZ = straffZ / sqrtStraff;

		if (!atTop) {
			// up and down
			this.posX += foX * roh * 0.05 * y;
			this.posZ += foZ * roh * 0.05 * y;
			this.lookAtX += foX * roh * 0.05 * y;
			this.lookAtZ += foZ * 0.05 * roh * y;

			// Straffing
			this.posX += stX * roh * 0.05 * x;
			this.posZ += stZ * roh * 0.05 * x;
			this.lookAtX += stX * roh * 0.05 * x;
			this.lookAtZ += stZ * 0.05 * roh * x;
		} else {
			// up and down
			this.posX += this.normalX * -roh * 0.05 * y;
			this.posZ += this.normalZ * -roh * 0.05 * y;
			this.lookAtX += this.normalX * -roh * 0.05 * y;
			this.lookAtZ += this.normalZ * 0.05 * -roh * y;
			stX = this.normalZ;
			stZ = -this.normalX;
			// Straffing
			this.posX += stX * -roh * 0.05 * x;
			this.posZ += stZ * -roh * 0.05 * x;
			this.lookAtX += stX * -roh * 0.05 * x;
			this.lookAtZ += stZ * 0.05 * -roh * x;
		}
		// computeSphericalCoordinates();
	}
	
	public void zoom(int i) {
		roh += i * 2.0;
		computeCartesianCoordinate();
	}

	private void computeSphericalCoordinates() {
		double deltaX = this.posX - this.lookAtX;
		double deltaY = this.posY - this.lookAtY;
		double deltaZ = this.posZ - this.lookAtZ;

		this.roh = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
		this.theta = Math.atan(deltaX / deltaZ);
		this.phi = Math.acos(deltaY / this.roh);
	}
	
	public void center() {
		this.lookAtX = initialX;
		this.lookAtZ = initialZ;
		this.lookAtY = 0;
		this.normalX=0;
		this.normalY=1;
		this.normalZ=0;
		this.zoom(0);
	}
	
	public void move(float x, float z) {
		if(x!=0) {
			this.posX = (float)this.posX+x;
			this.lookAtX = (float)this.lookAtX+x;
		}
		if(z!=0) {
			this.posZ = (float)this.posZ +z;
			this.lookAtZ = (float)this.lookAtZ+z;
		}
	}

	public void addTheta(double plusTheta) {
		this.theta += plusTheta;
		if (this.theta > Math.PI)
			this.theta = this.theta - 2 * Math.PI;
	}
}
