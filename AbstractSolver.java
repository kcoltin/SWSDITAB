//******************************************************************************
// AbstractSolver.java
//  
// Defined by http://www.brpreiss.com/books/opus5/html/page442.html  Note, 
// however, that this is for a problem where the goal is to *maximize* the 
// objective. -Max_VALUE and < would have to be switched to MAX_VALUE and > for
// a solver intended to minimize an objective function. 
//******************************************************************************

 
 

public abstract class AbstractSolver implements Solver 
{
	protected Solution bestSolution; 
	protected double bestObjective; 
	
	protected abstract void search (Solution initial); 
	
	public Solution solve (Solution initial)
	{
		bestSolution = null; 
		bestObjective = -Double.MAX_VALUE; 
		search (initial); 
		return bestSolution; 
	}
	
	//---------------------------------------------------------------------------
	// Called by search method - updates the best solution which maximizes the
	// objective function. 
	//---------------------------------------------------------------------------
	public void updateBest (Solution solution)
	{
		if (solution.isComplete() && solution.isFeasible() 
			&& solution.getObjective() > bestObjective)
		{
			bestSolution = solution; 
			bestObjective = solution.getObjective();
		}
	}
}



