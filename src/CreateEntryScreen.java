//*****************************************************************************
// CreateEntryScreen.java
// Kevin Coltin
// 
// Screen to create, edit, or delete entry  
//*****************************************************************************

 
 

import javax.swing.*; 
import java.awt.*; 
import java.awt.event.*; 

class CreateEntryScreen extends JPanel 
{
	private JScrollPane scrollPane; 
	private GridBagConstraints gbc; 
	private JPanel mainPanel; //panel contained in scroll pane 
	private JPanel topPanel, bottomPanel; 

	private EntriesPanel ep; 
	private Tournament tournament; 
	private Entry entry; 
		
	private JButton saveButton, cancelButton, deleteButton; 
	
	//for first and (if applicable) second partner 
	private JTextField[] firstNames, lastNames, labs;
	private JComboBox[] schoolBoxes; 
	private Object [] schools; //list of schools used in schoolBoxes 
	
	//--------------------------------------------------------------------------
	// Constructor. If entry is null, it's to create a new entry. 
	//--------------------------------------------------------------------------
	CreateEntryScreen (EntriesPanel panel, Tournament t, Entry e)
	{
		ep = panel; 
		tournament = t; 
		entry = e; 
		
		topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout()); 
		gbc = new GridBagConstraints(); 
		
		firstNames = new JTextField[2]; 
		lastNames = new JTextField[2]; 
		labs = new JTextField[2]; 
		schoolBoxes = new JComboBox[2]; 
		
		makeDebaterFields(1); 
		
		if (tournament.getEvent().getKidsPerTeam() == 2)
			makeDebaterFields(2); 
		
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
	// Creates the fields to enter information about the debater - the int 
	// argument indicates whether it's the first (on the left) or second (on the
	// right) debater being added. 
	//--------------------------------------------------------------------------
	private void makeDebaterFields (int n)
	{
		//n for first debaters is converted to 0 and for second to 1, simplicity
		n -= 1; 
		
		firstNames[n] = new JTextField();
		firstNames[n].setColumns(15);
		if (entry != null)
			firstNames[n].setText(entry.getStudents()[n].getFirstName());

		lastNames[n] = new JTextField();
		lastNames[n].setColumns(15);
		if (entry != null)
			lastNames[n].setText(entry.getStudents()[n].getLastName());

		//create combo box for schools 
		resetSchoolBox(n+1);
		
		labs[n] = new JTextField();
		labs[n].setColumns(3);
		if (entry != null)
			labs[n].setText(Integer.toString(entry.getStudents()[n].getLab()));
		else
			labs[n].setText(Integer.toString(0));
		
		//button that, if clicked, brings up a text screen explaining how "labs"
		//works
		JButton labsInfo = new JButton("?"); 
		labsInfo.addActionListener(new LabsInfoListener()); 
		
		JPanel labsPanel = new JPanel();
		labsPanel.setLayout(new BoxLayout(labsPanel, BoxLayout.X_AXIS));
		labsPanel.add(labs[n]);
		labsPanel.add(labsInfo);
		
		gbc.gridx = 2*n; 
		gbc.gridy = 0; 
		topPanel.add(new JLabel("First name:"), gbc);

		gbc.gridx = 2*n+1; 
		gbc.gridy = 0; 
		topPanel.add(firstNames[n], gbc);

		gbc.gridx = 2*n; 
		gbc.gridy = 1; 
		topPanel.add(new JLabel("Last name:"), gbc);

		gbc.gridx = 2*n+1; 
		gbc.gridy = 1; 
		topPanel.add(lastNames[n], gbc);
	
		gbc.gridx = 2*n; 
		gbc.gridy = 2; 
		topPanel.add(new JLabel("School:"), gbc);

		gbc.gridx = 2*n+1; 
		gbc.gridy = 2; 
		topPanel.add(schoolBoxes[n], gbc);

		gbc.gridx = 2*n; 
		gbc.gridy = 3; 
		topPanel.add(new JLabel("Lab group:"), gbc);

		gbc.gridx = 2*n+1; 
		gbc.gridy = 3; 
		topPanel.add(labsPanel, gbc);
	}




	//--------------------------------------------------------------------------
	// Updates school boxes. n is whether it's the first or second debater. With no 
	// argument, it does both. 
	//--------------------------------------------------------------------------
	private void resetSchoolBox ()
	{
		for (int i = 1; i <= tournament.getEvent().getKidsPerTeam(); i++)
			resetSchoolBox (i);
	}

	private void resetSchoolBox (int n)
	{
		n = n - 1; 
		
		//remove old box, if it exists
		try{topPanel.remove(schoolBoxes[n]); } catch(Exception e){};
		
		schools = new Object[tournament.getSchools().size()+2];
		
		schools[0] = "Select one";
		schools[1] = "Create new...";
		
		for (int i = 0; i < tournament.getSchools().size(); i++)
			schools[i+2] = tournament.getSchools().get(i);
		
		schoolBoxes[n] = new JComboBox(schools); 
		schoolBoxes[n].addActionListener(new NewSchoolListener(n+1)); 
		if (entry != null)
			schoolBoxes[n].setSelectedItem(entry.getStudents()[n].getSchool());
		else
			schoolBoxes[n].setSelectedIndex(0);

		gbc.gridx = 2*n+1; 
		gbc.gridy = 2; 
		topPanel.add(schoolBoxes[n], gbc);
		
		revalidate();
	}
	

	//--------------------------------------------------------------------------
	// Creates a new school 
	//--------------------------------------------------------------------------
	private class NewSchoolListener implements ActionListener 
	{
		int n; 
	
		//Constructor: the argument indicates whether this was selected by the
		//first or second debater. 
		NewSchoolListener (int N)
		{
			n = N - 1; 
		}
	
		public void actionPerformed (ActionEvent event)
		{
			//This doesn't do anything unless the option selected is "create new"
			JComboBox cb = (JComboBox) event.getSource();
			if (cb.getSelectedIndex() == 1)
			{
				String name = JOptionPane.showInputDialog(CreateEntryScreen.this, 
																"Enter name of new school:");
				
				if (name == null || name.length() == 0)
				{
					JOptionPane.showMessageDialog(CreateEntryScreen.this, 
						"School name cannot be blank.", "New school", 
						JOptionPane.ERROR_MESSAGE); 
					
					return; 
				}
				
				School school = new School (name); 
				tournament.addSchool(school); 

				resetSchoolBox();
			}
		}
	}


	//--------------------------------------------------------------------------
	// Displays a help message explaining how labs work
	//--------------------------------------------------------------------------
	private class LabsInfoListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			JOptionPane.showMessageDialog (CreateEntryScreen.this, "Enter an "
				+ "integer to represent which lab group this student is in. 1 "
				+ "should be the \"best\" lab, 2 the second best, and so on, with "
				+ "0 meaning that they are not assigned to a lab.", 
				"Help - Lab group", JOptionPane.PLAIN_MESSAGE); 
		}
	}



	//--------------------------------------------------------------------------
	// Creates a new debater or saves changes to an existing one. 
	//--------------------------------------------------------------------------
	private class SaveListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			int kidsPerTeam = tournament.getEvent().getKidsPerTeam();

			//this will be used if it's a new entry
			Competitor[] competitors = new Competitor[kidsPerTeam];

			//...and these are used if it's editing an existing one
			String[] firsts = new String[kidsPerTeam]; 
			String[] lasts = new String[kidsPerTeam]; 
			School[] schls = new School[kidsPerTeam];
			int[] labgrps = new int[kidsPerTeam]; 
		
			for (int i = 0; i < kidsPerTeam; i++)
			{
				String firstName, lastName; 
			
				try
				{
					firstName = firstNames[i].getText();
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(CreateEntryScreen.this, "First "
						+ "name is improperly formatted.", "Create new debater",
						JOptionPane.ERROR_MESSAGE); 
					
					return;
				}

				try
				{
					lastName = lastNames[i].getText();
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(CreateEntryScreen.this, "Last "
						+ "name is improperly formatted.", "Create new debater",
						JOptionPane.ERROR_MESSAGE); 
					
					return;
				}
				
				Object obj = schoolBoxes[i].getSelectedItem(); 
				
				if (obj instanceof String)
				{
					JOptionPane.showMessageDialog(CreateEntryScreen.this, 
						"Please select a school.", "Create new debater",
						JOptionPane.ERROR_MESSAGE);
					
					return; 
				}
				
				School school = (School) obj; 
				
				int lab; 
				
				try
				{
					lab = Integer.parseInt(labs[i].getText());
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(CreateEntryScreen.this, 
						"Lab group must be an integer.", "Create new debater",
						JOptionPane.ERROR_MESSAGE); 
					
					return;
				}
				
				if (entry == null)
					competitors[i] = new Competitor (firstName, lastName, school, lab);
				else 
				{
					firsts[i] = firstName;
					lasts[i] = lastName;
					schls[i] = school;
					labgrps[i] = lab;
				}
			}

			//This is a separate loop because we don't want to update the first 
			//kid if there turns out to be an error with the second one. 
			if (entry != null)
			{
				for (int i = 0; i < kidsPerTeam; i++)
				{
					entry.getStudents()[i].setName(firsts[i], lasts[i]);
					entry.getStudents()[i].setSchool (schls[i]); 
					entry.getStudents()[i].setLab (labgrps[i]); 
				}
			}
			else
			{
				if (kidsPerTeam == 1)
					entry = new Entry (tournament.getEvent(), tournament, 
												competitors[0]);
				else
					entry = new Entry (tournament.getEvent(), tournament, 
												competitors[0], competitors[1]);
				
				tournament.addEntry (entry); 
			}
		
			ep.restoreMainPanel(); 
		}
	}

	//--------------------------------------------------------------------------
	// Returns to previous screen 
	//--------------------------------------------------------------------------
	private class CancelListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			ep.restoreMainPanel(); 
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
			
			ep.restoreMainPanel(); 
		}
	}

	
}














