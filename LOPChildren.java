//******************************************************************************
// LOPChildren.java
// Kevin Coltin  
//
// 
//******************************************************************************

 
 

import java.util.NoSuchElementException; 

public class LOPChildren implements Enumeration
{
	//Index of the "next" element. There are always two elements, the two 
	//branches PA and PB of the problem P. 
	private int next; 
	
	//An array of LOPs - the two child tableaux
	private LOP[] children; 
	
	//LOP that is the container/parent of these two 
	private LOP parent; 

	//---------------------------------------------------------------------------
	// Constructor 
	//---------------------------------------------------------------------------
	public LOPChildren (LOP container)
	{
		parent = container; 
		next = 0; 
		children = parent.getChildren(); 
	}

	
	//---------------------------------------------------------------------------
	// Indicates if there is another Problem to go through. This is equivalent to
	// "next" being either 0 or 1	(because there are only two sub problems, and 
	// they're indexed as Java arrays) and the current LOP being feasible and
	// nonintegral, i.e. complete. 
	//---------------------------------------------------------------------------
	public boolean hasMoreElements ()
	{
		return next <= 1 && parent.isComplete() == false;
	}


	//---------------------------------------------------------------------------
	// Returns the next tableau. 
	//---------------------------------------------------------------------------
	public LOP nextElement() throws NoSuchElementException
	{
		if (hasMoreElements() == false) //this is true if next > 1
			throw new NoSuchElementException(); 

		next++; //have to increment next before return statement 
		
		return children[next-1]; 
	}
	
	
}






























