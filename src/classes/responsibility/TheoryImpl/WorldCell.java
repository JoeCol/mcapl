package responsibility.TheoryImpl;

import java.util.Random;

public class WorldCell
{
	private boolean hasDirt;
	private boolean traversable;
	private boolean occupied = false;
	private char zoneID;

	public boolean hasDirt() 
	{
		return hasDirt;
	}

	//changeOfDirt as percentage 0-100
	public void setChangeOfDirt(int changeOfDirt) 
	{
		if (!hasDirt)
		{
			if (new Random().nextInt(100000) <= (changeOfDirt - 1))
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
		return !traversable || occupied;
	}

	public WorldCell() 
	{
		super();
		traversable = false;
		zoneID = '0';
	}
	
	public WorldCell(char zone)
	{
		zoneID = zone;
		traversable = zone != '0';
	}

	public void setOccupied(boolean occupied) {
		this.occupied = occupied;
	}

	public boolean isTraversable() {
		return traversable;
	}

	public char getZoneID() {
		return zoneID;
	}
}
