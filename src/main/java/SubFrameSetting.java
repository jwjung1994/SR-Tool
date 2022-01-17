import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

//public class SubFrameSetting extends JFrame {
public class SubFrameSetting extends JPanel {
	JTable subtable2_sr, subtable2_2_sr;
	HashMap<String, String> Checked_SR = new HashMap<String, String>();  //체크된 행 반환
	private static final int BOOLEAN_COLUMN = 3;
	
	
	public SubFrameSetting(OntologyFunc owl, String case_nm, HashMap<String, String> aaa)  {		
		setVisible(true);		
		
	    JLabel titlelabel = new JLabel(case_nm);
	    titlelabel.setFont(new Font("Serif", Font.BOLD, 30));
	        
	    JLabel subtitlelabel1 = new JLabel("1. Attack Description of Group");
	    subtitlelabel1.setFont(new Font("Serif", Font.BOLD, 20));
	    
	    ArrayList sw_result = owl.LoadGroupSW(case_nm);
	    
	    String[][] result_arr1 = new String[sw_result.size()][];
	    for(int i = 0; i < sw_result.size(); i++) {
	    	String[] row = (String[]) sw_result.get(i);
	    	result_arr1[i] = new String[row.length];
	    	for(int j = 0; j < row.length; j++)
	    		result_arr1[i][j] = row[j];
	    	
	    }
	    
	    String[] result_header_sw = {"SW Name","Description","Work in Platform"};
	    JTable subtable1_sw = new JTable(new DefaultTableModel(result_arr1, result_header_sw));
	    subtable1_sw.getColumnModel().getColumn(1).setCellRenderer(new WordWrapCellRenderer());
	    JScrollPane subjsp1 = new JScrollPane(subtable1_sw);
	    subtable1_sw.getColumnModel().getColumn(0).setPreferredWidth(50);
	    subtable1_sw.getColumnModel().getColumn(1).setPreferredWidth(300);
	    subtable1_sw.getColumnModel().getColumn(2).setPreferredWidth(5);
	    subtable1_sw.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
	    
	    String[] result_header_tech = {"Technique Name", "Description", "Example"};
	    ArrayList tech_result = owl.LoadGroupTechnique(case_nm);
	    String[][] result_arr2 = new String[tech_result.size()][];
	    for(int i = 0; i < tech_result.size(); i++) {
	    	String[] row = (String[]) tech_result.get(i);
	    	result_arr2[i] = new String[row.length];
	    	for(int j = 0; j < row.length; j++)
	    		result_arr2[i][j] = row[j];
	    	
	    }
	  
	    JTable subtable1_tech = new JTable(result_arr2, result_header_tech);
	    subtable1_tech.getColumnModel().getColumn(1).setCellRenderer(new WordWrapCellRenderer());
	    subtable1_tech.getColumnModel().getColumn(2).setCellRenderer(new WordWrapCellRenderer());
	    JScrollPane subjsp2 = new JScrollPane(subtable1_tech);
	    subtable1_tech.getColumnModel().getColumn(0).setPreferredWidth(20);
	    subtable1_tech.getColumnModel().getColumn(1).setPreferredWidth(200);
	    subtable1_tech.getColumnModel().getColumn(2).setPreferredWidth(200);
	    subtable1_tech.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
	    
	    JPanel firstpanel = new JPanel();
	    firstpanel.setLayout(new BoxLayout(firstpanel, BoxLayout.Y_AXIS));
	    firstpanel.add(titlelabel);
	    firstpanel.add(subtitlelabel1);
	    firstpanel.add(subjsp1);
	    firstpanel.add(subjsp2);
	    	    
	    //======================================================================================================
	    JLabel subtitlelabel2 = new JLabel("2. Security Requirement from Attack Elements");
	    subtitlelabel2.setFont(new Font("Serif", Font.BOLD, 20));
	    
	    JLabel subtitlelabel2_1 = new JLabel("2.1 Security Requirement for Existing Groups");
	    subtitlelabel2_1.setFont(new Font("Serif", Font.BOLD, 15));
	    
	    String[] result_sr = {"Requirement ID", "Description", "Count", "Adoption"};
	    ArrayList sr_result = owl.LoadSR(case_nm);
	    Object[][] result_arr3 = new Object[sr_result.size()][];
	    for(int i = 0; i < sr_result.size(); i++) {
	    	String[] row = (String[]) sr_result.get(i);
	    	result_arr3[i] = new Object[row.length + 1];
	    	for(int j = 0; j < row.length; j++)
	    		result_arr3[i][j] = row[j];
	    	result_arr3[i][3] = false;
	    }
	    
	    DefaultTableModel dtm2 = new DefaultTableModel(result_arr3, result_sr);
	    subtable2_sr = new JTable(dtm2);
	    subtable2_sr.getColumn("Adoption").setCellRenderer(dcr);
	    JCheckBox box2 = new JCheckBox();
	    box2.setHorizontalAlignment(JLabel.CENTER);
	    subtable2_sr.getColumn("Adoption").setCellEditor(new DefaultCellEditor(box2));
	    
	    //subtable2_sr.getColumnModel().getColumn(0).setCellRenderer(new WordWrapCellRenderer());
	    subtable2_sr.getColumnModel().getColumn(1).setCellRenderer(new WordWrapCellRenderer());
	    JScrollPane sub2jsp1 = new JScrollPane(subtable2_sr);
	    subtable2_sr.getColumnModel().getColumn(0).setPreferredWidth(50);
	    subtable2_sr.getColumnModel().getColumn(1).setPreferredWidth(400);
	    subtable2_sr.getColumnModel().getColumn(2).setPreferredWidth(5);
	    subtable2_sr.getColumnModel().getColumn(3).setPreferredWidth(1);
	    subtable2_sr.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
	    //new resizeColumnWidth().resizeColumnWidth(10, subtable2_sr);
	    subtable2_sr.getModel().addTableModelListener(new CheckBoxModelListener());
	    
	    JLabel subtitlelabel2_2 = new JLabel("2.2 Additional Recommended Security Requirements");
	    subtitlelabel2_2.setFont(new Font("Serif", Font.BOLD, 15));
	    
	    String[] add_result_sr = {"Requirement ID", "Description", "Count", "Adoption"};
	    ArrayList add_sr_result = owl.LoadAdditionalSR(case_nm);
	    Object[][] result_arr4 = new Object[add_sr_result.size()][];
	    for(int i = 0; i < add_sr_result.size(); i++) {
	    	String[] row = (String[]) add_sr_result.get(i);
	    	result_arr4[i] = new Object[row.length + 1];
	    	for(int j = 0; j < row.length; j++)
	    		result_arr4[i][j] = row[j];
	    	result_arr4[i][3] = false;
	    	
	    }
	    DefaultTableModel dtm1 = new DefaultTableModel(result_arr4, add_result_sr);
	    subtable2_2_sr = new JTable(dtm1);
	    subtable2_2_sr.getColumn("Adoption").setCellRenderer(dcr);
	    JCheckBox box1 = new JCheckBox();
	    box1.setHorizontalAlignment(JLabel.CENTER);
	    subtable2_2_sr.getColumn("Adoption").setCellEditor(new DefaultCellEditor(box1));
	    
	    //subtable2_2_sr.getColumnModel().getColumn(0).setCellRenderer(new WordWrapCellRenderer());
	    subtable2_2_sr.getColumnModel().getColumn(1).setCellRenderer(new WordWrapCellRenderer());
	    JScrollPane sub2jsp2 = new JScrollPane(subtable2_2_sr);
	    subtable2_2_sr.getColumnModel().getColumn(0).setPreferredWidth(50);
	    subtable2_2_sr.getColumnModel().getColumn(1).setPreferredWidth(400);
	    subtable2_2_sr.getColumnModel().getColumn(2).setPreferredWidth(5);
	    subtable2_2_sr.getColumnModel().getColumn(3).setPreferredWidth(1);
	    subtable2_2_sr.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
	    //new resizeColumnWidth().resizeColumnWidth(10, subtable2_2_sr);
	    subtable2_2_sr.getModel().addTableModelListener(new CheckBoxModelListener());
	    
	    
	    JPanel secondpanel = new JPanel();
	    secondpanel.setLayout(new BoxLayout(secondpanel, BoxLayout.Y_AXIS));
	    secondpanel.add(subtitlelabel2);
	    secondpanel.add(subtitlelabel2_1);
	    secondpanel.add(sub2jsp1);
	    secondpanel.add(subtitlelabel2_2);
	    secondpanel.add(sub2jsp2);
	  //======================================================================================================= 
	    //JPanel buttonpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));  
	    //JButton save_btn = new JButton("Save");
	    //buttonpanel.add(save_btn, BorderLayout.SOUTH);
	   //=======================================================================================================
	   
	    
	    JPanel totalpanel = new JPanel();
	    //totalpanel.setLayout(new BoxLayout(totalpanel, BoxLayout.Y_AXIS));
	    //totalpanel.add(firstpanel);
	    //totalpanel.add(secondpanel);
	    //totalpanel.add(buttonpanel);
	    
	    //JScrollPane totaljsp = new JScrollPane(totalpanel);
	    //add(totaljsp);
	    
	    //setLayout(new BoxLayout(totalpanel, BoxLayout.Y_AXIS));
	    add(firstpanel);
	    add(secondpanel);
	}
	
	 public HashMap<String, String> getCheckedItems() {
	      return Checked_SR;
	}
	 
	 DefaultTableCellRenderer dcr = new DefaultTableCellRenderer()
	 {
	  public Component getTableCellRendererComponent  // 셀렌더러
	   (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	  {
	   JCheckBox box= new JCheckBox();
	   box.setSelected(((Boolean)value).booleanValue());  
	   box.setHorizontalAlignment(JLabel.CENTER);
	   return box;
	  }
	 };

   public class CheckBoxModelListener implements TableModelListener {

        public void tableChanged(TableModelEvent e) {
            int row = e.getFirstRow();
            int column = e.getColumn();
            if (column == BOOLEAN_COLUMN) {
                TableModel model = (TableModel) e.getSource();
                String columnName = model.getColumnName(column);
                Boolean checked = (Boolean) model.getValueAt(row, column);
                String SR_name = model.getValueAt(row, 0).toString();
            	String SR_description = model.getValueAt(row, 1).toString();
                if (checked) {
                	Checked_SR.put(SR_name, SR_description);
                }
                else {
                    Checked_SR.remove(SR_name);
                }
            }
        }
    }
	 
}
