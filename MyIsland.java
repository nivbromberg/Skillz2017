package bots;

import pirates.*;
import java.util.*;

public class MyIsland
{
    public Island island;
    private int id;
    private int numberOfPirates;
    
    public MyIsland(Island island) {
        this.island = island;
        this.id = island.id;
        numberOfPirates = 0;
    }
    
    public void addNumberOfPirates() { numberOfPirates++; }
    public int getId() { return id; }
    
    public boolean isMoreThan(int num) { return numberOfPirates > num; }
}
