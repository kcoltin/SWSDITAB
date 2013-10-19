//*****************************************************************************
// CreateJudgeScreen.java
// Kevin Coltin
// 
// Screen to create, edit, or delete judge  
//*****************************************************************************

 
 

import javax.swing.*; 
import java.awt.*; 
import java.awt.event.*; 

class CreateJudgeScreen extends JPanel 
{
	private JScrollPane scrollPane; 
	private GridBagConstraints gbc; 
	private JPanel mainPanel; //panel contained in scroll pane 
	private JPanel topPanel, bottomPanel; 
	
	private SchoolStrikesScreen ScSS; 
	private StudentStrikesScreen StSS; 

	private JudgesPanel jp; 
	private Tournament tournament; 
	private Judge judge; 
	
	private JTextField nameField; 
	private JLabel schoolStrikesLabel, studentStrikesLabel; 
	private JComboBox priorityBox; 
	private JButton saveButton, cancelButton, deleteButton; 
	
	//--------------------------------------------------------------------------
	// Constructor. If judge is null, it's to create a new judge. 
	//--------------------------------------------------------------------------
	CreateJudgeScreen (JudgesPanel panel, Tournament t, Judge j)
	{
		jp = panel; 
		tournament = t; 
		judge = j; 
		
		ScSS = new SchoolStrikesScreen (judge, tournament, this);
		StSS = new StudentStrikesScreen (judge, tournament, this); 
		
		gbc = new GridBagConstraints(); 
		
		JLabel nameLabel = new JLabel("Last name:");
		nameField = new JTextField(); 
		nameField.setColumns(15);
		
		if (judge != null)
			nameField.setText(judge.getName()); 
		
		JLabel priorityLabel = new JLabel("Default priority:"); 
		priorityBox = new JComboBox (Priority.PriorityLevel.values()); 
		
		if (judge == null)
			priorityBox.setSelectedItem(Priority.PriorityLevel.Normal); 
		else
			priorityBox.setSelectedItem(judge.getDefaultPriority()); 
		
		JButton schoolStrikes = new JButton("School strikes:"); 
		schoolStrikes.addActionListener (new SchoolStrikesListener()); 
		schoolStrikesLabel = new JLabel();
		resetSchoolStrikesLabel(); 
		
		JButton studentStrikes = new JButton ("Student strikes:"); 
		studentStrikes.addActionListener (new StudentStrikesListener()); 
		studentStrikesLabel = new JLabel();
		resetStudentStrikesLabel(); 

		topPanel = new JPanel(); 
		topPanel.setLayout (new GridLayout(4, 2)); 
		
		topPanel.add(nameLabel);
		topPanel.add(nameField);
		topPanel.add(priorityLabel);
		topPanel.add(priorityBox);
		topPanel.add(schoolStrikes);
		topPanel.add(schoolStrikesLabel);
		topPanel.add(studentStrikes);
		topPanel.add(studentStrikesLabel); 
				
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
	// Resets school strikes label with correct text 
	//--------------------------------------------------------------------------
	void resetSchoolStrikesLabel()
	{
		if (ScSS.getStrikes().isEmpty())
		{
			schoolStrikesLabel.setText("none");
			return;
		}
		
		String text = ""; 
			
		for (int i = 0; i < ScSS.getStrikes().size(); i++)
		{
			if (i > 0)
				text += ", ";
				
			text += ScSS.getStrikes().get(i).getName(); 
		}
		
		schoolStrikesLabel.setText(text); 
	}

	//--------------------------------------------------------------------------
	// Resets student strikes label with correct text 
	//--------------------------------------------------------------------------
	void resetStudentStrikesLabel()
	{
		if (StSS.getStrikes().isEmpty())
		{
			studentStrikesLabel.setText("none");
			return;
		}
		
		String text = ""; 
			
		for (int i = 0; i < StSS.getStrikes().size(); i++)
		{
			if (i > 0)
				text += ", ";
				
			text += StSS.getStrikes().get(i).getName(); 
		}
		
		studentStrikesLabel.setText(text); 
	}
		
		

	//--------------------------------------------------------------------------
	// Opens the screens to edit schools and student strikes 
	//--------------------------------------------------------------------------
	private class SchoolStrikesListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			ScSS.setVisible(true); 
		}
	}

	private class StudentStrikesListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			StSS.setVisible(true); 
		}
	}
	

	//--------------------------------------------------------------------------
	// Creates a new judge or saves changes to an existing one. 
	//--------------------------------------------------------------------------
	private class SaveListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			//check the text field input is acceptable 
			if (nameField.getText() == null || nameField.getText().length() == 0)
			{
				JOptionPane.showMessageDialog (CreateJudgeScreen.this, 
						"\"Name\" cannot be blank.", "Edit judge name", 
						JOptionPane.ERROR_MESSAGE);
				return; 
			}
			
			if (judge == null)
			{
				judge = new Judge (nameField.getText(), tournament); 
				tournament.addJudge (judge); 
			}
			else
				judge.setName (nameField.getText()); 
			
			judge.setDefaultPriority ((Priority.PriorityLevel) 
													priorityBox.getSelectedItem()); 
			judge.setSchoolStrikes (ScSS.getStrikes()); 
			judge.setStudentStrikes (StSS.getStrikes()); 
			
			jp.restoreMainPanel();
		}
	}		
			
			

	//--------------------------------------------------------------------------
	// Returns to previous screen 
	//--------------------------------------------------------------------------
	private class CancelListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			jp.restoreMainPanel(); 
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
			
			jp.restoreMainPanel(); 
		}
	}



}














