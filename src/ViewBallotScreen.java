//******************************************************************************
// ViewBallotScreen.java
// Kevin Coltin 
// 
// Screen to view or edit the ballot for a particular debate.  
//******************************************************************************

 
 

import javax.swing.*; 
import java.awt.*; 
import java.awt.event.*; 
import java.util.ArrayList; 

class ViewBallotScreen extends JPanel 
{
	private Debate debate; 
	private RoundPanel rp; 
	
	//Combo boxes for side and decision for each debater 
	JComboBox side1, side2, decision1, decision2; 
	
	
	//---------------------------------------------------------------------------
	// Constructor 
	//---------------------------------------------------------------------------
	ViewBallotScreen (Debate d, RoundPanel panel)
	{
		debate = d;
		rp = panel; 
		
		setLayout (new BoxLayout(this, BoxLayout.Y_AXIS)); 
		
		//Add label with name of round 
		add (new JLabel(rp.getRound().getName())); 
		
		//Initialize combo boxes - need to do this before calling add*Row methods 
		//to avoid null pointer exceptions. 
		side1 = new JComboBox (Side.values()); 
		side2 = new JComboBox (Side.values()); 

		
		//Create new array of debate outcomes, minus "not competing" (because you
		//can't be non competing if you're in a debate, duh)  
		Object[] outcomes = new Object[Debate.Outcome.values().length - 1];
		int i = 0; 
		
		for (int j = 0; j < Debate.Outcome.values().length; j++)
		{
			if (Debate.Outcome.values()[j] != Debate.Outcome.NOT_COMPETING)
			{
				outcomes[i] = Debate.Outcome.values()[j];
				i++;
			}			
		}
		//then, initialize other combo boxes
		decision1 = new JComboBox (outcomes); 
		decision2 = new JComboBox (outcomes); 

		
		//Add row showing info about the debate 
		addDebateInfoRow(); 
		
		//Add row for showing and selecting debaters' sides 
		addSideRow(); 		
		
		//Add row for showing decision for each debater 
		addDecisionRow(); 
		
		//Add row with buttons for "done" and "cancel" 
		JPanel bottomPanel = new JPanel(); 
		bottomPanel.setLayout (new BoxLayout (bottomPanel, BoxLayout.X_AXIS)); 
		
		JButton done = new JButton ("Done"); 
		done.addActionListener (new DoneListener()); 
		JButton cancel = new JButton ("Cancel"); 
		cancel.addActionListener (new CancelListener()); 
		
		bottomPanel.add(done); 
		bottomPanel.add(cancel); 
		add (bottomPanel); 
	}




	//---------------------------------------------------------------------------
	// Creates row showing the room, each debater, and judge(s) for the debate. 
	//---------------------------------------------------------------------------
	private void addDebateInfoRow ()
	{
		String text = debate.getRoom() + "\t" + debate.getTeam1().toString()
						+ "\t" + debate.getTeam2().toString() + "\t"; 
		
		for (int i = 0; i < debate.getJudges().size(); i++)
		{
			if (i != 0)
				text += ", ";
			
			text += debate.getJudges().get(i).getName(); 
		}
		
		add (new JLabel(text)); 
	}



	//---------------------------------------------------------------------------
	// Creates row showing and (if it's a flip for sides debate) letting user 
	// select debaters' sides. 
	//---------------------------------------------------------------------------
	private void addSideRow ()
	{
		JPanel panel = new JPanel(); 
		panel.setLayout (new BoxLayout (panel, BoxLayout.X_AXIS)); 
		
		panel.add (new JLabel(debate.getTeam1().toString() + ":")); 
		side1.addItemListener (new SideListener()); 
		panel.add (side1); 
		
		panel.add (new JLabel(debate.getTeam2().toString() + ":")); 
		side2.addItemListener (new SideListener()); 
		panel.add (side2); 

		//Set selected side for first team. (The item listener will cause this to 
		//automatically set the selected side for the second team as well.) 
		if (debate.isSideSet())
		{
			side1.setSelectedItem(Side.AFF);
			side2.setSelectedItem(Side.NEG);
		}
		else 
		{
			side1.setSelectedItem(Side.NONE); 
			side2.setSelectedItem(Side.NONE); 
		}
		
		//If it's sidelocked, can't change side. (Can only change it if it was a
		//flip for sides elim.) 
		if (debate.isSidelocked())
		{
			side1.setEnabled(false); 
			side2.setEnabled(false); 
		}
		
		add (panel); 
	}

		


	//---------------------------------------------------------------------------
	// Creates row showing and letting user select the outcome for each debater. 
	//---------------------------------------------------------------------------
	private void addDecisionRow ()
	{
		JPanel panel = new JPanel(); 
		panel.setLayout (new BoxLayout (panel, BoxLayout.X_AXIS)); 
		
		panel.add (new JLabel(debate.getTeam1().toString() + ":")); 
		decision1.addItemListener (new DecisionListener()); 
		panel.add (decision1); 
		
		panel.add (new JLabel(debate.getTeam2().toString() + ":")); 
		decision2.addItemListener (new DecisionListener()); 
		panel.add (decision2); 

		//Set selected decisions  
		if (debate.isBallotEntered())
		{
			decision1.setSelectedItem(debate.getAffDecision());
			decision2.setSelectedItem(debate.getNegDecision());
		}
		else 
		{
			decision1.setSelectedItem(Debate.Outcome.NO_DECISION); 
			decision2.setSelectedItem(Debate.Outcome.NO_DECISION); 
		}
		
		//Make it disabled if the side is not selected - you can't enter a record
		//before a side. 
		if (side1.getSelectedItem() == Side.NONE)
		{
			decision1.setSelectedItem (Debate.Outcome.NO_DECISION); 
			decision2.setSelectedItem (Debate.Outcome.NO_DECISION); 
			decision1.setEnabled(false);
			decision2.setEnabled(false); 
		}
						
		add (panel); 
	}


	//---------------------------------------------------------------------------
	// Listener for when a side combo box is changed. 
	//---------------------------------------------------------------------------
	private class SideListener implements ItemListener 
	{
		public void itemStateChanged (ItemEvent event) 
		{
			JComboBox source = (JComboBox) event.getSource(); 
			
			if (source == side1)
			{		
				//If one side is selected as none, make the other side none 
				if (side1.getSelectedItem() == Side.NONE 
					&& side2.getSelectedItem() != Side.NONE) 
				{
					side2.setSelectedItem(Side.NONE); 
				}

				//If one side is selected as aff, make the other side neg 
				else if (side1.getSelectedItem() == Side.AFF 
						&& side2.getSelectedItem() != Side.NEG) 
				{
					side2.setSelectedItem(Side.NEG); 
				}
					
				//If one side is selected as neg, make the other side aff 
				else if (side1.getSelectedItem() == Side.NEG 
						&& side2.getSelectedItem() != Side.AFF) 
				{
					side2.setSelectedItem(Side.AFF); 
				}
			}
			
			//Same as above, copied for side2 
			if (source == side2) 
			{
				if (side2.getSelectedItem() == Side.NONE 
					&& side1.getSelectedItem() != Side.NONE) 
				{
					side1.setSelectedItem(Side.NONE); 
				}

				else if (side2.getSelectedItem() == Side.AFF 
						&& side1.getSelectedItem() != Side.NEG) 
				{
					side1.setSelectedItem(Side.NEG); 
				}

					
				else if (side2.getSelectedItem() == Side.NEG 
						&& side1.getSelectedItem() != Side.AFF) 
				{
					side1.setSelectedItem(Side.AFF); 
				}
			}
			
			
			//Finally, if the sides are now set, make the decision listener 
			//enabled. 
			if (side1.getSelectedItem() == Side.NEG 
				|| side1.getSelectedItem() == Side.AFF)
			{
				decision1.setEnabled(true); 
				decision2.setEnabled(true); 
			}
			//Otherwise, set it disabled 
			else
			{
				decision1.setEnabled(false); 
				decision2.setEnabled(false); 
			}
		}
	}




	//---------------------------------------------------------------------------
	// Listener for when a decision combo box is changed. 
	//---------------------------------------------------------------------------
	private class DecisionListener implements ItemListener 
	{
		public void itemStateChanged (ItemEvent event) 
		{
			JComboBox source = (JComboBox) event.getSource(); 
			
			if (source == decision1)
			{		
				//If one decision is selected as none, make the other side none 
				if (decision1.getSelectedItem() == Debate.Outcome.NO_DECISION 
					&& decision2.getSelectedItem() != Debate.Outcome.NO_DECISION) 
				{
					decision2.setSelectedItem(Debate.Outcome.NO_DECISION); 
				}

				//If one decision is selected as win, make the other decision loss 
				else if (decision1.getSelectedItem() == Debate.Outcome.WIN 
						&& decision2.getSelectedItem() != Debate.Outcome.LOSS) 
				{
					decision2.setSelectedItem(Debate.Outcome.LOSS); 
				}
					
				//If one decision is selected as loss, make the other decision win 
				else if (decision1.getSelectedItem() == Debate.Outcome.LOSS 
						&& decision2.getSelectedItem() != Debate.Outcome.WIN) 
				{
					decision2.setSelectedItem(Debate.Outcome.WIN); 
				}

				//If one decision is selected as bye, make the other decision forfeit 
				else if (decision1.getSelectedItem() == Debate.Outcome.BYE 
						&& decision2.getSelectedItem() != Debate.Outcome.FORFEIT) 
				{
					decision2.setSelectedItem(Debate.Outcome.FORFEIT); 
				}
					
				//If one decision is selected as forfeit, make the other decision bye 
				else if (decision1.getSelectedItem() == Debate.Outcome.FORFEIT 
						&& decision2.getSelectedItem() != Debate.Outcome.BYE) 
				{
					decision2.setSelectedItem(Debate.Outcome.BYE); 
				}
			}
			
			//Same as above, copied for decision2 
			if (source == decision2) 
			{
				//If one decision is selected as none, make the other side
				//none 
				if (decision2.getSelectedItem() == Debate.Outcome.NO_DECISION 
					&& decision1.getSelectedItem() != Debate.Outcome.NO_DECISION) 
				{
					decision1.setSelectedItem(Debate.Outcome.NO_DECISION); 
				}

				//If one decision is selected as win, make the other decision loss 
				else if (decision2.getSelectedItem() == Debate.Outcome.WIN 
						&& decision1.getSelectedItem() != Debate.Outcome.LOSS) 
				{
					decision1.setSelectedItem(Debate.Outcome.LOSS); 
				}
					
				//If one decision is selected as loss, make the other decision win 
				else if (decision2.getSelectedItem() == Debate.Outcome.LOSS 
						&& decision1.getSelectedItem() != Debate.Outcome.WIN) 
				{
					decision1.setSelectedItem(Debate.Outcome.WIN); 
				}

				//If one decision is selected as bye, make the other decision forfeit 
				else if (decision2.getSelectedItem() == Debate.Outcome.BYE 
						&& decision1.getSelectedItem() != Debate.Outcome.FORFEIT) 
				{
					decision1.setSelectedItem(Debate.Outcome.FORFEIT); 
				}
					
				//If one decision is selected as forfeit, make the other decision bye 
				else if (decision2.getSelectedItem() == Debate.Outcome.FORFEIT 
						&& decision1.getSelectedItem() != Debate.Outcome.BYE) 
				{
					decision1.setSelectedItem(Debate.Outcome.BYE); 
				}
			}
			
		}
	}



	//---------------------------------------------------------------------------
	// Closes the screen and saves the changes  
	//---------------------------------------------------------------------------
	private class DoneListener implements ActionListener  
	{
		public void actionPerformed (ActionEvent event) 
		{
			//If any elim rounds after this round have been started, show a warning
			ArrayList<Round> rounds = rp.getRound().getTournament().getRounds(); 
			int i = rounds.size() - 1; 
			
			while (i >= 0)
			{
				//Quit when we get to this round or earlier. 
				if (rounds.get(i).compareTo(rp.getRound()) <= 0)
					break;
			
				if (rounds.get(i).hasHappened())
				{
					int choice; 
					
					if (rounds.get(i) instanceof ElimRound)
					{
						choice = JOptionPane.showConfirmDialog (rp, "Teams have " 
							+ "already broken to a subsequent outround\nbased on wins "
							+ "and losses from this round. Are you sure\nyou want to "
							+ "edit this ballot? It may cause unwanted\nand undefined "
							+ "behavior.", "Enter Ballot", JOptionPane.YES_NO_OPTION, 
							JOptionPane.ERROR_MESSAGE);
					}
					else 
					{
						choice = JOptionPane.showConfirmDialog (rp, "Teams have " 
							+ "already been paired in a subsequent round.\nAre you "
							+ "sure\nyou want to edit this ballot?", "Enter Ballot", 
							JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
					}
					
					if (choice != JOptionPane.YES_OPTION)
						return; 
				}
				
				i--;
			}
				
			//Then, make the changes to the sides (if applicable) of this debate. 
			Side team1Side = (Side) side1.getSelectedItem(); 
			
			if (team1Side == Side.AFF)
				debate.setAff (debate.getTeam1()); 
			else if (team1Side == Side.NEG)
				debate.setAff (debate.getTeam2()); 
			//Don't need to do anything if side is NONE - that means decision will
			//necessarily be "no decision," which means the ballot is defined as 
			//not entered. 

			//Finally, set the outcome. (The if/else is to make it work if it's an
			//outround and the user just set the team on the left to be neg and 
			//thus "team 2".) 
			if (team1Side == Side.AFF)
			{
				debate.setDecision(0, (Debate.Outcome) decision1.getSelectedItem()); 
				debate.setDecision(1, (Debate.Outcome) decision2.getSelectedItem()); 
			}
			else
			{
				debate.setDecision(1, (Debate.Outcome) decision1.getSelectedItem()); 
				debate.setDecision(0, (Debate.Outcome) decision2.getSelectedItem()); 
			}
			
		
			//Reset status of round, given that a ballot has been entered/unentered
			rp.getRound().resetStatus();
		
			//If this is an elim round and breaks have been set, reset the debaters
			//who may appear in each subsequent elim. (E.g., if a debater lost 
			//this round, they shouldn't appear in any subsequent elims.) 
			if (rp.getRound() instanceof ElimRound 
				&& rp.getRound().getTournament().getBreaks().isEmpty() == false)
			{
				Round rd = rp.getRound(); 
				
				while (rd.getNextRound() != null)
				{
					rd = rd.getNextRound(); 
					((ElimRound) rd).resetBreakEntries(); 
				}
			}
			
			rp.restoreMainScreen(); 
		}
	}
	
	

	//---------------------------------------------------------------------------
	// Closes the screen without saving any changes  
	//---------------------------------------------------------------------------
	private class CancelListener implements ActionListener  
	{
		public void actionPerformed (ActionEvent event) 
		{
			rp.restoreMainScreen(); 
		}
	}



}

























