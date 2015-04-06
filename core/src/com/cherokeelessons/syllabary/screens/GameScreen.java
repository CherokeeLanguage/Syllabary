package com.cherokeelessons.syllabary.screens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

	private static final float BOARD_TICK = 2f * 60f + 30f;
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

	private GameBoard gameboard;

	private SlotInfo info;

	private final Set<String> nodupes = new HashSet<String>();

	/**
	 * time since last "shuffle"
	 */
	private float currentCard_elapsed = 0f;

	final int slot;
	private float total_elapsed = 0f;
	private GameScreenDecks decks;

	public GameScreen(Screen caller, int slot) {
		super(caller);
		this.slot = slot;
		this.decks = new GameScreenDecks();
	}

	private float updateRemainingTick=0f;
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
		if (gameboard.isActive()) {
			updateTimes(delta);
			updateRemainingTick += delta;
			if (updateRemainingTick>1f) {
				gameboard.setRemaining(challenge_elapsed / CARD_TICK, 1f);
				updateRemainingTick-=1f;
			}
			super.act(delta);
			if (totalRight == 0) {
				gameboard.setActive(false);
			}
			return;
		}
		if (total_elapsed > BOARD_TICK) {
			endSession();
			return;
		}
		if (decks.pending.cards.size() == 0) {
			shuffle();
			return;
		}
		currentCard = getNextPendingCard();
		decks.discards.cards.add(currentCard);
		if (currentCard.newCard) {
			// newCardDialog(currentCard);
			currentCard.show_again_ms = Deck.getNextInterval(0);
			// reinsertCard(currentCard);
			// return;
		}
		loadGameboardWith(currentCard);
		gameboard.setActive(true);
		updateRemainingTick=0f;
		gameboard.setRemaining(0f, 0f);
	}

	private Card currentCard = null;

	private Card getNextPendingCard() {
		decks.pending.updateTime(currentCard_elapsed);
		currentCard_elapsed = 0f;
		challenge_elapsed = 0f;
		float minShiftTimeOf = (float) decks.pending.getMinShiftTimeOf()
				/ (float) ONE_SECOND_ms;
		App.log(this, "Min Shift Time Of: " + minShiftTimeOf);
		if (minShiftTimeOf > 120f) {
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
		App.log(this, "Session Complete: " + total_elapsed);
		info.deck.cards.clear();
		info.deck.loadAll(decks.pending);
		info.deck.loadAll(decks.discards);
		info.deck.loadAll(decks.reserved);
		info.deck.loadAll(decks.finished);
		info.deck.sortByShowTimeMinutes();
		info.recalculateStats();
		info.lastScore = gameboard.getScore();
		info.lastrun = System.currentTimeMillis();
		App.saveSlotInfo(slot, info);
		dialogShowing = true;
	}

	/**
	 * Retrieve either next reserved discard, or next new card, or next
	 * "finished" card.
	 * 
	 * @return
	 */
	private Card getNextAvailableCard() {
		if (decks.reserved.size() > 0) {
			return decks.reserved.cards.remove(0);
		}
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
			card.show_again_ms = Deck.getNextInterval(0);
			nodupes.add(card.answer);
			return card;
		}
		decks.finished.clampToMinutes();
		decks.finished.shuffle();
		decks.finished.sortByShowTimeMinutes();
		return decks.finished.cards.remove(0);
	}

	@Override
	public void hide() {
		super.hide();
	}

	private boolean bonus_round = false;

	private int totalRight = 0;

	private final List<ClickListener> wrongAnswers = new ArrayList<>();
	
	private void loadGameboardWith(final Card card) {
		gameboard.setChallenge_latin(card.challenge);
		if (card.box + card.correct_in_a_row < 2) {
			String answer_img = getGlyphFilename(card.answer.charAt(0), false)
					.toString();
			gameboard.setChallenge_img(answer_img);
		} else {
			gameboard.setChallenge_img("images/misc/003f_4.png");
		}
		boolean valid = false;
		char lastLetter = decks.getLastLetter();
		App.log(this, "Current Letter: '"+card.answer+"'");
		App.log(this, "Last Letter: '"+lastLetter+"'");
		do {
			totalRight = 0;
			wrongAnswers.clear();
			for (int ix = 0; ix < GameBoard.width; ix++) {
				for (int iy = 0; iy < GameBoard.height; iy++) {
					int letter = r.nextInt(lastLetter - 'Ꭰ' + 1) + 'Ꭰ';
					final boolean isCorrect = card.answer.equals(String
							.valueOf((char) letter));
					if (isCorrect) {
						valid = true;
						totalRight++;
					}
					StringBuilder img = getGlyphFilename(letter, true);
					gameboard.setImageAt(ix, iy, img.toString());
					gameboard.setColorAt(ix, iy, UI.randomBrightColor());
					final int score = card.box * 5 + 5 + card.correct_in_a_row;
					final int img_ix = ix;
					final int img_iy = iy;
					ClickListener listener = new ClickListener() {
						@Override
						public boolean touchDown(InputEvent event,
								float x, float y, int pointer,
								int button) {
							event.getListenerActor().setTouchable(
									Touchable.disabled);
							int amt = card.noErrors ? score : score / 2;
							if (isCorrect) {
								totalRight--;
								gameboard.addToScore(amt);
								gameboard.setImageAt(img_ix, img_iy,
										UI.CHECKMARK);
								gameboard.setColorAt(img_ix, img_iy,
										Color.GREEN);
							} else {
								gameboard.addToScore(-amt);
								gameboard.setImageAt(img_ix, img_iy,
										UI.HEAVYX);
								gameboard.setColorAt(img_ix, img_iy,
										Color.RED);
								if (card.correct_in_a_row > 0) {
									card.tries_remaining++;
									card.correct_in_a_row = 0;
								}
								card.noErrors = false;
							}
							if (totalRight <= 0) {
								if (card.noErrors) {
									card.correct_in_a_row++;
								}
								card.showCount++;
								card.showTime += currentCard_elapsed;
								card.tries_remaining--;
							}
							card.show_again_ms += Deck
									.getNextInterval(card.correct_in_a_row);
							if (card.tries_remaining <= 0) {
								card.tries_remaining = -1;
								App.log(this, "=== Retiring card: '"
										+ card.answer + "'");
								decks.finished.cards.add(card);
								decks.discards.cards.remove(card);
								decks.pending.cards.remove(card);
								if (card.sendToNextBox()) {
									card.box++;
								} else {
									card.box--;
								}
								card.show_again_ms += Deck
										.getNextSessionInterval(card.box);
							}
							return true;
						}
					};
					if (!isCorrect) {
						wrongAnswers.add(listener);
					}
					gameboard.getImageAt(ix, iy).addCaptureListener(listener);
				}
			}
		} while (!valid);
		Collections.shuffle(wrongAnswers);
	}

	private StringBuilder getGlyphFilename(int letter, boolean random) {
		StringBuilder img = new StringBuilder(Integer.toHexString(letter));
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
		info.deck.updateTime(ONE_DAY_ms + ONE_HOUR_ms);
		/*
		 * Make sure we don't have active cards pointing to no longer existing
		 * master deck cards
		 */
		Iterator<Card> ipending = info.deck.cards.iterator();
		while (ipending.hasNext()) {
			Card active = ipending.next();
			if (decks.master.cards.contains(active)) {
				continue;
			}
			ipending.remove();
			App.log(this, "Removed no longer valid entry: " + active.answer);
		}
		trackAlreadyCards(info.deck);
		retireNotYetCards(info.deck);
		decks.discards.loadAll(info.deck);
		decks.discards.resetScoring();
		decks.discards.resetCorrectInARow();
		decks.discards.resetRetriesCount();
		decks.discards.resetErrorMarker();
		decks.discards.clampBoxes();
		decks.discards.shuffle();
		decks.discards.sortByShowTimeMinutes();

		int size = decks.discards.size();
		int end = MinCardsInPlay < size ? MinCardsInPlay : size;
		List<Card> subList = decks.discards.cards.subList(0, end);
		decks.pending.cards.addAll(subList);
		subList.clear();
		decks.reserved.loadAll(decks.discards);
	}

	private void newCardDialog(Card card) {
		dialogShowing = true;
		UIDialog d = new UIDialog("New Letter", false, ui) {
			@Override
			protected void result(Object object) {
				dialogShowing = false;
			}
		};
		d.setFillParent(true);
		Label l1 = new Label("Memorize The Following:", ui.getLs());
		Label l2 = new Label(card.answer + " - " + card.challenge,
				ui.getLsLarge());
		TextButton ready = new TextButton("READY", ui.getTbs());
		d.text(l1).row();
		d.text(l2).row();
		d.button(ready);
		d.show(stage);
		card.newCard = false;
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
		App.log(this, "Moved " + decks.finished.cards.size()
				+ " future pending"
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
		App.log(this, "Shuffle");
		decks.pending.loadAll(decks.discards);
		decks.pending.shuffle();
		decks.pending.sortByShowTimeMinutes();
		if (decks.pending.size() < MinCardsInPlay) {
			Card nextAvailableCard = getNextAvailableCard();
			App.log(this, "\tAdded card: '" + nextAvailableCard.answer + "'");
			decks.pending.cards.add(nextAvailableCard);
		}
		App.log(this, "\tPending size: " + decks.pending.size());
		Card firstCard = decks.pending.cards.get(0);
		if (firstCard.equals(currentCard)) {
			decks.pending.cards.remove(0);
			decks.pending.cards.add(firstCard);
			currentCard = null;
		}
		decks.pending.updateTime(decks.pending.getMinShiftTimeOf());
		currentCard = null;
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
		currentCard_elapsed += delta;
	}
}