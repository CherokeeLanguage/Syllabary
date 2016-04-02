package com.cherokeelessons.syllabary.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.cherokeelessons.syllabary.one.App;
import com.cherokeelessons.ui.UI.UIDialog;
import com.cherokeelessons.util.GooglePlayGameServices.Callback;
import com.cherokeelessons.util.GooglePlayGameServices.GameScores;
import com.cherokeelessons.util.GooglePlayGameServices.GameScores.GameScore;

public class Leaderboard extends ChildScreen {

	private Table container;
	private ScrollPane scroll;
	private Table scrolltable;
	private Label message;

	public Leaderboard(Screen caller) {
		super(caller);
		container = ui.getMenuTable();
		stage.addActor(container);
	}

	@Override
	public void show() {
		super.show();
		container.addAction(Actions.delay(.1f, Actions.run(new InitView())));
	}

	public FileHandle p0;

	public String[] ranks = { "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th", "11th", "12th",
			"13th", "14th", "15th", "16th", "17th", "18th", "19th", "20th" };

	public Callback<GameScores> success_show_scores = new Callback<GameScores>() {
		@Override
		public void success(GameScores data) {
			Gdx.app.log("success_show_scores", "Scores received.");

			if (data == null) {
				message.setText("No scores for display");
				return;
			}
			message.setText("Top Scores");

			Table table = scrolltable;

			LabelStyle ls = ui.getLs();

			table.clear();
			table.defaults().expandX();
			String text = "Rank";
			table.add(new Label(text, ls)).center();
			text = "Letters";
			table.add(new Label(text, ls)).center();
			text = "Score";
			table.add(new Label(text, ls)).center();
			text = "Skill Level";
			table.add(new Label(text, ls)).center();
			text = "Display Name";
			table.add(new Label(text, ls)).center();

			for (GameScore score : data.list) {
				table.row().pad(0);
				table.add(new Label(score.rank, ls)).center();
				table.add(new Label(score.activeCards, ls)).right();
				table.add(new Label(score.score, ls)).right();
				table.add(new Label(score.tag, ls)).center();
				table.add(new Label(score.user, ls)).center();
			}

			for (int ix = data.list.size(); ix < ranks.length; ix++) {
				table.row().pad(0);
				table.add(new Label(ranks[ix], ls)).center();
				table.add(new Label("", ls)).right();
				table.add(new Label("", ls)).right();
				table.add(new Label("", ls)).center();
				table.add(new Label("", ls)).center();
			}

		}
	};

	private class InitView implements Runnable {
		@Override
		public void run() {
			TextButton button;

			final TextButtonStyle tbs = ui.getTbsSmall();

			ButtonGroup<TextButton> bgroup = new ButtonGroup<TextButton>();
			bgroup.setMaxCheckCount(1);
			bgroup.setMinCheckCount(1);

			container.row();

			button = new TextButton("BACK", tbs);
			container.add(button).left().top();
			button.addListener(exit);

			LabelStyle ls = ui.getLsSmall();
			message = new Label("...", ls);
			container.add(message).center().expandX().fillX().top();

			if (!App.services.isLoggedIn()) {
				button = new TextButton("Sync Login", tbs);
			} else {
				button = new TextButton("Sync Logout", tbs);
			}
			final TextButton play_button = button;
			final UIDialog login = new UIDialog("Sync Service", true, true, ui);
			login.text("Connecting to Sync Service ...");
			login.button("DISMISS");

			final UIDialog[] error = new UIDialog[1];
			error[0] = ui.errorDialog(new Exception(""), null);
			play_button.addListener(new ClickListener() {
				Callback<Void> success_in = new Callback<Void>() {
					@Override
					public void success(Void result) {
						error[0].hide();
						login.hide();
						requestScores();
						play_button.setText("Logout of Sync");
					}

					@Override
					public void error(Exception e) {
						error[0].hide();
						login.hide();
						success_out.withNull().run();
						error[0] = ui.errorDialog(e, null);
						error[0].show(stage);
					}
				};
				Callback<Void> success_out = new Callback<Void>() {
					@Override
					public void success(Void result) {
						error[0].hide();
						login.hide();
						requestScores();
						play_button.setText("Login to Sync");
					}

					@Override
					public void error(Exception exception) {
						error[0].hide();
						login.hide();
						success_out.withNull().run();
						error[0] = ui.errorDialog(exception, null);
						error[0].show(stage);
					}
				};

				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					if (App.services.isLoggedIn()) {
						App.services.logout(success_out);
					} else {
						login.show(stage);
						App.services.login(success_in);
					}
					return true;
				}
			});
			container.add(button).right().top();

			final int c = container.getCell(button).getColumn() + 1;

			scrolltable = new Table();
			scroll = new ScrollPane(scrolltable, ui.getSps());
			scroll.setColor(Color.DARK_GRAY);
			scroll.setFadeScrollBars(false);
			scroll.setSmoothScrolling(true);
			container.row();
			container.add(scroll).expand().fill().colspan(c);
			stage.setScrollFocus(scroll);
			stage.setKeyboardFocus(scroll);
			requestScores();
		}
	}

	private void requestScores() {
		message.setText("Loading ...");
		App.lb.lb_getScores(success_show_scores);
	}

	@Override
	public void render(float delta) {
		try {
			super.render(delta);
		} catch (Exception e) {
			e.printStackTrace();
			scrolltable.clear();
			scrolltable.remove();
			container.clear();
			container.remove();
			stage.clear();
			Gdx.app.postRunnable(getDoBack());
		}
	}
}
