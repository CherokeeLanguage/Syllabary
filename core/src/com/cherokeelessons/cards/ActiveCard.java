package com.cherokeelessons.cards;


public class ActiveCard {

	public static final int SendToNextBoxThreshold = 3;
	
	public boolean sendToNextBox(){
		return correct_in_a_row>=SendToNextBoxThreshold;
	}

	public boolean noErrors = true;

	/**
	 * Used to match this card up with the master deck card
	 */
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
		if (obj == null || !(obj instanceof ActiveCard)) {
			return false;
		}
		ActiveCard other = (ActiveCard) obj;
		return answer.equals(other.answer);
	}
}
