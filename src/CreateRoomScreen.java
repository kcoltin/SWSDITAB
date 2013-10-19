//*****************************************************************************
// CreateRoomScreen.java
// Kevin Coltin
// 
// Screen to create, edit, or delete room  
//*****************************************************************************

 
 

import javax.swing.*; 
import java.awt.*; 
import java.awt.event.*; 

class CreateRoomScreen extends JPanel 
{
	private JScrollPane scrollPane; 
	private GridBagConstraints gbc; 
	private JPanel mainPanel; //panel contained in scroll pane 
	private JPanel topPanel, bottomPanel; 
	
	private ResidentsScreen rs; 

	private RoomsPanel rp; 
	private Tournament tournament; 
	private Room room; 
	
	private JTextField nameField; 
	private JLabel residentsLabel; 
	private JComboBox prefBox, priorityBox; 
	private JButton saveButton, cancelButton, deleteButton; 
	
	//--------------------------------------------------------------------------
	// Constructor. If room is null, it's to create a new room. 
	//--------------------------------------------------------------------------
	CreateRoomScreen (RoomsPanel panel, Tournament t, Room j)
	{
		rp = panel; 
		tournament = t; 
		room = j; 
		
		rs = new ResidentsScreen (room, tournament, this);
		
		gbc = new GridBagConstraints(); 
		
		JLabel nameLabel = new JLabel("Name as it should appear on postings:");
		nameField = new JTextField(); 
		nameField.setColumns(15);
		
		if (room != null)
			nameField.setText(room.getName()); 
		
		JLabel prefLabel = new JLabel("Rating:"); 
		prefBox = new JComboBox (Room.RoomRating.values());
		
		if (room == null)
			prefBox.setSelectedItem(Room.RoomRating.C); 
		else
			prefBox.setSelectedItem(room.getRating());
		
		JLabel priorityLabel = new JLabel("Default priority:"); 
		priorityBox = new JComboBox (Priority.PriorityLevel.values()); 
		
		if (room == null)
			priorityBox.setSelectedItem(Priority.PriorityLevel.Normal); 
		else
			priorityBox.setSelectedItem(room.getDefaultPriority()); 
		
		JButton residentsButton = new JButton("Residents:"); 
		residentsButton.addActionListener (new ResidentsListener()); 
		residentsLabel = new JLabel();
		resetResidentsLabel(); 
		
		topPanel = new JPanel(); 
		topPanel.setLayout (new GridLayout(4, 2)); 
		
		topPanel.add(nameLabel);
		topPanel.add(nameField);
		topPanel.add(prefLabel);
		topPanel.add(prefBox);
		topPanel.add(priorityLabel);
		topPanel.add(priorityBox);
		topPanel.add(residentsButton);
		topPanel.add(residentsLabel);
				
		saveButton = new JButton ("Save");
		saveButton.addActionListener(new SaveListener()); 
		cancelButton = new JButton ("Cancel");
		cancelButton.addActionListener(new CancelListener()); 
		deleteButton = new JButton ("Delete");
		deleteButton.addActionListener(new DeleteListener()); 
		
		bottomPanel = new JPanel();
		bottomPanel.setLayout (new BoxLayout(bottomPanel, BoxLayout.X_AXIS)); 
		
		bottomPanel.add(saveButton);
		bottomPanel.add(cancelButton);
		bottomPanel.add(deleteButton); 

		mainPanel = new JPanel(); 
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(topPanel);
		mainPanel.add(bottomPanel);
		
		scrollPane = new JScrollPane(mainPanel);
		add(scrollPane); 
	}
	



	//--------------------------------------------------------------------------
	// Resets residents label with correct text 
	//--------------------------------------------------------------------------
	void resetResidentsLabel()
	{
		if (rs.getResidents().isEmpty())
		{
			residentsLabel.setText("N/A");
			return;
		}
		
		String text = ""; 
			
		for (int i = 0; i < rs.getResidents().size(); i++)
		{
			if (i > 0)
				text += ", ";
				
			text += rs.getResidents().get(i).getName(); 
		}
		
		residentsLabel.setText(text); 
	}


	//--------------------------------------------------------------------------
	// Opens the screen to edit residents  
	//--------------------------------------------------------------------------
	private class ResidentsListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			rs.setVisible(true); 
		}
	}
	

	//--------------------------------------------------------------------------
	// Creates a new room or saves changes to an existing one. 
	//--------------------------------------------------------------------------
	private class SaveListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			//check the text field input is acceptable 
			if (nameField.getText() == null || nameField.getText().length() == 0)
			{
				JOptionPane.showMessageDialog (CreateRoomScreen.this, 
						"\"Name\" cannot be blank.", "Edit room name", 
						JOptionPane.ERROR_MESSAGE);
				return; 
			}
			
			if (room == null)
			{
				room = new Room (nameField.getText(), tournament); 
				tournament.addRoom (room); 
			}
			else
				room.setName (nameField.getText()); 
			
			room.setRating ((Room.RoomRating) prefBox.getSelectedItem()); 
			room.setDefaultPriority ((Priority.PriorityLevel) 
													priorityBox.getSelectedItem()); 
			room.setResidents (rs.getResidents()); 
			
			rp.restoreMainPanel();
		}
	}		
			
			

	//--------------------------------------------------------------------------
	// Returns to previous screen 
	//--------------------------------------------------------------------------
	private class CancelListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			rp.restoreMainPanel(); 
		}
	}

	//--------------------------------------------------------------------------
	// Deletes the current entry 
	//--------------------------------------------------------------------------
	private class DeleteListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			// do some other stuff 
			
			rp.restoreMainPanel(); 
		}
	}



}














