package com.cherokeelessons.cards;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SlotInfo implements Serializable {
	
	public int slot=0;
	
	public static final int FULLY_LEARNED_BOX = 10;
	public static final int JUST_LEARNED_BOX = 1;
	public static final int PROFICIENT_BOX = 5;
	private static final int StatsVersion = 3;
	public static void calculateStatsFor(SlotInfo info) {
		Deck activeDeck = info.deck;
		/*
		 * Set "level" to ceil(average box value) found in active deck. Negative
		 * box values are ignored.
		 */

		int boxsum = 0;
		for (Card card : activeDeck.cards) {
			boxsum += (card.box > 0 ? card.box : 0);
		}
		info.level = LevelName.forLevel((int) Math.ceil((double) (boxsum)
				/ (double) activeDeck.cards.size()));

		/*
		 * How many are "fully learned" out of the active deck?
		 */

		info.longTerm = 0;
		for (Card card : activeDeck.cards) {
			if (card.box >= FULLY_LEARNED_BOX) {
				info.longTerm++;
			}
		}

		/*
		 * count all active cards that aren't "fully learned"
		 */
		info.activeCards = activeDeck.cards.size() - info.longTerm;

		/*
		 * How many are "well known" out of the active deck? (excluding full
		 * learned ones)
		 */
		info.mediumTerm = 0;
		for (Card card : activeDeck.cards) {
			if (card.box >= PROFICIENT_BOX && card.box < FULLY_LEARNED_BOX) {
				info.mediumTerm++;
			}
		}

		/*
		 * How many are "short term known" out of the active deck? (excluding
		 * full learned ones)
		 */
		info.shortTerm = 0;
		for (Card card : activeDeck.cards) {
			if (card.box >= JUST_LEARNED_BOX && card.box < PROFICIENT_BOX) {
				info.shortTerm++;
			}
		}
	}
	public int activeCards = 0;
	public Deck deck=new Deck();
	public long lastrun;
	public int lastScore;
	public LevelName level;
	public int longTerm = 0;
	public int mediumTerm = 0;
	public boolean perfect;
	public Settings settings = new Settings();
	public int shortTerm = 0;
	public String signature = "";

	private int version;

	private float elapsed_secs;

	public SlotInfo() {
	}

	public SlotInfo(SlotInfo info) {
		this.activeCards = info.activeCards;
		this.deck=new Deck(info.deck);
		this.lastrun=info.lastrun;
		this.lastScore=info.lastScore;
		this.level = info.level;
		this.longTerm = info.longTerm;
		this.mediumTerm = info.mediumTerm;
		this.settings = new Settings(info.settings);
		this.perfect=info.perfect;
		this.shortTerm=info.shortTerm;
		this.signature=info.signature.intern();
	}

	public int getVersion() {
		return StatsVersion;
	}

	public void recalculateStats() {
		calculateStatsFor(this);
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public boolean isUpdatedVersion() {
		return version == StatsVersion;
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

	public float getElapsed_secs() {
		return elapsed_secs;
	}

	public void setElapsed_secs(float elapsed_secs) {
		this.elapsed_secs = elapsed_secs;
	}
}
