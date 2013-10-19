//*****************************************************************************
// ElimRound.java
// Kevin Coltin   
//
// An elimination round/outround. 
//*****************************************************************************

 
 

import java.util.ArrayList; 

class ElimRound extends Round 
{
	private static final long serialVersionUID = 1584958490318523L; 
	
	//level of this round- e.g. Semis. If the round is "numbered" (see below),
	//this must be null.
	private Outround level; 
	
	//If this elimination round is scheduled before the user has decided what
	//level to break to, it is first just given an ordinal number, e.g. "1st 
	//outround." Later it will be decided whether it's semis, finals, etc. If 
	//the round is "leveled" (see above), this must be 0. 

	
	//--------------------------------------------------------------------------
	// Constructor for when the specific level to which the tournament will 
	// break is not yet decided 
	//--------------------------------------------------------------------------
	ElimRound (Tournament tournament, TournamentFrame tf, int num)
	{
		super(tournament, tf);

		number = num;
		name = "Elim Round " + number; 
		numJudges = 3;
		sidelocked = false; 
	}

	//--------------------------------------------------------------------------
	// Constructor for when the specific outround level is set 
	//--------------------------------------------------------------------------
	ElimRound (Tournament tournament, TournamentFrame tf, Outround outrd)
	{
		super(tournament, tf);

		level = outrd; 
		number = 0; 
		name = level.toString(); 
		numJudges = 3;
		sidelocked = false; 
	}


	//--------------------------------------------------------------------------
	// Returns the outround level (semis, quarters, etc.).  If the round is 
	// "numbered", this will return null. 
	//--------------------------------------------------------------------------
	Outround getLevel ()
	{
		return level; 
	}

	//Other mutators and accessors 
	int getNumber ()
	{
		return number; //will return null if this is a "leveled" outround 
	}
	
	void setLevel (Outround outround)
	{
		level = outround; 
		name = level.toString(); 
		number = 0; 
	}
	
	void setNumber (int num)
	{
		number = num; 
		name = "Elim Round " + number; 
		level = null; 
	}



	//---------------------------------------------------------------------------
	// Returns all the entries that could be in this outround, in seed order.
	//---------------------------------------------------------------------------
	ArrayList<Entry> getEntries ()
	{	
		return Sort.seed (this); 
	}


	//---------------------------------------------------------------------------
	// Sets it so that the entries in this round are precisely those that could
	// appear in this round. 
	//---------------------------------------------------------------------------
	void resetBreakEntries ()
	{
		ArrayList<Entry> entries = getEntries(); 
		
		//list to hold new entries - need to add them afterwards to avoid 
		//concurrent modification exception 
		ArrayList<Debate> newDebates = new ArrayList<Debate>(); 
		
		for (int i = getDebates().size() - 1; i >= 0; i--)
		{
			Debate debate = getDebates().get(i); 
		
			if (debate.isNonCompeting() == false)
			{
				if (debate.getTeam1() != null 
					&& entries.contains(debate.getTeam1()) == false)
				{
					//Remove the team from the debate container 
					Entry entry = debate.getTeam1();
					roundPanel.removeFromIOP(entry, debate);
					
					//Put the debater into a new "not competing" container 
					Debate nc = new Debate(this); 
					nc.setTeam(entry, 0); 
					nc.setDecision (0, Debate.Outcome.NOT_COMPETING); 
					newDebates.add (nc); 
				}
			
				//Repeat for team 2 
				if (debate.getTeam2() != null 
					&& entries.contains(debate.getTeam2()) == false)
				{
					//Remove the team from the debate container 
					Entry entry = debate.getTeam2();
					roundPanel.removeFromIOP(entry, debate);
					
					//Put the debater into a new "not competing" container 
					Debate nc = new Debate(this); 
					nc.setTeam(entry, 0); 
					nc.setDecision (0, Debate.Outcome.NOT_COMPETING); 
					newDebates.add (nc); 
				}
			}
		}
		
		//Add new noncompeting debates 
		for (Debate nc : newDebates)
			addDebate (nc); 
		
		//If there are any entries not in a debate who shouldn't be in this round,
		//put them in a noncompeting. 
		for (Entry entry : tournament.getEntries())
		{
			if (getDebate(entry) == null && entries.contains(entry) == false)
			{
				//Put the debater into a new "not competing" container 
				Debate nc = new Debate(this); 
				nc.setTeam(entry, 0); 
				nc.setDecision (0, Debate.Outcome.NOT_COMPETING); 
				addDebate (nc); 
			}
		}
		
		
		//Make sure there are no entries that *should* be in this round in a non-
		//competing debate, and if so, remove them from it. 
		for (Debate nc : getNotCompetings())
		{
			if (nc.getTeam1() != null && entries.contains(nc.getTeam1()))
			{
				nc.removeTeam(nc.getTeam1());
				removeDebate(nc); 
				getItemsOnPairings().remove(nc); 
			}
			else if (nc.getTeam2() != null && entries.contains(nc.getTeam2()))
			{
				nc.removeTeam(nc.getTeam2());
				removeDebate(nc); 
				getItemsOnPairings().remove(nc); 
			}
		}
		
	}


}





