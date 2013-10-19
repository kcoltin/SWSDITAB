//******************************************************************************
// BreakScreen.java
// Kevin Coltin 
//
// Screen for setting the level of the first outround and doing breaks.
//******************************************************************************

 
 

import javax.swing.*; 
import java.awt.*;
import java.awt.event.*; 
import java.util.ArrayList; 
import java.text.DecimalFormat; 

class BreakScreen extends JPanel 
{
	//Indicates the purpose of the panel: for actually doing the breaks and 
	//selecting who will break to outrounds, for setting what level (quarters, 
	//semis, etc.) the outround will be, or just for viewing the seeds.  
	private int purpose;
	final static int SET_BREAKS = 1, SET_BREAK_LEVEL = 2, VIEW_SEEDS = 3; 
	
	private Tournament tournament; 
	private RoundPanel rp; 
	
	private JPanel entriesPanel; //panel listing debaters/teams by seed	
	private JComboBox levelBox; //menu to select outround level 
	private JCheckBox cleanBreak; //box indicating whether it's a clean break
	
	private final static int GRID_WIDTH = 8; //width of an entry line. 
	
	//---------------------------------------------------------------------------
	// Constructor 
	//---------------------------------------------------------------------------
	BreakScreen (int panelPurpose, RoundPanel panel)
	{
		purpose = panelPurpose; 
		rp = panel; 
		tournament = rp.getRound().getTournament();
		
		setLayout (new BoxLayout(this, BoxLayout.Y_AXIS)); 
		
		//Header at top 
		JLabel topText = new JLabel();
		if (purpose == SET_BREAKS)
			topText.setText("<html><u>Break to Outrounds</u></html>");
		else if (purpose == SET_BREAK_LEVEL)
			topText.setText("<html><u>Select Level of Outround to Break To"
								+ "</u></html>");
		else if (purpose == VIEW_SEEDS)
			topText.setText("<html><u>View Seeds</u></html>");
		
		
		//Panel to hold level box and whether it's clean break 
		JPanel levelPanel = new JPanel();
		levelPanel.setLayout(new BoxLayout(levelPanel, BoxLayout.X_AXIS)); 
		
		levelBox = new JComboBox (Outround.values());
		if (tournament.isBreakLevelSet() && tournament.getElims().size() > 0)
			levelBox.setSelectedItem(tournament.getElims().get(0).getLevel());
		levelBox.addActionListener (new LevelListener()); 
		cleanBreak = new JCheckBox ("Clean break"); 
		cleanBreak.setHorizontalTextPosition (SwingConstants.LEFT); 
		cleanBreak.setSelected(tournament.isCleanBreak()); 
		cleanBreak.addItemListener (new CleanBreakListener()); 
		
		levelPanel.add(levelBox); 
		levelPanel.add(cleanBreak); 
		
		
		//Create panel listing debaters 
		entriesPanel = new JPanel (); 
		entriesPanel.setLayout (new GridBagLayout()); 
		GridBagConstraints gbc = new GridBagConstraints(); 
		gbc.fill = GridBagConstraints.BOTH; 
		
		//remove existing entries
		entriesPanel.removeAll(); 
		
		//Add header 
		gbc.gridy = 0; 
		
		gbc.gridx = 4; 
		entriesPanel.add(new JLabel("\tWins"), gbc); 
		
		gbc.gridx = 5; 
		entriesPanel.add(new JLabel("\tLosses"), gbc); 
		
		gbc.gridx = 6; 
		entriesPanel.add(new JLabel("\tOppWins"), gbc); 

		gbc.gridx = 7; 
		entriesPanel.add(new JLabel("\tRnd"), gbc); 
		
		//Add lines for each entry to entriesPanel 
		resetEntriesLines();

		JPanel bottomPanel = new JPanel(); 
		bottomPanel.setLayout (new BoxLayout (bottomPanel, BoxLayout.X_AXIS)); 

		if (purpose == SET_BREAKS)
		{
			JButton breakButton = new JButton("Break"); 
			breakButton.addActionListener (new BreakListener()); 
			JButton cancel = new JButton ("Cancel"); 
			cancel.addActionListener (new CancelListener());
			
			bottomPanel.add(breakButton); 
			bottomPanel.add(cancel); 
		}
		else if (purpose == SET_BREAK_LEVEL)
		{
			JButton done = new JButton("Done"); 
			done.addActionListener (new SetLevelListener()); 
			JButton cancel = new JButton ("Cancel"); 
			cancel.addActionListener (new CancelListener());

			bottomPanel.add(done); 
			bottomPanel.add(cancel); 
		}
		else if (purpose == VIEW_SEEDS)
		{
			JButton done = new JButton("Done"); 
			done.addActionListener(new CancelListener());

			bottomPanel.add(done); 
		}
		
		//In any case, add button to set students ineligible to break 
		JButton ineligibles = new JButton("Entries ineligible to break"); 
		ineligibles.addActionListener (new IneligibleEntriesListener()); 
		bottomPanel.add(ineligibles); 
		
		//add all to main panel 
		add(topText);
		add(levelPanel);
		add(entriesPanel); 
		add(bottomPanel); 
	}




	//---------------------------------------------------------------------------
	// Adds a line for each entry. (Doesn't include those who are ineligible to 
	// break.) 
	//---------------------------------------------------------------------------
	private void resetEntriesLines ()
	{
		GridBagConstraints gbc = new GridBagConstraints(); 
		gbc.fill = GridBagConstraints.BOTH; 
		gbc.gridy = 1; 
		ArrayList<Entry> seeds = Sort.seed(tournament); 
		
		for (int i = 0; i < seeds.size(); i++)
		{
			Entry entry = seeds.get(i);
			addEntryLine (entriesPanel, entry, i+1, gbc.gridy); 
			gbc.gridy++;
			
			//If this is the lowest-seeded entry that will break under this break
			//level, add a horizontal bar below it.  
			if(getNumWhoBreak() > 0 && entry.equals(seeds.get(getNumWhoBreak()-1)))
			{
				gbc.gridwidth = GRID_WIDTH;
				entriesPanel.add (new JSeparator(SwingConstants.HORIZONTAL), gbc);
				gbc.gridwidth = 1; 
				gbc.gridy++;
			}
		}

		revalidate();
		repaint();
	}

	
	//---------------------------------------------------------------------------
	// Adds a single line containing info for an entry. 
	//---------------------------------------------------------------------------
	private void addEntryLine (JPanel entriesPanel, Entry entry, int seedNum, 
										int gridy)
	{
		GridBagConstraints gbc = new GridBagConstraints(); 
		gbc.fill = GridBagConstraints.BOTH; 
		gbc.gridy = gridy; 
		
		//Add seed number 
		gbc.gridx = 0;
		entriesPanel.add (new JLabel(seedNum + "."), gbc);		
		
		//Add name 
		gbc.gridx = 1; 
		gbc.gridwidth = 3; 
		entriesPanel.add (new JLabel (entry.toString()), gbc);
		gbc.gridwidth = 1; 
		
		//Add wins and losses 
		gbc.gridx = 4; 
		entriesPanel.add (new JLabel (Integer.toString(entry.getWins())), gbc);
		gbc.gridx = 5; 
		entriesPanel.add (new JLabel (Integer.toString(entry.getLosses())), gbc);
		
		//Add strength of opp 
		gbc.gridx = 6; 
		entriesPanel.add (new JLabel (Integer.toString(entry.getOppWins())), gbc);
		
		//Add random tiebreaker 
		DecimalFormat fmt = new DecimalFormat("#.000"); 
		gbc.gridx = 7; 
		entriesPanel.add (new JLabel (fmt.format(entry.getRand())), gbc);
	}




	//---------------------------------------------------------------------------
	// Determines the number of competitors who will break, given the currently
	// selected outround level settings. 
	//---------------------------------------------------------------------------
	int getNumWhoBreak ()
	{
		Outround outround = (Outround) levelBox.getSelectedItem(); 
		ArrayList<Entry> seeds = Sort.seed(tournament); 
		
		//if it's not a clean break, it's just the number of teams that outround
		//can hold (or the total number of possible teams). 
		if (cleanBreak.isSelected() == false)
			return Math.min (outround.getNumEntries(), seeds.size()); 
		
		//Otherwise, it's a clean break. 

		//Get maximum and min numbers who could break 
		int min = Math.min (outround.getNumEntries()/2 + 1, seeds.size());
		int max = Math.min (outround.getNumEntries(), seeds.size()); 
		
		//if everyone who is on the brink (i.e. could potentially either break 
		//or not break) has the same number of wins, this is the same as a non-
		//clean break. "Max" will break.  (copied from above.) 
		if (seeds.get(min - 1).getWins() == seeds.get(max - 1).getWins())
			return max; 

		//If the "max"th entry has more wins than the "max+1"th entry, or if 
		//there are only "max" entries, then exactly "max" entries will break. 
		if (seeds.size() == max 
			|| seeds.get(max - 1).getWins() > seeds.get(max).getWins())
			return max;
		
		//Otherwise, the last breaking entry is the lowest-seeded one with seed
		//"max" or greater that has more wins than the entry below it. Start at
		//the entry that is one seed better than "max." 
		int n = max - 1; 
		
		while (n >= min) 
		{
			//subtract 1 to get arraylist indices 
			if (seeds.get(n-1).getWins() > seeds.get(n).getWins())
				return n;
			
			n--;
		}
		
		return min; //shouldn't get here; just to satisfy compiler
	}
	




	//---------------------------------------------------------------------------
	// Sets the outround level to break to. Used by set level listener and break 
	// listener. 
	//---------------------------------------------------------------------------
	private void setBreakLevel ()
	{
		//Don't permit it if outrounds have already started. 
		if (!(tournament.getElims().isEmpty())//shouldn't be empty, just checking.
			&& tournament.getElims().get(0).hasHappened())
		{
			JOptionPane.showMessageDialog (rp, "Cannot set breaks - " 
				+ tournament.getElims().get(0).getName() + " has already started.", 
				"Break to Elim Rounds", JOptionPane.ERROR_MESSAGE);
			return;
		}		


		Outround outround = (Outround) levelBox.getSelectedItem(); 
	
		//Get confirmation if there are too many elims created - e.g., if user
		//has created three elim rounds, but selects "semis" as the break 
		//level.
		int numLevelsAvailable = Outround.values().length - outround.ordinal();
		if (numLevelsAvailable < tournament.getElims().size())
		{
			int choice = JOptionPane.showConfirmDialog(rp, 
				tournament.getElims().size() + " elim rounds have been created. "
				+ "If you choose to\nbreak to " + outround.toString() 
				+ ", one or more of these rounds will be\ndeleted. Are you sure "
				+ "you want to break to " + outround.toString() + "?", 
				"Set Break Level", JOptionPane.YES_NO_OPTION, 
				JOptionPane.WARNING_MESSAGE);
				
			if (choice == JOptionPane.NO_OPTION)
				return; 
			else //delete all extraneous outrounds  
			{
				//note: i won't change in the loop - rather, the size of 
				//getElims() will just decrease. 
				int i = numLevelsAvailable; 
				
				while (i < tournament.getElims().size())
					tournament.getElims().remove(i);
			}
		}
		
		//Set level to break to. 
		tournament.setBreakLevel (outround); 
	}
			


	//---------------------------------------------------------------------------
	// Accessors 
	//---------------------------------------------------------------------------
	RoundPanel getRoundPanel ()
	{
		return rp; 
	}
	
	int getPurpose ()
	{
		return purpose;
	}



	//---------------------------------------------------------------------------
	// When a different level is selected, this just reinitializes the breaks, 
	// which will automatically show the correct level.
	//---------------------------------------------------------------------------
	private class LevelListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			resetEntriesLines(); 
		}
	}


	//---------------------------------------------------------------------------
	// Changes whether the tournament is a clean break or not. 
	//---------------------------------------------------------------------------
	private class CleanBreakListener implements ItemListener 
	{
		public void itemStateChanged (ItemEvent event)
		{
			tournament.setCleanBreak (cleanBreak.isSelected()); 
		}
	}



	//---------------------------------------------------------------------------
	// Sets which students will break. If the outround level has not yet been
	// set, it sets that too.  
	//---------------------------------------------------------------------------
	private class BreakListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			setBreakLevel(); 
			
			//set teams/debaters who break 
			ArrayList<Entry> seeds = Sort.seed(tournament); 
			ArrayList<Entry> breaks = new ArrayList<Entry>();
			int i = 0; 
			while (i < getNumWhoBreak() && i < seeds.size())
			{
				breaks.add(seeds.get(i));
				i++;
			}
			tournament.setBreaks(breaks); 
			
			rp.restoreMainScreen();
		}
	}



	//---------------------------------------------------------------------------
	// Sets the outround level to break to. 
	//---------------------------------------------------------------------------
	private class SetLevelListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			setBreakLevel();
			rp.restoreMainScreen();
		}
	}
 


	//---------------------------------------------------------------------------
	// Returns to the main Round Panel. 
	//---------------------------------------------------------------------------
	private class CancelListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			rp.restoreMainScreen();
		}
	}


	//---------------------------------------------------------------------------
	// Opens screen to view entries ineligible to break.  
	//---------------------------------------------------------------------------
	private class IneligibleEntriesListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			rp.showIneligibleEntries(); 
		}
	}


}











