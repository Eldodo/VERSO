package verso.graphics.textures;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.PixelGrabber;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL;

public class TextureLoader {
	Map<String,Integer> texturemap = new HashMap<String,Integer>();
	int[] textures;
	GL gl;
	int numberOfTexture = 0;
	
	public TextureLoader(int maxTextureNumber, GL gl)
	{
		textures = new int[maxTextureNumber];
		this.gl = gl;
		texturemap.clear();
	}

	public void loadTexture(String image, String textureName)
	{
		int width = -1;
		int height = -1;
		
		int red = 0;
		int green = 0;
		int blue = 0;
		
		if (numberOfTexture == textures.length)
			return;
		
		
		
		
		
		
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Image img = toolkit.getImage(image);
		while (width == -1 || height == -1)
		{
			width = img.getWidth(null);
			height = img.getHeight(null);
		}
		
		int[] pixels = new int[width*height];
		PixelGrabber pg = new PixelGrabber(img, 0, 0, img.getWidth(null), img.getHeight(null), pixels, 0, width);
		try{
		pg.grabPixels();
		}
		catch(Exception e){System.out.println(e);}
		
		gl.glGenTextures(numberOfTexture+1, textures, numberOfTexture);
		gl.glBindTexture(GL.GL_TEXTURE_2D, numberOfTexture+1);
		numberOfTexture++;
		
		texturemap.put(textureName, numberOfTexture);
		
		byte[] b = new byte[width*height*3];
		
		for (int i = 0,j = 0; i < b.length;j++ )
		{
			red   = (pixels[j] >> 16) & 0xff;
			green = (pixels[j] >>  8) & 0xff;
			blue  = (pixels[j]      ) & 0xff;
			b[i++] = (byte)red;
			b[i++] = (byte)green;
			b[i++] = (byte)blue;

		}
		
		//System.out.println(textures[0]);
		gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
	    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, 3, width, height, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap(b));
		
	}
	
	public void removeAllTextures()
	{
		texturemap.clear();
		gl.glDeleteTextures(numberOfTexture,textures,0);
		for (int i =0 ; i < textures.length; i++)
		{
			textures[i] = 0;
		}
		numberOfTexture = 0;
	}
	public int getCapacity()
	{
		return this.textures.length;
	}
	
	public int getSize()
	{
		return this.numberOfTexture;
	}
	
	public void bindTexture(String name)
	{
		gl.glBindTexture(GL.GL_TEXTURE_2D, texturemap.get(name));
	}
	
	
}
