//******************************************************************************
// Print.java
// Kevin Coltin 
// 
// Converts the postings from a round into a Latex (.tex) document.   
//******************************************************************************

 
 

import java.io.*; 
import javax.swing.JOptionPane;

class Print 
{
	//---------------------------------------------------------------------------
	// Primary method of the class - converts postings to latex doc. 
	//---------------------------------------------------------------------------
	static void printPostings (Round round)
	{
		String latexCode = getLatexCode (round); 
		saveTexFile (round, latexCode); 
	}
		
		
		
	//---------------------------------------------------------------------------
	// Creates the source code for a Latex doc of the postings. 
	//---------------------------------------------------------------------------
	private static String getLatexCode (Round round)
	{
		//Make latex header stuff 
		String code = makeLatexHeader();
		
		//Add header for the name of the tournament and the round 
		code += "\\begin{center}\n\\textbf{\\huge{" 
				+ round.getTournament().getName() + " " 
				+ round.getTournament().getEvent() + "}}\n\n" + "\\textbf{\\huge{"
				+ round.getName() + "}}\n\\end{center}\n\n"; 
		
		//Make a table containing the headers and all of the debates' info 
		code += makeDebatesTable(round); 
		
		//add comments at bottom 
		if (round.getCommentsOnPostings().length() > 0)
			code += "\n\n" + "\\let\\thefootnote\\relax\\footnotetext{"
					+ round.getCommentsOnPostings() + "}\n\n";
		
		code += "\\end{document}";
		
		return code; 
	}
	
	
	//---------------------------------------------------------------------------
	// Makes header stuff for tex 
	//---------------------------------------------------------------------------
	private static String makeLatexHeader ()
	{
		return "\\documentclass{article}\n\\setlength{\\hoffset}{0in}\n" 
				+ "\\setlength{\\voffset}{0in}\n\\setlength{\\oddsidemargin}{0in}\n"
				+ "\\setlength{\\evensidemargin}{0in}\n" 
				+ "\\setlength{\\topmargin}{0in}\n\\setlength{\\headheight}{0in}\n"
				+ "\\setlength{\\headsep}{0in}\n"
				+ "\\setlength{\\marginparsep}{0in}\n"
				+ "\\setlength{\\marginparwidth}{0in}\n"
				+ "\\setlength{\\marginparpush}{0in}\n"
				+ "\\pagestyle{empty}\n\\begin{document}\n\n"; 
	}
		
	
	//---------------------------------------------------------------------------
	// Makes all the info for each debate in the round 
	//---------------------------------------------------------------------------
	private static String makeDebatesTable (Round round)
	{
		String code = "\\begin{tabular}{llll}\n";
	
		//If it's flighted, add label for Flight A. Otherwise, just say "start 
		//time."
		code += round.isFlighted() ? "\\large{FLIGHT A: }" 
													: "\\large{START TIME: }"; 
		//Add start time 
		code += "&\\large{" + (round.isASAP() ? "ASAP" : round.getTime()) 
				+ "}\\\\\n\\\\\n"; 
		
		//Add header row with room, sides (or flip), and judge 
		code += "\\large{ROOM} & \\large{" + (round instanceof ElimRound 
													? "FLIP FOR SIDES} & &" 
													: "AFFIRMATIVE} & \\large{NEGATIVE} &");
		code += "\\large{JUDGE } \\\\\n\\\\\n";  
		
		//Add a row for each individual debate 
		for (Debate debate : round.getFullyPairedTrueDebates('A'))
			code += addDebateRow (debate); 
		
		//If it's flighted, add flight B 
		if (round.isFlighted())
		{
			//First, add flight header 
			code += "\n&\\\\\n&\\\\\n\\large{FLIGHT B:} & \\large{" 
						+ (round.isASAP() ? "ASAP" : round.getTimeFlightB()) 
						+ "}\\\\\n&\\\\\n"; 
			
			//Then add row for each debate 
			for (Debate debate: round.getFullyPairedTrueDebates('B'))
				code += addDebateRow (debate); 
		}
		
		
		//for each one-team bye, add to bottom 
		code += "&\\\\\n";
		for (Debate bye : round.getByes())
			code += addByeRow (bye.getTeam1());
		
		code += "\n\n\\end{tabular}\n\n";
		return code; 
	}
		
		
		
	//---------------------------------------------------------------------------	
	// Adds a row for a single debate onto the pairings. It is designed to fit in
	// the tabular environment from makeDebatesTable(). 
	// 
	// Note: this method will only be called on debates that are fully paired 
	// "true" debates. 
	//---------------------------------------------------------------------------	
	private static String addDebateRow (Debate debate)
	{
		String code = debate.getRoom().getName() + "&" 
						+ debate.getTeam1().toString() 
						+ (debate.isSpecialSidelocked() ? " (AFF)" : "") + "&"
						+ debate.getTeam2().toString() + "&" 
						+ debate.getJudges().get(0).getName() + "\\\\\n"; 
		
		//If it's a paneled round, add names of additional judges 
		if (debate.getJudges().size() > 1)
		{
			for (int i = 1; i < debate.getJudges().size(); i++)
				code += "&&&" + debate.getJudges().get(i).getName() + "\\\\\n";
			
			code += "&\\\\\n";
		}
		
		return code; 
	}
	


	//---------------------------------------------------------------------------	
	// Adds a row for a bye onto the pairings, designed to fit in the tabular 
	// environment from makeDebatesTable(). 
	//---------------------------------------------------------------------------	
	private static String addByeRow (Entry entry)
	{
		return "&" + entry.toString() + "& Bye \\\\\n";
	}
	




	//---------------------------------------------------------------------------
	// Saves the document in folder "postings" as round.getName() + ".tex" 
	// The folder Postings is in a folder one level above this one - e.g., say
	// the folder "tab" is in "Folder", then "Postings" will also be in "Folder". 
	//---------------------------------------------------------------------------
	private static void saveTexFile (Round round, String latexCode) 
	{	
		String roundName = round.getName(); 
		File file; 
		String filename; 
		PrintWriter outFile = null; 
		
		//Need to use different slashes depending if it's Windows or not (if 
		//not, it's assumed it's Mac/Unix) 
		if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0)
			filename = "Postings\\" + round.getTournament().getName() + " " 
						+ round.getTournament().getEvent().toString() + " " 
						+ roundName + ".tex";
		else
			filename = "Postings/" + round.getTournament().getName() + " " 
						+ round.getTournament().getEvent().toString() + " " 
						+ roundName + ".tex";
		
		file = new File (filename); 
		
		try
		{
			FileWriter fw = new FileWriter (file);
			BufferedWriter bw = new BufferedWriter (fw);
			outFile = new PrintWriter (bw);
			
			outFile.print (latexCode);
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog (null, "Error writing to file  (" 
									+ filename + ").", "Print Postings", 
									JOptionPane.ERROR_MESSAGE); 
			return;
		}
		finally
		{
			if (outFile != null)
				outFile.close();
		}
	}
	
}


















