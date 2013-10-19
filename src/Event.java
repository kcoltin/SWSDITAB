//*****************************************************************************
// Event.java
// Kevin Coltin
// 
// Represents a single forensics event - the only events supported are LD and 
// PFD.
//*****************************************************************************

 
 

enum Event 
{
	LD ("LD Debate", 1), PFD ("Public Forum Debate", 2);


	private final String name; 
	private final int kidsPerTeam; //maximum competitors allowed on a single 
											//debate team 
											
	//--------------------------------------------------------------------------
	// Constructor
	//--------------------------------------------------------------------------
	Event (String n, int kids)
	{
		name = n;
		kidsPerTeam = kids;
	}
	
	
	//--------------------------------------------------------------------------
	// Accessor methods
	//--------------------------------------------------------------------------
	String getName ()
	{
		return name; 
	}
	
	int getKidsPerTeam ()
	{
		return kidsPerTeam;
	}


}


