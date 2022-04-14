package responsibility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import ail.mas.DefaultEnvironment;
import ail.mas.scheduling.RoundRobinScheduler;
import ail.semantics.AILAgent;
import ail.syntax.Action;
import ail.syntax.Literal;
import ail.syntax.NumberTerm;
import ail.syntax.NumberTermImpl;
import ail.syntax.Predicate;
import ail.syntax.Term;
import ail.syntax.Unifier;
import ail.util.AILexception;
import ajpf.MCAPLJobber;
import ajpf.psl.MCAPLPredicate;
import gov.nasa.jpf.util.Pair;

interface UpdateToWorld{
	void worldUpdate();
}

public class CleaningWorld extends DefaultEnvironment implements MCAPLJobber
{
	Settings currentSettings = new Settings();
	WorldCell[][] world;
	RoundRobinScheduler rrs = new RoundRobinScheduler();
	ArrayList<UpdateToWorld> listeners = new ArrayList<UpdateToWorld>();
	HashMap<String, Pair<Integer, Integer>> agentLocations = new HashMap<String, Pair<Integer, Integer>>();
	
	Random r = new Random();
	
	public void addWorldListeners(UpdateToWorld u)
	{
		listeners.add(u);
	}
	
	//TODO implement settings 
	public void loadSettings()
	{
		
	}
	
	public Settings getSettings()
	{
		return currentSettings;
	}
	
	@Override
	public void init_after_adding_agents() 
	{
		//Randomly position agents
		int x = 0;
		for (AILAgent a : getAgents())
		{
			ArrayList<Term> terms = new ArrayList<Term>();
			terms.add(new NumberTermImpl(x++));
			terms.add(new NumberTermImpl(0));
			Predicate p = new Predicate("at");
			p.setTerms(terms);
			addPercept(a.getAgName(), p);
			agentLocations.put(a.getAgName(), new Pair<Integer,Integer>(x - 1, 0));
		}
		
	}

	public CleaningWorld()
	{
		world = new WorldCell[currentSettings.getWidth()][currentSettings.getHeight()];
		for (int x = 0; x < world.length; x++)
		{
			for (int y = 0; y < world[x].length; y++)
			{
				world[x][y] = new WorldCell(true);
			}
		}
		loadSettings();
		setup_scheduler(this, rrs);
		rrs.addJobber(this);
	}
	
	public Unifier executeAction(String agName, Action act) throws AILexception {
	   	Unifier theta = new Unifier();
	   	System.out.println(agName + ": executes:" + act.fullstring());
	   	
	   	if (act.getFunctor().equals("random_move"))
	   	{
	   		randomlyMoveAgent(agName, (int)((NumberTerm)act.getTerm(0)).solve(), (int)((NumberTerm)act.getTerm(1)).solve());
	   	}
	   	super.executeAction(agName, act);
    	return theta;
    }

	private void randomlyMoveAgent(String agName, int x, int y) 
	{
		ArrayList<Term> terms = new ArrayList<Term>();
		terms.add(new NumberTermImpl(x));
		terms.add(new NumberTermImpl(y));
		Predicate p = new Predicate("at");
		p.setTerms(terms);
		int newx = x;
		int newy = y;
		boolean occupied = true;
		int trys = 0;
		while (occupied && trys < 4)
		{
			newx = Math.floorMod(x + (r.nextInt(2) - 1),getWidth());
			newy = Math.floorMod(y + (r.nextInt(2) - 1),getHeight());
			for (AILAgent a : getAgents())
			{
				if ((a.getAgName() != agName) && !(agentLocations.get(a.getAgName())._1 == newx && agentLocations.get(a.getAgName())._2 == newy))
				{
					occupied = false;
				}
			}
			trys++;
			if (trys >= 4)
			{
				newx = x;
				newy = y;
			}
		}
		removePercept(agName, p);
		terms.clear();
		terms.add(new NumberTermImpl(newx));
		terms.add(new NumberTermImpl(newy));
		addPercept(agName, p);
		agentLocations.put(agName, new Pair<Integer,Integer>(x, y));
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
		//Allow for human eyes
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
