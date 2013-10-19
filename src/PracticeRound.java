//*****************************************************************************
// PracticeRound.java
// Kevin Coltin   
//
// A round which is not judged, in which there is no winner and loser. 
//*****************************************************************************

 
 

class PracticeRound extends Round 
{
	private static final long serialVersionUID = 25243646141613461L; 
	
	//--------------------------------------------------------------------------
	// Constructor 
	//--------------------------------------------------------------------------
	PracticeRound (Tournament tournament, TournamentFrame tf, int num)
	{
		super(tournament, tf);
		
		number = num; 
		name = "Practice Round " + number; 
	}

}
