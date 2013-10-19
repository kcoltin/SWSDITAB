//*****************************************************************************
// ConflictChecker.java
// Kevin Coltin 
//
// This is a utility class used to check whether there is any conflict in a 
// debate that has been paired. It returns a Conflict object or array of 
// Conflict objects representing the conflict(s). 
//*****************************************************************************

 
 

import java.util.ArrayList; 

class ConflictChecker 
{
	private static Tournament tournament; 
	private static Round round; 


	//---------------------------------------------------------------------------
	// Sets which round it is dealing with. Round is a static member - rather 
	// than an argument passed to each method - because it would be too much of a
	// pain to have to pass it to literally every single method. So, it is reset
	// by calling this method by RoundPanel or Pair whenever it starts to work on
	// a different round. 
	//---------------------------------------------------------------------------
	static void setRound (Round r)
	{
		round = r;
		tournament = round.getTournament(); 
	}


	//---------------------------------------------------------------------------
	// Combines the contents of two arrays of conflicts. 
	//---------------------------------------------------------------------------
	private static Conflict[] add (Conflict[] array1, Conflict[] array2)
	{
		//If either is null, the "sum" is equal to the other one. If both are 
		//null, the sum is null. 
		if (array1 == null)
			return array2; 
		if (array2 == null)
			return array1; 
	
		Conflict[] sum = new Conflict[array1.length + array2.length]; 
		
		int i = 0; 
		while (i < array1.length)
		{
			sum[i] = array1[i];
			i++;
		}
		
		//i is now equal to array1.length. 
		int j = 0; 
		
		while (j < array2.length)
		{
			sum[i+j] = array2[j]; 
			j++;
		}		
		
		return sum; 
	}



	//---------------------------------------------------------------------------
	// Converts ArrayList to array 
	//---------------------------------------------------------------------------
	private static Conflict[] toArray (ArrayList<Conflict> list)
	{
		//return null if list is null 
		if (list == null)
			return null; 
	
		Conflict[] array = new Conflict[list.size()]; 
		
		for (int i = 0; i < list.size(); i++)
			array[i] = list.get(i);
		
		return array;
	}



//******************************************************************************
// The first set of methods is used by the Pair algorithm for pairing rounds 
// automatically.  The second set, below, us used by Debates, JudgeInhabitable, 
// and RoomInhabitable containers (i.e., true and pseudo debates and 
// Assignments) to determine the color that an entity should appear on the 
// RoundPanel. 

	//---------------------------------------------------------------------------
	// This version of the method is just called by the pairings algorithm only 
	// when matching debaters. It does not account for sides, rooms, or judges.
	//---------------------------------------------------------------------------
	static Conflict[] checkForConflict (Entry team1, Entry team2)
	{
		//Just call the method to check if it's okay that these two teams hit each
		//other
		return checkMatchup (team1, team2);
	}



	//---------------------------------------------------------------------------
	// Called by Pair when assigning judges to round. If "sidelocked" is true, 
	// team1 is aff and team2 is neg. 
	//---------------------------------------------------------------------------
	static Conflict[] checkForConflict (Entry team1, Entry team2, 
								boolean sidelocked, Judge judge)
	{
		//Just need to check if the judge is okay given the teams and (if 
		//applicable) sides. (Note: don't need to worry about whether the judge is
		//okay by itself, because the Pair algorithm will prevent unavailable 
		//judges from being used or judges being used twice in one flight.) 
		Conflict[] conflicts = null; 
		
		conflicts = checkJudge(judge, team1, team2, sidelocked);
		
		return conflicts;
	}

	

	//---------------------------------------------------------------------------
	// Called by Pair when assigning rooms to round. (This method is not
	// currently used by Pair, but may be in the future.)
	//---------------------------------------------------------------------------
	static Conflict[] checkForConflict (Entry team1, Entry team2, 
										ArrayList<Judge> panel, Room room)
	{
		//As with the above method to check the judge(s), this doesn't need to 
		//consider whether the room in a vacuum is okay. 
		
		//First, see if there's a problem with the room and teams (i.e. if the 
		//room needs to be unlocked) 
		Conflict[] roomTeamConflicts = checkRoom (room, team1, team2); 
		
		//Then, check for room and judges. 
		return add (roomTeamConflicts, checkRoom(room, panel)); 
	}


	//---------------------------------------------------------------------------
	// Called by Pair when assigning rooms to round. The judge is assumed to 
	// already be assigned to one or two True Debates in the round, the question
	// is whether this room should be added to it.  
	//---------------------------------------------------------------------------
	static Conflict[] checkForConflict (Judge judge, Room room)
	{
		ArrayList<Conflict> conflicts = new ArrayList<Conflict>();
	
		//First, see if there's a debate in this room (in either flight, if 
		//applicable)
		boolean hasResident; 
		
		if (round.isFlighted())
			hasResident = hasResident(room, 'A') || hasResident(room, 'B'); 
		else
			hasResident = hasResident(room, 'A');
		
		if (hasResident == false)
			conflicts.add (new Conflict(Conflict.ROOM, 
													Conflict.Problem.NO_RESIDENTS)); 
		
		//Then, check the room's rating. 
		conflicts.add (new Conflict(Conflict.INTERNAL_ONLY, 
												Conflict.Problem.RATING, room.getRating()));

		//Check the room's priority 
		if (room.getPriority(round) == Priority.PriorityLevel.Low)
			conflicts.add (new Conflict(Conflict.INTERNAL_ONLY, 
													Conflict.Problem.LOW_ROOM_PRIORITY)); 
		else if (room.getPriority(round) == Priority.PriorityLevel.Normal)
			conflicts.add (new Conflict(Conflict.INTERNAL_ONLY, 
													Conflict.Problem.NORMAL_ROOM_PRIORITY)); 
		
		//Add judge's comfort level 
		conflicts.add (new Conflict(Conflict.INTERNAL_ONLY, 
			Conflict.Problem.JUDGE_COMFORT, judge, judge.getRoomsDesirability())); 
		
		return toArray(conflicts);	
	}





//******************************************************************************
// Second set of methods - these are used by Debates, JudgeInhabitable, and 
// RoomInhabitable containers (i.e., true and pseudo debates and Assignments) to
// determine the color that an entity should appear on the RoundPanel. 
	
	//---------------------------------------------------------------------------
	// Checks whether this particular debate has any conflicts. Returns an array
	// containing the conflicts, or a null return if there are none. This version
	// of the method is called on a [true] debate in the itemsonpairings array, 
	// or by the pairing algorithm when adding judges or rooms. 
	//---------------------------------------------------------------------------
	static Conflict[] checkForConflict (Debate debate)
	{
		//If it's a pseudo debate, only need to check if there are "bye" problems.
		if (debate.isPseudoDebate())
			return checkByes (debate); 
		
		Conflict[] conflicts = null; 
		Entry team1 = debate.getTeam1();
		Entry team2 = debate.getTeam2();
		
		//If there are two debaters, check for conflicts between them 
		if (team1 != null && team2 != null)
			conflicts = add (conflicts, checkMatchup(team1, team2)); 
			
		//If the sides are set, check for side conflicts
		if (debate.isSidelocked())
		{
			if (team1 != null && team2 != null)
				conflicts = add (conflicts, checkSides (team1, team2)); 
			if (team1 != null)
				conflicts = add (conflicts, checkSingleSide(team1, Side.AFF));
			if (team2 != null)
				conflicts = add (conflicts, checkSingleSide(team2, Side.NEG)); 
		}
		
		//If any judges are assigned, check for conflict with them. 
		for (Judge judge : debate.getJudges())
		{
			//First, check if the judge itself is okay: 
			conflicts = add (conflicts, checkJudge(judge, debate.getFlight()));
			
			//Then, check if judge fits with teams 
			conflicts = add (conflicts, checkJudge(judge, team1, team2, 
									debate.isSidelocked()));
		}
		
		//If room is assigned, check for conflicts 
		Room room = debate.getRoom(); 
		
		if (room != null)
		{
			//First check if room itself is okay 
			conflicts = add (conflicts, checkRoom(room, debate.getFlight())); 
			
			//Then, check if it fits with the judge panel 
			if (debate.getJudges().isEmpty() == false)
				conflicts = add (conflicts, checkRoom (room, debate.getJudges()));
			
			//Lastly, check if it fits with the debaters (i.e. if it's a dorm that
			//needs to be unlocked)
			conflicts = add (conflicts, checkRoom (room, team1, team2));
		}
		
		return conflicts;			
	}
	

	//---------------------------------------------------------------------------
	// Version called on a judgeroomassignment 
	//---------------------------------------------------------------------------
	static Conflict[] checkForConflict (JudgeRoomAssignment assnmnt)
	{
		ArrayList<Judge> judges = assnmnt.getJudges(); 
		Room room = assnmnt.getRoom(); 
	
		//First, check if there is a conflict with the room itself 
		Conflict[] conflicts = checkRoom (room, assnmnt.getFlight()); 

		//Then check judges 
		for (Judge judge : judges)
		{
			//Check if there's a problem with the judge themselves
			conflicts = add (conflicts, checkJudge(judge, assnmnt.getFlight())); 
			//Then check for conflicts with the room 
			conflicts = add (conflicts, checkRoom (room, judges)); 
		}
	
		return conflicts; 
	}

	//---------------------------------------------------------------------------
	// Version called on a judgeassignment 
	//---------------------------------------------------------------------------
	static Conflict[] checkForConflict (JudgeAssignment assnmnt)
	{
		Conflict[] conflicts = null; 
		
		//Only need to check the judges themselves, individually 
		for (Judge judge : assnmnt.getJudges())
			conflicts = add (conflicts, checkJudge(judge, assnmnt.getFlight())); 
		
		return conflicts; 
	}


	//---------------------------------------------------------------------------
	// Version called on a roomassignment 
	//---------------------------------------------------------------------------
	static Conflict[] checkForConflict (RoomAssignment assnmnt)
	{
		//Only need to check the room itself 
		return checkRoom (assnmnt.getRoom(), assnmnt.getFlight()); 
	}



//******************************************************************************
// Lastly, the remainder of the class is internal methods used by all of the 
// above methods. These check for the existence of specific types of conflict. 
// Places where conflicts may exist: 
/* 
		-Matchup - the fact that two teams are hitting each other, regardless of 
			the side/judge/room 
		-Single side - the fact that a single team is on a particular side, 
			regardless of opponent 
		-Sides - the fact that two debaters are facing each other on those sides, 
			as opposed to facing each other on opposite sides 

		-Byes - the fact that there are multiple (single team/pseudo debate) byes,
			or that in outrounds two debaters who are supposed to hit each other 
			both have byes 

		-Judge(s): one or more "unavailable" judges are in use, or a judge is 
			scheduled twice in the same flight 
		-Judge(s) and entry(ies): the fact that this panel is judging these one or 
			two entries, given the sides. 

		-Room: an "unavailable" room is in use, or a room is scheduled twice in 
			the same flight 
		-Room and judge(s): the fact that the given room is being used, given the 
			panel 
		-Room, and entry(ies): the fact that the given room is being used, given 
			the entry(ies)
*/

	//---------------------------------------------------------------------------
	// Checks for conflicts resulting from the matchup (the fact that two entries
	// are hitting each other, regardless of side/room/judge). Note that none of
	// these concerns are relevant in outrounds.  
	//---------------------------------------------------------------------------
	private static Conflict[] checkMatchup (Entry team1, Entry team2)
	{
		ArrayList<Conflict> conflicts = new ArrayList<Conflict>(); 
	
		//Check for the unlikely event that the teams have previously debated more 
		//than once 
		if (round instanceof ElimRound == false 
			&& team1.hasFacedTwice(team2, round))
			conflicts.add (new Conflict (Conflict.DEBATERS, 
													Conflict.Problem.DEBATING_THIRD_TIME));
		
		//Check if the teams have previously debated (the "else" is to skip this
		//if they've debated even more than once).  
		else if (round instanceof ElimRound == false 
			&& team1.hasFaced(team2, round))
			conflicts.add (new Conflict (Conflict.DEBATERS, 
															Conflict.Problem.DEBATING_AGAIN));
		
		//Check if the teams are due to debate on the same side 
		if (round instanceof ElimRound == false 
			&& team1.isDueForSameSide(team2, round))
			conflicts.add (new Conflict (Conflict.DEBATERS, 
														Conflict.Problem.DUE_FOR_SAME_SIDE));
		
		//Check if any of the debaters on either team are from the same school 
		if (round instanceof ElimRound == false && team1.isSameSchool(team2))
				conflicts.add (new Conflict (Conflict.DEBATERS, 
													Conflict.Problem.SAME_SCHOOL)); 
		
		//Check if their records are different and they shouldn't be powermatched
		//against each other 
		if (round.isPowermatched())
		{
			if (Math.abs(team1.getWins() - team2.getWins()) == 1)
				conflicts.add (new Conflict (Conflict.INTERNAL_ONLY, 
														Conflict.Problem.POWER_MATCH_1));
			
			else if (Math.abs(team1.getWins() - team2.getWins()) > 1)
				conflicts.add (new Conflict (Conflict.DEBATERS, 
														Conflict.Problem.POWER_MATCH_2));
		}
		
		//Check if they're not from the same lab, only if it's a non-powermatched
		//round  
		if (round.isPowermatched() == false && team1.isSameLab(team2) == false)
			conflicts.add (new Conflict (Conflict.INTERNAL_ONLY, 
													Conflict.Problem.DIFFERENT_LAB));
		
		return toArray(conflicts); 
	}





	//---------------------------------------------------------------------------
	// Checks for conflicts resulting from the side an entry is assigned to, 
	// regardless of judge or opponent. "side" should be one of the ints defined
	// in Entry - Side.AFF or Side.NEG. 
	//---------------------------------------------------------------------------
	private static Conflict[] checkSingleSide (Entry entry, Side side)
	{
		if (entry.getSideDueFor(round) != Side.NONE 
			&& entry.getSideDueFor(round) != side)
		{
			Conflict[] conflict = new Conflict[1];
			int source = side == Side.AFF 
										? Conflict.FIRST_DEBATER 
										: Conflict.SECOND_DEBATER; 
			conflict[0] = new Conflict (source, Conflict.Problem.WRONG_SIDE);
			return conflict;
		}
		else
			return null;
	}


	//---------------------------------------------------------------------------
	// Checks for conflicts resulting from the fact that two entries are facing
	// each other on particular sides, AS OPPOSED TO IF THEY WERE HITTING EACH 
	// OTHER ON THE OPPOSITE SIDES. Even in that case, it is very limited - it 
	// doesn't check if both debaters are due to be on the other side, because 
	// that's already covered by checkSingleSide - it ONLY checks whether the 
	// teams have previously debated on the same side. 
	//---------------------------------------------------------------------------
	private static Conflict[] checkSides (Entry aff, Entry neg)
	{
		//this int is the net number of times that "aff" has debated "neg" on the 
		//aff. E.g., if they've debated thrice and "aff" was aff twice, it would 
		//equal 1. 
		int net = 0; 
		
		//find all debates (if any) they've had against each other 
		for (Round rd : tournament.getRounds())
		{
			//only check rounds prior to the one in question. 
			if (rd.equals(round))
				break;
			
			Debate debate = rd.getDebate(aff);
			
			//increment net depending on what side "aff" was if they debated each
			//other. 
			if (debate != null && debate.getOpponent(aff) != null 
				&& debate.getOpponent(aff).equals(neg))
			{
				if (debate.getAff() != null && debate.getAff().equals(aff))
					net++;
				else if (debate.getNeg() != null && debate.getNeg().equals(aff))
					net--; 
			}
		}
		
		//Now, if "aff" has been on the aff more often than not, there will be a
		//conflict. 
		if (net > 0)
		{
			Conflict[] conflict = new Conflict[1];
			conflict[0] = new Conflict (Conflict.DEBATERS, 
												Conflict.Problem.DEBATING_AGAIN_SAME_SIDES);
			return conflict;
		}
		else
			return null; 
	}



	//---------------------------------------------------------------------------
	// Checks for conflicts resulting from byes - that there is more than one 
	// (pseudo debate/one person) bye, and/or that two entries supposed to hit
	// each other in outrounds are both bying.  The argument should be a pseudo-
	// debate bye, so the method only returns conflicts pertaining to that 
	// particular debate.  
	//---------------------------------------------------------------------------
	private static Conflict[] checkByes (Debate bye)
	{
		//Make sure the debate is actually a bye 
		if (bye.isOneTeamBye() == false)
			return null; 
	
		ArrayList<Conflict> conflicts = new ArrayList<Conflict>(); 
		
		//First, check if there are multiple byes in the same round 
		if (round.getByes().size() > 1)
			conflicts.add (new Conflict(Conflict.DEBATERS, //see footnote* 
													Conflict.Problem.MULTIPLE_BYES)); 
		
		//Next, if it's an outround, there's a conflict if the seed that this team
		//is supposed to be hitting also has a bye. (because they can't both 
		//advance)
		if (round instanceof ElimRound)
		{
			assert bye.getTeam1() != null; 
			
			Entry entry = bye.getTeam1();
			Entry opponent = Sort.getComplement ((ElimRound) round, entry); 
			
			//If opponent also has a bye, ther'es a conflict. 
			Debate oppsDebate = round.getDebate(opponent);
			if (oppsDebate != null && oppsDebate.isOneTeamBye())
				conflicts.add (new Conflict(Conflict.DEBATERS, //see footnote*
													Conflict.Problem.ELIM_DOUBLE_BYE));
		}
		
		return toArray(conflicts); 

		// *Might as well just assign the source as both debaters, since that will
		//  certainly cause the one debater to have a warning associated with 
		//  him or her. 
	}
	


	//---------------------------------------------------------------------------
	// Checks a single judge to see if they are assigned when they should be 
	// unavailable or if they are assigned to multiple debates in the same 
	// flight.
	//---------------------------------------------------------------------------
	private static Conflict[] checkJudge (Judge judge, char flight)
	{
		ArrayList<Conflict> conflicts = new ArrayList<Conflict>();
	
		//Create an error if judge is unavailable for this round 
		if (judge.getPriority(round) == Priority.PriorityLevel.Unavailable)
			conflicts.add (new Conflict(Conflict.JUDGE, 
												Conflict.Problem.UNAVAILABLE_JUDGE, judge));
		
		
		
		//Create error if judge is assigned to multiple debates in the same flight
		int n = 0; //number of debates the judge is assigned to in this flight
		
		for (Flightable container : round.getItemsOnPairings())
		{
			if (container.getFlight() == flight 
				&& container instanceof JudgeInhabitable 
				&& ((JudgeInhabitable) container).hasJudge(judge))
				n++;
		}
		
		if (n > 1)
			conflicts.add (new Conflict(Conflict.JUDGE, 
											Conflict.Problem.JUDGE_ASSIGNED_TWICE, judge));
		
		return toArray(conflicts);
	}



	//---------------------------------------------------------------------------
	// Checks whether it is okay for this judge to judge these entries. If 
	// argument "sidelocked" is true, then team1 is aff and team2 is neg (and the
	// method factors sides in when checking for conflicts). Otherwise, it's a 
	// flip for sides debate. 
	//---------------------------------------------------------------------------
	private static Conflict[] checkJudge (Judge judge, Entry team1, Entry team2, 
														boolean sidelocked)
	{
		ArrayList<Conflict> conflicts = new ArrayList<Conflict>(); 
		
		//Check if judge is struck against any of the students.  
		for (Competitor struck : judge.getStudentStrikes())
		{
			boolean breakOuterLoop = false; 
		
			for (int i = 0; i < tournament.getEvent().getKidsPerTeam(); i++)
			{
				if ((team1.getStudents().length > i
					&& team1.getStudents()[i] != null 
					&& struck.equals(team1.getStudents()[i]))
					|| (team2.getStudents().length > i
					&& team2.getStudents()[i] != null 
					&& struck.equals(team2.getStudents()[i])))
				{
					conflicts.add (new Conflict(Conflict.JUDGE, 
													Conflict.Problem.STUDENT_STRIKE, judge));
					breakOuterLoop = true;
					break;
				}
			}
			
			if (breakOuterLoop)
				break;
		}
		
		//Check if judge is struck against any of the schools 
		for (School struck : judge.getSchoolStrikes())
		{
			boolean breakOuterLoop = false; 
		
			for (int i = 0; i < tournament.getEvent().getKidsPerTeam(); i++)
			{
				if ((team1 != null 
					&& team1.getStudents().length > i
					&& team1.getStudents()[i] != null 
					&& struck.equals(team1.getStudents()[i].getSchool()))
					|| (team2 != null 
					&& team2.getStudents().length > i
					&& team2.getStudents()[i] != null 
					&& struck.equals(team2.getStudents()[i].getSchool())))
				{
					conflicts.add (new Conflict(Conflict.JUDGE, 
													Conflict.Problem.SCHOOL_STRIKE, judge));
					breakOuterLoop = true;
					break;
				}
			}
			
			if (breakOuterLoop)
				break;
		}
		
		//Check if judge has already judged a team on the same side 
		boolean hasRJSS = false; //indicates whether there's a repeat judge same 
											//side conflict
		if (sidelocked) //remember, team1 is aff, team2 is neg 
		{
			for (Debate debate : judge.getDebates())
			{
				//Only look to debates prior to this round 
				if (debate.getRound().compareTo(round) >= 0)
					continue;
			
				if ((debate.getAff() != null && debate.getAff().equals(team1))
				 || (debate.getNeg() != null && debate.getNeg().equals(team2)))
				{
					//Check whether it's prelims or elims - repeats aren't as bad in
					//elims. 
					Conflict.Problem problem;
					int source; 
					if (round instanceof ElimRound)
					{
						problem = Conflict.Problem.REPEAT_JUDGE_ELIMS;
						source = Conflict.INTERNAL_ONLY;
					}
					else
					{
						problem = Conflict.Problem.REPEAT_JUDGE_SAME_SIDE;
						source = Conflict.JUDGE;
					}
				
					conflicts.add (new Conflict(source, problem, judge));
					hasRJSS = true;
					break;
				}
			}
		}

		//Check if judge has already judged a team on a different side 
		if (hasRJSS == false) //skip this if they've judged on other side
		{
			for (Debate debate : judge.getDebates())
			{
				//Only look to debates prior to this round 
				if (debate.getRound().compareTo(round) >= 0)
					continue;

				if ((team1 != null && debate.hasEntry(team1)) 
					|| (team2 != null && debate.hasEntry(team2)))
				{
					//Check whether it's prelims or elims - repeats aren't as bad in
					//elims. 
					Conflict.Problem problem;
					if (round instanceof ElimRound)
						problem = Conflict.Problem.REPEAT_JUDGE_ELIMS;
					else
						problem = Conflict.Problem.REPEAT_JUDGE;
				
					conflicts.add (new Conflict(Conflict.JUDGE, problem, judge));
					break;
				}
			}
		}
		
		
		//Check if judge has priority set to "low" 
		if (judge.getPriority(round).equals(Priority.PriorityLevel.Low))
			conflicts.add (new Conflict(Conflict.INTERNAL_ONLY, 
								Conflict.Problem.LOW_PRIORITY, judge));

		//Check if judge has priority set to "normal" 
		if (judge.getPriority(round).equals(Priority.PriorityLevel.Normal))
			conflicts.add (new Conflict(Conflict.INTERNAL_ONLY, 
								Conflict.Problem.NORMAL_PRIORITY, judge));
		
		//note: already checked for REPEAT_JUDGE_ELIMS above. 
		
		//Lastly, check the number of rounds this judge has judged 
		int num = judge.getDebates().size();
		conflicts.add (new Conflict (Conflict.INTERNAL_ONLY, 
											Conflict.Problem.NUM_RDS_JUDGED, judge, num));
		
		return toArray(conflicts); 
	}

	
	//---------------------------------------------------------------------------
	// Checks a single room to see if it's assigned when it should be unavailable
	// or if it's assigned to multiple debates in the same flight.
	//---------------------------------------------------------------------------
	private static Conflict[] checkRoom (Room room, char flight)
	{
		ArrayList<Conflict> conflicts = new ArrayList<Conflict>();
	
		//Create an error if room is unavailable for this round 
		if (room.getPriority(round) == Priority.PriorityLevel.Unavailable)
			conflicts.add (new Conflict(Conflict.ROOM, 
												Conflict.Problem.UNAVAILABLE_ROOM));
		
		
		
		//Create error if room is assigned to multiple debates in the same flight
		int n = 0; //number of debates the room is assigned to in this flight
		
		for (Flightable container : round.getItemsOnPairings())
		{
			if (container.getFlight() == flight 
				&& container instanceof RoomInhabitable 
				&& ((RoomInhabitable) container).getRoom() != null
				&& ((RoomInhabitable) container).getRoom().equals(room))
				n++;
		}
		
		if (n > 1)
			conflicts.add (new Conflict(Conflict.ROOM, 
											Conflict.Problem.ROOM_ASSIGNED_TWICE));
		
		return toArray(conflicts);
	}




	//---------------------------------------------------------------------------
	// Checks if there are any problems with the given room being used with the
	// given panel. 
	//---------------------------------------------------------------------------
	private static Conflict[] checkRoom (Room room, ArrayList<Judge> panel)
	{
		Conflict[] conflicts = new Conflict[panel.size()];
	
		//Checks for judges' comfort level. 
		for (int i = 0; i < panel.size(); i++)
		{
			conflicts[i] = new Conflict(Conflict.INTERNAL_ONLY, 
											Conflict.Problem.JUDGE_COMFORT, panel.get(i),
											panel.get(i).getRoomsDesirability());
		}
		
		return conflicts; 
	}


	//---------------------------------------------------------------------------
	// Checks if there is a problem with assigning these entries to this room - 
	// this is really only if it's a dorm room that none of them live in. 
	// This method also checks the room's desirability rating (because it is 
	// called by the pairing algorithm, so that check had to go somewhere and
	// this is the most logical place). 
	//---------------------------------------------------------------------------
	private static Conflict[] checkRoom (Room room, Entry team1, Entry team2)
	{
		ArrayList<Conflict> conflicts = new ArrayList<Conflict>();
	
		//Cycle through all residents and check if any of them is in this debate. 
		boolean hasResident; //whether a resident of the room is in this debate
		
		//skip this if it's not a dorm 
		if (room.getResidents().isEmpty())
			hasResident = true;
		else
		{
			if (round.isFlighted())
				hasResident = hasResident(room, 'A') || hasResident(room, 'B'); 
			else 
				hasResident = hasResident(room, 'A');
		}
		
		if (hasResident == false)
			conflicts.add (new Conflict(Conflict.ROOM, 
													Conflict.Problem.NO_RESIDENTS));
		
		//Then, check the room's rating. 
		conflicts.add (new Conflict(Conflict.INTERNAL_ONLY, 
												Conflict.Problem.RATING, room.getRating()));
		
		//Check the room's priority 
		if (room.getPriority(round) == Priority.PriorityLevel.Low)
			conflicts.add (new Conflict(Conflict.INTERNAL_ONLY, 
													Conflict.Problem.LOW_ROOM_PRIORITY)); 
		else if (room.getPriority(round) == Priority.PriorityLevel.Normal)
			conflicts.add (new Conflict(Conflict.INTERNAL_ONLY, 
													Conflict.Problem.NORMAL_ROOM_PRIORITY)); 

		return toArray(conflicts);
	}




	//---------------------------------------------------------------------------
	// Indicates whether there is a resident of this room assigned to this room
	// in this round. Returns true if the room is not a dorm room. 
	//---------------------------------------------------------------------------
	private static boolean hasResident (Room room, char flight)
	{
		Debate debate = round.getDebate (room, flight); 
		if (debate == null) 
			return false; 
		Entry team1 = debate.getTeam1();
		Entry team2 = debate.getTeam2();
		
		if (room.getResidents().isEmpty())
			return true; 
	
		for (Competitor resident : room.getResidents())
		{
			for (int i = 0; i < tournament.getEvent().getKidsPerTeam(); i++)
			{
				if ((team1 != null
					&& team1.getStudents().length > i 
					&& team1.getStudents()[i] != null
					&& team1.getStudents()[i].equals(resident)) 
					|| (team2 != null
					&& team2.getStudents().length > i 
					&& team2.getStudents()[i] != null
					&& team2.getStudents()[i].equals(resident)))
				{
					return true;
				}
			}
		}
		
		return false; 
	}


	
}

























