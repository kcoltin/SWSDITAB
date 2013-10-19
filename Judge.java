//*****************************************************************************
// Judge.java
// Kevin Coltin 
// 
// Represents a judge at a tournament
//*****************************************************************************

 
 

import java.io.Serializable;
import java.util.ArrayList; 

class Judge implements Comparable, Serializable, Prioritizable 
{
	private static final long serialVersionUID = 5974891578917389L;
	private Tournament tournament; 
	
	private String name; //last names only 

	//a judge's priority level may differ by round and reflects how high a 
	//priority it is that they get a ballot in that round 
	private Priority.PriorityLevel defaultPriority; 
	private ArrayList<Priority> roundPriorities; 

	private ArrayList<School> schoolStrikes; //schools they cannot judge
	private ArrayList<Competitor> studentStrikes; //students they can't judge 
																//(besides those at schools 
																//they're struck against) 
	
	private ArrayList<JudgeInhabitable> locks; //objects in which this judge is 
														//locked. 

	
	//--------------------------------------------------------------------------
	// Constructor 
	//--------------------------------------------------------------------------
	Judge (String n, Tournament t)
	{
		tournament = t; 
		name = n;
		defaultPriority = Priority.PriorityLevel.Normal; 
		
		roundPriorities = new ArrayList<Priority>(); 
		schoolStrikes = new ArrayList<School>();
		studentStrikes = new ArrayList<Competitor>();
		locks = new ArrayList<JudgeInhabitable>(); 
	}


	//--------------------------------------------------------------------------
	// Mutator and accessor methods
	//--------------------------------------------------------------------------
	void setName (String n)
	{
		name = n; 
	}
	
	
	public void setDefaultPriority (Priority.PriorityLevel level)
	{
		//For all rounds that have already happened for which the judge had 
		//(their original) default priority, create a new Priority object to
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

	
	String getName ()
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
	
	

	//--------------------------------------------------------------------------
	// Test equality or compare judges - based on alphabetical order
	//--------------------------------------------------------------------------
	public boolean equals (Object another)
	{
		Judge other = (Judge) another; 
		return name.equals(other.getName());
	}
	
	public int compareTo (Object another)
	{
		Judge other = (Judge) another; 
		return name.compareTo(other.getName());
	}
	

	//--------------------------------------------------------------------------
	// Returns all rooms the judge has judged in - only counts rounds that have 
	// started. 
	//--------------------------------------------------------------------------
	ArrayList<Room> getRoomsJudged ()
	{
		ArrayList<Room> rooms = new ArrayList<Room>();
	
		for (Round round : tournament.getRounds())
		{
			if (round.hasHappened() == false)
				continue;
			
			Debate debate = round.getDebate(this, 'A');
			if (debate != null && debate.getRoom() != null)
				rooms.add(debate.getRoom());
			debate = round.getDebate(this, 'B');
			if (debate != null && debate.getRoom() != null)
				rooms.add(debate.getRoom());
		}
		
		return rooms;		
	}


	//--------------------------------------------------------------------------
	// Returns all debates the judge has judged in - only counts debates that 
	// have started. 
	//--------------------------------------------------------------------------
	ArrayList<Debate> getDebates ()
	{
		ArrayList<Debate> debates = new ArrayList<Debate>();
	
		for (Round round : tournament.getRounds())
		{
			if (round.hasHappened() == false)
				continue;
			
			Debate debate = round.getDebate(this, 'A');
			if (debate != null)
				debates.add(debate);
			debate = round.getDebate(this, 'B');
			if (debate != null)
				debates.add(debate);
		}
		
		return debates;		
	}
	


	
	//--------------------------------------------------------------------------
	// Returns the average desirability of the classrooms the judge has had to 
	// judge in.  This is calculated as an integer that is just the average, 
	// where A is 5, B is 4, etc. Higher results mean the judge has gotten to 
	// judge in better locations (and is due to judge in a worse one). 
	//--------------------------------------------------------------------------
	int getRoomsDesirability ()
	{
		int desirability = 0; 
	
		for (Room room : getRoomsJudged())
			desirability += 5 - room.getRating().ordinal();
		
		if (getRoomsJudged().isEmpty() == false) //make sure not dividing by zero
			desirability /= getRoomsJudged().size(); //divide to get average

		return desirability; 
	}


	//--------------------------------------------------------------------------
	// Gives the judge a priority level for a particular round that is different
	// from their default priority level. 
	//--------------------------------------------------------------------------
	public void setPriority (Round round, Priority.PriorityLevel level)
	{
		//If it's default - eliminate the judge's priority for this round if they 
		//have one; do nothing if they don't 
		if (level.equals(defaultPriority))
		{
			if (hasPriorityObject(round))
				removeRoundPriority(round); 
		}		
		
		//If it's not default, reset the judge's priority for this round if they 
		//have one; create one if they don't 			
		else 
		{
			if (hasPriorityObject(round))
				getPriorityObject(round).setPriority(level);
			else 
				roundPriorities.add (new Priority(round, level));
		}
	}
	

	//--------------------------------------------------------------------------
	// Returns the judge's priority level for a given round. 
	//--------------------------------------------------------------------------
	public Priority.PriorityLevel getPriority (Round round)
	{
		//If the judge has a priority level for this round, return that; return default 
		//priority if not 
		if (hasPriorityObject(round))
			return getPriorityObject(round).getPriority(); 
		else
			return defaultPriority; 		
	}
	


	//--------------------------------------------------------------------------
	// Internal method used by setPriority and getPriority to retrieve the 
	// Priority object for a given round if the judge has one. If the judge does
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
	// judge has a Priority object for a given round. 
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
	// Indicates whether the judge is struck against a given school/student 
	//--------------------------------------------------------------------------
	boolean isStruck (School school)
	{
		boolean struck = false; 
		
		for (School skl : schoolStrikes)
			if (school.equals(skl))
			{
				struck = true; 
				break;
			}
		
		return struck; 
	}

	boolean isStruck (Competitor student)
	{
		boolean struck = false; 
		
		for (Competitor stdnt : studentStrikes)
			if (student.equals(stdnt))
				struck = true; 
		
		return struck; 
	}
	
	


	//--------------------------------------------------------------------------
	// Locks or unlocks the judge for a particular JudgeInhabitable 
	//--------------------------------------------------------------------------
	void setLocked (JudgeInhabitable container, boolean locked)
	{
		if (locked == false)
		{
			if (isLocked (container))
				removeLock (container);
		}
		else
		{
			if (isLocked (container))
				return; 			
			else
				locks.add (container); 
		}
	}
	

	//--------------------------------------------------------------------------
	// Returns whether the judge is locked in a particular container. 
	//--------------------------------------------------------------------------
	boolean isLocked (JudgeInhabitable container)
	{
		for (JudgeInhabitable lock : locks)
			if (lock.equals(container))
				return true;
		
		return false; 
	}


	
	//--------------------------------------------------------------------------
	// Methods to add or remove from all ArrayLists  
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
	
	void addStrike (School school)
	{
		schoolStrikes.add(school); 
		Sort.sort(schoolStrikes);
	}
	
	void removeStrike (School school)
	{
		Sort.remove(schoolStrikes, school);
	}
	
	void setSchoolStrikes (ArrayList<School> schools)
	{
		schoolStrikes = schools; 
	}

	void addStrike (Competitor competitor) 
	{
		studentStrikes.add(competitor);
		Sort.sort(studentStrikes);
	}

	void removeStrike (Competitor competitor)
	{
		Sort.remove(studentStrikes, competitor);
	}
	
	void setStudentStrikes (ArrayList<Competitor> competitors)
	{
		studentStrikes = competitors; 
	}

	void addLock (JudgeInhabitable lock)
	{
		locks.add(lock); 
		Sort.sort(locks);
	}
	
	void removeLock (JudgeInhabitable lock)
	{
		Sort.remove(locks, lock);
	}
	

	//--------------------------------------------------------------------------
	// Accessor methods to get all array lists
	//--------------------------------------------------------------------------
	ArrayList<School> getSchoolStrikes()
	{
		return schoolStrikes;
	}
	
	ArrayList<Competitor> getStudentStrikes()
	{
		return studentStrikes;
	}

	ArrayList<JudgeInhabitable> getLocks()
	{
		return locks;
	}


}









