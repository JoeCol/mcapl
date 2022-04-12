package responsibility;

import javax.swing.JLabel;
import javax.swing.JPanel;

import ail.mas.AIL;
import ail.mas.MAS;
import ail.syntax.ast.GroundPredSets;
import ail.util.AILConfig;
import ajpf.MCAPLcontroller;

public class CleaningWorldThread implements Runnable
{
	private CleaningWorld world;
	private String configfile;
	private MCAPLcontroller mccontrol;
	private JPanel gui;
	
	@Override
	public void run() {
		if (gui != null)
		{
			world = new CleaningWorld();
		}
		else
		{
			world = new CleaningWorld(gui);
		}
		GroundPredSets.clear();
		AILConfig config = new AILConfig(configfile);
		AIL.configureLogging(config);
		
		// Create a controller
		mccontrol = new MCAPLcontroller(config, "");
	
		// Create the initial state of the multi-agent program.
		MAS mas = AIL.AILSetup(config, mccontrol);
		
		// Begin!
		mccontrol.begin(); 
	}
	
	public CleaningWorldThread(String config)
	{
		configfile = config;
	}

	public CleaningWorldThread(String config, JPanel pnlWorld)
	{
		configfile = config;
		gui = pnlWorld;
	}

	public void sendStop() 
	{
		mccontrol.stop();		
	}
}
