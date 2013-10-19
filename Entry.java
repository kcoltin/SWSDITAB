//*****************************************************************************
// Entry.java
// Kevin Coltin 
//
// Represents a single entry - e.g. John Doe in LD or John Doe and Jane Roe in
// PFD - at a tournament. As it is now, the maximum number of students that can
// be allowed in an event is two. 
//*****************************************************************************

 
 

import java.io.Serializable;
import java.util.ArrayList; 

class Entry implements Comparable, Serializable 
{
	private static final long serialVersionUID = 7584673589675891L;

	private Tournament tournament; 

	//event: either LD or PFD
	private Event event; 
	
	//Competitor(s). The one in slot 0 will always be the one whose name is 
	//alphabetically first, for consistency.  
	private Competitor[] students; 
	
	//random number used as tiebreaker for breaking 
	private final double rand;
	
	//boolean only used in rare cases to indicate a student ineligible to break
	private boolean ineligibleToBreak;

	//Codes used for writing tournament to file 
	private static String STUDENT = "01", FIRST_NAME = "02", LAST_NAME = "03", 
								SCHOOL = "04"; 
	
	//--------------------------------------------------------------------------
	// Constructor - LD
	//--------------------------------------------------------------------------
	Entry (Event e, Tournament t, Competitor stdnt)
	{
		event = e; 
		tournament = t; 
		students = new Competitor[2]; 
		students[0] = stdnt; 
		rand = Math.random();
		ineligibleToBreak = false; 
	}

	//--------------------------------------------------------------------------
	// Constructor - PFD
	//--------------------------------------------------------------------------
	Entry (Event e, Tournament t, Competitor stdnt1, Competitor stdnt2) 
	{
		event = e; 
		tournament = t; 
		students = new Competitor[2]; 
		
		if (stdnt1.compareTo(stdnt2) < 0)
		{
			students[0] = stdnt1; 
			students[1] = stdnt2; 
		}
		else
		{
			students[0] = stdnt2; 
			students[1] = stdnt1; 
		}
		
		rand = Math.random();
		ineligibleToBreak = false; 
	}
	
	
	
	//--------------------------------------------------------------------------
	// After anything about the students is changed, this method can be called 
	// to make sure they are in the proper "student" slots. 
	//--------------------------------------------------------------------------
	void updateCompetitors()
	{
		if (students.length < 2)
			return; 
		
		if (students[1] == null)
			return;
	
		if (students[0] == null)
		{
			students[0] = students[1];
			students[1] = null; 
			return;
		}
		
		if (students[0].compareTo(students[1]) > 0)
		{
			Competitor temp = students[0]; 
			students[0] = students[1]; 
			students[1] = temp; 
		}
	}
	

	//--------------------------------------------------------------------------
	// Add or remove students 
	//--------------------------------------------------------------------------
	void addStudent(Competitor competitor) throws Exception
	{
		int i = 0; 
		
		while (i < students.length)
		{
			if (students[i] == null)
			{
				students[i] = competitor; 
				updateCompetitors();
				return; 
			}
			
			i++;
		}
		
		//If it reaches this point, the student will not have been added
		throw new Exception ("Entry already contains maximum number of students "
									+ "allowable in this event.");
	}
	
	void removeStudent (Competitor competitor)
	{
		if (students[0].equals(competitor))
		{
			students[0] = null; 
			
			if (students.length > 1)
			{
				students[0] = students[1]; 
				students[1] = null;
			}
			
			return;
		}
		
		if (students.length > 1 && students[1].equals(competitor))
			students[1] = null; 
	}

	
	//--------------------------------------------------------------------------
	// Return string - how the entry will appear on postings 
	//--------------------------------------------------------------------------
	public String toString()
	{
		if (getNumKids() == 0)
			return "-";
	
		if (getNumKids() == 1)
			return students[0].getName();
		
		else
			return students[0].getLastName()	+ "-" + students[1].getLastName();
	}
	
	String toStringNoSpaces ()
	{
		return toString().replace(' ', '_');
	}
	

	//--------------------------------------------------------------------------
	// Overloaded version of toString with additional arguments, whether to show
	// debater(s)'s school and whether to show first names (for partner events).
	//--------------------------------------------------------------------------
	public String toString(boolean showFullNames, boolean showSchool)
	{
		if (getNumKids() == 0)
			return "-";
			
		else if (getNumKids() == 1 && showSchool == true)
			return students[0].getName() + " (" + students[0].getSchool() + ")";
		
		else if (getNumKids() == 1 && showSchool == false)
			return toString();

		else if (getNumKids() == 2 && showFullNames == true)
		{
			if (showSchool)
				return students[0].getName()	+ " (" + students[0].getSchool()
						+ ") - " + students[1].getName() + " (" 
						+ students[1].getSchool() + ")";
			else
				return students[0].getName() + " - " + students[1].getName(); 
		}
		else 
		{
			if (showSchool)
				return students[0].getLastName()	+ " (" + students[0].getSchool()
						+ ") - " + students[1].getLastName() + " (" 
						+ students[1].getSchool() + ")";
			else
				return toString();
		}
	}
			


	//--------------------------------------------------------------------------
	// Mutator and accessor methods
	//--------------------------------------------------------------------------
	Competitor[] getStudents ()
	{
		return students;
	}
	
	int getNumKids ()
	{
		if (students[0] != null && students[1] != null)
			return 2; 

		if (students[0] == null && students[1] == null)
			return 0; 

		return 1; 
	}
	
	void setEvent (Event e)
	{
		event = e; 
	}
	
	Event getEvent ()
	{
		return event;
	}
	
	//Get record as string 
	String getRecord ()
	{
		return getWins() + "-" + getLosses(); 						
	}
	
	//Get wins and losses. Only returns prelims.  
	int getWins ()
	{
		int wins = 0;
		
		for (Debate debate : tournament.getPrelims(this))
		{
			Debate.Outcome decision = debate.getDecision(this);
			
			if (decision.equals(Debate.Outcome.WIN) 
				|| decision.equals(Debate.Outcome.BYE))
				wins++;
		}
		
		return wins; 						
	}

	int getLosses ()
	{
		int losses = 0;
		
		for (Debate debate : tournament.getPrelims(this))
		{
			Debate.Outcome decision = debate.getDecision(this);
			
			if (decision.equals(Debate.Outcome.LOSS) 
				|| decision.equals(Debate.Outcome.FORFEIT))
				losses++;
		}
		
		return losses; 						
	}



	//Get strength of opp (total wins by opponents). Only considers prelims. 
	int getOppWins ()
	{
		int wins = 0; 
		
		for (Debate debate : tournament.getPrelims(this))
		{
			Entry opponent = debate.getOpponent (this);
			
			if (opponent != null)
				wins += opponent.getWins();
		}
		
		return wins;
	}


	
	//Get random tiebreaker 
	double getRand()
	{
		return rand;
	}
	
	//mutator and accessor for ineligible boolean 
	boolean isIneligibleToBreak ()
	{
		return ineligibleToBreak;
	}
	
	void setIneligibleToBreak (boolean inel)
	{
		ineligibleToBreak = inel; 
	}
	
	//Returns all debates this entry has been in 
	ArrayList<Debate> getDebates ()
	{
		return tournament.getDebates (this);
	}
	
	ArrayList<Debate> getPrelims ()
	{
		return tournament.getPrelims (this); 
	}


	//---------------------------------------------------------------------------
	// Returns this team's opponent in the given round, if any. 
	//---------------------------------------------------------------------------
	Entry getOpponent (Round round)
	{
		if (round == null || round.getDebate(this) == null)
			return null; 
		else
			return round.getDebate(this).getOpponent(this);	
	}



	//---------------------------------------------------------------------------
	// True iff these entries have debated each other (including a two-team bye/
	// forfeit) prior to the given round. 
	//---------------------------------------------------------------------------
	boolean hasFaced (Entry otherTeam, Round currentRound)
	{
		for (Round rd : tournament.getRounds())
		{
			//Quit the loop when we get to the current round 
			if (rd.equals(currentRound))
				break;
			
			if (getOpponent(rd) != null && getOpponent(rd).equals(otherTeam))
				return true;			
		}
		
		return false; 		
	}


	//---------------------------------------------------------------------------
	// True only in the unlikely even that these entries have debated each other
	// more than once prior to the given round. 
	//---------------------------------------------------------------------------
	boolean hasFacedTwice (Entry otherTeam, Round currentRound)
	{
		int n = 0; //times they've debated 
	
		for (Round rd : tournament.getRounds())
		{
			//Quit the loop when we get to the current round 
			if (rd.equals(currentRound))
				break;
			
			if (getOpponent(rd) != null && getOpponent(rd).equals(otherTeam))
				n++;			
		}
		
		return n >= 2 ? true : false; 
	}
	


	
	//--------------------------------------------------------------------------
	// Equality and comparison based on alphabetical order of students - first 
	// student first. Entries with no students are last. Can only compare within
	// same event. 
	//--------------------------------------------------------------------------
	public boolean equals (Object another)
	{
		if (another == null)
			return false; 
	
		Entry other = (Entry) another; 
		Competitor[] others = other.getStudents();
		
		boolean same = true;
		
		int i = 0; 
		
		while (i < students.length)
		{
			if ((students[i] == null && others[i] != null) 
				|| (students[i] != null && others[i] == null))
			{
				same = false; 
				break; 
			}
		
			if ((students[i] == null && others[i] == null) 
				|| (students[i].equals(others[i])))
				i++;
			else
			{
				same = false; 
				break;
			}
		}
			
		
		return same; 
	}

	public int compareTo (Object another) 
	{
		Entry other = (Entry) another; 
		Competitor[] others = other.getStudents();
		
		for (int i = 0; i < students.length; i++)
		{
			if (students[i] == null && others[i] == null)
				return i; 
			if (students[i] != null && others[i] == null)
				return -1; 
			if (students[i] == null && others[i] != null)
				return 1; 

			if (i == students.length-1)
				return students[i].compareTo(others[i]);
			else
			{
				if (students[i].compareTo(others[i]) != 0)
					return students[i].compareTo(others[i]);
			}
		}
		
		return 0; //this is unreachable; just here to satisfy compiler
	}
	


	//---------------------------------------------------------------------------
	// Compares it to another entry based on their seedings. Order of factors:
	//
	// 1.	Number of wins 
	// 2.	Strength of opp 
	// 3.	Total number of debates – a team that has missed one or more rounds 
	//		will be ranked lower .  (this is equivalent to number of losses, when 
	//		dealing with two teams with equal wins – the team with more losses 
	//		will, paradoxically, be higher seeded.) 
	// 4.	Head-to-head 
	// 5.	Random 
	// 
	// Note that if it returns a negative value, it means that this Entry comes
	// first, i.e. has a higher (better) seed. Also note that this is only based
	// on results from prelims, not practice rounds (obviously) or elim rounds. 
	//---------------------------------------------------------------------------
	int compareSeedTo (Object another)
	{
		Entry other = (Entry) another; 
		
		//First, test wins. 
		int compare = other.getWins() - getWins(); 
		if (compare != 0)
			return compare; 
		
		//Next, compare strength of opp 
		compare = other.getOppWins() - getOppWins(); 
		if (compare != 0)
			return compare; 
		
		//Next, compare total number of debates - i.e., losses. 
		compare = other.getLosses() - getLosses(); 
		if (compare != 0)
			return compare; 
		
		//Next, see if one has beaten the other head-to-head. h2h is the number of
		//times other has beaten this minus the number of times this has beaten 
		//other. (So, it will be negative if this has beaten other head to head.)
		int h2h = 0; 
		
		for (Debate debate : getPrelims())
		{
			if (debate.getOpponent(this) != null 
				&& debate.getOpponent(this).equals(other))
			{
				if (debate.getDecision(this) == Debate.Outcome.WIN)
					h2h--; 
				else if (debate.getDecision(this) == Debate.Outcome.LOSS)
					h2h++; 
			}
		}
		
		if (h2h != 0)
			return h2h; 
		
		//Finally, compare random numbers. 
		return rand > other.getRand() ? -1 : 1; 
	}



	//---------------------------------------------------------------------------
	// Indicates which lab group the students are in. For LD, this is 
	// straightforward. For PFD, if they're in the same lab it returns that lab, 
	// but otherwise it returns 0 if they're in a mixed lab (0 is equivalent to 
	// no lab). 
	//---------------------------------------------------------------------------
	int getLab ()
	{
		if (tournament.getEvent().getKidsPerTeam() == 1)
			return students[0].getLab(); 
		else
		{
			if (students[0].getLab() == students[1].getLab())
				return students[0].getLab();
			else
				return 0;
		}
	}


	//---------------------------------------------------------------------------
	// False only if both debaters on this team are from a different lab as both
	// debaters on the other team. 
	//---------------------------------------------------------------------------
	boolean isSameLab (Entry otherTeam)
	{
		//If it's LD, true if either team is unassigned or if in same lab. 
		if (event.getKidsPerTeam() == 1)
			return students[0].getLab() == 0 
				|| otherTeam.getStudents()[0].getLab() == 0
				|| students[0].getLab() == otherTeam.getStudents()[0].getLab(); 

		//Otherwise, it's true if either team is both zeros...
		if (students[0].getLab() == 0 && students[1].getLab() == 0)
			return true; 
		if (otherTeam.getStudents()[0].getLab() == 0 
			&& otherTeam.getStudents()[1].getLab() == 0)
			return true; 
		
		//...or if either debater in this entry is in the same lab as either kid
		//on the other team 
		for (int i = 0; i < event.getKidsPerTeam(); i++)
		{
			if (students[i] != null)
			{
				for (int j = 0; j < event.getKidsPerTeam(); j++)
				{
					if (otherTeam.getStudents()[j] != null
						&& students[i].getLab() != 0
						&& students[i].getLab() ==  otherTeam.getStudents(
																						)[j].getLab())
					return true; 
				}
			}
		}
		
		return false; 
	}
		


	//---------------------------------------------------------------------------
	// Indicates which side the entry is due to compete on in the given round, 
	// given all previous rounds (including previous rounds that have not yet
	// been started).
	//---------------------------------------------------------------------------
	Side getSideDueFor (Round nextRd)
	{
		//this int is the net number of times that this team has been aff - it is
		//equal to the number of aff rounds minus number of neg rounds.
		int net = 0; 
		for (Round round : tournament.getRounds())
		{
			//ignore rounds past the current one. 
			if (round.compareTo (nextRd) >= 0)
				break;
			
			Debate debate = round.getDebate(this); 
			
			if (debate != null)
			{
				if (debate.getSide(this) == Side.AFF)
					net++; 
				else if (debate.getSide(this) == Side.NEG)
					net--; 
			}
		}
		
		if (net > 0)
			return Side.NEG;
		if (net < 0)
			return Side.AFF;
		else
			return Side.NONE;		
	}



	//---------------------------------------------------------------------------
	// Indicates whether two entries are due to debate on the same side. 
	//---------------------------------------------------------------------------
	boolean isDueForSameSide (Entry other, Round rd)
	{
		if (getSideDueFor(rd) == Side.NONE || other.getSideDueFor(rd) == Side.NONE)
			return false; 
		else
			return getSideDueFor(rd) == other.getSideDueFor(rd);		
	}



	//---------------------------------------------------------------------------
	// True iff any of the debaters on this team are from the same school as any
	// of the debaters on the other team. 
	//---------------------------------------------------------------------------
	boolean isSameSchool (Entry otherTeam)
	{ 
		for (int i = 0; i < event.getKidsPerTeam(); i++)
		{
			if (students[i] != null)
			{
				for (int j = 0; j < event.getKidsPerTeam(); j++)
				{
					if (otherTeam.getStudents()[j] != null
						&& students[i].getSchool().equals(otherTeam.getStudents(
																				)[j].getSchool()))
					return true; 
				}
			}
		}
		
		return false; 
	}



	//---------------------------------------------------------------------------
	// Saves a backup text file 
	//---------------------------------------------------------------------------
	String writeBackup ()
	{
		String text = "";
	
		for (Competitor student : students)
		{
			if (student != null)
			{
				text += STUDENT + " "; 
	
				text += FIRST_NAME + " " + student.getFirstNameNoSpaces() + " " 
							+ LAST_NAME + " " + student.getLastNameNoSpaces() + " " 
							+ SCHOOL + " " + student.getSchool().getNameNoSpaces() 
							+ " "; 
			}
		}
		
		return text; 
	}
	

	//---------------------------------------------------------------------------
	// Indicates whether this team has had a bye before 
	//---------------------------------------------------------------------------
	boolean hasHadBye ()
	{
		ArrayList<Debate> debates = tournament.getDebates(this); 
		
		for (Debate debate : debates)
			if (debate.getDecision(this) == Debate.Outcome.BYE)
				return true; 
		
		return false; 
	}

	
	//---------------------------------------------------------------------------
	// Recovers an entry from a backed up text file. Given a text string :
	//---------------------------------------------------------------------------
	static Entry recoverBackup (String entryInfo, Tournament tourn)
	{
		Competitor[] students = new Competitor[tourn.getEvent().getKidsPerTeam()];
		
		int i = 0; 
		
		while (entryInfo.startsWith(STUDENT))
		{
			int start = STUDENT.length() + 1 + FIRST_NAME.length() + 1; 
			int end = entryInfo.indexOf(' ', start); 
			String firstName = entryInfo.substring(start, end); 
			entryInfo = entryInfo.substring(end + 1); 
			
			start = LAST_NAME.length() + 1; 
			end = entryInfo.indexOf(' ', start); 
			String lastName = entryInfo.substring(start, end); 
			entryInfo = entryInfo.substring(end + 1); 
			
			start = SCHOOL.length() + 1; 
			end = entryInfo.indexOf(' ', start); 
			String schoolName = entryInfo.substring(start, end); 
			entryInfo = entryInfo.substring(end + 1); 
			
			students[i] = new Competitor(firstName, lastName, new School(schoolName));
			
			i++;
		}
		
		if (students.length == 1 || students[1] == null)
			return new Entry (tourn.getEvent(), tourn, students[0]); 
		else
			return new Entry (tourn.getEvent(), tourn, students[0], students[1]); 
	}



}












