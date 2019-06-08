package oclruler.ui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;

import org.fife.ui.rtextarea.RTextScrollPane;

import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.Model;

public class ExampleSetExplorer extends JPanel {
	private static final long serialVersionUID = -6724035522422368451L;
	
	
	public final static Color  colorValid = new Color((int)( 0x083600));
	public final static String colorValidStr = "#083600";
	public final static Color  colorInvalid = new Color((int)( 0xa50606));
	public final static String colorInvalidStr = "#a50606";
	
	public final static Color  colorRight =  new Color((int)( 0xdff0d8));
	public final static String colorRightStr = "#dff0d8";
	public final static Color  colorWrong =  new Color((int)( 0xf0d8d8));
	public final static String colorWrongStr = "#f0d8d8";
	public final static Color  colorUndecided =  new Color((int)( 0xf0d8d8));
	public final static String colorUndecidedStr = "#f0d8d8";

	
	
	ExampleButtonList buttonList;
	ExampleXMITextArea xmiView;
	
	
	@SuppressWarnings("serial")
	public ExampleSetExplorer() {
		super(new BorderLayout());
		

		buttonList = new ExampleButtonList() {
			@Override
			public void selectModel(Model m) {
				xmiView.setModel(m);
			}
		};
		
		
		xmiView = new ExampleXMITextArea();
		
		RTextScrollPane sp = new RTextScrollPane(xmiView);
		
		add(sp, BorderLayout.CENTER);
		add(buttonList, BorderLayout.WEST);
		
		buttonList.selectModel(ExampleSet.getExamplesBeingUsed().get(0));
	}

	
	public void selectExample(Model m){
		buttonList.selectModel(m);
	}
}
