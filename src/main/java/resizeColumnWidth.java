import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class resizeColumnWidth {
	public void resizeColumnWidth(int width, JTable table) { 
		final TableColumnModel columnModel = table.getColumnModel();
		for (int column = 0; column < table.getColumnCount(); column++) { 
			if(width == 0)
				width = 20; // Min width
			else {
				for (int row = 0; row < table.getRowCount(); row++) {
					TableCellRenderer renderer = table.getCellRenderer(row, column);
					Component comp = table.prepareRenderer(renderer, row, column);
					width = Math.max(comp.getPreferredSize().width +1 , width);
				} 				
			}

			columnModel.getColumn(column).setPreferredWidth(width); 
		}
	}
}
