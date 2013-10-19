//******************************************************************************
// RoomInhabitable.java
// Kevin Coltin  
//
// Interface for objects that "have" a room - debates, room assignments, and 
// judgeroomassignments.
//******************************************************************************

 
 

import java.awt.Color; 

interface RoomInhabitable 
{
	void setRoom (Room room);
	Room getRoom ();
	boolean equals (Object other);
	void setRoomLocked (boolean locked); 
	boolean isRoomLocked ();
	Color getRoomColor (); 
}



