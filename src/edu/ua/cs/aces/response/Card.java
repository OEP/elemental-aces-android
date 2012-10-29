package edu.ua.cs.aces.response;

public class Card {
	
	public static final String
		TYPE_ELEMENT = "ELEMENT",
		TYPE_MAGIC = "MAGIC";
	
	public int id;
	public String name;
	public String description;
	public String cardType = Card.TYPE_ELEMENT;
	
	private Card(int id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}
	
	private boolean __Bumped = false;

	public boolean isBumped() {
		return __Bumped;
	}
	
	public void toggleBumped() {
		__Bumped = !__Bumped;
	}
	
	public void setBumped(boolean b) {
		__Bumped = b;
	}
	
	public String toString() {
		return (name != null) ? name : "";
	}
	
	public static final Card EMPTY_CARD = new Card(-1, null, null);
}
