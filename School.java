//*****************************************************************************
// School.java
// Kevin Coltin 
// 
// Represents a single team/school at a tournament
//*****************************************************************************

 
 

import java.io.Serializable;
import java.util.ArrayList; 

class School implements Comparable, Serializable 
{
	private static final long serialVersionUID = 58197403274893L;

	private String name; //name of the school

	//--------------------------------------------------------------------------
	// Constructor
	//--------------------------------------------------------------------------
	School (String n)
	{
		name = n;
	}


	//--------------------------------------------------------------------------
	// Get and set name
	//--------------------------------------------------------------------------
	String getName()
	{
		return name;
	}
	
	String getNameNoSpaces ()
	{
		return name.replace(' ', '_');
	}

	public String toString()
	{
		return name;
	}


	void setName (String n)
	{
		name = n;
	}


	//--------------------------------------------------------------------------
	// Test equality or compare schools - based on alphabetical order
	//--------------------------------------------------------------------------
	public boolean equals (Object another)
	{
		//this first part is necessary to avoid errors on one of the GUI screens
		if (another instanceof School == false)
			return false; 
	
		School other = (School) another; 
		return name.equals(other.getName());
	}
	
	public int compareTo (Object another)
	{
		School other = (School) another; 
		return name.compareTo(other.getName());
	}



}



/* Additional methods - these might be restored at a later version of this class,
if it's updated to include all the students who go to that school. 


	//--------------------------------------------------------------------------
	// Operations for adding, subtracting, etc., kids
	//--------------------------------------------------------------------------
	void addCompetitor (Competitor competitor)
	{
		roster.add(competitor);
		Sort.sort(roster);
	}

	void removeCompetitor (Competitor competitor)
	{
		int i = 0; 
		
		while (i < roster.size())
		{
			if (roster.get(i).equals(competitor))
				roster.remove(i);
			
			i++;
		}
	}
	
*/




