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
	public CleaningWorldThread cwt;
	private JButton btnNewButton = new JButton("Start");
	private boolean started = false;
	private String saveDir = "";

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
					String ailFile = "src/classes/responsibility/TheoryImpl/test.ail";
					int delay = 300;
					String saveLoc = "output/";
					int simSteps = 10000;
					int dirtInt = 15; 
					int badDirtInt = 5;
					String worldLoc = "src/classes/responsibility/TheoryImpl/10Rooms.world";
					boolean autoStart = false;
					for (int i = 0; i < args.length; i++)
					{
						switch (args[i].toLowerCase())
						{
						case "ailfile":
							ailFile = args[++i]; 
							break;
						case "delay":
							delay = Integer.valueOf(args[++i]);
							break;
						case "simsteps":
							simSteps = Integer.valueOf(args[++i]);
							break;
						case "saveloc":
							saveLoc = args[++i];
							break;
						case "dirtinterval":
							dirtInt = Integer.valueOf(args[++i]);
							badDirtInt = Integer.valueOf(args[++i]);
							break;
						case "worldlocation":
							worldLoc = args[++i];
							break;
						case "autostart":
							autoStart = true;
							break;
						default:
							System.out.println("Unrecognised argument: " + args[i]);
						}
					}
					ResponsibilityGUI window = new ResponsibilityGUI(ailFile, delay, saveLoc, simSteps, dirtInt, badDirtInt, worldLoc, autoStart);
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
	public ResponsibilityGUI(String ailFile, int delay, String saveLoc, int simSteps, int dirtInt, int badDirtInt, String worldLoc, boolean start) {
		cwt = new CleaningWorldThread(ailFile, delay, simSteps, dirtInt, badDirtInt, worldLoc);
		saveDir = saveLoc;
		initialize();
		if (start)
		{
			btnNewButton.doClick();
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmResponsibilityGwen = new JFrame();
		frmResponsibilityGwen.setTitle("Responsibility Gwen");
		frmResponsibilityGwen.setBounds(100, 100, 701, 521);
		frmResponsibilityGwen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		
		JPanel panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		frmResponsibilityGwen.getContentPane().add(panel, BorderLayout.SOUTH);
		
		panel.add(btnNewButton);
		
		JLabel lblSimStep = new JLabel("Steps Remaining: " + cwt.getRemainingSteps());
		panel.add(lblSimStep);
		cwt.addSimListeners(new UpdateToSimulationTime() {
			@Override
			public void simulationTimeUpdate(int time) {
				lblSimStep.setText("Steps Remaining: " + time);
				if (time <= 0)
				{
					cwt.sendStop(saveDir);
					System.exit(0);
				}
			}});
		
		JLabel lblDirt = new JLabel("Dirt: 0 Bad Dirt: 0");
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
