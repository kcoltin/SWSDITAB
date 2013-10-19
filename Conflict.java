//******************************************************************************
// Conflict.java
// Kevin Coltin 
//
// Objects of this class represent conflicts in a paired debate - e.g., if a 
// judge is judging someone from his or her own school. 
//******************************************************************************

 
 

class Conflict 
{
	//Contains the object or objects that caused the conflict - e.g., if two 
	//debaters from the same school are debating, it would contain the two 
	//debaters; if a judge is judging the same debater again, it would contain 
	//the judge. Will equal one of the following integer values. 
	final int source;  
	
	static final int DEBATERS = 1, //if the teams shouldn't be facing each other
				FIRST_DEBATER = 2, //problems with debater in left column
				SECOND_DEBATER = 3, //same (right column) 
				JUDGE = 4, //judge shouldn't be judging one or both teams 
				ROOM = 5, //if a judge is assigned to different rooms each flight 
				INTERNAL_ONLY = 6; //this is for items that are just nonessential 
								//priorities, like using judges who haven't judged many 
								//rounds yet. They don't show up on the RoundPanel and
								//are just used by the pairing algorithm. 
	
	//Type of conflict
	final Problem problem; 
	
	//Special values assigned to conflicts that do not enter into the pairing 
	//algorithm.
	static final int ERROR_VALUE = -1, WARNING_VALUE = -2; 
	
	enum Problem {
		//The first set of Problems are ones resulting from teams who should not 
		//be hitting each other 
		DEBATING_THIRD_TIME ("Teams have already debated each other twice.", 
			10000),
		DEBATING_AGAIN ("Teams have already debated each other.", 200), 
		SAME_SCHOOL ("Debaters are from the same school.", 30), 
		POWER_MATCH_2 ("Debaters should not be powermatched against each other.", 
							25), //if records differ by 2 or more in a powered round 
		DUE_FOR_SAME_SIDE ("Both teams are due to debate on the same side.", 20),
		
		POWER_MATCH_1 ("INTERNAL ONLY", 10), //if records differ by 1 in a powered
														//round. (Doesn't display to user
														//because it's so often inevitable.)
		DIFFERENT_LAB ("INTERNAL ONLY", 1),
		
		//The next set of Problems relates to which judge(s) is/are assigned to 
		//watch the teams 
		STUDENT_STRIKE ("Judge is struck against one of the students.", 100),
		SCHOOL_STRIKE ("Judge is struck against one of the students' schools.", 
			12), 
		REPEAT_JUDGE_SAME_SIDE ("Judge has already judged one of the students, "
			+ "on the same side.", 9),
		REPEAT_JUDGE ("Judge has already judged one of the students.", 6),
		LOW_PRIORITY ("INTERNAL ONLY", 3), //judge's priority is low
		NORMAL_PRIORITY ("INTERNAL ONLY", 2), //judge's priority is normal 
		REPEAT_JUDGE_ELIMS ("INTERNAL ONLY", 1), //in elims, if already judged
																//these teams
		NUM_RDS_JUDGED ("INTERNAL ONLY", 0), //so that judges who haven't judged
														//as much will get more ballots
		
		//This set of Problems relates to the room that the debate is assigned to
		NO_RESIDENTS ("Dorm room; a resident of this room should be assigned so "
			+ "they can unlock it.", 150), 
		LOW_ROOM_PRIORITY ("INTERNAL ONLY", 75), //room's priority is low
		NORMAL_ROOM_PRIORITY ("INTERNAL ONLY", 30), //room's priority is normal 
		RATING ("INTERNAL ONLY", 0), //so that higher rated rooms are used more.
												//ranges from 100-500. 
		JUDGE_COMFORT ("INTERNAL ONLY", 0), //so judges who've had crappy rooms 
														//before will get better ones. Ranges
														//from 1-5. 
		
		//This set is for Problems that are checked and must be fixed before the
		//pairing algorithm is even called, because they create impossible or 
		//error-prone situations. 
		JUDGE_ASSIGNED_TWICE ("Judge assigned twice in same flight", ERROR_VALUE),
		ROOM_ASSIGNED_TWICE ("Room assigned twice in same flight", ERROR_VALUE), 
		UNAVAILABLE_JUDGE ("Judge is unavailable for this round.", ERROR_VALUE), 
		UNAVAILABLE_ROOM ("Room is unavailable for this round.", ERROR_VALUE), 
		ELIM_DOUBLE_BYE ("Teams seeded to debate each other in this outround are "
			+ "both assigned a bye.", ERROR_VALUE), 
		HITTING_WRONG_SEED ("Teams are not seeded to debate each other in this " 
			+ "outround.", ERROR_VALUE), 
		
		//Finally are warnings: they are displayed on the round panel, but do not
		//affect the pairing algorithm (because the algorithm would never cause 
		//them - they are only caused by manual changes.) 
		DEBATING_AGAIN_SAME_SIDES ("Teams have met previously and should switch "
			+ "sides.", WARNING_VALUE), 
		WRONG_SIDE ("Team has had more debates on this side, should switch sides",
			WARNING_VALUE), 
		MULTIPLE_BYES ("Multiple teams are assigned a bye in the same round.", 
			WARNING_VALUE); 
			//end of list of types of problem 

		
		String explanation; //explanation to display to user

		//this value represents the "cost" of pairing a round with this conflict 
		//for the pairing algorithm, which uses the Simplex algorithm.
		int value; 
		
		//Constructor 
		Problem (String why, int val)
		{
			explanation = why;
			value = val;
		}
	}
		

	final Judge whichJudge; //indicates the judge that's the source (since there
									//may be more than one on a panel) 
	
	final int numRdsJudged; //how many rounds a judge has previously judged - 
									//only used by NUM_RDS_JUDGED type.

	final int roomRating; //used only by RATING type. 
	
	final int comfortRating; //used only by JUDGE_COMFORT type. 

	//---------------------------------------------------------------------------
	// Constructors 
	//---------------------------------------------------------------------------
	//Default 
	Conflict (int src, Problem prob)
	{
		source = src; 
		problem = prob; 
		
		//initialize just to satisfy compiler 
		numRdsJudged = -1;
		whichJudge = null; 
		roomRating = -1;
		comfortRating = -1; 
	}

	//For all judge-related problems
	Conflict (int src, Problem prob, Judge which)
	{
		source = src; 
		problem = prob; 
		whichJudge = which; 

		//initialize just to satisfy compiler 
		numRdsJudged = -1;
		roomRating = -1;
		comfortRating = -1; 
	}

	//for NUM_RDS_JUDGED 
	Conflict (int src, Problem prob, Judge which, int num)
	{
		assert src == INTERNAL_ONLY; 
	
		source = src; 
		problem = prob; 
		whichJudge = which;
		numRdsJudged = num;

		//initialize just to satisfy compiler 
		roomRating = -1;
		comfortRating = -1; 
	}

	//for RATING 
	Conflict (int src, Problem prob, Room.RoomRating rating)
	{
		assert src == INTERNAL_ONLY; 
	
		source = src; 
		problem = prob; 
		roomRating = 5 * (rating.ordinal() + 1); //multiplied to give it a 
													//proportionally good value relative to 
													//other concerns - see RATING. 

		//initialize just to satisfy compiler 
		numRdsJudged = -1;
		whichJudge = null; 
		comfortRating = -1; 
	}
	
	//for JUDGE_COMFORT 
	Conflict (int src, Problem prob, Room.RoomRating rating, 
				int judgeRoomsDesirabilityRating)
	{
		assert src == INTERNAL_ONLY; 
		
		source = src; 
		problem = prob;
			
		//Formula to compute comfort rating 
		comfortRating = 5 - Math.abs(rating.ordinal()+1 - 
												judgeRoomsDesirabilityRating);

		//initialize just to satisfy compiler 
		numRdsJudged = -1;
		whichJudge = null; 
		roomRating = -1;
	}
	
	
	
	
	//Returns string explanation of conflict 
	public String toString ()
	{
		return problem.explanation;
	}
	
	//Returns int value of the Problem 
	int getValue()
	{
		return problem.value;
	}
	

}





