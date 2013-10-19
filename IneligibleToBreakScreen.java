//******************************************************************************
// IneligibleToBreakScreen.java
// Kevin Coltin  
//
// Screen that appears from BreakScreen that lets the user select who is and 
// isn't eligible to break. The main reasons why we would want to make an entry
// ineligible is if it's a PFD team that was just created temporarily and its 
// members are not debating together now, or if it's a student who has to leave 
// camp early. 
//******************************************************************************

 
 

import javax.swing.*; 
import java.awt.*; 
import java.awt.event.*; 
import java.util.ArrayList; 

class IneligibleToBreakScreen extends JPanel 
{
	private RoundPanel rp; 
	private BreakScreen bs; 
	private Tournament tournament; 
	
	private ArrayList<EntryBox> entryBoxes; 
	
	//---------------------------------------------------------------------------
	// Constructor 
	//---------------------------------------------------------------------------
	IneligibleToBreakScreen (BreakScreen screen)
	{
		bs = screen; 
		rp = bs.getRoundPanel(); 
		tournament = rp.getRound().getTournament(); 
		
		setLayout (new BoxLayout (this, BoxLayout.Y_AXIS)); 
		
		JLabel header = new JLabel("Select Entries Ineligible to Break"); 
		JButton clear = new JButton ("Clear all"); 
		clear.addActionListener (new ClearListener()); 
		JButton done = new JButton ("Done"); 
		done.addActionListener (new DoneListener()); 
		JButton cancel = new JButton ("Cancel"); 
		cancel.addActionListener (new CancelListener()); 
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout (new BoxLayout (topPanel, BoxLayout.X_AXIS)); 
		topPanel.add(header); 
		topPanel.add(clear); 
		topPanel.add(done); 
		topPanel.add(cancel); 
		
		add(topPanel); 
		
		entryBoxes = new ArrayList<EntryBox>(); 
		
		for (Entry entry : tournament.getEntries()) 
		{
			EntryBox eb = new EntryBox (entry); 
			entryBoxes.add(eb); 
			add(eb); 
		}
	}


	//--------------------------------------------------------------------------
	// Returns all entries selected as ineligible 
	//--------------------------------------------------------------------------
	ArrayList<Entry> getIneligibleEntries ()
	{
		ArrayList<Entry> ineligibles = new ArrayList<Entry>(); 
		
		for (EntryBox eb : entryBoxes)
			if (eb.isSelected())
				ineligibles.add(eb.getEntry());
		
		return ineligibles; 
	}

	
	//--------------------------------------------------------------------------
	// Box for a given entry that can be selected 
	//--------------------------------------------------------------------------
	private class EntryBox extends JCheckBox 
	{
		private Entry entry; 
	
		EntryBox (Entry e)
		{
			super (e.toString(), e.isIneligibleToBreak()); 
			entry = e; 
		}
		
		Entry getEntry ()
		{
			return entry;
		}
	}	

	//--------------------------------------------------------------------------
	// Clears all ineligible entries  
	//--------------------------------------------------------------------------
	private class ClearListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			for (EntryBox eb : entryBoxes)
				eb.setSelected(false);
		}
	}

	//--------------------------------------------------------------------------
	// Return to previous screen without doing anything 
	//--------------------------------------------------------------------------
	private class CancelListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			rp.showBreakScreen (bs.getPurpose()); 
		}
	}
	
	

	//--------------------------------------------------------------------------
	// Return to previous screen and set ineligible entries accordingly. 
	//--------------------------------------------------------------------------
	private class DoneListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			//If the first outround has already started, display an error message. 
			if (tournament.getElims().isEmpty() == false 
				&& tournament.getElims().get(0).hasHappened())
			{
				JOptionPane.showMessageDialog (rp, 
					tournament.getElims().get(0).getName() + " has already started. "
					+ "Cannot\nmake changes to who can break.", "Set entries " 
					+ "ineligible to break", JOptionPane.ERROR_MESSAGE);
				return; 
			}
			
			//If breaks have already been set (but first outround has not started),
			//display a warning message. 
			if (tournament.getBreaks().isEmpty() == false)
			{
				int choice = JOptionPane.showConfirmDialog (rp, "The entries "
						+ "breaking to elimination rounds\nhave already been set. "
						+ "Are you sure you want to change\nwho is eligible to " 
						+ "break? If you click \"Yes\", it will reset all breaks.",
						"Set entries ineligible to break", JOptionPane.YES_NO_OPTION, 
						JOptionPane.WARNING_MESSAGE);
					
				if (choice == JOptionPane.YES_OPTION)
					tournament.getBreaks().clear(); 
				else if (choice == JOptionPane.NO_OPTION)
					return; 
			}
		
			for (EntryBox eb : entryBoxes)
					eb.getEntry().setIneligibleToBreak(eb.isSelected());

			rp.showBreakScreen (bs.getPurpose()); 
		}
	}

}


















