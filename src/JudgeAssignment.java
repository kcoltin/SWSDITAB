//*****************************************************************************
// JudgeAssignment.java
// Kevin Coltin 
//
// Represents a judge who is assigned to a specific round, but without a room 
// or any debaters assigned. 
//*****************************************************************************

 
 

import java.io.Serializable; 
import java.util.ArrayList; 
import java.awt.Color;

class JudgeAssignment implements Flightable, Serializable, JudgeInhabitable
{
	private static final long serialVersionUID = 152412654164316L;

	private ArrayList<Judge> judges; 
	private char flight;
	
	private Round round; 
	
	//Constructors 
	JudgeAssignment (Judge judge, char f, boolean lock, Round r)
	{
		judges = new ArrayList<Judge>();
		judges.add(judge); 
		judge.setLocked(this, lock);
		flight = f;
		round = r; 
	}
	
	JudgeAssignment (ArrayList<Judge> panel, char f, boolean lock, Round r)
	{
		judges = new ArrayList<Judge>();
		for (Judge judge : panel)
		{
			judges.add(judge);
			judge.setLocked(this, lock);
		}
		
		flight = f;
		round = r; 
	}
		
	
	//Note: by default, methods can be used when there's only one judge, as in 
	//prelims, so that's what they assume when you call certain methods like 
	//setJudge. 
	
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
		for (Judge j : judges)
			if (judges.equals(j))
				Sort.remove(judges, judge);
	}
	
	public void removeJudges ()
	{
		judges.clear();
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

	public void setFlight (char f)
	{
		flight = f;
	}
	
	public char getFlight ()
	{
		return flight;
	}
	
	//Equality defined by same judge(s) and same flight 
	public boolean equals (Object another)
	{
		if (another instanceof JudgeAssignment == false)
			return false; 
	
		JudgeAssignment other = (JudgeAssignment) another;
		
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
		
		//Now, it's true iff all judges are equal and it's same flight 
		return nummatches == judges.size() && flight == other.getFlight();
	}
	
	public boolean hasJudge (Judge judge)
	{
		for (Judge j : judges)
			if (j.equals(judge))
				return true; 
		
		return false; 
	}

	
	//Indicates whether the judge is manually locked 
	boolean isLocked()
	{
		return judges.get(0).isLocked(this);
	}
	
	public boolean isJudgeLocked()
	{
		return isLocked();
	}
	
	public boolean isJudgeLocked (Judge judge)
	{
		return judge.isLocked(this); 
	}
		
	
	void setLocked (boolean isLocked)
	{
		judges.get(0).setLocked (this, isLocked);
	}
	
	public void setJudgeLocked (boolean isLocked)
	{
		setLocked (isLocked); 
	}
	
	public void setJudgeLocked (Judge judge, boolean isLocked)
	{
		judge.setLocked (this, isLocked);
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
	
}









