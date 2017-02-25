package bots;

import pirates.*;
import java.util.*;

/**
 * The purpose of this class is to handle pirates with multiple destinations, and to determine which is better
 * 
 * @author Niv Bromberg
 * @since 24/02/2017
 */

public class MovingPirate {
	public Pirate pirate;
	private Set<Location> destinations;
	private int maxDistance;
	public boolean didAttack;
	
	/**
	 * Initializes all attributes and saves the pirate to be handled
	 * 
	 * @param pirate
	 *            The pirate which will be handled
	 * @param maxDistance
	 * 			  The maximum distance possible in the game
	 */
	public MovingPirate(Pirate pirate, int maxDistance)
	{
		this.pirate = pirate;
		this.maxDistance = maxDistance;
		destinations = new HashSet<Location>();
		didAttack = false;
	}
	
	/**
	 * Adds a given location to destinations
	 * 
	 * @param location
	 * 			A destination
	 */
	public void addDestination(Location location)
	{
		destinations.add(location);
	}
	
	/**
	 * @return the closest location to the pirate
	 */
	public Location getClosestLocation()
	{
		int minDistance = maxDistance;
		Location closestObject = null;
        for (Location location : destinations) {
            if (pirate.distance(location) < minDistance || closestObject == null)
            {
                minDistance = pirate.distance(location);
                closestObject = location;
            }
        }
        return closestObject;
	}
}