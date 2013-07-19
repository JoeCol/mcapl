// ----------------------------------------------------------------------------
// Copyright (C) 2012 Louise A. Dennis, Michael Fisher 
// 
// This file is part of Gwendolen
// 
// Gwendolen is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
// 
// Gwendolen is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with Gwendolen; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.csc.liv.ac.uk/~lad
//
//----------------------------------------------------------------------------

package gwendolen.easss_tutorial;

import ail.mas.DefaultEnvironment;
import ail.util.AILConfig;
import ail.util.AILexception;
import ail.syntax.Message;
import ail.syntax.Unifier;
import ail.syntax.Action;
import ail.syntax.SendAction;
import ail.syntax.Literal;
import ail.syntax.NumberTermImpl;
import ail.syntax.Predicate;
import ail.semantics.AILAgent;
import ajpf.util.AJPFLogger;

import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import gov.nasa.jpf.annotation.FilterField;
//import gov.nasa.jpf.jvm.abstraction.filter.FilterField;
import gov.nasa.jpf.jvm.Verify;

/**
 * Environment for a Trash Robot Scenario;
 * 
 * @author louiseadennis
 *
 */
public class RobotEnv extends DefaultEnvironment {
//	int casenumber = 1;
	boolean changer = true;
	boolean changel = true;
	boolean changemessage = true;
	@FilterField
	boolean canseehumanr = false;
	@FilterField
	boolean canseehumanl = false;
	@FilterField
	boolean havemessagel = false;
	boolean withlifter = false;
	boolean withlifter2 = false;
	boolean withsearcher = true;
	Literal human;
	Literal clear;
	Random random = new Random();
	
	String logname = "gwendolen.rescue.RobotEnv";
	
	/**
	 * Two performatives, perform and tell.
	 */
	public RobotEnv() {
		super();
		human=new Literal("human");
		clear = new Literal("clear");
	}
			
	public Set<Predicate> getPercepts(String agName, boolean update) {
		Set<Predicate> percepts = new HashSet<Predicate>();
		if (agName.equals("searcher")) {
			if (changer) {
				if (withsearcher) {
					canseehumanr = random.nextBoolean();
				}
			}
			if (canseehumanr) {
				percepts.add(human);
			}
			changer = false;
		
		} else {
			if (changel) {
				if (withlifter2) {
					canseehumanl = random.nextBoolean();
				}
			}
			
			if (canseehumanl) {
				percepts.add(clear);
			}
			changel = false;
			
		}
		
		AJPFLogger.fine(logname, percepts.toString());
		return percepts;
	}
	
/*	public Set<Message> getMessages(String agName) {
		Set<Message> messages = new HashSet<Message>();
		if (agName.equals("agentlifter")) {
			if (changemessage) {
				if ( withlifter || withlifter2) {
					havemessagel = random.nextBoolean();
				}
	
		
				if (havemessagel) {
					Literal humanloc = new Literal("human");
					humanloc.addTerm(new NumberTermImpl(2));
					humanloc.addTerm(new NumberTermImpl(2));
					if (agName.equals("agentlifter")) { 
						Message msg = new Message(1, "r", "agentlifter", humanloc);
						messages.add(msg);
						AJPFLogger.fine(logname, "Adding " + msg);
					}
				}
				changemessage=false;
			}
		}
		return messages;
	} */
	
	/**
	 * When a pickup action is executed the environment stores new perceptions
	 * for the agent - that its picked something up and its hands are now longer
	 * empty.
	 */
   public Unifier executeAction(String agName, Action act) throws AILexception {
	   	Unifier theta = new Unifier();
	   	if (act instanceof SendAction) {
	   		theta = super.executeAction(agName, act);
	   	} else {
	   		AJPFLogger.info("gwendolen.easss_tutorial.RobotEnv", agName + " done " + printAction(act));
	   	}
	   	
	   	change_for(agName);
	   	 
    	return theta;
    }
   
   public void change_for(String name) {
	   if (name.equals("searcher")) {
		   changer = true;
	   } else {
		   changel = true;
		   changemessage = true;
	   }
   }
   
   public boolean nothingPending(String agName) {
	   if (agName.equals("searcher")) {
		   return (!changer); 
	   } else {
		   return (!changel & !changemessage);
	   }
   }

	public void configure(AILConfig configuration) {
		super.configure(configuration);
		
		if (configuration.containsKey("withsearcher")) {
			withsearcher = Boolean.valueOf((String) configuration.get("withsearcher"));
		}
		if (configuration.containsKey("withlifter")) {
			withlifter = Boolean.valueOf((String) configuration.get("withlifter"));
		}
		if (configuration.containsKey("withlifter2")) {
			withlifter2 = Boolean.valueOf((String) configuration.get("withlifter2"));
		}
	} 
}


