package com.cherokeelessons.cards;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SlotInfo implements Serializable {
	private String signature = "";

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	private static final int StatsVersion = 1;
	public static final int FULLY_LEARNED_BOX = 10;
	public static final int PROFICIENT_BOX = 5;
	public static final int JUST_LEARNED_BOX = 1;

	/**
	 * The summed "box" values for all active cards
	 */
	public int fullScore = 0;
	/**
	 * The summbed "box" values for the most recent learning session
	 */
	public int sessionScore = 0;

	public int activeCards = 0;
	public int shortTerm = 0;
	public int mediumTerm = 0;
	public int longTerm = 0;
	public Settings settings = new Settings();
	private int version;
	public LevelName level;
	public int lastScore;
	public boolean perfect;
	public long lastrun;

	public SlotInfo() {
	}

	public SlotInfo(SlotInfo info) {
		this.activeCards = info.activeCards;
		this.level = info.level;
		this.longTerm = info.longTerm;
		this.mediumTerm = info.mediumTerm;
		this.settings = new Settings(info.settings);

	}

	public void validate() {
		if (level == null) {
			level = LevelName.Newbie;
		}
		if (settings == null) {
			settings = new Settings();
		}
		settings.validate();
	}

	public int getVersion() {
		return StatsVersion;
	}

	public boolean updatedVersion() {
		return version == StatsVersion;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public static void calculateStats(SlotInfo info, ActiveDeck activeDeck) {
		if (activeDeck == null || info == null || activeDeck.deck.size() == 0) {
			return;
		}

		/*
		 * Set "level" to ceil(average box value) found in active deck. Negative
		 * box values are ignored.
		 */

		int boxsum = 0;
		for (ActiveCard card : activeDeck.deck) {
			boxsum += (card.box > 0 ? card.box : 0);
		}
		info.level = LevelName.forLevel((int) Math.ceil((double) (boxsum)
				/ (double) activeDeck.deck.size()));

		/*
		 * How many are "fully learned" out of the active deck?
		 */

		info.longTerm = 0;
		for (ActiveCard card : activeDeck.deck) {
			if (card.box >= FULLY_LEARNED_BOX) {
				info.longTerm++;
			}
		}

		/*
		 * count all active cards that aren't "fully learned"
		 */
		info.activeCards = activeDeck.deck.size() - info.longTerm;

		/*
		 * How many are "well known" out of the active deck? (excluding full
		 * learned ones)
		 */
		info.mediumTerm = 0;
		for (ActiveCard card : activeDeck.deck) {
			if (card.box >= PROFICIENT_BOX && card.box < FULLY_LEARNED_BOX) {
				info.mediumTerm++;
			}
		}

		/*
		 * How many are "short term known" out of the active deck? (excluding
		 * full learned ones)
		 */
		info.shortTerm = 0;
		for (ActiveCard card : activeDeck.deck) {
			if (card.box >= JUST_LEARNED_BOX && card.box < PROFICIENT_BOX) {
				info.shortTerm++;
			}
		}
	}
}
