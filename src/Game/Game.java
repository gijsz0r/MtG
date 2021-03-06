package Game;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import AI.MonteCarloPlayer;
import AI.Node;
import AI.Player;
import AI.RandomPlayer;
import Cards.Card;
import Cards.CreatureCard;
import Cards.LandCard;

public class Game {

	public static final int P_ATTACK = 1, P_BLOCK = 2, P_PLAY = 3;

	private Player player1;
	private Player player2;
	private int startingPlayer;
	private int activePlayer;
	private Field field1;
	private Field field2;
	private Graveyard graveyard1;
	private Graveyard graveyard2;
	private Deck deck1;
	private Deck deck2;
	private Hand hand1;
	private Hand hand2;
	private int life1;
	private int life2;
	private Random random;
	private ArrayList<Node> tree;
	private ArrayList<CreatureCard> attackers;
	private int initial_UCT;
	private double c_constant;

	public Game(Player player1, Player player2, int startingPlayer, int initial_UCT, double c_constant) {
		this.player1 = player1;
		this.player2 = player2;
		field1 = new Field();
		field2 = new Field();
		graveyard1 = new Graveyard();
		graveyard2 = new Graveyard();
		deck1 = new Deck();
		deck2 = new Deck();
		hand1 = new Hand();
		hand2 = new Hand();
		life1 = 20;
		life2 = 20;
		tree = new ArrayList<Node>();

		this.initial_UCT = initial_UCT;
		this.c_constant = c_constant;
		random = new Random();

		this.startingPlayer = startingPlayer;
		this.activePlayer = startingPlayer;
		for (int i = 0; i < 6; i++) {
			this.hand1.addCard(this.deck1.draw());
			this.hand2.addCard(this.deck2.draw());
		}

		this.hand2.addCard(this.deck2.draw());

	}

	public int run() {

		while (!this.isOver()) {
			if (activePlayer == 1 && !this.isOver()) {
				this.turn(this.player1);
				this.activePlayer = 2;
			} else if (activePlayer == 2 && !this.isOver()) {
				this.turn(this.player2);
				this.activePlayer = 1;
			}
		}

		// System.out.println("Game over! The winner is player " +
		// this.getWinner());

		return (this.getWinnerNumber());

	}

	private void draw() {
		boolean deckEmpty = false;
		if (activePlayer == 1) {
			if (deck1.getSize() > 0) {
				this.hand1.addCard(this.deck1.draw());
			} else {
				deckEmpty = true;
			}
		} else if (activePlayer == 2) {
			if (deck2.getSize() > 0) {
				this.hand2.addCard(this.deck2.draw());
			} else {
				deckEmpty = true;
			}
		}

		if (deckEmpty == true) {
			System.out.println("Wee wo wee wo wee wo");
		}
	}

	/*
	 * private ArrayList<SimpleEntry<Integer, Integer>>
	 * createCombinations(ArrayList<Integer> blockers, ArrayList<Integer>
	 * attackers){ ArrayList<SimpleEntry<Integer, Integer>> links = new
	 * ArrayList<SimpleEntry<Integer, Integer>>();
	 * 
	 * for (int i = 0; i < blockers.size(); i++) { int blockerID =
	 * blockers.get(i);
	 * 
	 * for (int j = 0; j < attackers.size(); j++) { int attackerID =
	 * attackers.get(j); links.add(new SimpleEntry<Integer, Integer>(blockerID,
	 * attackerID)); } }
	 * 
	 * 
	 * }
	 */

	private void turn(Player player) {
		// System.out.println(player.getName() + " starts their turn!");

		int playerID = player == player1 ? 0 : 1;
		int enemyID = 1 - playerID;

		// +1, see method documentation
		Field field = getField(playerID + 1);
		Field enemyField = getField(enemyID + 1);

		Player enemyPlayer = getPlayerByID(enemyID);

		field.untapAllPermanents();
		this.draw();

		this.attackers = new ArrayList<CreatureCard>();
		ArrayList<ArrayList<CreatureCard>> blockers = new ArrayList<ArrayList<CreatureCard>>();

		// String that builds up a board state based on the creatures on the
		// field. Uncomment the printout below to see output in console
		String boardState = "";
		for (int i = 0; i < field.getCreatures().size(); i++) {
			boardState += (field.getCreatures().get(i).getName() + " ");
		}
		// System.out.println("Player 1: Current board: " + boardState);
		if (field.getCreatures().size() > 0) {
			if (player.isMCTS()) {
				attackers = this.integerToCreature(this.MCTSChoose(1, this), field.getCreatures());
				// attackers =
				// this.MonteCarloChooseAttackers(field.getCreatures(),
				// enemyField.getCreatures());
				// System.out.println("Hi!");
			} else {
				attackers = player.chooseAttackers(field.getCreatures(), enemyField.getCreatures());

			}
		}
		if (attackers.size() > 0) {
			if (enemyPlayer.isMCTS()) {
				// blockers =
				// singleToDoubleArray(integerToCreature(this.MCTSChoose(2,
				// this), enemyField.getCreatures()),
				// field.getCreatures().size());
				// blockers = this.MonteCarloChooseBlockers(attackers,
				// enemyField.getCreatures());
			} else {
				blockers = enemyPlayer.chooseBlockers(attackers, enemyField.getCreatures());
			}
		}

		this.resolve(attackers, blockers);

		this.enterMain();

	}

	/**
	 * Gets player by zero based ID.
	 * 
	 * @param id
	 * @return
	 */
	public Player getPlayerByID(int id) {
		return id == 0 ? player1 : player2;
	}

	public ArrayList<CreatureCard> findPlayableCreatures(ArrayList<Card> possibleCards, int mana) {
		ArrayList<CreatureCard> playableCreatures = new ArrayList<CreatureCard>();

		for (int i = 0; i < possibleCards.size(); i++) {
			if (possibleCards.get(i).getManaCost() <= mana && possibleCards.get(i).getType() == 1) {
				playableCreatures.add((CreatureCard) possibleCards.get(i));
			}

		}

		return playableCreatures;
	}

	private void enterMain() {
		if (!this.isOver()) {
			if (this.activePlayer == 1) {
				if (this.hand1.containsLand()) {
					// System.out.println("Playing land! :)");
					this.field1.playLand(this.hand1.playLand());
				}
				// if (player1.isMCTS()) {
				// ArrayList<CreatureCard> creaturesToPlay =
				// integerToCreature(this.MCTSChoose(3, this),
				// this.hand1.getCreatures());
				// for (int i = 0; i < creaturesToPlay.size(); i++) {
				// // System.out.println("Creatures: " +
				// // creaturesToPlay.size() + " current Creature: "
				// // + creaturesToPlay.get(i).getName() + " mana: " +
				// // field1.getUntappedLands());
				// field1.tapLands(creaturesToPlay.get(i).getManaCost());
				// field1.playCreature(creaturesToPlay.get(i));
				// hand1.removeCard(creaturesToPlay.get(i));
				// }
				//
				// } else {
				while (true) {
					ArrayList<CreatureCard> playableCreatures = this.findPlayableCreatures(hand1.getCardsInHand(),
							field1.getUntappedLands());
					if (playableCreatures.size() == 0) {
						break;
					}
					int creatureToPlay = -1;
					if (player1.isMCTS()) {

						for (int i = 0; i < playableCreatures.size(); i++) {
							// System.out.println(playableCreatures.get(i).getName());
						}
						creatureToPlay = MonteCarloPlayCreature(playableCreatures);
					} else {
						creatureToPlay = player1.playCreature(playableCreatures);
					}
					if (creatureToPlay == -1) {
						break;
					} else {
						CreatureCard creature = playableCreatures.get(creatureToPlay);
						field1.tapLands(creature.getManaCost());
						hand1.removeCard(creature);
						field1.playCreature(creature);
					}

				}
				// }
			} else if (this.activePlayer == 2) {
				if (this.hand2.containsLand()) {
					// System.out.println("Playing land! :)");
					this.field2.playLand(this.hand2.playLand());
				}
				// if (player2.isMCTS()) {
				// ArrayList<CreatureCard> creaturesToPlay =
				// integerToCreature(this.MCTSChoose(3, this),
				// this.hand2.getCreatures());
				// for (int i = 0; i < creaturesToPlay.size(); i++) {
				// // System.out.println("Creatures: " +
				// // creaturesToPlay.size() + " current Creature: "
				// // + creaturesToPlay.get(i).getName() + " mana: " +
				// // field2.getUntappedLands());
				// field2.tapLands(creaturesToPlay.get(i).getManaCost());
				// field2.playCreature(creaturesToPlay.get(i));
				// hand2.removeCard(creaturesToPlay.get(i));
				// }
				// } else {
				while (true) {
					ArrayList<CreatureCard> playableCreatures = this.findPlayableCreatures(hand2.getCardsInHand(),
							field2.getUntappedLands());

					if (playableCreatures.size() == 0) {
						break;
					}
					int creatureToPlay = -1;
					if (player1.isMCTS()) {
						creatureToPlay = MonteCarloPlayCreature(playableCreatures);
					} else {
						creatureToPlay = player2.playCreature(playableCreatures);
					}
					if (creatureToPlay == -1) {
						break;
					} else {
						CreatureCard creature = playableCreatures.get(creatureToPlay);
						field2.tapLands(creature.getManaCost());
						hand2.removeCard(creature);
						field2.playCreature(creature);

					}
				}
			}
		}

	}
	// }

	private void resolve(ArrayList<CreatureCard> attackers, ArrayList<ArrayList<CreatureCard>> blockers) {

		if (attackers != null) {
			for (int i = 0; i < attackers.size(); i++) {
				// if creature isn't blocked: deal damage to player
				if (blockers.isEmpty()) {
					if (this.activePlayer == 1) {
						life2 -= (attackers.get(i).getPower());
					} else if (this.activePlayer == 2) {
						life1 -= (attackers.get(i).getPower());
					}
				} else if (blockers.get(i).isEmpty()) {
					if (this.activePlayer == 1) {
						life2 -= (attackers.get(i).getPower());
					} else if (this.activePlayer == 2) {
						life1 -= (attackers.get(i).getPower());
					}
					// if it is blocked, check for power/toughness:
				} else {
					if (blockers.get(i).size() == 1) {
						if (attackers.get(i).getPower() >= blockers.get(i).get(0).getPower()) {
							// destroy blocker
							if (this.activePlayer == 1) {
								this.graveyard2.add(this.field2.destroyCreature(blockers.get(i).get(0)));
							}
							if (this.activePlayer == 2) {
								this.graveyard1.add(this.field1.destroyCreature(blockers.get(i).get(0)));
							}
						}
						if (blockers.get(i).get(0).getPower() >= attackers.get(i).getPower()) {
							// destroy attacker
							if (this.activePlayer == 1) {
								this.graveyard1.add(this.field1.destroyCreature(attackers.get(i)));
							}
							if (this.activePlayer == 2) {
								this.graveyard2.add(this.field2.destroyCreature(attackers.get(i)));
							}
						}

					} else {
						int totalPower = 0;
						int totalToughness = 0;

						for (int j = 0; j < blockers.get(i).size(); j++) {
							totalPower += blockers.get(i).get(j).getPower();
							totalToughness += blockers.get(i).get(j).getToughness();
						}
						if (attackers.get(i).getPower() >= totalToughness) {
							// destroy all blockers
							for (int j = 0; j < blockers.get(i).size(); j++) {
								if (this.activePlayer == 1) {
									this.field2.destroyCreature(blockers.get(i).get(j));
								}
								if (this.activePlayer == 2) {
									this.field1.destroyCreature(blockers.get(i).get(j));
								}
							}
						} else {
							// destroy some blockers
							int powerLeft = attackers.get(i).getPower();
							for (int j = 0; j < blockers.get(i).size(); j++) {
								if (powerLeft > blockers.get(i).get(j).getToughness()) {
									powerLeft -= blockers.get(i).get(j).getToughness();
									if (this.activePlayer == 1) {
										this.graveyard2.add(this.field2.destroyCreature(blockers.get(i).get(j)));
									}
									if (this.activePlayer == 2) {
										this.graveyard1.add(this.field1.destroyCreature(blockers.get(i).get(j)));
									}

								}
							}
						}
						if (attackers.get(i).getToughness() <= totalPower) {
							// destroy attacker
							if (this.activePlayer == 1) {
								this.graveyard1.add(this.field1.destroyCreature(attackers.get(i)));
							}
							if (this.activePlayer == 2) {
								this.graveyard2.add(this.field2.destroyCreature(attackers.get(i)));
							}
						}

					}
				}
			}
		}
		// System.out.println("Combat resolved! Player life totals: " +
		// this.player1.getName() + ": " + this.life1 + ". " +
		// this.player2.getName() + ": " + this.life2 + ".");
	}

	private Boolean isOver() {
		Boolean over = false;
		if (this.life1 < 1) {
			over = true;
		}
		if (this.life2 < 1) {
			over = true;
		}
		if (this.deck1.getSize() < 1) {
			over = true;
		}
		if (this.deck2.getSize() < 1) {
			over = true;
		}
		return over;
	}

	public Game copy() {
		Game newGame = new Game(this.player1.copy(), this.player2.copy(), this.startingPlayer, this.initial_UCT,
				this.c_constant);
		newGame.setActivePlayer(this.activePlayer);
		newGame.setField1(this.field1.copy());
		newGame.setField2(this.field2.copy());
		newGame.setGraveyard1(this.graveyard1.copy());
		newGame.setGraveyard2(this.graveyard2.copy());
		newGame.setDeck1(this.deck1.copy());
		newGame.setDeck2(this.deck2.copy());
		newGame.setHand1(this.hand1.copy());
		newGame.setHand2(this.hand2.copy());
		newGame.setLife1(this.life1);
		newGame.setLife2(this.life2);

		return newGame;
	}

	private void setHand2(Hand copy) {
		this.hand2 = copy;
	}

	private void setHand1(Hand copy) {
		this.hand1 = copy;
	}

	private void setLife2(int life22) {
		this.life2 = life22;

	}

	private void setLife1(int life12) {
		this.life1 = life12;
	}

	private void setDeck2(Deck copy) {
		this.deck2 = copy;
	}

	private void setDeck1(Deck copy) {
		this.deck1 = copy;
	}

	private void setGraveyard2(Graveyard copy) {
		this.graveyard2 = copy;
	}

	private void setGraveyard1(Graveyard copy) {
		this.graveyard1 = copy;
	}

	private void setField2(Field copy) {
		this.field2 = copy;
	}

	private void setField1(Field copy) {
		this.field1 = copy;
	}

	private void setActivePlayer(int activePlayer2) {
		this.activePlayer = activePlayer2;
	}

	public int getActivePlayer() {
		return this.activePlayer;
	}

	public void setPlayer1(Player copy) {
		this.player1 = copy;
	}

	public void setPlayer2(Player copy) {
		this.player2 = copy;
	}

	public int getWinnerNumber() {
		int winner = 0;
		if (!(life1 > 0) || !(deck1.getSize() > 0)) {
			winner = 2;
		} else if (!(life2 > 0) || !(deck2.getSize() > 0)) {
			winner = 1;
		}
		return winner;
	}

	// 1-based!
	public int getWinner(int playerToEvaluate) {
		int winner = 0;
		if (!(life1 > 0) || !(deck1.getSize() > 0)) {
			winner = 2;
		} else if (!(life2 > 0) || !(deck2.getSize() > 0)) {
			winner = 1;
		}
		if (playerToEvaluate == winner) {
			return 1;
		} else
			return -1;
	}

	public Player getPlayer1() {
		return player1;
	}

	public Player getPlayer2() {
		return player2;
	}

	public int MonteCarloPlayCreature(ArrayList<CreatureCard> possibleCreatures) {
		// System.out.println("MCTS is playing a creature!");
		if (possibleCreatures.size() > 0) {
			int amountOfOptions = possibleCreatures.size();
			double c = Math.sqrt(2);
			int totalVisitCounter = 0;
			ArrayList<ArrayList<Integer>> scores = new ArrayList<ArrayList<Integer>>();
			for (int i = 0; i < amountOfOptions; i++) {
				scores.add(new ArrayList<Integer>());
			}
			while (totalVisitCounter < 1000 * amountOfOptions) {

				double tmpLog = Math.log(totalVisitCounter);
				Game newGame = this.copy();
				newGame.setPlayer1(new RandomPlayer(this.player1.getName()));
				newGame.setPlayer2(new RandomPlayer(this.player2.getName()));

				double[] ucbValues = new double[amountOfOptions];

				for (int i = 0; i < amountOfOptions; i++) {

					if (scores.get(i).size() > 0) {
						double tmp = 0;
						for (int j = 0; j < scores.get(i).size(); j++) {
							tmp += scores.get(i).get(j);
						}
						double avg = tmp / scores.get(i).size();
						ucbValues[i] = avg + c * Math.sqrt((tmpLog / scores.get(i).size()));
					} else {
						ucbValues[i] = random.nextDouble() * 0.000001 + 1;
					}
				}
				int bestOption = 0;
				double bestScore = Integer.MIN_VALUE;
				for (int i = 0; i < amountOfOptions; i++) {
					if (ucbValues[i] > bestScore) {
						bestScore = ucbValues[i];
						bestOption = i;
					}
				}
				if (activePlayer == 1) {
					// System.out.println(possibleCreatures.get(bestOption).getName());
					newGame.field1.playCreature(possibleCreatures.get(bestOption));
					newGame.setActivePlayer(2);
					newGame.run();
					if (newGame.getWinnerNumber() == 1) {
						scores.get(bestOption).add(1);
					}
					if (newGame.getWinnerNumber() == 2) {
						scores.get(bestOption).add(-1);
					}
					if (newGame.getWinnerNumber() == 0) {
						scores.get(bestOption).add(0);
					}
				}
				if (activePlayer == 2) {
					newGame.field2.playCreature(possibleCreatures.get(bestOption));
					newGame.setActivePlayer(1);
					newGame.run();
					if (newGame.getWinnerNumber() == 2) {
						scores.get(bestOption).add(1);
					}
					if (newGame.getWinnerNumber() == 1) {
						scores.get(bestOption).add(-1);
					}
					if (newGame.getWinnerNumber() == 0) {
						scores.get(bestOption).add(0);
					}
				}
				totalVisitCounter++;
			}
			double[] ucbValues = new double[amountOfOptions];

			for (int i = 0; i < amountOfOptions; i++) {

				if (scores.get(i).size() > 0) {
					double tmp = 0;
					for (int j = 0; j < scores.get(i).size(); j++) {
						tmp += scores.get(i).get(j);
					}
					double avg = tmp / scores.get(i).size();
					ucbValues[i] = avg + c * Math.sqrt((Math.log(totalVisitCounter) / scores.get(i).size()));
				} else {
					ucbValues[i] = 100 + random.nextDouble() * 0.000001;
				}
			}
			int bestOption = 0;
			double bestScore = Integer.MIN_VALUE;
			for (int i = 0; i < amountOfOptions; i++) {
				if (ucbValues[i] > bestScore) {
					bestScore = ucbValues[i];
					bestOption = i;
				}
			}
			// System.out.println("MCTS found a creature to play.");
			return bestOption;
		}

		else
			// System.out.println("MCTS couldn't find a creature to play.");
			return -1;

	}

	public int DeterminizationplayCreature(ArrayList<CreatureCard> possibleCreatures) {
		System.out.println("Determinized MCTS is playing a creature!");
		if (possibleCreatures.size() > 0) {
			int amountOfOptions = possibleCreatures.size();
			double c = Math.sqrt(2);
			int totalVisitCounter = 0;
			ArrayList<ArrayList<ArrayList<Integer>>> scores = new ArrayList<ArrayList<ArrayList<Integer>>>();
			for (int i = 0; i < 10; i++) {
				scores.add(new ArrayList<ArrayList<Integer>>());
				for (int j = 0; j < amountOfOptions; j++) {
					scores.get(i).add(new ArrayList<Integer>());
				}
			}

			while (totalVisitCounter < 1000 * amountOfOptions) {

				double tmpLog = Math.log(totalVisitCounter);
				int currentIteration = totalVisitCounter % 10;
				Game newGame = this.copy();
				newGame.setPlayer1(new RandomPlayer(this.player1.getName()));
				newGame.setPlayer2(new RandomPlayer(this.player2.getName()));

				double[] ucbValues = new double[amountOfOptions];

				for (int i = 0; i < amountOfOptions; i++) {

					if (scores.get(i).size() > 0) {
						double tmp = 0;
						for (int j = 0; j < scores.get(currentIteration).get(i).size(); j++) {
							tmp += scores.get(currentIteration).get(i).get(j);
						}
						double avg = tmp / scores.get(currentIteration).get(i).size();
						ucbValues[i] = avg + c * Math.sqrt((tmpLog / scores.get(currentIteration).get(i).size()));
					} else {
						ucbValues[i] = random.nextDouble() * 0.000001 + 1;
					}
				}
				int bestOption = 0;
				double bestScore = Integer.MIN_VALUE;
				for (int i = 0; i < amountOfOptions; i++) {
					if (ucbValues[i] > bestScore) {
						bestScore = ucbValues[i];
						bestOption = i;
					}
				}
				if (activePlayer == 1) {
					Deck opponentDeck = this.getOpponentDeck(2);
					ArrayList<ArrayList<Card>> determinizations = new ArrayList<ArrayList<Card>>();
					determinizations = this.getDeterminizations(opponentDeck, hand2.size());
					newGame.field1.playCreature(possibleCreatures.get(bestOption));
					newGame.hand2.set(determinizations.get(currentIteration));
					newGame.setActivePlayer(2);
					newGame.run();
					if (newGame.getWinnerNumber() == 1) {
						scores.get(currentIteration).get(bestOption).add(1);
					}
					if (newGame.getWinnerNumber() == 2) {
						scores.get(currentIteration).get(bestOption).add(-1);
					}
					if (newGame.getWinnerNumber() == 0) {
						scores.get(currentIteration).get(bestOption).add(0);
					}
				}
				if (activePlayer == 2) {
					Deck opponentDeck = this.getOpponentDeck(1);
					ArrayList<ArrayList<Card>> determinizations = this.getDeterminizations(opponentDeck, hand1.size());
					newGame.field2.playCreature(possibleCreatures.get(bestOption));
					newGame.hand1.set(determinizations.get(currentIteration));
					newGame.setActivePlayer(1);
					newGame.run();
					if (newGame.getWinnerNumber() == 2) {
						scores.get(currentIteration).get(bestOption).add(1);
					}
					if (newGame.getWinnerNumber() == 1) {
						scores.get(currentIteration).get(bestOption).add(-1);
					}
					if (newGame.getWinnerNumber() == 0) {
						scores.get(currentIteration).get(bestOption).add(0);
					}
				}
				totalVisitCounter++;
			}
			double[][] ucbValues = new double[10][amountOfOptions];

			for (int i = 0; i < amountOfOptions; i++) {

				for (int j = 0; j < 10; j++) {

					if (scores.get(j).get(i).size() > 0) {
						double tmp = 0;
						for (int k = 0; k < scores.get(j).get(i).size(); k++) {
							tmp += scores.get(j).get(i).get(k);
						}
						double avg = tmp / scores.get(j).get(i).size();
						ucbValues[j][i] = avg
								+ c * Math.sqrt((Math.log(totalVisitCounter) / scores.get(j).get(i).size()));
					} else {
						ucbValues[j][i] = 100 + random.nextDouble() * 0.000001;
					}
				}
			}
			Integer[] bestOption = new Integer[10];
			double[] bestScore = new double[10];
			for (int i = 0; i < 10; i++) {
				bestScore[i] = Integer.MIN_VALUE;
			}
			for (int i = 0; i < amountOfOptions; i++) {
				for (int j = 0; j < 10; j++) {
					if (ucbValues[j][i] > bestScore[j]) {
						bestScore[j] = ucbValues[j][i];
						bestOption[j] = i;
					}
				}
			}
			Arrays.sort(bestOption);
			int previous = bestOption[0];
			int popular = bestOption[0];
			int count = 1;
			int maxCount = 1;

			for (int i = 1; i < 10; i++) {
				if (bestOption[i] == previous)
					count++;
				else {
					if (count > maxCount) {
						popular = bestOption[i - 1];
						maxCount = count;
					}
					previous = bestOption[i];
					count = 1;
				}
			}
			System.out.println("Found one!");
			return count > maxCount ? bestOption[9] : popular;
		}

		else
			System.out.println("Didn't find one..");
		return -1;

	}

	private ArrayList<ArrayList<Card>> getDeterminizations(Deck opponentDeck, int size) {
		ArrayList<ArrayList<Card>> determinizations = new ArrayList<ArrayList<Card>>();
		for (int i = 0; i < 10; i++) {
			Deck tmpDeck = opponentDeck.copy();
			determinizations.add(new ArrayList<Card>());
			for (int j = 0; j < size; j++) {
				determinizations.get(i).add(tmpDeck.remove(random.nextInt(tmpDeck.getSize())));
			}
		}

		return null;
	}

	public ArrayList<ArrayList<CreatureCard>> MonteCarloChooseBlockers(ArrayList<CreatureCard> attackers,
			ArrayList<CreatureCard> possibleBlockers) {
		// System.out.println("Attackers for which we choose blockers : " +
		// attackers.size() + " and blockers : " + possibleBlockers.size());
		// for (CreatureCard what : possibleBlockers)
		// System.out.println(what.getName());
		// System.out.println("MCTS is choosing blockers!");
		int size = possibleBlockers.size();
		Generator<Integer> combinatorics = this.newCombinations(size);
		int tmpcnt = 0;
		ArrayList<ArrayList<CreatureCard>> blockSets = new ArrayList<ArrayList<CreatureCard>>();

		int tmpSize = combinatorics.getOriginalVector().getSize();
		for (ICombinatoricsVector<Integer> subSet : combinatorics) {
			ArrayList<CreatureCard> set = new ArrayList<CreatureCard>();
			ArrayList<Integer> indices = (ArrayList<Integer>) subSet.getVector();
			for (int i = 0; i < indices.size(); i++) {
				set.add(possibleBlockers.get(indices.get(i)));
			}
			blockSets.add(set);

			// blockSets.add(new ArrayList<CreatureCard>());
			// for (int i = 0; i < tmpSize; i++) {
			// if (subSet.contains(i)) {
			// blockSets.get(tmpcnt).add(possibleBlockers.get(i));
			// }
			// }
			// tmpcnt++;

		}
		int amountOfOptions = blockSets.size();
		double c = Math.sqrt(2);
		ArrayList<ArrayList<Integer>> scores = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < amountOfOptions; i++) {
			scores.add(new ArrayList<Integer>());
		}
		int totalVisitCounter = 0;

		while (totalVisitCounter < amountOfOptions * 1000) {

			double tmpLog = Math.log(totalVisitCounter);
			Game newGame = this.copy();
			newGame.setPlayer1(new RandomPlayer(this.player1.getName()));
			newGame.setPlayer2(new RandomPlayer(this.player2.getName()));

			double[] ucbValues = new double[amountOfOptions];

			for (int i = 0; i < amountOfOptions; i++) {

				if (scores.get(i).size() > 0) {
					double tmp = 0;
					for (int j = 0; j < scores.get(i).size(); j++) {
						tmp += scores.get(i).get(j);
					}
					double avg = tmp / scores.get(i).size();
					ucbValues[i] = avg + c * Math.sqrt((tmpLog / scores.get(i).size()));
				} else {
					ucbValues[i] = random.nextDouble() * 0.000001 + 1;
				}
			}
			int bestOption = 0;
			double bestScore = Integer.MIN_VALUE;
			for (int i = 0; i < amountOfOptions; i++) {
				if (ucbValues[i] > bestScore) {
					bestScore = ucbValues[i];
					bestOption = i;
				}
			}

			if (activePlayer == 1) {
				ArrayList<ArrayList<CreatureCard>> blockers = new ArrayList<ArrayList<CreatureCard>>();

				ArrayList<CreatureCard> bestBlockers = blockSets.get(bestOption);
				for (int i = 0; i < attackers.size(); i++) {
					ArrayList<CreatureCard> blockCreatures = new ArrayList<CreatureCard>();
					for (int j = i; j < bestBlockers.size(); j += attackers.size()) {
						blockCreatures.add(bestBlockers.get(j));
					}
					blockers.add(blockCreatures);
				}
				// for (int i = 0; i < blockSets.get(bestOption).size(); i++) {
				// blockers.add(new ArrayList<CreatureCard>());
				// blockers.get(i % attackers.size()).add((CreatureCard)
				// blockSets.get(bestOption).get(i).copy());
				// }
				newGame.resolve(attackers, blockers);
				newGame.setActivePlayer(2);
				newGame.run();
				if (newGame.getWinnerNumber() == 1) {
					scores.get(bestOption).add(-1);
				}
				if (newGame.getWinnerNumber() == 2) {
					scores.get(bestOption).add(1);
				}
				if (newGame.getWinnerNumber() == 0) {
					scores.get(bestOption).add(0);
				}
			}
			if (activePlayer == 2) {
				ArrayList<ArrayList<CreatureCard>> blockers = new ArrayList<ArrayList<CreatureCard>>();

				ArrayList<CreatureCard> bestBlockers = blockSets.get(bestOption);
				for (int i = 0; i < attackers.size(); i++) {
					ArrayList<CreatureCard> blockCreatures = new ArrayList<CreatureCard>();
					for (int j = i; j < bestBlockers.size(); j += attackers.size()) {
						blockCreatures.add(bestBlockers.get(j));
					}
					blockers.add(blockCreatures);
				}
				// for (int i = 0; i < blockSets.get(bestOption).size(); i++) {
				// blockers.add(new ArrayList<CreatureCard>());
				// blockers.get(i % attackers.size()).add((CreatureCard)
				// blockSets.get(bestOption).get(i).copy());
				// }
				newGame.resolve(attackers, blockers);
				newGame.setActivePlayer(1);
				newGame.run();
				if (newGame.getWinnerNumber() == 1) {
					scores.get(bestOption).add(1);
				}
				if (newGame.getWinnerNumber() == 2) {
					scores.get(bestOption).add(-1);
				}
				if (newGame.getWinnerNumber() == 0) {
					scores.get(bestOption).add(0);
				}
			}
			totalVisitCounter++;
		}
		double[] ucbValues = new double[amountOfOptions];

		for (int i = 0; i < amountOfOptions; i++) {

			if (scores.get(i).size() > 0) {
				double tmp = 0;
				for (int j = 0; j < scores.get(i).size(); j++) {
					tmp += scores.get(i).get(j);
				}
				double avg = tmp / scores.get(i).size();
				ucbValues[i] = avg + c * Math.sqrt((Math.log(totalVisitCounter) / scores.get(i).size()));
			} else {
				ucbValues[i] = 100 + random.nextDouble() * 0.000001;
			}
		}
		int bestOption = 0;
		double bestScore = Integer.MIN_VALUE;
		for (int i = 0; i < amountOfOptions; i++) {
			if (ucbValues[i] > bestScore) {
				bestScore = ucbValues[i];
				bestOption = i;
			}
		}

		ArrayList<ArrayList<CreatureCard>> blockers = new ArrayList<ArrayList<CreatureCard>>();
		int number1 = attackers.size();
		for (int i = 0; i < number1; i++) {
			blockers.add(new ArrayList<CreatureCard>());
		}

		int number2 = blockSets.get(bestOption).size();

		for (int i = 0; i < number2; i++) {
			blockers.get(i % number1).add(blockSets.get(bestOption).get(i));
		}
		// System.out.println("MCTS found blockers!");
		return blockers;

	}

	public ArrayList<CreatureCard> MonteCarloChooseAttackers(ArrayList<CreatureCard> possibleAttackers,
			ArrayList<CreatureCard> possibleBlockers) {
		// System.out.println("MCTS is choosing attackers! Amount: " +
		// possibleAttackers.size());
		// int size = possibleAttackers.size();
		// Generator<Integer> combinatorics = this.newCombinations(size);
		// int tmpcnt = 0;
		// ArrayList<ArrayList<CreatureCard>> attackSets = new
		// ArrayList<ArrayList<CreatureCard>>();
		// for (ICombinatoricsVector<Integer> subSet : combinatorics) {
		// attackSets.add(new ArrayList<CreatureCard>());
		// for (int i = 0; i < combinatorics.getOriginalVector().getSize(); i++)
		// {
		// if (subSet.contains(i)) {
		// attackSets.get(tmpcnt).add(possibleAttackers.get(i));
		// }
		// }
		// tmpcnt++;
		// }
		//
		int size = possibleAttackers.size();
		Generator<Integer> combinatorics = this.newCombinations(size);
		int tmpcnt = 0;
		ArrayList<ArrayList<CreatureCard>> attackSets = new ArrayList<ArrayList<CreatureCard>>();

		int tmpSize = combinatorics.getOriginalVector().getSize();
		for (ICombinatoricsVector<Integer> subSet : combinatorics) {
			ArrayList<CreatureCard> set = new ArrayList<CreatureCard>();
			ArrayList<Integer> indices = (ArrayList<Integer>) subSet.getVector();
			for (int i = 0; i < indices.size(); i++) {
				set.add(possibleAttackers.get(indices.get(i)));
			}
			attackSets.add(set);
		}
		int amountOfOptions = attackSets.size();
		double c = Math.sqrt(2);
		ArrayList<ArrayList<Integer>> scores = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < amountOfOptions; i++) {
			scores.add(new ArrayList<Integer>());
		}
		int totalVisitCounter = 0;

		while (totalVisitCounter < amountOfOptions * 1000) {

			double tmpLog = Math.log(totalVisitCounter);
			Game newGame = this.copy();
			newGame.setPlayer1(new RandomPlayer(this.player1.getName()));
			newGame.setPlayer2(new RandomPlayer(this.player2.getName()));

			double[] ucbValues = new double[amountOfOptions];

			for (int i = 0; i < amountOfOptions; i++) {

				if (scores.get(i).size() > 0) {
					double tmp = 0;
					for (int j = 0; j < scores.get(i).size(); j++) {
						tmp += scores.get(i).get(j);
					}
					double avg = tmp / scores.get(i).size();
					ucbValues[i] = avg + c * Math.sqrt((tmpLog / scores.get(i).size()));
				} else {
					ucbValues[i] = random.nextDouble() * 0.000001 + 1;
				}
			}
			int bestOption = 0;
			double bestScore = Integer.MIN_VALUE;
			for (int i = 0; i < amountOfOptions; i++) {
				if (ucbValues[i] > bestScore) {
					bestScore = ucbValues[i];
					bestOption = i;
				}
			}

			if (activePlayer == 1) {
				ArrayList<CreatureCard> attackers = new ArrayList<CreatureCard>();
				ArrayList<ArrayList<CreatureCard>> blockers = new ArrayList<ArrayList<CreatureCard>>();
				for (int i = 0; i < attackSets.get(bestOption).size(); i++) {
					attackers.add((CreatureCard) attackSets.get(bestOption).get(i).copy());
				}
				if (attackers.size() > 0) {
					blockers = player2.chooseBlockers(attackers, this.field2.getCreatures());
				}
				newGame.resolve(attackers, blockers);
				newGame.setActivePlayer(2);
				newGame.run();

				// System.out.println(attackers.size() + " attackers simulated
				// vs " + blockers.size() + " blockers. Result: " +
				// newGame.getWinner());

				if (newGame.getWinnerNumber() == 1) {
					scores.get(bestOption).add(1);
				}
				if (newGame.getWinnerNumber() == 2) {
					scores.get(bestOption).add(-1);
				}
				if (newGame.getWinnerNumber() == 0) {
					scores.get(bestOption).add(0);
				}
			}
			if (activePlayer == 2) {
				ArrayList<CreatureCard> attackers = new ArrayList<CreatureCard>();
				ArrayList<ArrayList<CreatureCard>> blockers = new ArrayList<ArrayList<CreatureCard>>();
				for (int i = 0; i < attackSets.get(bestOption).size(); i++) {
					attackers.add((CreatureCard) attackSets.get(bestOption).get(i).copy());
				}
				if (attackers.size() > 0) {
					blockers = player1.chooseBlockers(attackers, this.field2.getCreatures());
				}
				newGame.resolve(attackers, blockers);
				newGame.setActivePlayer(1);
				newGame.run();
				// System.out.println(attackers.size() + " attackers simulated
				// vs " + blockers.size() + " blockers. Result: " +
				// newGame.getWinner());
				if (newGame.getWinnerNumber() == 1) {
					scores.get(bestOption).add(-1);
				}
				if (newGame.getWinnerNumber() == 2) {
					scores.get(bestOption).add(1);
				}
				if (newGame.getWinnerNumber() == 0) {
					scores.get(bestOption).add(0);
				}
			}
			totalVisitCounter++;
		}
		double[] ucbValues = new double[amountOfOptions];

		for (int i = 0; i < amountOfOptions; i++) {

			if (scores.get(i).size() > 0) {
				double tmp = 0;
				for (int j = 0; j < scores.get(i).size(); j++) {
					tmp += scores.get(i).get(j);
				}
				double avg = tmp / scores.get(i).size();
				ucbValues[i] = avg + c * Math.sqrt((Math.log(totalVisitCounter) / scores.get(i).size()));
			} else {
				ucbValues[i] = 100 + random.nextDouble() * 0.000001;
			}
		}
		int bestOption = 0;
		double bestScore = Integer.MIN_VALUE;
		for (int i = 0; i < amountOfOptions; i++) {
			if (ucbValues[i] > bestScore) {
				bestScore = ucbValues[i];
				bestOption = i;
			}
		}

		// System.out.println("MCTS found attackers!");
		// System.out.println(this.isOver());
		// System.out.println("Attackers: " + attackSets.get(bestOption).size()
		// + "Lands: " + field2.getLands().size());
		return attackSets.get(bestOption);
	}

	public Generator<Integer> newCombinations(int size) {
		Integer[] combinatorics = new Integer[size];
		for (int i = 0; i < size; i++) {
			combinatorics[i] = i;
		}
		// Create an initial vector/set
		ICombinatoricsVector<Integer> initialSet = Factory.createVector(combinatorics);

		// Create an instance of the subset generator
		Generator<Integer> gen = Factory.createSubSetGenerator(initialSet);

		return gen;
	}

	public Deck getOpponentDeck(int player) {
		ArrayList<ArrayList<Card>> determinizations = new ArrayList<ArrayList<Card>>();
		for (int i = 0; i < 10; i++) {
			determinizations.add(new ArrayList<Card>());
		}
		int[] cards = new int[7];
		Deck newDeck = new Deck();

		if (player == 1) {
			for (CreatureCard card : field1.getCreatures()) {
				if (card.getName() == "Elf") {
					cards[0]++;
				} else if (card.getName() == "Bear") {
					cards[1]++;
				} else if (card.getName() == "Goblin") {
					cards[2]++;
				} else if (card.getName() == "Cyclops") {
					cards[3]++;
				} else if (card.getName() == "Giant") {
					cards[4]++;
				} else if (card.getName() == "Dragon") {
					cards[5]++;
				}

			}
			cards[6] += field1.getLands().size();

			for (Card card : graveyard1.getCards()) {
				if (card.getName() == "Elf") {
					cards[0]++;
				} else if (card.getName() == "Bear") {
					cards[1]++;
				} else if (card.getName() == "Goblin") {
					cards[2]++;
				} else if (card.getName() == "Cyclops") {
					cards[3]++;
				} else if (card.getName() == "Giant") {
					cards[4]++;
				} else if (card.getName() == "Dragon") {
					cards[5]++;
				} else if (card.getName() == "Mana") {
					cards[6]++;
				}
			}

			newDeck.emptyDeck();
			for (int i = 0; i < 6; i++) {
				for (int j = 0; j < 4 - cards[i]; j++) {
					if (i == 0) {
						newDeck.add(new CreatureCard("Elf", 1, 1, 1));
					}
					if (i == 1) {
						newDeck.add(new CreatureCard("Bear", 2, 2, 2));
					}
					if (i == 2) {
						newDeck.add(new CreatureCard("Goblin", 3, 4, 2));
					}
					if (i == 3) {
						newDeck.add(new CreatureCard("Cyclops", 4, 5, 3));
					}
					if (i == 4) {
						newDeck.add(new CreatureCard("Giant", 5, 3, 6));
					}
					if (i == 5) {
						newDeck.add(new CreatureCard("Dragon", 5, 6, 4));
					}
				}
			}
			for (int i = 0; i < 17 - cards[6]; i++) {
				newDeck.add(new LandCard());
			}
			newDeck.shuffle();

		} else if (player == 2) {
			for (CreatureCard card : field2.getCreatures()) {
				if (card.getName() == "Elf") {
					cards[0]++;
				} else if (card.getName() == "Bear") {
					cards[1]++;
				} else if (card.getName() == "Goblin") {
					cards[2]++;
				} else if (card.getName() == "Cyclops") {
					cards[3]++;
				} else if (card.getName() == "Giant") {
					cards[4]++;
				} else if (card.getName() == "Dragon") {
					cards[5]++;
				}

			}
			cards[6] += field2.getLands().size();

			for (Card card : graveyard2.getCards()) {
				if (card.getName() == "Elf") {
					cards[0]++;
				} else if (card.getName() == "Bear") {
					cards[1]++;
				} else if (card.getName() == "Goblin") {
					cards[2]++;
				} else if (card.getName() == "Cyclops") {
					cards[3]++;
				} else if (card.getName() == "Giant") {
					cards[4]++;
				} else if (card.getName() == "Dragon") {
					cards[5]++;
				} else if (card.getName() == "Mana") {
					cards[6]++;
				}
			}

			newDeck.emptyDeck();
			for (int i = 0; i < 6; i++) {
				for (int j = 0; j < 4 - cards[i]; j++) {
					if (i == 0) {
						newDeck.add(new CreatureCard("Elf", 1, 1, 1));
					}
					if (i == 1) {
						newDeck.add(new CreatureCard("Bear", 2, 2, 2));
					}
					if (i == 2) {
						newDeck.add(new CreatureCard("Goblin", 3, 4, 2));
					}
					if (i == 3) {
						newDeck.add(new CreatureCard("Cyclops", 4, 5, 3));
					}
					if (i == 4) {
						newDeck.add(new CreatureCard("Giant", 5, 3, 6));
					}
					if (i == 5) {
						newDeck.add(new CreatureCard("Dragon", 5, 6, 4));
					}
				}
			}
			for (int i = 0; i < 17 - cards[6]; i++) {
				newDeck.add(new LandCard());
			}
			newDeck.shuffle();

		}
		return newDeck;
	}

	/**
	 * Use MCTS to choose the next move in the game
	 * 
	 * @param stage
	 *            The initial stage
	 * @param game
	 *            The game
	 * @return
	 */
	public ArrayList<Integer> MCTSChoose(int stage, Game game) {
		Game simulation = game.copy();
		// Random players that handle the logic for the playouts
		RandomPlayer player1 = new RandomPlayer("Random Player 1");
		RandomPlayer player2 = new RandomPlayer("Random Player 2");
		this.tree = new ArrayList<Node>();

		// The initial UCT with an added small value for tie breakers
		double initialUCT = this.initial_UCT + random.nextDouble() * 0.00001;
		// double initialUCT = 5 + random.nextDouble() * 0.00001;
		Node root = new Node(new ArrayList<Integer>(), stage, null, new ArrayList<Integer>(), initialUCT);
		this.tree.add(root);
		int n = 0;
		Node bestNode = root;

		// Simulation limit of 1000
		while (n < 20000) {
			// selection
			simulation = game.copy();
			n++;
			bestNode = root;
			int player = 0;
			ArrayList<CreatureCard> attackers = new ArrayList<CreatureCard>();
			ArrayList<ArrayList<CreatureCard>> blockers = new ArrayList<ArrayList<CreatureCard>>();

			// While there are children, resolve actions and keep going until we
			// get the best unexplored one
			boolean newChildFound = false;

			do {

				if (bestNode == null) {
					bestNode = root;
				}
				if (!bestNode.hasParent()) {
					if (bestNode.hasChildren()) {
						newChildFound = true;
						bestNode = bestNode.selectBestChild(player % 2);
					} else {
						newChildFound = false;
					}
					// player++;
					continue;
				}

				if (!newChildFound)
					break;

				player++;
				// If the past bestnode was an attack, we retrieve these
				// attackers. The attacks will be resolved when blockers have
				// been declared
				if (bestNode.getStage() == P_ATTACK) {
					attackers = this.MCTSattack(bestNode.getMove(), simulation);

					// If the past node was a block, we have all we need to
					// resolve
				} else if (bestNode.getStage() == P_BLOCK) {
					blockers = this.MCTSblock(bestNode.getMove(), simulation);
					simulation.resolve(attackers, blockers);
					attackers = null;
					blockers = null;
					// If the past node was a playnode, we need to resolve the
					// adding of creatures to the board
				} else if (bestNode.getStage() == P_PLAY) {
					// Retrieve the hand and field of the active player
					Hand hand = simulation.getActivePlayer() == 1 ? simulation.hand1 : simulation.hand2;
					Field field = simulation.getActivePlayer() == 1 ? simulation.field1 : simulation.field2;

					// From the move and the simulation, this retrieves a list
					// of the creatures that must be played
					ArrayList<CreatureCard> creatures = this.MCTScreatures(bestNode.getMove(), simulation);

					// If there is at least one land in the hand, play it
					if (hand.containsLand()) {
						field.playLand(hand.playLand());
					}

					// Play them on the field of the active player
					for (int i = 0; i < creatures.size(); i++) {
						field.playCreature(creatures.get(i));
					}
					// Remove the played card from the hand
					for (int i = 0; i < creatures.size(); i++) {
						hand.removeCard(creatures.get(i));
					}

					// After the play phase, switch players and draw a card
					simulation.setActivePlayer(simulation.getActivePlayer() % 2 + 1);
					simulation.draw();
				}

				// If the node had children, the journey continues
				if (bestNode.hasChildren()) {
					newChildFound = true;
					bestNode = bestNode.selectBestChild(player % 2);
				} else {
					newChildFound = false;
				}
			} while (newChildFound);

			// When we break free from the loop, a node for which to expand has
			// been found
			// System.out.println("Selection chose: " + bestNode);

			// expansion
			if (!simulation.isOver()) {

				Generator<Integer> choices = null;

				Field[] fields = new Field[] { simulation.field1, simulation.field2 };
				Hand[] hands = new Hand[] { simulation.hand1, simulation.hand2 };

				// To zero based index
				int currentPlayer = simulation.getActivePlayer() - 1; // Transposing
																		// for
																		// the
																		// arrays
																		// above
				int enemyPlayer = simulation.getActivePlayer() % 2;

				int previousStage = bestNode.getStage();

				// if we are at the root, special stage calculatrons
				if (!bestNode.hasParent()) {
					previousStage--;
					if (previousStage == 0) {
						previousStage = 3;
						// Switching commented out pending further investigation
						// TODO
						// int tmpPlayer = enemyPlayer;
						// enemyPlayer = currentPlayer;
						// currentPlayer = tmpPlayer;
					}
				}

				// Fetch the creature numbers of the field and the hand of the
				// players
				int enemyCreaturesField = fields[enemyPlayer].getNumberCreatures();
				int playerCreaturesField = fields[currentPlayer].getNumberCreatures();
				int enemyCreaturesHand = hands[enemyPlayer].getNumberCreatures();
				int playerCreaturesHand = hands[currentPlayer].getNumberCreatures();

				// If the previous stage was an attack, we must find blockers
				// among the enemy creatures
				if (previousStage == P_ATTACK) {
					// We need no blockers we declared no attackers, or no
					// creature is available to block
					if (bestNode.getMove().size() > 0 && enemyCreaturesField > 0) {
						choices = this.newCombinations(enemyCreaturesField);
					}
					// If the previous stage was a block, we must now play. So
					// the combinations come from our hand
				} else if (previousStage == P_BLOCK && playerCreaturesHand > 0) {
					choices = this.newCombinations(playerCreaturesHand);
					// IF the previous stage was a play, we must select
					// attackers from our field
				} else if (previousStage == P_PLAY && playerCreaturesField > 0) {
					choices = this.newCombinations(playerCreaturesField);
				}

				ArrayList<Node> children = new ArrayList<Node>();
				// THe next stage is chosen using the magic of modulons
				int newStage = bestNode.getStage() % 3;
				// Due to the initial setup the root node stays the same stage.
				if (bestNode.hasParent()) {
					newStage++;
				}
				// System.out.println(newStage);

				// IF choices is null, we had no options. IF he is null he will
				// be filled with an empty movenode later on
				if (choices != null) {
					// Get the hands and fields again
					Hand hand = simulation.getHand(currentPlayer + 1);
					Field field = simulation.getField(currentPlayer + 1);

					// The previous stage consisted of what we like to call
					// blocking
					if (previousStage == P_BLOCK) {
						// get the combinations of creatures in hand to play
						// after the blocking phase
						for (ICombinatoricsVector<Integer> subSet : choices) {
							ArrayList<Integer> integers = (ArrayList<Integer>) subSet.getVector();

							int total = 0;
							int max = field.getUntappedLands();
							// Each element of the combination of actions has a
							// mana cost, ensure the sum is <= the land
							for (int i = 0; i < integers.size(); i++) {
								total += hand.getCreatures().get(integers.get(i)).getManaCost();
							}

							// IF we have the mana, create a node for this
							// combination
							if (total <= max) {
								Node newNode = new Node(integers, newStage, bestNode, new ArrayList<Integer>(),
										initialUCT);
								tree.add(newNode);
								children.add(newNode);
							}

						}
						// If there was no blocking phase,
					} else {
						// If the previous stage was either play or attack, we
						// get the choices and add them to the node without a
						// need to check for mana
						for (ICombinatoricsVector<Integer> subSet : choices) {
							ArrayList<Integer> integers = (ArrayList<Integer>) subSet.getVector();

							Node newNode = new Node(integers, newStage, bestNode, new ArrayList<Integer>(), initialUCT);
							tree.add(newNode);
							children.add(newNode);
						}
					}
				}
				// Like I said before, if there were no children, a shell of one
				// is added
				if (children.isEmpty()) {

					Node noMoveNode = new Node(new ArrayList<Integer>(), newStage, bestNode, new ArrayList<Integer>(),
							initialUCT);

					children.add(noMoveNode);
					tree.add(noMoveNode);

				}
				bestNode.setChildren(children);
			}
			// System.out.println("Expansion made " + children.size() + "
			// children!");

			// TODO: select best/random child

			// playout

			simulation.setPlayer1(player1);
			simulation.setPlayer2(player2);
			int playerThatWon = simulation.run();
			int result = 0;
			if (playerThatWon == this.activePlayer) {
				result = 1;
			} else
				result = -1;
			// int result = simulation.getWinner(this.getActivePlayer());

			// System.out.println("Playout, winner is: " + result);

			// System.out.println("Show me the money! " + result);

			// propagate
			int tmp = 0;
			Node tmpNode = bestNode;
			while (tmpNode.getParent() != null) {
				tmpNode.addScore(result);
				tmpNode.updateUCT(n, c_constant);
				tmpNode = (Node) tmpNode.getParent();
				tmp++;
			}
			// System.out.println("Backpropagate reached root? " +
			// !tmpNode.hasParent()
			// + " the depth of the simulated node is " + tmp);

		}
		ArrayList<Integer> bestSet = new ArrayList<Integer>();

		bestSet = root.selectBestChild(this.activePlayer - 1).getMove();

		// System.out.println(bestSet.size() + " is best set size. Size of tree:
		// " + tree.size());
		return bestSet;
	}

	/**
	 * Returns the hand of the player with the given index, 1-based
	 * 
	 * @param player
	 * @return
	 */
	private Hand getHand(int player) {
		if (player == 1)
			return hand1;
		else if (player == 2)
			return hand2;
		else
			throw new RuntimeException("We don fucked up bois ;) - Hand");
	}

	/**
	 * Gets the field belonging to the player with the ID. This is a one based
	 * index.
	 * 
	 * @param player
	 * @return
	 */
	private Field getField(int player) {
		if (player == 1)
			return field1;
		else if (player == 2)
			return field2;
		else
			throw new RuntimeException("We don fucked up bois - Field");
	}

	private ArrayList<CreatureCard> integerToCreature(ArrayList<Integer> integer, ArrayList<CreatureCard> creature) {
		ArrayList<CreatureCard> toReturn = new ArrayList<CreatureCard>();
		for (int i = 0; i < integer.size(); i++) {
			toReturn.add(creature.get(integer.get(i)));
		}
		return toReturn;
	}

	private ArrayList<ArrayList<CreatureCard>> singleToDoubleArray(ArrayList<CreatureCard> creatures, int size) {
		ArrayList<ArrayList<CreatureCard>> toReturn = new ArrayList<ArrayList<CreatureCard>>();
		for (int i = 0; i < size; i++) {
			ArrayList<CreatureCard> creatureList = new ArrayList<CreatureCard>();
			for (int j = i; j < creatures.size(); j += size) {
				creatureList.add(creatures.get(j));
			}
			toReturn.add(creatureList);
		}
		return toReturn;
	}

	private ArrayList<CreatureCard> MCTScreatures(ArrayList<Integer> move, Game simulation) {
		ArrayList<CreatureCard> options = null;
		ArrayList<CreatureCard> creatures = new ArrayList<CreatureCard>();

		if (simulation.getActivePlayer() == 1) {
			options = simulation.hand1.getCreatures();
		} else if (simulation.getActivePlayer() == 2) {
			options = simulation.hand2.getCreatures();
		}

		for (int i = 0; i < move.size(); i++) {
			if (move.get(i) >= options.size()) {
				System.out.println("Help! " + simulation.isOver());
			}
		}
		for (int i = 0; i < move.size(); i++) {
			creatures.add(options.get(move.get(i)));
		}

		return creatures;
	}

	private ArrayList<ArrayList<CreatureCard>> MCTSblock(ArrayList<Integer> move, Game simulation) {
		ArrayList<CreatureCard> options = null;
		ArrayList<CreatureCard> creatures = new ArrayList<CreatureCard>();
		ArrayList<ArrayList<CreatureCard>> finalSet = new ArrayList<ArrayList<CreatureCard>>();
		if (simulation.attackers != null) {
			int attackSize = simulation.attackers.size();
			if (simulation.getActivePlayer() == 1) {
				options = simulation.field2.getCreatures();
			} else if (simulation.getActivePlayer() == 2) {
				options = simulation.field1.getCreatures();
			}

			for (int i = 0; i < move.size(); i++) {
				creatures.add(options.get(move.get(i)));
			}

			for (int i = 0; i < attackSize; i++) {
				ArrayList<CreatureCard> blockCreatures = new ArrayList<CreatureCard>();
				for (int j = i; j < creatures.size(); j += attackSize) {
					blockCreatures.add(creatures.get(j));
				}
				finalSet.add(blockCreatures);
			}
		}

		return finalSet;
	}

	private ArrayList<CreatureCard> MCTSattack(ArrayList<Integer> move, Game simulation) {
		ArrayList<CreatureCard> options = null;
		ArrayList<CreatureCard> creatures = new ArrayList<CreatureCard>();
		if (simulation.getActivePlayer() == 1) {
			options = simulation.field1.getCreatures();
		} else if (simulation.getActivePlayer() == 2) {
			options = simulation.field2.getCreatures();
		}

		for (int i = 0; i < move.size(); i++) {
			if (move.get(i) >= options.size()) {
				System.out.println("Help! " + simulation.isOver());
			}
		}
		// System.out.println(options.size() + " is the amount of creatures that
		// player " + simulation.getActivePlayer()
		// + " has. The amount of attackers that MCTS chose was " +
		// move.size());
		for (int i = 0; i < move.size(); i++) {
			// System.out.println("hERE");
			creatures.add(options.get(move.get(i)));
		}

		return creatures;
	}

	/*
	 * private int playout(Node bestNode) { Game simulation =
	 * bestNode.getGame().copy(); simulation.setPlayer1(new RandomPlayer(
	 * "Player 1")); simulation.setPlayer2(new RandomPlayer("Player 2")); int
	 * stage = bestNode.getStage(); int result = 0; if (stage == 1) { result =
	 * simulation.run(); } if (stage == 2) {
	 * simulation.getPlayer1().playCreature(simulation.findPlayableCreatures(
	 * simulation.hand1.getCardsInHand(),
	 * simulation.field1.getUntappedLands())); simulation.switchActivePlayer();
	 * result = simulation.run(); } if (stage == 3) {
	 * simulation.switchActivePlayer(); result = simulation.run(); } return
	 * result; }
	 */
	public Node selectBestNode() {
		double bestUCT = Double.MIN_VALUE;
		Node bestOption = null;
		for (Node node : this.tree) {
			double epsilon = random.nextDouble() * 0.00001;
			if (node.getUCT() + epsilon > bestUCT) {
				bestUCT = node.getUCT() + epsilon;
				bestOption = node;
			}
		}
		tree.remove(bestOption);
		return bestOption;
	}

	/*
	 * public void createChildren(Node bestNode) { if (bestNode.getStage() == 1)
	 * { // attack ArrayList<CreatureCard> possibleAttackers = new
	 * ArrayList<CreatureCard>(); if (bestNode.getGame().getActivePlayer() == 1)
	 * { possibleAttackers = bestNode.getGame().field1.getCreatures(); } if
	 * (bestNode.getGame().getActivePlayer() == 2) { possibleAttackers =
	 * bestNode.getGame().field2.getCreatures(); }
	 * 
	 * int size = possibleAttackers.size(); Generator<Integer> combinatorics =
	 * this.newCombinations(size); int tmpcnt = 0;
	 * ArrayList<ArrayList<CreatureCard>> attackSets = new
	 * ArrayList<ArrayList<CreatureCard>>();
	 * 
	 * int tmpSize = combinatorics.getOriginalVector().getSize(); for
	 * (ICombinatoricsVector<Integer> subSet : combinatorics) {
	 * ArrayList<CreatureCard> set = new ArrayList<CreatureCard>();
	 * ArrayList<Integer> indices = (ArrayList<Integer>) subSet.getVector(); for
	 * (int i = 0; i < indices.size(); i++) {
	 * set.add(possibleAttackers.get(indices.get(i))); } attackSets.add(set); }
	 * for (int i = 0; i < attackSets.size(); i++) { Game game =
	 * bestNode.getGame().copy(); ArrayList<CreatureCard> possibleBlockers =
	 * null; if (game.getActivePlayer() == 1) { possibleBlockers =
	 * game.field2.getCreatures(); } else if (game.getActivePlayer() == 2) {
	 * possibleBlockers = game.field1.getCreatures(); }
	 * ArrayList<ArrayList<CreatureCard>> blockers =
	 * game.MonteCarloChooseBlockers(attackSets.get(i), possibleBlockers);
	 * game.resolve(attackSets.get(i), blockers); tree.add(new Node(game, 2,
	 * bestNode, new ArrayList<Integer>(), 0));
	 * 
	 * }
	 * 
	 * } else if (bestNode.getStage() == 2) { // play creature Hand hand = null;
	 * int untappedMana = 0; if (bestNode.getGame().getActivePlayer() == 1) {
	 * hand = bestNode.getGame().hand1; untappedMana =
	 * bestNode.getGame().field1.getUntappedLands(); } else if
	 * (bestNode.getGame().getActivePlayer() == 2) { hand =
	 * bestNode.getGame().hand2; untappedMana =
	 * bestNode.getGame().field2.getUntappedLands();
	 * 
	 * } ArrayList<ArrayList<Integer>> options = this.getAllPossibilities(hand,
	 * untappedMana); for (int i = 0; i < options.size(); i++) { Game game =
	 * bestNode.getGame().copy(); if (bestNode.getGame().getActivePlayer() == 1)
	 * { for (int j = 0; j < options.get(i).size(); j++) {
	 * game.field1.playCreature( (CreatureCard)
	 * (hand1.removeCard(hand1.getCardsInHand().get(options.get(i).get(j))))); }
	 * } tree.add(new Node(game, 3, bestNode, new ArrayList<Integer>(), 0)); }
	 * 
	 * } else if (bestNode.getStage() == 3) { // block ArrayList<CreatureCard>
	 * attackers = null; ArrayList<CreatureCard> potentialBlockers = null; if
	 * (bestNode.getGame().getActivePlayer() == 1) { attackers =
	 * this.MonteCarloChooseAttackers(bestNode.getGame().field1.getCreatures(),
	 * bestNode.getGame().field2.getCreatures()); potentialBlockers =
	 * bestNode.getGame().field2.getCreatures(); } if
	 * (bestNode.getGame().getActivePlayer() == 2) { attackers =
	 * this.MonteCarloChooseAttackers(bestNode.getGame().field2.getCreatures(),
	 * bestNode.getGame().field1.getCreatures()); potentialBlockers =
	 * bestNode.getGame().field1.getCreatures(); }
	 * 
	 * Generator<Integer> combinatorics =
	 * this.newCombinations(potentialBlockers.size()); int tmpcnt = 0;
	 * ArrayList<ArrayList<CreatureCard>> blockSets = new
	 * ArrayList<ArrayList<CreatureCard>>();
	 * 
	 * int tmpSize = combinatorics.getOriginalVector().getSize(); for
	 * (ICombinatoricsVector<Integer> subSet : combinatorics) {
	 * ArrayList<CreatureCard> set = new ArrayList<CreatureCard>();
	 * ArrayList<Integer> indices = (ArrayList<Integer>) subSet.getVector(); for
	 * (int i = 0; i < indices.size(); i++) {
	 * set.add(potentialBlockers.get(indices.get(i))); } blockSets.add(set);
	 * 
	 * for (int i = 0; i < blockSets.size(); i++) { Game game =
	 * bestNode.getGame().copy(); ArrayList<ArrayList<CreatureCard>> blockers =
	 * new ArrayList<ArrayList<CreatureCard>>();
	 * 
	 * ArrayList<CreatureCard> currentBlockers = blockSets.get(i); for (int k =
	 * 0; k < attackers.size(); k++) { ArrayList<CreatureCard> blockCreatures =
	 * new ArrayList<CreatureCard>(); for (int j = k; j <
	 * currentBlockers.size(); j += attackers.size()) {
	 * blockCreatures.add(currentBlockers.get(j)); }
	 * blockers.add(blockCreatures); } game.resolve(attackers, blockers);
	 * game.switchActivePlayer(); tree.add(new Node(game, 1, bestNode, new
	 * ArrayList<Integer>(), 0)); } }
	 * 
	 * } }
	 */

	public void switchActivePlayer() {
		if (this.activePlayer == 1) {
			this.activePlayer = 2;
		} else {
			this.activePlayer = 1;
		}
	}

	public static void main(String args[]) {
		while (true) {
			Player MCTSBoiz = new MonteCarloPlayer("Pimp");
			Player RandomBoiz = new RandomPlayer("Scrubbie");

			Game game = new Game(MCTSBoiz, RandomBoiz, 1, 1, Math.sqrt(2));
			game.setLife1(100);
			game.setLife2(5);

			Field field1boiz = new Field();
			Field field2boiz = new Field();

			Hand hand1 = new Hand();
			// hand1.addCard(new CreatureCard("Ball", 0, 1, 1));
			field1boiz.playCreature(new CreatureCard("Piemel", 0, 100, 100));

			Hand hand2 = new Hand();
			hand2.addCard(new CreatureCard("Ball", 0, 134131231, 113134134));
			hand2.addCard(new CreatureCard("Ball", 0, 134131231, 113134134));
			hand2.addCard(new CreatureCard("Ball", 0, 134131231, 113134134));
			hand2.addCard(new CreatureCard("Ball", 0, 134131231, 113134134));
			hand2.addCard(new CreatureCard("Ball", 0, 134131231, 113134134));
			hand2.addCard(new CreatureCard("Ball", 0, 134131231, 113134134));

			// field2boiz.playCreature(new CreatureCard("Piemel", 0, 100, 100));

			game.setField1(field1boiz);
			game.setHand1(hand1);

			game.setField2(field2boiz);
			game.setHand2(hand2);

			int winner = game.run();
			System.out.println(winner);
		}
	}
}