package bots;

import pirates.*;
import java.util.*;

public class WeightIslands
{
	private List<Island> islands;
	
	public WeightIslands()
	{
		islands = new ArrayList<>();
		weights = new ArrayList<>();
	}
	
	{
		islands.add(island);
		weights.add(weight);
	}
	
	public Island getMaxWeighted()
	{
		if (islands.size() == 0) { return null; }
		
		{
			if (w > max) { max = w; }
		}
		
		int maxIndex = weights.indexOf(max);
		return islands.get(maxIndex);
	}
}