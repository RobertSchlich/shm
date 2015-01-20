package chart;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class DataTable extends JPanel{
	
	private static final long serialVersionUID = 1L;
	private JTable table;
	
	/**
	 * Creates the table with the data for index, time, acceleration of every measurement.
	 * 
	 * @param nodeIndex
	 * @param experimentIndex
	 */
	public DataTable(int nodeIndex, int experimentIndex) {
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		getData(nodeIndex, experimentIndex);
		
		scrollPane.setViewportView(table);
	}//Ende DataTable()
	
	private void getData(int nodeIndex, int experimentIndex){
		
		DefaultTableModel model = new DefaultTableModel();
		model.addColumn("index");
		model.addColumn("time [ms]");
		model.addColumn("acc [g]");
		
		try {
			DataReader dr = new DataReader(nodeIndex, experimentIndex);
			dr.readData("testing");
			
			for(int i=0; i<dr.getTime().size(); i++){
				
				double acc = dr.getAcceleration().get(i);
				long time = dr.getTime().get(i);
				
				model.addRow(new Object[0]);
				
				model.setValueAt(i, i, 0);
				model.setValueAt(time, i, 1);
				model.setValueAt(acc, i, 2);
				
			}
							
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		table = new JTable(model);
		
	}//Ende getData()

}
