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
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.cherokeelessons.cards.Card;
import com.cherokeelessons.cards.Deck;
import com.cherokeelessons.cards.DisplayMode;
import com.cherokeelessons.cards.SlotInfo;
import com.cherokeelessons.syllabary.one.App;
import com.cherokeelessons.syllabary.one.Syllabary;
import com.cherokeelessons.ui.GameBoard;
import com.cherokeelessons.ui.GameBoard.GameboardHandler;
import com.cherokeelessons.ui.UI;
import com.cherokeelessons.ui.UI.UIDialog;
import com.cherokeelessons.util.GooglePlayGameServices.Callback;
import com.cherokeelessons.util.WordUtils;

public class GameScreen extends ChildScreen implements GameboardHandler {

	private static final float BOARD_TICK = 2f * 60f;
	private static final float CARD_TICK = 4f;
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
	private boolean audio1_done = false;
	private boolean audio2_done = false;

	private float challenge_elapsed = 0f;

	private Card currentCard = null;

	/**
	 * time since last "shuffle"
	 */
	private float currentCard_elapsed = 0f;

	private GameScreenDecks decks;

	private boolean dialogShowing = false;

	private GameBoard gameboard;
	private SlotInfo info;
	private final Set<String> nodupes = new HashSet<String>();

	private int perfectCount = 0;

	private boolean perfectStage = true;
	final int slot;

	private int stageCount = 0;

	private float total_elapsed = 0f;

	private int totalRight = 0;

	private boolean updateChallengeElapsed = true;

	private float updateRemainingTick = 0f;
	private final List<ClickListener> wrongAnswers = new ArrayList<>();

	public GameScreen(Screen caller, int slot) {
		super(caller);
		this.slot = slot;
		this.decks = new GameScreenDecks();
		gameboard = ui.getGameBoard(stage, ui, gs);
		gameboard.setHandler(this);
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
		if (gameboard.isActive()) {
			if (!audio1_done) {
				audio1_done = true;
				updateChallengeElapsed = false;
				Runnable whenDone = new Runnable() {
					@Override
					public void run() {
						updateChallengeElapsed = true;
					}
				};
				gs.playGlyph(currentCard.answer.charAt(0), whenDone);
			}
			if (challenge_elapsed > CARD_TICK / 2f && !audio2_done) {
				audio2_done = true;
				updateChallengeElapsed = false;
				Runnable whenDone = new Runnable() {
					@Override
					public void run() {
						updateChallengeElapsed = true;
					}
				};
				gs.playGlyph(currentCard.answer.charAt(0), whenDone);
			}
			if (challenge_elapsed > CARD_TICK) {
				gameboard.addToScore(-(currentCard.box * 5 + 1));
				perfectStage = false;
				audio1_done = false;
				audio2_done = false;
				challenge_elapsed = 0;
				gameboard.setRemaining(0, 0);
				if (wrongAnswers.size() > 0) {
					Collections.shuffle(wrongAnswers);
					wrongAnswers.remove(0).touchDown(null, 0, 0, 0, 0);
					gs.badSound();
				} else {
					if (total_elapsed > BOARD_TICK) {
						gameboard.setActive(false);
					}
				}
			}
			updateTimes(delta);
			updateRemainingTick += delta;
			if (updateRemainingTick > .1f) {
				gameboard.setRemaining(challenge_elapsed / CARD_TICK, .1f);
				updateRemainingTick = 0f;
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
		audio1_done = false;
		audio2_done = false;
		decks.discards.cards.add(currentCard);
		if (currentCard.newCard) {
			newCardDialog(currentCard);
			audio1_done = true;
		}
		loadGameboardWith(currentCard);
		gameboard.setActive(true);
		updateRemainingTick = 0f;
		gameboard.setRemaining(0f, 0f);
	}

	private enum Choices {
		NextStage, MainMenu, Leaderboard;
	}

	private enum YesNo {
		Yes, No;
	}

	private Callback<Void> viewScoresAfterSubmit = new Callback<Void>() {
		@Override
		public void success(Void result) {
			App.getGame().setScreen(new Leaderboard(GameScreen.this));
		}
		public void error(Exception exception) {
			UIDialog error = new UIDialog("ERROR!", true, true, ui);
			error.text(WordUtils.wrap(exception.getMessage(), 60, "\n", true));
			error.button("OK");
			error.show(stage);
		};
	};

	private void endSession() {
		info.deck.cards.clear();
		info.deck.loadAll(decks.pending);
		info.deck.loadAll(decks.discards);
		info.deck.loadAll(decks.reserved);
		info.deck.loadAll(decks.finished);
		info.deck.sortByShowTimeMinutes();
		info.recalculateStats();
		info.lastrun = System.currentTimeMillis();
		App.saveSlotInfo(slot, info);

		final UIDialog finished = new UIDialog("Stage Complete!", true, true,
				ui) {
			@Override
			protected void result(Object object) {
				if (object != null) {
					if (object.equals(Choices.NextStage)) {
						GameScreen nextScreen = new GameScreen(
								GameScreen.this.caller, GameScreen.this.slot);
						if (perfectStage) {
							nextScreen.setStageCount(stageCount + 1);
						} else {
							nextScreen.setStageCount(1);
						}
						App.getGame().setScreen(nextScreen);
						GameScreen.this.dispose();
						return;
					}
					if (object.equals(Choices.MainMenu)) {
						GameScreen.this.goodBye();
						return;
					}
					if (object.equals(Choices.Leaderboard)) {
						cancel();
						if (App.isLoggedIn()) {
							App.getGame().setScreen(
									new Leaderboard(GameScreen.this));
							return;
						} else {
							UIDialog logind = new UIDialog(
									"Google Play Services", true, true, ui) {
								protected void result(Object object) {
									if (object.equals(YesNo.Yes)) {
										submitScore(viewScoresAfterSubmit);
									}
								};
							};
							logind.text("Leaderboard support requires that\n"
									+ "you to log in to Google Play Services.\n"
									+ "Would you like to login now?");
							logind.button("YES", YesNo.Yes);
							logind.button("NO", YesNo.No);
							logind.show(stage);
						}
						return;
					}
				}
			}
		};

		StringBuilder sb = new StringBuilder();
		if (perfectStage) {
			int sc = getStageCount();
			int extra = sc * 1000;
			sb.append("A PERFECT STAGE!\n");
			if (sc > 1) {
				sb.append("THAT'S " + sc + " PERFECT STAGES IN A ROW!\n");
			}
			sb.append("You get " + extra + " bonus points!\n");
			gameboard.addToScore(extra);
			gameboard.act(1f);
			gs.cashOut();
		}
		info.lastScore = gameboard.getScore();
		sb.append("Score: ");
		sb.append(info.lastScore);
		sb.append("\n");
		sb.append(info.activeCards);
		sb.append(" letters: ");
		sb.append(info.shortTerm);
		sb.append(" short, ");
		sb.append(info.mediumTerm);
		sb.append(" medium, ");
		sb.append(info.longTerm);
		sb.append(" long");
		sb.append("\n");

		finished.getContentTable().defaults();
		finished.textCenter(sb.toString());

		finished.button("NEXT STAGE", Choices.NextStage);
		finished.button("MAIN MENU", Choices.MainMenu);
		finished.button("VIEW TOP SCORES", Choices.Leaderboard);
		finished.show(stage);
		dialogShowing = true;

		if (App.isLoggedIn()) {
			finished.getButtonTable().setVisible(false);
			stage.addAction(Actions.sequence(Actions.delay(10f),
					Actions.run(new Runnable() {
						@Override
						public void run() {
							finished.getButtonTable().setVisible(true);
						}
					})));
			Callback<Void> enableButtons = new Callback<Void>() {
				@Override
				public void success(Void result) {
					finished.getButtonTable().setVisible(true);
				}
			};
			submitScore(enableButtons);
		}
	}

	private void submitScore(final Callback<Void> callback) {
		Callback<Void> do_submit = new Callback<Void>() {
			@Override
			public void success(Void result) {
				App.setLoggedIn(true);
				App.services.lb_submit(Leaderboard.BoardId, info.lastScore,
						info.level.getEnglish(), callback);
			}

			@Override
			public void error(Exception exception) {
				App.setLoggedIn(false);
				App.log(this, exception.getMessage());
				callback.error(exception);
			}
		};
		App.services.login(do_submit);
	}

	private StringBuilder getGlyphFilename(int letter, int font) {
		StringBuilder img = new StringBuilder(Integer.toHexString(letter));
		while (img.length() < 4) {
			img.insert(0, "0");
		}
		img.append("_");
		img.append(font);
		img.insert(0, "images/glyphs/");
		img.append(".png");
		return img;
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

	public int getPerfectCount() {
		return perfectCount;
	}

	public int getStageCount() {
		return stageCount;
	}

	private void loadGameboardWith(final Card card) {
		if (info.settings.display.equals(DisplayMode.Latin)) {
			gameboard.setChallenge_latin(card.challenge);
		} else {
			gameboard.setChallenge_latin("");
		}
		if (card.box == 0 && card.correct_in_a_row == 0) {
			String answer_img = getGlyphFilename(card.answer.charAt(0), 1)
					.toString();
			gameboard.setChallenge_img(answer_img);
		} else {
			gameboard.setChallenge_img("images/misc/003f_4.png");
		}
		boolean valid = false;
		char lastLetter = decks.getLastLetter();
		App.log(this, "Current Letter: '" + card.answer + "'");
		App.log(this, "Last Letter: '" + lastLetter + "'");
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
					int font = r.nextInt(5);
					StringBuilder img = getGlyphFilename(letter, font);
					gameboard.setImageAt(ix, iy, img.toString());
					gameboard.setColorAt(ix, iy, UI.randomBrightColor());
					final int score = card.box * 5 + 5 + card.correct_in_a_row;
					final int img_ix = ix;
					final int img_iy = iy;
					final Image img_actor = gameboard.getImageAt(ix, iy);
					ClickListener listener = new ClickListener() {
						@Override
						public boolean touchDown(InputEvent event, float x,
								float y, int pointer, int button) {
							img_actor.setTouchable(Touchable.disabled);
							int amt = card.noErrors ? score : score / 2;
							if (isCorrect) {
								challenge_elapsed = 0f;
								audio1_done = true;
								audio2_done = false;
								totalRight--;
								gameboard.addToScore(amt);
								gameboard.setImageAt(img_ix, img_iy,
										UI.CHECKMARK);
								gameboard.setColorAt(img_ix, img_iy,
										Color.GREEN);
							} else {
								wrongAnswers.remove(this);
								perfectStage = false;
								gameboard.addToScore(-amt);
								gameboard.setImageAt(img_ix, img_iy, UI.HEAVYX);
								gameboard.setColorAt(img_ix, img_iy, Color.RED);
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
								decks.remove(card);
								decks.finished.cards.add(card);
								if (card.sendToNextBox()) {
									card.box++;
									gameboard.addToScore(card.box * 100);
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

	private void newCardDialog(final Card card) {
		dialogShowing = true;
		final RunnableAction[] dialogDone = new RunnableAction[1];
		final UIDialog d = new UIDialog("New Letter", true, true, ui) {
			@Override
			protected void result(Object object) {
				cancel();
				getButtonTable().setTouchable(Touchable.disabled);
				addAction(dialogDone[0]);
			}
		};
		dialogDone[0] = new RunnableAction() {
			@Override
			public void run() {
				gs.dingding();
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						d.hide(null);
						dialogShowing = false;
					}
				};
				SequenceAction sequence = Actions.sequence(Actions.delay(1f),
						Actions.run(runnable));
				d.addAction(sequence);
			}
		};
		Label l1 = new Label(card.challenge, ui.getLsXLarge());
		TextButton ready = new TextButton("SKIP", ui.getTbs());
		Table content = d.getContentTable();
		Table pix = new Table();
		pix.defaults().pad(8);
		final List<RunnableAction> actions = new ArrayList<>();
		int startFont = 0;
		int endFont = 4;
		for (int ix = startFont; ix <= endFont; ix++) {
			String file = getGlyphFilename(card.answer.charAt(0), ix)
					.toString();
			final Image glyph = ui.loadImage(file);
			glyph.setScaling(Scaling.fit);
			pix.add(glyph).width(192f).height(192f);
			glyph.setColor(UI.randomBrightColor());
			glyph.addAction(Actions.alpha(0f));
			actions.add(new RunnableAction() {
				public void run() {
					Runnable whenDone = new Runnable() {
						public void run() {
							if (actions.size() > 0) {
								Action delay = Actions.delay(.7f);
								d.addAction(Actions.sequence(delay,
										actions.remove(0)));
							}
						};
					};
					glyph.addAction(Actions.alpha(1f, .3f));
					gs.playGlyph(card.answer.charAt(0), whenDone);
				};

			});
		}
		actions.add(dialogDone[0]);
		content.clearChildren();
		content.row();
		content.add(l1);
		content.row();
		content.add(pix).expandX().fillX();
		d.button(ready);
		d.setModal(true);
		d.setMovable(false);
		d.show(stage, null);
		card.newCard = false;
		d.addAction(actions.remove(0));
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

	public void setPerfectCount(int perfectCount) {
		this.perfectCount = perfectCount;
	}

	public void setStageCount(int stageCount) {
		this.stageCount = stageCount;
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
			decks.remove(firstCard);
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
		if (updateChallengeElapsed) {
			challenge_elapsed += delta;
		}
		currentCard_elapsed += delta;
	}

	@Override
	public void mainmenu() {
		goodBye();
	}

	@Override
	public void mute() {

	}
}