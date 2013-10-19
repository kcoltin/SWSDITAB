//*****************************************************************************
// JudgesPanel.java
// Kevin Coltin 
//
// Panel from which the user can view and edit judges.
//*****************************************************************************

 
 

import javax.swing.*; 
import java.awt.*; 
import java.awt.event.*; 

class JudgesPanel extends JScrollPane
{
	private Tournament tournament; 
	private TournamentFrame tf; 
	
	private GridBagConstraints gbc; 
	private JPanel mainPanel; //panel to go in the scroll pane 
	
	//currently expanded judge
	private Judge expandedJudge; 
	
	//--------------------------------------------------------------------------
	// Constructor 
	//--------------------------------------------------------------------------
	JudgesPanel (Tournament t, TournamentFrame frame)
	{
		tournament = t; 
		tf = frame; 
		
		mainPanel = new JPanel();
		
		resetJudges (null); 
		
		setViewportView(mainPanel); 
	}


	//--------------------------------------------------------------------------
	// Recreates the screen, listing each judge and, optionally, showing the 
	// full round-by-round details for one judge. 
	//--------------------------------------------------------------------------
	void resetJudges (Judge judgeToExpand)
	{
		mainPanel.removeAll(); 
		mainPanel.setLayout (new GridBagLayout()); 
		
		expandedJudge = judgeToExpand;
		
		JLabel nameLabel = new JLabel("<html><u>Name</u></html>");
		JButton newButton = new JButton ("New judge");
		newButton.addActionListener (new NewListener());		
		JLabel pref = new JLabel("<html><u>Rating</u></html>");
		JLabel priority = new JLabel("<html><u>Priority</u></html>"); 

		gbc = new GridBagConstraints();
		
		gbc.gridx = 0; 
		gbc.gridy = 0; 
		mainPanel.add(nameLabel, gbc); 

		gbc.gridx = 1; 
		gbc.gridy = 0; 
		mainPanel.add(newButton, gbc); 
		
		gbc.gridx = 3; 
		gbc.gridy = 0; 
		mainPanel.add(pref, gbc); 

		gbc.gridx = 4; 
		gbc.gridy = 0; 
		mainPanel.add(priority, gbc); 

		int gridy = 1; 
		
		for (int i = 0; i < tournament.getJudges().size(); i++)
		{
			Judge judge = tournament.getJudges().get(i); 
			listJudge (judge, gridy, expandedJudge != null 
							&& judge.equals(expandedJudge));
			gridy++;
			
			if (expandedJudge != null && judge.equals(expandedJudge))
			{
				for (int j = 0; j < tournament.getRounds().size(); j++)
				{
					Round round = tournament.getRounds().get(j);
					listRound (round, judge, gridy);
					
					//if the round is flighted, increment y by two (since a 
					//flighted round takes two lines)
					if (round.isFlighted())
						gridy = gridy + 2;
					else
						gridy++;
				}
			}
		}
		
		revalidate(); 
		repaint();
	}
	
	//--------------------------------------------------------------------------
	// Puts the basic information for this judge as a line on the screen 
	//--------------------------------------------------------------------------
	void listJudge (Judge judge, int gridy, boolean isExpanded)
	{
		JLabel name = new JLabel(judge.getName());
		
		JButton edit = new JButton ("Edit"); 
		edit.addActionListener (new EditListener(judge)); 
		
		JButton expand;
		
		if (isExpanded)
		{
			expand = new JButton("Collapse");
			expand.addActionListener (new CollapseListener()); 
		}
		else
		{
			expand = new JButton("Expand");
			expand.addActionListener (new ExpandListener(judge)); 
		}

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS)); 
		buttonPanel.add(edit);
		buttonPanel.add(expand); 
		
		JLabel blank = new JLabel(); 
		JLabel priority = new JLabel(judge.getDefaultPriority().toString()); 

		gbc.gridx = 0; 
		gbc.gridy = gridy; 
		mainPanel.add(name, gbc); 
		
		gbc.gridx = 1; 
		gbc.gridy = gridy; 
		mainPanel.add(buttonPanel, gbc); 
		
		gbc.gridx = 3; 
		gbc.gridy = gridy; 
		mainPanel.add(blank, gbc); 
		
		gbc.gridx = 4; 
		gbc.gridy = gridy; 
		mainPanel.add(priority, gbc); 
	}
	
	
	//--------------------------------------------------------------------------
	// Puts the basic information for this round as a line on the screen, under
	// its "expanded" corresponding judge.  
	//--------------------------------------------------------------------------
	void listRound (Round round, Judge judge, int gridy)
	{
		listFlight (round, judge, gridy, 'A');
		
		if (round.isFlighted())
			listFlight (round, judge, gridy + 1, 'B');
	}
	

	//--------------------------------------------------------------------------
	// Lists the info about a particular debate/flight in a round. This is the 
	// method that does the heavy lifting for listRound(). 
	//--------------------------------------------------------------------------
	void listFlight (Round round, Judge judge, int gridy, char flight)
	{
		Debate debate = round.getDebate(judge, flight); 
		
		JButton roundButton = new JButton (round.getName() + (round.isFlighted() 
																				? flight : "")); 
		roundButton.addActionListener (new RoundListener(round)); 
		
		JComboBox priority = new JComboBox(Priority.PriorityLevel.values());
		priority.setSelectedItem (judge.getPriority(round));
		priority.addActionListener (new PriorityListener(judge, round)); 

		gbc.gridx = 0; 
		gbc.gridy = gridy; 
		mainPanel.add(roundButton, gbc); 
		
		gbc.gridx = 4; 
		gbc.gridy = gridy; 
		mainPanel.add(priority, gbc); 
			
		if (debate == null) //if the judge is not assigned to a debate this flight
		{
			for (int x = 1; x <= 3; x++)
			{
				gbc.gridx = x; 
				gbc.gridy = gridy; 
				mainPanel.add(new JLabel("-"), gbc); 
			}
				
			return;
		}
		
		JLabel matchup = new JLabel(debate.toString()); 
		JLabel decision = new JLabel(debate.getDecision()); 

		JLabel roomLabel; 
		
		if (debate.getRoom() == null)
			roomLabel = new JLabel("-");
		else
			roomLabel = new JLabel(debate.getRoom().getName());
		
		gbc.gridx = 1; 
		gbc.gridy = gridy; 
		mainPanel.add(matchup, gbc); 

		gbc.gridx = 2; 
		gbc.gridy = gridy; 
		mainPanel.add(decision, gbc); 
		
		gbc.gridx = 3; 
		gbc.gridy = gridy; 
		mainPanel.add(roomLabel, gbc); 
	}



	//--------------------------------------------------------------------------
	// Called after the create judge screen is closed - this removes the judge
	// screen and restores the main panel. 
	//--------------------------------------------------------------------------
	void restoreMainPanel ()
	{
		setViewportView(mainPanel);
		tf.refresh(); 
	}


	//---------------------------------------------------------------------------
	// Returns currently expanded judge
	//---------------------------------------------------------------------------
	Judge getExpandedJudge()
	{
		return expandedJudge;
	}
	
	
	//---------------------------------------------------------------------------
	// Indicates whether a CreateJudgeScreen is currently open, as opposed to 
	// the main panel of the JudgesPanel itself being visible.
	//---------------------------------------------------------------------------
	boolean isEditScreenOpen ()
	{
		return getViewport().getView() instanceof CreateJudgeScreen;
	}

		

	//--------------------------------------------------------------------------
	// Listener for button to create a new judge 
	//--------------------------------------------------------------------------
	private class NewListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			CreateJudgeScreen cjs = new CreateJudgeScreen (JudgesPanel.this, 
																			tournament, null);
			JudgesPanel.this.setViewportView(cjs);
			
			//reset expanded view 
			expandedJudge = null; 
		}
	}


	//--------------------------------------------------------------------------
	// Listener for button to edit a judge 
	//--------------------------------------------------------------------------
	private class EditListener implements ActionListener 
	{
		private Judge judge; 
		
		EditListener (Judge j)
		{
			judge = j;
		}
		
		public void actionPerformed (ActionEvent event)
		{
			CreateJudgeScreen cjs = new CreateJudgeScreen (JudgesPanel.this,
																			tournament, judge);
			JudgesPanel.this.setViewportView(cjs); 
			
			//reset expanded view
			expandedJudge = null; 
		}
	}


	//--------------------------------------------------------------------------
	// Listener for button to expand the info about a judge 
	//--------------------------------------------------------------------------
	private class ExpandListener implements ActionListener 
	{
		private Judge judge; 
	
		ExpandListener (Judge j)
		{
			judge = j;
		}
		
		public void actionPerformed (ActionEvent event)
		{
			resetJudges (judge); 
		}
	}


	//--------------------------------------------------------------------------
	// Listener for button to collapse/hide the info about a judge 
	//--------------------------------------------------------------------------
	private class CollapseListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			resetJudges (null); 
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


	//--------------------------------------------------------------------------
	// Listener for the menu the user can use to set the judge's priority level
	// for each round 
	//--------------------------------------------------------------------------
	private class PriorityListener implements ActionListener 
	{
		private Judge judge; 
		private Round round; 
		
		PriorityListener (Judge j, Round r)
		{
			judge = j; 
			round = r; 
		}
	
		public void actionPerformed (ActionEvent event)
		{
			JComboBox cb = (JComboBox) event.getSource(); 
			Priority.PriorityLevel level; 
			level = (Priority.PriorityLevel) cb.getSelectedItem(); 
			
			judge.setPriority(round, level); 
		}
	}
		

	
	
}






























