package bots;
import pirates.*;
import java.util.*;

public class MyBot implements PirateBot {
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

    private List<StaticEnemy> staticEnemies;
    
    private boolean stayThere = false;
    private int pirateToStay;
    private Location whereToStay;
    
    @Override
    public void doTurn(PirateGame game) {
        init(game);
        
        if (stayThere)
        {
            //pirateToStay = getClosestPirateToLocation(whereToStay,myPirates).id;
            game.debug("#1 pirateToStay="+pirateToStay+", whereToStay="+whereToStay);
            goTo(game.getMyPirateById(pirateToStay), whereToStay);
            myPirates.remove(game.getMyPirateById(pirateToStay));
        }
        checkSusp();
        
        handlePirates();
        handleDrones();
    }
    
    /**
	 * Checks if any enemy pirate is stationary (i.e. 3 or more turns in the same location)
	 * @author Niv Bromberg
	 */
    private void checkSusp()
    {
        if (myCities.size() > 0)
        {
        	updateEnemyLocations();
            
            for (StaticEnemy enemyPirate : staticEnemies)
            {
                if (enemyPirate.turnsThatStatic >= 3 && getClosestPirateToLocation(myCities.get(0).getLocation(),enemyPirates).equals(enemyPirate.pirate) && !stayThere)
                {
                    Pirate p = getClosestPirate(enemyPirate.pirate,myPirates);
                    if (p != null)
                    {
                        myPirates.remove(p);
                        stayThere = true;
                        pirateToStay = p.id;
                        whereToStay = enemyPirate.pirate.getLocation();
                        game.debug("#2 Pirate "+p.id+" goes to stay where enemy "+enemyPirate.pirate.id);
                        goTo(p, enemyPirate.pirate.getLocation());
                    }
                }
            
                else if (enemyPirate.turnsThatStatic >= 3)
                {
                    Pirate p = getClosestPirate(enemyPirate.pirate,myPirates);
                    if (p != null)
                    {
                        myPirates.remove(p);
                        game.debug("#3 Pirate "+p.id+" goes to attack static enemy "+enemyPirate.pirate.id);
                        goTo(p, enemyPirate.pirate.getLocation());
                    }
                }
            }
        }
    }
    
    /**
	 * In the first turn it initializes an arrayList to all enemy pirates
	 * In every other turn it updates the list
	 */
    private void updateEnemyLocations()
    {
        if (game.getTurn() == 1)
        {
            staticEnemies = new ArrayList<StaticEnemy>();
        	
            for (Pirate enemyPirate : game.getAllEnemyPirates())
            {
                staticEnemies.add(new StaticEnemy(enemyPirate));
            }
        }
        else
        {
            for (StaticEnemy staticPirate : staticEnemies)
            {
                for (Pirate enemyPirate : game.getAllEnemyPirates())
                {
                    if (staticPirate.pirate.id == enemyPirate.id)
                    {
                        staticPirate.update(enemyPirate);
                    }
                }
            }
        }
    }
    
    private void init(PirateGame game)
    {
        this.game = game;
        enemyPirates = game.getEnemyLivingPirates();
        enemyDrones = game.getEnemyLivingDrones();
        myPirates = game.getMyLivingPirates();
        neutralIslands = game.getNeutralIslands();
        enemyIslands = game.getEnemyIslands();
        myIslands = game.getMyIslands();
        myDrones = game.getMyLivingDrones();
        myCities = game.getMyCities();
        minDistance=((int)Math.sqrt(Math.pow(game.getRowCount(),2)+Math.pow(game.getColCount(),2)))+1;
    }
    
    private boolean checkAttack(Pirate p)
    {
        for (Pirate ep : enemyPirates)
        {
            if (p.inAttackRange(ep))
            {
                game.attack(p,ep);
                return true;
            }
        }
        
        for (Drone ed : enemyDrones)
        {
            if (p.inAttackRange(ed))
            {
                game.attack(p,ed);
                return true;
            }
        }
        return false;
    }
    
    private boolean goTo(Aircraft a, Location loc)
    {
        if (a instanceof Pirate && checkAttack((Pirate)a))
        {
            return true;
        }
        
        if(!a.getLocation().equals(loc))
        {
            List<Location> list = game.getSailOptions(a,loc);
            if(!a.getLocation().equals(list.get(0)))
            {
                game.setSail(a, list.get(0));
                if (a instanceof Pirate)
                {
                    game.debug("#4 Pirate "+a+" goes to location:"+list.get(0));
                }
                return true;
            }
        }
        return false;
    }
    
    private boolean goTo(Pirate p, Island i){
        if (checkAttack(p))
        {
            return true;
        }
        
        if(!i.inControlRange(p))
        {
            List<Location> list = game.getSailOptions(p,i.getLocation());
            for(Location loc:list){
                if(!p.getLocation().equals(loc))
                {
                    game.setSail(p, loc);
                    //game.debug("pirate:"+a+", goes to location:"+loc);
                    return true;
                }
            }
            return false;
        }
        else
        {
            return true;
        }
        
    }
    
    private void handlePirates(){
        List<Island> neutralIslands=new ArrayList<Island>(this.neutralIslands);
        List<Pirate> piratesDidNotMove = new ArrayList<Pirate>();
        List<Pirate> myPirates=new ArrayList<Pirate>(this.myPirates);
        Pirate closestEnemyToCity = null;
        int minEnDisToCity = minDistance;
        
        /*for (Pirate ep : enemyPirates)
        {
            if (ep.distance(myCities.get(0)) <= 8 && minEnDisToCity > ep.distance(myCities.get(0)))
            {
                minEnDisToCity = ep.distance(myCities.get(0));
                closestEnemyToCity = ep;
            }
        }
        
        if(closestEnemyToCity != null)
        {
            Pirate p = getClosestPirate(closestEnemyToCity,myPirates);
            game.debug("L197 Pirate "+p+" goes to attack enemy "+closestEnemyToCity);
            goTo(p,closestEnemyToCity.getLocation());
            myPirates.remove(p);
        }*/
        
        for (Pirate p : myPirates)
        {
            if(neutralIslands.size()!=0){
                Island closestIsland=getClosestIsland(p,neutralIslands);
                if (closestIsland != null)
                {
                    goTo(p, closestIsland);
                    neutralIslands.remove(closestIsland);
                }
            }
            else if(enemyIslands.size()!=0){
                Island closestIsland=getClosestIsland(p,enemyIslands);
                if (closestIsland != null)
                {
                    goTo(p, closestIsland);
                }
            }
            else if(myIslands.size()!=0) {
                Island closestIsland=getClosestIsland(p,myIslands);
                if (closestIsland != null)
                {
                    goTo(p, closestIsland);
                }
            }
            else
                piratesDidNotMove.add(p);
        }
        neutralIslands = game.getNeutralIslands();
        for (Pirate pirate : piratesDidNotMove)
        {
            Island closestIsland = getClosestIsland(pirate,neutralIslands);
            if (closestIsland != null)
            {
                goTo(pirate, closestIsland);
            }
        }
    }
    
    private void handleDrones(){
        //game.debug("num of drones:"+myDrones.size());
        
        for (Drone d : myDrones)
        {
            goTo(d, getClosestCity(d,myCities).getLocation());
        }
    }
    
    private City getClosestCity(Aircraft a, List<City> l){
        int minDistance = this.minDistance;
        City closestObject=null;
        for(City c:l){
            if(a.distance(c)<minDistance || closestObject==null)
            {
                minDistance=a.distance(c);
                closestObject=c;
            }
        }
        return closestObject;
    }
    
    private Island getClosestIsland(Aircraft a, List<Island> l){
        int minDistance = this.minDistance;
        Island closestObject=null;
        for(Island i:l){
            if(a.distance(i)<minDistance || closestObject==null)
            {
                minDistance=a.distance(i);
                closestObject=i;
            }
        }
        return closestObject;
    }
    
    private Pirate getClosestPirate(Aircraft a, List<Pirate> l){
        int minDistance = this.minDistance;
        Pirate closestObject=null;
        for(Pirate p : l){
            if(p.distance(a)<minDistance || closestObject==null)
            {
                minDistance = p.distance(a);
                closestObject=p;
            }
        }
        return closestObject;
    }
    
    private Pirate getClosestPirateToLocation(Location loc, List<Pirate> l)
    {
        int minDistance = this.minDistance;
        Pirate closestObject=null;
        for(Pirate p : l){
            if(p.distance(loc)<minDistance || closestObject==null)
            {
                minDistance = p.distance(loc);
                closestObject=p;
            }
        }
        return closestObject;
    }
}
