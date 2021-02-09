package org.optaplanner.examples.batchscheduling.domain.solver;

import java.io.Serializable;
import java.util.Comparator;
import org.optaplanner.examples.batchscheduling.domain.AllocationPath;


public class RoutePathStrengthWeightFactory implements Comparator<AllocationPath>, Serializable {

	private static final long serialVersionUID = 2970371491784586248L;

	public int compare(AllocationPath a, AllocationPath b) 
	{
		if ((a != null) && (b!= null))
		{
			if (a.getRoutePathList().size() == b.getRoutePathList().size())
			{
				return 0;
			} else if (a.getRoutePathList().size() > b.getRoutePathList().size())
			{
				return -1;
			}
			else
			{
				return 1;
			}
		}	
	
		return 0;
	}

}
