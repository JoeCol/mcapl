package responsibility.TheoryImpl;

import java.io.Serializable;

public class Settings implements Serializable
{
	private static final long serialVersionUID = -4836286726986083154L;
	String worldFileLocation;
	int heightOfMap;
	int widthOfMap;
	int simulationSteps;
	int dirtInterval;
	int badDirtInterval;
	
	public Settings()
	{
		heightOfMap = 10;
		widthOfMap = 10;
		simulationSteps = 10000;
		dirtInterval = 15;
		badDirtInterval = 5;
	}
	
	public int getSimulationSteps() {
		return simulationSteps;
	}

	public void setSimulationSteps(int simulationSteps) {
		this.simulationSteps = simulationSteps;
	}

	public int getDirtInterval() {
		return dirtInterval;
	}

	public void setDirtInterval(int dirtInterval) {
		this.dirtInterval = dirtInterval;
	}

	public int getBadDirtInterval() {
		return badDirtInterval;
	}

	public void setBadDirtInterval(int badDirtInterval) {
		this.badDirtInterval = badDirtInterval;
	}

	public int getHeightOfMap() {
		return heightOfMap;
	}

	public void setHeightOfMap(int heightOfMap) {
		this.heightOfMap = heightOfMap;
	}

	public int getWidthOfMap() {
		return widthOfMap;
	}

	public void setWidthOfMap(int widthOfMap) {
		this.widthOfMap = widthOfMap;
	}

	public String getWorldFileLocation() {
		return worldFileLocation;
	}

	public void setWorldFileLocation(String text) {
		this.worldFileLocation = text;
		
	}

}
