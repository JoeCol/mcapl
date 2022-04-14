package responsibility;

import java.util.HashMap;

public class Settings
{
	HashMap<String,String> settingsList = new HashMap<String, String>();
	String worldFileLocation;
	int numberOfAgents;
	int heightOfMap;
	int widthOfMap;
	int dirtAppearanceChange;
	
	public Settings()
	{
		numberOfAgents = 1;
		heightOfMap = 10;
		widthOfMap = 10;
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
