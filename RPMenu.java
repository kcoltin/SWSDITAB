//*****************************************************************************
// RPMenu.java
// Kevin Coltin  
//
// This is the menu bar that sits at the top of a RoundPanel. 
//*****************************************************************************

 
 

import javax.swing.*; 
import java.awt.event.*; 
import java.util.ArrayList; 

class RPMenu extends JMenuBar
{
	private JMenu startMenu, pairMenu, outroundMenu, helpMenu; 
	
	private RoundPanel rp; 
	private Round round; 
	private Tournament tournament; 
	
	//---------------------------------------------------------------------------
	// Constructor 
	//---------------------------------------------------------------------------
	RPMenu (RoundPanel panel, boolean hasRdStarted)
	{
		rp = panel; 
		round = rp.getRound(); 
		tournament = round.getTournament(); 
		
		//Create start menu 
		startMenu = new JMenu ("Start"); 
		startMenu.setMnemonic(KeyEvent.VK_S); 
		
		JMenuItem startButton = new JMenuItem (); 
		startButton.setText(hasRdStarted ? "Cancel start" : "Start round"); 
		startButton.addActionListener (new StartListener()); 
		
		JMenuItem printButton = new JMenuItem ("Print schematics"); 
		printButton.addActionListener (new PrintListener()); 
		
		startMenu.add(startButton); 
		startMenu.add(printButton); 
		
		//Create pair menu 
		pairMenu = new JMenu ("Pairing");
		pairMenu.setMnemonic (KeyEvent.VK_P); 
		
		JMenuItem pairButton = new JMenuItem("Pair debates..."); 
		pairButton.addActionListener(new PairListener()); 
		
		//menu items to add blank lines 
		JMenuItem addLine = new JMenuItem("Add blank line"); 
		addLine.addActionListener (new AddLineListener()); 
		
		JMenu subMenuAdd = new JMenu("Add blank line to flight");
		JMenuItem addToA = new JMenuItem ("Flight A");
		addToA.addActionListener (new AddLineListener ('A'));
		JMenuItem addToB = new JMenuItem ("Flight B");
		addToB.addActionListener (new AddLineListener ('B'));
		subMenuAdd.add(addToA);
		subMenuAdd.add(addToB);

		//menu items to delete blank lines 
		JMenuItem deleteLine = new JMenuItem("Delete blank line"); 
		deleteLine.addActionListener (new DeleteLineListener()); 

		JMenu subMenuDelete = new JMenu("Delete blank line from flight");
		JMenuItem deleteFromA = new JMenuItem ("Flight A");
		deleteFromA.addActionListener (new DeleteLineListener ('A'));
		JMenuItem deleteFromB = new JMenuItem ("Flight B");
		deleteFromB.addActionListener (new DeleteLineListener ('B'));
		subMenuDelete.add(deleteFromA);
		subMenuDelete.add(deleteFromB);

		pairMenu.add(pairButton); 
		pairMenu.add(addLine); 
		pairMenu.add(subMenuAdd);
		pairMenu.add(deleteLine);
		pairMenu.add(subMenuDelete);  
		
		//Create outround menu 
		outroundMenu = new JMenu ("Outround");
		outroundMenu.setMnemonic (KeyEvent.VK_O); 
		
		JMenuItem viewSeedsButton = new JMenuItem ("View seeds");
		viewSeedsButton.addActionListener (new ViewSeedsListener());
		
		//Break buttons should only be enabled if this is the first outround.
		JMenuItem setLevelButton = new JMenuItem ("Set outround level..."); 
		if (round instanceof ElimRound 
			&& tournament.getElims().isEmpty() == false 
			&& round.equals(tournament.getElims().get(0)))
			setLevelButton.addActionListener (new SetLevelListener()); 
		else
			setLevelButton.setEnabled(false); 
		
		JMenuItem breakButton = new JMenuItem ("Break to outrounds...");
		if (round instanceof ElimRound 
			&& tournament.getElims().isEmpty() == false 
			&& round.equals(tournament.getElims().get(0)))
			breakButton.addActionListener (new BreakListener()); 
		else
			breakButton.setEnabled(false); 
		
		JMenuItem ineligibleButton = new JMenuItem ("Select teams ineligible to "
																+ "break...");
		if (round instanceof ElimRound 
			&& tournament.getElims().isEmpty() == false 
			&& round.equals(tournament.getElims().get(0)))
			ineligibleButton.addActionListener (new IneligibleListener()); 
		else
			ineligibleButton.setEnabled(false); 
		
		JMenuItem numJudgesButton = new JMenuItem ("Set number of judges..."); 
		if (round instanceof ElimRound)
			numJudgesButton.addActionListener (new NumJudgesListener()); 
		else
			numJudgesButton.setEnabled(false); 
		
		outroundMenu.add(viewSeedsButton);
		outroundMenu.add(setLevelButton); 
		outroundMenu.add(breakButton);
		outroundMenu.add(ineligibleButton); 
		outroundMenu.add(numJudgesButton); 

		//Create help menu 
		helpMenu = new JMenu("Help"); 
		helpMenu.setMnemonic (KeyEvent.VK_H); 
		
		JMenuItem helpMenuPlaceholder = 
									new JMenuItem("<html><em>In progress</em></html>");
		helpMenu.add(helpMenuPlaceholder); 
		
		//Add all menus 
		add (startMenu);
		add (pairMenu); 
		add (outroundMenu); 
		add (helpMenu); 
	}
	

	//---------------------------------------------------------------------------
	// Causes the round to be officially started. 
	//---------------------------------------------------------------------------
	private class StartListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			if (round.hasHappened())
			{
				showCancelDialog(); 
				return;
			}
			
			//If it's an elim round, can't start the round until its level has been
			//set. 
			if (round instanceof ElimRound && ((ElimRound) round).getLevel() == null)
			{
				JOptionPane.showMessageDialog(rp, "Cannot start this outround - "
					+ "its level (i.e. octos, quarters, etc.)\nhas not yet been "
					+ "set.", "Start Round", JOptionPane.ERROR_MESSAGE); 
				return;
			}
			
			//If it's an outround, can't start round until everyone is either in a 
			//debate or has a bye. 
			if (round instanceof ElimRound 
				&& round.getUnassignedDebaters().size() > 0)
			{
				JOptionPane.showMessageDialog(rp, "Cannot start this outround - "
					+ "not all debaters are\nassigned to a debate.", "Start Round",
					JOptionPane.ERROR_MESSAGE); 
				return;				
			}

			
			//Don't let them start the round unless all previous rounds have been
			//started. 
			int i = 0; 
			ArrayList<Round> rounds = tournament.getRounds();
			while (i < rounds.size() && rounds.get(i).compareTo(round) < 0)
			{
				if (rounds.get(i).hasHappened() == false)
				{
					JOptionPane.showMessageDialog(rp, "Not all previous rounds have "
							+ "been started - cannot start this round yet.", 
							"Start Round", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				i++;
			}


			//If it's an elim round, can't start the round until breaks have been
			//set. 
			if (round instanceof ElimRound && tournament.getElims().isEmpty())
			{
				JOptionPane.showMessageDialog (rp, "Tournament has not yet broken "
					+ "to outrounds - need to select\n\"Break to outrounds\" first.",
					"Start Round", JOptionPane.ERROR_MESSAGE); 
				return;
			}
		
			
			//Show a warning if the round is not adequately paired. 
			if (round.isFullyPaired() == false)
			{
				int choice = JOptionPane.showConfirmDialog (rp, "Not all debates "
								+ "in this round are fully paired. Are you sure you "
								+ "want to start the round?", "Start Round", 
								JOptionPane.YES_NO_OPTION, 
								JOptionPane.WARNING_MESSAGE);
				
				if (choice != JOptionPane.YES_OPTION)
					return; 
			}
			
			//Then, starts the round.
			round.setStatus (Round.Status.IN_PROGRESS); 
			rp.getTournamentFrame().refresh();
		}
		
		//Dialog for if the user clicks the button, allowing them to cancel the 
		//start of a round that has already been started.  
		private void showCancelDialog ()
		{
			//If the next round has already started, can't cancel this one 
			if (round.getNextRound() != null && round.getNextRound().hasHappened())
			{
				JOptionPane.showMessageDialog(rp, "Cannot cancel round; next round "
					+ "has already started. If you wish to cancel the\nstart of "
					+ "this round, please cancel subsequent rounds first.", 
					"Cancel Round Start", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			if (round instanceof PracticeRound == false 
				&& round.getNumBallotsEntered() > 0)
			{
				JOptionPane.showMessageDialog(rp, "Cannot cancel round; ballots "
					+ "have already been entered. If you wish to cancel the\nstart "
					+ "of this round, please un-enter all ballots first.", 
					"Cancel Round Start", JOptionPane.ERROR_MESSAGE); 
				return;
			}
			
			int choice = JOptionPane.showConfirmDialog (rp, "Are you sure you " 
							+ "want to cancel the start of this round?", 
							"Cancel Round Start", JOptionPane.YES_NO_OPTION, 
							JOptionPane.WARNING_MESSAGE);
			
			if (choice == JOptionPane.YES_OPTION)
			{
				round.setStatus (Round.Status.NOT_STARTED); 
				rp.getTournamentFrame().refresh(); 
			}
		}
	}



	//---------------------------------------------------------------------------
	// Saves the schematics as a .tex file, which can then be converted to PDF 
	// and printed. 
	//---------------------------------------------------------------------------
	private class PrintListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			Print.printPostings (round); 
		}
	}


	//---------------------------------------------------------------------------
	// Pairs the debates 
	//---------------------------------------------------------------------------
	private class PairListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			if (round.hasHappened())
			{
				JOptionPane.showMessageDialog(rp, "Cannot pair round - round has "
					+ "already started.", "Pair Round", JOptionPane.ERROR_MESSAGE); 
				return;
			}
			
/*	TODO: finish this. 
			if (round.hasErrors())
			{
				JOptionPane.showMessageDialog(rp, "Cannot pair round - need to fix "
				   + "some errors (in red) first.", "Pair Round", 
					JOptionPane.ERROR_MESSAGE); 
				return;
			}
*/			

			int opt = JOptionPane.showConfirmDialog(rp, "Are you sure you want "
				+ "to automatically\npair this round? This will remove all "
				+ "currently\nassigned debates.", "Pair Round", 
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); 
			
			if (opt != JOptionPane.YES_OPTION)
				return; 
			
			//Ask user to pair debates only; debates and judges; or debates, 
			//judges, and rooms. 
			String[] options = {"Debates only", "Debates and judges", "Debates, "
										+ "judges, and rooms", "Judges and rooms only", 
										"Judges only", "Rooms only",  }; 
			
			String choice = (String) JOptionPane.showInputDialog (rp, 
					"Select what you would like to pair:", "Pair Round", 
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			
			int type; 
			
			if (choice == null)
				return; 
			
			if (choice.equals(options[0]))
				type = Pair.DEBATES; 
			else if (choice.equals(options[1]))
				type = Pair.DEBATES_AND_JUDGES; 
			else if (choice.equals(options[2]))
				type = Pair.ALL_THREE; 
			else if (choice.equals(options[3]))
				type = Pair.JUDGES_AND_ROOMS; 
			else if (choice.equals(options[4]))
				type = Pair.JUDGES; 
			else 
				type = Pair.ROOMS; 
			
			Pair.setTournament(tournament); 
			Pair.pair(round, type); 
			rp.getTournamentFrame().refresh(); 
		}
	}


	
	//---------------------------------------------------------------------------
	// Adds one more blank line to the round panel. If the round is flighted, it 
	// adds it to whichever flight has fewer items, or to Flight A if it's a tie.
	//---------------------------------------------------------------------------
	private class AddLineListener implements ActionListener 
	{
		final private char flight; 
	
		//Constructor - default 
		AddLineListener ()
		{
			super();
			flight = '0';
		}
		//Constructor - specific flight 
		AddLineListener (char flt)
		{
			super();
			flight = flt;
		}
	
		public void actionPerformed (ActionEvent event)
		{
			if (flight == '0' || round.isFlighted() == false)
				rp.addBlankLine(); 
			else
			{
				round.getItemsOnPairings().add (new BlankLine(flight)); 
				rp.initialize(rp.getTournamentFrame());
			}
		}
	}

	//---------------------------------------------------------------------------
	// Removes a single blank line from the round panel. Removes the bottom-most
	// blank line from the flight that has the most items. 
	//---------------------------------------------------------------------------
	private class DeleteLineListener implements ActionListener 
	{
		final private char flight; 
	
		//Constructor - default 
		DeleteLineListener ()
		{
			super();
			flight = '0';
		}
		//Constructor - specific flight 
		DeleteLineListener (char flt)
		{
			super();
			flight = flt;
		}
	
		public void actionPerformed (ActionEvent event)
		{
			if (flight == '0' || round.isFlighted() == false)
				rp.deleteBlankLine(); 
			else 
				rp.deleteBlankLine(flight); 
		}
	}


	//---------------------------------------------------------------------------
	// Shows the screen to view debaters and their records/seeds.  
	//---------------------------------------------------------------------------
	private class ViewSeedsListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			rp.showBreakScreen (BreakScreen.VIEW_SEEDS); 
		}
	}


	//---------------------------------------------------------------------------
	// Shows the screen to set the level of outround to break to. 
	//---------------------------------------------------------------------------
	private class SetLevelListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			rp.showBreakScreen (BreakScreen.SET_BREAK_LEVEL); 
		}
	}


	//---------------------------------------------------------------------------
	// Shows the screen to set the level of outround to break to and set which 
	// debaters break. 
	//---------------------------------------------------------------------------
	private class BreakListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			//Don't let user do breaks until all ballots from all prelim rounds 
			//have been entered. 
			for (PrelimRound round : tournament.getPrelims())
			{
				if (round.getStatus() != Round.Status.COMPLETED)
				{
					JOptionPane.showMessageDialog (rp, "Cannot break to outrounds - "
						+ "not all ballotshave\nbeen entered for " + round.getName() 
						+ ".", "Break to Elim Rounds", JOptionPane.ERROR_MESSAGE); 
					
					return;
				}
			}
		
			rp.showBreakScreen (BreakScreen.SET_BREAKS); 
		}
	}




	//---------------------------------------------------------------------------
	// Opens a screen allowing user to select which debaters, if any, are 
	// ineligible to break to the first outround. 
	//---------------------------------------------------------------------------
	private class IneligibleListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			//TODO 
		}
	}




	//---------------------------------------------------------------------------
	// Allows the user to set the number of judges in an outround. 
	//---------------------------------------------------------------------------
	private class NumJudgesListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			Integer[] choices = {new Integer(1), new Integer(3), new Integer(5)};
		
			Object choice = JOptionPane.showInputDialog (rp, "Select the number "
								+ "of judges in each debate for this round:", 
								"Panel Size", JOptionPane.PLAIN_MESSAGE, null, 
								choices, new Integer(round.getNumJudges()));
			
			if (choice == null) //if user clicks cancel
				return; 
			
			int num = ((Integer) choice).intValue();
			round.setNumJudges (num); 
			rp.getTournamentFrame().refresh(); 
		}
	}



	
}





















