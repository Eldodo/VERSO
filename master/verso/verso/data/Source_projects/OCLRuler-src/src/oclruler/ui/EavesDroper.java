package oclruler.ui;

import javax.swing.JTextPane;

public class EavesDroper extends JTextPane {
	private static final long serialVersionUID = -9209545678593842515L;

	public EavesDroper() {
		super();
		setEditable(false);
		setContentType("text/html");
	}
	
	@Override
	public void setText(String t) {
		if(t == null)
			super.setText("-null-");
		super.setText("<span style=\"font-family: Arial, Helvetica, sans-serif; font-size:70%;\"> "+t.replace("\n","<br>").replaceAll(" ", "&nbsp;")+"</span>");
	}
}
