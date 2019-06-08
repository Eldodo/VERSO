package verso.view.mapping;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import verso.model.Element;
import verso.model.Entity;
import verso.model.Method;
import verso.model.Package;
import verso.model.metric.Metric;
import verso.representation.cubeLandscape.SceneLandscape;
import verso.representation.cubeLandscape.SceneLandscape.MappingListener;
import verso.representation.cubeLandscape.representationModel.repvisitor.Mapping;
import verso.representation.cubeLandscape.representationModel.repvisitor.MappingVisitor;

public class MappingModifier extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//Package
	List<JComboBox<String>> PackageCombo = new ArrayList<>();
	//Class
	List<JComboBox<String>> ClasseCombo = new ArrayList<>();
	//Met
	List<JComboBox<String>> MethodCombo = new ArrayList<>();
	Map<String,MappingVisitor> lst;
	JComboBox<String> jcb = null;
	ComboListener cl = new ComboListener();
	SceneLandscape sc = null;
	MappingListener mapLis = null;
	
	public MappingModifier(Package metRepPac, Element metRepElem, Method metRepMet, Map<String, MappingVisitor> lst,
			SceneLandscape sc, MappingListener mapLis) {
		this.mapLis = mapLis;
		this.sc = sc;
		this.lst = lst;
		JPanel topPan = new JPanel();
		JPanel middlePan = new JPanel();
		JPanel pankage = new JPanel();
		JPanel panClasse = new JPanel();
		JPanel panMethode = new JPanel();
		this.setLayout(new BorderLayout());
		this.add(topPan, BorderLayout.NORTH);
		this.add(middlePan, BorderLayout.CENTER);
		middlePan.setLayout(new GridLayout(3, 1));
		middlePan.add(pankage);
		middlePan.add(panClasse);
		middlePan.add(panMethode);
		this.setpankage(pankage, metRepPac);
		this.setPanClasse(panClasse, metRepElem);
		this.setPanMethod(panMethode, metRepMet);
		this.setPreferredSize(new Dimension(500, 500));
		this.setSize(new Dimension(500, 500));
		this.setTopPan(topPan, lst);
		this.setVisible(true);
	}

	private void setpankage(JPanel pan, Package metRepPac) {
		pan.setBorder(BorderFactory.createTitledBorder("Pakcages"));
		this.setPan(pan, metRepPac, PackageCombo);
	}

	private void setPanClasse(JPanel pan, Element metRepElem) {
		pan.setBorder(BorderFactory.createTitledBorder("Classes"));
		this.setPan(pan, metRepElem, ClasseCombo);
	}

	private void setPanMethod(JPanel pan, Method metRepMet) {
		pan.setBorder(BorderFactory.createTitledBorder("Methods"));
		this.setPan(pan, metRepMet, MethodCombo);
	}
	
	private void setTopPan(JPanel topPan, Map<String, MappingVisitor> lst) {
		topPan.setLayout(new GridLayout(1, 3));
		jcb = new JComboBox<>();
		jcb.setEditable(true);
		jcb.addActionListener(cl);
		for (String s : lst.keySet()) {
			jcb.addItem(s);
		}
		topPan.add(jcb);
		// topPan.add(new JLabel(""));
		JButton saveBut = new JButton("Save");
		saveBut.addActionListener(new SaveListener());
		topPan.add(saveBut);
		// topPan.add(new JLabel(""));
		JButton removeBut = new JButton("Remove");
		removeBut.addActionListener(new RemoveListener());
		topPan.add(removeBut);
	}
	
	private void setPan(JPanel pan, Entity metRep, List<JComboBox<String>> panList) {
		String[] mets = new String[metRep.getMetrics().size()];
		int i = 0;
		for (Metric<?> m : metRep.getMetrics()) {
			mets[i++] = m.getName();
		}
		pan.setLayout(new GridLayout(4, 2));
		// header
		pan.add(new JLabel("Graphical Caracteristic"));
		pan.add(new JLabel("Metric"));
		for (int j = 0; j < 6; j++) {
			JComboBox<String> tempCombo = new JComboBox<>();
			panList.add(tempCombo);
			pan.add(tempCombo);
		}
		for (int j = 0; j < 6; j = j + 2) {
			JComboBox<String> comb = panList.get(j);
			comb.addItem("Color");
			comb.addItem("ViascoColor");
			comb.addItem("Height");
			comb.addItem("Twist");
			comb = panList.get(j + 1);
			for (String s : mets) {
				comb.addItem(s);
			}

		}

	}
	
	private class SaveListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			String currentSelection = (String) jcb.getSelectedItem();
			MappingVisitor mv = new MappingVisitor((String) jcb.getSelectedItem());
			for (int i = 0; i < 3; i++) {
				String selectedItemPac = (String) PackageCombo.get(i * 2).getSelectedItem();
				if (selectedItemPac.compareTo("") != 0) {
					mv.addPackageMapping(
							new Mapping((String) PackageCombo.get(i * 2 + 1).getSelectedItem(), selectedItemPac));
				}
				String selectedItemCls = (String) ClasseCombo.get(i * 2).getSelectedItem();
				if (selectedItemCls.compareTo("") != 0) {
					mv.addClassMapping(
							new Mapping((String) ClasseCombo.get(i * 2 + 1).getSelectedItem(), selectedItemCls));
				}
				String selectedItemMet = (String) MethodCombo.get(i * 2).getSelectedItem();
				if (selectedItemMet.compareTo("") != 0) {
					mv.addMethodMapping(
							new Mapping((String) MethodCombo.get(i * 2 + 1).getSelectedItem(), selectedItemMet));
				}
			}
			lst.put(mv.getName(), mv);
			jcb.removeActionListener(cl);
			jcb.removeAllItems();
			for (String mvs : lst.keySet()) {
				jcb.addItem(mvs);
			}
			jcb.addActionListener(cl);
			jcb.setSelectedItem(currentSelection);
			sc.modifyMappings(lst);
		}
	}

	private class RemoveListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			lst.remove((String) jcb.getSelectedItem());
			jcb.removeItem(jcb.getSelectedItem());
			sc.modifyMappings(lst);

		}
	}

	private class ComboListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			@SuppressWarnings("unchecked")
			JComboBox<String> comb = (JComboBox<String>) e.getSource();
			String selectedItem = (String) comb.getSelectedItem();
			System.out.println("ComboListener: "+selectedItem);
			MappingVisitor vis = lst.get(selectedItem);
			if (vis != null) {
				fill(vis.getPackageMapping(), PackageCombo);
				fill(vis.getClassMapping(), ClasseCombo);
				fill(vis.getMethodMapping(), MethodCombo);
			}

		}

		private void fill(List<Mapping> mappinglist, List<JComboBox<String>> lstCombo) {
			for (int i = 0; i < 3; i++) {
				if (mappinglist.size() > i) {
					lstCombo.get(i * 2).setSelectedItem(mappinglist.get(i).getGvalue());
					lstCombo.get(i * 2 + 1).setSelectedItem(mappinglist.get(i).getMetric());
				} else {
					lstCombo.get(i * 2).setSelectedItem("");
				}
			}
		}
	}
	
	
}
