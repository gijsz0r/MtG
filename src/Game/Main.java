package Game;

import AI.HumanPlayer;
import AI.MonteCarloPlayer;
import AI.RandomPlayer;

public class Main {

	private static int startingPlayer;

	public static void main(String args[]) {
		System.out.println("Hello World!");
		startingPlayer = 1;

		int p1wins = 0;
		int p2wins = 0;
		int cnt = 0;
		while (cnt < 1000) {
			Game game = new Game(new MonteCarloPlayer("Player 1"), new RandomPlayer("Player 2"), startingPlayer);
			int result = game.run();
			if (startingPlayer == 1) {
				startingPlayer = 2;
			}
			else if (startingPlayer == 2) {
				startingPlayer = 1;
			}
			if (result == 1) {
				p1wins++;
			} else if (result == 2) {
				p2wins++;
			}
			
			System.out.println("Game " + Math.addExact(cnt,1) + " finished. The winner is player " + result + " P1 wins: " + p1wins + "P2 wins: " + p2wins);
			cnt++;
		}

		System.out.println("P1 wins: " + p1wins + " P2 wins: " + p2wins);
		
	}

}