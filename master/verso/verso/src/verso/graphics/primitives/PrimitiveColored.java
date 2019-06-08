package verso.graphics.primitives;

import java.awt.Color;

public abstract class PrimitiveColored extends Primitive {
	public abstract Color getBaseColor();
	public abstract void setBaseColor(Color baseColor);
	public abstract Color getTopColor();
	public abstract void setTopColor(Color topColor);
}
