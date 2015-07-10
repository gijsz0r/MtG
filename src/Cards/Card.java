package Cards;

public interface Card {
	
	//Type = 0 for land | 1 for creature
	public int getType();
	public boolean isTapped();
	public int getManaCost();
	public String getName();
	public Card copy();
}
