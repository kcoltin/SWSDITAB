//*****************************************************************************
// Flightable.java
// Kevin Coltin 
//
// Interface for items, such as debates, judge assignments, etc., that go on 
// the pairings sheet and RoundPanel screen and must be assigned to one of two
// flights. 




interface Flightable 
{
	void setFlight (char flight);
	char getFlight ();	
}
