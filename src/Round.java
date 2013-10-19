//*****************************************************************************
// Round.java
// Kevin Coltin 
// 
// Represents a single round of competition in a single event.  Round in this 
// case refers to a series of debates/sections, e.g. Round 1 refers to all of
// the debates that happened first round.
//*****************************************************************************

 
 

import java.io.Serializable;
import java.util.ArrayList; 

class Round implements Comparable, Serializable
{
	private static final long serialVersionUID = 357975091382590L;

	protected Tournament tournament; 

	//Name by which the round will be referenced, e.g. "Round 1". This is 
	//implemented by its child classes. 
	protected String name;
	
	//Number of round, e.g. prelim round 2; implemented by child classes 
	protected int number; 
	
	//ArrayList of all the debates in this round. The order in which debates
	//are held in this array is defined as the order in which they will appear
	//on postings - i.e. first are flight A rounds, then B, then byes, then
	//forfeits, then notcompetings. 
	protected ArrayList<Debate> debates; 
	
	//Round status, defined by the enum class Status, indicates at what stage 
	//the round is. 
	// Not paired: round has been created but no debates have been paired yet
	// Paired: one or more debates have been paired, but round has not yet 
	//		actually started 
	// In progress: round has started, some but not all ballots may have been
	//		entered
	// Completed: all ballots have been entered 
	protected Status roundStatus; 
	enum Status {NOT_STARTED("Not started", false), IN_PROGRESS("In progress",
					true), COMPLETED("Completed", true); 
		
		private String name; //string representation of the status
		private boolean hasHappened; //whether the round has happened yet
		
		Status (String n, boolean hh)
		{
			name = n; 
			hasHappened = hh;
		}
		
		public String toString ()
		{
			return name; 
		}

		String toStringNoSpaces ()
		{
			return name.replace(' ', '_');
		}
		
		boolean hasHappened ()
		{
			return hasHappened; 
		}
	}


	//number of judges in each debate in the round- almost always 1 or 3. This 
	//is just the default; it can be overridden if for some reason you want to 
	//have a different number of judges in different debates in the same round. 
	protected int numJudges; 
	
	//Whether the round is sidelocked or flip for sides. This is just the 
	//default - it may be changed for individual debates (e.g. if two debaters
	//are hitting each other again in an outround)
	protected boolean sidelocked; 
	
	//time the round is officially supposed to go off, to be printed on postings
	//and in official schedule of tournament. BTime is the time the B flight 
	//starts, if there is one. 
	protected String time, BTime; 
	
	//If true, postings will just print "ASAP" as the time rather than the 
	//actual scheduled time. 
	protected boolean asap; 
	
	//Day of the round - can be a day of the week, a date, or both - user's 
	//preference. 
	protected String day; 

	//Comments to print out at the bottom of postings 
	protected String commentsOnPostings; 
	
	//whether the round is flighted
	protected boolean flighted; 
	
	//Panel displaying the round 
	protected transient RoundPanel roundPanel; 

	//Array holding, in order, all of the items - debates, blank spaces, etc. - 
	//that may appear on a pairings that is in progress. They appear on the 
	//pairings exactly in the order in which they are in the array list. 
	protected ArrayList<Flightable> itemsOnPairings; 

	//Codes used for writing tournament to file 
	private static final String ROUND_TYPE = "00", NUMBER = "01", LEVEL = "02",
										DEBATE_START = "03", ROUND_STATUS = "04", 
										NUM_JUDGES = "05", FLIGHTED = "06";


	//--------------------------------------------------------------------------
	// Constructor 
	//--------------------------------------------------------------------------
	Round (Tournament t, TournamentFrame tf)
	{
		tournament = t; 
		name = ""; 
		debates = new ArrayList<Debate>(); 
		roundStatus = Status.NOT_STARTED; 
		numJudges = 1; 
		sidelocked = true; 
		time = "0:00"; 
		BTime = "0:00";
		asap = false; 
		day = ""; 
		commentsOnPostings = ""; 
		flighted = true; 	
		roundPanel = new RoundPanel(this, tf); 
		itemsOnPairings = new ArrayList<Flightable>(); 
	}
	

	//--------------------------------------------------------------------------
	// Methods for debates array list
	//--------------------------------------------------------------------------
	void addDebate (Debate debate)
	{
		debates.add(debate);
	}

	void removeDebate (Debate debate)
	{
		Sort.remove(debates, debate);
	}

	
	ArrayList<Debate> getDebates ()
	{
		return debates;
	}

	ArrayList<Debate> getDebates (char flight)
	{
		ArrayList<Debate> debs = new ArrayList<Debate>();
		
		for (Debate deb : debates)
			if (deb.getFlight() == flight)
				debs.add(deb);
	
		return debs;
	}
	
	//returns the debate in this round that this debater was in. (This one, 
	//unlike the other overloaded versions of the method, doesn't check the 
	//flight because an entry can only have one flight in a round.)  
	Debate getDebate (Entry entry)
	{
		for (Debate debate : debates)
			if (debate.hasEntry(entry))
				return debate;
		
		return null; //if the debater is not assigned to a debate that round
	}
	
	//returns the debate in this round and flight that this judge judged 
	Debate getDebate (Judge judge, char flight)
	{
		for (Debate debate : debates)
			if (debate.getFlight() == flight && debate.hasJudge(judge))
				return debate;
		
		return null; //if the judge did not judge that round 
	}

	//returns the debate in this round and flight held in this room
	Debate getDebate (Room room, char flight)
	{
		for (Debate debate : debates)
			if (debate.getFlight() == flight && room.equals(debate.getRoom()))
				return debate;
		
		return null; //if the room was unused that round 
	}
	
	
	//--------------------------------------------------------------------------
	// Mutator and accessor methods
	//--------------------------------------------------------------------------
	Tournament getTournament ()
	{
		return tournament; 
	}

	void setStatus (Status status)
	{
		roundStatus = status; 
	}
	
	//Called on an in progress or completed debate after a ballot is entered or 
	//removed to redetermine whether it's still completed or in progress 
	void resetStatus ()
	{
		int num = 0; 
		
		for (Debate debate : getTrueDebates())
			if (debate.isBallotEntered())
				num++; 
		
		//Change status, but keep it the same if it hasn't even started yet. 
		if (roundStatus == Status.NOT_STARTED)
			return;
		if (num == getTrueDebates().size() && getUnassignedDebaters().isEmpty()
			&& tournament.getEntries().size() > 0)
			roundStatus = Status.COMPLETED;
		else
			roundStatus = Status.IN_PROGRESS; 
	}
	
	void setNumJudges (int num)
	{
		numJudges = num; 
	}
	
	void setSidelocked (boolean locked)
	{
		sidelocked = locked; 
	}
	
	void setTime (String hour)
	{
		time = hour; 
	}
	
	void setTimeFlightB (String hour)
	{
		if (flighted)
			BTime = hour; 
	}
	
	void setASAP (boolean isASAP)
	{
		asap = isASAP;
	}
	
	void setDay (String date)
	{
		day = date; 
	}
	
	void setCommentsOnPostings (String comment)
	{
		commentsOnPostings = comment; 
	}

	void setFlighted (boolean isFlighted)
	{
		flighted = isFlighted;
		
		if (flighted && BTime == null)
		{
			BTime = "0:00";
		}
		if (flighted) //if it's becoming flighted
		{
			//Set all items on bottom half of pairings to flight B. 
			ArrayList<Flightable> items = itemsOnPairings; 
			int i = (int) Math.ceil(items.size() / 2.); 
			
			while (i < items.size())
			{
				items.get(i).setFlight('B');
				i++;
			}
		}
		else //if it's becoming unflighted 
		{
			BTime = null; 
			
			for (Flightable item : itemsOnPairings)
				item.setFlight('A');
		}
	}
	
		
	String getName ()
	{
		return name; 
	}

	Status getStatus ()
	{
		return roundStatus; 
	}
	
	String getStatusString ()
	{
		resetStatus(); 
		return roundStatus.toString(); //TODO: this method is incomplete, finish it. 
		//Should indicate how many ballots are in if it's in progress, or how many entries
		//still need to be paired if it hasn't started 
	}

	boolean hasHappened ()
	{
		return roundStatus.hasHappened(); 
	}

	int getNumJudges ()
	{
		return numJudges; 
	}
	
	boolean isSidelocked ()
	{
		return sidelocked; 
	}
	
	String getTime ()
	{
		return time; 
	}
	
	String getTimeFlightB ()
	{
		return BTime; 
	}
	
	boolean isASAP ()
	{
		return asap; 
	}
	
	String getDay ()
	{
		return day; 
	}
	
	String getCommentsOnPostings ()
	{
		return commentsOnPostings; 
	}
	
	boolean isFlighted ()
	{
		return flighted; 
	}
	
	ArrayList<Flightable> getItemsOnPairings ()
	{
		return itemsOnPairings; 
	}

	ArrayList<Flightable> getItemsOnPairings (char flight)
	{
		ArrayList<Flightable> items = new ArrayList<Flightable>();
		
		for (Flightable item : itemsOnPairings)
			if (item.getFlight() == flight)
				items.add(item); 
	
		return items; 
	}
	
	

	//--------------------------------------------------------------------------
	// First, practice rounds come before prelims come before outrounds. Next, 
	// lower-numbered rounds (or earlier outrounds) come before higher-numbered
	// rounds or later outrounds. 
	//--------------------------------------------------------------------------
	public int compareTo (Object another) 
	{
		Round other = (Round) another; 
		
		//First check if they're the same 
		if (this.equals(other))
			return 0; 
		
		int type = 1; //1=practice, 2=prelim, 3=elim
		int othertype = 1; 
		
		if (this instanceof PrelimRound)
			type = 2; 
		else if (this instanceof ElimRound)
			type = 3; 
			
		if (other instanceof PrelimRound)
			othertype = 2; 
		else if (other instanceof ElimRound)
			othertype = 3; 
		
		if (type != othertype)
			return type - othertype; 

		//if rounds are same type but not outrounds, or if the break level has
		//not yet been set (i.e. if outrounds are just called "elim round 1" etc)
		if (type != 3 || ((ElimRound) this).getLevel() == null)
			return name.compareTo(other.getName()); 
		
		//if rounds are outrounds with levels (semis, finals, etc.)
		return ((ElimRound) this).getLevel().ordinal() 
				- ((ElimRound) other).getLevel().ordinal(); 
	}
	
	//--------------------------------------------------------------------------
	// Equality based on name
	//--------------------------------------------------------------------------
	public boolean equals (Object another)
	{
		Round other = (Round) another; 
		return name.equals(other.getName()); 
	}

	//--------------------------------------------------------------------------
	// Methods for the RoundPanel 
	//--------------------------------------------------------------------------
	RoundPanel getPanel ()
	{
		return roundPanel;
	}
	

	//--------------------------------------------------------------------------
	// Returns all debates that are a single-entry bye. (I.e., ones that are 
	// paired to be a bye, and not just ones where one debater gets a free win
	// because the opponent doesn't show up.) 
	//--------------------------------------------------------------------------
	ArrayList<Debate> getByes ()
	{
		ArrayList<Debate> byes = new ArrayList<Debate>(); 
		
		for (Debate debate : debates)
			if (debate.isOneTeamBye())
				byes.add(debate);
		
		return byes; 
	}


	//--------------------------------------------------------------------------
	// Returns all debates that are a single-entry forfeit. (I.e., ones that are 
	// paired to be a bye, and not just ones where one debater gets a free win
	// because the opponent doesn't show up.) 
	//--------------------------------------------------------------------------
	ArrayList<Debate> getForfeits ()
	{
		ArrayList<Debate> ffts = new ArrayList<Debate>(); 
		
		for (Debate debate : debates)
			if (debate.isOneTeamFft())	
				ffts.add(debate); 
		
		return ffts; 
	}



	//--------------------------------------------------------------------------
	// Returns all debates that are not going to compete in this round, for
	// whatever reason, but won't receive a win or loss for it (i.e. it's not
	// a bye or forfeit).  
	//--------------------------------------------------------------------------
	ArrayList<Debate> getNotCompetings ()
	{
		ArrayList<Debate> ncs = new ArrayList<Debate>(); 
		
		for (Debate debate : debates)
			if (debate.isNonCompeting())
				ncs.add(debate); 
		
		return ncs; 
	}



	//--------------------------------------------------------------------------
	// Returns all entries who aren't assigned to a debate (neither a true 
	// debate nor a pseudo debate, i.e. byes/forfeits/notcompetings). 
	//--------------------------------------------------------------------------
	ArrayList<Entry> getUnassignedDebaters ()
	{
		//Note: cannot just say debaters = tournament.getEntries because it will
		//then refer to that object. 
		ArrayList<Entry> debaters = new ArrayList<Entry>(); 
		for (Entry entry : tournament.getEntries())
			debaters.add(entry); 
		
		for (Debate debate : debates)
		{
			if (debate.getTeam1() != null)
				Sort.remove (debaters, debate.getTeam1()); 
			if (debate.getTeam2() != null)
				Sort.remove (debaters, debate.getTeam2()); 
		}
		
		return debaters; 		
	}


	//--------------------------------------------------------------------------
	// Returns all "true" debates, as defined in Debate. 
	//--------------------------------------------------------------------------
	ArrayList<Debate> getTrueDebates ()
	{
		ArrayList<Debate> trues = new ArrayList<Debate>(); 
		
		for (Debate debate : debates)
			if (debate.isTrueDebate())
				trues.add(debate);
		
		return trues;
	}


	//--------------------------------------------------------------------------
	// Returns all "true" debates, as defined in Debate, for given flight
	//--------------------------------------------------------------------------
	ArrayList<Debate> getTrueDebates (char flight)
	{
		ArrayList<Debate> trues = new ArrayList<Debate>(); 
		
		for (Debate debate : debates)
			if (debate.isTrueDebate() && debate.getFlight() == flight)
				trues.add(debate);
		
		return trues;
	}


	//--------------------------------------------------------------------------
	// Returns all "true" debates in the given flight that are fully paired, 
	// i.e. they have a room, two teams, and the correct number of judges.  
	//--------------------------------------------------------------------------
	ArrayList<Debate> getFullyPairedTrueDebates (char flight)
	{
		ArrayList<Debate> trues = new ArrayList<Debate>(); 
		
		for (Debate debate : debates)
			if (debate.getFlight() == flight 
				&& debate.isTrueDebate()
				&& debate.getRoom() != null
				&& debate.getNumTeams() == 2
				&& debate.getJudges().size() == numJudges) 
			{
				trues.add(debate);
			}
			
		return trues;
	}
	
	
	

	//--------------------------------------------------------------------------
	// Returns all pseudo-debates, as defined in Debate. 
	//--------------------------------------------------------------------------
	ArrayList<Debate> getPseudoDebates ()
	{
		ArrayList<Debate> pseudos = new ArrayList<Debate>(); 
		
		for (Debate debate : debates)
			if (debate.isPseudoDebate())
				pseudos.add(debate);
		
		return pseudos;
	}
	
	
	//--------------------------------------------------------------------------
	// Returns the number of debates in Flight A. (Takes advantage of the fact 
	// that the arraylist "debates" will be sorted such that all Flight A rounds
	// are first.)  
	//--------------------------------------------------------------------------
	int getNumDebatesFlightA ()
	{
		if (flighted == false)
			return getTrueDebates().size();
		
		int n = 0; 
		
		while (n < debates.size())
		{
			if (debates.get(n).getFlight() == 'A')
				n++;
			else
				break;
		}
		
		return n; 
	}
	
	//Likewise, for flight B
	int getNumDebatersFlightB ()
	{
		if (flighted == false)
			return 0; 
		
		int n = 0; 
		
		while (n < debates.size())
		{
			if (debates.get(n).getFlight() == 'A')
				continue;
			else if (debates.get(n).getFlight() == 'B')
				n++;
			else
				break;
		}
		
		return n;
	}
	
	//---------------------------------------------------------------------------
	// True iff a judge/room is not assigned to any debates or assignments in
	// this round 
	//---------------------------------------------------------------------------
	boolean isUnassigned (Judge judge)
	{
		for (Flightable container : itemsOnPairings)
			if (container instanceof JudgeInhabitable 
				&& ((JudgeInhabitable) container).hasJudge(judge))
				return false;
		
		return true;
	}

	boolean isUnassigned (Room room)
	{
		for (Flightable container : itemsOnPairings)
			if (container instanceof RoomInhabitable 
				&& ((RoomInhabitable) container).getRoom() != null 
				&& ((RoomInhabitable) container).getRoom().equals(room))
				return false;
		
		return true;
	}
	
	//---------------------------------------------------------------------------
	// These two methods are true for judges or rooms that are unassigned for  
	// EITHER flight.  
	//---------------------------------------------------------------------------
	boolean isUnassignedEitherFlight (Judge judge)
	{
		if (flighted == false)
			return isUnassigned(judge); 
		
		//if flighted, only false if judge is in both flights. 
		boolean inFltA = false; 
		boolean inFltB = false; 

		for (Flightable container : itemsOnPairings)
		{
			if (container instanceof JudgeInhabitable 
				&& ((JudgeInhabitable) container).hasJudge(judge))
			{
				if (container.getFlight() == 'A')
					inFltA = true; 
				else if (container.getFlight() == 'B')
					inFltB = true;
			}
		}
		
		//true unless it's assigned in both flights.  
		return !(inFltA && inFltB); 		
	}

	boolean isUnassignedEitherFlight (Room room)
	{
		if (flighted == false)
			return isUnassigned(room); 
		
		//if flighted, only false if room is in both flights. 
		boolean inFltA = false; 
		boolean inFltB = false; 

		for (Flightable container : itemsOnPairings)
		{
			if (container instanceof RoomInhabitable 
				&& ((RoomInhabitable) container).getRoom() != null 
				&& ((RoomInhabitable) container).getRoom().equals(room))
			{
				if (container.getFlight() == 'A')
					inFltA = true; 
				else if (container.getFlight() == 'B')
					inFltB = true;
			}
		}
		
		//true unless it's assigned in both flights.  
		return !(inFltA && inFltB); 		
	}
	
	
	//---------------------------------------------------------------------------
	// Indicates whether all entries have been assigned to a debate (either true 
	// or pseudo debate) and all debates have been assigned a room and the 
	// correct number of judges. 
	//---------------------------------------------------------------------------
	boolean isFullyPaired ()
	{
		//if some entries are unassigned, it's false. 
		if (getUnassignedDebaters().isEmpty() == false)
			return false; 
		
		//make sure every true debate has a room and judge(s)
		for (Debate debate : getTrueDebates())
			if (debate.getRoom() == null || debate.getJudges().size() != numJudges)
				return false; 
		
		//if these check out, return true. 
		return true; 
	}
	
	


	//---------------------------------------------------------------------------
	// Returns the number of ballots entered. (Ballots are only counted from true
	// debates.) 
	//---------------------------------------------------------------------------
	int getNumBallotsEntered ()
	{
		int n = 0; 
		
		for (Debate debate : getTrueDebates())
			if (debate.isBallotEntered())
				n++; 
		
		return n; 
	}



	//---------------------------------------------------------------------------
	// Returns the round that preceded this one. If this is the first round of 
	// the tournament, returns null. 
	//---------------------------------------------------------------------------
	Round getPreviousRound ()
	{
		ArrayList<Round> rounds = tournament.getRounds(); 
		
		if (this.equals(rounds.get(0)))
			return null; 
		
		int i = 1; 
		
		while (i < rounds.size())
		{
			if (this.equals(rounds.get(i)))
				return rounds.get(i-1);
			
			i++;
		}
		
		return null; //just to satisfy compiler
	}



	//---------------------------------------------------------------------------
	// Returns the round that followed this one. If this is the last round of 
	// the tournament, returns null. 
	//---------------------------------------------------------------------------
	Round getNextRound ()
	{
		ArrayList<Round> rounds = tournament.getRounds(); 
		
		int i = 0; 
		
		while (i < rounds.size() - 1)
		{
			if (this.equals(rounds.get(i)))
				return rounds.get(i + 1);
			
			i++;
		}
		
		return null; //this will be true if it's the last round of the tournament.
	}



	//---------------------------------------------------------------------------
	// True unless it's a practice round or the first prelim. 
	//---------------------------------------------------------------------------
	boolean isPowermatched ()
	{
		return !(this instanceof PracticeRound || 
			(tournament.getPrelims().size() > 0 
			&& this.equals(tournament.getPrelims().get(0))));
	}



	//---------------------------------------------------------------------------
	// Converts to a text form for backup 
	//---------------------------------------------------------------------------
	String writeBackup ()
	{
		String text = ROUND_TYPE; 
		
		if (this instanceof PracticeRound)
			text += "PracticeRound";
		else if (this instanceof PrelimRound)
			text += "PrelimRound";
		else if (this instanceof ElimRound)
			text += "ElimRound";
		
		text += " " + NUMBER + number + " " + LEVEL;
		
		if (this instanceof ElimRound && ((ElimRound) this).getLevel() != null)
			text += ((ElimRound) this).getLevel().toString();
		else
			text += "null";
			
		text += " ";
		
		for (Debate debate : debates)
			text += DEBATE_START + debate.writeBackup() + " "; 
		
		text += ROUND_STATUS + roundStatus.toStringNoSpaces() + " ";
		
		text += NUM_JUDGES + numJudges + " ";
		
		text += FLIGHTED + flighted + " ";
		
		return text; 
	}



	//---------------------------------------------------------------------------
	// Recovers from a text document as backup 
	//---------------------------------------------------------------------------
	static Round recoverBackup(String info, Tournament tourn, TournamentFrame tf)
	{
		Round round; 
		int start, end;
		
		//Get type of round 
		start = info.indexOf(ROUND_TYPE) + ROUND_TYPE.length();
		end = info.indexOf(' ', start); 
		String type = info.substring(start, end); 
		info = info.substring(end + 1);
		
		//Get number of round 
		start = info.indexOf(NUMBER) + NUMBER.length();
		end = info.indexOf(' ', start); 
		String numStr = info.substring(start, end); 
		int num = Integer.parseInt(numStr); 
		info = info.substring(end + 1); 
		
		//Get level 
		start = info.indexOf(LEVEL) + LEVEL.length();
		end = info.indexOf(' ', start); 
		String lev = info.substring(start, end); 
		info = info.substring(end + 1); 
		
		Outround level = null;
		
		if (lev.equals("null") == false)
		{
			for (Outround or : Outround.values())
			{
				if (lev.equals(or.toString()))
				{
					level = or;
					break;
				}
			}
		}
		
		if (type.equals("PracticeRound"))
			round = new PracticeRound (tourn, tf, num);
		else if (type.equals("PrelimRound"))
			round = new PrelimRound (tourn, tf, num); 
		else //if elim
		{
			if (level == null)
				round = new ElimRound (tourn, tf, num);
			else
				round = new ElimRound (tourn, tf, level);
		}

		while (info.startsWith(DEBATE_START))
		{
			start = info.indexOf(DEBATE_START) + DEBATE_START.length();
			end = info.indexOf(' ', start); 
			String debateInfo = info.substring(start, end); 
			info = info.substring(end + 1);
			
			round.addDebate(Debate.recoverBackup(debateInfo, round));
		}

		//Add all debates to items on pairings 
		for (Debate debate : round.getTrueDebates())
			round.getItemsOnPairings().add(debate); 
		
		//Get round status 
		start = info.indexOf(ROUND_STATUS) + ROUND_STATUS.length();
		end = info.indexOf(' ', start); 
		String statusStr = info.substring(start, end); 
		info = info.substring(end + 1);
		
		for (Status status : Status.values())
		{
			if (statusStr.equals(status.toStringNoSpaces()))
			{
				round.setStatus(status); 
				break;
			}
		}
		
		//Get number of judges 
		start = info.indexOf(NUM_JUDGES) + NUM_JUDGES.length(); 
		end = info.indexOf(' ', start);
		String numJ = info.substring(start, end); 
		info = info.substring(end + 1); 
		
		round.setNumJudges (Integer.parseInt(numJ));
		
		//Get whether round is flighted 
		start = info.indexOf(FLIGHTED) + FLIGHTED.length(); 
		end = info.indexOf(' ', start); 
		String fltd = info.substring(start, end); 
		info = info.substring(end + 1); 

		round.setFlighted (fltd.equals("true"));
		
		return round; 
	}
	
	
	//---------------------------------------------------------------------------
	// Creates new round panel
	//---------------------------------------------------------------------------	
	void resetRoundPanel (TournamentFrame tf)
	{
		roundPanel = new RoundPanel(this, tf);
	}


	//---------------------------------------------------------------------------	
	// Returns the number of unique judges who are assigned to a true debate with 
	// two teams in this round. 
	//---------------------------------------------------------------------------	
	int getNumAssignedJudges ()
	{
		ArrayList<Judge> judges = new ArrayList<Judge>(); 
		for (Debate debate : getTrueDebates())
		{
			if (debate.getTeam1() != null && debate.getTeam2() != null 
				&& debate.getJudges().size() == 1 
				&& judges.contains(debate.getJudge()) == false)
			{
				judges.add(debate.getJudge()); 				
			}
		}
		
		return judges.size(); 
	}



}














