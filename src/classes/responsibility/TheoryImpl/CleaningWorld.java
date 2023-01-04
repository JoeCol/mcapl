package responsibility.TheoryImpl;

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
import ail.syntax.StringTermImpl;
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
	public enum AgentAction {aa_moveup, aa_movedown, aa_moveright, aa_moveleft, aa_clean, aa_observedirt, aa_moveupleft, aa_moveupright, aa_movedownleft, aa_movedownright, aa_finishclean}
	public enum Process {p_nochanges, p_updatedPercept, p_updatePercept};
	private volatile Process currentState = Process.p_nochanges;
	Routes routeToZones = new Routes();
	volatile WorldCell[][] world;
	int simulationDelay = 100;
	Timer environmentTimer = new Timer();
	Settings currentSettings;
	RoundRobinScheduler rrs = new RoundRobinScheduler();
	ArrayList<UpdateToWorld> listeners = new ArrayList<UpdateToWorld>();
	HashMap<String, Color> agentColours = new HashMap<String, Color>();
	HashMap<Integer, ArrayList<Pair<Integer, Integer>>> zoneSquares = new HashMap<Integer, ArrayList<Pair<Integer, Integer>>>();
	
	HashMap<String, ArrayDeque<AgentAction>> agentActions = new HashMap<String, ArrayDeque<AgentAction>>();
	HashMap<String, String> workingOn = new HashMap<String, String>();
	
	ConcurrentLinkedQueue<Pair<String, Predicate>> perceptAdds = new ConcurrentLinkedQueue<Pair<String, Predicate>>();
	ConcurrentLinkedQueue<Pair<String, Predicate>> perceptRems = new ConcurrentLinkedQueue<Pair<String, Predicate>>();
	
	
	
	Random r = new Random();
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
			agentActions.put(a.getAgName(), new ArrayDeque<AgentAction>());
		}
		
		//For Testing
		Predicate msgPred = new Predicate("assignment");
		ListTermImpl lt = new ListTermImpl();
		lt.add(new Predicate("initial"));
		msgPred.addTerm(lt);
		msgPred.addTerm(new Predicate("safety"));
		addMessage("manager", new Message(1, "initial", "manager", msgPred));
		
		environmentTimer.scheduleAtFixedRate(new TimerTask()
		{
			//method to run through thread, delay is caused by timer
			public void run()
			{
				//Each action to the environment gives a change at dirt.
				for (int x = 0; x < world.length; x++)
				{
					for (int y = 0; y < world[x].length; y++)
					{
						world[x][y].setChangeOfDirt(currentSettings.getDirtAppearanceChange());
					}
				}
				
				//Get ONE action from each agent
				for (AILAgent a : getAgents())
				{
					ArrayDeque<AgentAction> actionStack = agentActions.get(a.getAgName());
					if (!actionStack.isEmpty())
					{
						
						AgentAction action = actionStack.pop();
						Pair<Integer, Integer> agentLocation = getAgentLocation(a.getAgName());
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
						case aa_finishclean:
							//perceptAdds.add(new Pair<String, Predicate>(a.getAgName(), new Predicate("finished")));
							break;
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
			}

			
		}, 0, simulationDelay);
		
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

	public CleaningWorld()
	{
		loadSettings();
		try
		{
			zoneSquares.clear();
			world = new WorldCell[currentSettings.getHeightOfMap()][currentSettings.getWidthOfMap()];
			BufferedReader br = new BufferedReader(new FileReader(currentSettings.getWorldFileLocation()));
			String line = br.readLine();
			for (int y = 0; y < world.length; y++)
			{
				for (int x = 0; x < world[0].length; x++)
				{
					String lineChar = line.substring(x,x+1);
					int zoneNum = Integer.parseInt(lineChar);
					zoneSquares.putIfAbsent(zoneNum, new ArrayList<Pair<Integer, Integer>>());
					zoneSquares.get(zoneNum).add(new Pair<Integer, Integer>(x,y));
					world[y][x] = new WorldCell(zoneNum);
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
	   	Unifier theta = super.executeAction(agName, act);
	   	//System.out.println(agName + ": executes:" + act.fullstring());
  		//Should only place actions onto agentaction stack
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
	   		case "do_clean":
	   			addCleaningActions(agName, (int)((NumberTerm)act.getTerm(0)).solve());
	   			break;
	   		case "go_to_zone":
	   			goToZone(agName, (int)((NumberTerm)act.getTerm(0)).solve());
	   			break;
	   		case "finishClean":
	   			removePercept(agName, new Predicate("finished"));
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
	   			for (int i = 0; i < act.getTermsSize(); i++)
	   			{
	   				if (!act.getTerm(i).isString())
	   				{
	   					System.out.print(act.getTerm(i).toString());
	   				}
	   				else
	   				{
	   					System.out.print(agName + ": " +act.getTerm(i).toString().substring(1, act.getTerm(i).toString().length() - 1));
	   				}
	   			}
	   			System.out.println();
	   			break;
	   		case "do":
	   			Term action = act.getTerm(0);
	   			Boolean actionFinished = doAction(action.toString(),agName);
	   			Predicate finished = new Predicate(actionFinished.toString());
	   			finished.unifies(act.getTerm(1), theta);
	   			break;
	   		case "break":
	   			System.out.println("Breakpoint");
	   			break;
	   		case "printstate":
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
	
	private Boolean doAction(String action, String agName) 
	{
		//Check if agent has finished given task
		if (workingOn.containsKey(agName) && workingOn.get(agName).equals(action))
		{
			//Agent has no actions left for the task
			if (agentActions.get(agName).size() == 0)
			{
				workingOn.remove(agName); 
				return true;
			}
			else
			{
				return false; //Still working on the same action
			}
		}
		//Check if currently working on a different action, clear for new action
		if (workingOn.containsKey(agName) && !workingOn.get(agName).equals(action))
		{
			agentActions.clear();
			workingOn.remove(agName);
		}
		//Set the agent to working on
		workingOn.put(agName, action);
		switch(action) //while debugging
		{
		case "clean1":
			goToZone(agName, 1);
			addCleaningActions(agName,1);
			break;
		case "clean2":
			goToZone(agName, 2);
			addCleaningActions(agName,2);
			break;
		case "clean3":
			goToZone(agName, 3);
			addCleaningActions(agName,3);
			break;
		case "clean4":
			goToZone(agName, 4);
			addCleaningActions(agName,4);
			break;
		case "clean5":
			goToZone(agName, 5);
			addCleaningActions(agName,5);
			break;
		case "observe1":
			goToZone(agName, 1);
			observeDirt(agName);
			break;
		case "observe2":
			goToZone(agName, 2);
			observeDirt(agName);
			break;
		case "observe3":
			goToZone(agName, 3);
			observeDirt(agName);
			break;
		case "observe4":
			goToZone(agName, 4);
			observeDirt(agName);
			break;
		case "observe5":
			goToZone(agName, 5);
			observeDirt(agName);
			break;
		}
		//No action is instant
		return false;
	}

	private void addCleaningActions(String agName, int zone) 
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
		actions.add(AgentAction.aa_finishclean);
		agentActions.get(agName).addAll(actions);
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
					perceptAdds.add(new Pair<String, Predicate>(agName, p));
					perceptAdds.add(new Pair<String, Predicate>(agName, new Predicate("observed")));
					return 1;
				}
			}
		}
		perceptRems.add(new Pair<String, Predicate>(agName, p));
		perceptAdds.add(new Pair<String, Predicate>(agName, new Predicate("observed")));
		return 0;//No dirty, 0 is false
	}


	//Add move actions to agentactions stack
	private void goToZone(String agName, int zone) 
	{
		//get route
		ArrayDeque<AgentAction> moveActions = routeToZones.actionsToZone(world, getAgentLocation(agName), zoneSquares.get(zone).get(0));
		agentActions.get(agName).addAll(moveActions);
	}

	private void clean(int x, int y) 
	{
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
			zoneTerms.add(new NumberTermImpl(getCell(x,y).getZoneNumber()));
			Predicate p1 = new Predicate("zone");
			p1.setTerms(zoneTerms);
			perceptRems.add(new Pair<String, Predicate>(agName, p1));
			
			ArrayList<Term> zoneUpdateTerms = new ArrayList<Term>();
			zoneUpdateTerms.add(new NumberTermImpl(getCell(newX, newY).getZoneNumber()));
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
		for (UpdateToWorld u : listeners)
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
}
