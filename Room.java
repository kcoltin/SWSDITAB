//*****************************************************************************
//  Room.java
//  Kevin Coltin
//
// Represents a room in which debate rounds may be held.
//*****************************************************************************

 
 

import java.io.Serializable; 
import java.util.ArrayList; 

class Room implements Comparable, Serializable, Prioritizable 
{
	private static final long SerialVersionUID = 4520843958342095L; 
	private Tournament tournament; 
	
	//name of the room, e.g. "Jun 201." Should be in abbreviated form as it 
	//would appear on postings. 
	private String name; 
	
	//Preference ranking of how much we want to have debates there, mostly 
	//based on how easily accessible and how big/comfortable it is. 
	private RoomRating rating; 
	
	//Possible rankings - A is best, C is default 
	enum RoomRating {A, B, C, D, E};

	//a room's priority level may differ by round and reflects how high a 
	//priority it is that it be used in that round 
	private Priority.PriorityLevel defaultPriority; 
	private ArrayList<Priority> roundPriorities; 
	
	//if the rooms is a dorm room, this is the list of students who live there
	//and therefore should debate in it so they can unlock it
	private ArrayList<Competitor> residents; 
	
	//--------------------------------------------------------------------------
	// Constructor
	//--------------------------------------------------------------------------
	Room (String n, Tournament t)
	{
		tournament = t; 
		name = n; 
		rating = RoomRating.C; 
		defaultPriority = Priority.PriorityLevel.Normal; 
		roundPriorities = new ArrayList<Priority>();
		residents = new ArrayList<Competitor>();
	}

	
	
	//--------------------------------------------------------------------------
	// Mutator and accessor methods 
	//--------------------------------------------------------------------------
	RoomRating getRating ()
	{
		return rating; 
	}
	
	String getName()
	{
		return name; 
	}

	String getNameNoSpaces ()
	{
		return name.replace(' ', '_');
	}

	public String toString ()
	{
		return name; 
	}

	public Priority.PriorityLevel getDefaultPriority ()
	{
		return defaultPriority; 
	}
	
	void setRating (RoomRating r)
	{
		rating = r;
	}
	
	void setName (String n)
	{
		name = n;
	}

	public void setDefaultPriority (Priority.PriorityLevel level)
	{
		//For all rounds that have already happened for which the room had 
		//(its original) default priority, create a new Priority object to
		//preserve it. 
		for (Round round : tournament.getRounds())
		{
			if (round.hasHappened() && getPriorityObject(round) == null)
				roundPriorities.add (new Priority(round, defaultPriority));
		}			
		
		//Set new default priority level
		defaultPriority = level; 
		
		//Delete any preexisting Priority objects that had the new default
		//priority 
		for (int i = 0; i < roundPriorities.size(); i++) 
		{
			if (roundPriorities.get(i).getPriority().equals(defaultPriority))
				roundPriorities.remove(i); 
		}	
	}


	//--------------------------------------------------------------------------
	// Gives the room a priority level for a particular round that is different
	// from its default priority level. 
	//--------------------------------------------------------------------------
	public void setPriority (Round round, Priority.PriorityLevel level)
	{
		//If it's default - eliminate the room's priority for this round if it 
		//has one; do nothing if it doesn't 
		if (level.equals(defaultPriority))
		{
			if (hasPriorityObject(round))
				removeRoundPriority(round); 
		}		
		
		//If it's not default, reset the room's priority for this round if it 
		//has one; create one if it doesn't 			
		else 
		{
			if (hasPriorityObject(round))
				getPriorityObject(round).setPriority(level);
			else 
				roundPriorities.add (new Priority(round, level));
		}
	}
	

	//--------------------------------------------------------------------------
	// Returns the room's priority level for a given round. 
	//--------------------------------------------------------------------------
	public Priority.PriorityLevel getPriority (Round round)
	{
		//If the room has a priority level for this round, return that; return default 
		//priority if not 
		if (hasPriorityObject(round))
			return getPriorityObject(round).getPriority(); 
		else
			return defaultPriority; 		
	}
	


	//--------------------------------------------------------------------------
	// Internal method used by setPriority and getPriority to retrieve the 
	// Priority object for a given round if the room has one. If the room does
	// not have one, returns "null". 
	//--------------------------------------------------------------------------
	private Priority getPriorityObject (Round round)
	{
		for (Priority p : roundPriorities)
		{
			if (p.getRound() != null && round.equals(p.getRound()))
				return p; 
		}
		
		return null; 
	}
	
	//--------------------------------------------------------------------------
	// Internal method used by setPriority and getPriority to check whether the
	// room has a Priority object for a given round. 
	//--------------------------------------------------------------------------
	private boolean hasPriorityObject (Round round)
	{
		boolean has = false; 
	
		for (Priority p : roundPriorities)
		{
			if (p.getRound() != null && round.equals(p.getRound()))
			{
				has = true; 
				break;
			}
		}
		
		return has; 
	}


	//--------------------------------------------------------------------------
	// Internal method used to remove a priority from the array list  
	//--------------------------------------------------------------------------
	private void removeRoundPriority (Round round)
	{
		for (int i = 0; i < roundPriorities.size(); i++)
		{
			if (roundPriorities.get(i).getRound() != null  
					&& round.equals(roundPriorities.get(i).getRound()))
				roundPriorities.remove(i);
		}
	}


	//--------------------------------------------------------------------------
	// Methods for residents array 
	//--------------------------------------------------------------------------
	ArrayList<Competitor> getResidents ()
	{
		return residents; 
	}
	
	void addResident (Competitor res)
	{
		residents.add(res); 
		Sort.sort(residents);
	}
	
	void removeResident (Competitor res)
	{
		Sort.remove(residents, res); 
	}
	
	void setResidents (ArrayList<Competitor> roommates)
	{
		residents = roommates; 
	}
	
	boolean hasResident (Competitor res)
	{
		boolean has = false; 
		
		for (Competitor competitor : residents)
			if (res.equals(competitor))
			{
				has = true;
				break;
			}
		
		return has; 
	}


	//--------------------------------------------------------------------------
	// Equality - defined by name
	//--------------------------------------------------------------------------
	public boolean equals (Object another)
	{
		if (another instanceof Room == false)
			return false; 
	
		Room other = (Room) another; 
		return name.equals(other.getName());
	}
	
	//--------------------------------------------------------------------------
	// Comparison based first on desirability (better first) and then on name
	//--------------------------------------------------------------------------
	public int compareTo (Object another)
	{
		Room other = (Room) another;
		int comp = rating.ordinal() - other.getRating().ordinal();
		
		if (comp != 0)
			return comp; 
		else
			return name.compareTo(other.getName());
	}


}





