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
	private List<City> neutralCities;
	private List<City> neutralAndEnemyCities;
	private List<City> possibleCities;
	private Set<Drone> coveredEnemyDrones;
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
		
		/*else if(myCities.size() == 1 && enemyCities.size() == 1 && game.getMyPirateById(0).initialLocation.equals(new Location(4,25)))
		{
			int toRemove = handleDecoy();
			Matzor(toRemove);
			handleDrones();
		}*/
		
		else if (game.getAllIslands().size() == 2 && myCities.size() == 1 && enemyCities.size() == 1 && game.getRowCount() == 31 && game.getColCount() == 51)
	    {
			List<Pirate> myPirates = new ArrayList<>(this.myPirates);
			if (game.getMyPirateById(0).initialLocation.equals(new Location(4,25)) &&
			        game.getMyPirateById(0).location.equals(new Location(4,25)))
	        {
	            goTo(game.getMyPirateById(0), new Location(6,25));
	            myPirates.remove(game.getMyPirateById(0));
	        }
			
			handleRushToDefendPirates(myPirates);
			handlePiratesImpulseAttack(myPirates);
			
			if (myPirates.contains(game.getMyPirateById(0)))
			{
				goTo(game.getMyPirateById(0), new Location(9,25));
	            myPirates.remove(game.getMyPirateById(0));
			}
			
	    	for (Pirate pirate : myPirates)
	    	{
	    		goTo(pirate, new Location(25,25));
	    	}
	    	
	    	handleDrones();
	    }

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
		if (possibleCities.size() > 0) {
			updateEnemyLocations();

			for (Enemy enemyPirate : staticEnemies) {
				if (enemyPirate.turnsThatStatic >= 3 && enemyPirate.pirate.inRange(getClosestCity(enemyPirate, possibleCities),6) &&
				    myDrones.size() > 0) {
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
	    if (game.getTurn() == 1) {
	        coveredEnemyDrones = new HashSet<>();
	    }
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
		possibleCities = new ArrayList<City>(myCities);
		possibleCities.addAll(game.getNeutralCities());
		enemyCities = game.getEnemyCities();
		neutralCities=game.getNeutralCities();
		neutralAndEnemyCities=new ArrayList<>(enemyCities);
		neutralAndEnemyCities.addAll(neutralCities);
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
		
		int decoyId = -1;
		Location destination = null;
		List<Pirate> myPirates = new ArrayList<>(this.myPirates);
		List<Pirate> piratesToRemove = new ArrayList<>();
		for (Pirate pirate : myPirates)
		{
		    if (attackableEnemies(pirate).size() > 0)
		    {
		        piratesToRemove.add(pirate);
		    }
		}
		for (Pirate p : piratesToRemove) { myPirates.remove(p); }
		
		if (myPirates.size() > 0 && game.getAllIslands().size() == 2 && neutralCities.size() == 1 && enemyCities.size() == 0 && game.getRowCount() == 25 && game.getColCount() == 41)
		{
			if (game.getMyself().turnsToDecoyReload == 0) {
				int pirateIdToDecoy = myPirates.get(0).id;
				game.decoy(game.getMyPirateById(pirateIdToDecoy));
				this.myPirates.remove(game.getMyPirateById(pirateIdToDecoy));
				decoyId = pirateIdToDecoy;
			}
			myDecoy = game.getMyself().decoy;
    		if (myDecoy != null)
    		{
		        destination = new Location(12,16);
    		}
		}
		
		else if (myPirates.size() > 0 && myCities.size() == 2 && enemyCities.size() == 2 && neutralCities.size() == 1 && game.getAllIslands().size() == 2 
				&& game.getAllIslands().get(0).location.equals(new Location(8,17)) && game.getAllIslands().get(1).location.equals(new Location(8,25)))
		{
			Location destinationLocation = new Location(12,21);
			Pirate decoyPirate = getClosestPirate(destinationLocation, myPirates);
			
	        if (game.getMyself().turnsToDecoyReload == 0) {
    			int pirateIdToDecoy = decoyPirate.id;
    			game.decoy(game.getMyPirateById(pirateIdToDecoy));
    			this.myPirates.remove(game.getMyPirateById(pirateIdToDecoy));
    			decoyId = pirateIdToDecoy;
	        }
    		myDecoy = game.getMyself().decoy;
		    destination = destinationLocation;
		}
		
		else if (enemyDrones.size() > 0 && enemyCities.size() > 0 && myPirates.size() > 0)
		{
		    if (game.getMyself().turnsToDecoyReload == 0) {
		        City closestCityToDrones = getClosestCityToDrone(enemyDrones.get(0), game.getNotMyCities());
    			int pirateIdToDecoy = getClosestPirate(closestCityToDrones.location, myPirates).id;
    			game.decoy(game.getMyPirateById(pirateIdToDecoy));
    			this.myPirates.remove(game.getMyPirateById(pirateIdToDecoy));
    			decoyId = pirateIdToDecoy;
    		}
    		myDecoy = game.getMyself().decoy;
    		if (myDecoy != null)
    		{
		        destination = getClosestCity(myDecoy, enemyCities).location;
    		}
		}
		
		else if (myDrones.size() > 0 && myPirates.size() > 0)
		{
		    if (game.getMyself().turnsToDecoyReload == 0) {
		        City closestCityToDrones = getClosestCityToDrone(myDrones.get(0), possibleCities);
    			int pirateIdToDecoy = getClosestPirate(closestCityToDrones.location, myPirates).id;
    			game.decoy(game.getMyPirateById(pirateIdToDecoy));
    			this.myPirates.remove(game.getMyPirateById(pirateIdToDecoy));
    			decoyId = pirateIdToDecoy;
    		}
    		myDecoy = game.getMyself().decoy;
		    if (myDecoy != null)
    		{
		        destination = getClosestCity(myDecoy, possibleCities).location;
    		}
		}
		
		/*else if (myPirates.size() > 0)
		{
		    City randCity = null;
		    if (enemyCities.size() > 0)
		    {
	            randCity = enemyCities.get((int)(Math.random()*enemyCities.size()));
		    }
	        else
	        {
	            randCity = possibleCities.get((int)(Math.random()*possibleCities.size()));
	        }
	        Island destinationIsland = getClosestIsland(randCity, game.getAllIslands());
	        if (game.getMyself().turnsToDecoyReload == 0) {
    			int pirateIdToDecoy = getClosestPirateToLocation(destinationIsland.location, myPirates).id;
    			game.decoy(game.getMyPirateById(pirateIdToDecoy));
    			this.myPirates.remove(game.getMyPirateById(pirateIdToDecoy));
    			decoyId = pirateIdToDecoy;
	        }
    		myDecoy = game.getMyself().decoy;
		    destination = destinationIsland.location;
		}*/
		
		else if (myPirates.size() > 0)
		{
			Island destinationIsland = null;
			if (game.getNotMyIslands().size() > 0)
			{
				destinationIsland = game.getNotMyIslands().get(0);
			}
			else
			{
				destinationIsland = game.getMyIslands().get(0);
			}
			Pirate decoyPirate = getClosestPirate(destinationIsland, myPirates);
			
	        if (game.getMyself().turnsToDecoyReload == 0) {
    			int pirateIdToDecoy = decoyPirate.id;
    			game.decoy(game.getMyPirateById(pirateIdToDecoy));
    			this.myPirates.remove(game.getMyPirateById(pirateIdToDecoy));
    			decoyId = pirateIdToDecoy;
	        }
    		myDecoy = game.getMyself().decoy;
		    destination = destinationIsland.location;
		}

		myDecoy = game.getMyself().decoy;

		if (myDecoy != null && destination != null) {
			goTo(myDecoy, destination);
		}

		return decoyId;
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
	    if (myAircraft instanceof Drone) { return goToDrone((Drone) myAircraft); }
		Location tempDest = chooseTempLocation((Pirate) myAircraft, finalDest);
		if (tempDest != null) {
			game.setSail(myAircraft, tempDest);
			if (myAircraft instanceof Decoy)
				game.debug("D"+myAircraft.id+" goes to "+ tempDest+" with final "+finalDest);			
			else if (myAircraft instanceof Pirate)
				game.debug("P"+myAircraft.id+" goes to "+ tempDest+" with final "+finalDest);
			return true;
		}
		return false;
	}
	
	/**
	 * Sends the drone to the best city possible
	 * 
	 * @param drone
	 * @return true - if the drone moved
	 */
	private boolean goToDrone(Drone drone) {
		//Location tempDest = chooseTempDestForDrone(drone, getClosestCity(drone, possibleCities).location, 0);
		Location tempDest = chooseTempLocation(drone, getClosestCity(drone, possibleCities).location);
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
	
	private Location chooseTempLocation(Drone myDrone, Location finalDest) {
		List<Location> optionalTempDest = game.getSailOptions(myDrone, finalDest);
		int count = 0;
		Location possibleLocation;
		boolean willGoNearEnemyPirate = false, willGoNearMyDrone = false, willGoNearMyPirate = false;
		while (count < optionalTempDest.size()) {
			possibleLocation = optionalTempDest.get(count);
		    
			willGoNearEnemyPirate = isInAttackRange(possibleLocation,enemyPirates);
            willGoNearMyDrone = isNearDrone(possibleLocation, myDrones);
            willGoNearMyPirate = isInAttackRange(possibleLocation,myPirates);
            
            if (!willGoNearEnemyPirate && willGoNearMyPirate && !willGoNearMyDrone)
            {
                return possibleLocation;
            }
            
            if (!willGoNearEnemyPirate && willGoNearMyPirate)
            {
                return possibleLocation;
            }
            
            if (!willGoNearEnemyPirate)
            {
                return possibleLocation;
            }
            
            count++;
		}
		
		/*Location currentLocation = myDrone.getLocation();
		Location enemy = getClosestPirate(myDrone,enemyPirates).getLocation();
		Location newLoc = null;
		
		if (currentLocation.row < enemy.row && currentLocation.col < enemy.col)
		    newLoc = new Location(currentLocation.row-1, currentLocation.col-1);
		    
		if (currentLocation.row < enemy.row && currentLocation.col > enemy.col)
		    newLoc = new Location(currentLocation.row-1, currentLocation.col+1);
		    
		if (currentLocation.row < enemy.row && currentLocation.col == enemy.col)
		    newLoc = new Location(currentLocation.row-1, currentLocation.col);
		    
		if (currentLocation.row > enemy.row && currentLocation.col < enemy.col)
		    newLoc = new Location(currentLocation.row+1, currentLocation.col-1);
		    
		if (currentLocation.row > enemy.row && currentLocation.col > enemy.col)
		    newLoc = new Location(currentLocation.row+1, currentLocation.col+1);
		    
		if (currentLocation.row > enemy.row && currentLocation.col == enemy.col)
		    newLoc = new Location(currentLocation.row+1, currentLocation.col);
		    
		if (currentLocation.row == enemy.row && currentLocation.col == enemy.col)
		    newLoc = new Location(currentLocation.row+1, currentLocation.col-1);
		
		if (newLoc != null)
		    return chooseRandomTempLocation(myDrone, newLoc);*/
		
		return chooseRandomTempLocation(myDrone, finalDest);
	}
	
	private Location chooseTempLocation(Pirate myPirate, Location finalDest) {
		List<Location> optionalTempDest = game.getSailOptions(myPirate, finalDest);
		int count = 0;
		Location possibleLocation;
		boolean willGoNearEnemyPirate = false, willGoNearNeutralIsland = false, willGoNearEnemyDrone = false,
		        willGoNearMyDrone = false, willGoNearMyPirate = false;
		while (count < optionalTempDest.size()) {
			possibleLocation = optionalTempDest.get(count);
		    
			willGoNearEnemyPirate = isInAttackRange(possibleLocation,enemyPirates);
            willGoNearNeutralIsland = isNearNeutralIsland(possibleLocation,neutralIslands);
            willGoNearEnemyDrone = isNearDrone(possibleLocation, enemyDrones);
            willGoNearMyDrone = isNearDrone(possibleLocation, myDrones);
            willGoNearMyPirate = isInAttackRange(possibleLocation,myPirates);
            
            if (!willGoNearEnemyPirate && willGoNearNeutralIsland && willGoNearEnemyDrone && willGoNearMyDrone && willGoNearMyPirate)
            {
            	return possibleLocation;
            }
            
            if (!willGoNearEnemyPirate && willGoNearNeutralIsland && willGoNearEnemyDrone && willGoNearMyDrone)
            {
            	return possibleLocation;
            }
            
            if (!willGoNearEnemyPirate && willGoNearNeutralIsland && willGoNearEnemyDrone)
            {
            	return possibleLocation;
            }
            
            if (!willGoNearEnemyPirate && willGoNearNeutralIsland)
            {
            	return possibleLocation;
            }
            
            if (!willGoNearEnemyPirate)
            {
            	return possibleLocation;
            }
            
            count++;
		}
		
		return chooseRandomTempLocation(myPirate, finalDest);
	}
	
	/**
	 * Returns if location is in attack range of one of given pirates
	 * 
	 * @param location
	 * @param pirates
	 */
	private boolean isInAttackRange(Location location, List<Pirate> pirates)
	{
	    int multiplier = 1;
	    
	    //if ()
	    
	    for (Pirate p : pirates)
	    {
	        if (p.inRange(location, multiplier*game.getAttackRange())) { return true; }
	    }
	    
	    return false;
	}
	
	/**
	 * Returns if location is in control range of one of given islands
	 * 
	 * @param location
	 * @param islands
	 */
	private boolean isNearNeutralIsland(Location location, List<Island> islands)
	{
	    for (Island island : islands)
	    {
	        if (island.inControlRange(location)) { return true; }
	    }
	    
	    return false;
	}
	
	/**
	 * Returns if location is in range of one of given drones
	 * 
	 * @param location
	 * @param drones
	 */
	private boolean isNearDrone(Location location, List<Drone> drones)
	{
	    for (Drone drone : drones)
	    {
	        if (drone.inRange(location, game.getAttackRange())) { return true; }
	    }
	    
	    return false;
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
			    Aircraft target = null;
    		    for (Aircraft aircraft : attackableEnemies)
    		    {
    		        if (aircraft instanceof Drone)
    		        {
    		            target = aircraft;
    		            break;
    		        }
    		    }
        		if (target == null)
        		{
        		    target = attackableEnemies.get(0);
        		}
        		
        		Island island = getClosestIsland(pirate, game.getAllIslands());
        		
        		if (target instanceof Drone || countPirateHealthWithinAttackRange(pirate.location, myPirates) >= countPirateHealthWithinAttackRange(pirate.location, enemyPirates)
			        || countPirateHealthWithinAttackRange(pirate.location, myPirates) <= countPiratesOnLocation(pirate.location, enemyPirates) || island.inRange(pirate,island.controlRange+2));
        		{
    				attack(pirate, target);
    				piratesToRemove.add(pirate);
        		}
			}
		}
        game.debug("Impulse attack: "+piratesToRemove);
		for (Pirate pirate : piratesToRemove) {
			myPirates.remove(pirate);
			movingPirates[pirate.id].didAttack = true;
		}
	}
	
	private int countPirateHealthWithinAttackRange(Location location, List<Pirate> pirates)
	{
	    int count = 0;
	    
	    for (Pirate p : pirates)
	    {
	        if (p.inAttackRange(location))
	            count += p.currentHealth;
	    }
	    
	    return count;
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
	/*private void handlePiratesToIslands(List<Pirate> myPirates) {
		List<Island> neutralIslands = new ArrayList<Island>(this.neutralIslands);
		List<Pirate> piratesToRemove = new ArrayList<Pirate>();
		for (Pirate pirate : myPirates) {
			// assigns the closest island available in the following priority
			// order:
			// 1.neutral islands
			// 2.enemy islands
			// 3.my islands
			Island bestIsland = null;
			if (!neutralAndEnemyIslands.isEmpty()) {
				City closestCity = getClosestCity(pirate, possibleCities);
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
			}
			else if (!myIslands.isEmpty()) {
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
	}*/
	
	private void handlePiratesToIslands(List<Pirate> myPirates)
	{
	    List<Pirate> piratesToRemove = new ArrayList<Pirate>();
	    List<Island> neutralAndEnemyIslands = new ArrayList<>(this.neutralAndEnemyIslands);
	    game.debug(neutralAndEnemyIslands);
	    //List<Island> neutralAndEnemyIslands = new ArrayList<>(game.getAllIslands());
	    
	    
	    MyIsland[] islands = new MyIsland[neutralAndEnemyIslands.size()];
	    int i = 0;
	    for (Island island : neutralAndEnemyIslands)
	    {
	        islands[i] = new MyIsland(island);
	        i++;
	    }
	    
	    for (Island island : neutralAndEnemyIslands)
	    {
	        Pirate pirate = getClosestPirate(island, myPirates);
	        if (pirate != null)
	        {
	            movingPirates[pirate.id].addDestination(island.location);
    	        piratesToRemove.add(pirate);
    	        for (MyIsland myIsland : islands)
    	        {
    	            if (myIsland.getId() == island.id)
    	            {
    	                myIsland.addPirate();
    	                break;
    	            }
    	        }
	        }
	    }
	    
	    for (Pirate pirate : myPirates)
	    {
	        Island dest = getClosestIsland(pirate, neutralAndEnemyIslands);
	        if (dest != null)
	        {
    	        //goTo(pirate, dest.location);
    	        movingPirates[pirate.id].addDestination(dest.location);
    	        if (!piratesToRemove.contains(pirate))
    	            piratesToRemove.add(pirate);
    	        for (MyIsland myIsland : islands)
    	        {
    	            if (myIsland.getId() == dest.id)
    	            {
    	                myIsland.addPirate();
    	                if (myIsland.isPirateMoreThan(countPiratesOnLocation(myIsland.island.location, enemyPirates)))
    	                {
    	                    neutralAndEnemyIslands.remove(myIsland.island);
    	                }
    	                break;
    	            }
    	        }
	        }
	    }
	    
	    
	    game.debug("To Islands: "+piratesToRemove);
	    for (Pirate pirate : piratesToRemove) {
			myPirates.remove(pirate);
		}
	}
	
	private int countPiratesOnLocation(Location location, List<Pirate> pirates)
	{
	    int count = 0;
	    
	    for (Pirate p : pirates)
	    {
	        if(p.inAttackRange(location)) { count++; }
	    }
	    
	    return count;
	}
	
	private int countPiratesWithinAttackRange(Location location, List<Pirate> pirates)
	{
	    int count = 0;
	    
	    for (Pirate p : pirates)
	    {
	        if(location.inRange(p,game.getAttackRange())) { count++; }
	    }
	    
	    return count;
	}
	
	private int countPiratesHealthOnIsland(Island island, List<Pirate> pirates)
	{
	    int count = 0;
	    
	    for (Pirate p : pirates)
	    {
	        if(island.inControlRange(p)) { count += p.currentHealth; }
	    }
	    
	    return count;
	}

	private void handleRushToDefendPirates(List<Pirate> myPirates) {
		List<Pirate> piratesToRemove = new ArrayList<Pirate>();
		List<Drone> enemyDrones = new ArrayList<>(this.enemyDrones);
		double reqRangeCity = 2 * closeDroneToCityDistance;
    	//double reqRangePirate = 3 * game.getAttackRange();
		game.debug("RANGE TO CITY "+reqRangeCity);
		//game.debug("ATTACK RANGE FROM PIRATE "+reqRangePirate);
		for (Pirate pirate : myPirates) {
		    for(Drone drone : enemyDrones)
    		{
    			City city = getClosestCity(drone, game.getNotMyCities());
    			reqRangeCity = 2 * closeDroneToCityDistance;
    			if(game.getNeutralCities().contains(city))
    			{
    			    reqRangeCity*=4;
    			    //reqRangePirate*=3;
    			}
    			if (city != null && drone.distance(city) <= reqRangeCity && canPirateReachDrone(pirate,drone,city.location)) {
    				rushToDrone(pirate, drone);
    				game.debug("Rush: P"+pirate.id+" D"+drone.id);
    				enemyDrones.remove(drone);
    				piratesToRemove.add(pirate);
    				break;
    			}
    		}
    		if (piratesToRemove.size() == 2) { break; }
		}
		for (Pirate pirate : piratesToRemove) {
			myPirates.remove(pirate);
			movingPirates[pirate.id].didAttack = true;
		}
	}
	
	private boolean canPirateReachDrone(Pirate pirate, Drone drone, Location location)
	{
	    int droneDistance = drone.distance(location);
	    int pirateDistance = pirate.distance(location);
	    
	    return (pirateDistance-game.getAttackRange()) < (droneDistance + game.getUnloadRange());
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
	    game.debug("Unassigned: "+unassignedPirates);
		for (Pirate pirate : unassignedPirates) {
			Drone closestDrone = getClosestDroneToLocation(pirate.location, enemyDrones);
			if (closestDrone != null) {
				movingPirates[pirate.id].addDestination(closestDrone.location);
			}
			else {
			    List<Pirate> allPirates = new ArrayList<>(this.enemyPirates);
			    List<Pirate> myPirates = new ArrayList<>(this.myPirates);
			    myPirates.remove(pirate);
			    allPirates.addAll(myPirates);
			    if (getClosestIsland(getMostValuableCity(possibleCities),game.getAllIslands()) != null)
			        movingPirates[pirate.id].addDestination(getClosestIsland(getMostValuableCity(possibleCities),game.getAllIslands()).getLocation());
			        
			    else if (!getClosestPirate(pirate,myPirates).getLocation().equals(pirate.location))
			        movingPirates[pirate.id].addDestination(getClosestPirate(pirate,myPirates).getLocation());
			        
			    else
			        movingPirates[pirate.id].addDestination(getClosestIsland(pirate,myIslands).getLocation());
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
		
		/*MyIsland[] islands = new MyIsland[myIslands.size()];
	    int i = 0;
	    for (Island island : myIslands)
	    {
	        islands[i] = new MyIsland(island);
	        i++;
	    }
	    
	    List<Drone> myDrones = new ArrayList<>(this.myDrones);
	    List<Drone> droneToRemove = new ArrayList<>();
	    
	    for (Drone drone : myDrones)
	    {
	    	for (MyIsland island : islands)
	    	{
	    		if (island.island.location.equals(drone.location))
	    		{
	    			island.addDrone();
	    			break;
	    		}
	    	}
	    }
	    
	    for (Drone drone : myDrones)
	    {
	    	for (MyIsland island : islands)
	    	{
	    		if (island.island.location.equals(drone.initialLocation) && getClosestPirate(island.island.location, enemyPirates).distance(drone) <= 10)
	    		{
	    			goTo(drone, getClosestCity(drone, possibleCities).getLocation());
	    			droneToRemove.add(drone);
	    			break;
	    		}
	    	}
	    }
	    
	    for (Drone d : droneToRemove) { myDrones.remove(d); }*/
		
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
		double minDistance = (double)this.maxDistance;
		City closestObject = null;
		for (City c : l) {
			if (a.distance(c)*(1.0/c.valueMultiplier) < minDistance || closestObject == null) {
				minDistance = a.distance(c)*(1.0/c.valueMultiplier);
				closestObject = c;
			}
		}
		return closestObject;
	}

	private City getClosestCity(Enemy a, List<City> l) {
		return getClosestCity(a.pirate, l);
	}
	
	private City getClosestCity(Island island, List<City> l) {
		double minDistance = (double)this.maxDistance;
		City closestObject = null;
		for (City c : l) {
			if (island.distance(c)*(1.0/c.valueMultiplier) < minDistance || closestObject == null) {
				minDistance = island.distance(c)*(1.0/c.valueMultiplier);
				closestObject = c;
			}
		}
		return closestObject;
	}
	
	private City getMostValuableCity(List<City> l) {
		int maxValue = 0;
		City valuableCity = null;
		for (City c : l) {
			if (c.valueMultiplier > maxValue || valuableCity == null) {
				maxValue = c.valueMultiplier;
				valuableCity = c;
			}
		}
		return valuableCity;
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
		//if (a instanceof Pirate) { return getBestIsland((Pirate) a, l); }
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
	
	/**
	 * Return island calculated with distance and closest city value
	 */
	private Island getBestIsland(Pirate p, List<Island> l) {
		double minDistance = (double)this.maxDistance;
		WeightIslands w = new WeightIslands();
		for (Island i : l) {
			w.addIsland(i, (double)getClosestCity(i,possibleCities).valueMultiplier*(1.0/p.distance(i)));
		}
		if (w.getMaxWeighted() != null)
			game.debug("Pirate: "+p.id+" goes to island "+w.getMaxWeighted().id);
		return w.getMaxWeighted();
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
	
	private City getClosestCityToDrone(Drone drone, List<City> l) {
		int minDistance = this.maxDistance;
		City closestObject = null;
		for (City city : l) {
			if (city.distance(drone) < minDistance || closestObject == null) {
				minDistance = city.distance(drone);
				closestObject = city;
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
	
	private Location getMiddleLocation(Location loc1, Location loc2)
	{
	    int col = loc1.col + loc2.col;
	    int row = loc1.row + loc2.row;
	    
	    return new Location(row/2, col/2);
	}

	private void rushToDrone(Pirate myPirate, Drone drone) {
	    City droneDestination = getClosestCityToDrone(drone, game.getNotMyCities());
	    List<Aircraft> attackableEnemies = attackableEnemies(myPirate);
	    
	    Aircraft target = null;
		if (!attackableEnemies.isEmpty())
		{
		    for (Aircraft aircraft : attackableEnemies)
		    {
		        if(aircraft instanceof Drone)
		        {
		            target = aircraft;
		            break;
		        }
		    }
		    
		    if (target != null)
    		{
    		    attack(myPirate, target);
    		}
    		
    		else
    		    /*if (!droneDestination.inUnloadRange(myPirate))
			        goTo(myPirate,droneDestination.location);
			    else*/
			        goTo(myPirate,drone.location);
			
		}
		else
			/*if (!droneDestination.inUnloadRange(myPirate))
		        goTo(myPirate,droneDestination.location);
		    else*/
		        goTo(myPirate,drone.location);
	}
}