package AI;

import java.util.ArrayList;

import Cards.CreatureCard;
import Game.Game;

public abstract class Player {

	
	public abstract String getName();
	public abstract boolean isMCTS();
	public abstract Player copy();
	public abstract ArrayList<CreatureCard> chooseAttackers(ArrayList<CreatureCard> attackers, ArrayList<CreatureCard> blockers);

	public abstract ArrayList<ArrayList<CreatureCard>> chooseBlockers(
			ArrayList<CreatureCard> attackers, ArrayList<CreatureCard> potentialBlockers);
	public abstract int playCreature(ArrayList<CreatureCard> playableCreatures);




	
}
