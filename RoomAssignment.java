//*****************************************************************************
// RoomAssignment.java
// Kevin Coltin 
//
// Represents a room that is set to be used in a round, but without a judge or
// any debaters assigned. 
//*****************************************************************************

 
 

import java.io.Serializable; 
import java.awt.Color;

class RoomAssignment implements Flightable, Serializable, RoomInhabitable
{
	private static final long serialVersionUID = 19025203519L; 

	private Room room; 
	private char flight; 
	
	private Round round; 
	private boolean locked; 
	
	//Constructor
	RoomAssignment (Room rm, char flt, boolean islocked, Round rd)
	{
		room = rm; 
		flight = flt;
		locked = islocked; 
		round = rd;
	}

	
	//Mutators and accessors 
	public void setRoom (Room r)
	{
		room = r;
	}
	
	public Room getRoom()
	{
		return room;
	}
	
	public void setFlight (char f)
	{
		flight = f;
	}
	
	public char getFlight ()
	{
		return flight;
	}

	//Equality defined by same room and same flight 
	public boolean equals (Object another)
	{
		if (another instanceof RoomAssignment == false)
			return false; 
	
		RoomAssignment other = (RoomAssignment) another;
		return room.equals(other.getRoom()) && flight == other.getFlight();
	}
	
	//Indicates whether the room is manually locked 
	boolean isLocked()
	{
		return locked;
	}

	public boolean isRoomLocked()
	{
		return locked;
	}
		
	
	void setLocked (boolean islocked)
	{
		locked = islocked;
	}
	
	public void setRoomLocked (boolean isLocked)
	{
		locked = isLocked; 
	}
	
	
	//Indicates color that the room's label should be
	public Color getRoomColor ()
	{
		if (round.hasHappened())
			return Color.BLACK;
		
		//Otherwise, if the round has not yet started: 
		ConflictChecker.setRound(round);
		Conflict[] conflicts = ConflictChecker.checkForConflict(this); 
		
		if (conflicts == null)
			return Color.BLUE; 
		
		for (Conflict conflict : conflicts)
			if (conflict.source == Conflict.ROOM)
				return Color.RED; 
		
		return Color.BLUE;
	}
	
	
}












