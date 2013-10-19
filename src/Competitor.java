//*****************************************************************************
// Competitor.java
// Kevin Coltin 
// 
// Represents an individual student at a tournament
//*****************************************************************************

 
 

import java.io.Serializable;

class Competitor implements Serializable, Comparable 
{
	private static final long serialVersionUID = 7849578342523453454L;

	private String firstName, lastName; 
	private School school; 

	//number representing student's lab group - by convention, 1 should be the 
	//best group, 2 the second best, and so on. Zero means a lab has not been 
	//assigned. 
	private int lab; 
	
	//--------------------------------------------------------------------------
	// Constructor 
	//--------------------------------------------------------------------------
	Competitor (String first, String last, School hs)
	{
		firstName = first; 
		lastName = last; 
		school = hs; 
		lab = 0; 
	}

	Competitor (String first, String last, School hs, int group)
	{
		firstName = first; 
		lastName = last; 
		school = hs; 
		lab = group; 
	}


	//--------------------------------------------------------------------------
	// Mutator and accessor methods
	//--------------------------------------------------------------------------
	void setName (String first, String last)
	{
		firstName = first; 
		lastName = last; 
	}
	
	void setSchool (School hs)
	{
		school = hs; 
	}
	
	void setLab (int group)
	{
		lab = group;
	}
	
	String getFirstName()
	{
		return firstName;
	}
	
	String getLastName()
	{
		return lastName;
	}
	
	String getName()
	{
		return firstName + " " + lastName;
	}
	
	String getFirstNameNoSpaces ()
	{
		return firstName.replace(' ', '_');
	}

	String getLastNameNoSpaces ()
	{
		return lastName.replace(' ', '_');
	}
	
	School getSchool()
	{
		return school;
	}
	
	int getLab()
	{
		return lab;
	}


	//--------------------------------------------------------------------------
	// Test for equality - defined as having the same name 
	//--------------------------------------------------------------------------
	public boolean equals (Object another)
	{
		Competitor other = (Competitor) another; 
		return firstName.equals(other.getFirstName()) 
					&& lastName.equals(other.getLastName());
	}


	//--------------------------------------------------------------------------
	// Compare, based on comparing last and then first names alphabetically 
	//--------------------------------------------------------------------------
	public int compareTo (Object another)
	{
		Competitor other = (Competitor) another; 

		int comp = lastName.compareTo(other.getLastName());
		
		if (comp != 0)
			return comp; 
		else
			return firstName.compareTo(other.getFirstName());
	
	}
	
	
}




