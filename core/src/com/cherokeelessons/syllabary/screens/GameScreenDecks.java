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
			if (card.newCard) {
				continue;
			}
			letter = (char) Math.max(card.answer.charAt(0), letter);
		}
		for (Card card: this.finished.cards) {
			if (card.newCard) {
				continue;
			}
			letter = (char) Math.max(card.answer.charAt(0), letter);
		}
		for (Card card: this.pending.cards) {
			if (card.newCard) {
				continue;
			}
			letter = (char) Math.max(card.answer.charAt(0), letter);
		}
		for (Card card: this.reserved.cards) {
			if (card.newCard) {
				continue;
			}
			letter = (char) Math.max(card.answer.charAt(0), letter);
		}
		return letter;
	}
	/**
	 * Remove this card from all decks.
	 * @param card
	 */
	public void remove(Card card) {
		this.discards.cards.remove(card);
		this.finished.cards.remove(card);
		this.pending.cards.remove(card);
		this.reserved.cards.remove(card);
	}
}