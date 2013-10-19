//*****************************************************************************
// BlankLine.java
// Kevin Coltin 
//
// This is simply a dummy class that exists as a placeholder on the RoundPanel
// for any lines that do not contain a judge, room, or either team. It can be
// used by the SmartLabel as something to refer to when items are swapped or
// removed. 
//*****************************************************************************

 
 

import java.io.Serializable;

class BlankLine implements Flightable, Serializable 
{
	private static final long serialVersionUID = -12501269468401L; 

	private char flight; 
	
	//Constructor
	BlankLine (char flt)
	{
		flight = flt;
	}
	
	public void setFlight (char f)
	{
		flight = f;
	}
	
	public char getFlight ()
	{
		return flight;
	}
	
	//Equality iff they refer to the same object 
	public boolean equals (Object other)
	{
		return other instanceof BlankLine && this == (BlankLine) other;
	}
	
}
