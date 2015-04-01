package com.cherokeelessons.cards;

import java.util.ArrayList;
import java.util.List;

public class ActiveDeck {
	public ActiveDeck() {
	}
	public ActiveDeck(ActiveDeck activeDeck) {
		lastrun=activeDeck.lastrun;
		deck.clear();
		deck.addAll(activeDeck.deck);
	}
	public long lastrun=0;
	public List<ActiveCard> deck=new ArrayList<>();
}
