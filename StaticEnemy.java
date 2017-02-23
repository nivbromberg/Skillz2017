package bots;

import pirates.PirateGame;

/**
 *
 * @author Niv Bromberg
 * @since 24/02/2017
 */

public class StaticEnemy {
	private Pirate pirate;
	public int turnsThatStatic;
	private Location location;
	
	/**
	 * Initializes all attributes and saves the pirates that is being watched
	 * 
	 * @param pirate
	 *            The pirate which will be watched
	 */
	public StaticEnemy(Pirate pirate)
	{
		this.pirate = pirate;
		turnsThatStatic = 0;
		location = pirate.getLocation();
	}
	
	/**
	 * Will be called every turn to identify possible stationary pirate
	 * If pirate is not alive counter will be initialized
	 */
	public void update()
	{
		if (pirate.getLocation().equals(location) && pirate.isAlive())
        {
            turnsThatStatic++;
        }
        else
        {
        	location = pirate.getLocation();
            turnsThatStatic = 0;
        }
	}
}