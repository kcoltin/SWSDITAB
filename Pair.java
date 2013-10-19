//*****************************************************************************
// Pair.java
// Kevin Coltin 
// 
// Utility class used to pair rounds.   
//*****************************************************************************

 
 

import java.util.ArrayList; 
import javax.swing.JOptionPane; 

class Pair 
{
	private static Tournament tournament; 
	
	static final int DEBATES = 1, DEBATES_AND_JUDGES = 2, ALL_THREE = 3,
			JUDGES_AND_ROOMS = 4, JUDGES = 5, ROOMS = 6;  
	
	
	//*NOTE: As of now, this completely disregards "locks." That functionality 
	//will have to be added later. (The exception is that it acknowledges locks
	//(of debaters, judges, or rooms) in a true debate in which both teams are
	//locked.) 

	
	//--------------------------------------------------------------------------
	// Sets tournament 
	//--------------------------------------------------------------------------
	static void setTournament (Tournament t)
	{
		tournament = t;
	}

	
	//--------------------------------------------------------------------------
	// Primary method of the class - pairs a debate round. 
	//--------------------------------------------------------------------------
	static void pair (Round round, int type)
	{
		//First, if applicable, pair debates 
		if (type == DEBATES || type == DEBATES_AND_JUDGES || type == ALL_THREE)
		{
			if (!(round instanceof ElimRound))
				pairDebates (round); 
			else 
			{
				//can't pair if breaklevel isn't set yet
				if (tournament.isBreakLevelSet() == false)
				{
					JOptionPane.showMessageDialog (round.getPanel(), "Cannot pair "
						+ "round - need to set break\nlevel first.", "Pair Round", 
						JOptionPane.ERROR_MESSAGE); 
					return;
				}

				//if all ballots in previous rounds aren't entered, show warning. 
				for (Round rd : tournament.getRounds())
				{
					if (rd.compareTo(round) >= 0)
						break;
					
					if (rd.getStatus() != Round.Status.COMPLETED)
					{
						int choice = JOptionPane.showConfirmDialog (round.getPanel(), 
							"Not all ballots have been entered for " + rd.getName() 
							+ " - are you\nsure you want to pair this elim round?", 
							"Pair Round", JOptionPane.YES_NO_OPTION, 
							JOptionPane.WARNING_MESSAGE); 
						
						if (choice == JOptionPane.YES_OPTION)
							break;
						else
							return;
					}
				}
			
				pairElimDebates ((ElimRound) round);
			}
		}

		
		//Next, if applicable, pair judges to all true debates 
		if (type == DEBATES_AND_JUDGES || type == ALL_THREE 
			|| type == JUDGES_AND_ROOMS || type == JUDGES)
		{
			pairJudges(round, 'A');
			
			if (round.isFlighted())
			{
				pairJudges(round, 'B'); 
				
				//Sort debates in flight B by judge
				sortDebates(round);
			}
		}
		
		//Next, if applicable, pair rooms 
		if (type == ALL_THREE || type == JUDGES_AND_ROOMS || type == ROOMS)
			pairRooms(round);
	}
	



	//---------------------------------------------------------------------------
	// Pairs debates in the given round. 
	//---------------------------------------------------------------------------
	private static void pairDebates (Round round)
	{	
		assert !(round instanceof ElimRound);
		
		//Make array list of all debaters who aren't in a pseudo debate or in a 
		//true debate with both teams or an unlocked true debate.  
		ArrayList<Team> teams = new ArrayList<Team>(); 
		
		for (Entry entry : tournament.getEntries())
		{
			Debate debate = round.getDebate(entry);
			
			//For now, locks are only considered if both teams in a debate are 
			//locked. 
			if (debate == null 
				|| (debate.isTrueDebate() && debate.getOpponent(entry) == null)
				|| (debate.isTrueDebate() && (debate.isLocked(entry) == false 
				|| debate.isLocked(debate.getOpponent(entry)) == false)))
			{
				teams.add(new Team(entry));
			}
		}
		
		//If all debaters are assigned, then don't need to pair anything. 
		if (teams.isEmpty())
			return; 
		
		//If there's an odd number, need to assign a bye
		if (teams.size() % 2 != 0)
			assignBye(teams, round); 
		
		//Make tableau representing conflicts among each combination of unassigned
		//teams 
		int[][] tabOrig = makeTableau(round, teams); 
		
		//Copy to new tableau
		int[][] tabFinal = new int[tabOrig.length][tabOrig[0].length]; 
		for (int i = 0; i < tabOrig.length; i++)
			for (int j = 0; j < tabOrig[0].length; j++)
				tabFinal[i][j] = tabOrig[i][j]; 
		
		//Solve the tableau 
		LOP lop = new LOP(tabFinal, tabFinal[0].length - 2); 
		DepthFirstSolver solver = new DepthFirstSolver();
		tabFinal = ((LOP) solver.solve(lop)).getTableau();
		
		//Obtain a series of debates from this tableau 
		addDebates (round, teams, tabFinal, tabOrig); 
	}
	



	//---------------------------------------------------------------------------
	// Assigns all debates, based on seeding. 
	//---------------------------------------------------------------------------
	private static void pairElimDebates (ElimRound round) 
	{
		//Remove everything from itemsOnPairings. Iterates backwards to avoid 
		//concurrent modification exception. 
		ArrayList<Flightable> items = round.getItemsOnPairings(); 
		for (int i = items.size() - 1; i >= 0; i--)
		{
			if (items.get(i) instanceof Debate)
				round.removeDebate((Debate) items.get(i)); 

			items.remove(i);
		}
		
		//Get array containing all the seeds in this round, in order. 
		ArrayList<Entry> seeds = Sort.seed (round); 
		
		int i = 0; 
		
		//Cycle through the maximum number of debates there could be in this 
		//elim (e.g., 4 for quarters)
		while (i < round.getLevel().getNumEntries() / 2)
		{
			Entry team1 = seeds.get(i);
			int nComp = Sort.getComplementNumber (round.getLevel(), i); 
			Entry team2 = seeds.size() > nComp ? seeds.get(nComp) : null; 
			
			if (team1 != null && team2 != null) 
			{
				Debate debate = new Debate(round); 
				debate.setTeam(team1, 0); 
				debate.setTeam(team2, 1); 
				round.addDebate(debate); 
				round.getItemsOnPairings().add(debate); 
				
				//Set sides, if the debaters have hit each other before
				setSidelock (debate); 
			}
			//If there isn't a "complement" in the round, the team has a bye.  
			else if (team1 != null)
			{
				Debate bye = new Debate(round); 
				bye.setTeam(team1, 0);
				bye.setDecision(0, Debate.Outcome.BYE);  
				round.addDebate(bye); 
			}
		
			i++; 
		}
		
	}


	//---------------------------------------------------------------------------
	// Assigns judges to all true debates currently assigned to this flight.
	//---------------------------------------------------------------------------
	private static void pairJudges (Round round, char flight)
	{
		//Make tableau representing conflicts among each combination of judges and
		//debates 
		int[][] jTabOrig = makeJudgeTableau (round, flight); 

		//Copy to new tableau 
		int[][] jTabFinal = new int[jTabOrig.length][jTabOrig[0].length];
		for (int i = 0; i < jTabOrig.length; i++)
			for (int j = 0; j < jTabOrig[0].length; j++)
				jTabFinal[i][j] = jTabOrig[i][j]; 

		//Find number of debates and judges in this round, just to determine the 
		//number of problem variables in the LOP 
		int numdebates = round.getTrueDebates(flight).size(); 
		int numjudges = 0; 
		for (Judge judge : tournament.getJudges())
			if (judge.getPriority(round) != Priority.PriorityLevel.Unavailable)
				numjudges++; 
		
		//Solve the tableau 
		LOP jLop = new LOP(jTabFinal, numdebates * numjudges); 
		DepthFirstSolver solver = new DepthFirstSolver();
		boolean success = false; 
		try
		{
			jTabFinal = ((LOP) solver.solve(jLop)).getTableau(); 
			success = true; 
		}
		catch (NullPointerException e)
		{
			JOptionPane.showMessageDialog (null, "There are not enough judges "
			 + "available to judge this round - either\nadd more judges or make "
			 + "the round flighted.", "Pair Round", JOptionPane.ERROR_MESSAGE); 
			return;
		}

		//Set judges based on this tableau 
		if (success)
		{
			//Remove all judges currently assigned to rounds 
			for (Flightable container : round.getItemsOnPairings(flight))
			{
				if (container instanceof JudgeInhabitable)
					((JudgeInhabitable) container).removeJudges();
			}
		
			setJudges (round, flight, jTabFinal, jTabOrig); 
		}
	}
	


	//---------------------------------------------------------------------------
	// Assigns rooms to all true debates with a judge currently assigned to this
	// round.
	//---------------------------------------------------------------------------
	private static void pairRooms (Round round)
	{
		//Make tableau representing conflicts among each room and judge 
		int[][] tableauOrig = makeRoomsTableau(round); 

		//Copy to new tableau 
		int[][] tableauFinal = new int[tableauOrig.length][tableauOrig[0].length];
		for (int i = 0; i < tableauOrig.length; i++)
			for (int j = 0; j < tableauOrig[0].length; j++)
				tableauFinal[i][j] = tableauOrig[i][j]; 

		//Find number of debates and judges in this round, just to determine the 
		//number of problem variables in the LOP 
		int numjudges = round.getNumAssignedJudges(); 
		int numrooms = 0; 
		for (Room room : tournament.getRooms())
			if (room.getPriority(round) != Priority.PriorityLevel.Unavailable)
				numrooms++; 
		
		//Solve the tableau 
		LOP lop = new LOP(tableauFinal, numjudges * numrooms); 
		DepthFirstSolver solver = new DepthFirstSolver();
		boolean success = false; 
		try
		{
			tableauFinal = ((LOP) solver.solve(lop)).getTableau(); 
			success = true; 
		}
		catch (NullPointerException e)
		{
			JOptionPane.showMessageDialog (null, "There are not enough rooms "
			 + "available for this round - either\nadd more rooms or make "
			 + "the round flighted.", "Pair Round", JOptionPane.ERROR_MESSAGE); 
			return;
		}
	
		//Set rooms based on this tableau 
		if (success)
		{
			//Remove all rooms currently assigned to rounds 
			for (Flightable container : round.getItemsOnPairings())
			{
				if (container instanceof RoomInhabitable)
					((RoomInhabitable) container).setRoom(null);
			}
		
			setRooms (round, tableauFinal, tableauOrig); 
		}
	}
	
	
	
	
	//---------------------------------------------------------------------------
	// Makes a Simplex tableau representing the values of conflicts between 
	// different teams. 
	//---------------------------------------------------------------------------
	private static int[][] makeTableau (Round round, ArrayList<Team> teams)
	{
		int num = teams.size();
		int nmatch = choose2(num); //number of possible matchups
		int[][] tableau = new int[num+1][nmatch+2];

		//Far right column of tableau is all 1's 
		for (int i = 0; i < num; i++)
			tableau[i][nmatch+1] = 1;
		
		//Make z entry
		tableau[num][nmatch] = 1;
		
		//Add a 1 in each column for the entry that comes first 
		int n = num - 1; //number of column matchups with ones in each row
		int i = 0; //column index
		int count = 0; //number of 1s in this row 
			
		for (int j = 0; j < nmatch; j++)
		{
			tableau[i][j] = 1;
			count++;

			if (count == n)
			{
				i++;
				n--;
				count = 0;
			}
		}

		//Add a 1 in each column for each subsequent entry 
		i = 1; 
		int startRow = 1; 
		
		for (int j = 0; j < nmatch; j++)
		{
			tableau[i][j] = 1; 
			i++; 
			
			if (i == num)
			{
				startRow++; 
				i = startRow;
			}
		}

		//Add "costs" resulting from conflicts at bottom 
		ConflictChecker.setRound(round);
		
		for (int j = 0; j < nmatch; j++)
		{
			//Get the two teams in this column, compare conflicts of their matchup
			int[] indices = getTeamIndices(tableau, j);
			Team team1 = teams.get(indices[0]);
			Team team2 = teams.get(indices[1]);
			Conflict[] conflicts = ConflictChecker.checkForConflict(team1.entry, 
																					team2.entry);
			
			int cost = 0; 
			
			for (int k = 0; k < conflicts.length; k++)
				cost += conflicts[k].problem.value; 
			
			tableau[num][j] = cost;
		}

		
		return tableau; 
	}





	//---------------------------------------------------------------------------
	// Makes a Simplex tableau representing the values of conflicts with judges
	// judging various debates. Each row (constraint) represents either a debate
	// or a judge - debates come first (because they're equality), then judges 
	// (which are inequality). 
	// 
	// The system of indexing each column is that the matchup of debate n and 
	// judge m (where n and m are index starting at 0) is column j = n*numjudges
	// + m. 
	//---------------------------------------------------------------------------
	private static int[][] makeJudgeTableau (Round round, char flight)
	{
		ArrayList<Debate> debates = round.getTrueDebates(flight); 
		
		//Make list of judges available for this round 
		ArrayList<Judge> judges = new ArrayList<Judge>(); 
		for (Judge judge : tournament.getJudges())
			if (judge.getPriority(round) != Priority.PriorityLevel.Unavailable)
				judges.add(judge); 
		
		int nconsts = debates.size() + judges.size(); 
		int nvars = debates.size() * judges.size(); //number of possible matchups
		int nslacks = judges.size(); //slack variables (judges are inequalities)
		int[][] tableau = new int[nconsts+1][nvars+nslacks+2]; 
		
		//Far right column of tableau is all 1's for judges, and equal to number 
		//of judges per debate for debates 
		for (int i = 0; i < debates.size(); i++)
			tableau[i][tableau[i].length-1] = round.getNumJudges();
		for (int i = debates.size(); i < nconsts; i++)
			tableau[i][tableau[i].length-1] = 1; 
		
		//Make z entry
		tableau[nconsts][nvars+nslacks] = 1;
		
		//Add 1's for debates 
		int i = 0; 
		int count = 0; 
		
		for (int j = 0; j < nvars; j++)
		{
			//move down to the next row when we've made one for each judge
			if (count == judges.size())
			{
				count = 0; 
				i++;
			}
		 
			tableau[i][j] = 1; 
			count++; 
		}
		
		//Add 1's for judges 
		for (i = debates.size(); i < nconsts; i++)
		{
			int j = i - debates.size(); 
			
			while (j < nvars)
			{
				tableau[i][j] = 1; 
				j += judges.size(); 
			}
			
			//add slack var 
			tableau[i][nvars+i-debates.size()] = 1;
		}

		//Add "costs" resulting from conflicts at bottom 
		ConflictChecker.setRound(round);
		
		for (int j = 0; j < nvars; j++)
		{
			//Get the judge and debate in this column, compare conflicts of their 
			//matchup 
			Debate debate = debates.get(getDebateIndex(tableau, j)); 
			Judge judge = judges.get(getJudgeIndex(tableau, j, debates.size()));
			Conflict[] conflicts = ConflictChecker.checkForConflict(
				debate.getTeam1(), debate.getTeam2(), debate.isSidelocked(), judge);
			
			int cost = 0; 
			
			for (int k = 0; k < conflicts.length; k++)
				cost += conflicts[k].problem.value; 
			
			tableau[nconsts][j] = cost;
		}
		
		return tableau; 
	}



	//---------------------------------------------------------------------------
	// Creates a simplex tableau for pairing rooms to judges. Each row 
	// (constraint) represents either a judge or a room - judges come first 
	// (because they're equality), then rooms (which are inequality). 
	// 
	// The system of indexing each column is that the matchup of judge n and 
	// room m (where n and m are index starting at 0) is column j = n*numrooms
	// + m. 
	// 
	// Note that this assumes that there is only a single judge per each 
	// debate this round. It ignores debates that do not already have two teams 
	// and a judge. 
	//---------------------------------------------------------------------------
	private static int[][] makeRoomsTableau (Round round)
	{
		//Make list of judges who are assinged to a complete debate (i.e. one with
		//two debaters assigned) 
		ArrayList<Judge> judges = new ArrayList<Judge>(); 
		for (Debate debate : round.getTrueDebates())
		{
			if (debate.getTeam1() != null && debate.getTeam2() != null 
				&& debate.getJudges().size() == 1 
				&& judges.contains(debate.getJudge()) == false)
			{
				judges.add(debate.getJudge()); 				
			}
		}
				
		//Make list of rooms available for this round 
		ArrayList<Room> rooms = new ArrayList<Room>(); 
		for (Room room : tournament.getRooms())
			if (room.getPriority(round) != Priority.PriorityLevel.Unavailable)
				rooms.add(room); 
		
		int nconsts = judges.size() + rooms.size(); 
		int nvars = judges.size() * rooms.size(); //number of possible matchups 
		int nslacks = rooms.size(); //slack variables (rooms are inequalities) 
		int[][] tableau = new int[nconsts+1][nvars+nslacks+2];
		
		//Far right column of tableau is all 1's 
		for (int i = 0; i < nconsts; i++)
			tableau[i][tableau[i].length-1] = 1; 

		//Make z entry
		tableau[nconsts][nvars+nslacks] = 1;
		
		//Add 1's for judges 
		int i = 0; 
		int count = 0; 
		
		for (int j = 0; j < nvars; j++)
		{
			//move down to next row when we've made one for each room 
			if (count == rooms.size())
			{
				count = 0; 
				i++;
			}
			
			tableau[i][j] = 1;
			count++;
		}
		
		//Add 1's for rooms 
		for (i = judges.size(); i < nconsts; i++)
		{
			int j = i - judges.size();
			
			while (j < nvars)
			{
				tableau[i][j] = 1;
				j += rooms.size();
			}
			
			//Add slack var 
			tableau[i][nvars+i-judges.size()] = 1;
		}
		
		//Add "costs" resulting from conflicts at bottom 
		ConflictChecker.setRound(round);
		
		for (int j = 0; j < nvars; j++)
		{
			//Get the judge and room in this column, compare conflicts of their 
			//matchup 
			Judge judge = judges.get(getJudgeIndex(tableau, j));
			Room room = rooms.get(getRoomIndex(tableau, j, judges.size())); 
			Conflict conflicts[] = ConflictChecker.checkForConflict(judge, room); 
			
			int cost = 0; 
			
			for (int k = 0; k < conflicts.length; k++)
				cost += conflicts[k].problem.value;
			
			tableau[nconsts][j] = cost; 
		}
		
		return tableau;
	}
	
	
	
	
	//Implements nCr where r = 2
	private static int choose2 (int n)
	{
		return n * (n - 1) / 2; 
	}
	


	
	
	//---------------------------------------------------------------------------
	// Returns the index of the two teams represented by the matchup in this 
	// column. I.e., returns the indices of the two rows that contain 1's in 
	// this column. NOTE: This only applies to the ORIGINAL tableau. 
	//---------------------------------------------------------------------------
	private static int[] getTeamIndices (int[][] tableau, int column)
	{
		int[] indices = new int[2]; 
		
		int i = 0; 
		int count = 0; 
		
		while (count < 2 && i < tableau.length - 1)
		{
			if (tableau[i][column] == 1)
			{
				indices[count] = i;
				count++;
			}
			
			i++;
		}
		
		return indices; 
	}



	//---------------------------------------------------------------------------
	// Returns the index of the debate represented by the matchup in this column.
	// I.e., returns the index of the first row that contains a 1 in this column.
	// NOTE: This only applies to the ORIGINAL tableau. 
	//---------------------------------------------------------------------------
	private static int getDebateIndex (int[][] tableau, int column)
	{
		int i = 0; 
		
		while (i < tableau.length - 1)
		{
			if (tableau[i][column] == 1)
			{
				return i;
			}
			
			i++;
		}
		
		return -1; //error - shouldn't be reached.  
	}


	//---------------------------------------------------------------------------
	// Returns the index of the judge represented by the matchup in this column.
	// I.e. returns the index of the second row that contains a 1 in this column.  
	// FirstJudgeRow is the index of the first row that contains a judge (as 
	// opposed to a debate). NOTE: This only applies to the ORIGINAL tableau. 
	// This method is only used when assigning judges - when assigning rooms, 
	// use the other overloaded version of this method. 
	//---------------------------------------------------------------------------
	private static int getJudgeIndex (int[][] tableau, int column, 
													int firstJudgeRow)
	{
		int i = firstJudgeRow; 
		
		while (i < tableau.length - 1)
		{
			if (tableau[i][column] == 1)
			{
				return i - firstJudgeRow;
			}
			
			i++;
		}
		
		return -1; //error - shouldn't be reached.  
	}




	//---------------------------------------------------------------------------
	// Returns the index of the judge represented by the matchup in this column.
	// I.e., returns the index of the first row that contains a 1 in this column.
	// NOTE: This only applies to the ORIGINAL tableau. It should only be used
	// when pairing rooms, not when pairing judges (for those, use the overloaded
	// version of this method instead). 
	//---------------------------------------------------------------------------
	private static int getJudgeIndex (int[][] tableau, int column)
	{
		int i = 0; 
		
		while (i < tableau.length - 1)
		{
			if (tableau[i][column] == 1)
			{
				return i;
			}
			
			i++;
		}
		
		return -1; //error - shouldn't be reached.  
	}


	//---------------------------------------------------------------------------
	// Returns the index of the room represented by the matchup in this column.
	// I.e. returns the index of the second row that contains a 1 in this column.  
	// FirstRoomRow is the index of the first row that contains a room (as 
	// opposed to a judge). NOTE: This only applies to the ORIGINAL tableau. 
	//---------------------------------------------------------------------------
	private static int getRoomIndex (int[][] tableau, int column, 
													int firstRoomRow)
	{
		int i = firstRoomRow; 
		
		while (i < tableau.length - 1)
		{
			if (tableau[i][column] == 1)
			{
				return i - firstRoomRow;
			}
			
			i++;
		}
		
		return -1; //error - shouldn't be reached.  
	}


	//---------------------------------------------------------------------------
	// Given an iloptimal Simplex tableau, calculates the matchups it refers to 
	// and makes those into actual debates to be added to the round. 
	//---------------------------------------------------------------------------
	static void addDebates (Round round, ArrayList<Team> teams, int[][] tableau, 
									int[][] origTableau)
	{
		//Remove everything except locked true debates (i.e. ones that are already
		//assigned) from itemsOnPairings. Iterates backwards to avoid weird 
		//behavior when removing from a list while iterating over it. 
		ArrayList<Flightable> items = round.getItemsOnPairings(); 
		for (int i = items.size() - 1; i >= 0; i--)
		{
			if (!(items.get(i) instanceof Debate 
				&& ((Debate) items.get(i)).isTrueDebate() 
				&& ((Debate) items.get(i)).getTeam1() != null
				&& ((Debate) items.get(i)).getTeam2() != null
				&& ((Debate) items.get(i)).isLocked(0)
				&& ((Debate) items.get(i)).isLocked(1)))
			{
				if (items.get(i) instanceof Debate)
					round.removeDebate((Debate) items.get(i)); 
	
				items.remove(i);
			}
		}
	
		//Cycle through each row of the tableau 
		for (int i = 0; i < tableau.length - 1; i++)
		{
			int basic = -1; 
			
			//Get column variable that is basic in that row 
			for (int j = 0; j < tableau[0].length - 2; j++)
			{
				if (isBasicVariable(tableau, i, j) 
					&& tableau[i][tableau[i].length-1] != 0)
				{
					basic = j; 
					break;
				}
			}
			
			//If no variable is basic, go to next row
			if (basic == -1)
				continue; 
			
			//Get indices of teams who are represented in the debate in this column
			int[] indices = getTeamIndices (origTableau, basic); 
			Team team1 = teams.get(indices[0]);
			Team team2 = teams.get(indices[1]); 
			
			//Create new debate containing these teams 
			Debate debate = new Debate (round);
			debate.setTeam(team1.entry, 0);
			debate.setTeam(team2.entry, 1);
			
			//Get the side these seeds should be on 
			setSides (debate);
			
			//Add debate to round and items on pairings 
			round.addDebate (debate); 
			round.getItemsOnPairings().add(debate); 
		}
		
		//If round is flighted, set half the new debates to flight B 
		if (round.isFlighted())
		{
			for (int i = round.getTrueDebates().size() / 2; 
				i < round.getTrueDebates().size(); i++)
			{
				round.getTrueDebates().get(i).setFlight('B');
			}
		}
	}



	//---------------------------------------------------------------------------
	// Given a debate, sets the side that the debaters should be on. 
	// Note: This will only be called on true debates with two non-null entries.
	//---------------------------------------------------------------------------
	private static void setSides (Debate debate)
	{
		Round round = debate.getRound(); 
		Entry team1 = debate.getTeam1();
		Entry team2 = debate.getTeam2();
	
		//elims are flip for sides anyway 
		if (round instanceof ElimRound)
			return; 
		
		//If they've debated each other before, they should switch sides 
		if (team1.hasFaced(team2, round))
		{
			ArrayList<Debate> prevDebates = new ArrayList<Debate>(); 
			
			for (Debate deb : tournament.getDebates(team1))
			{
				if (deb.getOpponent(team1) != null
					&& deb.getOpponent(team1).equals(team2))
				{
					prevDebates.add(deb);
				}
			}
			
			//net number of times team1 has been aff 
			int aff = 0; 
			
			for (Debate deb : prevDebates)
			{
				if (deb.getAff() != null && deb.getAff().equals(team1))
					aff++; 
				else if (debate.getAff() != null && deb.getAff().equals(team2))
					aff--; 
			}
			
			if (aff > 0)
			{
				debate.setAff (team1); 
				return;
			}
			if (aff < 0)
			{
				debate.setAff (team2); 
				return;
			}
		}
		
		//Otherwise, if they're due for different sides, that's what it should be.
		if (team1.getSideDueFor(round) == Side.AFF 
			&& team2.getSideDueFor(round) != Side.AFF)
		{
			debate.setAff (team1);
			return;
		}
		if (team2.getSideDueFor(round) == Side.AFF 
			&& team1.getSideDueFor(round) != Side.AFF)
		{
			debate.setAff (team2);
			return;
		}
		if (team1.getSideDueFor(round) == Side.NEG 
			&& team2.getSideDueFor(round) != Side.NEG)
		{
			debate.setAff (team2);
			return;
		}
		if (team2.getSideDueFor(round) == Side.NEG 
			&& team1.getSideDueFor(round) != Side.NEG)
		{
			debate.setAff (team1);
			return;
		}

		//If it gets to this point, just arbitrarily pick an affirmative. 
		double rand = Math.random(); 
		debate.setAff(rand < .5 ? team1 : team2);
	}

			

	//---------------------------------------------------------------------------
	// For an elim round debate, determines if it needs to be sidelocked based on
	// whether the teams have hit each other before. 
	//---------------------------------------------------------------------------
	private static void setSidelock (Debate debate)
	{
		Entry team1 = debate.getTeam1();
		Entry team2 = debate.getTeam2();

		//this int is the net number of times that team1 has debated team1 on the 
		//aff. E.g., if they've debated thrice and team1 was aff twice, it would 
		//equal 1. 
		int net = 0; 
		
		//find all prelim debates (if any) they've had against each other 
		for (Round rd : tournament.getPrelims())
		{
			Debate deb = rd.getDebate(team1);
			
			//increment net depending on what side team1 was if they debated each
			//other. 
			if (deb != null && deb.getOpponent(team1) != null 
				&& deb.getOpponent(team1).equals(team2))
			{
				if (deb.getAff() != null && deb.getAff().equals(team1))
					net++;
				if (deb.getNeg() != null && deb.getNeg().equals(team1))
					net--; 
			}
		}
		
		if (net > 0)
			debate.switchSides(); 
		
		if (net != 0)
			debate.setSpecialSidelocked(true);
	}


	//---------------------------------------------------------------------------
	// Given a final solved tableau of judge/debate conflicts, assigns judges to
	// debates. 
	//---------------------------------------------------------------------------
	private static void setJudges (Round round, char flight, int[][] tableau, 
											int[][] origTableau)
	{
		ArrayList<Debate> debates = round.getTrueDebates(flight); 
		
		//Make list of judges available for this round 
		ArrayList<Judge> judges = new ArrayList<Judge>(); 
		for (Judge judge : tournament.getJudges())
			if (judge.getPriority(round) != Priority.PriorityLevel.Unavailable)
				judges.add(judge); 
		
		//Cycle through each row of the tableau 
		for (int i = 0; i < tableau.length - 1; i++)
		{
			int basic = -1; 
			
			//Get column variable that is basic in that row 
			for (int j = 0; j < debates.size() * judges.size(); j++)
			{
				if (isBasicVariable(tableau, i, j) 
					&& tableau[i][tableau[i].length-1] != 0)
				{
					basic = j; 
					break;
				}
			}
			
			//If no variable is basic, go to next row
			if (basic == -1)
				continue; 

			//Get judge and debate who are represented by this basic variable 
			Debate debate = debates.get(getDebateIndex(origTableau, basic)); 
			Judge judge = judges.get(getJudgeIndex(origTableau, basic, debates.size()));

			//Set this as (one of) the judge(s) of this debate 
			debate.addJudge(judge); 
		}
	}




	//---------------------------------------------------------------------------
	// Given a final tableau of solved judge/room matchups, assigns rooms to 
	// debates. 
	//---------------------------------------------------------------------------
	private static void setRooms(Round round, int[][] tableau, 
												int[][] origTableau)
	{
		//Make list of judges who are assigned to a complete debate (i.e. one with
		//two debaters assigned). Note that this list as well as the subsequent 
		//rooms list is the same as the one the indices of the tableaux refer to.
		ArrayList<Judge> judges = new ArrayList<Judge>(); 
		for (Debate debate : round.getTrueDebates())
		{
			if (debate.getTeam1() != null && debate.getTeam2() != null 
				&& debate.getJudges().size() == 1 
				&& judges.contains(debate.getJudge()) == false)
			{
				judges.add(debate.getJudge()); 				
			}
		}
		
		//Make list of rooms available for this round 
		ArrayList<Room> rooms = new ArrayList<Room>(); 
		for (Room room : tournament.getRooms())
			if (room.getPriority(round) != Priority.PriorityLevel.Unavailable)
				rooms.add(room); 
		
		//Cycle through each row of the tableau 
		for (int i = 0; i < tableau.length - 1; i++)
		{
			int basic = -1; 
			
			//Get column variable that is basic in that row 
			for (int j = 0; j < judges.size() * rooms.size(); j++)
			{
				if (isBasicVariable(tableau, i, j) 
					&& tableau[i][tableau[i].length-1] != 0)
				{
					basic = j; 
					break;
				}
			}
			
			//If no variable is basic, go to next row
			if (basic == -1)
				continue; 

			//Get judge and room that are represented by this basic variable 
			Judge judge = judges.get(getJudgeIndex(origTableau, basic)); 
			Room room = rooms.get(getRoomIndex(origTableau, basic, judges.size()));
			
			//Set this as the room for the debate(s) this judge is judging 
			Debate debate1 = round.getDebate(judge, 'A');
			
			if (debate1 != null)			
				debate1.setRoom(room); 
			
			if (round.isFlighted())
			{
				Debate debate2 = round.getDebate(judge, 'B');
				
				if (debate2 != null)			
					debate2.setRoom(room); 
			}
		}
		
	}
	

	

	//---------------------------------------------------------------------------
	// Randomly assigns a bye to a team in this round. The arraylist teams 
	// contains only teams that are not currently assigned and are in a position
	// to be randomly assigned to a debate/bye. 
	//---------------------------------------------------------------------------
	private static void assignBye (ArrayList<Team> teams, Round round)
	{
		//Don't do anything if there's an even number of teams
		if (teams.size() % 2 == 0)
			return; 
		
		//Create new array list containing the teams eligible to be chosen for 
		//a bye 
		ArrayList<Team> potentialByes = new ArrayList<Team>();
		for (Team team : teams)
		{
			if (team.entry.hasHadBye() == false 
				&& team.entry.getWins() < team.entry.getLosses())
			{
				potentialByes.add(team); 
			}
		}
		
		//In the event that every team has had a bye or losing record, choose one
		//at random 
		if (potentialByes.isEmpty())
		{
			for (Team team : teams)
				potentialByes.add(team); 
		}
		
		//Randomly choose one 
		int n = (int) Math.floor(Math.random() * potentialByes.size()); 
		
		Debate bye = new Debate(round); 
		bye.setTeam(potentialByes.get(n).entry, 0); 
		bye.setDecision(0, Debate.Outcome.BYE); 
		
		//Remove bye from teams arraylist 
		for (int i = 0; i < teams.size(); i++)
			if (teams.get(i).entry.equals(potentialByes.get(n).entry))
				teams.remove(i); 
	}



	//---------------------------------------------------------------------------
	// Determines whether there is a basic variable in the given column with its
	// coefficient in the given row. I.e., returns "true" if entry (row, column)
	// is the only nonzero entry in the given column. (Be careful- it will still
	// return true if the column is the z-column or the farthest left column,
	// even though these are not truly basic variables.)
	//---------------------------------------------------------------------------
	private static boolean isBasicVariable (int[][] tableau, int row, int column)
	{
		//Check that entry (row, column) is nonzero
		if (tableau[row][column] == 0)
			return false;

		//Check that all other entries in the column (excluding objective row)
		//are nonzero
		for (int i = 0; i < tableau.length - 1; i++)
		{
			//Skip the given row
			if (i == row)
				continue;
		
			if (tableau[i][column] != 0)
				return false;
		}

		return true;
	}

	

	//---------------------------------------------------------------------------
	// Sorts the debates in flight B so that the judges appear in the same order
	// on the pairings in flight A and B, to make it easier to read. (Note: for 
	// paneled rounds, this will just base it on the first judge.) 
	//---------------------------------------------------------------------------
	private static void sortDebates (Round round)
	{
		//Remove all debates in flight B from items on pairings 
		ArrayList<Debate> BDebates = new ArrayList<Debate>();  
		
		for (int i = round.getItemsOnPairings().size() - 1; 
			i >= 0 && round.getItemsOnPairings().get(i).getFlight() == 'B'; i--)
		{
			if (round.getItemsOnPairings().get(i) instanceof Debate)
			{
				BDebates.add ((Debate) round.getItemsOnPairings().get(i)); 
				round.getItemsOnPairings().remove(i);
			}
		}
		
		//Remove flight B debates from "Debates", then re-add them in order. (This
		//needs to be done after creating the BDebates array and before it is 
		//emptied in the subsequent loop.)
		for (int i = round.getDebates().size() - 1; i >= 0; i--)
			if (round.getDebates().get(i).getFlight() == 'B')
				round.getDebates().remove(i);
		
		//Need to add debates back in backwards because they're backwards in 
		//"BDebates"
		for (int i = BDebates.size() - 1; i >= 0; i--)
			round.getDebates().add(BDebates.get(i)); 
		
		//Re-add them in the order of their judge in flight A
		int i = 0; 
		
		while (i < round.getDebates('A').size())
		{
			Judge judge = round.getDebates('A').get(i).getJudge(); 
			if (judge != null)
			{
				Debate deb = getDebateWithJudge(BDebates, judge);

				if (deb != null)
				{
					round.getItemsOnPairings().add(deb); 
					BDebates.remove(deb);
				}
			}
			
			i++;
		}
		
		//Re-add any debates whose judge is not in flight A (i.e. who are still
		//remaining in BDebates)
		for (Debate deb : BDebates)
			round.getItemsOnPairings().add(deb);
	}


	//---------------------------------------------------------------------------
	// Returns the debate in the given arraylist that contains the given judge. 
	// If none, returns null. 
	//---------------------------------------------------------------------------
	private static Debate getDebateWithJudge (ArrayList<Debate> debates, 
															Judge judge)
	{
		for (Debate debate : debates)
			if (debate.hasJudge(judge))
				return debate; 
		
		return null; 
	}


	//---------------------------------------------------------------------------
	// Special verison of entry used just by Pair 
	//---------------------------------------------------------------------------
	private static class Team
	{
		Entry entry;
//		char flight;
//		Side side;

		//Constructor 
		Team (Entry e)
		{
			entry = e;
		}

	}



}

































