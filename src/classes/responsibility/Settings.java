package responsibility;

import java.util.HashMap;

public class Settings 
{
	HashMap<String,String> settingsList = new HashMap<String, String>();
	int numberOfAgents;
	int heightOfMap;
	int widthOfMap;
	int dirtAppearanceChange;
	
	public Settings()
	{
		numberOfAgents = 1;
		heightOfMap = 8;
		widthOfMap = 8;
		dirtAppearanceChange = 6;
	}

	public int getWidth() 
	{
		return widthOfMap;
	}

	public int getHeight() 
	{
		return heightOfMap;
	}

	public int getChangeOfDirt() 
	{
		return dirtAppearanceChange;
	}
}
