package oclruler.ui;

import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.Model;
import oclruler.utils.ToolBox;

public class ExampleXMITextArea extends RSyntaxTextArea {
	private static final long serialVersionUID = -2974656056017239284L;
	
	TitledBorder border = BorderFactory.createTitledBorder("XMI");
	
	Model selectedModel;
	HashMap<Model, String> xmis;

	public ExampleXMITextArea() {
		this(true);
	}
	
	public ExampleXMITextArea(boolean withBorder) {
		super();
		xmis = ToolBox.readXMIs(ExampleSet.getInstance().getAllExamples());
		setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
		setCodeFoldingEnabled(true);
		setEditable(false);
		if(withBorder)
			setBorder(border);
		
		
		// Ajout bouton d'affichage du coverage
//		JMenuItem validationMenuItem = new JMenuItem("Validate changes");
//		validationMenuItem.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				if(selectedModel != null){
//					System.out.println(selectedModel.getCoverage());
//				}
//			}
//		});
//		validationMenuItem.setMnemonic('V');
//		getPopupMenu().add(validationMenuItem, 0);
	}
	
	public void setTitle(String title) {
		border.setTitle(title);
	}

	public void setModel(Model m) {
		this.selectedModel = m;
		setText(xmis.get(m));
		setTitle(m.getName());
		setCaretPosition(0);
		updateUI();
	}
	public void reloadXMIs() {
		xmis = ToolBox.readXMIs(ExampleSet.getInstance().getAllExamples());
		if(selectedModel != null)
			setModel(selectedModel);
	}

	public String getXMI(Model selectedModel2) {
		return xmis.get(selectedModel2);
	}
}
