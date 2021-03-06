package bots;

import pirates.*;
import java.util.*;

/**
 * @author Shahaf Cohen Tarica
 *
 * @since 22.2.17
 */
public class Shahaf_22_2 implements PirateBot {
	// GAME DETAILS
	private PirateGame game;
	private List<Pirate> enemyPirates;
	private List<Drone> enemyDrones;
	private List<Pirate> myPirates;
	private List<Island> neutralIslands;
	private List<Island> enemyIslands;
	private List<Island> myIslands;
	private List<Drone> myDrones;
	private List<City> myCities;
	private int minDistance;

	private int[] turnsThatStatic;
	private Location[] enemyPiratesLocations;

	private boolean stayThere = false;
	private int pirateToStay;
	private Location whereToStay;

	@Override
	public void doTurn(PirateGame game) {
		init(game);

		if (stayThere) {
			// pirateToStay =
			// getClosestPirateToLocation(whereToStay,myPirates).id;
			game.debug("#1 pirateToStay=" + pirateToStay + ", whereToStay=" + whereToStay);
			goTo(game.getMyPirateById(pirateToStay), whereToStay);
			myPirates.remove(game.getMyPirateById(pirateToStay));
		}
	//	checkSusp();

		handlePirates();
		handleDrones();
	}

	/**
	 * Initializes all attributes in the 'GAME DETAILS' sector attributes
	 * 
	 * @param game
	 *            The PirateGame given in doTurn
	 */
	private void init(PirateGame game) {
		this.game = game;
		enemyPirates = game.getEnemyLivingPirates();
		enemyDrones = game.getEnemyLivingDrones();
		myPirates = game.getMyLivingPirates();
		neutralIslands = game.getNeutralIslands();
		enemyIslands = game.getEnemyIslands();
		myIslands = game.getMyIslands();
		myDrones = game.getMyLivingDrones();
		myCities = game.getMyCities();
		minDistance = ((int) Math.sqrt(Math.pow(game.getRowCount(), 2) + Math.pow(game.getColCount(), 2))) + 1;
	}

/*	private void checkSusp() {
		checkEnemyLocations();

		for (Pirate ep : enemyPirates) {
			if (turnsThatStatic[ep.id] >= 3
					&& getClosestPirateToLocation(myCities.get(0).getLocation(), enemyPirates).equals(ep)
					&& !stayThere) {
				Pirate p = getClosestPirate(ep, myPirates);
				if (p != null) {
					myPirates.remove(p);
					stayThere = true;
					pirateToStay = p.id;
					whereToStay = ep.getLocation();
					game.debug("#2 Pirate " + p.id + " goes to stay where enemy " + ep.id);
					goTo(p, ep.getLocation());
				}
			}  

			else if (turnsThatStatic[ep.id] >= 3) {
				Pirate p = getClosestPirate(ep, myPirates);
				if (p != null) {
					myPirates.remove(p);
					game.debug("#3 Pirate " + p.id + " goes to attack static enemy " + ep.id);
					goTo(p, ep.getLocation());
				}
			} 
		}
	}

	private void checkEnemyLocations() {
		if (game.getTurn() == 1) {
			turnsThatStatic = new int[enemyPirates.size()];
			for (int i = 0; i < turnsThatStatic.length; i++) {
				turnsThatStatic[i] = 0;
			}

			enemyPiratesLocations = new Location[enemyPirates.size()];

			for (Pirate ep : enemyPirates) {
				enemyPiratesLocations[ep.id] = ep.getLocation();
			}
		} else {
			for (Pirate ep : enemyPirates) {
				if (ep.getLocation().equals(enemyPiratesLocations[ep.id])) {
					turnsThatStatic[ep.id]++;
				} else {
					enemyPiratesLocations[ep.id] = ep.getLocation();
					turnsThatStatic[ep.id] = 0;
				}
			}
		}
	} */

	/**
	 * Returns all enemy aircrafts that can be attacked by myPirate according to
	 * {@link #checkAttack(Pirate, MapObject)}
	 * 
	 * @param myPirate
	 *            My pirate that is checked if he can attack
	 * 
	 * @return List&lt;Aircraft&gt; - all enemy aircrafts that can be attacked
	 *         by myPirate. If none exist then the list is empty.
	 * 
	 * @see MyBot_22_2#checkAttack(Pirate, MapObject)
	 * 
	 * @since 22.2.17
	 */
	private List<Aircraft> attackableEnemies(Pirate myPirate) {
		List<Aircraft> enemiesThatCanBeAttacked = new ArrayList<>();
		for (Pirate enemyPirate : enemyPirates) {
			if (checkAttack(myPirate, enemyPirate)) {
				enemiesThatCanBeAttacked.add(enemyPirate);
			}
		}

		for (Drone enemyDrone : enemyDrones) {
			if (checkAttack(myPirate, enemyDrone)) {
				enemiesThatCanBeAttacked.add(enemyDrone);
			}
		}
		return enemiesThatCanBeAttacked;
	}

	/**
	 * Orders myPirate to attack target
	 * 
	 * @param myPirate
	 *            My pirate that attacks
	 * @param target
	 *            The target that is attacked
	 * @see MyBot_22_2#attackableEnemies(Pirate)
	 * @see MyBot_22_2#checkAttack(Pirate, MapObject)
	 * @since 22.2.17
	 */
	private void attack(Pirate myPirate, Aircraft target) {
		game.attack(myPirate, target);
	}

	/**
	 * Checks if myPirate can attack target
	 * 
	 * @param myPirate
	 *            My pirate that is checked if he can attack
	 * @param target
	 *            The map object that is checked if it can be attacked by
	 *            myPirate
	 * @return true - if myPirate can attack target, false - otherwise.
	 * @since 22.2.17
	 */
	private boolean checkAttack(Pirate myPirate, MapObject target) {
		if (!myPirate.inAttackRange(target) || !myPirate.isAlive())
			return false;
		return true;
	}

	/**
	 * Orders myAircraft to move towards finalDest according to
	 * {@link #chooseRandomTempLocation(Aircraft, Location)}
	 * 
	 * @param myAircraft
	 *            My aircraft that is ordered to move
	 * @param finalDest
	 *            The final destination of myAircraft
	 * 
	 * @return true - if the aircraft has moved, false - otherwise
	 * 
	 * @see MyBot_22_2#chooseRandomTempLocation(Aircraft, Location)
	 */
	private boolean goTo(Aircraft myAircraft, Location finalDest) {
		Location tempDest = chooseRandomTempLocation(myAircraft, finalDest);
		if (tempDest != null) {
			game.setSail(myAircraft, tempDest);
			if (myAircraft instanceof Pirate) {
				game.debug("Pirate " + myAircraft + " goes to location:" + tempDest);
			}
			return true;
		}
		return false;
	}

	/**
	 * Returns the location that myAircraft needs to go to this turn towards
	 * finalDest.
	 * 
	 * @param myAircraft
	 *            My aircraft that needs to move
	 * @param finalDest
	 *            The final destination of myAircraft
	 * 
	 * @return Location - the temporary location that myAircraft will do this
	 *         turn.
	 */
	private Location chooseRandomTempLocation(Aircraft myAircraft, Location finalDest) {
		List<Location> optionalTempDest = game.getSailOptions(myAircraft, finalDest);
		// The while loop chooses a random location from optionalTempDest to
		// guarantee
		// a non predictable path.
		while (!optionalTempDest.isEmpty()) {
			int randInt = (int) (Math.random() * optionalTempDest.size());
			if (!myAircraft.getLocation().equals(optionalTempDest.get(randInt))) {
				return optionalTempDest.get(randInt);
			} else
				optionalTempDest.remove(randInt);
		}
		return null;
	}

	/**
	 * Handles pirates movement.<br>
	 * Priority: 1. Attack any enemy that is in range according to
	 * {@link #handlePiratesImpulseAttack(List)}<br>
	 * 2. Move pirates to conquer islands according to
	 * {@link #handlePiratesToIslands(List)}<br>
	 * 3. Move pirates without assigned action according to
	 * {@link #handleUnassignedPirates(List)}
	 * 
	 * @see MyBot_22_2#handlePiratesImpulseAttack(List)
	 * @see MyBot_22_2#handlePiratesToIslands(List)
	 * @see MyBot_22_2#handleUnassignedPirates(List)
	 * 
	 * @since 22.2.17
	 */
	private void handlePirates() {
		List<Pirate> unassignedPirates = new ArrayList<Pirate>(this.myPirates);
		handlePiratesImpulseAttack(unassignedPirates);
		handlePiratesToIslands(unassignedPirates);
		handleUnassignedPirates(unassignedPirates);
		/*
		 * for (Pirate ep : enemyPirates) { if (ep.distance(myCities.get(0)) <=
		 * 8 && minEnDisToCity > ep.distance(myCities.get(0))) { minEnDisToCity
		 * = ep.distance(myCities.get(0)); closestEnemyToCity = ep; } }
		 * 
		 * if(closestEnemyToCity != null) { Pirate p =
		 * getClosestPirate(closestEnemyToCity,myPirates); game.debug(
		 * "L197 Pirate "+p+" goes to attack enemy "+closestEnemyToCity);
		 * goTo(p,closestEnemyToCity.getLocation()); myPirates.remove(p); }
		 * 
		 * for (Pirate p : myPirates) { if (neutralIslands.size() != 0) { Island
		 * closestIsland = getClosestIsland(p, neutralIslands); if
		 * (closestIsland != null) { goTo(p, closestIsland);
		 * neutralIslands.remove(closestIsland); } } else if
		 * (enemyIslands.size() != 0) { Island closestIsland =
		 * getClosestIsland(p, enemyIslands); if (closestIsland != null) {
		 * goTo(p, closestIsland); } } else if (myIslands.size() != 0) { Island
		 * closestIsland = getClosestIsland(p, myIslands); if (closestIsland !=
		 * null) { goTo(p, closestIsland); } } else unassignedPirates.add(p); }
		 * 
		 * for (Pirate pirate : unassignedPirates) { Island closestIsland =
		 * getClosestIsland(pirate, neutralIslands); if (closestIsland != null)
		 * { goTo(pirate, closestIsland); } }
		 */
	}

	/**
	 * Orders all pirates that have an enemy in range according to
	 * {@link #attackableEnemies(Pirate)} to attack the enemy
	 * 
	 * @param myPirates
	 *            Pirates to order to attack an enemy if possible
	 * 
	 * @see MyBot_22_2#attackableEnemies(Pirate)
	 * @see MyBot_22_2#checkAttack(Pirate, MapObject)
	 * @see MyBot_22_2#attack(Pirate, Aircraft)
	 * 
	 * @since 22.2.17
	 */
	private void handlePiratesImpulseAttack(List<Pirate> myPirates) {
		for (Pirate pirate : myPirates) {
			List<Aircraft> attackableEnemies = attackableEnemies(pirate);
			if (!attackableEnemies.isEmpty()) {
				attack(pirate, attackableEnemies.get(0));
				myPirates.remove(pirate);
			}
		}
	}

	/**
	 * Handles movement of pirates (from parameter myPirates) to conquer/defend
	 * islands.<br>
	 * Priority: 1. Conquer neutral islands<br>
	 * 2. Conquer enemy islands<br>
	 * 3. Defend our islands
	 * 
	 * @param myPirates
	 *            Pirates to send to conquer islands.
	 * 
	 * @since 22.2.17
	 */
	private void handlePiratesToIslands(List<Pirate> myPirates) {
		List<Island> neutralIslands = new ArrayList<Island>(this.neutralIslands);
		for (Pirate pirate : myPirates) {
			// assigns the closest island available in the following priority
			// order:
			// 1.neutral islands
			// 2.enemy islands
			// 3.my islands
			Island closestIsland = null;
			if (!neutralIslands.isEmpty()) {
				closestIsland = getClosestIsland(pirate, neutralIslands);
			} else if (!enemyIslands.isEmpty()) {
				closestIsland = getClosestIsland(pirate, enemyIslands);
			} else if (!myIslands.isEmpty()) {
				closestIsland = getClosestIsland(pirate, myIslands);
			}
			if (closestIsland != null) {
				// orders the pirate to move to the chosen island if not in the
				// islnad's control area, if it is then does nothing.
				// PLEASE NOTICE: if the pirate is in the island's control area,
				// the pirate is still removed from the pirates list in order to
				// maintain his control on the island.
				if (!closestIsland.inControlRange(pirate)) {
					goTo(pirate, closestIsland.location);
				}
				// Checks if closest island is neutral, if it is, the island is
				// removed from the neutral islands list in order to not send
				// multiple pirates to the same neutral island (strategic
				// option).
				if (closestIsland.owner.equals(game.getNeutral()))
					neutralIslands.remove(closestIsland);

				// removes pirate from pirates list in order to not assign
				// multiple actions to the same pirate.
				myPirates.remove(pirate);
			}
		}
	}

	/**
	 * Handles movement of all unassigned pirates (from parameter).<br>
	 * Priority: 1. Send every unassigned pirate to conquer its closest neutral
	 * island.
	 * 
	 * @param unassignedPirates
	 *            Pirates without an assigned action.
	 * 
	 * @since 22.2.17
	 */
	private void handleUnassignedPirates(List<Pirate> unassignedPirates) {
		for (Pirate pirate : unassignedPirates) {
			Island closestIsland = getClosestIsland(pirate, neutralIslands);
			if (closestIsland != null && !closestIsland.inControlRange(pirate)) {
				goTo(pirate, closestIsland.location);
			}
		}
	}

	/**
	 * Handles drones movement<br>
	 * Priority: 1. Move every drone to its closest city.
	 * 
	 * @since 22.2.17
	 */
	private void handleDrones() {
		// game.debug("num of drones:"+myDrones.size());

		for (Drone d : myDrones) {
			goTo(d, getClosestCity(d, myCities).getLocation());
		}
	}

	private City getClosestCity(Aircraft a, List<City> l) {
		int minDistance = this.minDistance;
		City closestObject = null;
		for (City c : l) {
			if (a.distance(c) < minDistance || closestObject == null) {
				minDistance = a.distance(c);
				closestObject = c;
			}
		}
		return closestObject;
	}

	private Island getClosestIsland(Aircraft a, List<Island> l) {
		int minDistance = this.minDistance;
		Island closestObject = null;
		for (Island i : l) {
			if (a.distance(i) < minDistance || closestObject == null) {
				minDistance = a.distance(i);
				closestObject = i;
			}
		}
		return closestObject;
	}

	private Pirate getClosestPirate(Aircraft a, List<Pirate> l) {
		int minDistance = this.minDistance;
		Pirate closestObject = null;
		for (Pirate p : l) {
			if (p.distance(a) < minDistance || closestObject == null) {
				minDistance = p.distance(a);
				closestObject = p;
			}
		}
		return closestObject;
	}

	private Pirate getClosestPirateToLocation(Location loc, List<Pirate> l) {
		int minDistance = this.minDistance;
		Pirate closestObject = null;
		for (Pirate p : l) {
			if (p.distance(loc) < minDistance || closestObject == null) {
				minDistance = p.distance(loc);
				closestObject = p;
			}
		}
		return closestObject;
	}
}
