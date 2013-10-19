//*****************************************************************************
// JudgeRoomAssignment.java
// Kevin Coltin 
//
// Represents a judge who is assigned to a specific room in a certain round, 
// but without any debaters assigned. 
//*****************************************************************************

 
 

import java.io.Serializable;
import java.util.ArrayList; 
import java.awt.Color;

class JudgeRoomAssignment implements Flightable, Serializable, JudgeInhabitable,
									RoomInhabitable
{
	private static final long serialVersionUID = -312526416461417575L; 

	private ArrayList<Judge> judges; 
	private Room room; 
	private char flight; 
	
	private boolean roomLocked; 

	private Round round;
	
	//Constructors 
	JudgeRoomAssignment (Judge judge, Room rm, char f, boolean jl, boolean rl, 
								Round rd)
	{
		judges = new ArrayList<Judge>();
		judges.add(judge); 
		room = rm; 
		flight = f;
		judge.setLocked(this, jl);
		roomLocked = rl; 
		round = rd; 
	}

	JudgeRoomAssignment (ArrayList<Judge> panel, Room rm, char f, boolean jl, 
								boolean rl, Round rd)
	{
		judges = new ArrayList<Judge>();
		for (Judge judge : panel)
		{	
			judges.add(judge);
			judge.setLocked(this, jl);
		}
		
		room = rm; 
		flight = f;
		roomLocked = rl; 
		round = rd; 
	}
	
	
	//Mutators and accessors 
	public void setJudge (Judge judge)
	{
		if (judges.isEmpty())
			judges.add(judge); 
		else
			judges.set(0, judge);
	}
	
	public void addJudge (Judge judge)
	{
		judges.add(judge);
	}
	

	public void removeJudge (Judge judge)
	{
		for (int i = judges.size() - 1; i >= 0; i--)
			if (judge.equals(judges.get(i)))
				judges.remove(i);
	}
	
	public void removeJudges ()
	{
		judges.clear();
	}
	

	public void setRoom (Room r)
	{
		room = r;
	}
	
	public Judge getJudge()
	{
		if (judges.isEmpty())
			return null; 

		return judges.get(0);
	}

	public ArrayList<Judge> getJudges()
	{
		return judges; 
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

	//Equality defined by same judge(s), room, and flight 
	public boolean equals (Object another)
	{
		if (another instanceof JudgeRoomAssignment == false)
			return false; 
	
		JudgeRoomAssignment other = (JudgeRoomAssignment) another;

		if (judges.size() != other.getJudges().size())
			return false; 
		
		int nummatches = 0; //number of judges who match 
		
		for (Judge judge : judges)
		{
			for (Judge otherjudge : other.getJudges())
				if (judge.equals(otherjudge))
				{	
					nummatches++;
					break; //go to next judge in this assignement 
				}
		}
		
		//Now, it's true iff all judges are equal, same room, and same flight 
		return nummatches == judges.size() && room.equals(other.getRoom()) 
				&& flight == other.getFlight();
	}
	
	public boolean hasJudge (Judge judge)
	{
		for (Judge j : judges)
			if (j.equals(judge))
				return true; 
		
		return false; 
	}

	
	//Indicates whether the items are manually locked 
	public boolean isJudgeLocked()
	{
		return judges.get(0).isLocked(this);
	}
	
	public boolean isJudgeLocked (Judge judge)
	{
		return judge.isLocked(this); 
	}
		
	
	public void setJudgeLocked (boolean isLocked)
	{
		judges.get(0).setLocked(this, isLocked); 
	}
	
	public void setJudgeLocked (Judge judge, boolean isLocked)
	{
		judge.setLocked (this, isLocked);
	}	



	public boolean isRoomLocked()
	{
		return roomLocked;
	}
		
	
	public void setRoomLocked (boolean islocked)
	{
		roomLocked = islocked;
	}
	
	
	
	//Methods to get color, based on whether there are conflicts 
	public Color getJudgeColor ()
	{
		return getJudgeColor(judges.get(0));
	}
	
	
	public Color getJudgeColor (Judge judge)
	{
		if (round.hasHappened())
			return Color.BLACK;
		
		//Otherwise, if the round has not yet started: 
		ConflictChecker.setRound(round);
		Conflict[] conflicts = ConflictChecker.checkForConflict(this); 
		
		if (conflicts == null)
			return Color.BLUE; 
		
		for (Conflict conflict : conflicts)
			if (conflict.source == Conflict.JUDGE 
				&& conflict.whichJudge.equals(judge))
				return Color.RED; 
		
		return Color.BLUE;
	}
	
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














