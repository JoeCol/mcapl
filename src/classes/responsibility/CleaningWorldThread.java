package responsibility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Frame;

import javax.swing.JPanel;
import javax.swing.Timer;

import ail.mas.AIL;
import ail.mas.AILEnv;
import ail.mas.MAS;
import ail.semantics.AILAgent;
import ail.syntax.Literal;
import ail.syntax.NumberTerm;
import ail.syntax.ast.GroundPredSets;
import ail.util.AILConfig;
import ajpf.MCAPLcontroller;
import ajpf.util.AJPFLogger;

public class CleaningWorldThread extends JPanel implements Runnable
{
	private CleaningWorld world;
	private String configfile;
	private MCAPLcontroller mccontrol;
	private MAS mas;
	private int simulationDelay;
	private Timer envUpdate;
	Settings currentSettings;
	File settingsFile = new File("cleaning.settings");
	
	public void loadSettings()
	{
		if (settingsFile.exists())
		{
			ObjectInputStream is;
			try {
				is = new ObjectInputStream(new FileInputStream(settingsFile));
				currentSettings = (Settings)is.readObject();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public Settings getSettings()
	{
		return currentSettings;
	}
	
	
	@Override
	public void run() {
		loadSettings();
		world = new CleaningWorld(currentSettings);
		setLayout(new GridLayout(world.getHeight(), world.getWidth(), 0, 0));
		
		ActionListener updateWorld = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) {
				//Add Dirt
				for (int x = 0; x < world.getWidth(); x++)
				{
					for (int y = 0; y < world.getHeight(); y++)
					{
						world.getCell(x, y).setChangeOfDirt(currentSettings.getDirtAppearanceChange());
					}
				}
				//Process Actions
				
				//Send Responses
				
				//Refresh Screen
				invalidate();
				repaint();
			}
		};
		envUpdate = new Timer(simulationDelay, updateWorld);
		
		GroundPredSets.clear();
		AILConfig config = new AILConfig(configfile);
		AIL.configureLogging(config);
		
		// Create a controller
		mccontrol = new MCAPLcontroller(config, "");
	
		// Create the initial state of the multi-agent program.
		mas = AILSetup(config, mccontrol);
		System.out.println(mccontrol.getScheduler().toString());
		
		// Begin!
		envUpdate.start();
		mccontrol.begin(); 
		mas.cleanup();
	}
	
	/**
	 * Set up a multi-agent system from a configuration file.
	 * @param config
	 * @return
	 */
	public MAS AILSetup(AILConfig config, MCAPLcontroller control) {
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
	
	public CleaningWorldThread(String config)
	{
		configfile = config;
		setDoubleBuffered(true);
		//Grid Layout goes for y number of rows, then x number of columns
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.drawString(java.time.ZonedDateTime.now().toString(),5,g.getFontMetrics().getHeight() - 5);
		g.setColor(Color.BLACK);
		if (world != null)
		{
			int widthOfCell = getWidth() / world.getWidth();
			int heightOfCell = (getHeight() - g.getFontMetrics().getHeight()) / world.getHeight();
			
			for (int x = 0; x < world.getWidth(); x++)
			{
				for (int y = 0; y < world.getHeight(); y++)
				{
					if (!world.getCell(x, y).hasDirt())
					{
						g.setColor(Color.lightGray);
						g.fillRect(1+(x * widthOfCell), g.getFontMetrics().getHeight() + (y * heightOfCell), widthOfCell, heightOfCell);
						g.setColor(Color.BLACK);
					}
					if (!world.getCell(x,y).isTraversable())
					{
						g.fillRect(1+(x * widthOfCell), g.getFontMetrics().getHeight() + (y * heightOfCell), widthOfCell, heightOfCell);
					}
					else
					{
						g.drawRect(1+(x * widthOfCell), g.getFontMetrics().getHeight() + (y * heightOfCell), widthOfCell, heightOfCell);
						g.drawString("Zone:" + world.getCell(x, y).getZoneNumber(), 2+(x * widthOfCell), (g.getFontMetrics().getHeight() * 2) + (y * heightOfCell));
					}
				}
			}
			
			for (AILAgent ag : world.getAgents())
			{
				Iterator<Literal> lit = ag.getBB().getPercepts();
				
				while (lit.hasNext())
				{
					try
					{
						Literal l = lit.next();
						if (l.getFunctor().equalsIgnoreCase("at"))
						{
							int x = (int)((NumberTerm)l.getTerm(0)).solve();
							int y = (int)((NumberTerm)l.getTerm(1)).solve();
							int tx = 1+(x * widthOfCell);
							int ty = g.getFontMetrics().getHeight() + (y * heightOfCell);
							g.setColor(world.getAgentColor(ag));
							g.fillOval(tx, ty, widthOfCell, heightOfCell);
							g.setColor(Color.BLACK);
							g.drawString(ag.getAgName(), tx, ty + (heightOfCell / 2));
						}
					}
					catch (Exception ex)
					{
						
					}
				}
			}
		}
	}


	public void sendStop() 
	{
		envUpdate.stop();
		mccontrol.stop();		
	}

	public void setSimulationDelay(int value) 
	{
		simulationDelay = value;
		if (world != null)
		{
			world.setSimulationDelay(value);
		}
	}
}
