package responsibility.TheoryImpl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import ail.mas.AIL;
import ail.mas.AILEnv;
import ail.mas.MAS;
import ail.syntax.ast.GroundPredSets;
import ail.util.AILConfig;
import ajpf.MCAPLcontroller;
import ajpf.util.AJPFLogger;

public class ResponsibilityGUI {

	private JFrame frmResponsibilityGwen;
	private CleaningPanel visual = new CleaningPanel();
	private static CleaningWorld world;
	private static MCAPLcontroller mccontrol;
	private static MAS mas;
	
	
	/**
	 * Set up a multi-agent system from a configuration file.
	 * @param config
	 * @return
	 */
	public static MAS AILSetup(AILConfig config, MCAPLcontroller control) {
		if (! config.containsKey("suppress_version") || config.get("suppress_version").equals("false")) {
			AIL.print_version_info();
		}

		// First we need to build the multi-agent system
		MAS mas = AIL.buildMAS(config);
		mas.setController(control);
		
		// Then, if necessary, we attach an environment
		if (config.containsKey("env")) {
			try {
				AILEnv env = world;
				env.configure(config);
				env.init_before_adding_agents();
				mas.setEnv(env);
				// System.err.println("setting env");
				control.setMAS(mas);
				// System.err.println("a");
				env.init_after_adding_agents();
				// System.err.println("b");
				// System.err.println(mas);
				control.initialiseSpec();
				// System.err.println("c");
				env.setMAS(mas);
				// System.err.println("set mas");
			} catch (Exception e) {
				AJPFLogger.severe("ail.mas.AIL", e.getMessage());
				System.exit(1);
			}
		}
		mas.configure(config);
		return mas;
	}
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) 
	{
		String saveLoc = "output/";
		int simSteps = 10000;
		int dirtInt = 15; 
		int badDirtInt = 5;
		String worldLoc = "14Rooms.world";
		int simSpeed = 0;
		boolean gui = true;
		String ailFile = "/responsibility.ail";
		
		for (int i = 0; i < args.length; i++)
		{
			switch (args[i].toLowerCase())
			{
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
			case "delay":
				simSpeed = Integer.valueOf(args[++i]);
				break;
			case "nogui":
				gui = false;
				break;
			case "ailfile":
				ailFile = args[++i];
				break;
			default:
				System.out.println("Unrecognised argument: " + args[i]);
			}
		}
		
		world = new CleaningWorld(simSteps, dirtInt, badDirtInt, worldLoc, saveLoc, simSpeed);
		if (gui)
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
		GroundPredSets.clear();
		AILConfig config = new AILConfig(ailFile);
		AIL.configureLogging(config);
		
		// Create a controller
		mccontrol = new MCAPLcontroller(config, "");
	
		// Create the initial state of the multi-agent program.
		mas = AILSetup(config, mccontrol);
		//System.out.println(mccontrol.getScheduler().toString());
		
		mccontrol.begin();
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

		JPanel panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		frmResponsibilityGwen.getContentPane().add(panel, BorderLayout.SOUTH);
		
		JLabel lblSimStep = new JLabel();
		panel.add(lblSimStep);
		JLabel lblDirt = new JLabel("Dirt: 0 Bad Dirt: 0");
		panel.add(lblDirt);
		
		frmResponsibilityGwen.getContentPane().add(visual, BorderLayout.CENTER);
		
		visual.setLayout(new GridLayout(world.getHeight(), world.getWidth(), 0, 0));
		world.addWorldListeners(new UpdateToWorld()
		{

			@Override
			public void worldUpdate(int time, int dirt, int badDirt, WorldCell[][] world,
					HashMap<String, Pair<Integer, Integer>> agentLocations,
					HashMap<String, Color> agentColours) {
				lblSimStep.setText("Steps Remaining: " + time);
				lblDirt.setText("Dirt: " + dirt + " Bad Dirt: " + badDirt);
				visual.setWorld(world, agentLocations, agentColours);
				
			}

		});
		
		
	}

}
