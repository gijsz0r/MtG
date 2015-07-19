package Game;

import AI.HumanPlayer;
import AI.MonteCarloPlayer;
import AI.RandomPlayer;

public class Main {

//	private static Integer[] initial_UCT = { 0, 1, 10, 100, 1000, 100000 };
//	private static double[] c_constant = { 0.01, 1, Math.sqrt(2), 2, Math.sqrt(10) };
	private static Integer[] initial_UCT = {10000000};
	private static double[] c_constant = {Math.sqrt(2)};
	private static int startingPlayer;

	public static void main(String args[]) {
		System.out.println("Hello World!");
		startingPlayer = 1;

		int[][] wins = new int[5][5];
		
		
		for (int i = 0; i < initial_UCT.length; i++) {
			
			for (int j = 0; j < c_constant.length; j++) {
				int p1wins = 0;
				int p2wins = 0;
				int cnt = 0;
				while (cnt < 100) {
					Game game = new Game(new RandomPlayer("Player 1"), new MonteCarloPlayer("Player 2"), startingPlayer,
							initial_UCT[i], c_constant[j]);
					int result = game.run();
					if (startingPlayer == 1) {
						startingPlayer = 2;
					} else if (startingPlayer == 2) {
						startingPlayer = 1;
					}
					if (result == 1) {
						p1wins++;
					} else if (result == 2) {
						p2wins++;
					}

					System.out.println("Game " + Math.addExact(cnt, 1) + " finished. UCT: " + initial_UCT[i] + " , c constant: " + c_constant[j] + ". The winner is player " + result
							+ " P1 wins: " + p1wins + "P2 wins: " + p2wins);
					
					cnt++;
				}
				wins[i][j] = p1wins;
				System.out.println("P1 wins: " + p1wins + " P2 wins: " + p2wins);

			}
		}
		
		for (int i=0; i<5; i++) {
			for (int j=0; j<5; j++) {
				System.out.println(wins[i][j]);
			}
		}


	}

}