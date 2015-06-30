// ----------------------------------------------------------------------------
// Copyright (C) 2012 Louise A. Dennis, and  Michael Fisher 
//
// This file is part of the Engineering Autonomous Space Software (EASS) Library.
// 
// The EASS Library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
// 
// The EASS Library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with the EASS Library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.csc.liv.ac.uk/~lad
//
//----------------------------------------------------------------------------

package eass.mas.nxt;

import lejos.robotics.navigation.DifferentialPilot;

import java.util.ArrayList;

/**
 * An interface for a Lego Robot to be used in an EASS environment.
 * @author louiseadennis
 *
 */
public interface LegoRobot {
	/**
	 * Get the brick associated with this robot.
	 * @return
	 */
	public NXTBrick getBrick();
	
	/**
	 * Does this robot have a pilot?
	 * @return
	 */
	public boolean hasPilot();
	
	/**
	 * Set the pilot for this robot.
	 * @param pilot
	 */
	public void setPilot(DifferentialPilot pilot);
	
	/**
	 * Get the pilot for this robot.
	 * @return
	 */
	public DifferentialPilot getPilot();
	
	/**
	 * Attach a sensor to a port.
	 * @param portnumber
	 * @param sensor
	 */
	public void setSensor(int portnumber, EASSSensor sensor);
	
	/**
	 * Get this robot's sensors.
	 * @return
	 */
	public ArrayList<EASSSensor> getSensors();
	
	/**
	 * Add percepts from an EASS environement.
	 * @param env
	 */
	public void addPercepts(EASSNXTEnvironment env);
	
	/**
	 * Is this class connected to an actual robot?
	 * @return
	 */
	public boolean isConnected();
	
	/**
	 * Close the connection to the physical robot.
	 */
	public void close();
}