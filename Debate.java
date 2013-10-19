//*****************************************************************************
// Debate.java
// Kevin Coltin 
// 
// Represents a single debate at a tournament. This is a parent class for the 
// classes used to represent more specific types of debate. 
//*****************************************************************************

 
 

import java.io.Serializable;
import java.util.ArrayList; 
import java.awt.Color;

class Debate implements Serializable, Flightable, JudgeInhabitable, 
						RoomInhabitable
{
	private static final long serialVersionUID = 5195109583562564524L;

	protected Round round; //the round this debate is in

	//This is an array of size 2. teams[0] is defined as the entry that appears
	//on the lefthand side of pairings screen, and team[1] is on the right. In
	//practice rounds or prelims, the lefthand one is automatically Aff (iff 
	//it's a real debate). In a non-side-locked elim round, after the ballot 
	//is entered teams[0] will become whoever was aff and teams[1] will become
	//whoever was neg. If it's a pseudo debate, teams[0] is the only team. 
	protected Entry[] teams; 
	
	//Indicates whether the sides have been set in an Elim Round. 
	protected boolean specialSidelocked; 
	
	protected ArrayList<Judge> panel;
	protected Room room;
	
	protected char flight; //flight A or B. (If single flighted, all are A.)

	//Decision that each debater received - win, loss, etc., and enum class to
	//represent possible decisions. The difference between no decision and not 
	//competing is that no decision is if it is an actual debate that either has
	//not yet finished or was a practice debate without a winner or loser. Not
	//competing is for if there is a debater or team who is just not 
	//participating in that particular round but is not receiving a win/loss as
	//a bye or forfeit would imply. 
	//decision[i] contains the decision for teams[i]. 
	protected Outcome[] decisions; 
	
	enum Outcome {WIN("Win", "W"), LOSS("Loss", "L"), BYE("Bye", "B"), 
					FORFEIT("Forfeit", "F"), NO_DECISION("No decision", "-"), 
					NOT_COMPETING("Not competing", "-");  
					
					private String asString;
					private String abbrv; 
					
					//Constructor
					Outcome(String as, String ab)
					{
						asString = as; 
						abbrv = ab;
					}
					
					public String toString ()
					{
						return asString;
					}
					
					String toStringNoSpaces ()
					{
						return asString.replace(' ', '_');
					}
					
					String toShortString ()
					{
						return abbrv;
					}
	}
	
	//These indicated whether the user has manually locked any of these 
	//attributes. teamsLocked[i] indicates whether teams[i] is locked. 
	protected boolean[] teamsLocked;
	protected boolean roomLocked; 
	
	//Constants for backup use 
	private static final String TEAM1 = "%A", TEAM2 = "%B", JUDGE_START = "{", 
										JUDGE_END = "}", ROOM = "%C", FLIGHT = "%D", 
										DECISION1 = "%E", DECISION2 = "%F"; 
	
	//--------------------------------------------------------------------------
	// Constructor
	//--------------------------------------------------------------------------
	Debate (Round r)
	{
		round = r; 
		teams = new Entry[2]; 
		specialSidelocked = false; 
		
		panel = new ArrayList<Judge>();
		flight = 'A';
		
		decisions = new Outcome[2];
		decisions[0] = Outcome.NO_DECISION;
		decisions[1] = Outcome.NO_DECISION;
		
		teamsLocked = new boolean[2];
		teamsLocked[0] = false; 
		teamsLocked[1] = false; 
		roomLocked = false; 
	}



	//--------------------------------------------------------------------------
	// Set debaters. The argument "which" should be either 0 or 1, indicating 
	// whether it's the debater on the left or right. 
	//--------------------------------------------------------------------------
	void setTeam (Entry team, int which) 
	{
		teams[which] = team; 
	}
	
	void clearTeams()
	{
		teams[0] = null;
		teams[1] = null; 
	}
	
	void removeTeam(Entry team)
	{
		if (team.equals(teams[0]))
			teams[0] = null; 
		if (team.equals(teams[1]))
			teams[1] = null; 
	}
	
	void removeTeam (int which)
	{
		teams[which] = null; 
	}



	//--------------------------------------------------------------------------
	// Set sides - note that these can be called only after the debater whose
	// side is being set has already been addded to the debate; they don't
	// automatically add the debater, only set the side of who's already been
	// added. 
	//
	// Note: These should be called to set the sides when entering ballots for 
	// an elim round. 
	//--------------------------------------------------------------------------
	void setAff (Entry aff)
	{
		//This method only does anything if "aff" is currently the team in 
		//teams[1]. Otherwise, the team is either not yet in the debate, or is
		//already aff. 
		if (teams[1] != null && aff.equals(teams[1]))
		{
			Entry neg = teams[0];
			teams[0] = aff;
			teams[1] = neg;
		}
	}
		
	
	void setNeg (Entry neg)
	{
		if (teams[0] != null && neg.equals(teams[0]))
		{
			Entry aff = teams[1]; 
			teams[1] = neg;
			teams[0] = aff;
		}
	}
	
	

	//--------------------------------------------------------------------------
	// Set and get whether round is specially side locked
	//--------------------------------------------------------------------------
	boolean isSpecialSidelocked ()
	{
		return specialSidelocked;
	}
	
	void setSpecialSidelocked (boolean locked)
	{
		specialSidelocked = locked; 
	}
	
	//Checks whether the round is sidelocked for either reason (specially, or 
	//because it's a prelim/practice i.e. not an outround)
	boolean isSidelocked ()
	{
		return specialSidelocked || !(round instanceof ElimRound);
	}


	//Checks whether the side is set - either because it's a prelim/practice, a
	//specialsidelocked elim debate, or a flip-for-sides elim where the ballot
	//has already been entered. 
	boolean isSideSet ()
	{
		return isSidelocked() || isBallotEntered();
	}


	//--------------------------------------------------------------------------
	// Methods to set judge(s)
	//--------------------------------------------------------------------------
	public void addJudge (Judge judge)
	{
		panel.add(judge);
		Sort.sort(panel);
	}
	
	public void removeJudges ()
	{
		panel.clear();
	}
	
	public void removeJudge (Judge judge)
	{
		Sort.remove(panel, judge);
	}

	//Sets the FIRST judge in the panel to "judge". 
	public void setJudge (Judge judge)
	{
		if (panel.isEmpty())
			panel.add(judge); 
		else
			panel.set(0, judge);
	}
	
	//This shouldn't be called if there is more than one judge, but in that
	//case the least bad solution is to just return the first one. 
	public Judge getJudge () 
	{
		if (panel.size() == 0)
			return null; 
		else 
			return panel.get(0);
	}
	
	public ArrayList<Judge> getJudges()
	{
		return panel;
	}
	
	//returns a string listing all judges, each on a separate line
	String getJudgeString ()
	{
		if (panel.size() == 0)
			return "-";
			
		String str = "<html>" + panel.get(0).getName(); 
		
		for (int i = 1; i < panel.size(); i++)
			str += "<br />" + panel.get(i).getName();
		
		str += "</html>";
		return str; 
	}
	
	int getNumJudges ()
	{
		return panel.size();
	}
	
	public boolean hasJudge (Judge judge)
	{
		for (Judge critic : panel)
			if (judge.equals(critic))
				return true; 
		
		return false; 
	}
	

	//--------------------------------------------------------------------------
	// Returns a string e.g. John Doe (AFF) vs. Jane Roe (NEG)
	//--------------------------------------------------------------------------
	public String toString()
	{
		if (teams[0] == null && teams[1] == null)
			return "-";
		
		if (teams[1] == null && (decisions[0] == Outcome.BYE || decisions[0] 
			== Outcome.FORFEIT || decisions[0] == Outcome.NOT_COMPETING))
			return teams[0].toString() + " " + decisions[0].toString();

		if (teams[0] == null && (decisions[1] == Outcome.BYE || decisions[1] 
			== Outcome.FORFEIT || decisions[1] == Outcome.NOT_COMPETING))
			return teams[1].toString() + " " + decisions[1].toString();
		
		if (teams[1] == null && decisions[0] == Outcome.NO_DECISION)
			return teams[0].toString() + " vs. ___________";

		if (teams[0] == null && decisions[1] == Outcome.NO_DECISION)
			return teams[1].toString() + " vs. ___________";
		
		//if there are actually two teams: 
		if (round instanceof ElimRound && specialSidelocked == false 
			&& isBallotEntered() == false)
			return teams[0].toString() + " vs. " + teams[1].toString();
		
		else
			return teams[0].toString() + " (AFF) vs. " + teams[1].toString() 
					+ " (NEG)";
	}



	//--------------------------------------------------------------------------
	// Other mutators and accessors 
	//--------------------------------------------------------------------------
	public void setRoom (Room r)
	{
		room = r;
	}
	
	public void setFlight (char flt)
	{
		if (flt == 'A' || flt == 'B')
			flight = flt; 
	}
	

	void setDecision (Entry team, Outcome decision)
	{
		if (teams[0] != null && teams[0].equals(team))
			decisions[0] = decision; 
		else if (teams[1] != null && teams[1].equals(team))
			decisions[1] = decision; 
	}

	
	void setDecision (int which, Outcome decision)
	{
		decisions[which] = decision;
	}


	void setAffDecision (Outcome decision)
	{
		if (round instanceof ElimRound && specialSidelocked == false
			&& isBallotEntered() == false)
			return; //error! this shouldn't happen; just to check; 
		
		decisions[0] = decision;
	}
	
	void setNegDecision (Outcome decision)
	{
		if (round instanceof ElimRound && specialSidelocked == false
			&& isBallotEntered() == false)
			return; //error! this shouldn't happen; just to check; 
		
		decisions[1] = decision;
	}
	
	//returns the team that appears in the lefthand column - either the Aff or,
	//if it's flip for sides, the one who's alphabetically first, or the only 
	//team if there's only one. 
	Entry getTeam1 ()
	{
		return teams[0];
	}

	//returns the team that's alphabetically second, or null if there's only one
	//team
	Entry getTeam2 ()
	{
		return teams[1];
	}


	Entry getAff () 
	{
		if (round instanceof ElimRound && specialSidelocked == false
			&& isBallotEntered() == false)
			return null; 
		if (isPseudoDebate())
			return null; 
		else
			return teams[0];
	}


	Entry getNeg () 
	{
		if (round instanceof ElimRound && specialSidelocked == false
			&& isBallotEntered() == false)
			return null; 
		else
			return teams[1];
	}
	
	Side getSide (Entry entry)
	{
		if (getAff() != null && entry.equals(getAff()))
			return Side.AFF; 
		else if (getNeg() != null && entry.equals(getNeg()))
			return Side.NEG;
		else
			return Side.NONE;
	}
	
	//---------------------------------------------------------------------------
	// Switches the teams to the other side. 
	//---------------------------------------------------------------------------
	void switchSides ()
	{
		if (teams[0] == null || teams[1] == null)
			return; 
		
		Entry temp = teams[0]; 
		teams[0] = teams[1]; 
		teams[1] = temp;
	}
	
	
	
	int getNumTeams ()
	{
		int n = 0;
		
		if (teams[0] != null)
			n++;
		if (teams[1] != null)
			n++;
		
		return n;
	}
	
	Round getRound ()
	{
		return round;
	}
	
	
	//indicates whether the given entry was one of the teams in this debate
	boolean hasEntry (Entry entry)
	{
		return (teams[0] != null && entry.equals(teams[0])) 
			|| (teams[1] != null && entry.equals(teams[1]));
	}
	
	//Given one entry, returns the other entry, their opponent. 
	Entry getOpponent (Entry entry)
	{
		if (teams[0] != null && entry.equals(teams[0]))
			return teams[1]; 
		if (teams[1] != null && entry.equals(teams[1]))
			return teams[0];
		
		return null; //if that entry is not in this debate 
	}


	public Room getRoom ()
	{
		return room;
	}
	
	public char getFlight ()
	{
		return flight; 
	}
	
	Outcome getAffDecision ()
	{
		if (round instanceof ElimRound && specialSidelocked == false
			&& isBallotEntered() == false)
			return null; 
		else
			return decisions[0];
	}

	Outcome getNegDecision ()
	{
		if (round instanceof ElimRound && specialSidelocked == false
			&& isBallotEntered() == false)
			return null; 
		else
			return decisions[1];
	}
	
	//Returns decision for teams[i] 
	Outcome getDecision (int i)
	{
		return decisions[i];
	}
	
	
	//Returns what the decision was as a string, e.g. "AFF", "Forfeit", "-", etc
	String getDecision ()
	{
		if (getAffDecision().equals(Outcome.WIN))
			return "AFF";
		if (getNegDecision().equals(Outcome.WIN))
			return "NEG";
		if (decisions[0].equals(Outcome.FORFEIT) 
				|| decisions[1].equals(Outcome.FORFEIT))
			return Outcome.FORFEIT.toString();
		if (decisions[0].equals(Outcome.BYE) 
				|| decisions[1].equals(Outcome.BYE))
			return Outcome.BYE.toString(); 
		if (decisions[0].equals(Outcome.NOT_COMPETING) 
				|| decisions[1].equals(Outcome.NOT_COMPETING))
			return Outcome.NOT_COMPETING.toString(); 
		
		return Outcome.NO_DECISION.toString();		
	}

	//returns it as a short string 
	String getDecisionShort ()
	{
		if (getAffDecision().equals(Outcome.WIN))
			return "AFF";
		if (getNegDecision().equals(Outcome.WIN))
			return "NEG";
		if (decisions[0].equals(Outcome.FORFEIT) 
				|| decisions[1].equals(Outcome.FORFEIT))
			return Outcome.FORFEIT.toShortString();
		if (decisions[0].equals(Outcome.BYE) 
				|| decisions[1].equals(Outcome.BYE))
			return Outcome.BYE.toShortString(); 
		if (decisions[0].equals(Outcome.NOT_COMPETING) 
				|| decisions[1].equals(Outcome.NOT_COMPETING))
			return Outcome.NOT_COMPETING.toShortString(); 
		
		return Outcome.NO_DECISION.toShortString();		
	}
	
	
	//returns the decision for a given debater/team. 
	Outcome getDecision (Entry entry)
	{
		if (teams[0] != null && entry.equals(teams[0]))
			return decisions[0];
		if (teams[1] != null && entry.equals(teams[1]))
			return decisions[1];
		
		return null;
	}
	
	//--------------------------------------------------------------------------
	// Methods to get and set locks 
	//--------------------------------------------------------------------------
	void setTeamLocked (Entry team, boolean lock)
	{
		if (teams[0] != null && team.equals(teams[0]))
			teamsLocked[0] = lock;
		if (teams[1] != null && team.equals(teams[1]))
			teamsLocked[1] = lock; 
	}
	
	void setTeamLocked (int which, boolean lock)
	{
		teamsLocked[which] = lock;
	}
	
	public void setJudgeLocked (boolean lock)
	{
		panel.get(0).setLocked(this, lock);  
	}


	public void setJudgeLocked (Judge judge, boolean lock)
	{
		for (Judge j : panel)
			if (j.equals(judge))
				judge.setLocked(this, lock);
	}
	
	public void setRoomLocked (boolean lock)
	{
		roomLocked = lock;
	}
	
	void setAllLocked (boolean lock)
	{
		teamsLocked[0] = lock; 
		teamsLocked[1] = lock; 
		
		for (Judge judge : panel)
			judge.setLocked(this, lock); 
		
		roomLocked = lock; 
	}
	
	
	//Note that teams in a pseudo debate are never locked. They will not be 
	//passed to the pairing algorithm, but they don't get underlined. 
	boolean isLocked (Entry team)
	{
		if (isPseudoDebate())
			return false;
	
		if (teams[0] != null && team.equals(teams[0]))
			return teamsLocked[0];
		if (teams[1] != null && team.equals(teams[1]))
			return teamsLocked[1]; 
		
		return false; 
	}
	
	boolean isLocked (int which)
	{
		if (isPseudoDebate())
			return false;

		return teamsLocked[which];
	}
	
	public boolean isJudgeLocked ()
	{
		if (panel.isEmpty())
			return false; 
	
		return panel.get(0).isLocked(this);
	}

	public boolean isJudgeLocked (Judge judge)
	{
		if (panel.isEmpty())
			return false; 
		
		for (Judge j : panel)
			if (j.equals(judge))
				return j.isLocked(this);
		
		return false; //if it doesn't have the judge
	}
	
	public boolean isRoomLocked ()
	{
		if (room == null)
			return false; 
	
		return roomLocked; 
	}
	
	//--------------------------------------------------------------------------
	// Methods to compute which colors components should be
	//--------------------------------------------------------------------------
	Color getTeamColor (Entry team)
	{
		if (round.hasHappened())
			return Color.BLACK;
		
		//Otherwise, if the round has not yet started: 
		ConflictChecker.setRound(round);
		Conflict[] conflicts = ConflictChecker.checkForConflict(this); 
		
		if (conflicts == null)
			return Color.BLUE; 
		
		for (Conflict conflict : conflicts)
		{
			if (conflict.source == Conflict.DEBATERS)
				return Color.RED; 
			
			if (conflict.source == Conflict.FIRST_DEBATER && teams[0] != null 
				&& team.equals(teams[0]))
				return Color.RED; 
			
			if (conflict.source == Conflict.SECOND_DEBATER && teams[1] != null
				&& team.equals(teams[1]))
				return Color.RED;
		}
		
		return Color.BLUE;
	}
	
	Color getTeamColor (int which)
	{
		if (round.hasHappened())
			return Color.BLACK;
		
		//Otherwise, if the round has not yet started: 
		ConflictChecker.setRound(round);
		Conflict[] conflicts = ConflictChecker.checkForConflict(this); 
		
		if (conflicts == null)
			return Color.BLUE; 
		
		for (Conflict conflict : conflicts)
		{
			if (conflict.source == Conflict.DEBATERS)
				return Color.RED; 
			
			if (conflict.source == Conflict.FIRST_DEBATER && which == 0) 
				return Color.RED; 
			
			if (conflict.source == Conflict.SECOND_DEBATER && which == 1)
				return Color.RED;
		}
		
		return Color.BLUE;
	}


	public Color getJudgeColor ()
	{
		return getJudgeColor(panel.get(0));
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
	

	
	//--------------------------------------------------------------------------
	// Indicates whether the ballot for this round has been entered yet. It has
	// if and only if it is an actual round (i.e. not a one-person bye or 
	// forfeit and not a not-competing) and if both decisions have been entered.
	// For a practice round, a ballot is entered iff the round has started.
	//--------------------------------------------------------------------------
	boolean isBallotEntered ()
	{
		if (teams[0] == null || teams[1] == null)
			return false; 
		
		if (round.hasHappened() && room != null 
			&& panel.size() == round.getNumJudges()
			&& round instanceof PracticeRound)
			return true; 
		
		if (decisions[0] == Outcome.NO_DECISION 
			|| decisions[1] == Outcome.NO_DECISION)
			return false;
		
		else return true; 
	}


	//--------------------------------------------------------------------------
	// This is false if the debate is a one-team bye, one-team forfeit, or a not
	// competing. It is still true even if the debate doesn't have an outcome 
	// yet or only has a single entry entered, because in that case it can still
	// have another entry added and will become an actual debate. If it's a non-
	// one-team bye/forfeit (i.e. if a debate is scheduled and put on postings
	// but one team just doens't show up), then this is still true.  
	//--------------------------------------------------------------------------
	boolean isTrueDebate ()
	{
		//if it has no teams, it's not a real debate
		if (teams[0] == null && teams[1] == null)
			return false; 
		
		//if it has two teams, it must be a real debate 
		if (teams[0] != null && teams[1] != null)
			return true; 
		
		//If this loop is reached, exactly one team will be null. The point of 
		//the loop is just so I don't have to write the code twice - the code 
		//in the if clause will be executed exactly once. 
		for (int i = 0; i <= 1; i++)
		{
			if (teams[i] != null)
			{
				if (decisions[i].equals(Outcome.BYE) 
					|| decisions[i].equals(Outcome.FORFEIT)
					|| decisions[i].equals(Outcome.NOT_COMPETING))
					return false; 
			}
		}
		
		return true; 
	}
	
	//Opposite of a "true" debate 
	boolean isPseudoDebate ()
	{
		return !(isTrueDebate()); 
	}

	//--------------------------------------------------------------------------
	// Methods for whether it's a specific type of "pseudo debate"
	//--------------------------------------------------------------------------
	boolean isOneTeamBye ()
	{
		//if it has no teams, it's not a bye
		if (teams[0] == null && teams[1] == null)
			return false; 
		
		//if it has two teams, it must be a real debate 
		if (teams[0] != null && teams[1] != null)
			return false; 
		
		//If this loop is reached, exactly one team will be null. The point of 
		//the loop is just so I don't have to write the code twice - the code 
		//in the if clause will be executed exactly once. 
		for (int i = 0; i <= 1; i++)
		{
			if (teams[i] != null)
			{
				if (decisions[i].equals(Outcome.BYE))
					return true;
				else
					return false;
			}
		}
		
		return false; //just to satisfy compiler
	}

	boolean isOneTeamFft ()
	{
		//if it has no teams, it's not a forfeit
		if (teams[0] == null && teams[1] == null)
			return false; 
		
		//if it has two teams, it must be a real debate 
		if (teams[0] != null && teams[1] != null)
			return false; 
		
		//If this loop is reached, exactly one team will be null. The point of 
		//the loop is just so I don't have to write the code twice - the code 
		//in the if clause will be executed exactly once. 
		for (int i = 0; i <= 1; i++)
		{
			if (teams[i] != null)
			{
				if (decisions[i].equals(Outcome.FORFEIT))
					return true;
				else
					return false;
			}
		}

		return false; //just to satisfy compiler
	}

	//Note that this is "non" not "not" - that's to avoid confusion (since 
	//"is not competing" sounds like it'd mean something different). 
	boolean isNonCompeting ()
	{
		//if it has no teams, it's not a not competing
		if (teams[0] == null && teams[1] == null)
			return false; 
		
		//if it has two teams, it must be a real debate 
		if (teams[0] != null && teams[1] != null)
			return false; 
		
		//If this loop is reached, exactly one team will be null. The point of 
		//the loop is just so I don't have to write the code twice - the code 
		//in the if clause will be executed exactly once. 
		for (int i = 0; i <= 1; i++)
		{
			if (teams[i] != null)
			{
				if (decisions[i].equals(Outcome.NOT_COMPETING))
					return true;
				else
					return false;
			}
		}

		return false; //just to satisfy compiler
	}

	//--------------------------------------------------------------------------
	// Equality only if they're the same object 
	//--------------------------------------------------------------------------
	public boolean equals (Object other)
	{
		return other != null && this == other; 
	}




	//--------------------------------------------------------------------------
	// Writes backup text. (No spaces allowed!) 
	//--------------------------------------------------------------------------
	String writeBackup ()
	{
		String text = TEAM1 + (teams[0] == null ? "null" 
															: teams[0].toStringNoSpaces());
		text += TEAM2 + (teams[1] == null ? "null" : teams[1].toStringNoSpaces());
		
		for (Judge judge : panel)
			text += JUDGE_START + judge.getNameNoSpaces() + JUDGE_END;
		
		text += ROOM + (room == null ? "null" : room.getNameNoSpaces()); 
		
		text += FLIGHT + flight;
		
		text += DECISION1 + decisions[0].toStringNoSpaces(); 
		text += DECISION2 + decisions[1].toStringNoSpaces(); 
		
		return text; 		
	}


	//--------------------------------------------------------------------------
	// Recovers from text doc 
	//--------------------------------------------------------------------------
	static Debate recoverBackup (String info, Round rd)
	{
		Debate debate = new Debate (rd); 
		
		int start = info.indexOf(TEAM1) + TEAM1.length();
		int end = info.indexOf(TEAM2, start); 
		String team1Name = info.substring(start, end); 
		info = info.substring(end + 1); 
		
		if (team1Name.equals("null") == false)
		{
			for (Entry entry : rd.getTournament().getEntries())
			{
				if (team1Name.equals(entry.toStringNoSpaces()))
				{
					debate.setTeam(entry, 0);
					break;
				}
			}
		}

		start = info.indexOf(TEAM2) + TEAM2.length();
		end = info.indexOf(JUDGE_START, start); 
		if (end == -1) //if no judges 
			end = info.indexOf(ROOM, start); 
		String team2Name = info.substring(start, end); 
		info = info.substring(end + 1); 
		
		if (team2Name.equals("null") == false)
		{
			for (Entry entry : rd.getTournament().getEntries())
			{
				if (team2Name.equals(entry.toStringNoSpaces()))
				{			
					debate.setTeam(entry, 1);
					break;
				}
			}
		}
		
		while (info.startsWith(JUDGE_START))
		{
			start = info.indexOf(JUDGE_START) + JUDGE_START.length();
			end = info.indexOf(JUDGE_END, start); 
			String judgeName = info.substring(start, end); 
			info = info.substring(end + 1); 
			
			debate.addJudge (new Judge(judgeName, rd.getTournament())); 
		}
		
		start = info.indexOf(ROOM) + ROOM.length(); 
		end = info.indexOf(FLIGHT, start); 
		String roomName = info.substring(start, end); 
		info = info.substring(end + 1); 
		
		if (roomName.equals("null") == false)
			debate.setRoom(new Room(roomName, rd.getTournament()));
		
		start = info.indexOf(FLIGHT) + FLIGHT.length(); 
		end = info.indexOf(DECISION1, start); 
		char flight = info.substring(start, end).charAt(0); //should be length 1
		info = info.substring(end + 1); 
		debate.setFlight(flight); 
		
		start = info.indexOf(DECISION1) + DECISION1.length();
		end = info.indexOf(DECISION2, start); 
		String decisionStr = info.substring(start, end); 
		info = info.substring(end + 1); 
		
		for (Outcome decision : Outcome.values())
		{
			if (decisionStr.equals(decision.toStringNoSpaces()))
			{
				debate.setDecision(0, decision);
				break;
			}
		}

		start = info.indexOf(DECISION2) + DECISION1.length();
		decisionStr = info.substring(start); 
		
		for (Outcome decision : Outcome.values())
		{
			if (decisionStr.equals(decision.toStringNoSpaces()))
			{
				debate.setDecision(1, decision);
				break;
			}
		}
		
		return debate;		
	}
	
}










