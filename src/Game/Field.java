package Game;

import java.util.ArrayList;

import Cards.CreatureCard;
import Cards.LandCard;

public class Field {
	ArrayList<LandCard> lands;
	ArrayList<CreatureCard> creatures;

	
	public Field(){
		this.lands = new ArrayList<LandCard>();
		this.creatures = new ArrayList<CreatureCard>();
	}
	
	public void playLand(LandCard land){
		//System.out.println("Land added!  " +land.getName());
		lands.add(land);
	}
	public void playCreature(CreatureCard creature) {
		creatures.add(creature);
	}
	public ArrayList<LandCard> getLands() {
		return this.lands;
	}
	public ArrayList<CreatureCard> getCreatures() {
		return this.creatures;
	}
	
	public void untapAllPermanents() {
		for (int i=0; i<this.lands.size(); i++) {
			lands.get(i).untap();
		}
		for (int i=0; i<this.creatures.size(); i++) {
			creatures.get(i).untap();
		}
	}
	public void tapLands(int amount) {
		for (int i=0; i<amount; i++) {
			lands.get(i).tap();
		}
	}

	public int getUntappedLands() {
		//System.out.println("Total lands: "+this.lands.size());
		int cnt = 0;
		for (int i=0; i<this.lands.size(); i++) {
			if (!lands.get(i).isTapped()) {
				cnt++;
			}
		}
		return cnt;
	}
	
	public CreatureCard destroyCreature(CreatureCard creature) {
		for (int i=0; i<creatures.size(); i++) {
			if (creatures.get(i).getName() == creature.getName()) {
				return creatures.remove(i);
			}
		}
		return null;
	}
	public Field copy() {
		Field newField = new Field();
		for (int i=0; i<this.creatures.size(); i++) {
			newField.playCreature((CreatureCard) this.creatures.get(i).copy());
		}
		for (int i=0; i<this.lands.size(); i++) {
			newField.playLand((LandCard) this.lands.get(i).copy());
		}
		return newField;
	}
}
