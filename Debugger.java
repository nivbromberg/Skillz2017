package bots;

import pirates.PirateGame;

/**
 * @author Omer_Lahav
 * @since 22/02/2017
 *
 */
public class Debugger {
	public enum DebugType
	{
		All,None,Other
	}
	PirateGame game;
	DebugType debugType;
	public Debugger(PirateGame game, DebugType debugType) {
		this.game = game;
		this.debugType = debugType;
	}
	
	@SuppressWarnings("incomplete-switch")
	public void debug(String str,DebugType dType)
	{
		switch(debugType)
		{
		case All:
			game.debug(str);
			break;
		case Other:
			if(dType==DebugType.Other)
				game.debug(str);
			break;
		}
	}
	
}
