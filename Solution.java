//******************************************************************************
// Solution.java
//  
// Defined by http://www.brpreiss.com/books/opus5/html/page440.html
//******************************************************************************




public interface Solution 
{
	boolean isFeasible ();
	boolean isComplete ();
	double getObjective ();
	int getBound ();
	Enumeration getSuccessors (); 
}


