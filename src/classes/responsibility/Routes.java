package responsibility;

import java.util.Arrays;
import java.util.HashMap;

import gov.nasa.jpf.util.Pair;

/*
 *  Precalculates best route to zone start
 */
public class Routes
{

	HashMap<Integer, int[][]> costsForZones = new HashMap<Integer, int[][]>();
	WorldCell[][] tmpWorld;
	
	public Routes(WorldCell[][] world, HashMap<Integer, Pair<Integer, Integer>> zoneStart) 
	{
		tmpWorld = world;
		zoneStart.forEach((zone, startLocation) -> 
		{
			//arrays are stored y, x
			int[][] costs = new int[world.length][world[0].length];
			for (int[] rows : costs)
			{
				Arrays.fill(rows, Integer.MAX_VALUE);
			}
			costs[startLocation._2][startLocation._1] = 1;
			HashMap<Pair<Integer,Integer>, Boolean> checked = new HashMap<Pair<Integer,Integer>, Boolean>();
			checked.put(new Pair<Integer, Integer>(startLocation._1,startLocation._2), true);
			costs = fillSurrounding(costs, startLocation, checked);
			
			costsForZones.put(zone, costs);
		});
	}

	private int[][] fillSurrounding(int[][] costs, Pair<Integer, Integer> startLocation, HashMap<Pair<Integer, Integer>, Boolean> checked) 
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
						System.out.println(checked.size() + " vs " + (tmpWorld.length * tmpWorld[0].length));
						done = checked.size() == tmpWorld.length * tmpWorld[0].length;
					}
				}
			}
		}
		for (int[] rows : costs)
		{
			for (int cell : rows)
			{
				System.out.print(cell + ",");
			}
			System.out.println();
		}
		return costs;
	}

	public Pair<Integer, Integer> toZone(int zone, int x, int y) 
	{
		for (int newX = -1; newX < 2; newX++)
		{
			for (int newY = -1; newY < 2; newY++)
			{
				if (costsForZones.get(zone)[y][x] > costsForZones.get(zone)[y + newY][x + newX])
				{
					return new Pair<Integer, Integer>(x + newX, y + newY);
				}
			}
		}
		return new Pair<Integer, Integer>(x, y);
	}

}
