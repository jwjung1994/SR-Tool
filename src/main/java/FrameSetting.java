

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.sql.rowset.RowSetWarning;
import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.border.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.jfree.ui.RefineryUtilities;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;

//import SubFrameSetting.WordWrapCellRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class FrameSetting extends JFrame {
	String [] softwares, techniques, hardwares;
	
	JLabel status, conlabel1, conlabel2, conlabel3;
	JLabel subconsw1, subconsw2, subconsw3;
	JLabel subcontech1, subcontech2, subcontech3;
	JLabel subconscore1, subconscore2, subconscore3;
	
	JTable sw_table, tactic_table, scenario_table, template_table, calc_table;  //, contents_table
	JComboBox tech_cbo1, tech_cbo2;
	
	JComboBox sw_cbo;
	JComboBox em_position, system_asset, asset_category, contents;
	
	HashMap<String, String> CheckedList = new HashMap<String, String>();
	
	JButton submit_btn;
	JButton SR_btn;
	
	Toolkit toolkit = Toolkit.getDefaultToolkit();
    Image img = toolkit.getImage("img/logo.png");

    /*
	class Logopanel extends JPanel{
		public void paint(Graphics g) {
	    	g.drawImage(img, 0, 0, null);
	    }	
	}
    */
    
	class key implements KeyListener{ 
		@Override
		public void keyTyped(KeyEvent e) {	}

		@Override
		public void keyPressed(KeyEvent e) {
			//System.out.println(e.getKeyCode());
			if(e.getKeyCode()==127) {
			   DefaultTableModel model;
			   int[] rows;
			   
			   rows = sw_table.getSelectedRows();
			   if(rows.length != 0) {
				   model = (DefaultTableModel) sw_table.getModel();
				   for(int i=0;i<rows.length;i++){
					   model.removeRow(rows[i]-i);
				   }					   
			   }	
			   rows = tactic_table.getSelectedRows();
			   if(rows.length != 0) {
				   model = (DefaultTableModel) tactic_table.getModel();
				   for(int i=0;i<rows.length;i++){
					   model.removeRow(rows[i]-i);
				   }					   
			   }		   
			}
		}

		@Override
		public void keyReleased(KeyEvent e) { }  
	}  		
	
	// 선택한 콤보박스 값을 테이블로 옮기기
	public void InsertItem(JComboBox cbx, JTable tbl) {
		String item= (String)cbx.getSelectedItem();
		DefaultTableModel model = (DefaultTableModel) tbl.getModel();
		model.addRow(new Object[]{item});
	}

	public void InsertItem(JComboBox cbx1, JComboBox cbx2, JTable tbl) {
		String item1 = (String)cbx1.getSelectedItem();
		String item2 = (String)cbx2.getSelectedItem();
		DefaultTableModel model = (DefaultTableModel) tbl.getModel();
		model.addRow(new Object[]{item1, item2});
	}
	
	public void ChangeStatus(boolean status_value) {
		if(status_value) {
			status.setText("Complete Ontology Load.");
		}
		else {
			status.setText("Failed Ontology Load.");
		}
	}
	
	// 초기 콤보박스에 항목들 삽입
	public void InsertVec(List<String> list, JComboBox cbo) {
		for(int i = 0; i < list.size(); i++) {
			String list_item = list.get(i);
			cbo.addItem(list_item);
		}	
	}

	public ArrayList<String> GetColData(JTable table, int col) {
		TableModel model = table.getModel();
		ArrayList<String> list = new ArrayList<String>();
		for(int idx = 0; idx < model.getRowCount(); idx++) {
			list.add(model.getValueAt(idx, col).toString());
		}
		return list;
	}
	
	public double calcJaccardSimilarity(List<String> a, List<String> b){
		double similarity = 0;
	    //합집합
		List add_result = new ListUtil().union(a, b);
	    
	    //교집합
		List retain_result = new ListUtil().intersection(a, b);
	
	    if(add_result.size() == 0)
	    	similarity = 0;
	    else
	    	similarity = (double)retain_result.size() / (double)add_result.size();  
	    return similarity;
	}	
	
	public void MakeRadarGraph(OntologyFunc owl, JTable Inputtable, String group){
		ArrayList<String[]> grouptechs = owl.LoadTacticsfromTechList(group); //?a_l만 사용하기
		DefaultTableModel model = (DefaultTableModel)Inputtable.getModel();
		HashMap<String, Double> group_tactic_statistic = new HashMap<>();
		HashMap<String, Double> input_tactic_statistic = new HashMap<>();
		/*
		List<String> input_tech_list = GetColData(Inputtable, 1);
		List<String> group_tech_list = new ArrayList<String>(); 
		
		List retain_result = new ListUtil().intersection(input_tech_list, group_tech_list);
		
		for(int i = 0; i < grouptechs.size(); i++) {
			group_tech_list.add(grouptechs.get(i)[0]);
		}
		*/
		
		for(int i = 0; i < grouptechs.size(); i++) {	// dictionary of Used Tactics by APT Group
			String tactic_name = grouptechs.get(i)[1];
			if(group_tactic_statistic.containsKey(tactic_name)) {
				double value = group_tactic_statistic.get(tactic_name);
				group_tactic_statistic.put(tactic_name, ++value);
			}
			else {
				group_tactic_statistic.put(tactic_name, 1.0);
			}
		}
		

		for(int i = 0; i < model.getRowCount(); i++) {	// dictionary of Added Tactics by Input Panel
			String tactic_name = model.getValueAt(i, 0).toString();
			//System.out.println("인풋 테이플 전술이름 : " + tactic_name);
	    	switch(tactic_name) {
    		case "Reconnaissance":
    			tactic_name = "TA0043:Reconnaissance";
    			break;
    		case "Resource Development":
    			tactic_name = "TA0042:Resource Development";
    			break;
    		case "Initial Access":
    			tactic_name = "TA0001:Initial Access";
    			break;
    		case "Execution":
    			tactic_name = "TA0002:Execution";
    			break;
    		case "Persistence":
    			tactic_name = "TA0003:Persistence";
    			break;
    		case "Privilege Escalation":
    			tactic_name = "TA0004:Privilege Escalation";
    			break;
    		case "Defense Evasion":
    			tactic_name = "TA0005:Defense Evasion";
    			break;
    		case "Credential Access":
    			tactic_name = "TA0006:Credential Access";
    		case "Discovery":
    			tactic_name = "TA0007:Discovery";
    			break;
    		case "Lateral Movement":
    			tactic_name = "TA0008:Lateral Movement";
    		case "Collection":
    			tactic_name = "TA0009:Collection";
    			break;
    		case "Command and Control":
    			tactic_name = "TA0011:Command and Control";
    			break;
    		case "Exfiltration":
    			tactic_name = "TA0010:Exfiltration";
    			break;
    		case "Impact":
    			tactic_name = "TA0040:Impact";
    			break;
	    	}
	    	//System.out.println("변경 후 이름 : " + tactic_name);
			if(input_tactic_statistic.containsKey(tactic_name)) {
				double value = input_tactic_statistic.get(tactic_name);
				input_tactic_statistic.put(tactic_name, ++value);
			}
			else {
				input_tactic_statistic.put(tactic_name, 1.0);
			}
		}
		
		HashMap<String, Double> calc_each_tactic = new HashMap<>();
		double value;
		
		
		Set set = group_tactic_statistic.keySet();
		Iterator iterator = set.iterator();
		/* 초기 버전
		while(iterator.hasNext()) {
			String key = (String)iterator.next();
			int y = group_tactic_statistic.get(key);
			//System.out.println("y 값 : " + y);
			int x;
			if(!input_tactic_statistic.containsKey(key))
				x = 0;
			else {
				x = input_tactic_statistic.get(key);
			}
			//System.out.println("x 값 : " + x);
			value = (double) x / y;
			//System.out.println("최종 계산값 :" + value);
			calc_each_tactic.put(key, value);
		}
		*/	
		while(iterator.hasNext()) {
			String key = (String)iterator.next();
			double y = group_tactic_statistic.get(key);
			//System.out.println("y 값 : " + y);
			double x;
			if(!input_tactic_statistic.containsKey(key))
				x = 0;
			else {
				x = input_tactic_statistic.get(key);
			}
			//System.out.println("x 값 : " + x);
			value = (double) x / y;
			//System.out.println("최종 계산값 :" + value);
			calc_each_tactic.put(key, value);
		}
		
		//new RadarChart("Chart of " + group, calc_each_tactic);
		new RadarChart("Chart of " + group, "Input", group_tactic_statistic, input_tactic_statistic);
        //demo.pack();
        //RefineryUtilities.centerFrameOnScreen(demo);
        //demo.setVisible(true);
		
	}
	public HashMap<String, Double> CaseRetrieval2(OntologyFunc owl, List<String> tech) {          //weight similarity
		List<String> group = owl.LoadAttackGroups();
		HashMap<String, Integer> Input_Dictionary = new HashMap<>();
		HashMap<String, Integer> Group_Dictionary = new HashMap<>();		
		int j = 0;
		HashMap<String, Double> total_weight_map = new HashMap<>();
		//인풋 technique list 사전 만들기
		for(int i = 0; i < tech.size(); i++) {
			Input_Dictionary.put(tech.get(i), j++);
		}
		//System.out.println(Input_Dictionary);
		j = 0;
		
		for(int g = 0; g < group.size(); g++) {
			// 하나의 그룹의 techniques
			List<String> techniques = owl.LoadTechniquesFromGroup(group.get(g).toString());
			if(techniques.size() <= 0)
				continue;
			// 그룹 technique list 사전 만들기
			for(int h = 0; h < techniques.size(); h++) {
				//System.out.println(techniques.get(h));
				Group_Dictionary.put(techniques.get(h), h);
			}
			//System.out.println(Group_Dictionary);
			double[][] tech_matrix = new double[tech.size()][techniques.size()]; //매트릭스 2차원 배열 생성
			
			for(int i = 0; i < tech.size(); i++) {
				
				List<String> rule1_result = owl.Rule1(tech.get(i), group.get(g)); // rule1에 만족하는 technique 리스트 반환(부모/형제 관계)
				//System.out.println(rule1_result.size());
				for(int r1 = 0; r1 < rule1_result.size(); r1++) {
					tech_matrix[i][Group_Dictionary.get(rule1_result.get(r1))] += 0.16; // 0.16
				}
				
				List<String> rule2_result = owl.Rule2(tech.get(i), group.get(g)); // rule2에 만족하는 technique 리스트 반환(platform)
				for(int r2 = 0; r2 < rule2_result.size(); r2++) {
					tech_matrix[i][Group_Dictionary.get(rule2_result.get(r2))] += 0.52; // 0.52
				}
				
				List<String> rule3_result = owl.Rule3(tech.get(i), group.get(g)); // rule3에 만족하는 technique 리스트 반환(tactic)
				for(int r3 = 0; r3 < rule3_result.size(); r3++) {
					tech_matrix[i][Group_Dictionary.get(rule3_result.get(r3))] += 0.16; // 0.16
				}
				
				List<String> rule4_result = owl.Rule4(tech.get(i), group.get(g)); // rule4에 만족하는 technique 리스트 반환(attack pattern)
				for(int r4 = 0; r4 < rule4_result.size(); r4++) {
					tech_matrix[i][Group_Dictionary.get(rule4_result.get(r4))] += 0.16; // 0.16
				}
			}
			/*
			System.out.println("[ Group name : " + group.get(g) + " ]");
			System.out.println("=================================");
			for(int kk = 0; kk < tech_matrix.length; kk++) {
				for(int kkk = 0; kkk < tech_matrix[kk].length; kkk++) {
					System.out.printf("|%5s",tech_matrix[kk][kkk]);
				}
				System.out.println("|");
			}
			*/
			double[] row_weights = new double[tech_matrix.length];
			double total_weight = 0;
			for(int kk = 0; kk < tech_matrix.length; kk++) {
				for(int kkk = 0; kkk < tech_matrix[kk].length; kkk++) {
					row_weights[kk] += tech_matrix[kk][kkk];
				}
				row_weights[kk] /= tech_matrix[kk].length;
				total_weight += row_weights[kk];
			}
			total_weight /= tech_matrix.length;
			
			total_weight_map.put(group.get(g).toString(), total_weight);
		}
		
		
		return total_weight_map;
	}
	
	public ArrayList<HashMap<String, Double>> CaseRetrieval(OntologyFunc owl, List<String> sw, List<String> tech) { //Jaccard Similarity
		List<String> group = owl.LoadAttackGroups();
		HashMap<String, Double> total_score_map = new HashMap<>();
		HashMap<String, Double> sw_score_map = new HashMap<>();
		HashMap<String, Double> tech_score_map = new HashMap<>();
		
		for(int g = 0; g < group.size(); g++) {
			List<List> groupvec = owl.LoadSpecificGroup(group.get(g).toString());
			List<String> group_sw = (List)groupvec.get(0);
			List<String> group_tech = (List)groupvec.get(1);
			
			double sw_similarity_per_group = calcJaccardSimilarity(sw, group_sw);
			double tech_similarity_per_group = calcJaccardSimilarity(tech, group_tech);	
			double total_score = (tech_similarity_per_group + sw_similarity_per_group) / 2.0;
			
			total_score_map.put(group.get(g).toString(), total_score);
			sw_score_map.put(group.get(g).toString(), sw_similarity_per_group);
			tech_score_map.put(group.get(g).toString(), tech_similarity_per_group);
		}
		
		
		ArrayList<HashMap<String, Double>> obj = new ArrayList<HashMap<String, Double>>();
		obj.add(total_score_map);
		obj.add(sw_score_map);
		obj.add(tech_score_map);
		
		return obj;
	}
	
	//*sw
	public ArrayList<String[]> MakeTemplateRowFromSW(ArrayList swlist, OntologyFunc owl, List combinedTechs) {
		ArrayList<String[]> result = new ArrayList<String[]>();
		
		String tech = "";
		String sw = "";
		String tactic = "";
		String platform = "";
		String attack_pattern = "";
		String weakness = "";
		String vulnerability = "";
		
		for(int i = 0; i < swlist.size(); i++) {  //sw하나씩 반복문 진행
			sw = swlist.get(i).toString();
			
			List techs = owl.LoadTechniquesFromSW(swlist.get(i).toString());  // 하나의 sw에 대한 technique list 추출 (중복없음)
			techs.retainAll(combinedTechs); //교집합 진행.
			if(techs.size() == 0) {
				tech = "N/A";
				tactic = "N/A";
				platform = "N/A";
				attack_pattern = "N/A";
				weakness = "N/A";
				vulnerability = "N/A";
				result.add(new String[] {tech, sw, tactic, platform, attack_pattern, weakness, vulnerability});
			}
			
			for(int j = 0; j < techs.size(); j++) {  //교집합된 tech의 갯수만큼 반복문 진행
				tech = techs.get(j).toString();
				
				List platforms = owl.LoadWorkingPlatformfromTech(tech);  //해당 tech의 platform 가져오기
				platform = "";
				if(platforms.size() == 0)
					platform = "N/A";
				for(int k = 0; k < platforms.size(); k++) { 
					if(k == platforms.size() - 1)
						platform += platforms.get(k).toString();
					else
						platform += platforms.get(k).toString() + "\r\n, ";
				}	
				
				List tactics = owl.LoadTacticfromOneTech(tech);  //해당 tech의 tactic가져오기
				tactic = "";
				if(tactics.size() == 0) //tacic 없으면 N/A
					tactic = "N/A";
				for(int k = 0; k < tactics.size(); k++) { //tactic 리스트 한줄로 통합하기
					if(k == tactics.size() - 1)
						tactic += tactics.get(k).toString();
					else
						tactic += tactics.get(k).toString() + "\r\n, ";
				}
				
				List attack_patterns = owl.LoadAttackPatternfromOneTech(tech);  //해당 tech의 attack pattern 가져오기
				if(attack_patterns.size() == 0){
					attack_pattern = "N/A";
					weakness = "N/A";
					vulnerability = "N/A";
					result.add(new String[] {tech, sw, tactic, platform, attack_pattern, weakness, vulnerability});
				}
				for(int k = 0; k < attack_patterns.size(); k++) { // attack pattern이 있으면, 있는 갯수만큼 각 attack pattern에 대한 weakness 가져오기
					
					List weaknesses = owl.LoadWeaknessfromOneAttackPattern(attack_patterns.get(k).toString());
					if(weaknesses.size() == 0)
					{
						weakness = "N/A";
						vulnerability = "N/A";
						result.add(new String[] {tech, sw, tactic, platform, attack_pattern, weakness, vulnerability});
					}
					for(int h = 0; h < weaknesses.size(); h++) {
						weakness = weaknesses.get(h).toString();
						List vulnerabilities = owl.LoadVulnerabilityfromOneWeakness(weaknesses.get(h).toString());
						if(vulnerabilities.size() == 0)
							vulnerability = "N/A";
						for(int l = 0; l < vulnerabilities.size(); l++) {
							if(l == vulnerabilities.size() - 1)
								vulnerability += vulnerabilities.get(l).toString();
							else
								vulnerability += vulnerabilities.get(l).toString() + "\r\n, ";						
						}
						result.add(new String[] {tech, sw, tactic, platform, attack_pattern, weakness, vulnerability});
						
					}
					
					
				}				
			}
		}
		return result;
	}
	
	//*tech
	public ArrayList<String[]> MakeTemplateRowFromTech(ArrayList swlist, OntologyFunc owl, List techlist){
		ArrayList<String[]> result = new ArrayList<String[]>();
		
		String tech = "";
		String sw = "N/A";
		String tactic = "";
		String platform = "";
		String attack_pattern = "";
		String weakness = "";
		String vulnerability = "";
		
		
		for(int i = 0; i < swlist.size(); i++) {
			List techs = owl.LoadTechniquesFromSW(swlist.get(i).toString()); //sw가 수행하는 techlist load
			techlist.removeAll(techs);									//sw가 수행하는 techlist와 차집합 수행
		}
		
		for(int j = 0; j < techlist.size(); j++) {  //교집합된 tech의 갯수만큼 반복문 진행
			tech = techlist.get(j).toString();
			
			List platforms = owl.LoadWorkingPlatformfromTech(tech);  //해당 tech의 platform 가져오기
			platform = "";
			if(platforms.size() == 0)
				platform = "N/A";
			for(int k = 0; k < platforms.size(); k++) { 
				if(k == platforms.size() - 1)
					platform += platforms.get(k).toString();
				else
					platform += platforms.get(k).toString() + "\r\n, ";
			}	
			
			List tactics = owl.LoadTacticfromOneTech(tech);  //해당 tech의 tactic가져오기
			tactic = "";
			if(tactics.size() == 0) //tacic 없으면 N/A
				tactic = "N/A";
			else {
				for(int k = 0; k < tactics.size(); k++) { //tactic 리스트 한줄로 통합하기
					if(k == tactics.size() - 1)
						tactic += tactics.get(k).toString();
					else
						tactic += tactics.get(k).toString() + "\r\n, ";
				}				
			}			
			List attack_patterns = owl.LoadAttackPatternfromOneTech(tech);  //해당 tech의 attack pattern 가져오기
			if(attack_patterns.size() == 0){
				attack_pattern = "N/A";
				weakness = "N/A";
				vulnerability = "N/A";
				result.add(new String[] {tech, sw, tactic, platform, attack_pattern, weakness, vulnerability});
			}
			for(int k = 0; k < attack_patterns.size(); k++) { // attack pattern이 있으면, 있는 갯수만큼 각 attack pattern에 대한 weakness 가져오기
				attack_pattern = attack_patterns.get(k).toString();
				List weaknesses = owl.LoadWeaknessfromOneAttackPattern(attack_patterns.get(k).toString());
				if(weaknesses.size() == 0)
				{
					weakness = "N/A";
					vulnerability = "N/A";
					result.add(new String[] {tech, sw, tactic, platform, attack_pattern, weakness, vulnerability});
				}
				for(int h = 0; h < weaknesses.size(); h++) {
					weakness = weaknesses.get(h).toString();
					List vulnerabilities = owl.LoadVulnerabilityfromOneWeakness(weaknesses.get(h).toString());
					vulnerability = "";
					if(vulnerabilities.size() == 0)
						vulnerability = "N/A";
					for(int l = 0; l < vulnerabilities.size(); l++) {
						if(l == vulnerabilities.size() - 1)
							vulnerability += vulnerabilities.get(l).toString();
						else
							vulnerability += vulnerabilities.get(l).toString() + ", ";						
					}
					result.add(new String[] {tech, sw, tactic, platform, attack_pattern, weakness, vulnerability});					
				}
			}				
		}
		return result;
	}
	public boolean existsInTable(JTable table, int col, String value) {

	    // Get row and column count
	    int rowCount = table.getRowCount();
	    //int colCount = table.getColumnCount();

	    //System.out.println(curEntry);
	    // Check against all entries
	    for (int i = 0; i < rowCount; i++) {
	        String rowEntry = table.getValueAt(i, col).toString();
	        if (rowEntry.equalsIgnoreCase(value)) {
	            return true;
	        }
	    }
	    return false;
	}	
	
	/*
	 * ***********************************************************************************************************************************
	 * ***********************************************************************************************************************************
	 * ***********************************************************************************************************************************
	 * ***********************************************************************************************************************************
	 * ***********************************************************************************************************************************
	 * ***********************************************************************************************************************************
	 * ***********************************************************************************************************************************
	 */
	
	
	public void FrameSetting(OntologyFunc owl){
		Border margin = new EmptyBorder(0,2,5,2);
		
		JPanel leftPanel = new JPanel(); 			//왼쪽 패널
		
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
//========================================== Input Attack Information View =======================================================		
	
		
		
		JPanel InputPanel = new JPanel( );
		InputPanel.setLayout(new BoxLayout(InputPanel, BoxLayout.Y_AXIS));
		InputPanel.setBorder(new CompoundBorder(new TitledBorder(new LineBorder(Color.black,1),"Input of Attack Scenario"), margin));
		
		
//========================================== General Information of Attack View =======================================================
		JPanel pan_general_information = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pan_general_information.setLayout(new BoxLayout(pan_general_information, BoxLayout.Y_AXIS));
		pan_general_information.setBorder(new CompoundBorder(new TitledBorder(new LineBorder(Color.black,1),"General Information"), margin));
		
		JPanel pan_attack_goal = new JPanel(new FlowLayout(FlowLayout.LEFT));
		String[] attack_goal_contents = {"Financial Gain"};
		JComboBox attack_goal_cbo = new JComboBox(attack_goal_contents);
		JCheckBox cb1 = new JCheckBox();
		attack_goal_cbo.setPreferredSize(new Dimension(150,20));
		JTextField selected_attack_goal = new JTextField(25);
		
		attack_goal_cbo.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {
		    	String previous_text = selected_attack_goal.getText();
		    	if(previous_text.length() > 1) 
		    		selected_attack_goal.setText(previous_text + ", " + attack_goal_cbo.getSelectedItem().toString());
		    	else 
		    		selected_attack_goal.setText(attack_goal_cbo.getSelectedItem().toString());
		    }
		});
		
		
		JPanel pan_target_domain = new JPanel(new FlowLayout(FlowLayout.LEFT));
		String[] target_domain_contents = {"Bank"};
		JComboBox target_domain_cbo = new JComboBox(target_domain_contents);
		JCheckBox cb2 = new JCheckBox();
		target_domain_cbo.setPreferredSize(new Dimension(150,20));
		JTextField selected_domain_target = new JTextField(25);
		
		target_domain_cbo.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {
		    	String previous_text = selected_domain_target.getText();
		    	if(previous_text.length() > 1) 
		    		selected_domain_target.setText(previous_text + ", " + target_domain_cbo.getSelectedItem().toString());
		    	else 
		    		selected_domain_target.setText(target_domain_cbo.getSelectedItem().toString());
		    }
		});
		
		
		
		JPanel pan_used_software = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		List<String> loaded_SW = owl.LoadVectors("PDO:AC-Software_Used");
		String[] SWArray = loaded_SW.toArray(new String[0]);
		sw_cbo = new AutoCompleteComboBox(SWArray);
		
		JButton sw_btn = new JButton("Add");
		//sw_cbo = new JComboBox();
		
		
		pan_attack_goal.add(new JLabel("Attack Goals"));
		pan_attack_goal.add(attack_goal_cbo);
		pan_attack_goal.add(selected_attack_goal);
		pan_attack_goal.add(cb1);
		
		pan_target_domain.add(new JLabel("Targets        "));
		pan_target_domain.add(target_domain_cbo);
		pan_target_domain.add(selected_domain_target);
		pan_target_domain.add(cb2);
		
		pan_used_software.add(new JLabel("Software"));
		pan_used_software.add(sw_cbo);
		pan_used_software.add(sw_btn);	
		
		
		sw_table = new JTable(new DefaultTableModel(new Object[]{"Used Software List"}, 0));
		JScrollPane jsp1 = new JScrollPane(sw_table);
		
		sw_table.addKeyListener(new key());
		sw_btn.addActionListener(new ActionListener(){ //익명클래스로 리스너 작성
			public void actionPerformed(ActionEvent e){
				InsertItem(sw_cbo, sw_table);
			}
		});		
		
		pan_general_information.add(pan_attack_goal);
		pan_general_information.add(pan_target_domain);
		pan_general_information.add(pan_used_software);
		pan_general_information.add(jsp1);
//===================================     Attack Tactic View  ======================================		
		JPanel pan_attack_tactic = new JPanel();	
		pan_attack_tactic.setLayout(new BoxLayout(pan_attack_tactic, BoxLayout.Y_AXIS));
		pan_attack_tactic.setBorder(new CompoundBorder(new TitledBorder(new LineBorder(Color.black,1),"Tactics"), margin));
		
			
		JPanel pan_tactic_select1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel pan_tactic_select2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		String[] tactic_list = {"Reconnaissance", "Resource Development", "Initial Access", "Execution", "Persistence", "Privilege Escalation",
				"Defense Evasion", "Credential Access", "Discovery", "Lateral Movement", "Collection", "Command and Control", "Exfiltration",
				"Impact"};

		JComboBox tactic_cbo = new JComboBox(tactic_list);
		JButton tactic_add_btn = new JButton("Add");	
		tactic_cbo.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {
		    	String tactic = "";
		    	switch(tactic_cbo.getSelectedItem().toString()) {
		    		case "Reconnaissance":
		    			tactic = "TA0043:Reconnaissance";
		    			break;
		    		case "Resource Development":
		    			tactic = "TA0042:Resource Development";
		    			break;
		    		case "Initial Access":
		    			tactic = "TA0001:Initial Access";
		    			break;
		    		case "Execution":
		    			tactic = "TA0002:Execution";
		    			break;
		    		case "Persistence":
		    			tactic = "TA0003:Persistence";
		    			break;
		    		case "Privilege Escalation":
		    			tactic = "TA0004:Privilege Escalation";
		    			break;
		    		case "Defense Evasion":
		    			tactic = "TA0005:Defense Evasion";
		    			break;
		    		case "Credential Access":
		    			tactic = "TA0006:Credential Access";
		    		case "Discovery":
		    			tactic = "TA0007:Discovery";
		    			break;
		    		case "Lateral Movement":
		    			tactic = "TA0008:Lateral Movement";
		    		case "Collection":
		    			tactic = "TA0009:Collection";
		    			break;
		    		case "Command and Control":
		    			tactic = "TA0011:Command and Control";
		    			break;
		    		case "Exfiltration":
		    			tactic = "TA0010:Exfiltration";
		    			break;
		    		case "Impact":
		    			tactic = "TA0040:Impact";
		    			break;
		    	}
		    	List<String> result = owl.LoadTechniquesByTactic(tactic);
		    	tech_cbo1.removeAllItems();
		    	for(int i = 0; i < result.size(); i++)
		    		tech_cbo1.addItem(result.get(i));
		    	
		    }
		});
		tech_cbo1 = new AutoCompleteComboBox(new String[] {""});

		tech_cbo1.setPreferredSize(new Dimension(340,20));
		
		String example_header[] = {"Tactics", "Techniques"};

		tactic_table = new JTable(new DefaultTableModel(new Object[]{"Tactics", "Attack Elements"}, 0));
		tactic_table.addKeyListener(new key());
		tactic_table.getColumnModel().getColumn(0).setPreferredWidth(6);
		tactic_table.getColumnModel().getColumn(1).setPreferredWidth(200);
		tactic_table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		//tech_table = new JTable(new DefaultTableModel(example_contents, example_header));
		JScrollPane jsp2 = new JScrollPane(tactic_table);
		
		pan_tactic_select1.add(new JLabel("Attack Tactics"));
		pan_tactic_select1.add(tactic_cbo);
		pan_tactic_select2.add(new JLabel("Technique Element"));
		pan_tactic_select2.add(tech_cbo1);
		pan_tactic_select2.add(tactic_add_btn);
		
		
		
		tactic_add_btn.addActionListener(new ActionListener(){ 
			public void actionPerformed(ActionEvent e){
				InsertItem(tactic_cbo, tech_cbo1, tactic_table);
			}
		});	
		
		
		pan_attack_tactic.add(pan_tactic_select1);
		pan_attack_tactic.add(pan_tactic_select2);
		pan_attack_tactic.add(jsp2);
//============================================  Attack Scenario View  =============================================		
		JPanel pan_attack_scenario = new JPanel(new FlowLayout(FlowLayout.LEFT));	
		pan_attack_scenario.setLayout(new BoxLayout(pan_attack_scenario, BoxLayout.Y_AXIS));
		pan_attack_scenario.setBorder(new CompoundBorder(new TitledBorder(new LineBorder(Color.black,1),"Cyber Kill Chain"), margin));		
		
		String[] cyber_kill_chain_list = {"Reconnaissance", "Delivery", "Exploitation", "Command & Control", "Operation", "Action on Objective"};
		JComboBox scenario_cbo = new JComboBox(cyber_kill_chain_list);
		
		
		List<String> loaded_Tech = owl.LoadVectors("PDO:AC-Techniques_Used");
		String[] TechArray = loaded_Tech.toArray(new String[0]);
		tech_cbo2 = new AutoCompleteComboBox(TechArray);
		//tech_cbo2 = new AutoCompleteComboBox(new String[] {""});
		tech_cbo2.setPreferredSize(new Dimension(300,20));
		
		JButton phase_add_btn = new JButton("Add");	
		
		scenario_table = new JTable(new DefaultTableModel(new Object[]{"Cyber Kill Chain", "Attack Elements"}, 0));
		scenario_table.addKeyListener(new key());
		scenario_table.getColumnModel().getColumn(0).setPreferredWidth(6);
		scenario_table.getColumnModel().getColumn(1).setPreferredWidth(200);
		scenario_table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		JScrollPane jsp3 = new JScrollPane(scenario_table);
		
		JPanel pan_phase_select1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel pan_phase_select2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		pan_phase_select1.add(new JLabel("Cyber Kill Chain Elements"));
		pan_phase_select1.add(scenario_cbo);
		pan_phase_select2.add(new JLabel("Technique Element"));
		pan_phase_select2.add(tech_cbo2);
		pan_phase_select2.add(phase_add_btn);
		
		phase_add_btn.addActionListener(new ActionListener(){ 
			public void actionPerformed(ActionEvent e){
				InsertItem(scenario_cbo, tech_cbo2, scenario_table);
				//new resizeColumnWidth().resizeColumnWidth(5, scenario_table);
			}
		});			
		
		pan_attack_scenario.add(pan_phase_select1);
		pan_attack_scenario.add(pan_phase_select2);
		pan_attack_scenario.add(jsp3);
		
//====================================Left Panel 중 button panel=====================================================	

		JPanel btn_panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		JButton next_btn1 = new JButton("Next Step");
		
		
		btn_panel.add(next_btn1, BorderLayout.SOUTH);
		
		next_btn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List combinedTechs = new ListUtil().union(GetColData(tactic_table, 1), GetColData(scenario_table, 1)); //techlist 병합
				
				DefaultTableModel model = (DefaultTableModel)sw_table.getModel();
				ArrayList<String> swlist = new ArrayList<String>();
				for(int i = 0; i < model.getRowCount(); i++) {
					swlist.add(model.getValueAt(i, 0).toString());
				}
				List<String[]> result = MakeTemplateRowFromSW(swlist, owl, combinedTechs); //
				List<String[]> result2 = MakeTemplateRowFromTech(swlist, owl, combinedTechs);
				
				DefaultTableModel template_model = (DefaultTableModel)template_table.getModel();
				
				for(int i = 0; i < result.size(); i++) {
					String[] strData = result.get(i);
					template_model.addRow(new Object[] {strData[0],strData[1],strData[2],strData[3],strData[4],strData[5],strData[6]});
				}
				for(int i = 0; i < result2.size(); i++) {
					String[] strData = result2.get(i);
					template_model.addRow(new Object[] {strData[0],strData[1],strData[2],strData[3],strData[4],strData[5],strData[6]});
				}
				submit_btn.setEnabled(true);			
			}
		});

		
				
		InputPanel.add(pan_general_information);
		InputPanel.add(pan_attack_tactic);
		InputPanel.add(pan_attack_scenario);
		InputPanel.add(btn_panel);
		
		leftPanel.add(InputPanel);
//=============================================가운데 패널 (입력한 패널을 탬플릿으로 display 하는 화면)===================================================================================
		RelativeLayout rl = new RelativeLayout(RelativeLayout.Y_AXIS);
		rl.setFill(true);
		
		JPanel middlePanel = new JPanel( rl );
				
		JPanel template_panel = new JPanel();
		template_panel.setLayout(new BoxLayout(template_panel, BoxLayout.Y_AXIS));
		template_panel.setBorder(new CompoundBorder(new TitledBorder(new LineBorder(Color.black,1),"Attack Component"), margin));
		
		template_table = new JTable(new DefaultTableModel(new Object[]{"Techniques", "Software","Tactics", "Platforms", "Attack Pattern", "Weakness", "Vulnerability"}, 0));
		
		JScrollPane template_jsp = new JScrollPane( template_table );
		template_panel.add(template_jsp);
		
		JPanel calc_result_pan = new JPanel();
		calc_result_pan.setLayout(new BoxLayout(calc_result_pan, BoxLayout.Y_AXIS));
		calc_result_pan.setBorder(new CompoundBorder(new TitledBorder(new LineBorder(Color.black,1),"Similar Attack Candidates"), margin));

		//calc_table = new JTable(new DefaultTableModel(new Object[]{"Rank", "Attack Group", "Software Similarity of Jaccard","Technique Similarity of Jaccard", "Total Jaccard Similarity", "", "Review Status"}, 0));
		calc_table = new JTable(new DefaultTableModel(new Object[]{"Rank", "Attack Group", "Software Similarity of Jaccard","Technique Similarity of Jaccard", "Total Jaccard Similarity", "Weight", "Total Similarity", "", "Review Status"}, 0));
		

		
		Action newFrame = new AbstractAction()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        int modelRow = Integer.valueOf( e.getActionCommand() );
		        SubFrameSetting subframe = new SubFrameSetting(owl, calc_table.getValueAt(modelRow, 1).toString(), CheckedList);
				JScrollPane myScrollPane = new JScrollPane(subframe){
				    @Override
				    public Dimension getPreferredSize() {
				        return new Dimension(1200, 800);
				    }
				};
		        
		        //new SubFrameSetting().OpenNewWindow(owl, calc_table.getValueAt(modelRow, 1).toString());
		        int result = JOptionPane.showOptionDialog(null,
		        		myScrollPane,
		        		calc_table.getValueAt(modelRow, 1).toString(), //title
		        		JOptionPane.OK_CANCEL_OPTION,
		        		JOptionPane.PLAIN_MESSAGE,
		        		null,
		        		new String[] {"Save", "Close without saving"},
		        		"default");
		        
		        if(result == JOptionPane.OK_OPTION) {         //아직 버그있음
		        	HashMap<String, String> checkeditems = subframe.getCheckedItems();
		        	if(!checkeditems.isEmpty()) {
		        		checkeditems.forEach(
		        			    (key, value) -> CheckedList.merge( key, value, (v1, v2) -> v1.equalsIgnoreCase(v2) ? v1 : v1 + "," + v2)
		        		);
			        	calc_table.setValueAt("Y", modelRow, 8);
			        	//SR_btn.setEnabled(true);
		        	}
		        	//System.out.println(CheckedList.size());	
		        	boolean is_there_any_checked = existsInTable(calc_table, 8, "Y");
		        	System.out.println(is_there_any_checked);
		        	if(is_there_any_checked)
		        		SR_btn.setEnabled(true);
		        	else
		        		SR_btn.setEnabled(false);
		        }
		        
		        
		    }
		};
		Action chart = new AbstractAction()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        int modelRow = Integer.valueOf( e.getActionCommand() );
		        MakeRadarGraph(owl, tactic_table, calc_table.getValueAt(modelRow, 1).toString());
		    }
		};		 
		ButtonColumn buttonColumn1 = new ButtonColumn(calc_table, newFrame, 1);
		ButtonColumn buttonColumn2 = new ButtonColumn(calc_table, chart, 7);
		
		buttonColumn1.setMnemonic(KeyEvent.VK_D);
		buttonColumn1.setMnemonic(KeyEvent.VK_D);
		
		
		calc_table.getColumnModel().getColumn(0).setPreferredWidth(20);
		calc_table.getColumnModel().getColumn(1).setPreferredWidth(80);
		calc_table.getColumnModel().getColumn(2).setPreferredWidth(150);
		calc_table.getColumnModel().getColumn(3).setPreferredWidth(150);
		calc_table.getColumnModel().getColumn(4).setPreferredWidth(100);
		calc_table.getColumnModel().getColumn(5).setPreferredWidth(100);
		calc_table.getColumnModel().getColumn(6).setPreferredWidth(100);
		calc_table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		
		JScrollPane calc_jsp = new JScrollPane(calc_table);
		calc_result_pan.add(calc_jsp);
		
		JPanel btn_panel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel slider_panel_1 = new JPanel();
		JLabel s0_label = new JLabel("Cyber Kill Chain");
		JSlider slider0 = new JSlider(0, 100, 50);
		slider0.setPreferredSize(new Dimension(100,20));
		slider0.setMajorTickSpacing(50);
		slider0.setPaintTicks(true);
		//slider0.setEnabled(false);
		
		
		JPanel slider_panel = new JPanel(new GridLayout(2, 4, 5, 5));
		slider_panel.setPreferredSize( new Dimension( 500, 40 ) );
		JLabel s1_label = new JLabel("Platform");
		JLabel s2_label = new JLabel("Tactic");
		JLabel s3_label = new JLabel("Node Relation");
		JLabel s4_label = new JLabel("Attack Pattern");
		
		JSlider slider1 = new JSlider(0, 100, 50);
		slider1.setMajorTickSpacing(50);
		slider1.setPaintTicks(true);
		JSlider slider2 = new JSlider(0, 100, 50);
		slider2.setMajorTickSpacing(50);
		slider2.setPaintTicks(true);
		JSlider slider3 = new JSlider(0, 100, 50);
		slider3.setMajorTickSpacing(50);
		slider3.setPaintTicks(true);
		JSlider slider4 = new JSlider(0, 100, 50);
		slider4.setMajorTickSpacing(50);
		slider4.setPaintTicks(true);
		
		slider0.setEnabled(false);
		slider1.setEnabled(false);
		slider2.setEnabled(false);
		slider3.setEnabled(false);
		slider4.setEnabled(false);
		
		
		
		String[] similarity_measure_contents = {"Jaccard Similarity", "Jaccard + Weights"};
		JComboBox sim_cbo = new JComboBox(similarity_measure_contents);
		submit_btn = new JButton("Next Step");
		submit_btn.setEnabled(false);
		JButton remove_btn = new JButton("Remove all contents");
		
		
		
		slider_panel.add(s1_label);
		slider_panel.add(slider1);
		slider_panel.add(s2_label);
		slider_panel.add(slider2);
		slider_panel.add(s3_label);
		slider_panel.add(slider3);
		slider_panel.add(s4_label);
		slider_panel.add(slider4);
		
		btn_panel2.add(s0_label);
		btn_panel2.add(slider0);
		btn_panel2.add(slider_panel);
		btn_panel2.add(sim_cbo);
		btn_panel2.add(submit_btn);
		btn_panel2.add(remove_btn);
		
		sim_cbo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				int index = cb.getSelectedIndex();
				if(index == 1) {
					slider0.setEnabled(true);
					slider1.setEnabled(true);
					slider2.setEnabled(true);
					slider3.setEnabled(true);
					slider4.setEnabled(true);
				}
				else
				{
					slider0.setEnabled(false);
					slider1.setEnabled(false);
					slider2.setEnabled(false);
					slider3.setEnabled(false);
					slider4.setEnabled(false);
				}
			}
		});
		

		submit_btn.addActionListener(new ActionListener(){    
			public void actionPerformed(ActionEvent e){
				ArrayList<HashMap<String, Double>> objs = CaseRetrieval(owl, GetColData(sw_table, 0), new ListUtil().union(GetColData(tactic_table, 1),GetColData(scenario_table, 1)));
				Map<String, Double> weights = new HashMap<String, Double>();
				
				HashMap<String, Double> total_scores = objs.get(0);
				HashMap<String, Double> sw_scores = objs.get(1);
				HashMap<String, Double> tech_scores = objs.get(2);
				
				DefaultTableModel model = (DefaultTableModel)calc_table.getModel();
				
				
				if(sim_cbo.getSelectedItem().toString().equals("Jaccard + Weights")) {

					weights = CaseRetrieval2(owl, new ListUtil().union(GetColData(tactic_table, 1),GetColData(scenario_table, 1)));
					
					HashMap<String, Double> total = new HashMap<String, Double>(weights);
					//total.forEach((key, value) -> total_scores.merge(key, value, (v1, v2) -> v1 + v2));
					total_scores.entrySet().forEach(entry -> total.compute( entry.getKey(),
					            (key, value) -> value == null ? 
					                            entry.getValue() : 
					                            entry.getValue() + value));
					//total_scores.entrySet().forEach(entry -> total.merge(entry.getKey(), entry.getValue(), (key, value) -> entry.getValue() + value));
					
					List<String> JaccardSetList = new ArrayList<>(total.keySet());
					System.out.println("제이커드=> "+total_scores);
					System.out.println("가중치 => " + weights);
					System.out.println("최종 =>"+total);
					
					Collections.sort(JaccardSetList, (o1, o2) -> (total.get(o2).compareTo(total.get(o1))));
					//Collections.sort(JaccardSetList, (o1, o2) -> (total_scores.get(o2).compareTo(total_scores.get(o1))));
					for(int i = 0; i < 5; i++) {
						model.addRow(new Object[]{Integer.toString(i + 1), JaccardSetList.get(i).toString(), String.format("%.3f", sw_scores.get(JaccardSetList.get(i).toString())),
								String.format("%.3f", tech_scores.get(JaccardSetList.get(i).toString())),
								String.format("%.3f", total_scores.get(JaccardSetList.get(i).toString())),
								String.format("%.3f", weights.get(JaccardSetList.get(i).toString())),
								String.format("%.3f", total.get(JaccardSetList.get(i).toString())),
								"Chart", "N"});
					}
				}
				else {
				
					List<String> JaccardSetList = new ArrayList<>(total_scores.keySet());
					Collections.sort(JaccardSetList, (o1, o2) -> (total_scores.get(o2).compareTo(total_scores.get(o1))));
					
					for(int i = 0; i < 5; i++) {
						model.addRow(new Object[]{Integer.toString(i + 1), JaccardSetList.get(i).toString(), String.format("%.3f", sw_scores.get(JaccardSetList.get(i).toString())),
								String.format("%.3f", tech_scores.get(JaccardSetList.get(i).toString())),
								String.format("%.3f", total_scores.get(JaccardSetList.get(i).toString())),
								"-",
								String.format("%.3f", total_scores.get(JaccardSetList.get(i).toString())),
								"Chart", "N"});
					}
				}
				

			}
		});
			
		remove_btn.addActionListener(new ActionListener(){ 
			public void actionPerformed(ActionEvent e){
				JTable[] table_list = {sw_table, tactic_table, scenario_table, template_table, calc_table};
				for(int i = 0; i < table_list.length; i++) {
					DefaultTableModel model = (DefaultTableModel)table_list[i].getModel();
					model.setNumRows(0);
				}
				CheckedList.clear();
				SR_btn.setEnabled(false);
				submit_btn.setEnabled(false);
			}
		});
		
		calc_table.addMouseMotionListener(new MouseMotionAdapter()
		{
		   public void mouseMoved(MouseEvent e)
		   {
		      int row = calc_table.rowAtPoint(e.getPoint());
		      if (row > -1)
		      {
		         // easiest way:
		    	  calc_table.clearSelection();
		    	  calc_table.setRowSelectionInterval(row, row);
		      }
		      else
		      {
		    	  calc_table.setSelectionBackground(Color.blue);
		      }
		   }
		});
		
		
		JPanel btn_panel3 = new JPanel();
		
		JButton mitre_example1 = new JButton("Full set of Carbanak");
		JButton sw_example1 = new JButton("Used SW of Carbanak");
		JButton tactic_example1 = new JButton("TA_WP");
		JButton tactic_example2 = new JButton("TA_ITT");
		JButton tactic_example3 = new JButton("TA_NoALP");
		JButton phase_example1 = new JButton("CKC_Bank");
		
		btn_panel3.add(mitre_example1);
		btn_panel3.add(sw_example1);
		btn_panel3.add(tactic_example1);
		btn_panel3.add(tactic_example2);
		btn_panel3.add(tactic_example3);
		btn_panel3.add(phase_example1);
		
		mitre_example1.addActionListener(new ActionListener() {		
			String example_tactic_contents[][] = {
					{"Command and Control","T1008:Fallback_Channels"},
					{"Defense Evasion","T1036.004:Masquerading:Masquerade_Task_or_Service"},
					{"Defense Evasion","T1036.005:Masquerading:Match_Legitimate_Name_or_Location"},
					{"Initial Access","T1078:Valid_Accounts"},
					{"Command and Control","T1102.002:Web_Service:Bidirectional_Communication"},
					{"Defense Evasion","T1218.011:Signed_Binary_Proxy_Execution:Rundll32"},
					{"Command and Control","T1219:Remote_Access_Software"},
					{"Persistence","T1543.003:Create_or_Modify_System_Process:Windows_Service"},
					{"Defense Evasion","T1562.004:Impair_Defenses:Disable_or_Modify_System_Firewall"}
			};
			String example_sw_contents[] = {"S0002:Mimikatz","S0030:Carbanak","S0029:PsExec", "S0108:netsh"};
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel tactic_model = (DefaultTableModel)tactic_table.getModel();
				for(int i = 0; i < example_tactic_contents.length; i++) {
					tactic_model.addRow(new Object[] {example_tactic_contents[i][0], example_tactic_contents[i][1]});
				}
				DefaultTableModel sw_model = (DefaultTableModel)sw_table.getModel();
				for(int i = 0; i < example_sw_contents.length; i++) {
					sw_model.addRow(new Object[] {example_sw_contents[i]});
				}
			};
		});
		tactic_example1.addActionListener(new ActionListener() {		
			String example_tactic_contents[][] = {
					{"Execution","T1204:User_Execution"},
					{"Execution","T1559.001:Inter-Process_Communication:Component_Object_Model"},
					{"Execution","T1059.005:Command_and_Scripting_Interpreter:Visual_Basic"},
					{"Defense Evasion","T1027.001:Obfuscated_Files_or_Information:Binary_Padding"},
					{"Defense Evasion","T1027.002:Obfuscated_Files_or_Information:Software_Packing"},
					{"Defense Evasion","T1140:Deobfuscate/Decode_Files_or_Information"},
					{"Execution","T1059.003:Command_and_Scripting_Interpreter:Windows_Command_Shell"},
					{"Execution","T1059.007:Command_and_Scripting_Interpreter:JavaScript/JScript"},
					{"Command and Control","T1071.001:Application_Layer_Protocol:Web_Protocols"},
					{"Command and Control","T1573.002:Encrypted_Channel:Asymmetric_Cryptography"}
			};
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel tactic_model = (DefaultTableModel)tactic_table.getModel();
				for(int i = 0; i < example_tactic_contents.length; i++) {
					tactic_model.addRow(new Object[] {example_tactic_contents[i][0], example_tactic_contents[i][1]});
				}

			};
		});
		tactic_example2.addActionListener(new ActionListener() {		
			/*
			String example_tactic_contents[][] = {
					{"Discovery","T1082:System_Information_Discovery"},
					{"Discovery","T1057:Process_Discovery"},
					{"Command and Control","T1105:Ingress_Tool_Transfer"},
					{"Execution","T1059.003:Command_and_Scripting_Interpreter:Windows_Command_Shell"},
					{"Execution","T1559.001:Inter-Process_Communication:Component_Object_Model"},
					{"Collection","T1113:Screen_Capture"},
					{"Exfiltration","T1041:Exfiltration_Over_C2_Channel"}
			};
			*/
			String example_tactic_contents[][] = {   // 이버튼으로 사례 연구 진행
					{"Defense Evasion","T1036.005:Masquerading:Match_Legitimate_Name_or_Location"},
					{"Defense Evasion","T1562.001:Impair_Defenses:Disable_or_Modify_Tools"},
					{"Defense Evasion","T1562.004:Impair_Defenses:Disable_or_Modify_System_Firewall"},
					{"Initial Access","T1566.001:Phishing:Spearphishing_Attachment"},
					{"Persistence","T1078:Valid_Accounts"},
					{"Execution","T1059.003:Command_and_Scripting_Interpreter:Windows_Command_Shell"},
					{"Command and Control","T1219:Remote_Access_Software"},
					{"Impact", "T1565.001:Data_Manipulation:Stored_Data_Manipulation"},
					{"Defense Evasion", "T1055.003:Process_Injection:Thread_Execution_Hijacking"},
					{"Reconnaissance", "T1589.002:Gather_Victim_Identity_Information:Email_Addresses"},
					{"Defense Evasion", "T1564.001:Hide_Artifacts:Hidden_Files_and_Directories"}
					
			};
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel tactic_model = (DefaultTableModel)tactic_table.getModel();
				for(int i = 0; i < example_tactic_contents.length; i++) {
					tactic_model.addRow(new Object[] {example_tactic_contents[i][0], example_tactic_contents[i][1]});
				}

			};
		});
		tactic_example3.addActionListener(new ActionListener() {		
			String example_tactic_contents[][] = {
					{"Execution","T1059.003:Command_and_Scripting_Interpreter:Windows_Command_Shell"},
					{"Defense Evasion","T1112:Modify_Registry"},
					{"Defense Evasion","T1027:Obfuscated_Files_or_Information"},
					{"Discovery","T1012:Query_Registry"},
					{"Command and Control","T1105:Ingress_Tool_Transfer"},
					{"Execution","T1059.001:Command_and_Scripting_Interpreter:PowerShell"},
					{"Defense Evasion", "T1140:Deobfuscate/Decode_Files_or_Information"},
					{"Execution", "T1106:Native_API"},
					{"Command and Control", "T1095:Non-Application_Layer_Protocol"}
					
			};
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel tactic_model = (DefaultTableModel)tactic_table.getModel();
				for(int i = 0; i < example_tactic_contents.length; i++) {
					tactic_model.addRow(new Object[] {example_tactic_contents[i][0], example_tactic_contents[i][1]});
				}

			};
		});
		phase_example1.addActionListener(new ActionListener() {		
			String example_tactic_contents[][] = {
					{"Reconnaissance","T1589:Gather_Victim_Identity_Information"},
					{"Delivery","T1566.001:Phishing:Spearphishing_Attachment"},
					{"Exploitation","T1059.003:Command_and_Scripting_Interpreter:Windows_Command_Shell"},
					{"Command & Control","T1219:Remote_Access_Software"},
					{"Operation","T1078:Valid_Accounts"},
					{"Operation","T1003:OS_Credential_Dumping"},
					{"Operation", "T1021.001:Remote_Services:Remote_Desktop_Protocol"},
					{"Action on Objective", "T1030:Data_Transfer_Size_Limits"}
					
			};
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel scenario_model = (DefaultTableModel)scenario_table.getModel();
				for(int i = 0; i < example_tactic_contents.length; i++) {
					scenario_model.addRow(new Object[] {example_tactic_contents[i][0], example_tactic_contents[i][1]});
				}

			};
		});
		sw_example1.addActionListener(new ActionListener() {		
			String example_sw_contents[] = {"S0029:PsExec", "S0108:netsh"};
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel sw_model = (DefaultTableModel)sw_table.getModel();
				for(int i = 0; i < example_sw_contents.length; i++) {
					sw_model.addRow(new Object[] {example_sw_contents[i], example_sw_contents[i]});
				}

			};
		});

		JPanel btn_panel4 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		SR_btn = new JButton("View Recommend Security Requirements");
		SR_btn.setEnabled(false);
		btn_panel4.add(SR_btn);
		
		SR_btn.addActionListener(new ActionListener() {		
			public void actionPerformed(ActionEvent e) {
				new SRFrame("  Recommended Security Requirements", CheckedList);
			};
		});		
		
		middlePanel.add(template_panel, new Float(60));
		middlePanel.add(btn_panel2, new Float(5)); 
		middlePanel.add(calc_result_pan, new Float(30));
		middlePanel.add(btn_panel4, new Float(5));

//========================================================총 패널========================================================================		
							
		RelativeLayout rl2 = new RelativeLayout(RelativeLayout.X_AXIS);
		rl2.setFill(true);

		JPanel outerpanel = new JPanel(rl2); 
		outerpanel.add(leftPanel, new Float(40));
		outerpanel.add(middlePanel, new Float(60));
		/*
		JScrollPane scroll = new JScrollPane(
				outerpanel, 
				ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
		);
		*/
//================================================================================================================================
		JPanel bottomBar = new JPanel(new GridLayout(1, 2));
		bottomBar.setBorder(new CompoundBorder(new LineBorder(Color.DARK_GRAY), new EmptyBorder(4, 4, 4, 4)));
		//JPanel statusBar = new JPanel();
		JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
	    //statusBar.setBorder(new CompoundBorder(new LineBorder(Color.DARK_GRAY), new EmptyBorder(4, 4, 4, 4)));
	    status = new JLabel("Load CB-PDO...");
	    statusBar.add(status);
	    
	    bottomBar.add(statusBar);
	    bottomBar.add(btn_panel3);
		
	    
	    
	    setIconImage(img);
	    getContentPane().setLayout(new BorderLayout());
		//add(scroll, BorderLayout.CENTER);
		add(outerpanel, BorderLayout.CENTER);
		//add(statusBar, BorderLayout.SOUTH);
		add(bottomBar, BorderLayout.SOUTH);
		setTitle("CB_PDO Security Requirements Recommendation");
		//setSize(1500, 900);
		setSize(1850, 900);

		
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		//addKeyListener(new key());
		//setFocusable(true);
	}

}

