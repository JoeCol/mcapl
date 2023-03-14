package responsibility.TheoryImpl;

import java.awt.Color;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayDeque;
import java.util.Random;
import java.util.Timer;
import java.util.TreeSet;
import ail.mas.DefaultEnvironment;
import ail.mas.scheduling.RoundRobinScheduler;
import ail.semantics.AILAgent;
import ail.syntax.Action;
import ail.syntax.ListTerm;
import ail.syntax.ListTermImpl;
import ail.syntax.Literal;
import ail.syntax.Message;
import ail.syntax.NumberTerm;
import ail.syntax.Predicate;
import ail.syntax.StringTerm;
import ail.syntax.StringTermImpl;
import ail.syntax.Term;
import ail.syntax.Unifier;
import ail.util.AILexception;
import ajpf.MCAPLJobber;

interface UpdateToWorld{
	void worldUpdate(int time, int dirt, int badDirt, WorldCell[][] world, HashMap<String, Pair<Integer,Integer>> agentLocations, HashMap<String, Color> agentColours);
}

public class CleaningWorld extends DefaultEnvironment implements MCAPLJobber
{
	RoundRobinScheduler rrs = new RoundRobinScheduler();
	public enum AgentAction {aa_moveup, aa_movedown, aa_moveright, aa_moveleft, aa_clean, aa_observedirt, aa_moveupleft, aa_moveupright, aa_movedownleft, aa_movedownright, aa_finish, aa_none}
	Routes routeToZones = new Routes();
	WorldCell[][] world;
	int remainingSteps = 100;
	int totalTime = 100;
	int simSpeed = 350;
	Settings currentSettings;
	String saveLocation;
	ArrayDeque<Pair<String, Predicate>> perceptAdds = new ArrayDeque<Pair<String, Predicate>>();
	ArrayDeque<Pair<String, Predicate>> perceptRems = new ArrayDeque<Pair<String, Predicate>>();
	ArrayDeque<Pair<String, Message>> perceptFin = new ArrayDeque<Pair<String, Message>>();
	ArrayList<UpdateToWorld> worldListeners = new ArrayList<UpdateToWorld>();
	ArrayList<String> agents = new ArrayList<String>();
	HashMap<String, Color> agentColours = new HashMap<String, Color>();
	HashMap<Character, ArrayList<Pair<Integer, Integer>>> zoneSquares = new HashMap<Character, ArrayList<Pair<Integer, Integer>>>();
	
	HashMap<String, Pair<Integer,Integer>> agentLocations = new HashMap<String, Pair<Integer,Integer>>();
	HashMap<String, ArrayDeque<AgentAction>> agentActions = new HashMap<String, ArrayDeque<AgentAction>>();
	HashMap<String, String> workingOn = new HashMap<String, String>();
	HashMap<String, HashMap<String, Integer>> agentCares = new HashMap<String, HashMap<String,Integer>>();
	
	
	Random r = new Random();
	private HashMap<String, Integer> cleanCountdown = new HashMap<String, Integer>();
	private Integer cleanLength = 30;
	//Variables for naive cleaner
	ArrayDeque<Character> naiveQueue = new ArrayDeque<Character>();
	boolean naive;

	//variables for dirt management
	int dirtNum = 0;
	int badDirtNum = 0;
	int totalDirt = 0;
	int totalBadDirt = 0;
	ArrayList<Pair<Integer,Integer>> possibleDirtLocations = new ArrayList<Pair<Integer,Integer>>();
	DirtRecord dirtRecord = new DirtRecord();
	
	int envCount = 0;
	int gwenTime = 2000;

	public Settings getSettings()
	{
		return currentSettings;
	}
	
	public void addWorldListeners(UpdateToWorld u)
	{
		worldListeners.add(u);
	}
	
	@Override
	public void init_after_adding_agents() 
	{
		//Randomly position agents
		int x = 1;
		for (AILAgent a : getAgents())
		{	
			agentLocations.put(a.getAgName(), new Pair<Integer,Integer>(x++,1));
			agentColours.put(a.getAgName(), new Color(r.nextInt(0xFFFFFF)));
			agentActions.put(a.getAgName(), new ArrayDeque<AgentAction>());
			
			//Add agent care function to map for easy reference
   			ArrayList<Literal> beliefs = a.getBB().getAll();
   			HashMap<String, Integer> care = new HashMap<String,Integer>();
			for (int j = 0; j < beliefs.size(); j++)
			{
				if (beliefs.get(j).getFunctor().equals("care"))
				{
					care.put(beliefs.get(j).getTerm(0).toString(), Integer.parseInt(beliefs.get(j).getTerm(1).toString()));
				}
			}
			agentCares.put(a.getAgName(), care);
		}
		
		for (UpdateToWorld u : worldListeners)
		{
			u.worldUpdate(remainingSteps, totalDirt, totalBadDirt, world, agentLocations, agentColours);
		}
	}
	
	public void addDirt(boolean bad, int time) 
	{
		if (possibleDirtLocations.size() > 0)
		{
			Collections.shuffle(possibleDirtLocations);//to ensure that dirt is not evenly distributed as it is cleaned.
			Pair<Integer,Integer> newDirt = possibleDirtLocations.remove(0);
			getCell(newDirt.getFirst(),newDirt.getSecond()).setDirty(bad, time);
			totalDirt++;
			if (bad) {totalBadDirt++;}
			dirtRecord.addRecord(remainingSteps, totalDirt, totalBadDirt);
		}
	}

	public Pair<Integer, Integer> getAgentLocation(String agName) 
	{
		return agentLocations.get(agName);
	}

	public CleaningWorld(int simSteps, int dirtInt, int badDirtInt, String worldLoc, String saveLoc, int _simSpeed)
	{
		currentSettings = new Settings(0, 0, simSteps, dirtInt, badDirtInt, worldLoc);
		simSpeed = _simSpeed;
		totalTime = simSteps;
		saveLocation = saveLoc;
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
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		setup_scheduler(this, rrs);
		rrs.addJobber(this);
	}
	
	public Unifier executeAction(String agName, Action act) throws AILexception {
	   	Unifier theta = super.executeAction(agName, act);
	   	//System.out.println(agName + ": executes:" + act.fullstring());
  		//Should only place actions onto agentaction stack
	   	ListTerm fullRes;
	   	switch (act.getFunctor())
	   	{
	   		case "addFin":
	   			agentActions.get(agName).add(AgentAction.aa_finish);
	   			break;
	   		case "assumeClean":
	   			char zone = act.getTerm(0).toString().charAt(1);
	   			//System.out.println("assuming clean " + zone);
	   			Predicate p = new Predicate("observed");
	   			p.addTerm(new Predicate((zone + "").toLowerCase()));
	   			p.addTerm(new Predicate("false"));
	   			p.addTerm(new Predicate("false"));
	   			p.addTerm(new Predicate("true"));
	   			perceptFin.add(new Pair<String,Message>(agName, new Message(1,"env",agName, p)));
	   			updateCareValues(agName,zone + "",false,false);
	   			break;
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
	   		case "observe":
	   			agentActions.get(agName).add(AgentAction.aa_observedirt);
	   			break;
	   		case "clean":
	   			addCleaningActions(agName, ((StringTerm)act.getTerm(0)).getString().charAt(0));
	   			break;
	   		case "goToZone":
	   			goToZone(agName, ((StringTerm)act.getTerm(0)).getString().charAt(0));
	   			break;
	   		case "addResItem":
	   			Predicate res = (Predicate)act.getTerm(0);
	   			ListTerm resList = (ListTerm)act.getTerm(1);
	   			int careOfRes = agentCares.get(agName).get(res.toString());
	   			if (resList.size() != 0)
	   			{
	   				int posToAdd = resList.size();
		   			for (int i = 0; i < resList.size(); i++)
		   			{
		   				if (careOfRes > agentCares.get(agName).get(resList.get(i).toString()))
		   				{
		   					posToAdd = i;
		   					break;
		   				}
		   			}
		   			resList.add(posToAdd,res);
	   			}
	   			else
	   			{
	   				resList.add(res);
	   			}
	   			resList.unifies(act.getTerm(2), theta);
	   			break;
	   		case "getCombos":
	   			fullRes = (ListTerm)act.getTerm(0);
	   			ListTerm combos = getCombos(fullRes);
	   			combos.unifies(act.getTerm(1), theta);
	   			break;
	   		case "getViable":
	   			fullRes = (ListTerm)act.getTerm(0);
	   			ListTerm viable = getViable(agName, fullRes);
	   			viable.unifies(act.getTerm(1), theta);
	   			break;
	   		case "selectCared":
	   			fullRes = (ListTerm)act.getTerm(0);
	   			ListTerm mostCared = getMostCared(fullRes, agName);
	   			mostCared.unifies(act.getTerm(1), theta);
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
	   		case "clearActions":
	   			agentActions.get(agName).clear();
	   			break;
	   		case "doing":
	   			workingOn.put(agName, act.getTerm(0).toString());
	   			break;
	   		case "getName":
	   			Predicate uName = new Predicate(agName);
	   			uName.unifies(act.getTerm(0), theta);
	   			break;
	   		case "break":
	   			System.out.print(agName + ": ");
	   			System.out.println("Breakpoint");
	   			System.out.println(agentActions.toString());
	   			break;
	   		case "printstate":
	   		case "send":
	   			break;
	   		default:
	   			System.out.println(act.getFunctor() + " has not been implemented");
	   	}
	   	//System.out.println(agName + ":" + agentActions.get(agName).toString());
    	return theta;
    }
	
	private ListTerm getMostCared(ListTerm fullRes, String agName) {
		ListTerm fullResUnique = new ListTermImpl();
		for (int i = 0; i < fullRes.size(); i++)
		{
			ListTerm resUnique = new ListTermImpl();
			ListTerm lst = (ListTerm)fullRes.get(i);
			for (int j = 0; j < lst.size(); j++)
			{
				boolean unique = false;
				for (int k = 0; k < fullRes.size(); k++)
				{
					if (i != k)
					{
						if (!((ListTerm)fullRes.get(k)).contains(lst.get(j)))
						{
							unique = true;
							break;
						}
					}
				}
				if (unique)
				{
					resUnique.add(lst.get(j));
				}
			}
			fullResUnique.add(resUnique);
		}
		TreeSet<CaredItem> caredList = new TreeSet<CaredItem>();
		for (int i = 0; i < fullResUnique.size(); i++)
		{
			ListTerm lst = (ListTerm)fullResUnique.get(i);
			CaredItem item = new CaredItem();
			item.index = i;
			item.size = lst.size();
			int max = 0;
			for (int j = 0; j < lst.size(); j++)
			{
				max = agentCares.get(agName).get(lst.get(j).toString()) > max ? agentCares.get(agName).get(lst.get(j).toString()) : max;
			}
			item.highestValue = max;
			caredList.add(item);
		}
		return (ListTerm)fullRes.get(caredList.last().index);
	}

	private ListTerm getViable(String agName, ListTerm fullRes) {
		ListTerm viableLst = new ListTermImpl();
		ListTerm commonLst = new ListTermImpl();
		ArrayList<Predicate> individual = new ArrayList<Predicate>();

		String[] items = fullRes.toString().substring(1, fullRes.toString().length() - 1).split(",");
		for (String item : items)
		{
			if (item.matches("cleanBadDirt[A-Z]"))
			{
				individual.add(new Predicate(item));
			}
			else if (item.matches("clean[A-Z]"))
			{
				individual.add(new Predicate(item));
			}
			else if (item.matches("observe[A-Z]"))
			{
				individual.add(new Predicate(item));
			}
			else if (!item.matches("cleanBadDirt[A-Z]|clean[A-Z]|observe[A-Z]"))
			{
				commonLst.add(new Predicate(item));
			}
		}
		if (individual.size() > 0)
		{
			for (Predicate p : individual)
			{
				ListTerm tmp = new ListTermImpl();
				for (Term t : commonLst)
				{
					tmp.add(t);
				}
				tmp.add(p);
				tmp.sort(new Comparator<Term>() {
					@Override
					public int compare(Term o1, Term o2) {
						int rst = agentCares.get(agName).get(o1.toString()).compareTo(agentCares.get(agName).get(o2.toString()));
						return -rst;//largest first
					}
					
				});
				viableLst.add(tmp);
			}
		}
		else
		{
			viableLst.add(commonLst);
		}
		
		return viableLst;
	}
	
	private List<Term> getSubset(List<Term> input, int[] index) {
		List<Term> result = new ArrayList<Term>(); 
	    for (int i = 0; i < index.length; i++)
	    {
	        result.add(input.get(index[i]));
		}
	    return result;
	}
	
	private ListTerm getCombos(ListTerm fullRes) {
		List<Term> res = fullRes.getAsList();
		ArrayList<List<Term>> subsets = new ArrayList<>();
		//Get all combinations
		for (int size = 1; size <= res.size(); size++)
		{
			int[] s = new int[size];                  // here we'll keep indices pointing to elements in input array

			if (size <= res.size()) 
			{
				// first index sequence: 0, 1, 2, ...
				for (int j = 0; j < size; j++)
				{
					s[j] = j;
				}
				subsets.add(getSubset(res,s));
				boolean itemFound = true;
				while (itemFound)
				{
					int start;
					for (start = size - 1; start >= 0 && s[start] == res.size() - size + start; start--); //Decrement until item found
					if (start >= 0)
					{
						s[start]++;                    // increment this item
						for (++start; start < size; start++) {    // fill up remaining items
							s[start] = s[start - 1] + 1; 
						}
						subsets.add(getSubset(res, s));
					}
					else
					{
						itemFound = false;
					}
				}
			}
		}
		ListTerm lt = new ListTermImpl();
		for (int i = 0; i < subsets.size(); i++)
		{
			ListTerm sl = new ListTermImpl();
			for (int j = 0; j < subsets.get(i).size(); j++)
			{
				sl.add(subsets.get(i).get(j));
			}
			lt.add(sl);
		}
		return lt;
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
		agentActions.get(agName).addAll(actions);
	}

	private void observeDirt(String agName) 
	{
		//Get zone for agent
		Pair<Integer,Integer> l = agentLocations.get(agName);
		char zone = getCell(l.getFirst(),l.getSecond()).getZoneID();
		boolean hasDirt = false;
		boolean hasBadDirt = false;
		boolean isClear = false;
		//System.out.println(zone + " observed");
		for (int i = 0; i < world.length; i++)
		{
			WorldCell[] row = world[i];
			for (int j = 0; j < row.length; j++)
			{
				WorldCell cell = row[j];
				if (cell.getZoneID() == zone)
				{
					if (cell.hasBadDirt())
					{
						hasDirt = true;
						hasBadDirt = true;
						//System.out.println(zone + " has bad dirt at " + j + " " + i);
						break;
					}
					else if (cell.hasDirt())
					{
						hasDirt = true;
						//System.out.println(zone + " has dirt at " + j + " " + i);
					}
				}
			}
		}
		isClear = !hasDirt && !hasBadDirt;
		Predicate p = new Predicate("observed");
		p.addTerm(new Predicate((zone + "").toLowerCase()));
		p.addTerm(new Predicate(hasDirt + ""));
		p.addTerm(new Predicate(hasBadDirt + ""));
		p.addTerm(new Predicate(isClear + ""));
		updateCareValues(agName,zone + "",hasDirt,hasBadDirt);
		perceptFin.add(new Pair<String,Message>(agName, new Message(1, "env", agName, p)));
	}


	private void updateCareValues(String agName, String zone, boolean hasDirt, boolean hasBadDirt) 
	{
		int dirtCare = hasDirt ? 6 : 1;
		int badDirtCare = hasBadDirt ? 7 : 2;
		String dirtCareStr = "clean" + zone.toUpperCase();
		String baddirtCareStr = "cleanBadDirt" + zone.toUpperCase();
		agentCares.get(agName).put(dirtCareStr, dirtCare);
		agentCares.get(agName).put(baddirtCareStr, badDirtCare);
	}

	//Add move actions to agentactions stack
	private void goToZone(String agName, char zone) 
	{
		//get route
		ArrayDeque<AgentAction> moveActions = routeToZones.actionsToZone(world, getAgentLocation(agName), zoneSquares.get(zone).get(0));
		agentActions.get(agName).addAll(moveActions);
	}

	private void clean(int x, int y, int time) 
	{
		if (getCell(x,y).hasDirt())
		{
			boolean badDirt = false;
			if (getCell(x,y).hasBadDirt())
			{
				totalBadDirt--;
				badDirt = true;
			}
			totalDirt--;
			possibleDirtLocations.add(new Pair<Integer,Integer>(x,y));
			dirtRecord.addRecord(remainingSteps, totalDirt, totalBadDirt);
			getCell(x, y).clean(time);
			dirtRecord.addTimeRecord(badDirt, getCell(x, y).timeAlive());
		}
	}
	
	//Change environment percepts
	private void moveAgent(String agName, int x, int y)
	{
		agentLocations.put(agName, new Pair<Integer,Integer>(x,y));		
	}

	@Override
	public int compareTo(MCAPLJobber o) 
	{
		return o.hashCode() - hashCode();
	}

	@Override
	public void do_job() 
	{
		if (envCount >= gwenTime)
		{
			envCount = -1;
			for (AILAgent a : getAgents())
			{
				ArrayDeque<AgentAction> actionStack = agentActions.get(a.getAgName());
				if (!actionStack.isEmpty())
				{
					AgentAction action = actionStack.peek();
					Pair<Integer, Integer> agentLocation = getAgentLocation(a.getAgName());
					boolean finishedAction = true;
					if (agentLocation.getFirst() != -1)
					{
						switch (action)
						{
						case aa_clean:
							if (cleanCountdown.containsKey(a.getAgName()) && cleanCountdown.get(a.getAgName()) == 0)
							{
								clean(agentLocation.getFirst(), agentLocation.getSecond(),remainingSteps); 
								cleanCountdown.remove(a.getAgName());
							}
							else if (cleanCountdown.containsKey(a.getAgName()))
							{
								cleanCountdown.put(a.getAgName(),cleanCountdown.get(a.getAgName()) - 1);
								finishedAction = false;
							}
							else
							{
								cleanCountdown.put(a.getAgName(),cleanLength);
								finishedAction = false;
							}
							break;
						case aa_movedown:
							moveAgent(a.getAgName(), agentLocation.getFirst(), agentLocation.getSecond() + 1);
							break;
						case aa_moveleft:
							moveAgent(a.getAgName(), agentLocation.getFirst() - 1, agentLocation.getSecond());
							break;
						case aa_moveright:
							moveAgent(a.getAgName(), agentLocation.getFirst() + 1, agentLocation.getSecond());
							break;
						case aa_moveup:
							moveAgent(a.getAgName(), agentLocation.getFirst(), agentLocation.getSecond() - 1);
							break;
						case aa_movedownleft:
							moveAgent(a.getAgName(), agentLocation.getFirst() - 1, agentLocation.getSecond() + 1);
							break;
						case aa_movedownright:
							moveAgent(a.getAgName(), agentLocation.getFirst() + 1, agentLocation.getSecond() + 1);
							break;
						case aa_moveupleft:
							moveAgent(a.getAgName(), agentLocation.getFirst() - 1, agentLocation.getSecond() - 1);
							break;
						case aa_moveupright:
							moveAgent(a.getAgName(), agentLocation.getFirst() + 1, agentLocation.getSecond() - 1);
							break;
						case aa_observedirt:
							observeDirt(a.getAgName());
							break;
						case aa_finish:
							perceptAdds.add(new Pair<String, Predicate>(a.getAgName(), new Predicate("finished")));
							Predicate msgPred = new Predicate("finished");
							if (workingOn.containsKey(a.getAgName()))
							{
								msgPred.addTerm(new Predicate(workingOn.get(a.getAgName())));
								workingOn.remove(a.getAgName());
						 		perceptFin.add(new Pair<String,Message>(a.getAgName(),new Message(1,"env",a.getAgName(),msgPred)));
							}
							else
							{
								System.out.println("Should never happen");
							}
							break;
						case aa_none:
							break;
						default:
							break;
						}
					}
					if (finishedAction)
					{
						actionStack.poll();
					}
				}
			}
			
			//Do dirt step
			dirtNum = (++dirtNum) % currentSettings.getDirtInterval();
			if (dirtNum == 0)
			{
				badDirtNum = (++badDirtNum) % currentSettings.getBadDirtInterval();
				if (badDirtNum != 0)
				{
					addDirt(false,remainingSteps);addDirt(false,remainingSteps);addDirt(false,remainingSteps);addDirt(false,remainingSteps);addDirt(false,remainingSteps);
				}
				else
				{
					addDirt(true,remainingSteps);addDirt(true,remainingSteps);addDirt(true,remainingSteps);
				}
			}
			remainingSteps--;
			if (simSpeed > 0)
			{
				try {
					Thread.sleep(simSpeed);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		//Do dirt step
		envCount++;
		
		while (!perceptRems.isEmpty())
		{
			Pair<String, Predicate> p = perceptRems.poll();
			//System.out.println("Removing percept " + p.getSecond().toString() + " from agent " + p.getFirst());
			removePercept(p.getFirst(), p.getSecond());
		}
		while (!perceptAdds.isEmpty())
		{
			Pair<String, Predicate> p = perceptAdds.poll();
			//System.out.println("Adding percept " + p.getSecond().toString() + " to agent " + p.getFirst());
			addPercept(p.getFirst(), p.getSecond());
		}
		while (!perceptFin.isEmpty())
		{
			Pair<String, Message> p = perceptFin.poll();
			addMessage(p.getFirst(), p.getSecond());
			//System.out.println("Message: " + p.getSecond().toString());
		}
		
		this.notifyListeners();
		
		for (UpdateToWorld u : worldListeners)
		{
			u.worldUpdate(remainingSteps, totalDirt, totalBadDirt, world, agentLocations, agentColours);
		}
		
		if (remainingSteps <= 0)
		{
			save();
			System.exit(0);
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

	private WorldCell getCell(int x, int y) 
	{
		if (y == -1 || x == -1 || x >= world[0].length || y >= world.length)
		{
			System.out.println("Out of bounds (" + x + "," + y + ")");
		}
		return world[y][x];
	}

	public void save() 
	{
		dirtRecord.saveToFile(saveLocation,totalTime);
	}
}
