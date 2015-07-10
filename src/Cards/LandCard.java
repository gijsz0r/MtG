package Cards;


public class LandCard implements Card{

	private int manaCost;
	private int power;
	private int toughness;
	private int type;
	private boolean tapped;
	private String name;
	
	public LandCard() {
		this.manaCost = 0;
		this.power = 0;
		this.toughness = 0;
		this.type = 0;
		this.tapped = false;
		this.name = "Mana";
	}
	
	
	@Override
	public int getType() {
		return this.type;
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
	public int getManaCost() {
		return this.manaCost;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public Card copy() {
		LandCard newCard = new LandCard();
		if (this.isTapped()) {
			newCard.tap();
		}
		return newCard;
	}
}
