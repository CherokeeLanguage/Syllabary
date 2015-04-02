package com.cherokeelessons.syllabary.screens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;
import com.cherokeelessons.cards.ActiveCard;
import com.cherokeelessons.cards.ActiveDeck;
import com.cherokeelessons.cards.Card;
import com.cherokeelessons.cards.Deck;
import com.cherokeelessons.cards.SlotInfo;
import com.cherokeelessons.syllabary.one.App;
import com.cherokeelessons.syllabary.one.Syllabary;
import com.cherokeelessons.ui.GameBoard;
import com.cherokeelessons.ui.UI;

public class GameScreen extends ChildScreen {
	
	private static final float BOARD_TICK = 120f;
	private static final long ONE_DAY_ms;
	private static final long ONE_HOUR_ms;
	private static final long ONE_MINUTE_ms;
	private static final long ONE_SECOND_ms;
	static {
		ONE_SECOND_ms = 1000l;
		ONE_MINUTE_ms = 60l * ONE_SECOND_ms;
		ONE_HOUR_ms = 60l * ONE_MINUTE_ms;
		ONE_DAY_ms = 24l * ONE_HOUR_ms;
	}

	private float challenge_elapsed = 0f;

	/**
	 * currently being looped through for display
	 */
	private final ActiveDeck current_active = new ActiveDeck();
	/**
	 * holding area for cards that have just been displayed or are not scheduled
	 * yet for display
	 */
	private final ActiveDeck current_discards = new ActiveDeck();
	private final ActiveDeck current_done = new ActiveDeck();

	/**
	 * holding area for cards that are "due" but deck size says don't show yet
	 */
	private final ActiveDeck current_due = new ActiveDeck();
	private Deck deck;

	private GameBoard gameboard;

	private SlotInfo info;
	
	private Runnable loadDeck = new Runnable() {
		@Override
		public void run() {
			deck = Syllabary.getDeck();
			info = App.getSlotInfo(slot);
			
			current_due.deck = new ArrayList<ActiveCard>(info.activeDeck.deck);
			current_due.lastrun=info.activeDeck.lastrun;
			current_due.updateTime(ONE_DAY_ms + ONE_HOUR_ms);
			/*
			 * Make sure we don't have active cards pointing to no longer
			 * existing master deck cards
			 */
			Iterator<ActiveCard> ipending = current_due.deck.iterator();
			while (ipending.hasNext()) {
				ActiveCard active = ipending.next();
				if (deck.getCardByAnswer(active.answer) != null) {
					continue;
				}
				ipending.remove();
				App.log(this, "Removed no longer valid entry: " + active.answer);
			}
			current_due.resetScoring();
			current_due.resetCorrectInARow();
			current_due.resetRetriesCount();
			current_due.resetErrorMarker();
			current_due.clampBoxes();
			current_due.clampToMinutes();
			current_due.shuffle();
			current_due.sortByShowTimeChunks();
			recordAlreadySeen(current_due);
			retireNotYetCards(current_due);
			current_due.deck.add(nextCardForUse());
			
			Gdx.app.postRunnable(startGame);
		}
	};
	
	private float maxTime = 10f;
	
	private final Set<String> nodupes = new HashSet<String>();
	
	private boolean ready = false;
	/**
	 * time since last "shuffle"
	 */
	private float sinceLastNextCard_elapsed = 0f;
	final int slot;
	
	private ActiveCard activeCard;
	
	private RunnableAction updateBoard = new RunnableAction(){
		public void run() {
			App.log(this, "updateBoard");
			for (int iy = 0; iy < GameBoard.height; iy++) {
				for (int ix = 0; ix < GameBoard.width; ix++) {
					final String img = UI.DISC;
					Color img_color;
					do {
						img_color = new Color(new Random().nextFloat(),
								new Random().nextFloat(), new Random().nextFloat(),
								1f);
					} while (UI.luminance(img_color) < .6);
					gameboard.setImageAt(ix, iy, img);
					gameboard.setColorAt(ix, iy, img_color);
				}
			}
			gameboard.setImageAt(0, 0, UI.IMG_SYNC);
			gameboard.setChallenge_latin(deck.getCardByAnswer(activeCard.answer).challenge);
		};
	};
	
	private RunnableAction nextCard = new RunnableAction(){
		private ActiveCard previousCard;
		public void run() {
			App.log(this, "nextCard");
			do {
				activeCard = getNextCard();
				if (activeCard == null) {
					break;
				}
				if (!activeCard.equals(previousCard)) {
					break;
				}
				if (current_discards.deck.size() == 0
						&& current_active.deck.size() == 0) {
					break;
				}
				activeCard.show_again_ms = Deck.getNextInterval(activeCard
						.correct_in_a_row + 1);
				previousCard = null;
			} while (true);
			if (activeCard == null) {
				App.log(this, "activeCard==null");
				if (total_elapsed < BOARD_TICK) {
					App.log(this, "session time is not up");
					if (current_discards.deck.size()+current_active.deck.size()==0) {
						ActiveCard newCard = nextCardForUse();
						App.log(this, "adding card to deck: "+newCard.answer);
						current_active.deck.add(newCard);
						stage.addAction(nextCard);
						return;
					}
					long shift_by_ms = getMinShiftTimeOf(current_discards);
					App.log(this, "shifting discards to zero point: "
							+ (shift_by_ms / ONE_SECOND_ms));
					if (shift_by_ms >= 15l * ONE_SECOND_ms) {
						current_active.deck.add(nextCardForUse());
					}
					current_discards.updateTime(shift_by_ms);
					stage.addAction(nextCard);
					return;
				}
				/*
				 * Session time is up, force time shift cards into active show
				 * range...
				 */
				if (total_elapsed > BOARD_TICK && current_discards.deck.size() > 0) {
					long shift_by_ms = getMinShiftTimeOf(current_discards);
					App.log(this, "shifting discards to zero point: "
							+ (shift_by_ms / ONE_SECOND_ms));
					current_discards.updateTime(shift_by_ms);
					stage.addAction(nextCard);
					return;
				}
				if (total_elapsed > BOARD_TICK) {
					ready=false;
					App.log(this, "no cards remaining");
					info.activeDeck.deck.clear();
					info.activeDeck.deck.addAll(current_active.deck);
					info.activeDeck.deck.addAll(current_due.deck);
					info.activeDeck.deck.addAll(current_discards.deck);
					info.activeDeck.deck.addAll(current_done.deck);
					info.setElapsed_secs(total_elapsed);
					info.lastrun=System.currentTimeMillis();
					App.saveSlotInfo(slot, info);
					ui.getReadyDialog(new Runnable() {
						@Override
						public void run() {
							goodBye();
						}
					});
					return;
				}
			} else {
				stage.addAction(updateBoard);
			}
		};
	};

	/**
	 * Calculates amount of ms needed to shift by to move deck to "0" point.
	 * 
	 * @param current_pending
	 * @return
	 */
	private long getMinShiftTimeOf(ActiveDeck current_pending) {
		if (current_pending.deck.size() == 0) {
			return 0;
		}
		long by = Long.MAX_VALUE;
		Iterator<ActiveCard> icard = current_pending.deck.iterator();
		while (icard.hasNext()) {
			ActiveCard card = icard.next();
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
	
	private Runnable startGame = new Runnable() {
		@Override
		public void run() {
			ready = true;
			stage.addAction(nextCard);
		}
	};

	private float total_elapsed = 0f;

	private float update_prev = 0f;

	public GameScreen(Screen caller, int slot) {
		super(caller);
		this.slot = slot;
	}
	
	private void reInsertCard(ActiveCard card) {
		if (current_active.deck.size() < 2) {
			current_active.deck.add(card);
		} else {
			current_active.deck.add(1, card);
		}
		current_discards.deck.remove(card);
	}

	@Override
	public void act(float delta) {
		if (gameboard.isPaused()) {
			return;
		}
		if (gameboard.isDoElapsed() && ready) {
			total_elapsed += delta;
			challenge_elapsed += delta;
			sinceLastNextCard_elapsed += delta;
			if (challenge_elapsed - update_prev > 1f) {
				while (challenge_elapsed - update_prev > 1f) {
					update_prev += 1f;
				}
				gameboard.setRemaining(challenge_elapsed / maxTime, 1f);
			}
		}
		super.act(delta);
	}

	private ActiveCard getNextCard() {
		if (current_active.deck.size() == 0) {
			current_discards.updateTime((long)(sinceLastNextCard_elapsed*1000f));
			sinceLastNextCard_elapsed = 0;
			Iterator<ActiveCard> itmp = current_discards.deck.iterator();
			while (itmp.hasNext()) {
				ActiveCard tmp = itmp.next();
				if (tmp.noErrors && tmp.sendToNextBox()) {
					tmp.box++;
					current_done.deck.add(tmp);
					tmp.show_again_ms = tmp.show_again_ms
							+ Deck.getNextSessionInterval(tmp.box);
					itmp.remove();
					App.log(this, "Bumped Card: " + tmp.answer);
					return getNextCard();
				}
				if (tmp.tries_remaining < 0) {
					tmp.box--;
					current_done.deck.add(tmp);
					tmp.show_again_ms = tmp.show_again_ms
							+ Deck.getNextSessionInterval(tmp.box);
					itmp.remove();
					App.log(this, "Retired Card: " + tmp.answer);
					return getNextCard();
				}
				if (tmp.show_again_ms > 0) {
					continue;
				}
				current_active.deck.add(tmp);
				itmp.remove();
			}
			if (current_active.deck.size() == 0) {
				return null;
			}
			Collections.shuffle(current_active.deck);
			current_active.sortByShowTimeChunks();
		}
		ActiveCard card = current_active.deck.get(0);
		current_active.deck.remove(0);
		current_discards.deck.add(card);
		return card;
	}
	@Override
	public void hide() {
		super.hide();
	}
	private ActiveCard nextCardForUse() {
		/**
		 * look for previous cards to load first, if their delay time is up
		 */
		Iterator<ActiveCard> ipending = current_due.deck.iterator();
		while (ipending.hasNext()) {
			ActiveCard next = ipending.next();
			if (next.box >= SlotInfo.FULLY_LEARNED_BOX) {
				continue;
			}
			if (next.show_again_ms > 0) {
				continue;
			}
			ipending.remove();
			return next;
		}

		/**
		 * not enough already seen cards, add new never seen cards
		 */
		Iterator<Card> ideck = deck.cards.iterator();
		while (ideck.hasNext()) {
			Card next = ideck.next();
			if (nodupes.contains(next.answer)) {
				continue;
			}
			ActiveCard activeCard = new ActiveCard();
			activeCard.box = 0;
			activeCard.noErrors = true;
			activeCard.newCard = true;
			activeCard.answer=next.answer;
			activeCard.show_again_ms = 0;
			activeCard.correct_in_a_row=0;
			activeCard.tries_remaining = ActiveCard.SendToNextBoxThreshold;
			nodupes.add(activeCard.answer);
			return activeCard;
		}
		/**
		 * yikes! They processed ALL the cards!
		 */
		ipending = current_due.deck.iterator();
		while (ipending.hasNext()) {
			ActiveCard next = ipending.next();
			ipending.remove();
			return next;
		}
		return current_done.deck.iterator().next();
	}
	/**
	 * record all cards currently "in-play" so that when cards are retrieved
	 * from the master deck they are new cards
	 * 
	 * @param activeDeck
	 */
	private void recordAlreadySeen(ActiveDeck activeDeck) {
		Iterator<ActiveCard> istat = activeDeck.deck.iterator();
		while (istat.hasNext()) {
			ActiveCard next = istat.next();
			nodupes.add(next.answer);
		}
	}
	private void retireNotYetCards(ActiveDeck current_pending) {
		Iterator<ActiveCard> icard = current_pending.deck.iterator();
		while (icard.hasNext()) {
			ActiveCard card = icard.next();
			if (card.show_again_ms < ONE_HOUR_ms
					&& card.box < SlotInfo.PROFICIENT_BOX) {
				continue;
			}
			current_done.deck.add(card);
			icard.remove();
		}
		App.log(this, "Moved " + current_done.deck.size() + " future pending"
				+ " or fully learned cards into the 'done' deck.");
	}

	@Override
	public void show() {
		super.show();
		gameboard = ui.getGameBoard(stage, ui, gs);
		Gdx.app.postRunnable(loadDeck);
	}
}