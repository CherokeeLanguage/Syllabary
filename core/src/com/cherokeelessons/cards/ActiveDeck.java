package com.cherokeelessons.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class ActiveDeck {
	private static final long ONE_MINUTE_ms = 60l*1000l;

	public ActiveDeck() {
	}
	public ActiveDeck(ActiveDeck activeDeck) {
		lastrun=activeDeck.lastrun;
		deck.clear();
		deck.addAll(activeDeck.deck);
	}
	public long lastrun=0;
	public List<ActiveCard> deck=new ArrayList<>();
	
	/**
	 * time-shift all cards by time since last recorded run.
	 * 
	 * @param currentDeck
	 */
	public void updateTime(long ms) {
		Iterator<ActiveCard> istat = deck.iterator();
		while (istat.hasNext()) {
			ActiveCard next = istat.next();
			next.show_again_ms -= ms;
		}
	}
	
	public void sortByShowTimeChunks() {
		Collections.sort(deck, byShowTimeChunks);
	}
	
	public void sortByShowTime() {
		Collections.sort(deck, byShowTime);
	}
	
	private static Comparator<ActiveCard> byShowTimeChunks = new Comparator<ActiveCard>() {
		@Override
		public int compare(ActiveCard o1, ActiveCard o2) {
			long dif = o1.show_again_ms - o2.show_again_ms;
			if (dif < 0)
				dif = -dif;
			if (dif < ONE_MINUTE_ms) {
				return 0;
			}
			return o1.show_again_ms > o2.show_again_ms ? 1 : -1;
		}
	};
	
	private static Comparator<ActiveCard> byShowTime = new Comparator<ActiveCard>() {
		@Override
		public int compare(ActiveCard o1, ActiveCard o2) {
			if (o1.show_again_ms != o2.show_again_ms) {
				return o1.show_again_ms > o2.show_again_ms ? 1 : -1;
			}
			return o1.box - o2.box;
		}
	};

	public void shuffle() {
		Collections.shuffle(deck);
	}
	public void clampToMinutes() {
		for (ActiveCard card : deck) {
			card.show_again_ms -= (card.show_again_ms % ONE_MINUTE_ms);
		}
	}
	public void clampBoxes() {
		for (ActiveCard card : deck) {
			if (card.box < 0) {
				card.box = 0;
				continue;
			}
		}		
	}
	public void resetErrorMarker() {
		for (ActiveCard card : deck) {
			card.noErrors = true;
		}		
	}
	public void resetRetriesCount() {
		for (ActiveCard card : deck) {
			card.tries_remaining = ActiveCard.SendToNextBoxThreshold;
		}		
	}
	public void resetCorrectInARow() {
		for (ActiveCard card : deck) {
			card.correct_in_a_row=0;
		}	
	}
	public void resetScoring() {
		for (ActiveCard card : deck) {
			card.showCount = 0;
			card.showTime = 0f;
		}		
	}
}
