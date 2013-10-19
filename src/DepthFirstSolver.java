//******************************************************************************
// DepthFirstSolver.java
// 
// Defined by http://www.brpreiss.com/books/opus5/html/page443.html
//******************************************************************************

 
 

public class DepthFirstSolver extends AbstractSolver 
{
	protected void search (Solution solution) 
	{
		if (solution.isComplete())
		{
			updateBest(solution); 
		}
		else
		{
			Enumeration i = solution.getSuccessors();
			while (i.hasMoreElements())
			{
				Solution successor = (Solution) i.nextElement(); 
				search (successor); 
			}
		}
	}	
}















