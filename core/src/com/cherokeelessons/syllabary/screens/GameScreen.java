package com.cherokeelessons.syllabary.screens;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cherokeelessons.cards.Card;
import com.cherokeelessons.cards.Deck;
import com.cherokeelessons.cards.SlotInfo;
import com.cherokeelessons.syllabary.one.App;
import com.cherokeelessons.syllabary.one.Syllabary;
import com.cherokeelessons.ui.GameBoard;
import com.cherokeelessons.ui.UI;
import com.cherokeelessons.ui.UI.UIDialog;

public class GameScreen extends ChildScreen {

	private static final float BOARD_TICK = 120f;
	private static final float CARD_TICK = 15f;
	private static final int MinCardsInPlay = 3;
	private static final long ONE_DAY_ms;
	private static final long ONE_HOUR_ms;
	private static final long ONE_MINUTE_ms;
	private static final long ONE_SECOND_ms;
	private static final Random r = new Random();

	static {
		ONE_SECOND_ms = 1000l;
		ONE_MINUTE_ms = 60l * ONE_SECOND_ms;
		ONE_HOUR_ms = 60l * ONE_MINUTE_ms;
		ONE_DAY_ms = 24l * ONE_HOUR_ms;
	}

	private float challenge_elapsed = 0f;
	private boolean dialogShowing = false;

	private GameBoard gameboard;

	private SlotInfo info;


	private final Set<String> nodupes = new HashSet<String>();

	public static class Decks {
		/**
		 * master copy of all cards
		 */
		public Deck master = null;
		/**
		 * the deck we are starting out with
		 */
		public Deck starting = null;
		/**
		 * currently being looped through for display
		 */
		public final Deck pending = new Deck();
		/**
		 * holding area for cards that have just been displayed
		 */
		public final Deck discards = new Deck();
		/**
		 * holding area for cards with no more scheduled showings for this session
		 */
		public final Deck finished = new Deck();
	}

	/**
	 * time since last "shuffle"
	 */
	private float sinceLastNextPendingCard_elapsed = 0f;

	final int slot;
	private float total_elapsed = 0f;
	private Decks decks;

	public GameScreen(Screen caller, int slot) {
		super(caller);
		this.slot = slot;
		this.decks = new Decks();
	}

	@Override
	public void act(float delta) {
		if (decks.master == null) {
			loadMasterdeck();
			return;
		}
		if (info == null) {
			loadUserdeck();
			return;
		}
		if (dialogShowing || gameboard.isPaused()) {
			super.act(delta);
			return;
		}
		if (total_elapsed > BOARD_TICK) {
			endSession();
			return;
		}
		if (gameboard.isActive()) {
			updateTimes(delta);
			gameboard.setRemaining(
					sinceLastNextPendingCard_elapsed / CARD_TICK, 1f);
			super.act(delta);
			if (totalRight==0) {
				gameboard.setActive(false);
			}
			return;
		}
		if (decks.pending.cards.size() == 0) {
			shuffle();
			return;
		}
		Card card = getNextPendingCard();
		decks.discards.cards.add(card);
		if (card.newCard) {
			newCardDialog(card);
			card.show_again_ms = Deck.getNextInterval(0);
			reinsertCard(card);
			return;
		}
		loadGameboardWith(card);
		gameboard.setActive(true);
	}

	private Card getNextPendingCard() {
		decks.pending.updateTime(sinceLastNextPendingCard_elapsed);
		sinceLastNextPendingCard_elapsed = 0f;
		long minShiftTimeOf = decks.pending.getMinShiftTimeOf()/ONE_SECOND_ms;
		App.log(this, "Min Shift Time Of: " + minShiftTimeOf);
		if (minShiftTimeOf > 60l) {
			decks.pending.updateTime(minShiftTimeOf);
			Card nextAvailableCard = getNextAvailableCard();
			App.log(this, "Adding '" + nextAvailableCard.answer
					+ "' to pending deck.");
			return nextAvailableCard;
		}
		return decks.pending.cards.remove(0);
	}

	private void endSession() {
		// TODO Auto-generated method stub
		App.log(this, "Session Complete: "+total_elapsed);
	}

	/**
	 * Retrieve either next unseen card, or if all seen, oldest "finished" card.
	 * 
	 * @return
	 */
	private Card getNextAvailableCard() {
		Iterator<Card> ideck = decks.master.cards.iterator();
		while (ideck.hasNext()) {
			Card next = ideck.next();
			if (nodupes.contains(next.answer)) {
				continue;
			}
			Card card = new Card(next);
			card.reset();
			card.box = 0;
			card.newCard = true;
			card.show_again_ms = 0;
			nodupes.add(card.answer);
			return card;
		}
		decks.finished.clampToMinutes();
		decks.finished.shuffle();
		decks.finished.sortByShowTime();
		return decks.finished.cards.remove(0);
	}

	@Override
	public void hide() {
		super.hide();
	}

	private boolean bonus_round = false;

	private int totalRight=0;
	private void loadGameboardWith(final Card card) {
		gameboard.setChallenge_latin(card.challenge);
		if (card.box==0 && card.correct_in_a_row==0) {
			String answer_img = getGlyphFilename(card.answer.charAt(0), false).toString();
			gameboard.setChallenge_img(answer_img);
		} else {
			gameboard.setChallenge_img("images/misc/003f_4.png");
		}
		boolean valid = false;
		do {
			totalRight=0;
			for (int ix = 0; ix < GameBoard.width; ix++) {
				for (int iy = 0; iy < GameBoard.height; iy++) {
					int letter = r.nextInt('Ᏼ' - 'Ꭰ') + 'Ꭰ';
					final boolean isCorrect = card.answer.equals(String.valueOf((char) letter));
					if (isCorrect) {
						valid = true;
						totalRight++;
					}
					StringBuilder img = getGlyphFilename(letter, true);
					gameboard.setImageAt(ix, iy, img.toString());
					gameboard.setColorAt(ix, iy, UI.randomBrightColor());
					final int score = (card.box)+5+card.correct_in_a_row;
					final int img_ix=ix;
					final int img_iy=iy;
					gameboard.getImageAt(ix, iy).addCaptureListener(new ClickListener(){
						@Override
						public boolean touchDown(InputEvent event, float x,
								float y, int pointer, int button) {
							event.getListenerActor().setTouchable(Touchable.disabled);
							if (isCorrect) {
								totalRight--;
								gameboard.addToScore(score);
								gameboard.setImageAt(img_ix, img_iy, UI.CHECKMARK);
								gameboard.setColorAt(img_ix, img_iy, Color.GREEN);
								card.correct_in_a_row++;
							} else {
								gameboard.addToScore(-score);
								gameboard.setImageAt(img_ix, img_iy, UI.HEAVYX);
								gameboard.setColorAt(img_ix, img_iy, Color.RED);
								card.correct_in_a_row=0;
								card.tries_remaining++;
								card.noErrors=false;
							}
							card.show_again_ms=Deck.getNextInterval(card.correct_in_a_row);
							card.tries_remaining--;
							if (card.tries_remaining==0) {
								decks.finished.cards.add(card);
								decks.discards.cards.remove(card);
								decks.pending.cards.remove(card);
							}
							return true;
						}
					});
				}
			}
		} while (!valid);
	}

	private StringBuilder getGlyphFilename(int letter, boolean random) {
		StringBuilder img = new StringBuilder(
				Integer.toHexString(letter));
		while (img.length() < 4) {
			img.insert(0, "0");
		}
		img.append("_");
		if (!bonus_round) {
			if (random) {
				img.append(r.nextInt(5));
			} else {
				img.append(1);
			}
		} else {
			img.append("5");
		}
		img.insert(0, "images/glyphs/");
		img.append(".png");
		return img;
	}

	private void loadMasterdeck() {
		decks.master = Syllabary.getDeck();
	}

	private void loadUserdeck() {
		info = App.getSlotInfo(slot);
		decks.starting = new Deck(info.deck);
		decks.starting.updateTime(ONE_DAY_ms + ONE_HOUR_ms);
		/*
		 * Make sure we don't have active cards pointing to no longer existing
		 * master deck cards
		 */
		Iterator<Card> ipending = decks.starting.cards.iterator();
		while (ipending.hasNext()) {
			Card active = ipending.next();
			if (decks.master.cards.contains(active)) {
				continue;
			}
			ipending.remove();
			App.log(this, "Removed no longer valid entry: " + active.answer);
		}
		decks.starting.resetScoring();
		decks.starting.resetCorrectInARow();
		decks.starting.resetRetriesCount();
		decks.starting.resetErrorMarker();
		decks.starting.clampBoxes();
		decks.starting.clampToMinutes();
		decks.starting.shuffle();
		decks.starting.sortByShowTime();
		trackAlreadyCards(decks.starting);
		retireNotYetCards(decks.starting);
	}

	private void newCardDialog(Card card) {
		dialogShowing=true;
		UIDialog d = new UIDialog("New Letter", true, ui) {
			@Override
			protected void result(Object object) {
				dialogShowing=false;
			}
		};
		Label l1 = new Label("Memorize The Following:", ui.getLs());
		Label l2 = new Label(card.answer+" - "+card.challenge, ui.getLsLarge());
		TextButton ready = new TextButton("READY", ui.getTbs());
		d.text(l1);
		d.text(l2);
		d.button(ready);
		d.show(stage);
		card.newCard=false;
	}

	private void reinsertCard(Card card) {
		if (decks.pending.size()<2) {
			decks.pending.cards.add(card);
		} else {
			decks.pending.cards.add(1, card);
		}
		decks.discards.cards.remove(card);
		decks.finished.cards.remove(card);
	}

	private void retireNotYetCards(Deck deck) {
		Iterator<Card> icard = deck.cards.iterator();
		while (icard.hasNext()) {
			Card card = icard.next();
			if (card.show_again_ms < ONE_HOUR_ms
					&& card.box < SlotInfo.PROFICIENT_BOX) {
				continue;
			}
			decks.finished.cards.add(card);
			icard.remove();
		}
		App.log(this, "Moved " + decks.finished.cards.size() + " future pending"
				+ " or fully learned cards into the 'finished' deck.");
	}

	@Override
	public void show() {
		super.show();
		gameboard = ui.getGameBoard(stage, ui, gs);
	}

	/**
	 * Loads pending with discards, user_deck, and deck
	 */
	private void shuffle() {
		decks.discards.shuffle();
		decks.pending.loadAll(decks.discards);
		decks.pending.sortByShowTime();
		/*
		 * add new cards to the end of the current deck
		 */
		while (decks.pending.size() < MinCardsInPlay) {
			Card nextAvailableCard = getNextAvailableCard();
			App.log(this, "Added card: '" + nextAvailableCard.answer + "'");
			decks.pending.cards.add(nextAvailableCard);
		}
	}

	/**
	 * record all cards currently "in-play" so that when cards are retrieved
	 * from the master deck they are new cards
	 * 
	 * @param deck
	 */
	private void trackAlreadyCards(Deck deck) {
		Iterator<Card> istat = deck.cards.iterator();
		while (istat.hasNext()) {
			Card next = istat.next();
			nodupes.add(next.answer);
		}
	}

	private void updateTimes(float delta) {
		total_elapsed += delta;
		challenge_elapsed += delta;
		sinceLastNextPendingCard_elapsed += delta;
	}
}