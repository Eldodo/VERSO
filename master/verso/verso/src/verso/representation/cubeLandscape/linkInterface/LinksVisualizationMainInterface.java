package verso.representation.cubeLandscape.linkInterface;

import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.JPanel;

import verso.representation.cubeLandscape.SceneLandscape;
import verso.representation.cubeLandscape.representationModel.SystemRepresentation;

public class LinksVisualizationMainInterface extends javax.swing.JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private SceneLandscape sc;
	private HashMap<String, SystemRepresentation> sysreps;
	private SystemRepresentation currSysRep;
	private SysRepControlPanel nodesControlPanel;
	private HashMap<String, SysRepControlPanel> linksTypeControlPanels;
	private JPanel currLinksTypeControlPanels;
	
    /** Creates new form LinksVisualizationInterface */
    public LinksVisualizationMainInterface(SceneLandscape sc, HashMap<String, SystemRepresentation> sysreps, SystemRepresentation chosen, SysRepControlPanel nodesControlPanel, HashMap<String, SysRepControlPanel> linksTypeControlPanels) {
    	initComponents();
    	
    	this.sc = sc;
    	this.sysreps = sysreps;
    	this.nodesControlPanel = nodesControlPanel;
    	this.linksTypeControlPanels = linksTypeControlPanels;    	
    	
    	
    	for (String sysRepStr : sysreps.keySet()) 
    		this.jcbSystem.addItem(sysRepStr);
    	this.currSysRep = chosen;
    	
    	for (String linkType : linksTypeControlPanels.keySet()) 
    		this.jcbLinksType.addItem(linkType);
    	this.currLinksTypeControlPanels = this.linksTypeControlPanels.values().iterator().next();
    	
    	
    	this.sc.clearRenderable();
    	this.sc.addRenderable(this.currSysRep);
    	this.sc.setSysRep(currSysRep);

    	setNodesPanel(this.nodesControlPanel);
    	setLinksPanel(this.currLinksTypeControlPanels); 
    	
    	sc.refreshScene();
    }

    
    
    
    public void setNodesPanel(JPanel nodesPanel) {
        this.jpNodesControlPanel.removeAll();
        this.jpNodesControlPanel.add(nodesPanel);
        this.jpNodesControlPanel.validate();
        this.repaint();
    }

    public void setLinksPanel(JPanel linksPanel) {
        this.jpLinksControlPanel.removeAll();
        this.jpLinksControlPanel.add(linksPanel);
        this.jpLinksControlPanel.validate();
        this.repaint();
    }



    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jpSystemControlPanel = new javax.swing.JPanel();
        jlblSystem = new javax.swing.JLabel();
        jcbSystem = new javax.swing.JComboBox<String>();
        jcbSystem.setPreferredSize(new Dimension(10, 100));
        jlblLinksType = new javax.swing.JLabel();
        jcbLinksType = new javax.swing.JComboBox<String>();
        jpMainInterfaceControlPanel = new javax.swing.JPanel();
        jpLinksControlPanel = new javax.swing.JPanel();
        jpNodesControlPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jpSystemControlPanel.setBackground(new java.awt.Color(255, 255, 255));
        jpSystemControlPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jlblSystem.setFont(new java.awt.Font("Tahoma", 1, 12));
        jlblSystem.setText("Systême :");

        jcbSystem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbSystemActionPerformed(evt);
            }
        });

        jlblLinksType.setFont(new java.awt.Font("Tahoma", 1, 12));
        jlblLinksType.setText("Type de liens :");

        jcbLinksType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbLinksTypeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jpSystemControlPanelLayout = new javax.swing.GroupLayout(jpSystemControlPanel);
        jpSystemControlPanel.setLayout(jpSystemControlPanelLayout);
        jpSystemControlPanelLayout.setHorizontalGroup(
            jpSystemControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpSystemControlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jlblSystem)
                .addGap(4, 4, 4)
                .addComponent(jcbSystem, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jlblLinksType)
                .addGap(10, 10, 10)
                .addComponent(jcbLinksType, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jpSystemControlPanelLayout.setVerticalGroup(
            jpSystemControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpSystemControlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpSystemControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpSystemControlPanelLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(jlblSystem))
                    .addComponent(jcbSystem, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jpSystemControlPanelLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(jlblLinksType))
                    .addComponent(jcbLinksType, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jpMainInterfaceControlPanel.setBackground(new java.awt.Color(255, 255, 255));
        jpMainInterfaceControlPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jpMainInterfaceControlPanel.setLayout(new java.awt.GridBagLayout());

        jpLinksControlPanel.setBackground(new java.awt.Color(255, 255, 255));
        jpLinksControlPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jpLinksControlPanel.setLayout(new javax.swing.BoxLayout(jpLinksControlPanel, javax.swing.BoxLayout.LINE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 911;
        gridBagConstraints.ipady = 42;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 13, 12);
        jpMainInterfaceControlPanel.add(jpLinksControlPanel, gridBagConstraints);

        jpNodesControlPanel.setBackground(new java.awt.Color(255, 255, 255));
        jpNodesControlPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jpNodesControlPanel.setLayout(new javax.swing.BoxLayout(jpNodesControlPanel, javax.swing.BoxLayout.LINE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 911;
        gridBagConstraints.ipady = 42;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 12);
        jpMainInterfaceControlPanel.add(jpNodesControlPanel, gridBagConstraints);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jpSystemControlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addComponent(jpMainInterfaceControlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jpSystemControlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jpMainInterfaceControlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jcbSystemActionPerformed(java.awt.event.ActionEvent evt) {
    	this.currSysRep = this.sysreps.get(this.jcbSystem.getSelectedItem());
		
    	this.nodesControlPanel.setCurrSysRep(this.currSysRep);
    	
    	for (SysRepControlPanel lcp : this.linksTypeControlPanels.values()) {
			lcp.setCurrSysRep(this.currSysRep);
		}	
    	
    	sc.clearRenderable();
    	sc.addRenderable(this.currSysRep);
		sc.setSysRep(this.currSysRep);
		sc.setCurrentRoot(currSysRep.getPackages().iterator().next());
		
		sc.refreshScene();
		//sc.redisplay();
		//sc.repaint();
    }

    private void jcbLinksTypeActionPerformed(java.awt.event.ActionEvent evt) {
        setLinksPanel(this.linksTypeControlPanels.get(this.jcbLinksType.getSelectedItem().toString()));
    	
        /*
    	if (this.jcbLinksType.getSelectedItem().toString() == "Liens directs") {
    		this.currSysRep.setDisplayDirectLinks(true);
        	this.currSysRep.setDisplayEdgeBundles(false);
        	this.currSysRep.setDisplayHierarchicalLinks(false);
        }
        else if (this.jcbLinksType.getSelectedItem().toString() == "Edge Bundles") {
        	this.currSysRep.setDisplayDirectLinks(false);
        	this.currSysRep.setDisplayEdgeBundles(true);
        	this.currSysRep.setDisplayHierarchicalLinks(false);
        }
        else if (this.jcbLinksType.getSelectedItem().toString() == "Liens hiérarchiques") {
        	this.currSysRep.setDisplayDirectLinks(false);
        	this.currSysRep.setDisplayEdgeBundles(false);
        	this.currSysRep.setDisplayHierarchicalLinks(true);
        }
    	*/
        
    	this.sc.refreshScene();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> jcbLinksType;
    private javax.swing.JComboBox<String> jcbSystem;
    private javax.swing.JLabel jlblLinksType;
    private javax.swing.JLabel jlblSystem;
    private javax.swing.JPanel jpLinksControlPanel;
    private javax.swing.JPanel jpMainInterfaceControlPanel;
    private javax.swing.JPanel jpNodesControlPanel;
    private javax.swing.JPanel jpSystemControlPanel;
    // End of variables declaration//GEN-END:variables

}
