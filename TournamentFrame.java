//*****************************************************************************
// TournamentFrame.java 
// Kevin Coltin 
// 
// Screen containing the info for a single tournament. This is the dashboard
// from which the user can do whatever he or she wants with the tournament.
//*****************************************************************************




import javax.swing.*;
import java.awt.*;
import java.io.*; 

class TournamentFrame extends JFrame 
{
	private static final Dimension DEFAULT_SIZE = new Dimension (1000, 600);
	
	private JTabbedPane tp;

	private HomePanel hp; 
	private EntriesPanel ep; 
	private JudgesPanel jp; 
	private RoomsPanel rp; 

	private Tournament tournament; 
	private File file; //file where the tournament is saved 
	private File backupFile; //file where backup version of tournament is saved

	private boolean hasWarnedSave; //whether a warning has been given about saving
	private boolean hasWarnedBackup; //same, for backup saving


	//--------------------------------------------------------------------------
	// Constructor, for initializing a new frame without a tournament already 
	// loaded
	//--------------------------------------------------------------------------
	TournamentFrame ()
	{
		super("SWSDITAB");
		reset();
	}


	//--------------------------------------------------------------------------
	// Constructor, for initializing a new frame with a tournament already 
	// loaded
	//--------------------------------------------------------------------------
	TournamentFrame (Tournament t, File f, File bf)
	{
		super("SWSDITAB - " + t.getName());
		reset(t, f, bf);
	}


	//--------------------------------------------------------------------------
	// The two versions of the reset method do the heavy lifting for the 
	// constructor. They may also be called from outside the class if a new 
	// tournament is opened. 
	//--------------------------------------------------------------------------
	void reset ()
	{
		hp = new HomePanel(this);
		
		tp = new JTabbedPane();
		tp.addTab ("Home", hp);
		
		setJMenuBar (new TopMenu(this)); 
		getContentPane().add(tp); 
		setSize(DEFAULT_SIZE);
		setVisible(true);	
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		
		hasWarnedSave = false;
		hasWarnedBackup = false; 
	}
	
	//Second version 
	void reset (Tournament t, File f, File bf)
	{
		tournament = t; 
		file = f; 
		backupFile = bf;
		
		setTitle("SWSDITAB - " + tournament.getName()); 
		
		getContentPane().removeAll(); 
		validate();
		hp = new HomePanel(tournament, this); 
		ep = new EntriesPanel(tournament, this); 
		jp = new JudgesPanel(tournament, this); 
		rp = new RoomsPanel(tournament, this);
		
		tp = new JTabbedPane(); 
		tp.addTab ("Home", hp); 
		tp.addTab ("Entries", ep); 
		tp.addTab ("Judges", jp);
		tp.addTab ("Rooms", rp); 
		
		//Recreate RoundPanels 
		for (Round round : tournament.getRounds())
			round.resetRoundPanel(this);

		setJMenuBar (new TopMenu(this)); 
		getContentPane().add(tp); 
		setSize(DEFAULT_SIZE);
		setVisible(true);	
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 

		hasWarnedSave = false;
		hasWarnedBackup = false; 
	}


	//--------------------------------------------------------------------------
	// Not to be confused with reset, this just updates all screens in response
	// to a change. It also saves the tournament.  
	//--------------------------------------------------------------------------
	void refresh ()
	{
		//refresh home panel 
		hp.refresh();

		//re initialize any open RoundPanels 
		for (Round round : tournament.getRounds())
		{
			RoundPanel rp = round.getPanel();
			
			if (rp.isVisible())
				rp.initialize(this);
		}
		
		//if EntriesPanel does not have CreateEntryScreen open, refresh it, while
		//keeping currently expanded entry expanded 
		if (ep.isEditScreenOpen() == false)
			ep.resetEntries (ep.getExpandedEntry());

		//if JudgesPanel does not have CreateJudgeScreen open, refresh it, while
		//keeping currently expanded judge expanded 
		if (jp.isEditScreenOpen() == false)
			jp.resetJudges (jp.getExpandedJudge()); 

		//if RoomsPanel does not have CreateRoomScreen open, refresh it, while
		//keeping currently expanded room expanded 
		if (rp.isEditScreenOpen() == false)
			rp.resetRooms (rp.getExpandedRoom());
		
		//Save the tournament 
		try
		{
			save();
		}
		catch (Exception e)
		{
			//do nothing major; this is just a default save so it's ok.
			System.out.println("Warning: save threw an exception (" + e.toString() 
									+ ").");
		}
	}

	//--------------------------------------------------------------------------
	// Accessors and mutators
	//--------------------------------------------------------------------------
	Tournament getTournament ()
	{
		return tournament; 
	}
	
	File getFile ()
	{
		return file; 
	}
	
	void setFile (File f)
	{
		file = f;
	}
	
	File getBackupFile ()
	{
		return backupFile;
	}
	
	void setBackupFile (File bf)
	{
		backupFile = bf;
	}
	


	//--------------------------------------------------------------------------
	// Saves the tournament 
	//--------------------------------------------------------------------------
	private void save () throws Exception
	{
		if (file != null)
			tournament.write(file);
		else if (hasWarnedSave == false)
		{
			System.out.println("Warning: Tournament is not being saved to a "
									+ "file."); 
			hasWarnedSave = true;
		}
		
		if (backupFile != null)
			tournament.backup(backupFile); 
		else if (hasWarnedBackup == false)
		{
			System.out.println("Warning: No backup text version of the "
									+ "tournament is being saved."); 
			hasWarnedBackup = true;
		}
	}


}













