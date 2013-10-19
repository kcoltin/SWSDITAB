//*****************************************************************************
// ResidentsScreen.java
// Kevin Coltin 
// 
// Screen that allows the user to select which students live in a particular 
// dorm room that is being used for practice rounds.  This is so the program 
// can make sure to assign them to that room so that they can unlock it. 
//*****************************************************************************

 
 

import javax.swing.*; 
import java.awt.*; 
import java.awt.event.*; 
import java.util.ArrayList; 

class ResidentsScreen extends JFrame 
{
	private static final Dimension DEFAULT_SIZE = new Dimension (500, 500); 

	private CreateRoomScreen crs; 
	private Tournament tournament; 
	private Room room; 
	
	private JScrollPane scrollPane; 
	private JPanel mainPanel, topPanel; 
	
	private JLabel nameLabel; 
	private JButton clear, done; 
	private ArrayList<StudentBox> studentBoxes; 
	
	
	//--------------------------------------------------------------------------
	// Constructor 
	//--------------------------------------------------------------------------
	ResidentsScreen (Room r, Tournament t, CreateRoomScreen screen)
	{
		super ("Dorm room residents"); 
		
		room = r; 
		tournament = t; 
		crs = screen; 
		
		if (room == null)
			nameLabel = new JLabel ("New room - Residents"); 
		else
			nameLabel = new JLabel (room.getName() + " - Residents"); 

		clear = new JButton ("Clear all"); 
		clear.addActionListener (new ClearListener()); 
		done = new JButton ("Done"); 
		done.addActionListener (new DoneListener()); 
		
		topPanel = new JPanel();
		topPanel.setLayout (new BoxLayout (topPanel, BoxLayout.X_AXIS)); 
		topPanel.add(nameLabel); 
		topPanel.add(clear); 
		topPanel.add(done); 
		
		mainPanel = new JPanel(); 
		mainPanel.setLayout (new BoxLayout (mainPanel, BoxLayout.Y_AXIS)); 
		mainPanel.add(topPanel); 
		
		studentBoxes = new ArrayList<StudentBox>(); 
		
		for (Competitor student : tournament.getCompetitors()) 
		{
			StudentBox sb = new StudentBox (student); 
			studentBoxes.add(sb); 
			mainPanel.add(sb); 
		}
		
		scrollPane = new JScrollPane(mainPanel); 
		getContentPane().add(scrollPane); 
		
		setSize (DEFAULT_SIZE); 
		setVisible(false); //will only become visible when activated from CRS
		setDefaultCloseOperation (JFrame.HIDE_ON_CLOSE); 
	}
	
	//--------------------------------------------------------------------------
	// Returns all student selected as residents 
	//--------------------------------------------------------------------------
	ArrayList<Competitor> getResidents ()
	{
		ArrayList<Competitor> residents = new ArrayList<Competitor>(); 
		
		for (StudentBox sb : studentBoxes)
			if (sb.isSelected())
				residents.add(sb.getStudent());
		
		return residents; 
	}

	
	//--------------------------------------------------------------------------
	// Box for a given student that can be selected 
	//--------------------------------------------------------------------------
	private class StudentBox extends JCheckBox 
	{
		private Competitor student; 
	
		StudentBox (Competitor s)
		{
			super (s.getName(), room != null && room.hasResident(s)); 
			student = s; 
		}
		
		Competitor getStudent ()
		{
			return student;
		}
	}	

	//--------------------------------------------------------------------------
	// Clears all strikes 
	//--------------------------------------------------------------------------
	private class ClearListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			for (StudentBox sb : studentBoxes)
				sb.setSelected(false);
		}
	}

	//--------------------------------------------------------------------------
	// Return to previous screen
	//--------------------------------------------------------------------------
	private class DoneListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			crs.resetResidentsLabel(); 
			ResidentsScreen.this.setVisible(false); 
		}
	}
			
	

}













