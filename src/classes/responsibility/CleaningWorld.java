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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import ail.mas.DefaultEnvironment;
import ail.mas.scheduling.RoundRobinScheduler;
import ail.semantics.AILAgent;
import ail.syntax.Action;
import ail.syntax.Literal;
import ail.syntax.NumberTerm;
import ail.syntax.NumberTermImpl;
import ail.syntax.Plan;
import ail.syntax.Predicate;
import ail.syntax.StringTerm;
import ail.syntax.Term;
import ail.syntax.Unifier;
import ail.util.AILexception;
import ajpf.MCAPLJobber;
import gov.nasa.jpf.util.Pair;

interface UpdateToWorld{
	void worldUpdate();
}

public class CleaningWorld extends DefaultEnvironment implements MCAPLJobber
{
	enum CleaningStatus {cs_notcleaning, cs_gotostart, cs_cleanright, cs_cleanleft, cs_moveright, cs_movedown, cs_moveleft};
	
	Settings currentSettings;
	File settingsFile = new File("cleaning.settings");
	Routes routeToZones;
	WorldCell[][] world;
	RoundRobinScheduler rrs = new RoundRobinScheduler();
	ArrayList<UpdateToWorld> listeners = new ArrayList<UpdateToWorld>();
	HashMap<String, Color> agentColours = new HashMap<String, Color>();
	HashMap<Integer, Pair<Integer, Integer>> zoneStart = new HashMap<Integer, Pair<Integer, Integer>>();
	HashMap<String, CleaningStatus> agentCleaningStatus = new HashMap<String, CleaningStatus>();
	
	Stack<Integer> zonesToClean = new Stack<Integer>();
	
	Random r = new Random();
	private int simulationDelay;
	
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
			agentCleaningStatus.put(a.getAgName(), CleaningStatus.cs_notcleaning);
		}
		
	}

	public CleaningWorld()
	{
		try
		{
			loadSettings();
			zoneStart.clear();
			world = new WorldCell[currentSettings.getHeightOfMap()][currentSettings.getWidthOfMap()];
			BufferedReader br = new BufferedReader(new FileReader(currentSettings.getWorldFileLocation()));
			String line = br.readLine();
			for (int y = 0; y < world.length; y++)
			{
				for (int x = 0; x < world[0].length; x++)
				{
					String lineChar = line.substring(x,x+1);
					int zoneNum = Integer.parseInt(lineChar);
					zoneStart.putIfAbsent(zoneNum, new Pair<Integer, Integer>(x,y));
					world[y][x] = new WorldCell(zoneNum);
				}
				line = br.readLine();
			}
			for (int zoneNum = 1; zoneNum < zoneStart.size(); zoneNum++)
			{
				zonesToClean.add(zoneNum);
			}
			Collections.shuffle(zonesToClean);
			zoneStart.remove(0);//Remove wall zone
			routeToZones = new Routes(world,zoneStart);
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
	   	//System.out.println(agName + ": executes:" + act.fullstring());
	   	switch (act.getFunctor())
	   	{
	   		case "random_move":
	   			randomlyMoveAgent(agName, (int)((NumberTerm)act.getTerm(0)).solve(), (int)((NumberTerm)act.getTerm(1)).solve());
	   			break;
	   		case "do_clean":
	   			clean(agName, (int)((NumberTerm)act.getTerm(0)).solve(), (int)((NumberTerm)act.getTerm(1)).solve(), (int)((NumberTerm)act.getTerm(2)).solve());
	   			break;
	   		case "go_to_zone":
	   			goToZone(agName, (int)((NumberTerm)act.getTerm(0)).solve(), (int)((NumberTerm)act.getTerm(1)).solve(), (int)((NumberTerm)act.getTerm(2)).solve());
	   			break;
	   		case "checkForRequest":
	   			checkForRequest(agName);
	   			break;
	   		case "finishCleaning":
	   			finishCleaning(agName, (int)((NumberTerm)act.getTerm(0)).solve());
	   			break;
	   		case "print":
	   			System.out.println();
	   			break;
	   		default:
	   			System.out.println(act.getFunctor() + " has not been implemented");
	   	}
	   	super.executeAction(agName, act);
    	return theta;
    }

	private void finishCleaning(String agName, int zone) 
	{
		Predicate p = new Predicate("cleanRequest");
		p.addTerm(new NumberTermImpl(zone));
		removePercept(agName, p);	
		
		Predicate p1 = new Predicate("cleaned");
		p1.addTerm(new NumberTermImpl(zone));
		removePercept(agName, p1);	

		zonesToClean.add(zone);
		Collections.shuffle(zonesToClean);
	}

	private void checkForRequest(String agName) 
	{
		Predicate p = new Predicate("cleanRequest");
		int zoneToClean = zonesToClean.pop();
		p.addTerm(new NumberTermImpl(zoneToClean));
		addPercept(agName, p);		
	}

	private void goToZone(String agName, int zone, int x, int y) 
	{
		int newx = routeToZones.toZone(zone, x, y)._1;
		int newy = routeToZones.toZone(zone, x, y)._2;
		
		moveAgent(agName, x, y, newx, newy);
	}

	private void clean(String agName, int zone, int x, int y) 
	{
		switch (agentCleaningStatus.get(agName))
		{
		case cs_cleanright:
			getCell(x,y).clean();
			if (getCell(x+1,y).getZoneNumber() != zone)
			{
				if (getCell(x,y+1).getZoneNumber() != zone)
				{
					//Finished cleaning
					agentCleaningStatus.put(agName, CleaningStatus.cs_notcleaning);
					Predicate p = new Predicate("cleaned");
					p.addTerm(new NumberTermImpl(zone));
					addPercept(agName, p);	
				}
				else
				{
					agentCleaningStatus.put(agName, CleaningStatus.cs_movedown);
				}
			}
			else
			{
				agentCleaningStatus.put(agName, CleaningStatus.cs_moveright);
			}
			break;
		case cs_gotostart:
			Pair<Integer, Integer> zoneLocation = zoneStart.get(zone);
			if (zoneLocation._1 == x && zoneLocation._2 == y)
			{
				agentCleaningStatus.put(agName, CleaningStatus.cs_cleanright);
			}
			else
			{
				zoneLocation = routeToZones.toZone(zone, x, y);
				moveAgent(agName, x, y, zoneLocation._1, zoneLocation._2);
			}
			break;
		case cs_notcleaning:
			agentCleaningStatus.put(agName, CleaningStatus.cs_gotostart);
			break;
		case cs_cleanleft:
			getCell(x,y).clean();
			if (getCell(x-1,y).getZoneNumber() != zone)
			{
				if (getCell(x,y+1).getZoneNumber() != zone)
				{
					agentCleaningStatus.put(agName, CleaningStatus.cs_notcleaning);
					//Finished cleaning
					Predicate p = new Predicate("cleaned");
					p.addTerm(new NumberTermImpl(zone));
					addPercept(agName, p);	
				}
				else
				{
					agentCleaningStatus.put(agName, CleaningStatus.cs_movedown);
				}
			}
			else
			{
				agentCleaningStatus.put(agName, CleaningStatus.cs_moveleft);
			}
			break;
		case cs_moveleft:
			moveAgent(agName, x, y, x-1, y);
			agentCleaningStatus.put(agName, CleaningStatus.cs_cleanleft);
			break;
		case cs_movedown:
			moveAgent(agName, x, y, x, y+1);
			if (getCell(x-1,y+1).getZoneNumber() != zone)
			{
				agentCleaningStatus.put(agName, CleaningStatus.cs_cleanright);
			}
			else
			{
				agentCleaningStatus.put(agName, CleaningStatus.cs_cleanleft);
			}
			break;
		case cs_moveright:
			moveAgent(agName, x, y, x+1, y);
			agentCleaningStatus.put(agName, CleaningStatus.cs_cleanright);
			break;
		default:
			System.out.println("Issue with cleaning status");
			break;
		}
	}

	private void randomlyMoveAgent(String agName, int x, int y) 
	{
		int newx = x;
		int newy = y;
		
		int addToX = (r.nextInt(3)) - 1;
		int addToY = (r.nextInt(3)) - 1;
		newx = Math.floorMod(x + addToX, getWidth());
		newy = Math.floorMod(y + addToY, getHeight());
		moveAgent(agName, x, y, newx, newy);
		
	}
	
	private void moveAgent(String agName, int x, int y, int newX, int newY)
	{
		//boolean occupied = getCell(newX, newY).isOccupied();
		boolean occupied = false;
		if (!occupied)
		{
			getCell(x, y).setOccupied(false);
			//Remove old at belief
			ArrayList<Term> terms = new ArrayList<Term>();
			terms.add(new NumberTermImpl(x));
			terms.add(new NumberTermImpl(y));
			Predicate p = new Predicate("at");
			p.setTerms(terms);
			removePercept(agName, p);
			
			//Set New at belief
			terms.clear();
			terms.add(new NumberTermImpl(newX));
			terms.add(new NumberTermImpl(newY));
			addPercept(agName, p);
			
			//Update zone belief
			ArrayList<Term> zoneTerms = new ArrayList<Term>();
			zoneTerms.add(new NumberTermImpl(getCell(x,y).getZoneNumber()));
			Predicate p1 = new Predicate("zone");
			p1.setTerms(zoneTerms);
			removePercept(agName, p1);
			
			zoneTerms.clear();
			zoneTerms.add(new NumberTermImpl(getCell(newX, newY).getZoneNumber()));
			p1.setTerms(zoneTerms);
			addPercept(agName, p1);
			getCell(newX, newY).setOccupied(true);
		}
		
		
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
		if (simulationDelay != 0)
		{
			try {
				Thread.sleep(simulationDelay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public String getName() {
		return "Cleaning Environment Jobber";
	}
	
	@Override
	public boolean done()
	{
		//Always something to clean
		return false;
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

	public void setSimulationDelay(int value) 
	{
		simulationDelay = value;
	}
}
