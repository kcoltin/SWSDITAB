//*****************************************************************************
// SchoolStrikesScreen.java
// Kevin Coltin 
// 
// Screen that allows the user to select which schools a judge is struck 
// against.   
//*****************************************************************************

 
 

import javax.swing.*; 
import java.awt.*; 
import java.awt.event.*; 
import java.util.ArrayList; 

class SchoolStrikesScreen extends JFrame 
{
	private static final Dimension DEFAULT_SIZE = new Dimension (500, 500); 

	private CreateJudgeScreen cjs; 
	private Tournament tournament; 
	private Judge judge; 
	
	private JScrollPane scrollPane; 
	private JPanel mainPanel, topPanel; 
	
	private JLabel nameLabel; 
	private JButton clear, done; 
	private ArrayList<SchoolBox> schoolBoxes; 
	
	
	//--------------------------------------------------------------------------
	// Constructor 
	//--------------------------------------------------------------------------
	SchoolStrikesScreen (Judge j, Tournament t, CreateJudgeScreen screen)
	{
		super ("School Strikes"); 
		
		judge = j; 
		tournament = t; 
		cjs = screen; 
		
		if (judge == null)
			nameLabel = new JLabel("New judge - strikes"); 
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
		
		schoolBoxes = new ArrayList<SchoolBox>(); 
		
		for (School school : tournament.getSchools()) 
		{
			SchoolBox sb = new SchoolBox (school); 
			schoolBoxes.add(sb); 
			mainPanel.add(sb); 
		}
		
		scrollPane = new JScrollPane(mainPanel); 
		getContentPane().add(scrollPane); 
		
		setSize (DEFAULT_SIZE); 
		setVisible(false); //will only become visible when activated from CJS
		setDefaultCloseOperation (JFrame.HIDE_ON_CLOSE); 
	}
	
	//--------------------------------------------------------------------------
	// Returns all school selected as struck 
	//--------------------------------------------------------------------------
	ArrayList<School> getStrikes ()
	{
		ArrayList<School> strikes = new ArrayList<School>(); 
		
		for (SchoolBox sb : schoolBoxes)
			if (sb.isSelected())
				strikes.add(sb.getSchool());
		
		return strikes; 
	}

	
	//--------------------------------------------------------------------------
	// Box for a given school that can be selected 
	//--------------------------------------------------------------------------
	private class SchoolBox extends JCheckBox 
	{
		private School school; 
	
		SchoolBox (School s)
		{
			super (s.getName(), judge != null && judge.isStruck(s)); 
			school = s; 
		}
		
		School getSchool ()
		{
			return school;
		}
	}	

	//--------------------------------------------------------------------------
	// Clears all strikes 
	//--------------------------------------------------------------------------
	private class ClearListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			for (SchoolBox sb : schoolBoxes)
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
			cjs.resetSchoolStrikesLabel(); 
			SchoolStrikesScreen.this.setVisible(false); 
		}
	}
			
	

}













