package bots;

import pirates.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import javax.tools.JavaFileManager.Location;

/**
 * The purpose of this class is to handle aircraft that has queue of location to go through
 * 
 * @author Niv Bromberg
 * @since 26/02/2017
 */
public class LocationQueue
{
	private PriorityQueue<Location> queue;
	
	public LocationQueue()
	{
		queue = new PriorityQueue<Location>();
	}
	
	/**
	 * Checks if the aircraft is in its current destination, if so it sends it to the next one.<br>
	 * If not, it sends him to the current one.
	 * 
	 * @param currentLocation
	 * 			gets the aircraft current location
	 */
	public Location getNext(Location currentLocation)
	{
		if (queue.peek().equals(currentLocation))
		{
			queue.offer(queue.poll());			
		}
		
		return queue.peek();
	}
	
	public void add(Location location)
	{
		queue.offer(location);
	}
	
	public boolean isEmpty()
	{
		return queue.size() == 0;
	}
}