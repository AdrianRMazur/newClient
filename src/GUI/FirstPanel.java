package GUI;

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
import javax.swing.JTextField;

import btclient.BTClient;


public class FirstPanel extends JFrame{

	public static JTextField torrent; 
	public static JTextField saved; 
	public  static JTextField ip;
	private JButton download; 
	private JButton help; 
	private JButton cancel; 
	
	
	public FirstPanel(String title) {
		super (title);
		builder(); 
		

		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				dispose();
				System.exit(1);

			}
		});

		help.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String x = "-Type the torrent name and the destination file \n-Follow the prompts"
						+ "\n-Kosti & Adrian 2014";
				JOptionPane.showMessageDialog(null, x, "Help Window",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
		download.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e1) {

				if (torrent.getText().equals("") || saved.getText().equals("")){
					error(); 
				}
				else {
					
					nextdisplay();
					dispose();
					DisplayPanel.error("Warning! Torrenting may be illegal. Proceed at your own risk");
				}
			}
		});
	}
	
	
	private void nextdisplay(){
		JFrame displayframe= new DisplayPanel("Ru BT Client");
		displayframe.pack();
		displayframe.setVisible(true);
		displayframe.setLocationRelativeTo(null);
		displayframe.setMinimumSize(new Dimension(400, 300));
		displayframe.setResizable(true);
		displayframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	
	private void error() {
		String x = "Error: Please enter the torrent file and saved file name";
		JOptionPane.showMessageDialog(null, x, "Error",
				JOptionPane.ERROR_MESSAGE);
	}
	
	

	private void builder(){
		torrent = new JTextField(); 
		saved = new JTextField(); 
		download = new JButton ("Begin Torrent");
		help = new JButton ("Help");
		cancel = new JButton ("Cancel");
		ip = new JTextField(); 
		
		JLabel info1 = new JLabel ("Enter a torrent file name and a save name");
		JLabel info2 = new JLabel ("Torrent File:");
		JLabel info3 = new JLabel ("Save File:");
		JLabel info4 = new JLabel ("Local IP:");
		JLabel info5 = new JLabel ("A local IP can also be entered");
		
		
		JPanel group1 = new JPanel();
		group1.setLayout(new BoxLayout(group1, BoxLayout.X_AXIS));
		group1.add(info2);
		group1.add(Box.createRigidArea(new Dimension(10, 0)));
		group1.add(torrent);
		group1.setMaximumSize(new Dimension(300, 30));
		
		JPanel group2 = new JPanel();
		group2.setLayout(new BoxLayout(group2, BoxLayout.X_AXIS));
		group2.add(info3);
		group2.add(Box.createRigidArea(new Dimension(10, 0)));
		group2.add(saved);
		group2.setMaximumSize(new Dimension(300, 30));
		
		JPanel group4 = new JPanel();
		group4.setLayout(new BoxLayout(group4, BoxLayout.X_AXIS));
		group4.add(info4);
		group4.add(Box.createRigidArea(new Dimension(10, 0)));
		group4.add(ip);
		group4.setMaximumSize(new Dimension(300, 30));
		
		JPanel group3 = new JPanel();
		group3.setLayout(new BoxLayout(group3, BoxLayout.X_AXIS));
		group3.add(download);
		group3.add(Box.createRigidArea(new Dimension(10, 0)));
		group3.add(help);
		group3.add(Box.createRigidArea(new Dimension(10, 0)));
		group3.add(cancel);
		
		
		JPanel finalpanel = new JPanel();
		finalpanel.setLayout(new BoxLayout(finalpanel, BoxLayout.Y_AXIS));
		
		finalpanel.add(Box.createRigidArea(new Dimension(0, 10)));
		finalpanel.add(info1);
		info1.setAlignmentX(CENTER_ALIGNMENT);
		finalpanel.add(info5);
		info5.setAlignmentX(CENTER_ALIGNMENT);
		finalpanel.add(Box.createRigidArea(new Dimension(0, 10)));
		finalpanel.add(group1);
		group1.setAlignmentX(CENTER_ALIGNMENT);
		finalpanel.add(Box.createRigidArea(new Dimension(0, 10)));
		finalpanel.add(group2);
		group2.setAlignmentX(CENTER_ALIGNMENT);
		finalpanel.add(Box.createRigidArea(new Dimension(0, 10)));
		finalpanel.add(group4);
		group4.setAlignmentX(CENTER_ALIGNMENT);
		finalpanel.add(Box.createRigidArea(new Dimension(0, 10)));
		finalpanel.add(group3);
		group3.setAlignmentX(CENTER_ALIGNMENT);
		
		this.add(finalpanel);
		
	}
	
	
	public static void main (String [] args)  {
		
		
		JFrame firstframe = new FirstPanel("Ru BT Client");
		firstframe.pack();
		firstframe.setVisible(true);
		firstframe.setLocationRelativeTo(null);
		firstframe.setMinimumSize(new Dimension(400, 300));
		firstframe.setResizable(true);
		firstframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	
}

}
