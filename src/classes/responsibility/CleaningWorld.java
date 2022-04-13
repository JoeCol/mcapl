package responsibility;

import java.util.ArrayList;

import ail.mas.DefaultEnvironment;
import ail.mas.scheduling.RoundRobinScheduler;
import ail.syntax.Action;
import ail.syntax.Unifier;
import ail.util.AILexception;
import ajpf.MCAPLJobber;

interface UpdateToWorld{
	void worldUpdate();
}

public class CleaningWorld extends DefaultEnvironment implements MCAPLJobber
{
	Settings currentSettings = new Settings();
	WorldCell[][] world;
	RoundRobinScheduler rrs = new RoundRobinScheduler();
	ArrayList<UpdateToWorld> listeners = new ArrayList<UpdateToWorld>();
	
	public void addWorldListeners(UpdateToWorld u)
	{
		listeners.add(u);
	}
	
	//TODO implement settings 
	public void loadSettings()
	{
		
	}
	
	@Override
	public void init_before_adding_agents() 
	{
		
	}

	public CleaningWorld()
	{
		world = new WorldCell[currentSettings.getWidth()][currentSettings.getHeight()];
		for (int x = 0; x < world.length; x++)
		{
			for (int y = 0; y < world[x].length; y++)
			{
				world[x][y] = new WorldCell();
			}
		}
		loadSettings();
		setup_scheduler(this, rrs);
		rrs.addJobber(this);
	}
	
	public Unifier executeAction(String agName, Action act) throws AILexception {
	   	Unifier theta = new Unifier();
	   	System.out.println(agName + ": executes:" + act.fullstring());
	   	super.executeAction(agName, act);
    	return theta;
    }

	@Override
	public int compareTo(MCAPLJobber o) 
	{
		return o.hashCode() - hashCode();
	}

	@Override
	public void do_job() 
	{
		for (int x = 0; x < world.length; x++)
		{
			for (int y = 0; y < world[x].length; y++)
			{
				world[x][y].setChangeOfDirt(currentSettings.getChangeOfDirt());
			}
		}
		for (UpdateToWorld u : listeners)
		{
			u.worldUpdate();
		}
	}

	@Override
	public String getName() {
		return "Cleaning Environment Jobber";
	}

	public int getHeight() 
	{
		return currentSettings.getHeight();
	}

	public int getWidth() 
	{
		return currentSettings.getWidth();
	}
}
