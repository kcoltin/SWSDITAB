//*****************************************************************************
// JudgeInhabitable.java
// Kevin Coltin  
//
// Interface for objects that "have" a judge - debates, judge assignments, and 
// judgeroomassignments.
//*****************************************************************************

 
 

import java.util.ArrayList;
import java.awt.Color; 

interface JudgeInhabitable 
{
	void setJudge (Judge judge);
	void addJudge (Judge judge);
	void removeJudge (Judge judge);
	void removeJudges (); 

	Judge getJudge ();
	ArrayList<Judge> getJudges();

	boolean equals (Object other); 
	boolean hasJudge (Judge judge); 

	void setJudgeLocked (boolean locked);
	void setJudgeLocked (Judge judge, boolean locked); 
	boolean isJudgeLocked();
	boolean isJudgeLocked (Judge judge);
	
	Color getJudgeColor(); 
	Color getJudgeColor (Judge judge);
}


