//*****************************************************************************
// StudentStrikesScreen.java
// Kevin Coltin 
// 
// Screen that allows the user to select which students a judge is struck 
// against.   
//*****************************************************************************

 
 

import javax.swing.*; 
import java.awt.*; 
import java.awt.event.*; 
import java.util.ArrayList; 

class StudentStrikesScreen extends JFrame 
{
	private static final Dimension DEFAULT_SIZE = new Dimension (500, 500); 

	private CreateJudgeScreen cjs; 
	private Tournament tournament; 
	private Judge judge; 
	
	private JScrollPane scrollPane; 
	private JPanel mainPanel, topPanel; 
	
	private JLabel nameLabel; 
	private JButton clear, done; 
	private ArrayList<StudentBox> studentBoxes; 
	
	
	//--------------------------------------------------------------------------
	// Constructor 
	//--------------------------------------------------------------------------
	StudentStrikesScreen (Judge j, Tournament t, CreateJudgeScreen screen)
	{
		super ("Student Strikes"); 
		
		judge = j; 
		tournament = t; 
		cjs = screen; 
		
		if (judge == null)
			nameLabel = new JLabel ("New judge - Strikes"); 
		else
			nameLabel = new JLabel (judge.getName() + " - Strikes"); 

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
		setVisible(false); //will only become visible when activated from CJS
		setDefaultCloseOperation (JFrame.HIDE_ON_CLOSE); 
	}
	
	//--------------------------------------------------------------------------
	// Returns all students selected as struck 
	//--------------------------------------------------------------------------
	ArrayList<Competitor> getStrikes ()
	{
		ArrayList<Competitor> strikes = new ArrayList<Competitor>(); 
		
		for (StudentBox sb : studentBoxes)
			if (sb.isSelected())
				strikes.add(sb.getStudent());
		
		return strikes; 
	}

	
	//--------------------------------------------------------------------------
	// Box for a given student that can be selected 
	//--------------------------------------------------------------------------
	private class StudentBox extends JCheckBox 
	{
		private Competitor student; 
	
		StudentBox (Competitor s)
		{
			super (s.getName(), judge != null && judge.isStruck(s)); 
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
			cjs.resetStudentStrikesLabel(); 
			StudentStrikesScreen.this.setVisible(false); 
		}
	}
			
	

}













