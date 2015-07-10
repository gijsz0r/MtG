package Game;

import java.util.ArrayList;

import Cards.Card;
import Cards.CreatureCard;
import Cards.LandCard;

public class Hand {

	private ArrayList<Card> cardsInHand;
	
	public Hand() {
	this.cardsInHand = new ArrayList<Card>();
	}
	
	public void addCard(Card card) {
		this.cardsInHand.add(card);
	}
	public Card removeCard(Card card) {
		this.cardsInHand.remove(card);
		return card;
	}
	
	public ArrayList<Card> getCardsInHand() {
		return this.cardsInHand;
	}

	public boolean containsLand() {
		for (int i=0; i<cardsInHand.size(); i++) {
			if (cardsInHand.get(i).getType() == 0) {
				return true;
			}
		}
		return false;
	}
	public LandCard playLand() {
		for (int i=0; i<cardsInHand.size(); i++) {
			if (cardsInHand.get(i).getType() == 0) {
				return (LandCard) cardsInHand.remove(i);
			}
		}
		return null;
	}

	public boolean containsCreatures() {
		for (int i=0; i<cardsInHand.size(); i++) {
			if (cardsInHand.get(i).getType() == 1) {
				return true;
			}
		}
		return false;
	}

	public CreatureCard playBiggestCreature(int mana) {
		Card greatestCard = new CreatureCard("Null",0,0,0);
		int index = 0;
		for (int i=0; i<cardsInHand.size(); i++) {
			if (cardsInHand.get(i).getType() == 1) {
				if (((CreatureCard) cardsInHand.get(i)).getManaCost() > greatestCard.getManaCost() && cardsInHand.get(i).getManaCost() <= mana) {
					greatestCard = cardsInHand.get(i);
					index = i;
				}
			}
		}
		return (CreatureCard) cardsInHand.remove(index);
	}

	public int getSmallestManaCost() {
		int smallestMana = 999;
		for (int i=0; i<cardsInHand.size(); i++) {
			if (cardsInHand.get(i).getType() == 1) {
				if (cardsInHand.get(i).getManaCost() < smallestMana) {
					smallestMana = cardsInHand.get(i).getManaCost();
				}
			}
		}
		return smallestMana;
	}

	
	public Hand copy() {
		Hand newHand = new Hand();
		for (int i=0; i<cardsInHand.size();  i++) {
			newHand.addCard(cardsInHand.get(i).copy());
		}
		return newHand;
	}

	public int size() {
		return cardsInHand.size();
	}

	public void set(ArrayList<Card> arrayList) {
		cardsInHand = arrayList;
	}

	public ArrayList<CreatureCard> getCreatures() {
		ArrayList<CreatureCard> creatures = new ArrayList<CreatureCard>();
		for (int i=0; i<cardsInHand.size(); i++) {
			if (cardsInHand.get(i).getType() == 1) {
				creatures.add((CreatureCard) cardsInHand.get(i));
			}
		}
		return creatures; 
	}
	
	public int getNumberCreatures(){
		int creatures = 0;
		for (int i=0; i<cardsInHand.size(); i++) {
			if (cardsInHand.get(i).getType() == 1) {
				creatures++;
			}
		}
		return creatures; 
	}
}
