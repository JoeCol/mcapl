package responsibility;

import javax.swing.JPanel;

import ail.mas.DefaultEnvironment;
import ail.syntax.Action;
import ail.syntax.Unifier;
import ail.util.AILexception;

public class CleaningWorld extends DefaultEnvironment
{
	Settings currentSettings = new Settings();
	WorldCell[][] world;
	JPanel visual;
	
	//TODO implement settings 
	public void loadSettings()
	{
		
	}
	
	public CleaningWorld()
	{
		world = new WorldCell[currentSettings.getWidth()][currentSettings.getHeight()];
	}
	
	public void simStep()
	{
		for (int x = 0; x < world.length; x++)
		{
			for (int y = 0; x < world[x].length; y++)
			{
				world[x][y].setChangeOfDirt(currentSettings.getChangeOfDirt());
			}
		}
	}

	public CleaningWorld(JPanel gui)
	{
		world = new WorldCell[currentSettings.getWidth()][currentSettings.getHeight()];
		visual = gui;
		visual.removeAll();
	}
	
	public Unifier executeAction(String agName, Action act) throws AILexception {
	   	Unifier theta = new Unifier();
	   	System.out.println(agName + ": executes:" + act.fullstring());
	   	super.executeAction(agName, act);
	   	 
    	return theta;
    }
}
