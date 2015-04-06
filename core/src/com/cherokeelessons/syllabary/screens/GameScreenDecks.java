package com.cherokeelessons.syllabary.screens;

import com.cherokeelessons.cards.Card;
import com.cherokeelessons.cards.Deck;

public class GameScreenDecks {
	/**
	 * master copy of all cards
	 */
	public Deck master = null;
	/**
	 * the deck we are starting out with
	 */
	// public Deck starting = null;
	/**
	 * currently being looped through for display
	 */
	public final Deck pending = new Deck();
	/**
	 * holding area for cards that have just been displayed
	 */
	public final Deck discards = new Deck();
	/**
	 * holding area for cards with no more scheduled showings for this
	 * session
	 */
	public final Deck finished = new Deck();
	/**
	 * Extra cards from the discard stack that aren't active this display
	 * cycle yet ...
	 */
	public final Deck reserved = new Deck();
	public char getLastLetter() {
		char letter = 'Ꭰ';
		for (Card card: this.discards.cards) {
			letter = (char) Math.max(card.answer.charAt(0), letter);
		}
		for (Card card: this.finished.cards) {
			letter = (char) Math.max(card.answer.charAt(0), letter);
		}
		for (Card card: this.pending.cards) {
			letter = (char) Math.max(card.answer.charAt(0), letter);
		}
		for (Card card: this.reserved.cards) {
			letter = (char) Math.max(card.answer.charAt(0), letter);
		}
		return letter;
	}
}