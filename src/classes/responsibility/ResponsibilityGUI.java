package responsibility;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.FlowLayout;
import javax.swing.border.BevelBorder;

import ail.mas.AIL;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import java.awt.GridLayout;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class ResponsibilityGUI {

	private JFrame frmResponsibilityGwen;
	private Thread cwtThread;
	//private CleaningWorldThread cwt = new CleaningWorldThread("/src/classes/responsibility/responsibility.ail");
	private CleaningWorldThread cwt = new CleaningWorldThread("/src/classes/responsibility/test.ail");
	private boolean started = false;
	private File settingsFile = new File("cleaning.settings");

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) 
	{
		EventQueue.invokeLater(new Runnable() 
		{
			public void run() {
				try 
				{
					ResponsibilityGUI window = new ResponsibilityGUI();
					window.frmResponsibilityGwen.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ResponsibilityGUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmResponsibilityGwen = new JFrame();
		frmResponsibilityGwen.setTitle("Responsibility Gwen");
		frmResponsibilityGwen.setBounds(100, 100, 701, 521);
		frmResponsibilityGwen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frmResponsibilityGwen.setJMenuBar(menuBar);
		
		JMenu mnNewMenu = new JMenu("File");
		menuBar.add(mnNewMenu);
		
		JMenuItem mntmNewMenuItem = new JMenuItem("Settings");
		mntmNewMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings s = new Settings();
				if (settingsFile.exists())
				{
					ObjectInputStream is;
					try {
						is = new ObjectInputStream(new FileInputStream(settingsFile));
						s = (Settings)is.readObject();
					} catch (FileNotFoundException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					} catch (IOException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					} catch (ClassNotFoundException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}
					
				}
				SettingsDialog sd = new SettingsDialog(s);
				sd.setVisible(true);
				System.out.println("Closed?");
				if (sd.updateSettings)
				{
					ObjectOutputStream os;
					try {
						os = new ObjectOutputStream(new FileOutputStream(settingsFile));
						os.writeObject(s);
						os.close();
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		mnNewMenu.add(mntmNewMenuItem);
		
		JMenu mnNewMenu_1 = new JMenu("Simulation");
		menuBar.add(mnNewMenu_1);
		
		JMenuItem mntmNewMenuItem_2 = new JMenuItem("Start");
		mnNewMenu_1.add(mntmNewMenuItem_2);
		
		JMenuItem mntmNewMenuItem_3 = new JMenuItem("Stop");
		mnNewMenu_1.add(mntmNewMenuItem_3);
		
		JPanel panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		frmResponsibilityGwen.getContentPane().add(panel, BorderLayout.SOUTH);
		
		JButton btnNewButton = new JButton("Start");
		panel.add(btnNewButton);
		
		JLabel lblNewLabel = new JLabel("Simulation delay");
		panel.add(lblNewLabel);
		
		JSlider slider = new JSlider();
		slider.setMinimum(300);
		slider.setMaximum(3000);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				cwt.setSimulationDelay(slider.getValue());
			}
		});
		slider.setMajorTickSpacing(200);
		slider.setSnapToTicks(true);
		slider.setPaintTicks(true);
		slider.setValue(0);
		panel.add(slider);
		frmResponsibilityGwen.getContentPane().add(cwt, BorderLayout.CENTER);
		
		btnNewButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				if (!started)
				{
					//frmResponsibilityGwen.getContentPane().removeAll();
					//cwt = new CleaningWorldThread("/src/classes/responsibility/responsibility.ail");
					cwtThread = new Thread(cwt);
					cwtThread.start();
					started = true;
					
				}
				else
				{
					cwt.sendStop();
					started = false;
				}
			}
		});
		
	}

}
