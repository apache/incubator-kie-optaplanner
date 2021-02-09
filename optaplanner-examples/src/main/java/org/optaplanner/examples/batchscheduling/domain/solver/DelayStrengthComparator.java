package org.optaplanner.examples.batchscheduling.domain.solver;

import java.util.Comparator;

public class DelayStrengthComparator implements Comparator<Long> {

	public int compare(Long a, Long b) 
    {
		if ((a !=null) && (b != null)){	
			return b.compareTo(a);
		} 

		return 0;
    }
}
