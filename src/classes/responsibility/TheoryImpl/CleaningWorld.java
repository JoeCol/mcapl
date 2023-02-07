package responsibility.TheoryImpl;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayDeque;
import java.util.Random;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import ail.mas.DefaultEnvironment;
import ail.mas.scheduling.RoundRobinScheduler;
import ail.semantics.AILAgent;
import ail.syntax.Action;
import ail.syntax.ListTerm;
import ail.syntax.ListTermImpl;
import ail.syntax.Literal;
import ail.syntax.Message;
import ail.syntax.NumberTerm;
import ail.syntax.NumberTermImpl;
import ail.syntax.Predicate;
import ail.syntax.StringTerm;
import ail.syntax.StringTermImpl;
import ail.syntax.Term;
import ail.syntax.Unifier;
import ail.util.AILexception;
import ajpf.MCAPLJobber;
import gov.nasa.jpf.util.Pair;

interface UpdateToWorld{
	void worldUpdate();
}

interface UpdateToDirtLevels{
	void dirtLevelUpdate(int dirt, int badDirt);
}

interface UpdateToSimulationTime{
	void simulationTimeUpdate(int time);
}

public class CleaningWorld extends DefaultEnvironment implements MCAPLJobber
{
	public enum AgentAction {aa_moveup, aa_movedown, aa_moveright, aa_moveleft, aa_clean, aa_observedirt, aa_moveupleft, aa_moveupright, aa_movedownleft, aa_movedownright, aa_finish}
	public enum Process {p_nochanges, p_updatedPercept, p_updatePercept};
	private volatile Process currentState = Process.p_nochanges;
	Routes routeToZones = new Routes();
	volatile WorldCell[][] world;
	int simulationDelay = 100;
	int remainingSteps = 1;
	Timer environmentTimer = new Timer();
	Settings currentSettings;
	RoundRobinScheduler rrs = new RoundRobinScheduler();
	ArrayList<UpdateToWorld> worldListeners = new ArrayList<UpdateToWorld>();
	ArrayList<UpdateToDirtLevels> dirtListeners = new ArrayList<UpdateToDirtLevels>();
	ArrayList<UpdateToSimulationTime> simListeners = new ArrayList<UpdateToSimulationTime>();
	HashMap<String, Color> agentColours = new HashMap<String, Color>();
	HashMap<Character, ArrayList<Pair<Integer, Integer>>> zoneSquares = new HashMap<Character, ArrayList<Pair<Integer, Integer>>>();
	
	HashMap<String, ArrayDeque<AgentAction>> agentActions = new HashMap<String, ArrayDeque<AgentAction>>();
	HashMap<String, String> workingOn = new HashMap<String, String>();
	
	ConcurrentLinkedQueue<Pair<String, Predicate>> perceptAdds = new ConcurrentLinkedQueue<Pair<String, Predicate>>();
	ConcurrentLinkedQueue<Pair<String, Predicate>> perceptRems = new ConcurrentLinkedQueue<Pair<String, Predicate>>();
	
	Random r = new Random();
	
	//Variables for naive cleaner
	ArrayDeque<Character> naiveQueue = new ArrayDeque<Character>();
	
	//variables for dirt management
	int dirtNum = 0;
	int badDirtNum = 0;
	int totalDirt = 0;
	int totalBadDirt = 0;
	ArrayList<Pair<Integer,Integer>> possibleDirtLocations = new ArrayList<Pair<Integer,Integer>>();
	DirtRecord dirtRecord = new DirtRecord();

	public Settings getSettings()
	{
		return currentSettings;
	}
	
	public void addWorldListeners(UpdateToWorld u)
	{
		worldListeners.add(u);
	}
	
	public void addDirtListeners(UpdateToDirtLevels u)
	{
		dirtListeners.add(u);
	}
	
	public void addSimListeners(UpdateToSimulationTime u)
	{
		simListeners.add(u);
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
			term.add(new StringTermImpl("A"));
			Predicate p1 = new Predicate("zone");
			p1.setTerms(term);
			addPercept(a.getAgName(), p1);
			
			agentColours.put(a.getAgName(), new Color(r.nextInt(0xFFFFFF)));
			agentActions.put(a.getAgName(), new ArrayDeque<AgentAction>());
		}
		
		//For Testing
		Predicate msgPred = new Predicate("assignment");
		ListTermImpl lt = new ListTermImpl();
		lt.add(new Predicate("initial"));
		msgPred.addTerm(lt);
		msgPred.addTerm(new Predicate("report"));
		addMessage("cleaner1", new Message(1, "initial", "cleaner1", msgPred));
		
		Predicate msgClean = new Predicate("assignment");
		msgClean.addTerm(lt);
		msgClean.addTerm(new Predicate("cleanF"));
		addMessage("cleaner1", new Message(1, "initial", "cleaner1", msgClean));
		
		environmentTimer.scheduleAtFixedRate(new TimerTask()
		{
			//method to run through thread, delay is caused by timer, works as a simulation step
			public void run()
			{	
				//Get ONE action from each agent
				for (AILAgent a : getAgents())
				{
					ArrayDeque<AgentAction> actionStack = agentActions.get(a.getAgName());
					if (!actionStack.isEmpty())
					{
						AgentAction action = actionStack.pop();
						Pair<Integer, Integer> agentLocation = getAgentLocation(a.getAgName());
						if (agentLocation._1 != -1)
						{
							switch (action)
							{
							case aa_clean:
								clean(agentLocation._1, agentLocation._2); 
								break;
							case aa_movedown:
								moveAgent(a.getAgName(), agentLocation._1, agentLocation._2, agentLocation._1, agentLocation._2 + 1);
								break;
							case aa_moveleft:
								moveAgent(a.getAgName(), agentLocation._1, agentLocation._2, agentLocation._1 - 1, agentLocation._2);
								break;
							case aa_moveright:
								moveAgent(a.getAgName(), agentLocation._1, agentLocation._2, agentLocation._1 + 1, agentLocation._2);
								break;
							case aa_moveup:
								moveAgent(a.getAgName(), agentLocation._1, agentLocation._2, agentLocation._1, agentLocation._2 - 1);
								break;
							case aa_movedownleft:
								moveAgent(a.getAgName(), agentLocation._1, agentLocation._2, agentLocation._1 - 1, agentLocation._2 + 1);
								break;
							case aa_movedownright:
								moveAgent(a.getAgName(), agentLocation._1, agentLocation._2, agentLocation._1 + 1, agentLocation._2 + 1);
								break;
							case aa_moveupleft:
								moveAgent(a.getAgName(), agentLocation._1, agentLocation._2, agentLocation._1 - 1, agentLocation._2 - 1);
								break;
							case aa_moveupright:
								moveAgent(a.getAgName(), agentLocation._1, agentLocation._2, agentLocation._1 + 1, agentLocation._2 - 1);
								break;
							case aa_observedirt:
								observeDirt(a.getAgName());
								break;
							case aa_finish:
								perceptAdds.add(new Pair<String, Predicate>(a.getAgName(), new Predicate("finished")));
								break;
							}
						}
						//Update agent beliefs
						//Get actions from environment thread
						currentState = Process.p_updatePercept;
						
						while (!(currentState == Process.p_updatedPercept))
						{
							
						}
						currentState = Process.p_nochanges;
					}
				}
				//Do dirt step
				dirtNum = (++dirtNum) % currentSettings.getDirtInterval();
				if (dirtNum == 0)
				{
					badDirtNum = (++badDirtNum) % currentSettings.getBadDirtInterval();
					addDirt(badDirtNum == 0);
				}
				remainingSteps--;
				for (UpdateToSimulationTime u : simListeners)
				{
					u.simulationTimeUpdate(remainingSteps);
				}
			}

			
		}, 0, simulationDelay);
		
	}
	
	public void addDirt(boolean bad) 
	{
		Collections.shuffle(possibleDirtLocations);//to ensure that dirt is not evenly distributed as it is cleaned.
		Pair<Integer,Integer> newDirt = possibleDirtLocations.remove(0);
		getCell(newDirt._1,newDirt._2).setDirty(bad);
		totalDirt++;
		if (bad) {totalBadDirt++;}
		dirtRecord.addRecord(remainingSteps, totalDirt, totalBadDirt);
		for (UpdateToDirtLevels u : dirtListeners)
		{
			u.dirtLevelUpdate(totalDirt, totalBadDirt);
		}
	}

	public Pair<Integer, Integer> getAgentLocation(String agName) 
	{
		
		for (AILAgent a : getAgents())
		{
			if (agName.equals(a.getAgName()))
			{
				Iterator<Literal> it = a.getBB().getPercepts();
				while (it.hasNext())
				{
					Literal l = it.next();
					if (l.getFunctor().equals("at"))
					{
						return new Pair<Integer, Integer>((int)((NumberTerm)l.getTerm(0)).solve(),(int)((NumberTerm)l.getTerm(1)).solve());
					}
				}
			}
		}
		System.out.println("Could not find agent location:" + agName);
		return new Pair<Integer, Integer>(-1,-1);
	}

	public CleaningWorld(int simSteps, int dirtInt, int badDirtInt, String worldLoc)
	{
		currentSettings = new Settings(0, 0, simSteps, dirtInt, badDirtInt, worldLoc);
		try
		{
			zoneSquares.clear();
			remainingSteps = currentSettings.getSimulationSteps();
			RandomAccessFile fr = new RandomAccessFile(currentSettings.getWorldFileLocation(), "r");
			String line = fr.readLine();
			currentSettings.setWidthOfMap(line.length());
			int height = 1;
			while (fr.readLine() != null) {height++;}
			currentSettings.setHeightOfMap(height);
			fr.seek(0);
			
			world = new WorldCell[currentSettings.getHeightOfMap()][currentSettings.getWidthOfMap()];
			
			for (int y = 0; y < world.length; y++)
			{
				line = fr.readLine();
				for (int x = 0; x < world[0].length; x++)
				{
					char zoneID = line.charAt(x);
					zoneSquares.putIfAbsent(zoneID, new ArrayList<Pair<Integer, Integer>>());
					zoneSquares.get(zoneID).add(new Pair<Integer, Integer>(x,y));
					if (zoneID != '0')//0 is reserved for walls
					{
						possibleDirtLocations.add(new Pair<Integer, Integer>(x,y));
					}
					world[y][x] = new WorldCell(zoneID);
				}
			}
			/*for (int y = 0; y < getHeight(); y++)
			{
				for (int x = 0; x < getWidth(); x++)
				{
					System.out.print(getCell(x,y).getZoneID());
				}
				System.out.println();
			}*/
			//Add zones for naive cleaner
			for (char zoneID : zoneSquares.keySet())
			{
				naiveQueue.add(zoneID);
			}
			naiveQueue.remove('0');//Remove wall room
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
  		//Should only place actions onto agentaction stack
	   	switch (act.getFunctor())
	   	{
	   		case "remfin":
	   			removePercept(agName, new Predicate("finished"));
	   			this.notifyListeners();
	   			break;
	   		case "addBack":
	   			StringTerm finRoomID = (StringTerm)act.getTerm(0);
	   			naiveQueue.add(finRoomID.getString().charAt(0));
	   			break;
	   		case "getRoomToClean":
	   			String roomID = String.valueOf(naiveQueue.removeFirst());
	   			StringTerm roomIDTerm = new StringTermImpl(roomID);
	   			roomIDTerm.unifies(act.getTerm(0), theta);
	   			break;
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
	   			ListTerm listToRem = (ListTerm)act.getTerm(0);
	   			ListTerm currList = (ListTerm)act.getTerm(1);
	   			currList.removeAll(listToRem);
	   			currList.unifies(act.getTerm(2), theta);
	   			break;
	   		case "deleteItem":
	   			Predicate diItem = (Predicate)act.getTerm(0);
	   			ListTerm diList = (ListTerm)act.getTerm(1);
	   			diList.remove(diItem);
	   			diList.unifies(act.getTerm(2), theta);
	   			break;
	   		case "removeObserved":
	   			removePercept(agName, new Predicate("observed"));
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
	   			agentActions.get(agName).add(AgentAction.aa_observedirt);
	   			break;
	   		case "clean":
	   			addCleaningActions(agName, ((StringTerm)act.getTerm(0)).getString().charAt(0));
	   			break;
	   		case "goToZone":
	   			goToZone(agName, ((StringTerm)act.getTerm(0)).getString().charAt(0));
	   			break;
	   		case "getCared":
	   			ListTerm res = (ListTerm)act.getTerm(0);
	   			ListTerm completedRes = (ListTerm)act.getTerm(2);
	   			Predicate caredMostRes = new Predicate();
	   			int caredMostResV = 0;
	   			AILAgent agent = getAgents().get(0);
	   			for (int i = 0; i < getAgents().size(); i++)
	   			{
	   				agent = getAgents().get(i);
	   				if (agent.getAgName().equals(agName))
	   				{
	   					break;
	   				}
	   			}
	   			ArrayList<Literal> beliefs = agent.getBB().getAll();
	   			int resValue = 0;
	   			for (int i = 0; i < res.size(); i++)
	   			{
	   				Term cRes = res.get(i);
	   				for (int j = 0; j < beliefs.size(); j++)
	   				{
	   					if (beliefs.get(j).fullstring().contains("care(" + cRes.toString()))
	   					{
	   						resValue = Integer.parseInt(beliefs.get(j).getTerm(1).toString());
	   						break;
	   					}
	   				}
	   				if (resValue > caredMostResV && !completedRes.contains(cRes))
	   				{
	   					caredMostRes.setFunctor(cRes.toString());
	   					caredMostResV = resValue;
	   				}
	   			}
	   			caredMostRes.unifies(act.getTerm(1), theta);
	   			break;
	   		case "getNextInList":
	   			ListTerm todo = (ListTerm)act.getTerm(0);
	   			ListTerm done = (ListTerm)act.getTerm(1);
	   			Predicate nextItem = new Predicate("none");
	   			for (int i = 0; i < todo.size(); i++)
	   			{
	   				Term termToFind = todo.get(i);
	   				boolean found = false;
	   				for (int j = 0; j < done.size(); j++)
	   				{
	   					if (done.get(j).toString().equals(termToFind.toString()))
	   					{
	   						found = true;
	   						break;
	   					}
	   				}
	   				if (!found)
	   				{
	   					nextItem.setFunctor(termToFind.toString());
	   					break;
	   				}
	   			}
	   			nextItem.unifies(act.getTerm(2), theta);
	   			break;
	   		case "doneAll":
	   			ListTerm todoList = (ListTerm)act.getTerm(0);
	   			ListTerm doneList = (ListTerm)act.getTerm(1);
	   			Predicate doneAll = new Predicate("false");
	   			if (doneList.containsAll(todoList))
	   			{
	   				doneAll.setFunctor("true");
	   			}
	   			doneAll.unifies(act.getTerm(2), theta);
	   			break;
	   		case "prt":
	   			System.out.print(agName + ": ");
	   			for (int i = 0; i < act.getTermsSize(); i++)
	   			{
	   				if (!act.getTerm(i).isString())
	   				{
	   					System.out.print(act.getTerm(i).toString());
	   				}
	   				else
	   				{
	   					System.out.print(act.getTerm(i).toString().substring(1, act.getTerm(i).toString().length() - 1));
	   				}
	   			}
	   			System.out.println();
	   			break;
	   		case "getDone":
	   			Boolean actionFinished = checkDone(agName);
	   			Predicate finished = new Predicate(actionFinished.toString());
	   			finished.unifies(act.getTerm(0), theta);
	   			break;
	   		case "break":
	   			System.out.print(agName + ": ");
	   			System.out.println("Breakpoint");
	   			System.out.println(agentActions.toString());
	   			break;
	   		case "randomMove":
	   		case "printstate":
	   		case "print":
	   		case "send":
	   		case "sum":
	   			//System.out.println();
	   			break;
	   		default:
	   			System.out.println(act.getFunctor() + " has not been implemented");
	   	}
	   	//System.out.println(agName + ":" + agentActions.get(agName).toString());
    	return theta;
    }
	
	private Boolean checkDone(String agName) 
	{
		//Check if agent has any actions on the stack
		if (agentActions.get(agName).size() == 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private void addCleaningActions(String agName, char zone) 
	{
		//Assumes correct starting place (i.e. go to zone is called first)
		ArrayList<Pair<Integer, Integer>> allSquares = zoneSquares.get(zone);
		Pair<Integer, Integer> prevSquare = allSquares.get(0);
		Pair<Integer, Integer> nextSquare;
		ArrayDeque<AgentAction> actions = new ArrayDeque<AgentAction>();
		actions.add(AgentAction.aa_clean);
		for (int i = 1; i < allSquares.size(); i++)
		{
			nextSquare = allSquares.get(i);
			actions.addAll(routeToZones.actionsToZone(world, prevSquare, nextSquare));
			actions.add(AgentAction.aa_clean);
			prevSquare = nextSquare;
			
		}
		actions.add(AgentAction.aa_finish);
		agentActions.get(agName).addAll(actions);
	}

	private void observeDirt(String agName) 
	{
		//Get zone for agent
		char zone = 0;
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
						zone = ((StringTerm)l.getTerm(0)).getString().charAt(0);
						break;
					}
				}
			}
		}
		String dirtBelief = "dirt";
		String badDirtBelief = "badDirt";
		Predicate badDirt = new Predicate(badDirtBelief);
		badDirt.addTerm(new StringTermImpl(String.valueOf(zone)));
		Predicate dirt = new Predicate(dirtBelief);
		dirt.addTerm(new StringTermImpl(String.valueOf(zone)));
		boolean hasDirt = false;
		boolean hasBadDirt = false;
		for (WorldCell[] row : world)
		{
			for (WorldCell cell : row)
			{
				if (cell.getZoneID() == zone)
				{
					if (cell.hasBadDirt())
					{
						hasDirt = true;
						hasBadDirt = true;
						break;
					}
					else if (cell.hasDirt())
					{
						hasDirt = true;
					}
				}
			}
		}
		if (hasDirt)
		{
			perceptAdds.add(new Pair<String, Predicate>(agName, dirt));
		}
		else
		{
			perceptRems.add(new Pair<String, Predicate>(agName, dirt));
		}
		if (hasBadDirt)
		{
			perceptAdds.add(new Pair<String, Predicate>(agName, badDirt));
		}
		else
		{
			perceptRems.add(new Pair<String, Predicate>(agName, badDirt));
		}
	}


	//Add move actions to agentactions stack
	private void goToZone(String agName, char zone) 
	{
		//get route
		ArrayDeque<AgentAction> moveActions = routeToZones.actionsToZone(world, getAgentLocation(agName), zoneSquares.get(zone).get(0));
		moveActions.add(AgentAction.aa_finish);
		agentActions.get(agName).addAll(moveActions);
	}

	private void clean(int x, int y) 
	{
		if (getCell(x,y).hasDirt())
		{
			if (getCell(x,y).hasBadDirt())
			{
				totalBadDirt--;
			}
			totalDirt--;
			possibleDirtLocations.add(new Pair<Integer,Integer>(x,y));
			dirtRecord.addRecord(remainingSteps, totalDirt, totalBadDirt);
			for (UpdateToDirtLevels u : dirtListeners)
			{
				u.dirtLevelUpdate(totalDirt, totalBadDirt);
			}
		}
		getCell(x, y).clean();
		
	}
	
	//Change environment percepts
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
			perceptRems.add(new Pair<String, Predicate>(agName, p));
			
			//Set New at belief
			Predicate pAt = new Predicate("at");
			ArrayList<Term> termsAt = new ArrayList<Term>();
			termsAt.clear();
			termsAt.add(new NumberTermImpl(newX));
			termsAt.add(new NumberTermImpl(newY));
			pAt.setTerms(termsAt);
			perceptAdds.add(new Pair<String, Predicate>(agName, pAt));
			
			//Update zone belief
			ArrayList<Term> zoneTerms = new ArrayList<Term>();
			zoneTerms.add(new StringTermImpl(String.valueOf(getCell(x,y).getZoneID())));
			Predicate p1 = new Predicate("zone");
			p1.setTerms(zoneTerms);
			perceptRems.add(new Pair<String, Predicate>(agName, p1));
			
			ArrayList<Term> zoneUpdateTerms = new ArrayList<Term>();
			zoneUpdateTerms.add(new StringTermImpl(String.valueOf(getCell(newX,newY).getZoneID())));
			Predicate up1 = new Predicate("zone");
			up1.setTerms(zoneUpdateTerms);
			up1.setTerms(zoneUpdateTerms);
			perceptAdds.add(new Pair<String, Predicate>(agName, up1));
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
		if (currentState == Process.p_updatePercept)
		{
			while (!perceptRems.isEmpty())
			{
				Pair<String, Predicate> p = perceptRems.poll();
				//System.out.println("Removing percept " + p._2.toString() + " from agent " + p._1);
				removePercept(p._1, p._2);
			}
			while (!perceptAdds.isEmpty())
			{
				Pair<String, Predicate> p = perceptAdds.poll();
				//System.out.println("Adding percept " + p._2.toString() + " to agent " + p._1);
				addPercept(p._1, p._2);
			}
			this.notifyListeners();
			currentState = Process.p_updatedPercept;
		}
		//Update GUI
		for (UpdateToWorld u : worldListeners)
		{
			u.worldUpdate();
		}
	}

	@Override
	public String getName() {
		return "Cleaning Environment Jobber";
	}
	
	@Override
	public boolean done()
	{
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
		if (y == -1 || x == -1 || x >= world[0].length || y >= world.length)
		{
			System.out.println("Out of bounds (" + x + "," + y + ")");
		}
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

	public int getRemainingSteps() 
	{
		return remainingSteps;
	}

	public void save(String saveDir) 
	{
		dirtRecord.saveToFile(saveDir);
	}
}
