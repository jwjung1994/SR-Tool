import java.awt.Font;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class SRFrame extends JFrame{
	public SRFrame(String title, HashMap<String, String> SR_Map) {
		
		
		JLabel titlelabel = new JLabel(title);
	    titlelabel.setFont(new Font("Serif", Font.BOLD, 30));
	    
	    
	    String[] headers = {"Requirement ID", "Description"};
	    Object[][] contents = new Object[SR_Map.size()][2];
	    
	    int index = 0;
        for(Entry<String, String> elem : SR_Map.entrySet()){
            //System.out.println("키 : " + elem.getKey() + "값 : " + elem.getValue());
            contents[index][0] = elem.getKey().toString();
            contents[index][1] = elem.getValue().toString();
            index++;
        }
        DefaultTableModel dtm = new DefaultTableModel(contents, headers);
        JTable jtable = new JTable(dtm);
        
        jtable.getColumnModel().getColumn(1).setCellRenderer(new WordWrapCellRenderer());
        
        jtable.getColumnModel().getColumn(0).setPreferredWidth(20);
        jtable.getColumnModel().getColumn(1).setPreferredWidth(80);
        JScrollPane jsp = new JScrollPane(jtable);
        
        
	    JPanel panel = new JPanel();
	    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	    panel.add(titlelabel);
	    panel.add(jsp);
	    
	    add(panel);
	    setSize(900, 400);
	    setVisible(true);
	}
}
