package responsibility;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;

import gov.nasa.jpf.util.Pair;
import responsibility.CleaningWorld.AgentAction;

/*
 *  Precalculates best route to zone start
 */
public class Routes
{
	public static boolean samePair(Pair<Integer,Integer> a, Pair<Integer, Integer> b)
	{
		if (a._1 == b._1 && a._2 == b._2)
		{
			return true;
		}
		return false;
	}
	
	private int[][] fillSurrounding(WorldCell[][] tmpWorld, int[][] costs, Pair<Integer, Integer> startLocation, HashMap<Pair<Integer, Integer>, Boolean> checked) 
	{
		boolean done = false;
		while (!done)
		{
			done = true;
			for (int x = 0; x < tmpWorld[0].length; x++)
			{
				for (int y = 0; y < tmpWorld.length; y++)
				{
					if (checked.get(new Pair<Integer, Integer>(x,y)) == null)
					{
						
						if (!tmpWorld[y][x].isTraversable())
						{
							checked.put(new Pair<Integer, Integer>(x,y), true);
						}
						else
						{
							//Get Surrounding Requires surround wall
							int minimum = Integer.MAX_VALUE;
							minimum = Math.min(costs[y + 1][x + 1], minimum);
							minimum = Math.min(costs[y + 1][x + -1], minimum);
							minimum = Math.min(costs[y + 1][x], minimum);
							minimum = Math.min(costs[y][x - 1], minimum);
							minimum = Math.min(costs[y][x + 1], minimum);
							minimum = Math.min(costs[y - 1][x + 1], minimum);
							minimum = Math.min(costs[y - 1][x -1], minimum);
							minimum = Math.min(costs[y - 1][x], minimum);
							if (minimum != Integer.MAX_VALUE && minimum != 0)
							{
								costs[y][x] = minimum + 1;
								checked.put(new Pair<Integer, Integer>(x,y), true);
							}
						}
						//System.out.println(checked.size() + " vs " + (tmpWorld.length * tmpWorld[0].length));
						done = checked.size() == tmpWorld.length * tmpWorld[0].length;
					}
				}
			}
		}
		return costs;
	}

	
	private int[][] generateCosts(WorldCell[][] world, int destX, int destY)
	{
		WorldCell[][] tmpWorld = world;
		//arrays are stored y, x
		int[][] costs = new int[world.length][world[0].length];
		for (int[] rows : costs)
		{
			Arrays.fill(rows, Integer.MAX_VALUE);
		}
		costs[destY][destX] = 1;
		HashMap<Pair<Integer,Integer>, Boolean> checked = new HashMap<Pair<Integer,Integer>, Boolean>();
		checked.put(new Pair<Integer, Integer>(destX,destY), true);
		costs = fillSurrounding(world, costs, new Pair<Integer, Integer>(destX, destY), checked);
		
		/*for (int[] rows : costs)
		{
			for (int i : rows)
			{
				System.out.print(i + ",");
			}
			System.out.println();
		}*/
		return costs;
	}

	public ArrayDeque<AgentAction> actionsToZone(WorldCell[][] world, Pair<Integer, Integer> agentLocation, Pair<Integer, Integer> agentDestination) 
	{
		int[][] costs = generateCosts(world, agentDestination._1, agentDestination._2);
		
		Pair<Integer, Integer> travXY = agentLocation;
		ArrayDeque<AgentAction> toRet = new ArrayDeque<AgentAction>();
		
		int[] costNext = new int[9];
		int min = Integer.MAX_VALUE;
		int minIndex = 0;
		while (!samePair(travXY, agentDestination))
		{
			min = Integer.MAX_VALUE;
			minIndex = 0;
			//Get costs of surrounding squares Cost Array is Y X
			costNext[0] = costs[travXY._2 - 1][travXY._1 - 1];//Top left
			costNext[1] = costs[travXY._2 - 1][travXY._1];//Top middle
			costNext[2] = costs[travXY._2 - 1][travXY._1 + 1];//Top right
			costNext[3] = costs[travXY._2][travXY._1 - 1];//middle left
			//costNext[4] = costs[travXY._1][travXY._2]; //Middle square
			costNext[4] = costs[travXY._2][travXY._1 + 1]; //Middle right
			costNext[5] = costs[travXY._2 + 1][travXY._1 - 1]; //bottom left square
			costNext[6] = costs[travXY._2 + 1][travXY._1]; //bottom middle square;
			costNext[7] = costs[travXY._2 + 1][travXY._1 + 1]; //bottom left square;
			//Find minimum, and its index
			for (int i = 0; i < 8; i++)
			{
				if (costNext[i] < min)
				{
					min = costNext[i];
					minIndex = i;
				}
			}
			//choose action
			switch(minIndex)
			{
			case 0:
				toRet.add(AgentAction.aa_moveupleft);
				travXY = new Pair<Integer, Integer>(travXY._1 - 1, travXY._2 - 1);
				break;
			case 1:
				toRet.add(AgentAction.aa_moveup);
				travXY = new Pair<Integer, Integer>(travXY._1, travXY._2 - 1);
				break;
			case 2:
				toRet.add(AgentAction.aa_moveupright);
				travXY = new Pair<Integer, Integer>(travXY._1 + 1, travXY._2 - 1);
				break;
			case 3:
				toRet.add(AgentAction.aa_moveleft);
				travXY = new Pair<Integer, Integer>(travXY._1 - 1, travXY._2);
				break;
			/*case 4:
				travXY = new Pair<Integer, Integer>(travXY._1, travXY._2);
				break;*/
			case 4:
				toRet.add(AgentAction.aa_moveright);
				travXY = new Pair<Integer, Integer>(travXY._1 + 1, travXY._2);
				break;
			case 5:
				toRet.add(AgentAction.aa_movedownleft);
				travXY = new Pair<Integer, Integer>(travXY._1 - 1, travXY._2 + 1);
				break;
			case 6:
				toRet.add(AgentAction.aa_movedown);
				travXY = new Pair<Integer, Integer>(travXY._1, travXY._2 + 1);
				break;
			case 7:
				toRet.add(AgentAction.aa_movedownright);
				travXY = new Pair<Integer, Integer>(travXY._1 + 1, travXY._2 + 1);
				break;
			}
		}
		
		return toRet;
	}

}
