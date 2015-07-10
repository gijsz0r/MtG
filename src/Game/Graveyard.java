package Game;

import java.util.ArrayList;

import Cards.Card;

public class Graveyard {

	private ArrayList<Card> graveyard = new ArrayList<Card>();

	public Graveyard() {
		// graveyard = new ArrayList<Card>();
	}

	public void add(Card card) {
		this.graveyard.add(card);
	}

	public ArrayList<Card> getCards() {
		return graveyard;
	}

	public Graveyard copy() {
		Graveyard newGraveyard = new Graveyard();
		if (!this.graveyard.isEmpty()) {
//			System.out.println("Size of graveyard : " + this.graveyard.size());
			for (int i = 0; i < this.graveyard.size(); i++) {
				// System.out.println(i + " " +
				// this.graveyard.get(i).getName());
//				System.out.println("Card number : " + i);
				if (this.graveyard.get(i) != null) {
					Card copyCard = this.graveyard.get(i).copy();

					newGraveyard.add(copyCard);
				}
			}
		}
		return newGraveyard;
	}

}
