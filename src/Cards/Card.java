package Cards;

public abstract class Card {
	
	int manaCost;
	int power;
	int toughness;
	int type;
	boolean tapped;
	String name;
	
	//Type = 0 for land | 1 for creature
	public abstract int getType();
	public abstract boolean isTapped();
	public abstract int getManaCost();
	public abstract String getName();
	public abstract Card copy();
}
