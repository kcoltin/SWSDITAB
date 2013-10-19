//*****************************************************************************
// Sort.java
// Kevin Coltin
//
// Class to implement sorting algorithm on ArrayLists, and also includes a 
// method to easily remove an object from an ArrayList. 
//*****************************************************************************

 
 

import java.util.ArrayList; 

class Sort 
{
	//--------------------------------------------------------------------------
	// Primary method - implements sort
	//--------------------------------------------------------------------------
	static void sort (ArrayList objects)
	{
		int start = 0; 
		int end = objects.size() - 1; 
		mergeSort (objects, start, end);
	}
	
	
	//--------------------------------------------------------------------------
	// Removes an object from an ArrayList. Object should implement the "equals"
	// method. 
	//--------------------------------------------------------------------------
	static void remove (ArrayList objects, Object obj)
	{
		int i = 0; 
		
		while (i < objects.size())
		{
			if (objects.get(i).equals(obj))
				objects.remove(i);
			
			i++;
		}	
	}



	//---------------------------------------------------------------------------
	// Special methods for comparing debaters' seeds rather than alphabetical 
	// order. Unlike sort(), this returns an array list. It only seeds based on
	// results from prelim rounds, not practices (duh) or elims. If "showAll" is
	// true, the returned array will contain all entries in the tournament; if it
	// is false (default), then it will only show those who are actually 
	// breaking.
	//---------------------------------------------------------------------------
	static ArrayList<Entry> seed (Tournament tournament)
	{
		//Copy objects into new array list to be returned (to avoid changing 
		//entries in tournament.getEntries(), if that's what is passed to the 
		//argument. 
		ArrayList<Entry> seeds = new ArrayList<Entry>(); 
		
		for (Entry entry : tournament.getEntries())
			if (!(entry.isIneligibleToBreak())) //ignore those who can't break
				seeds.add(entry);
	
		int start = 0; 
		int end = seeds.size()-1; 
		mergeSortSeeds (seeds, start, end);
		return seeds; 
	}

	

	//---------------------------------------------------------------------------
	// Returns the seeds of entries in a particular elim round. This takes as 
	// given the entries who have broken at the tournament (the "breaks" array
	// list in Tournament) for the first outround, then recursively computes the
	// entries who are left standing in each subsequent outround. The returned
	// arraylist will contain precisely those entries who could potentially 
	// compete in the given outround. If the tournament hasn't broken yet, it
	// includes all entries, sorted in order based on their seeds from all 
	// prelims entered so far. If the tournament has already broken, it contains 
	// the entries who haven't yet been eliminated, in order by their seed in 
	// this outround.
	//---------------------------------------------------------------------------
	static ArrayList<Entry> seed (ElimRound round)
	{
		Tournament tournament = round.getTournament(); 
	
		if (tournament.getBreaks().isEmpty()) //if it hasn't broken yet 
			return seed (tournament); 
			
		//After this point, the tournament has already broken. 
	
		if (round.equals(tournament.getElims().get(0)))
		{
			return tournament.getBreaks(); 
		}
		
		
		ArrayList<ElimRound> elims = round.getTournament().getElims();
		ArrayList<Entry> seeds = new ArrayList<Entry>();
		
		//Recursively get seeds from the outround immediately before this one 
		ElimRound previousRd = (ElimRound) round.getPreviousRound(); 
		ArrayList<Entry> previousSeeds = seed (previousRd); 
		
		int i = 0; 
		
		//Cycle through the student who should be in this round - e.g., if it's 
		//semis, the top four. 
		while (i < round.getLevel().getNumEntries()) 
		{
			//If the ith entry won the previous outround, or if its debate hasn't
			//happened yet, it keeps its seed.
			Entry entry = previousSeeds.get(i); 
			Debate.Outcome decision = previousRd.getDebate(entry) != null 
										? previousRd.getDebate(entry).getDecision(entry)
										: Debate.Outcome.NO_DECISION; 

			if (decision == Debate.Outcome.WIN 
				|| decision == Debate.Outcome.BYE
				|| decision == Debate.Outcome.NO_DECISION)
			{
				seeds.add (entry); 
			}
			//Otherwise, if its "complement" (the entry it debated in that 
			//outround, based on seeds) won its round, the complement steals its 
			//seed. 
			else
			{
				int nComp =	getComplementNumber(previousRd.getLevel(), i); 
				if (previousSeeds.size() <= nComp)
					continue; //skip if there is no complement 
				Entry complement = previousSeeds.get(nComp); 
				Debate.Outcome decision2 = previousRd.getDebate(complement) != null 
							? previousRd.getDebate(complement).getDecision(complement)
							: Debate.Outcome.NO_DECISION; 
				
				if (decision2 == Debate.Outcome.WIN 
					|| decision2 == Debate.Outcome.BYE)
					seeds.add (complement); 
			}
			
			i++;
		}
		
		//Next, cycle through the remaining entries in the previous round and add 
		//them if the round hasn't been decided. 
		while (i < previousSeeds.size())
		{
			Entry entry = previousSeeds.get(i); 
			Debate.Outcome decision = previousRd.getDebate(entry) != null 
										? previousRd.getDebate(entry).getDecision(entry)
										: Debate.Outcome.NO_DECISION; 
			
			if ((decision == Debate.Outcome.WIN 
				|| decision == Debate.Outcome.BYE
				|| decision == Debate.Outcome.NO_DECISION)
				&& seeds.contains(entry) == false)
			{
				seeds.add (entry); 
			}
			
			i++;
		}
		
		return seeds; 
	}



	//---------------------------------------------------------------------------
	// Returns the entry's complement in the given outround, i.e. the team it 
	// is seeded to debate against. 
	//---------------------------------------------------------------------------
	static Entry getComplement (ElimRound round, Entry entry)
	{
		ArrayList<Entry> seeds = seed(round);
		
		int index = -1; //index of "entry", initialized to dummy value
		
		for (int i = 0; i < seeds.size(); i++)
		{
			if (seeds.get(i).equals(entry))
			{
				index = i;
				break;
			}
		}
		
		//if index is still at the original dummy value, this entry is not in this
		//outround. 
		if (index == -1)
			return null; 
		
		int nComp = getComplementNumber (round.getLevel(), index);
		
		//this may occur if it's a partial outround 
		if (nComp > seeds.size()-1)
			return null; 
		else
			return (seeds.get(nComp));
	}



	//---------------------------------------------------------------------------
	// Returns the index number (NOT the seed) of the "complement" of a given 
	// debater in a given outround. E.g., if it's quarters (8 entries), the index
	// of the opponent of the 2nd debater (3 seed) is 8 - 2 - 1 = 5 (6th 
	// seed). "index" is the index number (again, not the seed) of the given
	// debater. Note that this works in general, whether "index" is in the top 
	// or bottom half of the seeds. 
	//---------------------------------------------------------------------------
	static int getComplementNumber (Outround level, int index)
	{
		return level.getNumEntries() - index - 1; 
	}










	//---------------------------------------------------------------------------
	// Internal method - implements merge sort algorithm 
	//---------------------------------------------------------------------------
	private static void mergeSort (ArrayList objects, int start, int end)
	{
		if (start < end)
		{
			int mid = (start + end) / 2; //integer division
			mergeSort (objects, start, mid);
			mergeSort (objects, mid + 1, end);
			merge (objects, start, mid, end);
		}
	}

	//--------------------------------------------------------------------------
	// Internal method used by mergeSort
	//--------------------------------------------------------------------------
	private static void merge (ArrayList objects, int start, int mid, 
										int end)
	{
		ArrayList left = new ArrayList();
		ArrayList right = new ArrayList();
		int i, j, k;
		int length1 = mid - start + 1;
		int length2 = end - mid;

		for (i = 0; i < length1; i++)
			left.add(objects.get(start + i));
		for (j = 0; j < length2; j++)
			right.add(objects.get(mid + 1 + j));

		i = 0;
		j = 0;
		k = start;

		while (i < length1 && j < length2)
		{
			//Cast the currently selected objects into Comparable objects to use
			//the compareTo method.
			Comparable leftObj = (Comparable) left.get(i);
			Comparable rightObj = (Comparable) right.get(j);
			if (leftObj.compareTo(rightObj) <= 0)
			{
				objects.set(k, left.get(i));
				i++;
				k++;
			}
			else
			{
				objects.set(k, right.get(j));
				j++;
				k++;
			}
		}

		if (i == length1)
		{
			while (k <= end)
			{
				objects.set(k, right.get(j));
				j++;
				k++;
			}
		}
		else
		{
			while (k <= end)
			{
				objects.set(k, left.get(i));
				i++;
				k++;
			}
		}
	}


	//--------------------------------------------------------------------------
	// Internal method - implements merge sort algorithm 
	//--------------------------------------------------------------------------
	private static void mergeSortSeeds (ArrayList<Entry> objects, int start, 
													int end)
	{
		if (start < end)
		{
			int mid = (start + end) / 2; //integer division
			mergeSortSeeds (objects, start, mid);
			mergeSortSeeds (objects, mid + 1, end);
			mergeSeeds (objects, start, mid, end);
		}
	}

	//--------------------------------------------------------------------------
	// Internal method used by mergeSort
	//--------------------------------------------------------------------------
	private static void mergeSeeds (ArrayList<Entry> objects, int start, int mid, 
										int end)
	{
		ArrayList<Entry> left = new ArrayList<Entry>();
		ArrayList<Entry> right = new ArrayList<Entry>();
		int i, j, k;
		int length1 = mid - start + 1;
		int length2 = end - mid;

		for (i = 0; i < length1; i++)
			left.add(objects.get(start + i));
		for (j = 0; j < length2; j++)
			right.add(objects.get(mid + 1 + j));

		i = 0;
		j = 0;
		k = start;

		while (i < length1 && j < length2)
		{
			Entry leftObj = left.get(i);
			Entry rightObj = right.get(j);
			if (leftObj.compareSeedTo(rightObj) <= 0)
			{
				objects.set(k, left.get(i));
				i++;
				k++;
			}
			else
			{
				objects.set(k, right.get(j));
				j++;
				k++;
			}
		}

		if (i == length1)
		{
			while (k <= end)
			{
				objects.set(k, right.get(j));
				j++;
				k++;
			}
		}
		else
		{
			while (k <= end)
			{
				objects.set(k, left.get(i));
				i++;
				k++;
			}
		}
	}
}












