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
import ail.syntax.ListTerm;
import ail.syntax.ListTermImpl;
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
import ajpf.psl.MCAPLNumberTermImpl;
import ajpf.psl.MCAPLTerm;
import gov.nasa.jpf.util.Pair;

interface UpdateToWorld{
	void worldUpdate();
}

public class CleaningWorld extends DefaultEnvironment implements MCAPLJobber
{
	enum CleaningStatus {cs_notcleaning, cs_move, cs_clean, cs_done};
	
	Routes routeToZones;
	WorldCell[][] world;
	RoundRobinScheduler rrs = new RoundRobinScheduler();
	ArrayList<UpdateToWorld> listeners = new ArrayList<UpdateToWorld>();
	HashMap<String, Color> agentColours = new HashMap<String, Color>();
	HashMap<Integer, Pair<Integer, Integer>> zoneStart = new HashMap<Integer, Pair<Integer, Integer>>();
	HashMap<String, CleaningStatus> agentCleaningStatus = new HashMap<String, CleaningStatus>();
	
	HashMap<String, Stack<Pair<Integer, Integer>>> agentSquToClean = new HashMap<String, Stack<Pair<Integer, Integer>>>();
	
	Random r = new Random();
	
	public void addWorldListeners(UpdateToWorld u)
	{
		listeners.add(u);
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

	public CleaningWorld(Settings currentSettings)
	{
		try
		{
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
	   	Unifier theta = super.executeAction(agName, act);
	   	//System.out.println(agName + ": executes:" + act.fullstring());
  		
	   	switch (act.getFunctor())
	   	{
	   		case "getItem":
	   			ListTerm list = (ListTerm)act.getTerm(0);
	   			NumberTerm itemNo = (NumberTerm)act.getTerm(1);
	   			Term item = list.get((int)itemNo.solve());
	   			item.unifies(act.getTerm(2), theta);
	   			break;
	   		case "appendList":
	   			ListTerm firstList = (ListTerm)act.getTerm(0);
	   			ListTerm secondList = (ListTerm)act.getTerm(1);
	   			ListTerm appendedList = new ListTermImpl();
	   			for (int i = 0; i < firstList.getAsList().size(); i++)
	   			{
	   				if (!appendedList.getAsList().contains(firstList.get(i)))
	   				{
	   					appendedList.add(appendedList.size(), firstList.get(i));
	   				}
	   			}
	   			for (int i = 0; i < secondList.getAsList().size(); i++)
	   			{
	   				if (!appendedList.getAsList().contains(secondList.get(i)))
	   				{
	   					appendedList.add(appendedList.size(), secondList.get(i));
	   				}
	   			}
	   			appendedList.unifies(act.getTerm(2), theta);
	   			break;
	   		case "iterateRes":
	   			ListTerm irList = (ListTerm)act.getTerm(0);
	   			Term t = irList.get(0);
	   			irList.remove(0);
	   			irList.add(irList.size(), t);
	   			irList.unifies(act.getTerm(1), theta);
	   			break;
	   		case "removeList":
	   			ListTerm currList = (ListTerm)act.getTerm(0);
	   			ListTerm listToRem = (ListTerm)act.getTerm(1);
	   			currList.removeAll(listToRem);
	   			currList.unifies(act.getTerm(2), theta);
	   			break;
	   		case "deleteItem":
	   			Predicate diItem = (Predicate)act.getTerm(0);
	   			ListTerm diList = (ListTerm)act.getTerm(1);
	   			diList.remove(diItem);
	   			diList.unifies(act.getTerm(2), theta);
	   			break;
	   		case "delete":
	   			ListTerm firstList1 = (ListTerm)act.getTerm(0);
	   			ListTerm secondList1 = (ListTerm)act.getTerm(1);
	   			if (firstList1.size() > 1)
	   			{
	   				secondList1.removeAll(firstList1);
	   			}
	   			else
	   			{
	   				secondList1.remove(firstList1);
	   			}
	   			secondList1.unifies(act.getTerm(2), theta);
	   			break;
	   		case "observeDirt":
	   			observeDirt(agName);
	   			break;
	   		case "random_move":
	   			randomlyMoveAgent(agName, (int)((NumberTerm)act.getTerm(0)).solve(), (int)((NumberTerm)act.getTerm(1)).solve());
	   			break;
	   		case "do_clean":
	   			CleaningStatus cs = clean(agName, (int)((NumberTerm)act.getTerm(0)).solve(), (int)((NumberTerm)act.getTerm(1)).solve(), (int)((NumberTerm)act.getTerm(2)).solve());
	   			NumberTerm cleaningNT;
	   			if (cs == CleaningStatus.cs_done)
	   			{
	   				cleaningNT = new NumberTermImpl(1);
	   			}
	   			else
	   			{
	   				cleaningNT = new NumberTermImpl(0);
	   			}
	   			cleaningNT.unifies(act.getTerm(3), theta);
	   			break;
	   		case "go_to_zone":
	   			goToZone(agName, (int)((NumberTerm)act.getTerm(0)).solve(), (int)((NumberTerm)act.getTerm(1)).solve(), (int)((NumberTerm)act.getTerm(2)).solve());
	   			break;
	   		case "getRandomZone":
	   			NumberTerm n = new NumberTermImpl(1 + r.nextInt(zoneStart.keySet().size()));
	   			n.unifies(act.getTerm(0), theta);
	   			break;
	   		case "print":
	   		case "send":
	   		case "sum":
	   			//System.out.println();
	   			break;
	   		default:
	   			System.out.println(act.getFunctor() + " has not been implemented");
	   	}
	   	
    	return theta;
    }
	
	private int observeDirt(String agName) 
	{
		//Get zone for agent
		int zone = 0;
		for (AILAgent a : getAgents())
		{
			if (agName.equals(a.getAgName()))
			{
				Iterator<Literal> it = a.getBB().getPercepts();
				while (it.hasNext())
				{
					Literal l = it.next();
					if (l.getFunctor().equals("zone"))
					{
						zone = (int)((NumberTerm)l.getTerm(0)).solve();
						break;
					}
				}
			}
		}
		String dirtBelief = "dirt_" + zone;
		Predicate p = new Predicate(dirtBelief);
		for (WorldCell[] row : world)
		{
			for (WorldCell cell : row)
			{
				if (cell.getZoneNumber() == zone && cell.hasDirt())
				{
					addPercept(agName, p);
					return 1;
				}
			}
		}
		removePercept(agName, p);
		return 0;//No dirty, 0 is false
	}


	private void goToZone(String agName, int zone, int x, int y) 
	{
		int newx = routeToZones.toZone(zone, x, y)._1;
		int newy = routeToZones.toZone(zone, x, y)._2;
		
		moveAgent(agName, x, y, newx, newy);
	}

	private CleaningStatus clean(String agName, int zone, int x, int y) 
	{
		switch (agentCleaningStatus.get(agName))
		{
		//Either not cleaned or finished a previous cleaning
		case cs_notcleaning:
		case cs_done:
			//Setup list of squares to clean, inefficient should precalculate
			agentSquToClean.put(agName, new Stack<Pair<Integer, Integer>>());
			for (int wx = 0; wx < getWidth(); wx++)
			{
				for (int wy = 0; wy < getHeight(); wy++)
				{
					if (getCell(wx,wy).getZoneNumber() == zone)
					{
						agentSquToClean.get(agName).push(new Pair<Integer, Integer>(wx,wy));
					}
				}
			}
			agentCleaningStatus.put(agName, CleaningStatus.cs_move);
		//Move to dirty square
		case cs_move:
			//If empty, done cleaning
			if (agentSquToClean.get(agName).isEmpty())
			{
				agentCleaningStatus.put(agName, CleaningStatus.cs_done);
			}
			else
			{
				Pair<Integer,Integer> p = agentSquToClean.get(agName).peek();//Destination
				if (p._1 == x && p._2 == y) //At square to clean
				{
					agentSquToClean.get(agName).pop();
					agentCleaningStatus.put(agName, CleaningStatus.cs_clean);
				}
				else
				{
					//Find route to p
					Pair<Integer, Integer> nextSquare = routeToZones.nextSquareToZone(world, x, y, p._1, p._2);
					moveAgent(agName, x, y, nextSquare._1, nextSquare._2);
				}
			}
			break;
		//Remove dirt
		case cs_clean:
			getCell(x, y).clean();
			agentCleaningStatus.put(agName, CleaningStatus.cs_move);
			break;
		default:
			System.out.println("Issue with cleaning status");
			break;
		}
		return agentCleaningStatus.get(agName);
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
		return world.length;
	}

	public int getWidth() 
	{
		return world[0].length;
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
