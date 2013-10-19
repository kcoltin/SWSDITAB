//*****************************************************************************
// SmartLabel.java
// Kevin Coltin 
//
// A label used on a RoundPanel that contains a reference to an object - either
// an entry, judge, or room - and can be moved and clicked on to display 
// attributes of or perform actions on that object. May also refer to a blank
// placeholder and be used to swap other items into its position. 
// 
// A Smart Label may refer to: 
//		-An Entry
//			-in a "real" Debate, i.e. a Debate in itemsOnPairings 
//			-in a pseudo-Debate, on the bottom panel 
//			-unassigned to any Debate, shown on the bottom panel (container==null)
//		-A Judge
//			-in a Debate in itemsOnPairings 
//			-in a JudgeAssignment, in itemsOnPairings 
//			-in a JudgeRoomAssignment, in itemsOnPairings 
//			-unassigned, shown on the bottom panel (container==null)
//		-A Room 
//			-in a Debate in itemsOnPairings 
//			-in a RoomAssignment, in itemsOnPairings 
//			-in a JudgeRoomAssignment, in itemsOnPairings 
//			-unassigned, shown on the bottom panel (container==null)
//		-A null object, identified with an item from enum BlankType, in an 
//		 itemsOnPairings object 
//			-Types: Left Debater, Right Debater, Judge, Room 
//			-Containers: in a Debate, JudgeAssignment, RoomAssignment, 
//			 JudgeRoomAss, or BlankLine 
//
//It follows that the container may be: 
//		-A Debate 
//			-a "real" Debate, in itemsOnPairings 
//			-a pseudo-debate, on the bottom panel 
//		-a JudgeAssignment, in itemsOnPairings
//		-a RoomAssignment, in itemsOnPairings
//		-a JudgeRoomAssignment, in itemsOnPairings
//		-a BlankLine, in itemsOnPairings 
//		-a null object 
//			-either a Debater (without a bye/fft/notcompeting), Judge, or Room on
//			 the bottom panel 
//*****************************************************************************

 
 

import java.io.Serializable; 
import javax.swing.*; 
import java.awt.Color;
import java.awt.event.*;  

class SmartLabel extends JLabel implements Serializable 
{
	private static final long serialVersionUID = 41546934068943860L; 

	private RoundPanel rp; 
	private Round round;

	//object this refers to, e.g. a debater or room 
	private Object reference; 
	
	//object in the itemsOnPairings arrays that this is in - may be a Debate,
	//JudgeAssignment, RoomAssignment, JudgeRoomAssignment, or BlankLine. 
	private Flightable container; 
	
	//If it's a label referring to a null object, this indicates which column
	//of the pairings screen it is in 
	private BlankType blankType; 
	enum BlankType {LEFT_DEBATER, RIGHT_DEBATER, ROOM, JUDGE}; 
	
	//indicates whether this label is currently selected 
	private boolean selected; 
	

	//--------------------------------------------------------------------------
	// Constructor. Container may be null if the object is not assigned to a 
	// debate yet. 
	//--------------------------------------------------------------------------
	SmartLabel (RoundPanel panel, Flightable con, Object obj)
	{
		rp = panel; 
		round = rp.getRound();
		container = con; 
		reference = obj; 
		selected = false;
		
		setText (reference.toString()); 
		setUnderlined(isReferenceLocked());
		resetColor(); 

		addMouseListener (new Listener()); 
	}
	
	//--------------------------------------------------------------------------
	// Constructor for a blank item - rather than the item, it takes as an 
	// argument the type of item it is. 
	//--------------------------------------------------------------------------
	SmartLabel (RoundPanel panel, Flightable con, BlankType type)
	{
		rp = panel; 
		round = rp.getRound();
		container = con; 
		reference = null; 
		blankType = type; 
		selected = false; 
		
		setText("-"); 
		resetColor(); 
		
		addMouseListener (new BlankListener()); 
	}


	//--------------------------------------------------------------------------
	// Resets the color of the label text
	//--------------------------------------------------------------------------
	private void resetColor ()
	{
		if (container instanceof Debate //includes pseudo-debates 
			&& reference instanceof Entry)
			setForeground(((Debate) container).getTeamColor((Entry)reference));

		else if (container instanceof JudgeInhabitable 
					&& reference instanceof Judge)
			setForeground (((JudgeInhabitable) container).getJudgeColor()); 
		
		else if (container instanceof RoomInhabitable 
					&& reference instanceof Room)
			setForeground (((RoomInhabitable) container).getRoomColor()); 
		
		//lastly, set color to blue or black depending on if round has started
		else if (round.hasHappened())
			setForeground (Color.BLACK);
		else
			setForeground (Color.BLUE);
	}
	

	//---------------------------------------------------------------------------
	// Indicates whether the reference this is referring to is locked - this is 
	// just an internal method used to determine whether it should be set to 
	// locked. 
	//---------------------------------------------------------------------------
	boolean isReferenceLocked ()
	{
		if (container == null)
			return false; 
	
		if (reference instanceof Entry && container instanceof Debate)
			return ((Debate) container).isLocked ((Entry) reference);
		
		if (reference instanceof Judge)
			return ((JudgeInhabitable) container).isJudgeLocked();
		
		if (reference instanceof Room)
			return ((RoomInhabitable) container).isRoomLocked();
		
		return false; //just to satisfy compiler	
	}



	//---------------------------------------------------------------------------
	// Called when the label is deselected because something else has been 
	// clicked on. This shouldn't be called directly; rather, 
	// RoundPanel.deselectItem() should be called and will call this. 
	//---------------------------------------------------------------------------
	void deselect ()
	{
		selected = false; 
		resetColor(); 
		
		//make it non-bold and normal font size
		setText(reference.toString()); 
		setUnderlined (isReferenceLocked()); //underline if it's locked 
	}
	
	
	//--------------------------------------------------------------------------
	// Called when this is shift-clicked 
	//--------------------------------------------------------------------------
	void select ()
	{
		selected = true; 
	
		//Labels referring to a blank item may not be selected. (This method 
		//should never be called for them, so this is just a safeguard.)
		if (reference == null)
			return; 
		
		//If it is a judge or room, on the bottom panel (i.e. the container is 
		//null), which is unavailable for this round, display error message 
		if (reference instanceof Judge && container == null && ((Judge) reference
				).getPriority(round).equals(Priority.PriorityLevel.Unavailable))
		{
			JOptionPane.showMessageDialog(this, "This judge is set to "
				+ "\"unavailable\" for this round. Change judge's status in order "
				+ "to select him or her.", "Select judge", 
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (reference instanceof Room && container == null && ((Room) reference 
			).getPriority(round).equals(Priority.PriorityLevel.Unavailable))
		{
			JOptionPane.showMessageDialog(this, "This room is set to "
				+ "\"unavailable\" for this round. Change room's status in order "
				+ "to select it.", "Select room", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//make it big, bold, orange font 
		setBold(true);
		setForeground(Color.ORANGE);		
		
		//set it as the item selected on the round panel 
		rp.selectItem(this); 
	}


	
	//Accessors and mutators 
	boolean isSelected ()
	{
		return selected;
	}
	
	Object getReference ()
	{
		return reference; 
	}
	
	Flightable getContainer ()
	{
		return container; 
	}
	
	BlankType getBlankType ()
	{
		return blankType;
	}
	
	Round getRound ()
	{
		return round;
	}
	
	
	
	//Bolds the text 
	private void setBold (boolean bold)
	{
		String text = getText();
	
		if (bold)
		{
			//quit if already bold
			if (text.startsWith("<html>") && text.indexOf("<strong>") != -1)
				return;
			
			if (text.startsWith("<html>"))
				text = "<html>" + "<strong>" + text.substring(text.indexOf(">") + 1, 
						text.lastIndexOf("<")) + "</strong></html>";
			else 
				text = "<html><strong>" + text + "</strong></html>";
		}
		else 
		{
			//quit if already non-bold 
			if (text.indexOf("<strong>") == -1)
				return; 
			
			//test to see if it was underlined
			boolean underlined = text.indexOf("<u>") != -1; 
			
			//remove all tags
			while (text.indexOf(">") != -1)
				text = text.substring(text.indexOf(">") + 1, text.lastIndexOf("<")); 
			
			//restore underlining if necessary 
			if (underlined)
				setUnderlined(true);
		}
		
		setText(text);
	}



	//Underlines the text 
	private void setUnderlined (boolean underlined)
	{
		String text = getText(); 
	
		if (underlined)
		{
			//quit if already underlined
			if (text.startsWith("<html>") && text.indexOf("<u>") != -1)
				return;
			
			if (text.startsWith("<html>"))
				text = "<html>" + "<u>" + text.substring(text.indexOf(">") + 1, 
						text.lastIndexOf("<")) + "</u></html>";
			else 
				text = "<html><u>" + text + "</u></html>";
		}
		else 
		{
			//quit if already non-underlined 
			if (text.indexOf("<u>") == -1)
				return; 
			
			//test to see if it was bold
			boolean bold = text.indexOf("<strong>") != -1; 
			
			//remove all tags
			while (text.indexOf(">") != -1)
				text = text.substring(text.indexOf(">") + 1, text.lastIndexOf("<")); 
			
			//restore bold if necessary 
			if (bold)
				setBold(true);
		}
		
		setText(text); 
	}


	//---------------------------------------------------------------------------
	// Indicates whether two SmartLabels refer to the same thing - room, entry, 
	// or judge. 
	//---------------------------------------------------------------------------
	private boolean isSameReferenceType (SmartLabel other)
	{
		Object otherRef = other.getReference();
		BlankType otherType = otherRef == null ? other.getBlankType() : null;
		
		if (reference instanceof Entry || (reference == null 
			&& (blankType == BlankType.LEFT_DEBATER 
			|| blankType == BlankType.RIGHT_DEBATER)))
		{
			return otherRef instanceof Entry || (otherRef == null 
				&& (otherType == BlankType.LEFT_DEBATER 
				|| otherType == BlankType.RIGHT_DEBATER));
		}
		
		else if (reference instanceof Judge 
				|| (reference == null && blankType == BlankType.JUDGE))
		{
			return otherRef instanceof Judge 
				|| (otherRef == null && otherType == BlankType.JUDGE);
		}
		
		assert reference instanceof Room 
				|| reference == null && blankType == BlankType.ROOM;
		
		//else, if it's a room 
		return otherRef instanceof Room 
			|| (otherRef == null && otherType == BlankType.ROOM);
	}

	
	//---------------------------------------------------------------------------
	// Indicates whether this SmartLabel is for an object on the BottomPanel, 
	// i.e. anything in a null container or an Entry in a pseudo debate. 
	//---------------------------------------------------------------------------
	private boolean isOnBottomPanel ()
	{
		return container == null || (container instanceof Debate 
				&& ((Debate) container).isPseudoDebate());
	}
	
	

	
	
	//---------------------------------------------------------------------------
	// This is what happens when you shift-click on this label (and it's not a 
	// null reference). 
	//---------------------------------------------------------------------------
	void shiftClick ()
	{
		SmartLabel other = rp.getSelectedItem();
		
		//If no other item is selected, select this one. 
		if (other == null)
		{
			SmartLabel.this.select();
			return;
		}
		
		//Deselect the other item. (This will occur no matter what other
		//conditions are met, below.)
		rp.deselectItem();
		
		//If user clicked on the currently selected label, do nothing else
		if (other == SmartLabel.this)
			return;
		
		//If an item of a different type is selected, or if both items are 
		//on the bottom panel, select this one. 
		if (SmartLabel.this.isSameReferenceType(other) == false 
			|| SmartLabel.this.isOnBottomPanel() && other.isOnBottomPanel())
		{
			SmartLabel.this.select();
			return;
		}

		//Now, both items are of the same type. 
		
		//If it's a different Judge or judge blank space in the same 
		//container (because it's a round with multiple judges per debate),
		//don't do anything. 
		if ((reference instanceof Judge || (reference == null 
			&& blankType == BlankType.JUDGE)) 
			&& container != null && other != null && other.getContainer() != null
			&& container.equals(other.getContainer()))
			return;
			
		//If a ballot is entered for either item, say that you can't swap 
		//until ballots are unentered. 
		if (round.hasHappened() && ((container instanceof Debate 
			&& ((Debate) container).isBallotEntered()) 
			|| (other.getContainer() instanceof Debate 
			&& ((Debate) other.getContainer()).isBallotEntered())))
		{
			JOptionPane.showMessageDialog(rp, "You cannot make changes "
				+ "to a debate that has\nalready happened. If you must make "
				+ "this change, please\nun-enter the ballot first.", 
				"Make change on pairings", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//If the round has happened but neither ballot is entered, still 
		//warn user before continuing 
		if (round.hasHappened())
		{
			int status = JOptionPane.showConfirmDialog(SmartLabel.this, 
								"This round has already started. Are you sure "
								+ "you want to make this change?", 
								"Make change on pairings", 
								JOptionPane.YES_NO_OPTION, 
								JOptionPane.WARNING_MESSAGE);
			
			if (status == JOptionPane.NO_OPTION)
				return;
		}
		
		//Swap the two references 
		rp.swap (reference, container, other.getReference(), 
						other.getContainer());
			return;
	}



	//---------------------------------------------------------------------------
	// Method that gets called if you shift-click on a blank label. 
	//---------------------------------------------------------------------------
	void shiftClickBlank ()
	{
		SmartLabel other = rp.getSelectedItem();
		
		//If no other item is selected, do nothing. 
		if (other == null)
			return;

		//Deselect the other item. (This will occur no matter what other
		//conditions are met, below.)
		rp.deselectItem();


		//If an item of a different type is selected, do nothing. 
		if (SmartLabel.this.isSameReferenceType(other) == false 
			|| SmartLabel.this.isOnBottomPanel() && other.isOnBottomPanel())
			return;
		
		//Now, both items are of the same type. 

		//If it's a different Judge or judge blank space in the same 
		//container (because it's a round with multiple judges per debate),
		//don't do anything. 
		if ((reference instanceof Judge || (reference == null 
			&& blankType == BlankType.JUDGE)) 
			&& container.equals(other.getContainer()))
			return;

		//If a ballot is entered for either item, say that you can't swap 
		//until ballots are unentered. 
		if (round.hasHappened() && (other.getContainer() instanceof Debate 
				&& ((Debate) other.getContainer()).isBallotEntered()))
		{
			JOptionPane.showMessageDialog(rp, "You cannot make changes "
				+ "to a debate that has\nalready happened. If you must make "
				+ "this change, please\nun-enter the ballot first.", 
				"Make change on pairings", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//If the round has happened but neither ballot is entered, still 
		//warn user before continuing 
		if (round.hasHappened())
		{
			int status = JOptionPane.showConfirmDialog(SmartLabel.this, 
								"This round has already started. Are you sure "
								+ "you want to make this change?", 
								"Make change on pairings", 
								JOptionPane.YES_NO_OPTION, 
								JOptionPane.WARNING_MESSAGE);
			
			if (status == JOptionPane.NO_OPTION)
				return;
		}
		
		//Swap the two references 
		rp.swap (reference, container, other.getReference(), 
						other.getContainer(), blankType);
		
		return;
	}


	//---------------------------------------------------------------------------
	// Returns an array of the conflicts in this label, sorted in descending 
	// error of importance. Only includes those that are visible to the user - 
	// excludes "internal only"'s. 
	//---------------------------------------------------------------------------
	Conflict[] getConflicts ()
	{
		Conflict[] conflicts = null; 
		ConflictChecker.setRound(round); 

		if (container instanceof Debate)
			conflicts = ConflictChecker.checkForConflict((Debate) container);
		else if (container instanceof JudgeRoomAssignment)
			conflicts = ConflictChecker.checkForConflict(
															(JudgeRoomAssignment) container);
		else if (container instanceof JudgeAssignment)
			conflicts = ConflictChecker.checkForConflict(
															(JudgeAssignment) container);
		else if (container instanceof RoomAssignment)
			conflicts = ConflictChecker.checkForConflict(
															(RoomAssignment) container);
		
		//If conflicts is null, return null 
		if (conflicts == null)
			return null; 
		
		//Array containing conflicts sorted by value
		Conflict[] sorted = new Conflict[conflicts.length];
		
		int i = 0; 
		
		while (i < sorted.length)
		{
			int max = 0;
		
			//find remaining conflict with maximum point value  
			for (int j = 0; j < conflicts.length; j++)
			{
				//Error conflicts come first, followed by warning conflicts 
				if (conflicts[j] != null 
					&& conflicts[j].problem.value == Conflict.ERROR_VALUE)
				{
					max = j; 
				}
				
				else if (conflicts[j] != null 
					&& conflicts[j].problem.value == Conflict.WARNING_VALUE
					&& conflicts[max] != null
					&& conflicts[max].problem.value != Conflict.ERROR_VALUE)
				{
					max = j; 
				}
			
				else if (conflicts[j] != null 
					&& conflicts[max] != null 
					&& conflicts[max].problem.value != Conflict.ERROR_VALUE 
					&& conflicts[max].problem.value != Conflict.WARNING_VALUE
					&& conflicts[j].source != Conflict.INTERNAL_ONLY 
					&& conflicts[j].problem.value > conflicts[max].problem.value)
				{
					max = j; 
				}
			}
		
			//Add conflict to sorted array in order 
			if (conflicts[max] != null 
				&& conflicts[max].source != Conflict.INTERNAL_ONLY)
			{
				sorted[i] = conflicts[max]; 
				conflicts[max] = null; 
			}
			
			i++; 
		}
		
		//convert to a new array that is the correct length 
		int length = 0; 
		while (length < sorted.length && sorted[length] != null)
			length++; 
		
		Conflict[] out = new Conflict[length]; 
		
		for (i = 0; i < length; i++)
			out[i] = sorted[i];
		
		return out; 
	}


	
	//---------------------------------------------------------------------------
	// This is the listener class for all non-blank SmartLabels, i.e. all that 
	// refer to actual objects. Note: A SmartLabel with a null reference cannot
	// be selected. It can however be shift-clicked after another object is 
	// selected to swap the items. 
	//---------------------------------------------------------------------------
	private class Listener implements MouseListener 
	{
		public void mousePressed (MouseEvent event)
		{
			if (event.isShiftDown())
			{
				shiftClick();
			}
		

			//Right-click or ctrl-click causes a popup menu to appear, defined in 
			//class SLPopupMenu 
			if (event.isControlDown() || event.isMetaDown())
			{
				SLPopupMenu popupMenu = new SLPopupMenu (SmartLabel.this); 
				popupMenu.show(SmartLabel.this, 0, 0);				
			}
		}

		
		
		//Other methods of interface - need not be implemented 
		public void mouseClicked(MouseEvent event) {}
		public void mouseReleased(MouseEvent event) {}
		public void mouseEntered(MouseEvent event) {}
		public void mouseExited(MouseEvent event) {}
	}


	//--------------------------------------------------------------------------
	// This is the listener class for all blank SmartLabels.
	//--------------------------------------------------------------------------
	private class BlankListener implements MouseListener 
	{
		public void mousePressed (MouseEvent event)
		{
			if (event.isShiftDown())
			{
				shiftClickBlank(); 
			}

			//Right-click or ctrl-click causes a popup menu to appear, defined in 
			//class SLPopupMenu 
			if (event.isControlDown() || event.isMetaDown())
			{
				SLPopupMenu popupMenu = new SLPopupMenu (SmartLabel.this); 
				popupMenu.show(SmartLabel.this, 0, 0);	
			}

		}
	
		//Other methods of interface - need not be implemented 
		public void mouseClicked(MouseEvent event) {}
		public void mouseReleased(MouseEvent event) {}
		public void mouseEntered(MouseEvent event) {}
		public void mouseExited(MouseEvent event) {}
	}
	
	
}







