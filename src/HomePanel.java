//*****************************************************************************
// HomePanel.java
// Kevin Coltin   
//
// This is the main screen from which the user can interact with the 
// tournament. It displays and lets the user edit basic info such as the 
// tournament name, and displays every round of the tournament and lets the 
// user click on rounds to view, edit, or pair them. 
//*****************************************************************************




import javax.swing.*; 
import java.awt.*; 
import java.awt.event.*; 
import java.util.ArrayList; 

class HomePanel extends JPanel 
{
	private Tournament tournament; 
	private TournamentFrame tf;

	private JScrollPane scrollPane; 
	private JPanel mainPanel; //panel containing everything on the main screen, 
										//to go in the scroll pane 
	private JPanel eventPanel, roundsPanel; 
	
	private JLabel tournamentNameLabel, eventLabel, entriesLabel, statusLabel, 
						timeLabel, flightedLabel;
	private JButton newRoundButton; 

	private enum RoundTypes {Practice, Prelim, Outround}; //used by NewListener
	

	//--------------------------------------------------------------------------
	// Default constructor, for when no tournament has been opened yet 
	//--------------------------------------------------------------------------
	HomePanel (TournamentFrame frame)
	{
		tf = frame;
	
		JLabel placeholder = new JLabel ("<html>Welcome to SWSDITAB! Please use "
				+ "the File menu to create a<br />new tournament or open an existing "
				+ "one.</html"); 
		add (placeholder); 
	}

	
	//--------------------------------------------------------------------------
	// Constructor 
	//--------------------------------------------------------------------------
	HomePanel (Tournament t, TournamentFrame frame) 
	{
		tournament = t; 
		tf = frame;
	
		tournamentNameLabel = new JLabel("<html><strong>"  + tournament.getName()
													+ "</strong></html>"); 
		
		eventPanel = new JPanel(); 
		eventPanel.setLayout (new BoxLayout(eventPanel, BoxLayout.X_AXIS)); 
		eventLabel = new JLabel (tournament.getEvent().getName()); 
		entriesLabel = new JLabel ();
		resetEntriesLabel();
		eventPanel.add(eventLabel); 
		eventPanel.add(Box.createHorizontalGlue());
		eventPanel.add(entriesLabel); 
		
		roundsPanel = new JPanel(); 
		refresh(); 
		
		mainPanel = new JPanel(); 
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); 
		mainPanel.add(tournamentNameLabel); 
		mainPanel.add(eventPanel); 
		mainPanel.add(roundsPanel);
		
		scrollPane = new JScrollPane(mainPanel); 
		add (scrollPane); 
		
		revalidate(); 
	}
	
	
	//--------------------------------------------------------------------------
	// Recreates the bottom part of the screen, showing info and buttons about 
	// all rounds in the tournament.  This method also updates anything else in
	// the panel that might need to be frequently changed.  
	//--------------------------------------------------------------------------
	void refresh ()
	{
		entriesLabel.setText (tournament.getEntries().size() + " entries"); 
		
		roundsPanel.removeAll(); 
		roundsPanel.setLayout (new GridBagLayout()); 
		
		newRoundButton = new JButton ("Add new round"); 
		newRoundButton.addActionListener (new NewRoundListener()); 
		
		statusLabel = new JLabel("<html><u>Status</u></html>"); 
		timeLabel = new JLabel("<html><u>Time</u></html>"); 
		flightedLabel = new JLabel("<html><u>Flighted</u></html>");
		
		GridBagConstraints gbc = new GridBagConstraints ();

		gbc.gridx = 1;
		gbc.gridy = 0;
		roundsPanel.add (newRoundButton, gbc);

		gbc.gridx = 2;
		gbc.gridy = 0;
		roundsPanel.add (statusLabel, gbc);

		gbc.gridx = 3;
		gbc.gridy = 0;
		roundsPanel.add (timeLabel, gbc);

		gbc.gridx = 4;
		gbc.gridy = 0;
		roundsPanel.add (flightedLabel, gbc);
		
		ArrayList<Round> rounds = tournament.getRounds(); 
		
		for (int i = 0; i < rounds.size(); i++)
		{
			Round round = rounds.get(i); 
			
			JButton button = new JButton("View"); 
			button.addActionListener(new ViewListener(round)); 
			JLabel name = new JLabel(round.getName());
			JLabel status = new JLabel(round.getStatusString());
			JLabel time = new JLabel(round.getTime());

			JLabel flighted = new JLabel();
			if (round.isFlighted())
				flighted.setText("Yes");
			else
				flighted.setText("-");

			gbc.gridx = 0; 
			gbc.gridy = i+1; 
			roundsPanel.add (button, gbc); 
			
			gbc.gridx = 1; 
			gbc.gridy = i+1; 
			roundsPanel.add (name, gbc); 
			
			gbc.gridx = 2; 
			gbc.gridy = i+1; 
			roundsPanel.add (status, gbc); 
						
			gbc.gridx = 3; 
			gbc.gridy = i+1; 
			roundsPanel.add (time, gbc); 
			
			gbc.gridx = 4; 
			gbc.gridy = i+1; 
			roundsPanel.add (flighted, gbc);
		}
		
		revalidate();
	}
	
	
	//--------------------------------------------------------------------------
	// This is called to reset the number of entries shown at top
	//--------------------------------------------------------------------------
	void resetEntriesLabel ()
	{
		entriesLabel.setText(tournament.getEntries().size() + " entries");
	}
		

	//--------------------------------------------------------------------------
	// Creates a new round 
	//--------------------------------------------------------------------------
	private class NewRoundListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			Object choice = JOptionPane.showInputDialog(HomePanel.this, 
				"Select type of round:", "Add new round", 
				JOptionPane.PLAIN_MESSAGE, null, RoundTypes.values(), 
				RoundTypes.Prelim); 
			
			if (choice == null)
				return; 
			
			else if (choice.equals(RoundTypes.Practice))
			{
				int roundNum = tournament.getPractices().size() + 1; 
				PracticeRound round = new PracticeRound (tournament, tf, roundNum); 
				tournament.addPractice(round); 
				HomePanel.this.refresh();
			}
			else if (choice.equals(RoundTypes.Prelim))
			{
				int roundNum = tournament.getPrelims().size() + 1; 
				PrelimRound round = new PrelimRound (tournament, tf, roundNum); 
				tournament.addPrelim(round); 
				HomePanel.this.refresh();
			}
			else if (choice.equals(RoundTypes.Outround))
			{
				ElimRound round; 
			
				//if the user has already set the break level, have them select 
				//what level this round will be 
				if (tournament.isBreakLevelSet() == true)
				{
					Outround level; 
					
					do
					{
						choice = JOptionPane.showInputDialog(HomePanel.this, 
							"Select which outround to add:", "Add new round", 
							JOptionPane.PLAIN_MESSAGE, null, Outround.values(), 
							Outround.Octos); 
						
						if (choice == null) //if user presses "cancel" 
							return;
						
						level = (Outround) choice; 
						
						if (hasOutround(level))
							JOptionPane.showMessageDialog(HomePanel.this,
								level.toString() + " already exists - please select "
								+ "another outround.", "Add new round", 
								JOptionPane.ERROR_MESSAGE); 
					}
					while (hasOutround(level));
					
					round = new ElimRound (tournament, tf, level); 
				}

				//otherwise, just name this outround in order, just as above with
				//practice and prelim rounds. 
				else
				{
					int roundNum = tournament.getElims().size() + 1; 
					round = new ElimRound (tournament, tf, roundNum); 
				}
				
				tournament.addElim(round); 

				if (tournament.isBreakLevelSet() == true)
				{	
					//If there is a "gap" between this elim and another existing 
					//elim (e.g. if you just created octos and only semis previously
					//existed), fill in intermediate ones 
					tournament.fillOutrounds(); 
				}
				
				tf.refresh();
			}
		}
		
		//Checks whether the tournament already has a particular outround
		private boolean hasOutround (Outround level)
		{
			ArrayList<ElimRound> rounds = tournament.getElims(); 
			
			for (ElimRound round : rounds)
			{
				if (round.getLevel().equals(level))
					return true; 
			}
			
			return false; 
		}
	}
	
	//--------------------------------------------------------------------------
	// Causes the screen for the round to pop out with the user clicks on the 
	// button to view the round. 
	//--------------------------------------------------------------------------
	private class ViewListener implements ActionListener 
	{
		Round round; 
	
		ViewListener (Round r)
		{
			round = r; 
		}
	
		public void actionPerformed (ActionEvent event)
		{
			round.getPanel().initialize(tf); 
		}
	}


		
}









