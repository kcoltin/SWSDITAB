//******************************************************************************
// Enumeration.java
//  
// Defined by http://www.brpreiss.com/books/opus5/html/page124.html
//******************************************************************************

 
 

import java.util.NoSuchElementException; 

public interface Enumeration 
{
	boolean hasMoreElements(); 
	Object nextElement() throws NoSuchElementException; 	
}


