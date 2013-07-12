// ----------------------------------------------------------------------------
// Copyright (C) 2008-2012 Louise A. Dennis, Berndt Farwer, Michael Fisher and 
// Rafael H. Bordini.
// 
// This file is part of GOAL (AIL version) - GOAL-AIL
//
// GOAL-AIL is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
// 
// GOAL-AIL is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with GOAL-AIL if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.csc.liv.ac.uk/~lad
//----------------------------------------------------------------------------

package goal;

import ajpf.MCAPLcontroller;
import ajpf.util.AJPFException;
import ajpf.util.AJPFLogger;
import ail.mas.ActionScheduler;
import ail.mas.MAS;
import goal.mas.GoalEnvironment;


/**
 * Example Set up for running the pickup agent.
 * 
 * @author louiseadennis
 *
 */
public abstract class GOALMAS {
	MCAPLcontroller mccontrol;

	public GOALMAS(String[] args) {
		String filename = args[0];
		String abs_filename = null;
		try {
			abs_filename = MCAPLcontroller.getFilename(filename);
		} catch (AJPFException e) {
			AJPFLogger.severe("goal.GOALMAS", e.getMessage());
			System.exit(1);
		}

		String propertystring = getProperty(0);
		if (args.length == 2) {
			propertystring = getProperty(Integer.parseInt(args[1]));
		}
		
		MAS mas = (new GOALMASBuilder(abs_filename, true)).getMAS();
		GoalEnvironment env = new GoalEnvironment();
		ActionScheduler s = new ActionScheduler();
		env.setScheduler(s);
		env.addPerceptListener(s);
		mas.setEnv(env);
			
			// Set up a MCAPL controller and specification.
		mccontrol = new MCAPLcontroller(mas, propertystring, 1);
			// Start the system.
		
	}
		
	public void runexample() {
			// Start the system.
		mccontrol.begin(); 
           
 
	}
	
	public abstract String getProperty(int i);

}
