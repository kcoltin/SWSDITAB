//*****************************************************************************
// RoomsPanel.java
// Kevin Coltin 
//
// Panel from which the user can view and edit rooms used to hold debates.
//*****************************************************************************

 
 

import javax.swing.*; 
import java.awt.*; 
import java.awt.event.*; 

class RoomsPanel extends JScrollPane 
{
	private Tournament tournament; 
	private TournamentFrame tf; 
	
	private GridBagConstraints gbc; 
	private JPanel mainPanel; //panel to go in the scroll pane 
	
	//currently expanded room
	private Room expandedRoom; 
	
	//--------------------------------------------------------------------------
	// Constructor 
	//--------------------------------------------------------------------------
	RoomsPanel (Tournament t, TournamentFrame frame)
	{
		tournament = t; 
		tf = frame; 
		
		mainPanel = new JPanel();
		
		resetRooms (null); 
		
		setViewportView(mainPanel); 
	}


	//--------------------------------------------------------------------------
	// Recreates the screen, listing each room and, optionally, showing the 
	// full round-by-round details for one room. 
	//--------------------------------------------------------------------------
	void resetRooms (Room roomToExpand)
	{
		mainPanel.removeAll(); 
		mainPanel.setLayout (new GridBagLayout());
		
		expandedRoom = roomToExpand; 
		
		JLabel roomLabel = new JLabel("<html><u>Room</u></html>");
		JButton newButton = new JButton ("New room");
		newButton.addActionListener (new NewListener());	
		JLabel pref = new JLabel("<html><u>Rating</u></html>");
		JLabel priority = new JLabel("<html><u>Priority</u></html>");
		
		gbc = new GridBagConstraints();
		
		gbc.gridx = 0; 
		gbc.gridy = 0; 
		mainPanel.add(roomLabel, gbc); 

		gbc.gridx = 1; 
		gbc.gridy = 0; 
		mainPanel.add(newButton, gbc); 
		
		gbc.gridx = 2; 
		gbc.gridy = 0; 
		mainPanel.add(pref, gbc); 

		gbc.gridx = 3; 
		gbc.gridy = 0; 
		mainPanel.add(priority, gbc); 

		int gridy = 1; 
		
		for (int i = 0; i < tournament.getRooms().size(); i++)
		{
			Room room = tournament.getRooms().get(i); 
			listRoom (room, gridy, expandedRoom != null 
							&& room.equals(expandedRoom));
			gridy++;
			
			if (expandedRoom != null && room.equals(expandedRoom))
			{
				for (int j = 0; j < tournament.getRounds().size(); j++)
				{
					Round round = tournament.getRounds().get(j);
					listRound (round, room, gridy);

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
	// Puts the basic information for this room as a line on the screen 
	//--------------------------------------------------------------------------
	void listRoom (Room room, int gridy, boolean isExpanded)
	{
		JLabel name = new JLabel(room.getName());

		JButton edit = new JButton ("Edit"); 
		edit.addActionListener (new EditListener(room)); 

		JButton expand;
		
		if (isExpanded)
		{
			expand = new JButton("Collapse");
			expand.addActionListener (new CollapseListener()); 
		}
		else
		{
			expand = new JButton("Expand");
			expand.addActionListener (new ExpandListener(room)); 
		}

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS)); 
		buttonPanel.add(edit);
		buttonPanel.add(expand); 

		JLabel rating = new JLabel(room.getRating().toString()); 
		JLabel priority = new JLabel(room.getDefaultPriority().toString()); 
		
		gbc.gridx = 0; 
		gbc.gridy = gridy; 
		mainPanel.add(name, gbc); 
		
		gbc.gridx = 1; 
		gbc.gridy = gridy; 
		mainPanel.add(buttonPanel, gbc); 
		
		gbc.gridx = 2; 
		gbc.gridy = gridy; 
		mainPanel.add(rating, gbc); 
		
		gbc.gridx = 3; 
		gbc.gridy = gridy; 
		mainPanel.add(priority, gbc); 
	}


	//--------------------------------------------------------------------------
	// Puts the basic information for this round as a line on the screen, under
	// its "expanded" corresponding room.  
	//--------------------------------------------------------------------------
	void listRound (Round round, Room room, int gridy)
	{
		listFlight (round, room, gridy, 'A');
		
		if (round.isFlighted())
			listFlight (round, room, gridy + 1, 'B');	
	}


	//--------------------------------------------------------------------------
	// Lists the info about a particular debate/flight in a round. This is the 
	// method that does the heavy lifting for listRound(). 
	//--------------------------------------------------------------------------
	void listFlight (Round round, Room room, int gridy, char flight)
	{
		Debate debate = round.getDebate(room, flight); 
		
		JButton roundButton = new JButton (round.getName() + (round.isFlighted() 
																				? flight : "")); 
		roundButton.addActionListener (new RoundListener(round)); 
		
		JComboBox priority = new JComboBox(Priority.PriorityLevel.values());
		priority.setSelectedItem (room.getPriority(round)); 
		priority.addActionListener (new PriorityListener(room, round)); 

		gbc.gridx = 0; 
		gbc.gridy = gridy; 
		mainPanel.add(roundButton, gbc); 
		
		gbc.gridx = 4; 
		gbc.gridy = gridy; 
		mainPanel.add(priority, gbc); 
			
		if (debate == null) //if the room is not assigned to a debate this round
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

		JLabel judgeLabel; 
		
		if (debate.getJudges().isEmpty())
			judgeLabel = new JLabel("-");
		else
			judgeLabel = new JLabel(debate.getJudgeString());
		
		gbc.gridx = 1; 
		gbc.gridy = gridy; 
		mainPanel.add(matchup, gbc); 

		gbc.gridx = 2; 
		gbc.gridy = gridy; 
		mainPanel.add(judgeLabel, gbc); 
	}


	//--------------------------------------------------------------------------
	// Called after the create room screen is closed - this removes the room
	// screen and restores the main panel. 
	//--------------------------------------------------------------------------
	void restoreMainPanel ()
	{
		setViewportView(mainPanel);
		tf.refresh(); 
	}



	//---------------------------------------------------------------------------
	// Returns currently expanded room
	//---------------------------------------------------------------------------
	Room getExpandedRoom()
	{
		return expandedRoom;
	}
	
	
	//---------------------------------------------------------------------------
	// Indicates whether a CreateRoomScreen is currently open, as opposed to 
	// the main panel of the RoomsPanel itself being visible.
	//---------------------------------------------------------------------------
	boolean isEditScreenOpen ()
	{
		return getViewport().getView() instanceof CreateRoomScreen;
	}
		

	//--------------------------------------------------------------------------
	// Listener for button to create a new room 
	//--------------------------------------------------------------------------
	private class NewListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			CreateRoomScreen crs = new CreateRoomScreen (RoomsPanel.this, 
																			tournament, null);
			RoomsPanel.this.setViewportView(crs);

			//reset expanded view 
			expandedRoom = null; 
		}
	}


	//--------------------------------------------------------------------------
	// Listener for button to edit a room 
	//--------------------------------------------------------------------------
	private class EditListener implements ActionListener 
	{
		private Room room; 
		
		EditListener (Room r)
		{
			room = r;
		}
		
		public void actionPerformed (ActionEvent event)
		{
			CreateRoomScreen crs = new CreateRoomScreen (RoomsPanel.this,
																			tournament, room);
			RoomsPanel.this.setViewportView(crs); 
			
			//reset expanded view 
			expandedRoom = null; 
		}
	}


	//--------------------------------------------------------------------------
	// Listener for button to expand the info about a room 
	//--------------------------------------------------------------------------
	private class ExpandListener implements ActionListener 
	{
		private Room room; 
	
		ExpandListener (Room r)
		{
			room = r;
		}
		
		public void actionPerformed (ActionEvent event)
		{
			resetRooms (room); 
		}
	}


	//--------------------------------------------------------------------------
	// Listener for button to collapse/hide the info about a room 
	//--------------------------------------------------------------------------
	private class CollapseListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			resetRooms (null); 
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
	// Listener for the menu the user can use to set the room's priority level
	// for each round 
	//--------------------------------------------------------------------------
	private class PriorityListener implements ActionListener 
	{
		private Room room; 
		private Round round; 
		
		PriorityListener (Room rm, Round rd)
		{
			room = rm; 
			round = rd; 
		}
	
		public void actionPerformed (ActionEvent event)
		{
			JComboBox cb = (JComboBox) event.getSource(); 
			Priority.PriorityLevel level; 
			level = (Priority.PriorityLevel) cb.getSelectedItem(); 
			room.setPriority(round, level); 
		}
	}
		

	
	
	
}









