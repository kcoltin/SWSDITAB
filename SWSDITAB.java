//*****************************************************************************
// SWSDITAB.java 
// Kevin Coltin 
// 
// Main driver program of the tab program. Initializes a screen, then lets
// the user open a new tournament or do whatever else.
//*****************************************************************************




import javax.swing.*;
import java.awt.*;
import java.util.ArrayList; 

public class SWSDITAB
{
	//each frame contains one tournament
	private static ArrayList<TournamentFrame> frames; 
	
	public static void main (String[] args)
	{
		System.out.println("Loading SWSDITAB...");

		frames = new ArrayList<TournamentFrame>();
		
		//Open new frame for a tournament
		TournamentFrame tf = new TournamentFrame();
		frames.add(tf);

		System.out.println("Loading complete.");
	}
	
}

