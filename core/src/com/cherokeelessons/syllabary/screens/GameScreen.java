package com.cherokeelessons.syllabary.screens;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
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
	private static final int MinCardsInPlay = 7;
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
	/**
	 * holding area for cards that have just been displayed
	 */
	private final Deck discards = new Deck();

	/**
	 * holding area for cards with no more scheduled showings for this session
	 */
	private final Deck finished = new Deck();

	private GameBoard gameboard;

	private SlotInfo info;

	/**
	 * master copy of all cards
	 */
	private Deck master_deck = null;

	private final Set<String> nodupes = new HashSet<String>();

	/**
	 * currently being looped through for display
	 */
	private final Deck pending = new Deck();

	/**
	 * time since last "shuffle"
	 */
	private float sinceLastNextPendingCard_elapsed = 0f;

	final int slot;
	private float total_elapsed = 0f;

	// private RunnableAction updateBoard = new RunnableAction(){
	// public void run() {
	// App.log(this, "updateBoard");
	// for (int iy = 0; iy < GameBoard.height; iy++) {
	// for (int ix = 0; ix < GameBoard.width; ix++) {
	// final String img = UI.DISC;
	// Color img_color;
	// do {
	// img_color = new Color(new Random().nextFloat(),
	// new Random().nextFloat(), new Random().nextFloat(),
	// 1f);
	// } while (UI.luminance(img_color) < .6);
	// gameboard.setImageAt(ix, iy, img);
	// gameboard.setColorAt(ix, iy, img_color);
	// }
	// }
	// gameboard.setImageAt(0, 0, UI.IMG_SYNC);
	// gameboard.setChallenge_latin(deck.getCardByAnswer(Card.answer).challenge);
	// };
	// };

	// private RunnableAction nextCard = new RunnableAction(){
	// private Card previousCard;
	// public void run() {
	// App.log(this, "nextCard");
	// do {
	// Card = getNextCard();
	// if (Card == null) {
	// break;
	// }
	// if (!Card.equals(previousCard)) {
	// break;
	// }
	// if (current_discards.deck.size() == 0
	// && current_active.deck.size() == 0) {
	// break;
	// }
	// Card.show_again_ms = Deck.getNextInterval(Card
	// .correct_in_a_row + 1);
	// previousCard = null;
	// } while (true);
	// if (Card == null) {
	// App.log(this, "Card==null");
	// if (total_elapsed < BOARD_TICK) {
	// App.log(this, "session time is not up");
	// if (current_discards.deck.size()+current_active.deck.size()==0) {
	// Card newCard = nextCardForUse();
	// App.log(this, "adding card to deck: "+newCard.answer);
	// current_active.deck.add(newCard);
	// stage.addAction(nextCard);
	// return;
	// }
	// long shift_by_ms = getMinShiftTimeOf(current_discards);
	// App.log(this, "shifting discards to zero point: "
	// + (shift_by_ms / ONE_SECOND_ms));
	// if (shift_by_ms >= 15l * ONE_SECOND_ms) {
	// current_active.deck.add(nextCardForUse());
	// }
	// current_discards.updateTime(shift_by_ms);
	// stage.addAction(nextCard);
	// return;
	// }
	// /*
	// * Session time is up, force time shift cards into active show
	// * range...
	// */
	// if (total_elapsed > BOARD_TICK && current_discards.deck.size() > 0) {
	// long shift_by_ms = getMinShiftTimeOf(current_discards);
	// App.log(this, "shifting discards to zero point: "
	// + (shift_by_ms / ONE_SECOND_ms));
	// current_discards.updateTime(shift_by_ms);
	// stage.addAction(nextCard);
	// return;
	// }
	// if (total_elapsed > BOARD_TICK) {
	// ready=false;
	// App.log(this, "no cards remaining");
	// info.activeDeck.deck.clear();
	// info.activeDeck.deck.addAll(current_active.deck);
	// info.activeDeck.deck.addAll(current_due.deck);
	// info.activeDeck.deck.addAll(current_discards.deck);
	// info.activeDeck.deck.addAll(current_done.deck);
	// info.setElapsed_secs(total_elapsed);
	// info.lastrun=System.currentTimeMillis();
	// App.saveSlotInfo(slot, info);
	// ui.getReadyDialog(new Runnable() {
	// @Override
	// public void run() {
	// goodBye();
	// }
	// });
	// return;
	// }
	// } else {
	// stage.addAction(updateBoard);
	// }
	// };
	// };

	/**
	 * holding area for cards that are "due" but deck size says don't show yet
	 */
	private Deck user_deck = null;

	public GameScreen(Screen caller, int slot) {
		super(caller);
		this.slot = slot;
	}

	@Override
	public void act(float delta) {
		if (master_deck == null) {
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
			return;
		}
		if (pending.cards.size() == 0) {
			shuffle();
			return;
		}
		Card card = getNextPendingCard();
		discards.cards.add(card);
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
		pending.updateTime(sinceLastNextPendingCard_elapsed);
		sinceLastNextPendingCard_elapsed = 0f;
		if (pending.getMinShiftTimeOf() > 10l * ONE_SECOND_ms) {
			pending.updateTime(pending.getMinShiftTimeOf());
			Card nextAvailableCard = getNextAvailableCard();
			App.log(this, "Adding '" + nextAvailableCard.answer
					+ "' to pending deck.");
			pending.cards.add(nextAvailableCard);
			pending.shuffle();
			pending.sortByShowTime();
		}
		;
		return pending.cards.remove(0);
	}

	private void endSession() {
		// TODO Auto-generated method stub

	}

	/**
	 * Retrieve either next unseen card, or if all seen, oldest "finished" card.
	 * 
	 * @return
	 */
	private Card getNextAvailableCard() {
		Iterator<Card> ideck = master_deck.cards.iterator();
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
		finished.clampToMinutes();
		finished.shuffle();
		finished.sortByShowTime();
		ideck = finished.cards.iterator();
		Card card = ideck.next();
		ideck.remove();
		card.reset();
		return card;
	}

	@Override
	public void hide() {
		super.hide();
	}

	private boolean bonus_round = false;

	private void loadGameboardWith(Card card) {
		gameboard.setChallenge_latin(card.challenge);
		if (card.box==0 && card.correct_in_a_row==0) {
			String answer_img = getGlyphFilename(card.answer.charAt(0), false).toString();
			gameboard.setChallenge_img(answer_img);
		} else {
			gameboard.setChallenge_img("images/misc/003f_4.png");
		}
		boolean valid = false;
		do {
			for (int ix = 0; ix < GameBoard.width; ix++) {
				for (int iy = 0; iy < GameBoard.height; iy++) {
					int letter = r.nextInt('Ᏼ' - 'Ꭰ') + 'Ꭰ';
					if (card.answer.equals(String.valueOf((char) letter))) {
						valid = true;
					}
					StringBuilder img = getGlyphFilename(letter, true);
					gameboard.setImageAt(ix, iy, img.toString());
					gameboard.setColorAt(ix, iy, UI.randomBrightColor());
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
		master_deck = Syllabary.getDeck();
	}

	private void loadUserdeck() {
		info = App.getSlotInfo(slot);
		user_deck = new Deck(info.deck);
		user_deck.updateTime(ONE_DAY_ms + ONE_HOUR_ms);
		/*
		 * Make sure we don't have active cards pointing to no longer existing
		 * master deck cards
		 */
		Iterator<Card> ipending = user_deck.cards.iterator();
		while (ipending.hasNext()) {
			Card active = ipending.next();
			if (master_deck.cards.contains(active)) {
				continue;
			}
			ipending.remove();
			App.log(this, "Removed no longer valid entry: " + active.answer);
		}
		user_deck.resetScoring();
		user_deck.resetCorrectInARow();
		user_deck.resetRetriesCount();
		user_deck.resetErrorMarker();
		user_deck.clampBoxes();
		user_deck.clampToMinutes();
		user_deck.shuffle();
		user_deck.sortByShowTime();
		trackAlreadyCards(user_deck);
		retireNotYetCards(user_deck);
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
		if (pending.size()<2) {
			pending.cards.add(card);
		} else {
			pending.cards.add(1, card);
		}
		discards.cards.remove(card);
		finished.cards.remove(card);
	}

	private void retireNotYetCards(Deck deck) {
		Iterator<Card> icard = deck.cards.iterator();
		while (icard.hasNext()) {
			Card card = icard.next();
			if (card.show_again_ms < ONE_HOUR_ms
					&& card.box < SlotInfo.PROFICIENT_BOX) {
				continue;
			}
			finished.cards.add(card);
			icard.remove();
		}
		App.log(this, "Moved " + finished.cards.size() + " future pending"
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
		discards.shuffle();
		pending.loadAll(discards);
		while (pending.size() < MinCardsInPlay) {
			Card nextAvailableCard = getNextAvailableCard();
			App.log(this, "Added card: '" + nextAvailableCard.answer + "'");
			pending.cards.add(nextAvailableCard);
		}
		pending.sortByShowTime();
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