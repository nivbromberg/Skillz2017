package bots;

import pirates.*;
import java.util.*;

public class MyBot implements PirateBot {
	// CONSTANTS
	private final int attackingDistanceFromCity = 0;
	private final int defendingDistanceFromCity = 3;
	private final int closeDroneToCityDistance = 7;
	private final int closePirateToCityDistance = 5;
	private final int droneDangerDistanceFromEnemyPirate = 4;
	private double weightDistanceMultiplier = 3.5;

	// GAME DETAILS
	private PirateGame game;
	private List<Pirate> enemyPirates;
	private List<Drone> enemyDrones;
	private List<Pirate> myPirates;
	private List<Island> neutralIslands;
	private List<Island> enemyIslands;
	private List<Island> neutralAndEnemyIslands;
	private List<Island> myIslands;
	private List<Drone> myDrones;
	private List<City> myCities;
	private List<City> enemyCities;
	private int maxDistance;

	private List<Enemy> staticEnemies;
	private MyPirate[] movingPirates;

	private Decoy myDecoy;
	private Decoy enemyDecoy;
	private int numberOfEnemyPirates;
	private LocationQueue locationQueue = null;

	@Override
	public void doTurn(PirateGame game) {
		init(game);

		if (myCities.size() == 0 && game.getNeutralCities().size() == 0) // MAP : shaldag
		{
			shaldag();
		}

		else if (game.getAllIslands().size() == 3 && myCities.size() == 1 && enemyCities.size() == 1
				&& game.getAllMyPirates().size() == 5) {
			int toRemove = handleDecoy();
			Snoonit(toRemove);
			handleDrones();
		}

		else if (myCities.size() == 1 && enemyCities.isEmpty() && game.getAllEnemyPirates().size()==5)
		    gal();
		else if (myCities.size() == 1 && enemyCities.isEmpty() && game.getAllEnemyPirates().size()==3)
	        dvora();

		else // MAP : OTHER
		{
			checkSusp();

			handleDecoy();
			handlePirates();
			handleDrones();
			movePirates();
		}
	}

	private void gal() {
	    double multiplier=7;
		List<Pirate> unassignedPirates = new ArrayList<Pirate>(this.myPirates);
		Location piratesDestination;
		if (myIslands.isEmpty()) {
			piratesDestination = neutralIslands.get(0).getLocation();
		} else {
			piratesDestination = myCities.get(0).getLocation();
			List<Drone> myDrones = new ArrayList<>(this.myDrones);
			List<Drone> dronesInIsland = getDronesInIsland(myIslands.get(0), myDrones);
			if (dronesInIsland.size() < enemyPirates.size() * multiplier) {
				myDrones.removeAll(dronesInIsland);
			}
			for (Drone drone : myDrones) {
				goTo(drone, myCities.get(0).getLocation());
			}
		}
		handlePiratesImpulseAttack(unassignedPirates);
		for (Pirate pirate : unassignedPirates) {
			goTo(pirate, piratesDestination);
		}
	}
	
	/*private void dvora() {
		List<Pirate> unassignedPirates = new ArrayList<Pirate>(this.myPirates);
		handlePiratesImpulseAttack(unassignedPirates);
		if (myIslands.isEmpty()) {
			for (Pirate pirate : unassignedPirates) {
			    goTo(pirate, neutralIslands.get(0).getLocation());
		    }
		} else {
		    for(Pirate pirate:unassignedPirates){
		        switch(pirate.id){
		            case 0: goTo(pirate,new Location(pirate.getLocation().row-2,0));break;
		            case 1: goTo(pirate,new Location(pirate.getLocation().row,0));break;
		            default: goTo(pirate,new Location(pirate.getLocation().row+2,0));break;
		        }
		    }
	    	for (Drone drone : myDrones) {
    			goTo(drone, myCities.get(0).getLocation());
		    }
		}
	}*/
	
	private void dvora() {
	    Location piratesDestination;
		if (myIslands.isEmpty()) {
			piratesDestination = neutralIslands.get(0).getLocation();
			for (Pirate pirate : myPirates)
			{
			    goTo(pirate,piratesDestination);
			}
		}
		else if (myDrones.size() == 3)
		{
		    if (myDrones.get(0).location.col > 5)
		        goTo(myDrones.get(0),new Location(15,5));
		    else
		        goTo(myDrones.get(0), myCities.get(0).getLocation());
		    if (myDrones.get(1).location.col > 11)
		        goTo(myDrones.get(1),new Location(2,11));
		    else
		        goTo(myDrones.get(1), myCities.get(0).getLocation());
		    if (myDrones.get(2).location.col > 11)
		        goTo(myDrones.get(2),new Location(2,11));
		    else
		        goTo(myDrones.get(2), myCities.get(0).getLocation());
		    
		    for (Pirate pirate : myPirates)
			{
			    if (pirate.location.col > 10)
			        goTo(pirate,new Location(12,10));
			    else
			    {
			        List<Aircraft> attackable = attackableEnemies(pirate);
			        if (!attackable.isEmpty())
			        {
			            attack(pirate,attackable.get(0));
			        }
			        else
			        {
			            if (getClosestPirate(pirate,enemyPirates) != null)
			            {
			                goTo(pirate,getClosestPirate(pirate,enemyPirates).getLocation());
			            }
			        }
			    }
			}
		}
		
	}

	private List<Drone> getDronesInIsland(Island island, List<Drone> drones) {
		List<Drone> dronesInIsland = new ArrayList<>();
		for (Drone drone : drones) {
			if (drone.getLocation().equals(island.getLocation()))
				dronesInIsland.add(drone);
		}
		return dronesInIsland;
	}

	/**
	 * Handles the Shaldag map by chasing drones before they arrive in the city
	 */
	private void shaldag() {
		Pirate myPirate = myPirates.get(0);
		Drone drone = getClosestDroneToLocation(game.getEnemyCities().get(0).getLocation(), enemyDrones);

		if (drone != null && checkAttack(myPirate, drone)) {
			attack(myPirate, drone);
		} else if (drone != null && drone.distance(enemyCities.get(0)) <= closeDroneToCityDistance) {
			goTo(myPirate, drone.getLocation());
		} else {
			goTo(myPirate, enemyCities.get(0).location);
		}
	}

	private void Snoonit(int toRemove) {
		Island island = null;
		for (Island i : game.getAllIslands()) {
			if (i.id == 0) {
				island = i;
				break;
			}
		}
		List<Pirate> myPirates = game.getAllMyPirates();
		myPirates.remove(game.getMyPirateById(toRemove));
		myPirates.remove(game.getMyPirateById(4));
		handlePiratesImpulseAttack(myPirates);
		for (Pirate pirate : myPirates) {
			if (pirate.isAlive()) {
				goTo(pirate, island.location);
			}
		}
		Pirate pirate = game.getMyPirateById(4);
		if (pirate.isAlive()) {
			List<Aircraft> attackableEnemies = attackableEnemies(pirate);
			if (!attackableEnemies.isEmpty()) {
				attack(pirate, attackableEnemies.get(0));
			} else {
				Drone drone = getClosestDroneToLocation(game.getEnemyCities().get(0).getLocation(), enemyDrones);

				if (drone != null && checkAttack(pirate, drone)) {
					attack(pirate, drone);
				} else if (drone != null && drone.distance(enemyCities.get(0)) <= closeDroneToCityDistance) {
					goTo(pirate, drone.getLocation());
				} else {
					// goTo(pirate,enemyCities.get(0).location);
					goTo(pirate, new Location(8, 30));
				}
			}
		}
	}

	/**
	 * Checks if any enemy pirate is stationary (i.e. 3 or more turns in the
	 * same location).<br>
	 * If an enemy's pirate's distance to my city is 10 or less,
	 * 
	 * @author Niv Bromberg
	 */
	private void checkSusp() {
		if (myCities.size() > 0) {
			updateEnemyLocations();

			for (Enemy enemyPirate : staticEnemies) {
				if (enemyPirate.turnsThatStatic >= 3) {
					enemyPirate.isStatic = true;
					Pirate pirate = getClosestPirate(enemyPirate.pirate, myPirates);
					movingPirates[pirate.id].addDestination(enemyPirate.pirate.getLocation());
				}
			}
		}
	}

	/**
	 * In the first turn it initializes an arrayList to all enemy pirates In
	 * every other turn it updates the list
	 */
	private void updateEnemyLocations() {
		if (game.getTurn() == 1) {
			staticEnemies = new ArrayList<Enemy>();

			for (Pirate enemyPirate : game.getAllEnemyPirates()) {
				staticEnemies.add(new Enemy(enemyPirate));
			}
		} else {
			/*
			 * For unknown reasons, Object pirate does not updates and keeps the
			 * original location, so the loops check for the same enemy pirate
			 * as the static one and updates the reference
			 */
			for (Enemy staticPirate : staticEnemies) {
				if (staticPirate != null && staticPirate.pirate != null) {
					staticPirate.update(game.getAllEnemyPirates());
				}
			}
		}
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
		neutralAndEnemyIslands = new ArrayList<>(neutralIslands);
		neutralAndEnemyIslands.addAll(enemyIslands);
		myIslands = game.getMyIslands();
		myDrones = game.getMyLivingDrones();
		myCities = game.getMyCities();
		enemyCities = game.getEnemyCities();
		maxDistance = ((int) Math.sqrt(Math.pow(game.getRowCount(), 2) + Math.pow(game.getColCount(), 2))) + 1;

		movingPirates = new MyPirate[game.getAllMyPirates().size()];
		for (Pirate pirate : myPirates) {
			movingPirates[pirate.id] = new MyPirate(pirate, maxDistance);
		}
		if (game.getTurn() == 1) {
			if (neutralIslands.size() == 3 && myCities.size() == 1 && enemyCities.size() == 1)
				weightDistanceMultiplier = 6;
		}
	}

	/**
	 * Handles decoy pirates
	 * 
	 * @return The id of the ship removed. If no ship was removed, return -1
	 * 
	 * @since 26/2/2017
	 */
	private int handleDecoy() {
		//findEnemyDecoy();
		
		int decoyId = -1;
		if (locationQueue == null) {
			locationQueue = new LocationQueue();
			HashSet<Location> allLocations = new HashSet<>();
			for (Island island : game.getAllIslands()) {
				allLocations.add(island.getLocation());
			}

			for (City city : enemyCities) {
				allLocations.add(city.getLocation());
			}

			for (Location location : allLocations) {
				locationQueue.add(location);
			}
		}

		if (game.getMyself().turnsToDecoyReload == 0) {
			int rand = (int) (Math.random() * myPirates.size());
			game.decoy(myPirates.get(rand));
			myPirates.remove(rand);
			decoyId = rand;
		}
		myDecoy = game.getMyself().decoy;

		if (myDecoy != null) {
			goTo(myDecoy, locationQueue.getNext(myDecoy.location));
		}

		return decoyId;
	}
	
	/**
	 * Manage to get who is the enemy decoy
	 * 
	 * @since 28/2/2017
	 */
	private void findEnemyDecoy() {
		if (game.getTurn() == 1) {
			numberOfEnemyPirates = game.getAllEnemyPirates().size();
		}
		else {
			int temp  = game.getAllEnemyPirates().size();
			if (temp > numberOfEnemyPirates) {
				int[] id = new int[game.getAllEnemyPirates().size()];
				for (int i = 0; i < id.length; i++) { id[i] = 0; }
				
				for (Pirate enemyPirate : game.getAllEnemyPirates()) {					
					id[enemyPirate.id]++;
				}
				
				int decoyId = -1;
				for (int i = 0; i < id.length; i++) {
					if (id[i] > 1)
					{
						decoyId = i;
						break;
					}
				}
				
				if (decoyId != -1) // if decoy exists
				{
					decoyId = decoyId;
				}
			}
		}
	}

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
				game.debug("Pirate " + myAircraft + " goes to final location:" + tempDest);
			}
			return true;
		}
		return false;
	}

	private boolean goToDrone(Drone drone) {
		Location tempDest = chooseTempDestForDrone(drone, getClosestCity(drone, myCities).location, 0);
		if (tempDest != null) {
			game.setSail(drone, tempDest);
			return true;
		}
		return false;
	}

	private Location chooseTempDestForDrone(Drone drone, Location finalDest, int count) {
		List<Location> optionalTempDests = game.getSailOptions(drone, finalDest);
		for (Location tempDest : optionalTempDests) {
			if (getClosestPirate(tempDest, enemyPirates).distance(tempDest) > droneDangerDistanceFromEnemyPirate) {
				return tempDest;
			}
		}
		if (count >= 5) {
			return chooseRandomTempLocation(drone, finalDest);
		}
		if (Math.abs(finalDest.col - drone.location.col) < Math.abs(finalDest.row - drone.location.row)) {
			final int totalCols = game.getColCount();
			int newCol1 = finalDest.col - totalCols / 2;
			int newCol2 = finalDest.col + totalCols / 2;
			Location loc1 = null, loc2 = null;
			if (newCol1 >= 0)
				loc1 = chooseTempDestForDrone(drone, new Location(finalDest.row, newCol1), count + 1);
			if (newCol2 < totalCols)
				loc2 = chooseTempDestForDrone(drone, new Location(finalDest.row, newCol2), count + 1);
			if (loc1 == null)
				return loc2;
			else if (loc2 == null)
				return loc1;
			if (getClosestPirate(loc1, enemyPirates).distance(loc1) > droneDangerDistanceFromEnemyPirate
					&& getClosestPirate(loc2, enemyPirates).distance(loc2) > droneDangerDistanceFromEnemyPirate) {
				int rand = (int) (Math.random() * 2);
				if (rand == 0)
					return loc1;
				else
					return loc2;
			} else if (getClosestPirate(loc1, enemyPirates).distance(loc1) > droneDangerDistanceFromEnemyPirate)
				return loc1;
			else
				return loc2;

		} else {
			final int totalRows = game.getRowCount();
			int newRow1 = finalDest.row - totalRows / 2;
			int newRow2 = finalDest.row + totalRows / 2;
			Location loc1 = null, loc2 = null;
			if (newRow1 >= 0)
				loc1 = chooseTempDestForDrone(drone, new Location(newRow1, finalDest.col), count + 1);
			if (newRow2 < totalRows)
				loc2 = chooseTempDestForDrone(drone, new Location(newRow2, finalDest.col), count + 1);
			if (loc1 == null)
				return loc2;
			else if (loc2 == null)
				return loc1;
			if (getClosestPirate(loc1, enemyPirates).distance(loc1) > droneDangerDistanceFromEnemyPirate
					&& getClosestPirate(loc2, enemyPirates).distance(loc2) > droneDangerDistanceFromEnemyPirate) {
				int rand = (int) (Math.random() * 2);
				if (rand == 0)
					return loc1;
				else
					return loc2;
			} else if (getClosestPirate(loc1, enemyPirates).distance(loc1) > droneDangerDistanceFromEnemyPirate)
				return loc1;
			else
				return loc2;
		}
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
		if (enemyCities.size() > 0)
		{
			handleRushToDefendPirates(unassignedPirates);
		}
		handlePiratesImpulseAttack(unassignedPirates);
		handlePiratesToIslands(unassignedPirates);
		handleUnassignedPirates(unassignedPirates);
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
		List<Pirate> piratesToRemove = new ArrayList<Pirate>();

		for (Pirate pirate : myPirates) {
			List<Aircraft> attackableEnemies = attackableEnemies(pirate);
			if (!attackableEnemies.isEmpty()) {
				attack(pirate, attackableEnemies.get(0));
				piratesToRemove.add(pirate);
			}
		}

		for (Pirate pirate : piratesToRemove) {
			myPirates.remove(pirate);
			movingPirates[pirate.id].didAttack = true;
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
		List<Pirate> piratesToRemove = new ArrayList<Pirate>();
		for (Pirate pirate : myPirates) {
			// assigns the closest island available in the following priority
			// order:
			// 1.neutral islands
			// 2.enemy islands
			// 3.my islands
			Island bestIsland = null;
			/*
			 * if (!neutralIslands.isEmpty()) { City closestCity =
			 * getClosestCity(pirate, myCities); Island closestIslandToPirate =
			 * getClosestIsland(pirate, neutralIslands), closestIslandToCity =
			 * getClosestIsland(closestCity, neutralIslands);
			 * game.debug(pirate+","+closestIslandToPirate+","+
			 * closestIslandToCity); if
			 * (closestIslandToPirate.equals(closestIslandToCity)) bestIsland =
			 * closestIslandToPirate; else { double weight1 =
			 * pirate.distance(closestIslandToPirate)
			 * +weightDistanceMultiplier*closestCity.distance(
			 * closestIslandToPirate);
			 * 
			 * double weight2 = pirate.distance(closestIslandToCity) +
			 * weightDistanceMultiplier*closestCity.distance(closestIslandToCity
			 * ); game.debug(pirate+","+weight1+","+weight2+","+
			 * weightDistanceMultiplier); if (weight1 < weight2) bestIsland =
			 * closestIslandToPirate; else if (weight1 == weight2) {
			 * List<Island> temp = new ArrayList<>();
			 * temp.add(closestIslandToCity); temp.add(closestIslandToPirate);
			 * bestIsland = getClosestIsland(pirate, temp); } else { bestIsland
			 * = closestIslandToCity; } }
			 * 
			 * } else if (!enemyIslands.isEmpty()) { bestIsland =
			 * getClosestIsland(pirate, enemyIslands); }
			 */
			if (!neutralAndEnemyIslands.isEmpty()) {
				City closestCity = getClosestCity(pirate, myCities);
				Island closestIslandToPirate = getClosestIsland(pirate, neutralAndEnemyIslands),
						closestIslandToCity = getClosestIsland(closestCity, neutralAndEnemyIslands);
				game.debug(pirate + "," + closestIslandToPirate + "," + closestIslandToCity);
				if (closestIslandToPirate.equals(closestIslandToCity))
					bestIsland = closestIslandToPirate;
				else {
					double weight1 = pirate.distance(closestIslandToPirate)
							+ weightDistanceMultiplier * closestCity.distance(closestIslandToPirate);

					double weight2 = pirate.distance(closestIslandToCity)
							+ weightDistanceMultiplier * closestCity.distance(closestIslandToCity);
					game.debug(pirate + "," + weight1 + "," + weight2 + "," + weightDistanceMultiplier);
					if (weight1 < weight2)
						bestIsland = closestIslandToPirate;
					else if (weight1 == weight2) {
						List<Island> temp = new ArrayList<>();
						temp.add(closestIslandToCity);
						temp.add(closestIslandToPirate);
						bestIsland = getClosestIsland(pirate, temp);
					} else {
						bestIsland = closestIslandToCity;
					}
				}
			} else if (!myIslands.isEmpty()) {
				bestIsland = getClosestIsland(pirate, myIslands);
			}
			if (bestIsland != null) {
				// orders the pirate to move to the chosen island if not in the
				// islnad's control area, if it is then does nothing.
				// PLEASE NOTICE: if the pirate is in the island's control area,
				// the pirate is still removed from the pirates list in order to
				// maintain his control on the island.
				if (!bestIsland.inControlRange(pirate)) {
					movingPirates[pirate.id].addDestination(bestIsland.location);
				}
				// Checks if closest island is neutral, if it is, the island is
				// removed from the neutral islands list in order to not send
				// multiple pirates to the same neutral island (strategic
				// option).
				// if (bestIsland.owner.equals(game.getNeutral()))
				// neutralIslands.remove(bestIsland);

				// removes pirate from pirates list in order to not assign
				// multiple actions to the same pirate.
				piratesToRemove.add(pirate);
			}
		}

		for (Pirate pirate : piratesToRemove) {
			myPirates.remove(pirate);
		}
	}

	private void handleRushToDefendPirates(List<Pirate> myPirates) {
		List<Pirate> piratesToRemove = new ArrayList<Pirate>();
		City closestCity;
		for (Pirate pirate : myPirates) {
			closestCity = getClosestCity(pirate, enemyCities);
			Drone drone = getClosestDroneToLocation(closestCity.location, enemyDrones);
			double reqRangeCity = 3 * closeDroneToCityDistance;
			double reqRangePirate = 3 * game.getAttackRange();
			if (drone != null && drone.distance(closestCity) <= reqRangeCity && drone.distance(pirate) <= reqRangePirate) {
				rushToDrone(pirate, drone);
				piratesToRemove.add(pirate);
			}
		}

		for (Pirate pirate : piratesToRemove) {
			myPirates.remove(pirate);
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
				movingPirates[pirate.id].addDestination(closestIsland.location);
			}
		}
	}

	/**
	 * Moves all my pirates to their closest destination
	 */
	private void movePirates() {
		for (MyPirate pirate : movingPirates) {
			if (pirate != null && !pirate.didAttack && pirate.getClosestLocation() != null) {
				goTo(pirate.pirate, pirate.getClosestLocation());
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
		List<City> possibleCities = myCities;
		possibleCities.addAll(game.getNeutralCities());
		
		for (Drone d : myDrones) {
			goTo(d, getClosestCity(d, possibleCities).getLocation());
		}
	}

	/**
	 * Finds closest city to a given aircraft
	 * 
	 * @param a
	 *            Given aircraft
	 * @param l
	 *            List of cities
	 * 
	 * @return closest city
	 */
	private City getClosestCity(Aircraft a, List<City> l) {
		int minDistance = this.maxDistance;
		City closestObject = null;
		for (City c : l) {
			if (a.distance(c) < minDistance || closestObject == null) {
				minDistance = a.distance(c);
				closestObject = c;
			}
		}
		return closestObject;
	}

	private City getClosestCity(Enemy a, List<City> l) {
		return getClosestCity(a.pirate, l);
	}

	/**
	 * Finds closest island to a given aircraft
	 * 
	 * @param a
	 *            Given aircraft
	 * @param l
	 *            List of islands
	 * 
	 * @return closest island
	 */
	private Island getClosestIsland(Aircraft a, List<Island> l) {
		int minDistance = this.maxDistance;
		Island closestObject = null;
		for (Island i : l) {
			if (a.distance(i) < minDistance || closestObject == null) {
				minDistance = a.distance(i);
				closestObject = i;
			}
		}
		return closestObject;
	}

	private Island getClosestIsland(City city, List<Island> islands) {
		int minDistance = this.maxDistance;
		Island closestObject = null;
		for (Island i : islands) {
			if (city.distance(i) < minDistance || closestObject == null) {
				minDistance = city.distance(i);
				closestObject = i;
			}
		}
		return closestObject;
	}

	private Pirate getClosestPirate(Aircraft aircraft, List<Pirate> pirates) {
		return getClosestPirate(aircraft.getLocation(), pirates);
	}

	private Pirate getClosestPirate(MapObject mapObject, List<Pirate> pirates) {
		return getClosestPirate(mapObject.getLocation(), pirates);
	}

	private Pirate getClosestPirate(Location location, List<Pirate> pirates) {
		int minDistance = this.maxDistance;
		Pirate closestObject = null;
		for (Pirate p : pirates) {
			if (p.distance(location) < minDistance || closestObject == null) {
				minDistance = p.distance(location);
				closestObject = p;
			}
		}
		return closestObject;
	}

	/**
	 * Finds closest drone to a given location
	 * 
	 * @param loc
	 *            Given location
	 * @param l
	 *            List of drones
	 * 
	 * @return closest drone
	 */
	private Drone getClosestDroneToLocation(Location loc, List<Drone> l) {
		int minDistance = this.maxDistance;
		Drone closestObject = null;
		for (Drone drone : l) {
			if (drone.distance(loc) < minDistance || closestObject == null) {
				minDistance = drone.distance(loc);
				closestObject = drone;
			}
		}
		return closestObject;
	}

	/**
	 * Finds closest pirate to a given location
	 * 
	 * @param loc
	 *            Given location
	 * @param l
	 *            List of pirates
	 * 
	 * @return closest pirate
	 */
	private Pirate getClosestPirateToLocation(Location loc, List<Pirate> l) {
		int minDistance = this.maxDistance;
		Pirate closestObject = null;
		for (Pirate pirate : l) {
			if (pirate.distance(loc) < minDistance || closestObject == null) {
				minDistance = pirate.distance(loc);
				closestObject = pirate;
			}
		}
		return closestObject;
	}

	private void rushToDrone(Pirate myPirate, Drone drone) {
		if (checkAttack(myPirate, drone))
			attack(myPirate, drone);
		else
			goTo(myPirate, drone.location);
	}
}
