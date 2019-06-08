package verso.representation.cubeLandscape.linkInterface;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JColorChooser;

import verso.graphics.primitives.PrimitiveColored;
import verso.representation.cubeLandscape.SceneLandscape;
import verso.representation.cubeLandscape.representationModel.SystemRepresentation;
import verso.representation.cubeLandscape.representationModel.link.EdgeBundleLinkRepresentation;
import verso.representation.cubeLandscape.representationModel.link.LinkRepresentation;

public class EdgeBundlesControlPanel extends SysRepControlPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	SceneLandscape sc;

	private HashMap<String, PrimitiveColored> meshes;
	
	private int meshSizePrecision = 1000;
	private int betaPrecision = 100;
	
    /** Creates new form EdgeBundlesControlPanel */
    public EdgeBundlesControlPanel(SceneLandscape sc, SystemRepresentation currSysRep, HashMap<String, PrimitiveColored> meshes) {
        super(currSysRep);
    	initComponents();
        
        this.sc = sc;
        
        this.meshes = meshes;
        Iterator<String> meshesItr = this.meshes.keySet().iterator();
        while (meshesItr.hasNext()) {
        	this.jcbLinkMesh.addItem(meshesItr.next());
        }
    }
    
    //private String panelName;
    
    public SceneLandscape getSc() {
    	return this.sc;
    }
    
    public void setSc(SceneLandscape sc) {
    	this.sc = sc;
    }
    
    public SystemRepresentation getCurrSysRep() {
    	return this.currSysRep;
    }
    
    public void setCurrSysRep(SystemRepresentation currSysRep) {
    	this.currSysRep = currSysRep;
    }    
    
    public int getMeshSizePrecision() {
    	return this.meshSizePrecision;
    }
    
    public void setMeshSizePrecision(int meshSizePrecision) {
    	this.meshSizePrecision = meshSizePrecision;
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jlblEndColorDisplay = new javax.swing.JLabel();
        jlblStartColorDisplay = new javax.swing.JLabel();
        jlblLinkMesh = new javax.swing.JLabel();
        jcbLinkMesh = new javax.swing.JComboBox<String>();
        jlblEndColor = new javax.swing.JLabel();
        jslMeshSize = new javax.swing.JSlider();
        jlblMeshSize = new javax.swing.JLabel();
        jlblBidirectionnalColor = new javax.swing.JLabel();
        jlblBidirectionnalColorDisplay = new javax.swing.JLabel();
        jlblStartColor = new javax.swing.JLabel();
        jlblBeta = new javax.swing.JLabel();
        jlblDegree = new javax.swing.JLabel();
        jlblNbreSegments = new javax.swing.JLabel();
        jslDegree = new javax.swing.JSlider();
        jslBeta = new javax.swing.JSlider();
        jslNbreSegments = new javax.swing.JSlider();
        jchbStraightenCP = new javax.swing.JCheckBox();
        jchbRemoveLCA = new javax.swing.JCheckBox();
        jchbVerticalPlanar = new javax.swing.JCheckBox();
        jchbHorizontalPlanar = new javax.swing.JCheckBox();

        setBackground(new java.awt.Color(255, 255, 255));

        jlblEndColorDisplay.setBackground(new java.awt.Color(255, 0, 0));
        jlblEndColorDisplay.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jlblEndColorDisplay.setOpaque(true);
        jlblEndColorDisplay.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jlblEndColorDisplayMouseClicked(evt);
            }
        });

        jlblStartColorDisplay.setBackground(new java.awt.Color(0, 255, 0));
        jlblStartColorDisplay.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jlblStartColorDisplay.setOpaque(true);
        jlblStartColorDisplay.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jlblStartColorDisplayMouseClicked(evt);
            }
        });

        jlblLinkMesh.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jlblLinkMesh.setText("Afficher avec :");

        jcbLinkMesh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbLinkMeshActionPerformed(evt);
            }
        });

        jlblEndColor.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jlblEndColor.setText("Couleur d'arriv�e :");

        jslMeshSize.setBackground(new java.awt.Color(255, 255, 255));
        jslMeshSize.setMaximum(1000);
        jslMeshSize.setMinimum(10);
        jslMeshSize.setOrientation(javax.swing.JSlider.VERTICAL);
        jslMeshSize.setPaintTicks(true);
        jslMeshSize.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jslMeshSizeMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jslMeshSizeMouseReleased(evt);
            }
        });
        jslMeshSize.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jslMeshSizeStateChanged(evt);
            }
        });

        jlblMeshSize.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jlblMeshSize.setText("Taille :");

        jlblBidirectionnalColor.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jlblBidirectionnalColor.setText("Couleur bidirectionnelle :");

        jlblBidirectionnalColorDisplay.setBackground(new java.awt.Color(204, 0, 204));
        jlblBidirectionnalColorDisplay.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jlblBidirectionnalColorDisplay.setOpaque(true);
        jlblBidirectionnalColorDisplay.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jlblBidirectionnalColorDisplayMouseClicked(evt);
            }
        });

        jlblStartColor.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jlblStartColor.setText("Couleur de d�part :");

        jlblBeta.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jlblBeta.setText("B�ta :");

        jlblDegree.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jlblDegree.setText("Degr�e courbe :");

        jlblNbreSegments.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jlblNbreSegments.setText("Nombre de segments :");

        jslDegree.setBackground(new java.awt.Color(255, 255, 255));
        jslDegree.setMaximum(10);
        jslDegree.setMinimum(1);
        jslDegree.setOrientation(javax.swing.JSlider.VERTICAL);
        jslDegree.setPaintTicks(true);
        jslDegree.setValue(3);
        jslDegree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jslDegreeMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jslDegreeMouseReleased(evt);
            }
        });
        jslDegree.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jslDegreeStateChanged(evt);
            }
        });

        jslBeta.setBackground(new java.awt.Color(255, 255, 255));
        jslBeta.setOrientation(javax.swing.JSlider.VERTICAL);
        jslBeta.setPaintTicks(true);
        jslBeta.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jslBetaMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jslBetaMouseReleased(evt);
            }
        });
        jslBeta.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jslBetaStateChanged(evt);
            }
        });

        jslNbreSegments.setBackground(new java.awt.Color(255, 255, 255));
        jslNbreSegments.setMaximum(250);
        jslNbreSegments.setMinimum(1);
        jslNbreSegments.setOrientation(javax.swing.JSlider.VERTICAL);
        jslNbreSegments.setPaintTicks(true);
        jslNbreSegments.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jslNbreSegmentsMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jslNbreSegmentsMouseReleased(evt);
            }
        });
        jslNbreSegments.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jslNbreSegmentsStateChanged(evt);
            }
        });

        jchbStraightenCP.setBackground(new java.awt.Color(255, 255, 255));
        jchbStraightenCP.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jchbStraightenCP.setText("Resserer les points de contr�les");
        jchbStraightenCP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jchbStraightenCPActionPerformed(evt);
            }
        });
        
        jchbRemoveLCA.setBackground(new java.awt.Color(255, 255, 255));
        jchbRemoveLCA.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jchbRemoveLCA.setText("Enlever le PPCA");
        jchbRemoveLCA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jchbRemoveLCAActionPerformed(evt);
            }
        });
        
        jchbVerticalPlanar.setBackground(new java.awt.Color(255, 255, 255));
        jchbVerticalPlanar.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jchbVerticalPlanar.setText("Aplanir verticalement");
        jchbVerticalPlanar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jchbVerticalPlanarActionPerformed(evt);
            }
        });
        
        jchbHorizontalPlanar.setBackground(new java.awt.Color(255, 255, 255));
        jchbHorizontalPlanar.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jchbHorizontalPlanar.setText("Aplanir horizontalement");
        jchbHorizontalPlanar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jchbHorizontalPlanarActionPerformed(evt);
            }
        });
        
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jlblMeshSize)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jslMeshSize, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jlblLinkMesh)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jcbLinkMesh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jlblStartColor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jlblStartColorDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jlblEndColor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jlblEndColorDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jlblBidirectionnalColor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jlblBidirectionnalColorDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jlblBeta)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jslBeta, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jlblDegree)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jslDegree, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jlblNbreSegments)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jslNbreSegments, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(57, 57, 57)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jchbVerticalPlanar)
                            .addComponent(jchbHorizontalPlanar)
                            .addComponent(jchbRemoveLCA)
                            .addComponent(jchbStraightenCP))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jlblMeshSize))
                    .addComponent(jslMeshSize, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jlblStartColorDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jlblStartColor)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jlblEndColorDisplay, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jlblEndColor)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jlblBidirectionnalColorDisplay, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jlblBidirectionnalColor)))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jlblLinkMesh)
                                .addComponent(jcbLinkMesh, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(54, 54, 54)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jlblBeta)
                            .addComponent(jlblDegree)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jslBeta, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jslDegree, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(9, 9, 9)
                                .addComponent(jlblNbreSegments))
                            .addComponent(jslNbreSegments, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jchbStraightenCP)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jchbRemoveLCA)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jchbVerticalPlanar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jchbHorizontalPlanar)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jlblStartColorDisplayMouseClicked(java.awt.event.MouseEvent evt) {
    	JColorChooser colorChooser = new JColorChooser();
    	colorChooser.setLocation(this.jlblStartColorDisplay.getX(), this.jlblStartColorDisplay.getY());
    	Color linkEndColor = colorChooser.showDialog(this, "Couleurs noeuds packages", this.jlblStartColorDisplay.getBackground());
    	
    	if (linkEndColor != null) { 		
        	this.jlblStartColorDisplay.setBackground(linkEndColor);
	        
        	Iterator<LinkRepresentation> linkItr = this.currSysRep.getLinks().iterator();
	        while (linkItr.hasNext()) {
	        	linkItr.next().setLinkStartColor(linkEndColor);
	        }
	        
	        this.sc.refreshScene();
    	}
    }
    
    private void jlblEndColorDisplayMouseClicked(java.awt.event.MouseEvent evt) {
    	JColorChooser colorChooser = new JColorChooser();
    	colorChooser.setLocation(this.jlblEndColorDisplay.getX(), this.jlblEndColorDisplay.getY());
    	Color linkEndColor = colorChooser.showDialog(this, "Couleurs noeuds packages", this.jlblEndColorDisplay.getBackground());
    	
    	if (linkEndColor != null) { 		
        	this.jlblEndColorDisplay.setBackground(linkEndColor);
	        
        	Iterator<LinkRepresentation> linkItr = this.currSysRep.getLinks().iterator();
	        while (linkItr.hasNext()) {
	        	linkItr.next().setLinkEndColor(linkEndColor);
	        }
	        
	        this.sc.refreshScene();
    	}
    }

    private void jlblBidirectionnalColorDisplayMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jlblBidirectionnalColorDisplayMouseClicked
    	JColorChooser colorChooser = new JColorChooser();
    	colorChooser.setLocation(this.jlblBidirectionnalColorDisplay.getX(), this.jlblBidirectionnalColorDisplay.getY());
    	Color linkEndColor = colorChooser.showDialog(this, "Couleurs noeuds packages", this.jlblBidirectionnalColorDisplay.getBackground());
    	
    	if (linkEndColor != null) { 		
        	this.jlblBidirectionnalColorDisplay.setBackground(linkEndColor);
	        
        	Iterator<LinkRepresentation> linkItr = this.currSysRep.getLinks().iterator();
	        while (linkItr.hasNext()) {
	        	linkItr.next().setLinkBidirectionalColor(linkEndColor);
	        }
	        
	        this.sc.refreshScene();
    	}
    }
    
    private void jcbLinkMeshActionPerformed(java.awt.event.ActionEvent evt) {
    	/*
    	PrimitiveColored coloredMesh = this.meshes.get(this.jcbLinkMesh.getSelectedItem());
		Iterator<LinkRepresentation> linkItr = this.currSysRep.getLinks().iterator();
		while (linkItr.hasNext()) {
			linkItr.next().setColoredMesh(coloredMesh);
		}
		
		this.sc.refreshScene();
		*/
    }

    private void jslMeshSizeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jslMeshSizeMousePressed
        // TODO add your handling code here:
    }

    private void jslMeshSizeMouseReleased(java.awt.event.MouseEvent evt) {
        Iterator<LinkRepresentation> linkItr = this.currSysRep.getLinks().iterator();
        LinkRepresentation currLink;
        float sliderValue = (float)this.jslMeshSize.getValue() / (float)this.meshSizePrecision;
        while (linkItr.hasNext()) {
        	currLink = linkItr.next();
        	currLink.setMeshSize(sliderValue/4);
        	currLink.setLineSize(sliderValue*10);
        }
        
        this.sc.refreshScene();
    }

    private void jslMeshSizeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jslMeshSizeStateChanged
        // TODO add your handling code here:
    }

    private void jslDegreeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jslDegreeMousePressed
        // TODO add your handling code here:
    }

    private void jslDegreeMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jslDegreeMouseReleased
        /*
    	Iterator<LinkRepresentation> linkItr = this.currSysRep.getLinks().iterator();
        while (linkItr.hasNext()) {
        	linkItr.next().setDegree(this.jslDegree.getValue());
        }
        
        this.sc.refreshScene();
        */
    }

    private void jslDegreeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jslDegreeStateChanged
        // TODO add your handling code here:
    }

    private void jslBetaMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jslBetaMousePressed
        // TODO add your handling code here:
    }

    private void jslBetaMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jslBetaMouseReleased
        Iterator<LinkRepresentation> linkItr = this.currSysRep.getLinks().iterator();
        float sliderValue = (float)this.jslBeta.getValue() / (float)this.betaPrecision;
        
        LinkRepresentation currLink;
        while (linkItr.hasNext()) {
        	currLink = linkItr.next();
        	if (currLink instanceof EdgeBundleLinkRepresentation) {
        		((EdgeBundleLinkRepresentation)currLink).setBeta(sliderValue);
        	}
        }
        
        this.sc.refreshScene();
    }

    private void jslBetaStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jslBetaStateChanged
        // TODO add your handling code here:
    	sc.refreshScene();
    }

    private void jslNbreSegmentsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jslNbreSegmentsMousePressed
        // TODO add your handling code here:
    	sc.refreshScene();
    }

    private void jslNbreSegmentsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jslNbreSegmentsMouseReleased
    	Iterator<LinkRepresentation> linkItr = this.currSysRep.getLinks().iterator();
        
    	LinkRepresentation currLink;
    	while (linkItr.hasNext()) {
    		currLink = linkItr.next();
    		
    		if (currLink instanceof EdgeBundleLinkRepresentation) {
    			((EdgeBundleLinkRepresentation)currLink).setNbreSegments(this.jslNbreSegments.getValue());
    		}
        }
        
        this.sc.refreshScene();
        
    }
    private void jslNbreSegmentsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jslNbreSegmentsStateChanged
        // TODO add your handling code here:
    }

    private void jchbStraightenCPActionPerformed(java.awt.event.ActionEvent evt) {
        Iterator<LinkRepresentation> linkItr = this.currSysRep.getLinks().iterator();
        
        LinkRepresentation currLink;
        while (linkItr.hasNext()) {
        	currLink = linkItr.next();
        	
        	if (currLink instanceof EdgeBundleLinkRepresentation) {
        		((EdgeBundleLinkRepresentation)currLink).setStraightenControlPoints(this.jchbStraightenCP.isSelected());
        	}
        }
        
        this.sc.refreshScene();
    }
    
    private void jchbRemoveLCAActionPerformed(java.awt.event.ActionEvent evt) {
        Iterator<LinkRepresentation> linkItr = this.currSysRep.getLinks().iterator();
        
        LinkRepresentation currLink;
        while (linkItr.hasNext()) {
        	currLink = linkItr.next();
        	
        	if (currLink instanceof EdgeBundleLinkRepresentation) {
        		((EdgeBundleLinkRepresentation)currLink).setRemoveLCA(this.jchbRemoveLCA.isSelected());
        	}
        }
        
        this.sc.refreshScene();
    }

    private void jchbVerticalPlanarActionPerformed(java.awt.event.ActionEvent evt) {
        /*
    	Iterator<EdgeBundleLinkRepresentation> edgeBundleLinkItr = this.currSysRep.getEdgeBundles().iterator();
        while (edgeBundleLinkItr.hasNext()) {
        	edgeBundleLinkItr.next().setVerticalPlanar(this.jchbVerticalPlanar.isSelected());
        }
        
        this.sc.refreshScene();
        */
    }

    private void jchbHorizontalPlanarActionPerformed(java.awt.event.ActionEvent evt) {
        /*
    	Iterator<EdgeBundleLinkRepresentation> edgeBundleLinkItr = this.currSysRep.getEdgeBundles().iterator();
        while (edgeBundleLinkItr.hasNext()) {
        	edgeBundleLinkItr.next().setHorizontalPlanar(this.jchbHorizontalPlanar.isSelected());
        }
        
        this.sc.refreshScene();
        */
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> jcbLinkMesh;
    private javax.swing.JCheckBox jchbHorizontalPlanar;
    private javax.swing.JCheckBox jchbRemoveLCA;
    private javax.swing.JCheckBox jchbStraightenCP;
    private javax.swing.JCheckBox jchbVerticalPlanar;
    private javax.swing.JLabel jlblBeta;
    private javax.swing.JLabel jlblBidirectionnalColor;
    private javax.swing.JLabel jlblBidirectionnalColorDisplay;
    private javax.swing.JLabel jlblDegree;
    private javax.swing.JLabel jlblEndColor;
    private javax.swing.JLabel jlblEndColorDisplay;
    private javax.swing.JLabel jlblLinkMesh;
    private javax.swing.JLabel jlblMeshSize;
    private javax.swing.JLabel jlblNbreSegments;
    private javax.swing.JLabel jlblStartColor;
    private javax.swing.JLabel jlblStartColorDisplay;
    private javax.swing.JSlider jslBeta;
    private javax.swing.JSlider jslDegree;
    private javax.swing.JSlider jslMeshSize;
    private javax.swing.JSlider jslNbreSegments;
    // End of variables declaration//GEN-END:variables

}
