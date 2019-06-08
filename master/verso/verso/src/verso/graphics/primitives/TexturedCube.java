package verso.graphics.primitives;

import javax.media.opengl.GL;

import verso.graphics.textures.SceneTexture;

public class TexturedCube extends Primitive{
	
	public void render(GL gl)
	{
		gl.glPushMatrix();
		SceneTexture.tl.bindTexture("google");
		gl.glBegin(GL.GL_QUADS);
			//top side
			gl.glNormal3f(0.0f, 1.0f, 0.0f);
			gl.glTexCoord2f(0.0f, 2.0f);gl.glVertex3f(-0.5f, 0.5f, 0.5f);
			gl.glTexCoord2f(0.0f, 0.0f);gl.glVertex3f(-0.5f, 0.5f, -0.5f);
			gl.glTexCoord2f(2.0f, 0.0f);gl.glVertex3f(0.5f, 0.5f, -0.5f);
			gl.glTexCoord2f(2.0f, 2.0f);gl.glVertex3f(0.5f, 0.5f, 0.5f);
			
		gl.glEnd();
		SceneTexture.tl.bindTexture("udem");
		gl.glBegin(GL.GL_QUADS);
			//front side
			gl.glNormal3f(0.0f, 0.0f, 1.0f);
			gl.glTexCoord2f(0.0f, 0.0f);gl.glVertex3f(-0.5f, 0.5f, 0.5f);
			gl.glTexCoord2f(0.0f, 1.0f);gl.glVertex3f(-0.5f, -0.5f, 0.5f);
			gl.glTexCoord2f(1.0f, 1.0f);gl.glVertex3f(0.5f, -0.5f, 0.5f);
			gl.glTexCoord2f(1.0f, 0.0f);gl.glVertex3f(0.5f, 0.5f, 0.5f);
		gl.glEnd();
		gl.glDisable(GL.GL_TEXTURE_2D);
		gl.glBegin(GL.GL_QUADS);
			//right side
			gl.glNormal3f(1.0f, 0.0f, 0.0f);
			gl.glVertex3f(0.5f, -0.5f, -0.5f);
			gl.glVertex3f(0.5f, -0.5f, 0.5f);
			gl.glVertex3f(0.5f, 0.5f, 0.5f);
			gl.glVertex3f(0.5f, 0.5f, -0.5f);
			
			//left side
			//gl.glColor3f(1.0f, 1.0f, 0.0f);
			gl.glNormal3f(-1.0f, 0.0f, 0.0f);
			gl.glVertex3f(-0.5f, -0.5f, -0.5f);
			gl.glVertex3f(-0.5f, -0.5f, 0.5f);
			gl.glVertex3f(-0.5f, 0.5f, 0.5f);
			gl.glVertex3f(-0.5f, 0.5f, -0.5f);
			
			//back side
			//gl.glColor3f(0.0f, 1.0f, 1.0f);
			gl.glNormal3f(0.0f, 0.0f, -1.0f);
			gl.glVertex3f(-0.5f, 0.5f, -0.5f);
			gl.glVertex3f(-0.5f, -0.5f, -0.5f);
			gl.glVertex3f(0.5f, -0.5f, -0.5f);
			gl.glVertex3f(0.5f, 0.5f, -0.5f);
		gl.glEnd();
	gl.glPopMatrix();
	}
	
	public String getName()
	{
		return "TexturedCube";
	}

	
	public String getSimpleName() {
		// TODO Auto-generated method stub
		return getName();
	}

}
