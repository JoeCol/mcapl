// ----------------------------------------------------------------------------
// Copyright (C) 2013 Louise A. Dennis, Michael Fisher
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

package ail.syntax;

import java.util.Iterator;
import java.util.List;

import ail.semantics.AILAgent;

/**
 * Represents a logical formula  (a single atom, negation of an atom or a conjunct)
 * that can appear in plan guards and so refers explicitly to beliefs and goals rather
 * than logical predicates.
 * 
 */
public interface GLogicalFormula extends Cloneable, Unifiable {
    /**
     * Checks whether the formula is a
     * logical consequence of the belief base.
     * 
     * Returns an iterator for all unifiers that are consequence.
     */
    public Iterator<Unifier> logicalConsequence(AILAgent ag, Unifier un, List<String> varnames);
    
    /**
     * Clone this Formula
     * @return
     */
    public GLogicalFormula clone();
    
    /* public boolean isGuardAtom();
    
    public boolean isNegatedGuardAtom();
    
    public boolean isConjunct(); */
    
    /**
     * Expresses the logical formula as a term.
     * @return
     */
    // public Term toTerm();
    
    /**
     * Returns a list of Beliefs that appear positively in a formula (can be used potentially for quick filtering of plans).
     * @return
     */
    // public List<LogicalFormula> getPosTerms();
    
    /**
     * Return a list of the conjuncts that make up this logical forumla
     * @return
     */
    // public List<LogicalFormula> conjuncts();
}
