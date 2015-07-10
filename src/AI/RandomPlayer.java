package AI;

import java.util.ArrayList;
import java.util.Random;

import Cards.CreatureCard;
import Game.Deck;
import Game.Field;
import Game.Graveyard;
import Game.Hand;

public class RandomPlayer extends Player {

	private String name;
	private boolean MCTS = false;

	public RandomPlayer(String name) {
		this.name = "RandomPlayer " + name;
	}

	public boolean isMCTS() {
		return MCTS;
	}
	
	public Player copy() {
		Player newPlayer = new RandomPlayer(this.name);

		return newPlayer;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public ArrayList<CreatureCard> chooseAttackers(ArrayList<CreatureCard> attackers, ArrayList<CreatureCard> blockers) {
		ArrayList<CreatureCard> chosen = new ArrayList<CreatureCard>();
		Random random = new Random();
		for (CreatureCard card : attackers) {
			if (random.nextDouble() > 0.5) {
				chosen.add(card);
			}
		}
		return chosen;
	}

	@Override
	public ArrayList<ArrayList<CreatureCard>> chooseBlockers(ArrayList<CreatureCard> attackers, ArrayList<CreatureCard> potentialBlockers) {
		Random random = new Random();
		ArrayList<ArrayList<CreatureCard>> chosen = new ArrayList<ArrayList<CreatureCard>>();
		for (int i = 0; i < attackers.size(); i++) {
			chosen.add(new ArrayList<CreatureCard>());
		}
		if (potentialBlockers.size() > 0) {
			for (CreatureCard card : potentialBlockers) {
				if (random.nextDouble() > 0.5) {
					chosen.get(random.nextInt(attackers.size())).add(card);
				}
			}
		}
		return chosen;

	}

	@Override
	public int playCreature(ArrayList<CreatureCard> playableCreatures) {
		Random random = new Random();
		if (playableCreatures.size() > 0) {
			return random.nextInt(playableCreatures.size());
		}
		else return -1;
	}

}
