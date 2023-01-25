package responsibility.TheoryImpl;

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
import javax.swing.JTabbedPane;

public class ResponsibilityGUI {

	private JFrame frmResponsibilityGwen;
	private JSlider slider = new JSlider();
	private Thread cwtThread;
	//private CleaningWorldThread cwt = new CleaningWorldThread("/src/classes/responsibility/TheoryImpl/responsibility.ail");
	private CleaningWorldThread cwt = new CleaningWorldThread("/src/classes/responsibility/TheoryImpl/test.ail",300);
	private boolean started = false;
	private File settingsFile = new File("cleaning.settings");
	public String saveDir = "";

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
					if (args.length > 0)
					{
						window.saveDir = args[1];
					}
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
		
		JLabel lblNewLabel = new JLabel("Action time");
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				lblNewLabel.setText("Action time: " + slider.getValue() + "ms");
			}});
		slider.setMinimum(50);
		slider.setMaximum(3000);
		slider.setValue(300);
		slider.setMajorTickSpacing(50);
		slider.setSnapToTicks(true);
		slider.setPaintTicks(true);
		panel.add(slider);
		
		panel.add(lblNewLabel);
		
		
		
		JLabel lblSimStep = new JLabel();
		panel.add(lblSimStep);
		cwt.addSimListeners(new UpdateToSimulationTime() {
			@Override
			public void simulationTimeUpdate(int time) {
				lblSimStep.setText("Steps Remaining: " + time);
				if (time <= 0)
				{
					cwt.sendStop(saveDir);
				}
			}});
		
		JLabel lblDirt = new JLabel();
		panel.add(lblDirt);
		
		cwt.addDirtListeners(new UpdateToDirtLevels() {
			@Override
			public void dirtLevelUpdate(int dirt, int badDirt) {
				lblDirt.setText("Dirt: " + dirt + " Bad Dirt: " + badDirt);
				
			}});
		
		frmResponsibilityGwen.getContentPane().add(cwt, BorderLayout.CENTER);
		
		btnNewButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				slider.setEnabled(!started);
				if (!started)
				{
					//frmResponsibilityGwen.getContentPane().removeAll();
					//cwt = new CleaningWorldThread("/src/classes/responsibility/responsibility.ail");
					cwt.setSimulationDelay(slider.getValue());
					cwtThread = new Thread(cwt);
					cwtThread.start();
					started = true;
					
				}
				else
				{
					cwt.sendStop(saveDir);
					started = false;
				}
			}
		});
		
	}

}
