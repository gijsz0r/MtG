package Game;

import java.util.ArrayList;
import java.util.Collections;

import Cards.Card;
import Cards.CreatureCard;
import Cards.LandCard;

public class Deck {

	private ArrayList<Card> deck;
	
	public Deck(){
 		this.deck = new ArrayList<Card>();
		
		int n1 = 0;
		int n2 = 0;
		while (n1<4) {
			deck.add(new CreatureCard("Elf",1, 1, 1));
			deck.add(new CreatureCard("Bear", 2, 2, 2));
			deck.add(new CreatureCard("Goblin",3,4,2));
			deck.add(new CreatureCard("Cyclops",4, 5, 3));
			deck.add(new CreatureCard("Giant",5,3,6));
			deck.add(new CreatureCard("Dragon",6,6,4));

			n1++;
		}
		while (n2<17) {
			deck.add(new LandCard());
			n2++;
				}
		
		this.shuffle();
		
	}
	
	public void emptyDeck() {
		this.deck = new ArrayList<Card>();
	}
	
	public void shuffle() {
		Collections.shuffle(deck);
	}

	public int getSize() {
		return this.deck.size();
	}
	public void add(Card card) {
		this.deck.add(card);
	}
	
	public Card draw() {
		if (deck.size() > 0) {
		return this.deck.remove(0);
		}
		else return null;
	}
	
	
	public Deck copy() {
		Deck newDeck = new Deck();
		newDeck.emptyDeck();
		for (int i=0; i<this.deck.size(); i++) {
			newDeck.add(deck.get(i).copy());
		}
		return newDeck;
	}

	public Card remove(int nextInt) {
		return deck.remove(nextInt);
	}
}
