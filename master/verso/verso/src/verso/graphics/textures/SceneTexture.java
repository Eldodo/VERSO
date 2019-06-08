package verso.graphics.textures;

import java.awt.Dimension;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.JFrame;

import verso.graphics.VersoScene;
import verso.graphics.primitives.TexturedCube;

public class SceneTexture extends VersoScene{
	byte[] b = new byte[9*9*3];
	public static int[] textures = new int[1];
	public static TextureLoader tl;
	
	public void init(GLAutoDrawable glauto)
	{
		GL gl = glauto.getGL();
		super.init(glauto);
		/*
		int color = 0;
		for (int i = 0; i < b.length; )
		{
			color = 1- color;
			b[i++] = (byte)(0);
			b[i++] = (byte)(color*250);
			b[i++] = (byte)((1-color)*250);
		}
		
		GL gl = glauto.getGL();
		gl.glGenTextures(1, textures,0);
		System.out.println(textures[0]);
		gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
	    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, 3, 9, 9, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap(b));
		*/
		tl = new TextureLoader(10,gl);
		tl.loadTexture("C:\\Documents and Settings\\langelig\\Bureau\\nav_logo4.png", "google");
		tl.loadTexture("C:\\Documents and Settings\\langelig\\Bureau\\Im_centre_dec_2008_verglas_2.jpg", "udem");
		tl.bindTexture("google");
		gl.glEnable(GL.GL_TEXTURE_2D);
		
	}
	
	public static void main(String[] args)
	{
		VersoScene sc = new SceneTexture();
		sc.addRenderable(new TexturedCube());
		
		JFrame jf = new JFrame();
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setPreferredSize(new Dimension(1000,1000));
		jf.setSize(new Dimension(1200,900));
		jf.add(sc.getContainner());
		jf.setVisible(true);	
	}
}
