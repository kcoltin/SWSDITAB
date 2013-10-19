//*****************************************************************************
// Priority.java
// Kevin Coltin  
//
// An object of this class represents a "priority assignment" for a judge or
// in a particular round. E.g., it could represent that judge John Doe has high
// priority to be given a ballot in round 3 or that room Juniper 101 has high
// priority to be used in round 3. Each of these objects is contained by the 
// Prioritizable object representing the judge or room in question. 
//*****************************************************************************

 
 

import java.io.Serializable; 

class Priority implements Serializable
{
	private static final long serialVersionUID = -13259281594552364L; 

	//Rating of levels of importance for a particular judge or room to be used
	//in a round
	enum PriorityLevel {High, Normal, Low, Unavailable}; 
	
	private Round round; 
	private PriorityLevel priority; 
	
	//--------------------------------------------------------------------------
	// Constructor 
	//--------------------------------------------------------------------------
	Priority (Round r, PriorityLevel p)
	{
		round = r; 
		priority = p; 
	}

	//--------------------------------------------------------------------------
	// Mutator and accessor methods 
	//--------------------------------------------------------------------------
	void setPriority (PriorityLevel p)
	{
		priority = p; 
	}
	
	Round getRound () //When calling this, always check that round != null
	{
		return round; 
	}
	
	PriorityLevel getPriority ()
	{
		return priority; 
	}	


	//--------------------------------------------------------------------------
	// Equality defined by having the same round (since each judge/room should 
	// only have one priority per round of course). This should only be called 
	// with the Prioritizable object as given of course, since Priority does not
	// "know" its judge/room. 
	//--------------------------------------------------------------------------
	public boolean equals (Object another)
	{
		Priority other = (Priority) another; 
		return round.equals (other.getRound()); 
	}


}







