//*****************************************************************************
// Prioritizable.java
// Kevin Coltin
//
// Interface used by objects (judges and rooms, as of now) that can be given a
// different priority depending on how important it is that they be used in a
// particular round.   
//*****************************************************************************

 
 

import java.util.ArrayList; 

interface Prioritizable 
{
	//Set and get default priority levels 
	void setDefaultPriority (Priority.PriorityLevel level);
	Priority.PriorityLevel getDefaultPriority ();

	//Set and get priority levels for particular rounds 
	void setPriority (Round round, Priority.PriorityLevel level);
	Priority.PriorityLevel getPriority (Round round);
}
