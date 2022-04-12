package responsibility;

import java.util.Random;

public class WorldCell
{
	private boolean hasDirt;

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
}
