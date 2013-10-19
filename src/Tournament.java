//*****************************************************************************
// Tournament.java
// Kevin Coltin
//  
// Represents a single tournament. As of now, each tournament may only contain
// a single event, either LD or PFD. 
//*****************************************************************************

 
 

import java.io.*; 
import java.util.ArrayList; 
import javax.swing.JOptionPane; 

class Tournament implements Serializable 
{
	private static final long serialVersionUID = -48235904851L; 
	
	//Event of the tournament - either LD or PFD
	private final Event event; 
	
	private String name; 
	
	//This indicates whether the user has decided yet what level to break to - 
	//i.e. quarters, semis, etc. If it is true, outrounds will be named 
	//"Quarters" etc. and can be paired, but if not, they will just be called
	//"Elim round 1" etc. 
	private boolean breakLevelSet; 
	
	//indicates whether it's a clean break, as opposed to breaking exactly 4, 8, 
	//16, etc. 
	private boolean cleanBreak; 
	
	private ArrayList<School> schools; 
	private ArrayList<Entry> entries; 
 	private ArrayList<Judge> judges; 
	private ArrayList<Room> rooms; 
	private ArrayList<PracticeRound> practices; 
	private ArrayList<PrelimRound> prelims; 
	private ArrayList<ElimRound> elims; 
	private ArrayList<Entry> breaks; //teams who broke to elims, in seed order. 
	
	//Codes used for writing tournament to file 
	private static final String EVENT = "01", NAME = "02", 
							BREAK_LEVEL_SET = "03", CLEAN_BREAK = "04", SCHOOLS = "05",
							ENTRIES = "06", JUDGES = "07", ROOMS = "08", 
							PRACTICES = "09", PRELIMS = "10", ELIMS = "11", 
							BREAKS = "12"; 
	
	//--------------------------------------------------------------------------
	// Constructor 
	//--------------------------------------------------------------------------
	Tournament (Event e, String n)
	{
		event = e; 
		name = n; 
		breakLevelSet = false; 
		cleanBreak = false; 
		
		schools = new ArrayList<School>(); 		
		entries = new ArrayList<Entry>();
		judges = new ArrayList<Judge>();
		rooms = new ArrayList<Room>();
		practices = new ArrayList<PracticeRound>();
		prelims = new ArrayList<PrelimRound>();
		elims = new ArrayList<ElimRound>();
		breaks = new ArrayList<Entry>(); 
	}
	
	

	//--------------------------------------------------------------------------
	// Accessors and mutators 
	//--------------------------------------------------------------------------
	String getName()
	{
		return name; 
	}
	
	Event getEvent ()
	{
		return event;
	}
	
	boolean isBreakLevelSet ()
	{
		return breakLevelSet;
	}
	
	void setName (String n)
	{
		name = n;
	}
	
	//Sets the level of outround to break to, and changes it accordingly in each
	//outround in order. If the argument is null, it resets outrounds to just be
	//a number. 
	void setBreakLevel (Outround level)
	{
		if (level == null)
		{
			breakLevelSet = false; 
			
			for (int i = 0; i < elims.size(); i++)
				elims.get(i).setNumber(i + 1); 
		}
		else
		{
			breakLevelSet = true;
			
			int i = 0;
			int levnum = level.ordinal(); 
			
			while (i < elims.size())
			{
				elims.get(i).setLevel(Outround.values()[levnum]); 
				i++; 
				levnum++; 
			}
		}
	}
	
	boolean isCleanBreak ()
	{
		return cleanBreak; 
	}
	
	void setCleanBreak (boolean clean)
	{
		cleanBreak = clean; 
	}
	
	//Note: the breaks array list will always be in order of seed. I.e., 
	//breaks.get(0) is the top seed after prelims, etc. 
	ArrayList<Entry> getBreaks()
	{
		return breaks;
	}
	
	void setBreaks(ArrayList<Entry> brks)
	{
		breaks = brks;
	}
	



	//--------------------------------------------------------------------------
	// Returns an array list consisting of all types of rounds. 
	//--------------------------------------------------------------------------
	ArrayList<Round> getRounds()
	{
		ArrayList rounds = new ArrayList(); 
		
		for (PracticeRound round : practices)
			rounds.add(round); 

		for (PrelimRound round : prelims)
			rounds.add(round); 

		for (ElimRound round : elims)
			rounds.add(round); 
	
		ArrayList<Round> allRounds = (ArrayList<Round>) rounds;
		return allRounds;		
	}
	

	//--------------------------------------------------------------------------
	// Methods to access, add, and remove from all arrays 
	//--------------------------------------------------------------------------
	ArrayList<School> getSchools()
	{
		return schools; 
	}
	
	ArrayList<Entry> getEntries()
	{
		return entries; 
	}
	
	ArrayList<Competitor> getCompetitors()
	{
		ArrayList<Competitor> competitors = new ArrayList<Competitor>(); 
		
		for (Entry entry : entries)
		{
			Competitor[] students = entry.getStudents(); 
			
			if (students[0] != null)
				competitors.add(students[0]); 
			if (students[1] != null)
				competitors.add(students[1]); 
		}
		
		Sort.sort(competitors); 		
		return competitors; 
	}
	
	
	ArrayList<Judge> getJudges()
	{
		return judges; 
	}
	
	ArrayList<Room> getRooms()
	{
		return rooms; 
	}
	
	ArrayList<PracticeRound> getPractices()
	{
		return practices;
	}
	
	ArrayList<PrelimRound> getPrelims()
	{
		return prelims; 
	}
	
	ArrayList<ElimRound> getElims()
	{
		return elims;
	}
	
	//Returns all debates for a given entry 
	ArrayList<Debate> getDebates (Entry entry)
	{
		ArrayList<Debate> debates = new ArrayList<Debate>();
		
		for (Round round : getRounds())
		{
			for (Debate debate : round.getDebates())
			{
				if (debate.hasEntry (entry))
				{
					debates.add(debate);
					break; //advance to next round 
				}
			}
		}
		
		return debates; 
	}
	
	//Returns all prelims for a given entry 
	ArrayList<Debate> getPrelims (Entry entry)
	{
		ArrayList<Debate> debates = new ArrayList<Debate>();
		
		for (PrelimRound round : getPrelims())
		{
			for (Debate debate : round.getDebates())
			{
				if (debate.hasEntry (entry))
				{
					debates.add(debate);
					break; //advance to next round 
				}
			}
		}
		
		return debates; 
	}



	
	void addSchool (School school)
	{
		schools.add(school);
		Sort.sort(schools);
	}
	
	void removeSchool (School school)
	{
		Sort.remove(schools, school);
	}
	
	void addJudge (Judge judge)
	{
		judges.add(judge);
		Sort.sort(judges);
	}
	
	void removeJudge (Judge judge)
	{
		Sort.remove(judges, judge);
	}
	
	void addEntry (Entry entry)
	{
		entries.add(entry);
		Sort.sort(entries);
	}
	
	void removeEntry (Entry entry)
	{
		Sort.remove(entries, entry);
	}
	
	void addRoom (Room room)
	{
		rooms.add(room);
		Sort.sort(rooms);
	}
	
	void removeRoom (Room room)
	{
		Sort.remove(rooms, room);
	}	
	
	void addPractice (PracticeRound round)
	{
		practices.add(round);
		Sort.sort(practices);
	}
	
	void removePractice (PracticeRound round)
	{
		Sort.remove(practices, round);
	}

	void addPrelim (PrelimRound round)
	{
		prelims.add(round);
		Sort.sort(prelims);
	}
	
	void removePrelim (PrelimRound round)
	{
		Sort.remove(prelims, round);
	}

	void addElim (ElimRound round)
	{
		elims.add(round);
		Sort.sort(elims);
	}
	
	void removeElim (ElimRound round)
	{
		Sort.remove(elims, round);
	}
	
	void removeRound (Round round)
	{
		if (round instanceof PracticeRound)
			removePractice((PracticeRound) round); 
		else if (round instanceof PrelimRound)
			removePrelim((PrelimRound) round); 
		else if (round instanceof ElimRound)
			removeElim((ElimRound) round); 
	}
	
	//---------------------------------------------------------------------------
	// Gets judges or rooms not assigned to any debates in a round. NOTE: For 
	// double flighted rounds, this returns all judges/rooms who are available 
	// for at least ONE of the two flights.  
	//---------------------------------------------------------------------------
	ArrayList<Judge> getUnassignedJudges (Round round)
	{
		ArrayList<Judge> list = new ArrayList<Judge>(); 
		
		for (Judge judge : judges)
			if (round.isUnassignedEitherFlight(judge))
				list.add(judge);
		
		return list; 
	}

	ArrayList<Room> getUnassignedRooms (Round round)
	{
		ArrayList<Room> list = new ArrayList<Room>(); 
		
		for (Room room : rooms)
			if (round.isUnassignedEitherFlight(room))
				list.add(room);
		
		return list; 
	}


	//--------------------------------------------------------------------------
	// If there is a "gap" between this elim and another existing elim (e.g. if
	// you just created octos and only semis previously existed), this fills in
	// all missing intermediate ones 
	//--------------------------------------------------------------------------
	void fillOutrounds ()
	{
		if (breakLevelSet == false)
			return;
		
		ArrayList<Outround> missingOutrounds = new ArrayList<Outround>();
		
		for (int i = 0; i < elims.size() - 1; i++)
		{
			if (elims.get(i+1).getLevel().ordinal() 
				!= elims.get(i).getLevel().ordinal() + 1)
			{
				missingOutrounds.add (Outround.values()[elims.get(i).getLevel(
																					).ordinal()+1]);
			}
		}
		
		for (Outround outround : missingOutrounds)
		{
			addElim(new ElimRound(this, elims.get(0).getPanel(
															).getTournamentFrame(), outround));
		}
	}


	
	//--------------------------------------------------------------------------
	// Writes the Tournament object to a file to save it
	//--------------------------------------------------------------------------
	void write (File file) throws IOException
	{
		//Make sure it can only be saved to a .tab file 
		if (file.getName().length() < 5 
			|| file.getName().endsWith(".tab") == false)
		{
			JOptionPane.showMessageDialog(null, "Must save tournament to a file "
							+ "ending in \".tab\".", "Save Tournament", 
							JOptionPane.ERROR_MESSAGE);
			return;
		}
	
		FileOutputStream fos = new FileOutputStream (file);
		ObjectOutputStream oos = new ObjectOutputStream (fos);
		oos.writeObject (this);
		oos.close();
	}
	

	//---------------------------------------------------------------------------
	// Writes the tournament to a text file in a form that can be recovered later
	// if serialization errors occur in the main file. 
	//---------------------------------------------------------------------------
	void backup (File file) throws IOException
	{
		//Create print writer 
		PrintWriter outFile = null; 
		
		try
		{
			FileWriter fw = new FileWriter (file);
			BufferedWriter bw = new BufferedWriter (fw);
			outFile = new PrintWriter (bw);
			outFile.print(writeBackup());
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog (null, "Error writing to file  (" 
									+ file.getName() + ").", "Save Backup", 
									JOptionPane.ERROR_MESSAGE); 
			return;
		}
		finally
		{
			if (outFile != null)
				outFile.close();
		}
	}
	
	
	
	//---------------------------------------------------------------------------
	// Recovers a text file from a file 
	//---------------------------------------------------------------------------
	static Tournament recover (File file, TournamentFrame tf)
	{
		BufferedReader inFile = null; 
		String text = "";
		
		try
		{
			FileReader fr = new FileReader (file); 
			inFile = new BufferedReader (fr); 
			
			String line = inFile.readLine(); 
			
			while (line != null)
			{
				text += line + "\n"; 
				line = inFile.readLine(); 
			}
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog (null, "Error recovering from file.", 
												"File Recover", JOptionPane.ERROR_MESSAGE); 
			e.printStackTrace(); 
			return null; 
		}
		finally
		{
			if (inFile != null)
			{
				try
				{
					inFile.close(); 
				}
				catch (IOException e) {}
			}
		}
		
		return recoverBackup (text, tf); 
	}



	//---------------------------------------------------------------------------
	// Creates text form for backing up file. 
	// 
	// NOTE: Each of these items is in this particular order so that each entry
	// will have fields (data members) that consists only of items before it - 
	// e.g., since a PracticeRound contains a Judge and a Room, we have to read
	// in all Judges and Rooms before reading in PracticeRounds, so that is the 
	// order in which they are output. 
	//---------------------------------------------------------------------------
	private String writeBackup ()
	{
		//First, write event and name of tournament  
		String text = EVENT + " " + event.toString() + "\n" 
							+ NAME + " " + name + "\n"; 
		
		//Add whether break level is set 
		text += BREAK_LEVEL_SET + " " + breakLevelSet + "\n" 
					+ CLEAN_BREAK + " " + cleanBreak + "\n"; 
		
		//Add schools 
		for (School school : schools)
			text += SCHOOLS + " " + school.getName() + "\n";
		
		//Add entries 
		for (Entry entry : entries) 
			text += ENTRIES + " " + entry.writeBackup() + "\n";
		
		//Add judges 
		for (Judge judge : judges)
			text += JUDGES + " " + judge.getName() + "\n";
		
		//Add rooms 
		for (Room room : rooms)
			text += ROOMS + " " + room.getName() + "\n"; 
		
		//For each round, add a line with its info 
		for (PracticeRound round : practices)
			text += PRACTICES + " " + round.writeBackup() + "\n";
		for (PrelimRound round : prelims)
			text += PRELIMS + " " + round.writeBackup() + "\n"; 
		for (ElimRound round : elims)
			text += ELIMS + " " + round.writeBackup() + "\n"; 
		
		//Add each debater who broke 
		for (Entry brk : breaks)
			text += BREAKS + " " + brk.toString() + "\n"; 
		
		return text; 
	}



	//---------------------------------------------------------------------------
	// Converts text doc into a tournament. 
	//---------------------------------------------------------------------------
	private static Tournament recoverBackup (String text, TournamentFrame tf)
	{
		//Recover the tournament itself- event and tournament name 
		int start = text.indexOf(EVENT) + EVENT.length() + 1; 
		int end = text.indexOf('\n', start); 
		String eventName = text.substring (start, end); 
		text = text.substring(end + 1);
		
		Event event = null;  
		
		for (Event ev : Event.values())
		{
			if (ev.toString().equals(eventName))
			{
				event = ev;
				break;
			}
		}
		
		//If event doesn't match any, it's probably a bad file. 
		if (event == null)
		{
			JOptionPane.showMessageDialog(null, "Trouble reading the Event of the "
				+ "tournament\nfrom text file - make sure this is a valid\n"
				+ "tournament backup file.", "Recover Backup File", 
				JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		start = text.indexOf(NAME) + NAME.length() + 1;
		end = text.indexOf('\n', start);
		String tournamentName = text.substring (start, end); 
		text = text.substring(end + 1);
		
		Tournament tournament = new Tournament (event, tournamentName); 
		
		//Get boolean args for breaks 
		start = text.indexOf(BREAK_LEVEL_SET) + BREAK_LEVEL_SET.length() + 1; 
		end = text.indexOf('\n', start); 
		String bls = text.substring (start, end); 
		text = text.substring (end + 1); 
		
		//don't do anything with this boolean until later, when adding elims
		boolean breakLevelSet = bls.equalsIgnoreCase("true"); 
		
		start = text.indexOf(CLEAN_BREAK) + CLEAN_BREAK.length() + 1; 
		end = text.indexOf('\n', start); 
		String cb = text.substring(start, end); 
		text = text.substring(end + 1); 
		
		boolean cleanBreak = cb.equalsIgnoreCase("true"); 
		tournament.setCleanBreak(cleanBreak); 
		
		//Get schools 
		while (text.startsWith(SCHOOLS))
		{
			start = SCHOOLS.length() + 1; 
			end = text.indexOf('\n', start); 
			String schoolName = text.substring(start, end); 
			text = text.substring(end + 1); 
			
			tournament.addSchool (new School (schoolName)); 
		}
		
		//Get entries 
		while (text.startsWith(ENTRIES))
		{
			start = ENTRIES.length() + 1; 
			end = text.indexOf('\n', start); 
			String entryInfo = text.substring(start, end); 
			text = text.substring(end + 1); 
			
			tournament.addEntry (Entry.recoverBackup(entryInfo, tournament)); 
		}
		
		//Get judges 
		while (text.startsWith(JUDGES))
		{
			start = JUDGES.length() + 1; 
			end = text.indexOf('\n', start);
			String judgeName = text.substring(start, end); 
			text = text.substring(end + 1); 
			
			tournament.addJudge (new Judge (judgeName, tournament)); 
		}
		
		//Get rooms 
		while (text.startsWith(ROOMS))
		{
			start = ROOMS.length() + 1; 
			end = text.indexOf('\n', start);
			String roomName = text.substring(start, end); 
			text = text.substring(end + 1); 
			
			tournament.addRoom (new Room (roomName, tournament)); 
		}

		//Get rounds 
		while (text.startsWith(PRACTICES))
		{
			start = PRACTICES.length() + 1; 
			end = text.indexOf('\n', start); 
			String practiceInfo = text.substring(start, end); 
			text = text.substring(end + 1); 
			
			tournament.addPractice ((PracticeRound) (Round.recoverBackup(
																practiceInfo, tournament, tf)));
		}
		
		while (text.startsWith(PRELIMS))
		{
			start = PRELIMS.length() + 1; 
			end = text.indexOf('\n', start); 
			String prelimInfo = text.substring(start, end); 
			text = text.substring(end + 1); 
			
			tournament.addPrelim ((PrelimRound) (Round.recoverBackup(
																prelimInfo, tournament, tf)));
		}
		
		while (text.startsWith(ELIMS))
		{
			start = ELIMS.length() + 1; 
			end = text.indexOf('\n', start); 
			String elimInfo = text.substring(start, end); 
			text = text.substring(end + 1); 
			
			tournament.addElim ((ElimRound) (Round.recoverBackup(
																	elimInfo, tournament, tf)));
		}
		
		//Get breaks 
		ArrayList<Entry> brks = new ArrayList<Entry>(); 
		
		while (text.startsWith(BREAKS)) 
		{
			start = BREAKS.length() + 1;
			end = text.indexOf('\n', start); 
			String breakInfo = text.substring(start, end);
			text = text.substring(end + 1);
		
			brks.add (Entry.recoverBackup(breakInfo, tournament)); 
		}
		
		tournament.setBreaks(brks);
		
		return tournament;		
	}

}















