//*****************************************************************************
// PrelimRound.java
// Kevin Coltin   
//
// A preliminary round which is judged, with a winner and a loser (as opposed 
// to a practice round). 
//*****************************************************************************

 
 

class PrelimRound extends Round 
{
	private static final long serialVersionUID = 151748597485435324L; 
	
	//--------------------------------------------------------------------------
	// Constructor 
	//--------------------------------------------------------------------------
	PrelimRound (Tournament tournament, TournamentFrame tf, int num)
	{
		super(tournament, tf);
		
		number = num; 
		name = "Round " + number; 
	}

}
