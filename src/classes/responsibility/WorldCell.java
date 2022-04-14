package responsibility;

import java.util.Random;

public class WorldCell
{
	private boolean hasDirt;
	private boolean traversable;
	private boolean occupied;

	public boolean hasDirt() 
	{
		return hasDirt;
	}

	//changeOfDirt as percentage 0-100
	public void setChangeOfDirt(int changeOfDirt) 
	{
		if (!hasDirt)
		{
			if (new Random().nextInt(100) <= (changeOfDirt - 1))
			{
				hasDirt = true;
			}
		}
	}
	
	public void clean()
	{
		hasDirt = false;
	}

	public boolean isOccupied() {
		return traversable || occupied;
	}

	public WorldCell(boolean traversable) {
		super();
		this.traversable = traversable;
	}

	public void setOccupied(boolean occupied) {
		this.occupied = occupied;
	}

	public boolean isTraversable() {
		return traversable;
	}
}
