package verso.graphics;


import javax.media.opengl.GLJPanel;

import verso.graphics.camera.CameraSphere;


public class RotateTest implements Runnable {
	public CameraSphere cam;
	public GLJPanel glPanel;
	public boolean rotate = false;
	
	
	public void run() {
		// TODO Auto-generated method stub
		while (rotate)
		{
			this.cam.addTheta(Math.PI /25.0);
			this.cam.computeCartesianCoordinate();
			this.glPanel.display();
		}
	}

}
