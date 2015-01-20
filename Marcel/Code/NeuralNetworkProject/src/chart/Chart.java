package chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

public class Chart extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JPanel panel;
	private GroupLayout gl_contentPane;
	private JButton accButton;
	private JComboBox<String> comboExperiment;
	private JComboBox<String> comboNode;
	private int sample_period;
	private JButton btnLoadData;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DataReader dr = new DataReader();
					Chart frame = new Chart(dr.countNodes("measurement"), dr.countExperiments("measurement"), dr.getSamplePeriod("measurement"));
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Chart(int nodeCount, int experimentCount, int sample_period) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		this.sample_period= sample_period;
		
		panel = new JPanel();
		panel.setBackground(Color.WHITE);
		
		accButton = new JButton("Acceleration");
		accButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ACC();
			}
		});
		
		comboExperiment = new JComboBox<String>();
		for(int i=1; i<experimentCount; i++){
			comboExperiment.addItem("Experiment " + i);
		}
		comboExperiment.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				combo();
			}
		});
		
		JButton btnTable = new JButton("Table");
		btnTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				TABLE();
			}
		});
		
		comboNode = new JComboBox<String>();
		for(int i=1; i<=nodeCount; i++){
			comboNode.addItem("Node " + i);
		}
		comboNode.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				combo();
			}
		});
		
		JButton btnPrint = new JButton("Copy Chart");
		btnPrint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				copyImage();
			}
		});		
		
		btnLoadData = new JButton("Load Data");
		btnLoadData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				loadData();
			}
		});
		
		gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(accButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(btnTable, GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
						.addComponent(btnPrint, GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
						.addComponent(comboNode, 0, 103, Short.MAX_VALUE)
						.addComponent(comboExperiment, 0, 103, Short.MAX_VALUE)
						.addComponent(btnLoadData, GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel, GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE)
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(accButton)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnTable)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnPrint)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnLoadData)
							.addPreferredGap(ComponentPlacement.RELATED, 245, Short.MAX_VALUE)
							.addComponent(comboNode, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(comboExperiment, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(panel, GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE))
					.addContainerGap())
		);
		panel.setLayout(new BorderLayout(0, 0));
		
		contentPane.setLayout(gl_contentPane);
	}
	
	/**
	 * Shows the chart in the panel.
	 * 
	 * @param c
	 */
	public void drawPanel(JPanel c){
		
		panel.removeAll();
		
		panel.add(c, BorderLayout.CENTER);
				
		contentPane.repaint();contentPane.validate();
		contentPane.setLayout(gl_contentPane);
		
	}//Ende drawPanel
	
	/**
	 * Behaviour of the acceleration button.
	 * Shows the acceleration chart.
	 */
	private void ACC(){
		if(comboExperiment.getItemCount()==0 || comboNode.getItemCount()==0){return;}
		drawPanel(new ChartContent(comboNode.getSelectedIndex(), comboExperiment.getSelectedIndex()+1, sample_period));
	}//Ende ACC()
	
	/**
	 * Behaviour of the table button.
	 * Shows a table with the values for index, time, acceleration of a measurement.
	 */
	private void TABLE(){
		if(comboExperiment.getItemCount()==0 || comboNode.getItemCount()==0){return;}
		drawPanel(new DataTable(comboNode.getSelectedIndex(), comboExperiment.getSelectedIndex()+1));
	}//Ende TABLE()
	/**
	 * Behaviour of the comboboxes.
	 * Changes the origin [Experiment, Node] of the view.
	 */
	private void combo(){
		if(comboExperiment.getItemCount()==0 || comboNode.getItemCount()==0){return;}
		drawPanel(new ChartContent(comboNode.getSelectedIndex(), comboExperiment.getSelectedIndex()+1, sample_period));
	}//Ende combo()
	
	/**
	 * Behaviour of the copy button.
	 * Copies the view into the clipboard.
	 */
	public void copyImage(){
		int w = panel.getWidth();
	    int h = panel.getHeight();
	    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	    Graphics2D g = bi.createGraphics();
	    panel.paint(g);
	    @SuppressWarnings("unused")
		CopyImage ci = new CopyImage(bi);
	}//Ende copyImage()
	
	/**
	 * Behaviour of the load data button.
	 * Looks whether there is new data in the folder.
	 */
	private void loadData(){
		
		DataReader dr = new DataReader();
		int experimentCount = dr.countExperiments("measurement");
		if(experimentCount<=1){return;}
		comboExperiment.removeAllItems();
		for(int i=1; i<experimentCount; i++){
			comboExperiment.addItem("Experiment " + i);
		}
		int nodeCount = dr.countNodes("measurement");
		if(nodeCount<=0){return;}
		comboNode.removeAllItems();
		for(int i=1; i<=nodeCount; i++){
			comboNode.addItem("Node " + i);
		}
		
	}//Ende loadData()
	
	
}
