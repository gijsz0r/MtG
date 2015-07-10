package Cards;


public class CreatureCard implements Card{

	private int manaCost;
	private int power;
	private int toughness;
	private int type;
	private boolean tapped;
	private String name;
	
	public CreatureCard(String name, int manaCost, int power, int toughness) {
		this.name = name;
		this.manaCost = manaCost;
		this.power = power;
		this.toughness = toughness;
		this.type = 1;
		this.tapped = false;
		
	}
	
	
	@Override
	public int getType() {
		return this.type;
	}

	public int getManaCost() {
		return this.manaCost;
	}
	public int getPower() {
		return this.power;
	}
	public int getToughness() {
		return this.toughness;
	}
	
	public boolean isTapped() {
		return this.tapped;
	}
	public void untap() {
		this.tapped = false;
	}
	public void tap() {
		this.tapped = true;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public Card copy() {
		CreatureCard newCard = new CreatureCard(this.name, this.manaCost, this.power, this.toughness);
		if (this.isTapped()) {
			newCard.tap();
		}
		return newCard;
	}
}
