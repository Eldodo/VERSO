package oclruler.ui;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

import org.fife.ui.rtextarea.RTextScrollPane;

import oclruler.genetics.Oracle;
import oclruler.genetics.OraculizationException;
import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.InvalidModelException;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.Model;
import oclruler.utils.Config;

public class ExampleEditionUi extends JFrame {
	private static final long serialVersionUID = -8863514019288943147L;
	
	String subtitle;

	OCLTextArea oclText;
	ExampleXMITextArea xmiView;
	JMenuItem validationMenuItem;
	
	ExampleButtonList buttonList;
	RTextScrollPane spOCL, spXMI;
	FragmentsPane fragmentsPane;
	Model selectedModel;
	
	TitledBorder oclBorder = BorderFactory.createTitledBorder("ocl");
	TitledBorder xmiBorder = BorderFactory.createTitledBorder("xmi");
	
	@SuppressWarnings("serial")
	public ExampleEditionUi(String subtitle) {
		super(Config.METAMODEL_NAME+" - "+subtitle + " (Debug)");
		
		JPanel panel = new JPanel(new BorderLayout());
		
		buttonList = new ExampleButtonList() {
			@Override
			public void selectModel(Model m) {
				setSelectedModel(m);
			}
		};
		updateExampleButtonTitles();
		
		
		oclText = new OCLTextArea();
		oclText.setCodeFoldingEnabled(false);
		
		
		
		
		spOCL = new RTextScrollPane(oclText);
		spOCL.setBorder(oclBorder);
		
		JTabbedPane jtp = new JTabbedPane(JTabbedPane.TOP);
		jtp.add("OCL", spOCL);
		jtp.add("Fragments", fragmentsPane = new FragmentsPane());
		
		
		
		xmiView = new ExampleXMITextArea(false);
		xmiView.setEditable(true);
		validationMenuItem = new JMenuItem("Validate changes");
		validationMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>(){
			         @Override
			         protected Void doInBackground() throws Exception {
			        	validateTextualChangesOnXMI();
			            return null;
			         }
			      };
				loadingDIalog(e, "Validating", mySwingWorker);
			}
		});
		validationMenuItem.setMnemonic('V');
		
		xmiView.getPopupMenu().add(validationMenuItem, 0);
		
		
		spXMI = new RTextScrollPane(xmiView);
		spXMI.setBorder(xmiBorder);
		
		JSplitPane jps = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, spXMI, jtp);
		jps.setDividerLocation(400);
		
		JButton jbUpdate = new JButton("Reload") ;
		jbUpdate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>(){
			         @Override
			         protected Void doInBackground() throws Exception {
			            reload();
			            return null;
			         }
			      };
				loadingDIalog(e, "Reloading", mySwingWorker);
			}
		});
		
		
		panel.add(jbUpdate, BorderLayout.NORTH);
		panel.add(jps, BorderLayout.CENTER);
		panel.add(buttonList, BorderLayout.WEST);
		add(panel);
		
		setSize(1280, 900);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println("ExampleEditionUi.ExampleEditionUi(...).new WindowListener() {...}.windowClosing()");
				System.out.println("   -> Check if current model text has been modified.");
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				
			}
		});
	}



	public void updateExampleButtonTitles() {
		double cov = Metamodel.fragmentSet.evaluateCoverage(ExampleSet.getInstance().getAllExamples());
		buttonList.setTitle("COV: "+cov);
	}
	
	
	protected void validateTextualChangesOnXMI() {
		String newXMI = xmiView.getText().trim();
		String oldXMI = xmiView.getXMI(selectedModel).trim();
		
		if(!newXMI.equals(oldXMI)){
			boolean rewrittingSUccess = selectedModel.rewriteXMI(newXMI);
			if(rewrittingSUccess){
				xmiView.xmis.put(selectedModel, newXMI);
				try {
					selectedModel.reload();
					
					ExampleSet.unleashConsideredExamples();//Ensure number of considered pos/neg remains coherent. //Attention couplage !
					Oracle.getInstance().oraculize();
					
					oclText.setProgram(Oracle.getInstance(), selectedModel);
					updateOCLBorder();
					
					updateXMITitle();

					fragmentsPane.setModel(selectedModel);
					
					spOCL.repaint();
					spXMI.repaint();
					
					repaintButtonsPane();
					
					repaint();
					
				} catch (InvalidModelException e) {
//					System.out.println("Popup: Invalid model modification !" + e.getMessage());

//					final JDialog dialog = new JDialog(this.getOwner(), "Attention, model invalid.", ModalityType.APPLICATION_MODAL);
//					dialog.setAlwaysOnTop(true);
//					dialog.setVisible(true);
//					JTextArea panel = new JTextArea("Invalid model modification !" + e.getMessage());
//					dialog.add(panel);
//					dialog.pack();
//					dialog.setLocationRelativeTo(this.getOwner());
//					dialog.setVisible(true);
					
					JOptionPane.showMessageDialog(null, "Invalid model modification !" + e.getMessage(), "Attention, model invalid.",
                            JOptionPane.ERROR_MESSAGE);
					// e.printStackTrace();
				} catch (OraculizationException e) {
					e.printStackTrace();
				}
			}
		}
	}



	public void updateXMITitle() {
		double cov = Metamodel.fragmentSet.evaluateCoverage(selectedModel);
		xmiBorder.setTitle(selectedModel.getName()+ "    cov:"+cov);
		spXMI.repaint();
	}



	public void repaintButtonsPane() {
		buttonList.updateButtons();
		updateExampleButtonTitles();
	}

	public void setSelectedModel(Model selectedModel) {
		this.selectedModel = selectedModel;
		
		fragmentsPane.setModel(selectedModel);
		
		xmiView.setModel(selectedModel);

		updateOCLBorder();
		oclText.setProgram(Oracle.getInstance(), selectedModel);

		updateXMITitle();

		spOCL.repaint();
		spXMI.repaint();
	}
	
	private void updateOCLBorder() {
		String title = Oracle.getInstance().getName() + "";
		if(selectedModel != null)
			title += " : " + selectedModel.getNumberOfOracleFires() + " fire" + (selectedModel.getNumberOfOracleFires() > 1 ? "s" : "");
		oclBorder.setTitle( title);
	}

	/**
	 */
	protected void reload() {
		ExampleSet.getInstance().reloadExamples();
		Oracle o = Oracle.instantiateOracle(ExampleSet.getInstance());
		try {
			o.oraculize();
		} catch (OraculizationException e1) {
			// e1.printStackTrace();
		}
		oclText.setProgram(Oracle.getInstance(), selectedModel);
		xmiView.reloadXMIs();
		repaintButtonsPane();
		updateXMITitle();
		if (selectedModel != null)
			setSelectedModel(selectedModel);
		
		repaint();
	}
	
	
	
	public void loadingDIalog(ActionEvent e, String title, SwingWorker<Void, Void> sw) {
	      java.awt.Window win = SwingUtilities.getWindowAncestor((AbstractButton)e.getSource());
	      final JDialog dialog = new JDialog(win, title, ModalityType.APPLICATION_MODAL);

	      sw.addPropertyChangeListener(new PropertyChangeListener() {
	         @Override
	         public void propertyChange(PropertyChangeEvent evt) {
	            if (evt.getPropertyName().equals("state")) {
	               if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
	                  dialog.dispose();
	               }
	            }
	         }
	      });
	      sw.execute();

	      JProgressBar progressBar = new JProgressBar();
	      progressBar.setIndeterminate(true);
	      JPanel panel = new JPanel(new BorderLayout());
	      panel.add(progressBar, BorderLayout.CENTER);
	      panel.add(new JLabel("Please wait......."), BorderLayout.PAGE_START);
	      dialog.add(panel);
	      dialog.pack();
	      dialog.setLocationRelativeTo(win);
	      dialog.setVisible(true);
	}
	

}
