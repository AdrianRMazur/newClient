package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import btclient.BTClient;
import btclient.Serialization;

public class DisplayPanel extends JFrame  {

	
	public static JTextField percent; 	
	public static Object data[][];
	public static Object data2[][];
	private JButton close; 
	
	public DisplayPanel(String title) {
		super(title);
		
		builder(); 
		
		BTClient x = new BTClient (FirstPanel.torrent.getText(), FirstPanel.saved.getText(), FirstPanel.ip.getText());
		
		final Thread thread = new Thread ( x);
	
		
		thread.start();
	
		
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {


				
				
				try {
					Serialization.serialize(BTClient.downloaded, BTClient.fileName);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				dispose();
				
				System.exit(1);
				
			}
		});
		
	
		
	}
	
	
	
	public static void error(String x) {
		
		JOptionPane.showMessageDialog(null, x, "Error",
				JOptionPane.ERROR_MESSAGE);
	}
	

	
	
	private void builder(){
		JLabel info = new JLabel("Percentage Downloaded:");
		percent = new JTextField();
		percent.setEditable(false);
		percent.setForeground(Color.RED);
		JLabel info2 = new JLabel ("Download Peers");
		JLabel info3 = new JLabel ("Upload Peers");
		
		close = new JButton ("Close");
		data = new Object [20][3];
		data2 = new Object [20][3];
		
		String[] columnNames1 = { "IP", "PORT", "CONNECTED?"  };
		final JTable table1 = new JTable(data, columnNames1);
		table1.setPreferredScrollableViewportSize(new Dimension(100, 100));
		table1.setFillsViewportHeight(true);
		table1.setEnabled(false);
		JScrollPane scrollPane1 = new JScrollPane(table1);
		/*
		final JTable table2 = new JTable(data2, columnNames1);
		table2.setPreferredScrollableViewportSize(new Dimension(100, 100));
		table2.setFillsViewportHeight(true);
		table2.setEnabled(false);
		JScrollPane scrollPane2 = new JScrollPane(table2);
		*/
		
		JPanel group3 = new JPanel();
		group3.setLayout(new BoxLayout(group3, BoxLayout.X_AXIS));
		group3.add(info);
		group3.add(Box.createRigidArea(new Dimension(10, 0)));
		group3.add(percent);
		group3.add(Box.createRigidArea(new Dimension(10, 0)));
		
		
		JPanel finalpanel = new JPanel();
		finalpanel.setLayout(new BoxLayout(finalpanel, BoxLayout.Y_AXIS));
		finalpanel.add(group3);
		info.setAlignmentX(CENTER_ALIGNMENT);
		finalpanel.add(info2);
		info2.setAlignmentX(CENTER_ALIGNMENT);
		finalpanel.add(scrollPane1);
		finalpanel.add(Box.createRigidArea(new Dimension(0, 10)));
		finalpanel.add(info3);
		info3.setAlignmentX(CENTER_ALIGNMENT);
		//finalpanel.add(scrollPane2);
		//finalpanel.add(Box.createRigidArea(new Dimension(0, 10)));
		finalpanel.add(close);
		close.setAlignmentX(CENTER_ALIGNMENT);
		
		this.add(finalpanel);
		
		
	
		
	}	

}
