//*****************************************************************************
// TopMenu.java
// Kevin Coltin  
//
// This is the menu bar that sits at the top of all screens in the tab program.
//*****************************************************************************

 
 

import javax.swing.*; 
import java.awt.event.*; 
import java.io.*; 

class TopMenu extends JMenuBar
{
	private JMenu fileMenu, helpMenu; 
	private JMenuItem newButton, openButton, saveButton, saveAsButton, 
				backupButton, backupRecoverButton, quitButton;
	
	private TournamentFrame tf; 
	private Tournament tournament; 
	private File file; //file where the tournament is saved 
	private File backupFile; //file where the tournament is backed up as text 
	
	//--------------------------------------------------------------------------
	// Constructor 
	//--------------------------------------------------------------------------
	TopMenu (TournamentFrame frame)
	{
		tf = frame; 
		tournament = tf.getTournament();
		file = tf.getFile(); 
		backupFile = tf.getBackupFile(); 
	
		//create File menu 
		fileMenu = new JMenu("File"); 
		fileMenu.setMnemonic(KeyEvent.VK_F); 
		
		newButton = new JMenuItem("New Tournament...");
		newButton.addActionListener (new NewListener());
		
		openButton = new JMenuItem("Open..."); 
		openButton.addActionListener (new OpenListener()); 
		
		saveButton = new JMenuItem("Save"); 
		saveButton.addActionListener (new SaveListener()); 

		saveAsButton = new JMenuItem("Save as..."); 
		saveAsButton.addActionListener (new SaveAsListener()); 
		
		backupButton = new JMenuItem("Backup Tournament..."); 
		backupButton.addActionListener(new BackupListener()); 
		
		backupRecoverButton = new JMenuItem("Recover from backed up file..."); 
		backupRecoverButton.addActionListener (new BackupRecoverListener()); 
		
		quitButton = new JMenuItem("Quit SWSDITAB"); 
//		quitButton.addActionListener (new QuitListener()); 
		
		fileMenu.add(newButton); 
		fileMenu.add(openButton); 
		fileMenu.addSeparator(); 
		fileMenu.add(saveButton); 
		fileMenu.add(saveAsButton); 
		fileMenu.add(backupButton); 
		fileMenu.add(backupRecoverButton); 
		fileMenu.addSeparator();
		fileMenu.add(quitButton); 
		
		//Create help menu 
		helpMenu = new JMenu("Help"); 
		helpMenu.setMnemonic (KeyEvent.VK_H); 
		
		JMenuItem helpMenuPlaceholder = 
									new JMenuItem("<html><em>In progress</em></html>");
		helpMenu.add(helpMenuPlaceholder); 
		
		add (fileMenu); 
		add (helpMenu); 
	}




	//--------------------------------------------------------------------------
	// Reads a selected file to obtain the Tournament object.
	//--------------------------------------------------------------------------
	private static Tournament read (File inFile) 
		throws IOException, ClassNotFoundException
	{
		FileInputStream fis = new FileInputStream (inFile);
		ObjectInputStream ois = new ObjectInputStream (fis);
		Tournament tournament = (Tournament) ois.readObject();
		ois.close();
		return tournament;
	}



		


	//--------------------------------------------------------------------------
	// Initiates a "save as" dialog prompting the user where to save the 
	// tournament. 
	//--------------------------------------------------------------------------
	private void saveAs ()
	{
		//Create a file chooser window, with the directory initalized to the 
		//current directory (i.e. the directory in which the "tab" folder is
		//held.)
		JFileChooser chooser;
		try
		{
			chooser = new JFileChooser (new File(".").getCanonicalPath());
		}
		catch (IOException e)
		{
			//If something goes wrong, initialize it with the default directory
			chooser = new JFileChooser (); 
		}

		chooser.addChoosableFileFilter (new TabFilter());

		File outFile = null; 
		int status = chooser.showSaveDialog (null);

		//Perform actions based on whether a file is opened, the users selects
		//"cancel," or an error occurs.
		if (status == JFileChooser.APPROVE_OPTION)
			outFile = chooser.getSelectedFile();
		if (status == JFileChooser.CANCEL_OPTION)
			return;
		if (status == JFileChooser.ERROR_OPTION)
		{
			JOptionPane.showMessageDialog(tf, "An error occurred when trying "
							+ "to save the file.", "Unknown error", 
							JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//Display error if file isn't a .tab file 
		if (outFile.getName().endsWith(".tab") == false)
		{
			JOptionPane.showMessageDialog(tf, "Error: Tournament files must be "
				+ "saved as \".tab\". Please\ntry again.", "Save Tournament", 
				JOptionPane.ERROR_MESSAGE); 
			saveAs();
		}


		//If file selection was successful, try to save the tournament

		try
		{
			tournament.write(outFile);
			//set the "file" object in the whole program to the new file location
			file = outFile; 
			tf.setFile(file); 
			assert tf.getFile().equals(file);
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(tf, "An error occurred when "
								+ "trying to save the file. Try again or try "
								+ "using \"Save as\" to save it in a different "
								+ "location.", "File error", 
								JOptionPane.ERROR_MESSAGE);
			e.printStackTrace(); 
		}
	}




	//--------------------------------------------------------------------------
	// Initiates a "save as" dialog prompting the user where to save the 
	// backup file for the tournament. 
	//--------------------------------------------------------------------------
	private void saveBackupAs ()
	{
		//Create a file chooser window, with the directory initalized to the 
		//current directory (i.e. the directory in which the "tab" folder is
		//held.)
		JFileChooser chooser;
		try
		{
			chooser = new JFileChooser (new File(".").getCanonicalPath());
		}
		catch (IOException e)
		{
			//If something goes wrong, initialize it with the default directory
			chooser = new JFileChooser (); 
		}

		chooser.addChoosableFileFilter (new TxtFilter());

		File outFile = null; 
		int status = chooser.showSaveDialog (null);

		//Perform actions based on whether a file is opened, the user selects
		//"cancel," or an error occurs.
		if (status == JFileChooser.APPROVE_OPTION)
			outFile = chooser.getSelectedFile();
		if (status == JFileChooser.CANCEL_OPTION)
			return;
		if (status == JFileChooser.ERROR_OPTION)
		{
			JOptionPane.showMessageDialog(tf, "An error occurred when trying "
							+ "to save the file.", "Unknown error", 
							JOptionPane.ERROR_MESSAGE);
			return;
		}

		//Display error if file isn't a .txt file 
		if (outFile.getName().endsWith(".txt") == false)
		{
			JOptionPane.showMessageDialog(tf, "Error: Tournament backup files "
				+ "must be saved as \".txt\". Please\ntry again.", 
				"Backup Tournament",	JOptionPane.ERROR_MESSAGE); 
			saveBackupAs();
		}



		//If file selection was successful, try to save the text file 

		try
		{
			tournament.backup (outFile);
			//set the "file" object in the whole program to the new file location
			file = outFile; 
			tf.setBackupFile(file); 
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(tf, "An error occurred when "
								+ "trying to save the file. Try again or try "
								+ "using \"Save as\" to save it in a different "
								+ "location.", "Backup File", 
								JOptionPane.ERROR_MESSAGE);
			e.printStackTrace(); 
		}
	}




	//--------------------------------------------------------------------------
	// Listener for "New Tournament...", to create a new file
	//--------------------------------------------------------------------------
	private class NewListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			//Prompt the user to select the event 
			Object choice = JOptionPane.showInputDialog (tf, "Select event for "
								+ "this tournament:", "New Tournament", 
								JOptionPane.QUESTION_MESSAGE, null, Event.values(), 
								Event.LD); 
			
			if (choice == null)
				return; 
			
			Event ev = (Event) choice; 

		
			//Prompt the user to name the tournament 
			String name = ""; 
			
			do
			{	
				name = JOptionPane.showInputDialog (tf, "Enter name for " 
								+ "this tournament:",	"New Tournament", 
								JOptionPane.QUESTION_MESSAGE); 
				
				if (name == null)
					return;
				
				if (name.trim().length() == 0)
					JOptionPane.showMessageDialog (tf, "Name may not be blank. "
								+ "Enter another name.", "New Tournament", 
								JOptionPane.WARNING_MESSAGE);
			}
			while (name.trim().length() == 0);
			
			//Create new tournament 
			Tournament newTournament = new Tournament (ev, name); 
			
			if (tournament == null)
				tf.reset(newTournament, null, null); 
			else
				new TournamentFrame (newTournament, null, null); 
		}
	}



	//--------------------------------------------------------------------------
	// Listener for "Open...", to open a new file
	//--------------------------------------------------------------------------
	private class OpenListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			File inFile = null; 

			//Create a file chooser window, with the directory initalized to the 
			//current directory (i.e. the directory in which the "tab" folder is
			//held.)
			JFileChooser chooser;
			try
			{
				chooser = new JFileChooser (new File(".").getCanonicalPath());
			}
			catch (IOException e)
			{
				//If something goes wrong, initialize it with the default directory
				chooser = new JFileChooser (); 
			}

			chooser.addChoosableFileFilter (new TabFilter());

			int status = chooser.showOpenDialog (null);

			//Perform actions based on whether a file is opened, the users selects
			//"cancel," or an error occurs.
			if (status == JFileChooser.APPROVE_OPTION)
				inFile = chooser.getSelectedFile();
			if (status == JFileChooser.CANCEL_OPTION)
				return;
			if (status == JFileChooser.ERROR_OPTION)
			{
				JOptionPane.showMessageDialog(tf, "An error occurred when trying "
								+ "to open the file.", "Unknown error", 
								JOptionPane.ERROR_MESSAGE);
				return;
			}


			//If file selection was successful, try to open the tournament
			try
			{
				Tournament newTournament = TopMenu.read(inFile); 
				
				if (tournament == null)
					tf.reset(newTournament, inFile, null); 
				else
					new TournamentFrame(newTournament, inFile, null);
			}
			catch (IOException e) //This will occur if the selected file doesn't
											//actually contain a Tournament object
			{
				JOptionPane.showMessageDialog(tf, "Error: Not a valid Tournament "
								+ "file.", "File error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace(); 
			}
			catch (ClassNotFoundException e)
			{
				JOptionPane.showMessageDialog(tf, "Error accessing file " 
								+ "\"Tournament.class\".", "File error", 
								JOptionPane.ERROR_MESSAGE);
			}
			catch (Exception e) //catch-all
			{
				JOptionPane.showMessageDialog(tf, "An error occurred when trying "
								+ "to open the file.", "Unknown error", 
								JOptionPane.ERROR_MESSAGE);
				e.printStackTrace(); 
			}
		}

	}




	//--------------------------------------------------------------------------
	// Listener called when a file is saved. Saves the Tournament object as a 
	// .tab file. 
	//--------------------------------------------------------------------------
	private class SaveListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			if (tournament == null)
			{
				JOptionPane.showMessageDialog(tf, "Cannot save - no tournament is "
							+ "currently open.", "Cannot save", 
							JOptionPane.ERROR_MESSAGE);
				return;
			}
		
			if (file == null)
				saveAs ();
			
			else 
			{
				try
				{
					tournament.write(file);
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(tf, "An error occurred when "
										+ "trying to save the file. Try again or try "
										+ "using \"Save as\" to save it in a different "
										+ "location.", "File error", 
										JOptionPane.ERROR_MESSAGE);
					e.printStackTrace(); 
				}
			}
		}
	}
		


	//--------------------------------------------------------------------------
	// Listener called when the Save As button is clicked. Saves the Tournament 
	// object as a .tab file with the name and location selected.  
	//--------------------------------------------------------------------------
	private class SaveAsListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			if (tournament == null)
			{
				JOptionPane.showMessageDialog(tf, "Cannot save - no tournament is "
							+ "currently open.", "Cannot save", 
							JOptionPane.ERROR_MESSAGE);
				return;
			}
			else
				saveAs();
		}
	}



	//--------------------------------------------------------------------------
	// Backs up the file and saves it as text that can be recovered
	//--------------------------------------------------------------------------
	private class BackupListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			saveBackupAs(); 
		}
	}




	//--------------------------------------------------------------------------
	// Backs up the file and saves it as text that can be recovered
	//--------------------------------------------------------------------------
	private class BackupRecoverListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			File inFile = null; 

			//Create a file chooser window, with the directory initalized to the 
			//current directory (i.e. the directory in which the "tab" folder is
			//held.)
			JFileChooser chooser;
			try
			{
				chooser = new JFileChooser (new File(".").getCanonicalPath());
			}
			catch (IOException e)
			{
				//If something goes wrong, initialize it with the default directory
				chooser = new JFileChooser (); 
			}

			chooser.addChoosableFileFilter (new TxtFilter());

			int status = chooser.showOpenDialog (null);

			//Perform actions based on whether a file is opened, the users selects
			//"cancel," or an error occurs.
			if (status == JFileChooser.APPROVE_OPTION)
				inFile = chooser.getSelectedFile();
			if (status == JFileChooser.CANCEL_OPTION)
				return;
			if (status == JFileChooser.ERROR_OPTION)
			{
				JOptionPane.showMessageDialog(tf, "An error occurred when trying "
								+ "to open the file.", "Unknown error", 
								JOptionPane.ERROR_MESSAGE);
				return;
			}


			//If file selection was successful, try to open the file and recover 
			//the tournament 
			try
			{
				Tournament newTournament = Tournament.recover(inFile, tf); 
				
				//Quit if it didn't recover sucecssfully 
				if (newTournament == null)
					return;
				
				if (tournament == null)
					tf.reset(newTournament, null, inFile); 
				else
					new TournamentFrame(newTournament, null, inFile);
			}
			catch (Exception e) //catch-all
			{
				JOptionPane.showMessageDialog(tf, "An error occurred when trying "
								+ "to open the file.", "Unknown error", 
								JOptionPane.ERROR_MESSAGE);
				e.printStackTrace(); 
			}
		}
	}





	//--------------------------------------------------------------------------
	// File Filter used to filter the file chooser dialog box so that only .tab
	// files appear.
	//--------------------------------------------------------------------------
	private class TabFilter extends javax.swing.filechooser.FileFilter
	{
		public boolean accept(File file)
		{
			//get the last four characters of the file name
			String fileName = file.getName();

			if (fileName.length() < 4)
				return false;

			String extension = fileName.substring(fileName.length()-4); 

			if (extension.equals(".tab"))
				return true;
			else
				return false;
		}

		public String getDescription ()
		{
			return ".tab";
		}
	}


	//--------------------------------------------------------------------------
	// File Filter used to filter the file chooser dialog box so that only .txt
	// files appear.
	//--------------------------------------------------------------------------
	private class TxtFilter extends javax.swing.filechooser.FileFilter
	{
		public boolean accept(File file)
		{
			//get the last four characters of the file name
			String fileName = file.getName();

			if (fileName.length() < 4)
				return false;

			String extension = fileName.substring(fileName.length()-4); 

			if (extension.equals(".txt"))
				return true;
			else
				return false;
		}

		public String getDescription ()
		{
			return ".txt";
		}
	}


}












