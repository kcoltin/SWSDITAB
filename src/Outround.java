//*****************************************************************************
// Outround.java
// Kevin Coltin
//
// Enumerated class representing the different possible outrounds that a
// debater can reach at a tournament.  An outround is an elimination round,
// for example "finals" or "semifinals."
//*****************************************************************************




enum Outround 
{
	Octos (16), Quarters(8), Semis(4), Finals(2);
	
	private final int numEntries;
	
	//Constructor 
	Outround (int num)
	{
		numEntries = num;
	}
	
	//Accessor 
	int getNumEntries ()
	{
		return numEntries;
	}
	
}
