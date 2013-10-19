//******************************************************************************
// ManualEdit.java
// Kevin Coltin   
//
// This is an editable file designed for the programmer (me) to use to manually
// edit tournament (.tab) files that have been somehow damaged or that need to
// be edited in a way that cannot be done via the SWSDITAB GUI. 
//******************************************************************************

 
 

import java.io.*; 
import java.util.ArrayList; 

public class ManualEdit 
{
	public static void main (String[] args)
	{
		//------------------------------------------------------------------------
		//Open the file to read in the tournament - this part of the program should
		//be kept the same no matter what you're trying to do with the tournament. 
		
		final String filename = "SWSDI LD.tab"; 
		Tournament tournament = read (filename); 
		//------------------------------------------------------------------------
		

		for (Room room : tournament.getRooms())
			if (room.getName().equals("JUN 201"))
			{
				tournament.getRooms().remove(room);
				break;
			}
		
		
		//------------------------------------------------------------------------
		//Save the newly changed tournament file - this part of the program should
		//be kept the same no matter what you're trying to do with the tournament. 
		
		write (tournament, filename); 
		//------------------------------------------------------------------------
	}


	//--------------------------------------------------------------------------
	// Reads a selected file to obtain the Tournament object.
	//--------------------------------------------------------------------------
	private static Tournament read (String filename) 
	{
		Tournament tournament = null; 
		ObjectInputStream ois = null; 
		try
		{
			File inFile = new File (filename); 
			FileInputStream fis = new FileInputStream (inFile);
			ois = new ObjectInputStream (fis);
			tournament = (Tournament) ois.readObject();
			ois.close();
		}
		catch (Exception e)
		{
			e.printStackTrace(); 
			try{ois.close();}catch(Exception ex){ex.printStackTrace();} 
			System.exit(1);
		}
		
		return tournament;
	}



	//--------------------------------------------------------------------------
	// Saves the tournament to the selected file
	//--------------------------------------------------------------------------
	private static void write (Tournament tournament, String filename)
	{
		File file = new File (filename); 
		
		try
		{
			tournament.write(file); 
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}


}













