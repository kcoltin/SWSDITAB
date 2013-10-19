//*****************************************************************************
// EntriesPanel.java
// Kevin Coltin 
//
// Panel from which the user can view and edit entries and competitors in the 
// tournament. 
//*****************************************************************************

 
 

import javax.swing.*; 
import java.awt.*; 
import java.awt.event.*; 

class EntriesPanel extends JScrollPane
{
	private Tournament tournament; 
	private TournamentFrame tf;
	
	private GridBagConstraints gbc; 
	private JPanel mainPanel; //panel to go in the scroll pane 
	
	private JLabel entriesLabel; 
	private JButton newButton; 
	
	//currently expanded entry
	private Entry expandedEntry; 
	
	
	
	//--------------------------------------------------------------------------
	// Constructor 
	//--------------------------------------------------------------------------
	EntriesPanel (Tournament t, TournamentFrame frame)
	{
		tournament = t; 
		tf = frame;
		
		mainPanel = new JPanel();
		
		resetEntries (null); 
		
		setViewportView(mainPanel);
	}
	
	
	//--------------------------------------------------------------------------
	// Recreates the screen, listing each entry and, optionally, showing the 
	// full round-by-round results for one entry. 
	//--------------------------------------------------------------------------
	void resetEntries (Entry entryToExpand)
	{
		mainPanel.removeAll(); 
		mainPanel.setLayout(new GridBagLayout()); 
		
		expandedEntry = entryToExpand; 
		
		entriesLabel = new JLabel();
		resetEntriesLabel();
		
		newButton = new JButton ("New entry");
		newButton.addActionListener (new NewListener());		
		
		gbc = new GridBagConstraints();
		
		gbc.gridx = 0; 
		gbc.gridy = 0; 
		mainPanel.add(entriesLabel,gbc);

		gbc.gridx = 4; 
		gbc.gridy = 0; 
		mainPanel.add(newButton,gbc);
		
		int gridy = 1;
		
		for (int i = 0; i < tournament.getEntries().size(); i++)
		{
			Entry entry = tournament.getEntries().get(i); 
			listEntry (entry, gridy, expandedEntry != null 
							&& entry.equals(expandedEntry));
			gridy++;
			
			if (expandedEntry != null && entry.equals(expandedEntry))
			{
				for (int j = 0; j < tournament.getRounds().size(); j++)
				{
					listRound (tournament.getRounds().get(j), entry, gridy);
					gridy++;
				}
			}
			
		}
		
		revalidate();
		repaint();
	}


	//--------------------------------------------------------------------------
	// Puts the basic information for this entry as a line on the screen 
	//--------------------------------------------------------------------------
	void listEntry (Entry entry, int gridy, boolean isExpanded)
	{
		JLabel name;
		
		if (isExpanded)
			name = new JLabel(entry.toString(true, true));
		else
			name = new JLabel(entry.toString(true, false));
			
		JLabel record = new JLabel (entry.getRecord());
		
		JButton expand; 
		
		if (isExpanded)
		{
			expand = new JButton("Collapse");
			expand.addActionListener(new CollapseListener());
		}
		else
		{
			expand = new JButton("Expand");
			expand.addActionListener (new ExpandListener(entry)); 
		}
		
		JButton edit = new JButton("Edit");
		edit.addActionListener (new EditListener(entry)); 
		
		gbc.gridx = 0;
		gbc.gridy = gridy;  
		mainPanel.add(name, gbc);

		gbc.gridx = 2;
		gbc.gridy = gridy;  
		mainPanel.add(record, gbc);
	
		gbc.gridx = 3;
		gbc.gridy = gridy;  
		mainPanel.add(expand, gbc);

		gbc.gridx = 4;
		gbc.gridy = gridy;  
		mainPanel.add(edit, gbc);
	}


	//--------------------------------------------------------------------------
	// Puts the basic information for this round as a line on the screen, under
	// its "expanded" corresponding entry.  
	//--------------------------------------------------------------------------
	void listRound (Round round, Entry entry, int gridy)
	{
		Debate debate = round.getDebate(entry); 

		JButton roundButton = new JButton (round.getName()); 
		roundButton.addActionListener (new RoundListener(round)); 
		
		gbc.gridx = 0; 
		gbc.gridy = gridy; 
		mainPanel.add(roundButton, gbc);
		
		if (debate == null) //if the debater is not assigned to a debate this rd
		{
			for (int x = 1; x <= 4; x++)
			{
				gbc.gridx = x;
				mainPanel.add(new JLabel("-"), gbc);
			}
			
			return;
		}
		
		String matchup = ""; 
		
		if (entry.equals(debate.getAff()))
			matchup += "AFF ";
		if (entry.equals(debate.getNeg()))
			matchup += "NEG ";
		
		Entry opponent = debate.getOpponent(entry);
		
		if (opponent != null)
			matchup += "vs. " + opponent.toString();
		
		if (matchup == "")
			matchup = "-";
		
		JLabel matchupLabel = new JLabel(matchup); 
		JLabel recordLabel = new JLabel(debate.getDecision(entry).toShortString()); 
		JLabel judgeLabel = new JLabel(debate.getJudgeString());
		
		JLabel roomLabel; 
		
		if (debate.getRoom() == null)
			roomLabel = new JLabel("-");
		else
			roomLabel = new JLabel(debate.getRoom().getName());
		
		gbc.gridx = 1; 
		mainPanel.add(matchupLabel, gbc);

		gbc.gridx = 2; 
		mainPanel.add(recordLabel, gbc);

		gbc.gridx = 3; 
		mainPanel.add(judgeLabel, gbc);

		gbc.gridx = 4; 
		mainPanel.add(roomLabel, gbc);
	}



	//--------------------------------------------------------------------------
	// Called after the create entry screen is closed - this removes the entry
	// screen and restores the main panel. 
	//--------------------------------------------------------------------------
	void restoreMainPanel ()
	{
		setViewportView(mainPanel);
		
		//Since the number of entries may have been changed, make it so that every
		//round panel will be refilled so that it has enough blank spaces. 
		for (Round round : tournament.getRounds())
			round.getPanel().setNeedToRefillBlanks(true);
			
		tf.refresh(); 
	}
	
	
	//---------------------------------------------------------------------------
	// Returns currently expanded entry
	//---------------------------------------------------------------------------
	Entry getExpandedEntry()
	{
		return expandedEntry;
	}
	
	
	//---------------------------------------------------------------------------
	// Indicates whether a CreateEntryScreen is currently open, as opposed to 
	// the main panel of the EntriesPanel itself being visible.
	//---------------------------------------------------------------------------
	boolean isEditScreenOpen ()
	{
		return getViewport().getView() instanceof CreateEntryScreen;
	}
	

	//--------------------------------------------------------------------------
	// This is called to reset the number of entries shown at top
	//--------------------------------------------------------------------------
	void resetEntriesLabel ()
	{
		entriesLabel.setText(tournament.getEvent().getName() + " - " + 
									tournament.getEntries().size() + " entries");
	}




	//--------------------------------------------------------------------------
	// Listener for button to create a new entry 
	//--------------------------------------------------------------------------
	private class NewListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			CreateEntryScreen ces = new CreateEntryScreen (EntriesPanel.this, 
																			tournament, null);
			EntriesPanel.this.setViewportView(ces);
			
			//reset expanded view
			expandedEntry = null; 
		}
	}


	//--------------------------------------------------------------------------
	// Listener for button to edit an entry 
	//--------------------------------------------------------------------------
	private class EditListener implements ActionListener 
	{
		private Entry entry; 
		
		EditListener (Entry e)
		{
			entry = e;
		}
		
		public void actionPerformed (ActionEvent event)
		{
			CreateEntryScreen ces = new CreateEntryScreen (EntriesPanel.this,
																			tournament, entry);
			EntriesPanel.this.setViewportView(ces); 

			//reset expanded view
			expandedEntry = null; 
		}
	}


	//--------------------------------------------------------------------------
	// Listener for button to expand the info about an entry 
	//--------------------------------------------------------------------------
	private class ExpandListener implements ActionListener 
	{
		private Entry entry; 
	
		ExpandListener (Entry e)
		{
			entry = e;
		}
		
		public void actionPerformed (ActionEvent event)
		{
			resetEntries (entry); 
		}
	}


	//--------------------------------------------------------------------------
	// Listener for button to collapse/hide the info about an entry 
	//--------------------------------------------------------------------------
	private class CollapseListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			resetEntries (null); 
		}
	}


	//--------------------------------------------------------------------------
	// Clicking on this button will make the RoundPanel for that round appear
	//--------------------------------------------------------------------------
	private class RoundListener implements ActionListener
	{
		private Round round; 
		
		RoundListener (Round r)
		{
			round = r;
		}
		
		public void actionPerformed (ActionEvent event)
		{
			round.getPanel().initialize(tf); 
		}
	}

	

}









