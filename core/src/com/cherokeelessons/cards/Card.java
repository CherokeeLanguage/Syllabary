package com.cherokeelessons.cards;


public class Card implements Comparable<Card> {

	public static final int SendToNextBoxThreshold = 7;
	
	public boolean sendToNextBox(){
		return correct_in_a_row>=SendToNextBoxThreshold && noErrors;
	}
	
	public Card() {
	}
	
	public float showAgainDays(){
		return (10l*show_again_ms/(1000l*60l*60l*24l))/10f;
	}

	public Card(Card card) {
		this.answer = card.answer.intern();
		this.box=card.box;
		this.challenge = card.challenge.intern();
		this.correct_in_a_row=card.correct_in_a_row;
		this.id = card.id;
		this.newCard=card.newCard;
		this.noErrors=card.noErrors;
		this.show_again_ms=card.show_again_ms;
		this.showCount=card.showCount;
		this.showTime=card.showTime;
		this.tries_remaining=card.tries_remaining;
	}
	
	public boolean noErrors = true;

	public int id;
	public String challenge;
	public String answer;

	/**
	 * Is this a brand new card never seen before?
	 */
	public boolean newCard;
	/**
	 * How many tries left for this session?
	 */
	public int tries_remaining;
	/**
	 * How many times has it been correct in a row?
	 */
	public int correct_in_a_row;

	/**
	 * What proficiency box is this assigned to? This is used to select which
	 * SM2 "interval" is used to add for the "show again secs" value after a
	 * card is "retired" or "bumped"
	 */
	public int box;
	
	/**
	 * How long before this card should be tried again?
	 */
	public long show_again_ms;
	
	/**
	 * total elapsed time card has been displayed
	 */
	public float showTime=0f;
	/**
	 * total times user was challenged with card
	 */
	public int showCount=0;

	/**
	 * Two active cards are equal if both the pgroup and vgroup and the same.
	 * All other attributes are ignored.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Card)) {
			return false;
		}		
		return compareTo((Card)obj)==0;
	}
	
	@Override
	public int compareTo(Card o) {
		return sortKey().compareTo(o.sortKey());
	}

	private String sortKey() {
		StringBuilder key = new StringBuilder();
		key.append(answer);
		key.append("+");
		key.append(challenge);
		return key.toString().intern();
	}

	public void reset() {
		correct_in_a_row=0;
		noErrors=true;
		showCount=0;
		showTime=0f;
		tries_remaining = Card.SendToNextBoxThreshold;
	}
}
