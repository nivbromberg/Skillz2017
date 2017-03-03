package bots;

import pirates.*;
import java.util.*;

public class WeightIslands
{
	private List<Island> islands;
	private List<Double> weights;
	
	public WeightIslands()
	{
		islands = new ArrayList<>();
		weights = new ArrayList<>();
	}
	
	public void addIsland(Island island, double weight)
	{
		islands.add(island);
		weights.add(weight);
	}
	
	public Island getMaxWeighted()
	{
		if (islands.size() == 0) { return null; }
		double max = weights.get(0);
		
		for (double w : weights)
		{
			if (w > max) { max = w; }
		}
		
		int maxIndex = weights.indexOf(max);
		return islands.get(maxIndex);
	}
}