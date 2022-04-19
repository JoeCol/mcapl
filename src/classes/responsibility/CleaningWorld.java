package responsibility;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
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
	Settings currentSettings;
	File settingsFile = new File("cleaning.settings");
	WorldCell[][] world;
	RoundRobinScheduler rrs = new RoundRobinScheduler();
	ArrayList<UpdateToWorld> listeners = new ArrayList<UpdateToWorld>();
	HashMap<String, Color> agentColours = new HashMap<String, Color>();
	
	Random r = new Random();
	
	public void addWorldListeners(UpdateToWorld u)
	{
		listeners.add(u);
	}
	
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
	public void init_after_adding_agents() 
	{
		//Randomly position agents
		int x = 1;
		for (AILAgent a : getAgents())
		{
			ArrayList<Term> terms = new ArrayList<Term>();
			terms.add(new NumberTermImpl(x++));
			terms.add(new NumberTermImpl(1));
			Predicate p = new Predicate("at");
			p.setTerms(terms);
			addPercept(a.getAgName(), p);
			
			ArrayList<Term> term = new ArrayList<Term>();
			term.add(new NumberTermImpl(1));
			Predicate p1 = new Predicate("zone");
			p1.setTerms(term);
			addPercept(a.getAgName(), p1);
			
			agentColours.put(a.getAgName(), new Color(r.nextInt(0xFFFFFF)));
		}
		
	}

	public CleaningWorld()
	{
		try
		{
			loadSettings();
			world = new WorldCell[currentSettings.getHeightOfMap()][currentSettings.getWidthOfMap()];
			BufferedReader br = new BufferedReader(new FileReader(currentSettings.getWorldFileLocation()));
			String line = br.readLine();
			for (int x = 0; x < world.length; x++)
			{
				for (int y = 0; y < world[x].length; y++)
				{
					String lineChar = line.substring(y,y+1);
					int zoneNum = Integer.parseInt(lineChar);
					world[x][y] = new WorldCell(zoneNum);
				}
				line = br.readLine();
			}
			setup_scheduler(this, rrs);
			rrs.addJobber(this);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public Unifier executeAction(String agName, Action act) throws AILexception {
	   	Unifier theta = new Unifier();
	   	System.out.println(agName + ": executes:" + act.fullstring());
	   	
	   	switch (act.getFunctor())
	   	{
	   		case "random_move":
	   			randomlyMoveAgent(agName, (int)((NumberTerm)act.getTerm(0)).solve(), (int)((NumberTerm)act.getTerm(1)).solve());
	   			break;
	   		case "clean":
	   			clean(agName);
	   			break;
	   		case "goToZone":
	   			goToZone(agName, (int)((NumberTerm)act.getTerm(0)).solve());
	   			break;
	   		case "checkForRequest":
	   			checkForRequest(agName);
	   			break;
	   		default:
	   			System.out.println(act.getFunctor() + " has not been implemented");
	   	}
	   	super.executeAction(agName, act);
    	return theta;
    }

	private void checkForRequest(String agName) 
	{
		Predicate p = new Predicate("cleanRequest");
		p.addTerm(new NumberTermImpl(r.nextInt(5) + 1));
		addPercept(agName, p);		
	}

	private void goToZone(String agName, int zone) 
	{
		// TODO Auto-generated method stub
		ArrayList<Term> terms = new ArrayList<Term>();
		terms.add(new NumberTermImpl(x));
		terms.add(new NumberTermImpl(y));
		Predicate p = new Predicate("at");
		p.setTerms(terms);
		int newx = x;
		int newy = y;
		
		int addToX = (r.nextInt(3)) - 1;
		int addToY = (r.nextInt(3)) - 1;
		newx = Math.floorMod(x + addToX, getWidth());
		newy = Math.floorMod(y + addToY, getHeight());
		boolean occupied = getCell(newx, newy).isOccupied();
		if (occupied)
		{
			newx = x;
			newy = y;
		}
		removePercept(agName, p);
		getCell(x, y).setOccupied(false);
		terms.clear();
		terms.add(new NumberTermImpl(newx));
		terms.add(new NumberTermImpl(newy));
		addPercept(agName, p);
		getCell(newx, newy).setOccupied(true);
		
		ArrayList<Term> zoneTerms = new ArrayList<Term>();
		zoneTerms.add(new NumberTermImpl(getCell(x,y).getZoneNumber()));
		Predicate p1 = new Predicate("zone");
		p1.setTerms(zoneTerms);
		removePercept(agName, p1);
		
		zoneTerms.clear();
		zoneTerms.add(new NumberTermImpl(getCell(newx, newy).getZoneNumber()));
		p1.setTerms(zoneTerms);
		addPercept(agName, p1);
	}

	private void clean(String agName) 
	{
		// TODO Auto-generated method stub
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
		
		int addToX = (r.nextInt(3)) - 1;
		int addToY = (r.nextInt(3)) - 1;
		newx = Math.floorMod(x + addToX, getWidth());
		newy = Math.floorMod(y + addToY, getHeight());
		boolean occupied = getCell(newx, newy).isOccupied();
		if (occupied)
		{
			newx = x;
			newy = y;
		}
		removePercept(agName, p);
		getCell(x, y).setOccupied(false);
		terms.clear();
		terms.add(new NumberTermImpl(newx));
		terms.add(new NumberTermImpl(newy));
		addPercept(agName, p);
		getCell(newx, newy).setOccupied(true);
		
		ArrayList<Term> zoneTerms = new ArrayList<Term>();
		zoneTerms.add(new NumberTermImpl(getCell(x,y).getZoneNumber()));
		Predicate p1 = new Predicate("zone");
		p1.setTerms(zoneTerms);
		removePercept(agName, p1);
		
		zoneTerms.clear();
		zoneTerms.add(new NumberTermImpl(getCell(newx, newy).getZoneNumber()));
		p1.setTerms(zoneTerms);
		addPercept(agName, p1);
		
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
				world[x][y].setChangeOfDirt(currentSettings.getDirtAppearanceChange());
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
		return currentSettings.getHeightOfMap();
	}

	public int getWidth() 
	{
		return currentSettings.getWidthOfMap();
	}

	public WorldCell getCell(int x, int y) 
	{
		return world[y][x];
	}

	public Color getAgentColor(AILAgent ag) 
	{
		return agentColours.get(ag.getAgName());
	}
}
