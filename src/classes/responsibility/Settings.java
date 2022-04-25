package responsibility;

import java.io.Serializable;

public class Settings implements Serializable
{
	private static final long serialVersionUID = -4836286726986083154L;
	String worldFileLocation;
	int heightOfMap;
	int widthOfMap;
	int dirtAppearanceChange;
	
	public Settings()
	{
		heightOfMap = 10;
		widthOfMap = 10;
		dirtAppearanceChange = 6;
	}
	
	public String getWorldFileLocation() {
		return worldFileLocation;
	}

	public void setWorldFileLocation(String worldFileLocation) {
		this.worldFileLocation = worldFileLocation;
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

	public int getDirtAppearanceChange() {
		return dirtAppearanceChange;
	}

	public void setDirtAppearanceChange(int dirtAppearanceChange) {
		this.dirtAppearanceChange = dirtAppearanceChange;
	}

}
