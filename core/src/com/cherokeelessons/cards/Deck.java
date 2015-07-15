package com.cherokeelessons.cards;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.cherokeelessons.syllabary.one.App;

public class Deck implements Serializable {
	private static Comparator<Card> byShowTime = new Comparator<Card>() {
		@Override
		public int compare(Card o1, Card o2) {
			long a = o1.show_again_ms;
			long b = o2.show_again_ms;
			if (a != b) {
				return a > b ? 1 : -1;
			}
			return o1.box - o2.box;
		}
	};
	private static Comparator<Card> byShowTimeMinutes = new Comparator<Card>() {
		@Override
		public int compare(Card o1, Card o2) {
			long a = o1.show_again_ms / ONE_MINUTE_ms;
			long b = o2.show_again_ms / ONE_MINUTE_ms;
			if (a != b) {
				return a > b ? 1 : -1;
			}
			return o1.box - o2.box;
		}
	};
	private static final long ONE_MINUTE_ms = 60l * 1000l;

	private static final List<Long> pimsleur_intervals = new ArrayList<Long>();
	private static final long serialVersionUID = 1L;

	private static final List<Long> sm2_intervals = new ArrayList<Long>();

	static {
		/*
		 * for Pimsleur gaps
		 */
		long ms = 1000l;
		for (int i = 0; i < 15; i++) {
			ms *= 5l;
			pimsleur_intervals.add(ms);
		}
		/*
		 * for SM2 gaps
		 */
		long ms_day = 1000l * 60l * 60l * 24;
		float days = 4f;
		sm2_intervals.add(ms_day);
		for (int i = 0; i < 15; i++) {
			sm2_intervals.add((long) (ms_day * days));
			days *= 1.7f;
		}
	}

	/**
	 * Pimsleur staggered intervals (powers of 5) seconds as ms
	 * 
	 * @param correct_in_a_row
	 * @return
	 */
	public static long getNextInterval(int correct_in_a_row) {
		if (correct_in_a_row < 0) {
			correct_in_a_row = 0;
		}
		if (correct_in_a_row > pimsleur_intervals.size() - 1) {
			correct_in_a_row = pimsleur_intervals.size() - 1;
		}
		return pimsleur_intervals.get(correct_in_a_row);
	}

	/**
	 * SM2 staggered intervals (powers of 1.7) days as ms
	 * 
	 * @param box
	 * @return
	 */
	public static long getNextSessionInterval(int box) {
		if (box >= sm2_intervals.size()) {
			box = sm2_intervals.size() - 1;
		}
		if (box < 0) {
			box = 0;
		}
		return sm2_intervals.get(box);
	}

	public final List<Card> cards = new ArrayList<>();
	public long lastrun = 0;
	private long version = -1;

	public Deck() {
	}

	public Deck(Deck deck) {
		this.cards.addAll(deck.cards);
		this.lastrun = deck.lastrun;
		this.version = serialVersionUID;
	}

	public void clampBoxes() {
		for (Card card : cards) {
			if (card.box < 0) {
				card.box = 0;
				continue;
			}
		}
	}

	public void clampToMinutes() {
		for (Card card : cards) {
			card.show_again_ms -= (card.show_again_ms % ONE_MINUTE_ms);
		}
	}

	public long getVersion() {
		return serialVersionUID;
	}

	public boolean isUpdatedVersion() {
		return version == serialVersionUID;
	}

	public void resetCorrectInARow() {
		for (Card card : cards) {
			card.correct_in_a_row = 0;
		}
	}

	public void resetErrorMarker() {
		for (Card card : cards) {
			card.noErrors = true;
		}
	}

	public void resetRetriesCount() {
		for (Card card : cards) {
			card.tries_remaining = Card.SendToNextBoxThreshold-card.box*3;
			if (card.tries_remaining<1) {
				card.tries_remaining=1;
			}
		}
	}

	public void resetScoring() {
		for (Card card : cards) {
			card.showCount = 0;
			card.showTime = 0f;
		}
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public void shuffle() {
		Collections.shuffle(cards);
	}

	public void sortByShowTimeMinutes() {
		Collections.sort(cards, byShowTimeMinutes);
	}

	public void sortByShowTime() {
		Collections.sort(cards, byShowTime);
	}

	/**
	 * time-shift all cards by time since last recorded run.
	 * 
	 * @param currentDeck
	 */
	public void updateTime(long ms) {
		Iterator<Card> istat = cards.iterator();
		while (istat.hasNext()) {
			Card next = istat.next();
			next.show_again_ms -= ms;
		}
	}

	public void updateTime(float secs) {
		long ms = (long) (secs * 1000f);
		Iterator<Card> istat = cards.iterator();
		while (istat.hasNext()) {
			Card next = istat.next();
			next.show_again_ms -= ms;
			if (next.show_again_ms < 0l) {
				next.show_again_ms = 0l;
			}
		}
	}

	/**
	 * Calculates amount of ms needed to shift by to move deck to "0" point.
	 * 
	 * @param deck
	 * @return
	 */
	public long getMinShiftTimeOf() {
		if (cards.size() == 0) {
			return 0l;
		}
		long by = Long.MAX_VALUE;
		Iterator<Card> icard = cards.iterator();
		while (icard.hasNext()) {
			Card card = icard.next();
			if (card.tries_remaining < 1) {
				continue;
			}
			if (by > card.show_again_ms) {
				by = card.show_again_ms;
			}
		}
		if (by == Long.MAX_VALUE) {
			by = ONE_MINUTE_ms;
		}
		return by;
	}

	/**
	 * Moves all cards from source deck into this deck.
	 * 
	 * @param source
	 */
	public void loadAll(Deck source) {
		this.cards.addAll(source.cards);
		source.cards.clear();
	}

	/**
	 * Return count of cards in this deck.
	 * 
	 * @return
	 */
	public int size() {
		return cards.size();
	}

	/**
	 * Look for previous cards that are in box 0 and that show errors and mark
	 * them as new to trigger retraining.
	 */
	public void resetNewFlag() {
		for (Card card : cards) {
			if (card.box > 0) {
				continue;
			}
			if (card.noErrors == true) {
				continue;
			}
			if (card.correct_in_a_row > 3) {
				continue;
			}
			card.newCard = true;
			App.log(this, "Retraining for: '"+card.answer+"'");
		}
	}
}
