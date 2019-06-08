package verso.graphics.camera;

public class Camera {

	protected double posX = 25.0, posY = 25.0, posZ = 5.0;
	protected double lookAtX = 0, lookAtY = 0, lookAtZ = 0;
	protected double normalX = 0, normalY = 1.0, normalZ = 0;
	
	public double getX()
	{
		return posX;
	}
	
	public double getY()
	{
		return posY;
	}
	
	public double getZ()
	{
		return posZ;
	}
	
	public double getLookAtX()
	{
		return lookAtX;
	}
	
	public double getLookAtY()
	{
		return lookAtY;
	}
	
	public double getLookAtZ()
	{
		return lookAtZ;
	}
	
	public double getNormalX()
	{
		return normalX;
	}
	
	public double getNormalY()
	{
		return normalY;
	}
	
	public double getNormalZ()
	{
		return normalZ;
	}
	
	public void setX(float posX)
	{
		this.posX = posX;
	}
	
	public void setY(float posY)
	{
		this.posY = posY;
	}
	
	public void setZ(float posZ)
	{
		this.posZ = posZ;
	}
	
	public void setLookAtX(float lookAtX)
	{
		this.lookAtX = lookAtX;
	}
	
	public void setLookAtY(float lookAtY)
	{
		this.lookAtY = lookAtY;
	}
	
	public void setLookAtZ(float lookAtZ)
	{
		this.lookAtZ = lookAtZ;
	}
	
	public void setNormalX(float normalX)
	{
		this.normalX = normalX;
	}
	
	public void setNormalY(float normalY)
	{
		this.normalY = normalY;
	}
	
	public void setNormalZ(float normalZ)
	{
		this.normalZ = normalZ;
	}
	
	public void moveVerticalHorizontal(int x, int y)
	{
		
	}
	
	public void moveCross(int x, int y)
	{
		
	}
	
	public void zoom(int zoom)
	{}
}
