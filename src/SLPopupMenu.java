//******************************************************************************
// SLPopupMenu.java
// Kevin Coltin  
//
//		IF IT'S NOT A BLANK SMARTLABEL: 
			/*
			Right-click or ctrl-click Cause a popup menu to appear. Menu options: 
			
			-lock (if unlocked) or unlock (if locked) 
			-select (just calls the method that happens when you shift-click on it) 
			-view info (just calls the method that happens when you alt-click on it) 
			-if label is red, see warning message (this will be disabled if it's not red)
			-if object is a member of "items on pairings", remove (same as swapping with a blank
				label at the bottom, in unassigned area) (otherwise this will be disabled)
			-switch flight (moves the entire itemsOnPairings object to the other flight) (disable
				if round is not flighted or if it's not an itemsOnpairings item) 
				-switch this itemsOnPairings object to the other flight's arraylist 
				-then, call tournamentframe.refresh(). 
			*/
//
		//IF IT'S A BLANK SMARTLABEL: 
//			if (event.isControlDown() || event.isMetaDown())
			/*
			Cause a popup menu to appear. Menu options: 
			
			-select (just calls the method that happens when you shift-click on it) 
			-switch flight (moves the entire itemsOnPairings object to the other flight) (disable
				if round is not flighted or if it's not an itemsOnpairings item) 
			*/
		
//******************************************************************************

 
 

import javax.swing.*; 
import java.awt.*; 
import java.awt.event.*; 

class SLPopupMenu extends JPopupMenu 
{
	private SmartLabel label; 

	//---------------------------------------------------------------------------
	// Constructor 
	//---------------------------------------------------------------------------
	SLPopupMenu (SmartLabel sl)	
	{
		label = sl; 
		
		if (label.getReference() != null)
			initialize(); 
		else
			initializeBlank(); 
	}
		


	//---------------------------------------------------------------------------
	// Pseudo constructor, for when it's not a blank smart label 
	//---------------------------------------------------------------------------
	private void initialize ()
	{
		//Labels showing warning messages, if applicable 
		if (label.getConflicts() != null)
			for (Conflict conflict : label.getConflicts())
				add (new JMenuItem(conflict.problem.explanation));
		
		if (label.getConflicts() != null && label.getConflicts().length != 0)
			add (new JSeparator()); 
		
		//Button that shows info about this item - same as when you alt-click it
		JMenuItem showInfo = new JMenuItem ("Show info"); 
		showInfo.addActionListener (new ShowInfoListener()); 
		add (showInfo); 
		
		//Button to remove an item from itemsOnPairings and put in at the bottom 
		//in an unassigned panel 
		JMenuItem remove = new JMenuItem ("Remove"); 
		remove.addActionListener (new RemoveListener()); 
		add (remove); 
		//This should be disabled if it's already at the bottom, either in a null 
		//container or a pseudo debate
		if (label.getContainer() == null 
			|| (label.getContainer() instanceof Debate 
			&& ((Debate) label.getContainer()).isPseudoDebate()))
		{
			remove.setEnabled (false); 
		}
		
		//Add button to lock or unlock item 
		if (label.isReferenceLocked())
		{
			JMenuItem lock = new JMenuItem ("Lock"); 
			lock.addActionListener (new LockListener());
			add (lock); 
		}
		else
		{
			JMenuItem unlock = new JMenuItem ("Unlock"); 
			unlock.addActionListener (new UnlockListener());
			add (unlock); 
		}
		
		//Button that selects the label - same as shift clicking on it 
		JMenuItem select = new JMenuItem ("Select"); 
		select.addActionListener (new SelectListener()); 
		add (select); 
		
		//Button to move this entire container object to the other flight 
		JMenuItem switchFlight = new JMenuItem ("Switch flight"); 
		switchFlight.addActionListener (new SwitchFlightListener()); 
		add (switchFlight); 
		//Disable if it's not in items on pairings or if the round isn't flighted
		if (label.getRound().isFlighted() == false 
			|| (label.getContainer() == null 
			|| (label.getContainer() instanceof Debate 
			&& ((Debate) label.getContainer()).isPseudoDebate())))
			switchFlight.setEnabled(false); 
	}
		
		
	//---------------------------------------------------------------------------
	// Pseudo constructor, for when it is a blank smart label 
	//---------------------------------------------------------------------------
	private void initializeBlank ()
	{
		//Button that selects the label - same as shift clicking on it 
		JMenuItem select = new JMenuItem ("Select"); 
		select.addActionListener (new SelectListener()); 
		add (select); 
		
		//Button to move this entire container object to the other flight 
		JMenuItem switchFlight = new JMenuItem ("Switch flight"); 
		switchFlight.addActionListener (new SwitchFlightListener()); 
		add (switchFlight); 
		//Disable if it's not in items on pairings or if the round isn't flighted
		if (label.getRound().isFlighted() == false 
			|| (label.getContainer() == null 
			|| (label.getContainer() instanceof Debate 
			&& ((Debate) label.getContainer()).isPseudoDebate())))
			switchFlight.setEnabled(false); 
	}
	
	
	
	//---------------------------------------------------------------------------
	// Listener to remove an item from items on pairings and put in on the bottom
	// panel 
	//---------------------------------------------------------------------------
	private class RemoveListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			//Don't allow it if ballot is entered 
			if (label.getRound() instanceof PracticeRound == false
				&& label.getRound().hasHappened() 
				&& label.getContainer() instanceof Debate 
				&& ((Debate) label.getContainer()).isBallotEntered())
			{
				JOptionPane.showMessageDialog(label.getRound().getPanel(), 
					"Cannot make changes - ballot has already been entered. Remove\n"
					+ "ballot before making any changes.", "Pair Round", 
					JOptionPane.ERROR_MESSAGE);
				return;
			}

			//Show warning if round has started 
			if (label.getRound().hasHappened())
			{
				int choice = JOptionPane.showConfirmDialog(
					label.getRound().getPanel(), "Round has already started - are "
					+ "you sure\nyou want to make this change?", "Pair Round",  
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				
				if (choice != JOptionPane.YES_OPTION)
					return;
			}
			
			label.getRound().getPanel().removeFromIOP (label.getReference(), 
																		label.getContainer());
		}
	}


	private class ShowInfoListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			//TODO 
		}
	}


	private class LockListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			//TODO 
		}
	}


	private class UnlockListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			//TODO 
		}
	}


	//---------------------------------------------------------------------------
	// Selects it, just like shift-clicking on the label 
	//---------------------------------------------------------------------------
	private class SelectListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			label.select(); 
		}
	}


	//---------------------------------------------------------------------------
	// Moves to the other flight 
	//---------------------------------------------------------------------------
	private class SwitchFlightListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			if (label.getContainer().getFlight() == 'A')
				label.getContainer().setFlight('B');
			else
				label.getContainer().setFlight('A');
			
			label.getRound().getPanel().getTournamentFrame().refresh(); 
		}
	}

}











