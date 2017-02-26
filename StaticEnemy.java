package bots;

import pirates.*;
import java.util.*;

/**
 * The purpose of this class is to provide data about possible stationary
 * pirates
 * 
 * @author Niv Bromberg
 * @since 24/02/2017
 */

public class Enemy {
	public Pirate pirate;
	public int turnsThatStatic;
	private Location location;
	public boolean isStatic;

	/**
	 * Initializes all attributes and saves the pirates that is being watched
	 * 
	 * @param pirate
	 *            The pirate which will be watched
	 */
	public Enemy(Pirate pirate) {
		this.pirate = pirate;
		turnsThatStatic = 0;
		location = pirate.getLocation();
		isStatic = false;
	}

	/**
	 * Will be called every turn to identify possible stationary pirate If
	 * pirate is not alive counter will be initialized
	 * 
	 * @param pirates
	 *            - For unknown reasons, Object pirate does not updates and
	 *            keeps the original location, so the loops check for the same
	 *            enemy pirate as the static one and updates the reference
	 */
	public void update(List<Pirate> enemyPirates) {
		for (Pirate enemyPirate : enemyPirates) {
			if (pirate.id == enemyPirate.id) {
				pirate = enemyPirate;
			}
		}

		if (pirate.getLocation().equals(location) && pirate.isAlive()) {
			turnsThatStatic++;
		} else {
			location = pirate.getLocation();
			turnsThatStatic = 0;
			isStatic = false;
		}
	}

	public String toString() {
		return "Pirate " + pirate.id + ", turns: " + turnsThatStatic + ", loc: " + location;
	}
}