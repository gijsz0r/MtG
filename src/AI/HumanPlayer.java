package AI;

import java.util.ArrayList;
import java.util.Scanner;

import Cards.CreatureCard;
import Game.Game;

public class HumanPlayer extends Player {
	private String name;
	private boolean MCTS = false;

	public HumanPlayer(String name) {
		this.name = name;
	}

	public boolean isMCTS() {
		return MCTS;
	}
	
	public int playCreature(ArrayList<CreatureCard> creatures) {
		if (creatures.size() == 0) {
			return -1;
		}
		System.out.println("Which creature do you want to play? Your playable creatures are:");
		String names = "-1 Nothing! ";
		for (int i = 0; i < creatures.size(); i++) {
			names += (i + " " + creatures.get(i).getName() + " ");
		}
		System.out.println(names);
		Scanner in = new Scanner(System.in);
		int output = in.nextInt();
		if (output >= 0 && output < creatures.size()) {
			return output;
		} else {
			return -1;
		}
	}

	public ArrayList<CreatureCard> chooseAttackers(ArrayList<CreatureCard> potentialAttackers, ArrayList<CreatureCard> blockers) {
		ArrayList<CreatureCard> attackers = new ArrayList<CreatureCard>();
		if (potentialAttackers.size() > 0) {

			String list = "You have creatures: ";
			for (int i = 0; i < potentialAttackers.size(); i++) {
				list += i + 1 + ": " + potentialAttackers.get(i).getName() + ", ";
			}
			System.out.println(list);
			System.out.println("Which creatures should attack? Give the number");
			Scanner in = new Scanner(System.in);
			while (in.hasNextInt()) {
				int targetInt = in.nextInt();
				if (targetInt > 0 && targetInt <= potentialAttackers.size()) {
					attackers.add(potentialAttackers.get(targetInt - 1));
				} else
					break;
			}

		}
		return attackers;
	}

	public ArrayList<ArrayList<CreatureCard>> chooseBlockers(ArrayList<CreatureCard> attackers, ArrayList<CreatureCard> potentialBlockers) {
		ArrayList<ArrayList<CreatureCard>> blockers = new ArrayList<>();
		ArrayList attacks = attackers;
		for (int i = 0; i < attackers.size(); i++) {
			blockers.add(new ArrayList<CreatureCard>());
		}
		int cnt = 0;
		while (!attacks.isEmpty() && !potentialBlockers.isEmpty()) {

			
			System.out.println("There is a total of " + attacks.size() + " more creatures to block. ");
			CreatureCard currentAttacker = (CreatureCard) attacks.remove(0);
			System.out.println("The current attacker is " + currentAttacker.getName() + ". Which creatures do you want to use to block this creature?");
			
			Scanner in = new Scanner(System.in);
			boolean done = false;
			while (potentialBlockers.size() > 0 && !done) {
				String list = "You have creatures: ";
			for (int i = 0; i < potentialBlockers.size(); i++) {
				list += i + 1 + ": " + potentialBlockers.get(i).getName() + ", ";
			}
			System.out.println(list);
				int targetInt = in.nextInt();
				if (targetInt > 0 && targetInt < potentialBlockers.size()) {
					blockers.get(cnt).add(potentialBlockers.remove(targetInt - 1));
				} else
					done = true;
			}
			cnt++;

		}
		return blockers;

	}

	public String getName() {
		return this.name;
	}

	public Player copy() {
		Player newPlayer = new HumanPlayer(this.name);

		return newPlayer;
	}
}
