package AI;

import java.util.ArrayList;
import java.util.Scanner;

import Cards.CreatureCard;
import Game.Deck;
import Game.Field;
import Game.Game;
import Game.Graveyard;
import Game.Hand;

public class MonteCarloPlayer extends Player{

	private String name;
	private boolean MCTS = true;
	

	public MonteCarloPlayer(String name) {
		this.name = name;
		
	}

	public boolean isMCTS() {
		return MCTS;
	}

	public void enterMain() {

	}
/*
	public ArrayList<CreatureCard> chooseAttackers(
			ArrayList<CreatureCard> creatures) {
		ArrayList<CreatureCard> attackers = new ArrayList<CreatureCard>();
		if (creatures.size() > 0) {
			int choices = (int) Math.pow(2, creatures.size() + 1);
			int cnt = 0;
			ArrayList<ArrayList<CreatureCard>> combinations = this
					.combinations(creatures);
			ArrayList<Double> counter = new ArrayList<Double>();

			// Simple version
			
			 * for (int i = 0; i < combinations.size(); i++) { counter.add(0.0);
			 * } while (cnt < choices * 1000) { int result =
			 * this.playOut(combinations.get(cnt % combinations.size()));
			 * counter.set(cnt % combinations.size(), counter.get(cnt %
			 * combinations.size() + result)); }
			 

			// UCB1 version
			int startingValue = 0;
			ArrayList<Integer> visitCounter = new ArrayList<Integer>();
			for (int i = 0; i < combinations.size(); i++) {
				counter.add(startingValue + Math.random() * 0.0002);
				visitCounter.add(0);
			}

			int c = 2;

//			while (cnt < choices * 1000) {
				int bestOption = -100;
				double bestValue = -10000;
				for (int i = 0; i < combinations.size(); i++) {
					if (visitCounter.get(i) > 0) {
						if (counter.get(i)
								/ visitCounter.get(i)
								+ c
								- (Math.sqrt(Math.log(visitCounter.get(i) / cnt))) > bestValue) {
							bestValue = counter.get(i);
							bestOption = i;
						}
					} else {
						if (counter.get(i)
								/ visitCounter.get(i)
								+ c
								- (Math.sqrt(Math.log(visitCounter.get(i) / cnt))) > bestValue) {
							bestValue = counter.get(i);
							bestOption = i;
						}
					}

				}
			}

			int bestCombination = -10;
			double bestScore = -10000;
			for (int i = 0; i < combinations.size(); i++) {
				if (counter.get(i) > bestScore) {
					bestCombination = i;
					bestScore = counter.get(i);
				}
			}
			attackers = combinations.get(bestCombination);
		}
		return attackers;
	}*/

	private int playOut(ArrayList<CreatureCard> arrayList) {
		RandomPlayer player1 = new RandomPlayer(this.name);
		RandomPlayer player2 = new RandomPlayer("Opponent");

		Game newGame = new Game(player1, player2, 1);
		newGame.setPlayer1(this.copy());

		return 0;
	}

	public ArrayList<ArrayList<CreatureCard>> chooseBlockers(
			ArrayList<CreatureCard> attackers) {
		return null;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String newName) {
		this.name = newName;
	}


	public MonteCarloPlayer copy() {
		MonteCarloPlayer newPlayer = new MonteCarloPlayer(this.name);
		
		return newPlayer;
	}

	@Override
	public ArrayList<CreatureCard> chooseAttackers(ArrayList<CreatureCard> attackers, ArrayList<CreatureCard> blockers) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<ArrayList<CreatureCard>> chooseBlockers(ArrayList<CreatureCard> attackers, ArrayList<CreatureCard> potentialBlockers) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int playCreature(ArrayList<CreatureCard> playableCreatures) {
		// TODO Auto-generated method stub
		return 0;
	}

}
