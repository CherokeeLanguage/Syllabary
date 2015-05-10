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
import com.cherokeelessons.util.GooglePlayGameServices.Collection;
import com.cherokeelessons.util.GooglePlayGameServices.GameScores;
import com.cherokeelessons.util.GooglePlayGameServices.GameScores.GameScore;
import com.cherokeelessons.util.GooglePlayGameServices.TimeSpan;
import com.cherokeelessons.util.WordUtils;

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

	private TimeSpan ts = TimeSpan.DAILY;
	public FileHandle p0;

	public String[] ranks = { "1st", "2nd", "3rd", "4th", "5th", "6th", "7th",
			"8th", "9th", "10th" };

	public Callback<GameScores> success_show_scores = new Callback<GameScores>() {
		@Override
		public void success(GameScores data) {
			Gdx.app.log("success_show_scores", "Scores received.");
			if (data==null) {
				message.setText("You must login to Google Play for Leaderboard Support");
				return;
			}
			if (data.collection==null) {
				data.collection=lb_collection;
			}
			if (data.ts==null) {
				data.ts=ts;
			}
			if (data.collection.equals(Collection.PUBLIC)) {
				message.setText(data.ts.getEngrish() + " Top Public Scores");
			}
			if (data.collection.equals(Collection.SOCIAL)) {
				message.setText(data.ts.getEngrish() + " Top Circle Scores");
			}

			Table table = scrolltable;

			LabelStyle ls = ui.getLsLarge();

			table.clear();
			table.defaults().expandX();
			String text = "Rank";
			table.add(new Label(text, ls)).padLeft(15).padRight(15).center();
			text = "Score";
			table.add(new Label(text, ls)).center();
			text = "Skill Level";
			table.add(new Label(text, ls)).center();
			text = "Display Name";
			table.add(new Label(text, ls)).center();

			for (GameScore score : data.list) {
				table.row();
				table.add(new Label(score.rank, ls)).padLeft(15).padRight(15)
						.center();
				table.add(new Label(score.value, ls)).right().padRight(30);
				table.add(new Label(score.tag, ls)).center();
				table.add(new Label(score.user, ls)).center();
			}

			for (int ix = data.list.size(); ix < ranks.length; ix++) {
				table.row();
				table.add(new Label(ranks[ix], ls)).padLeft(15).padRight(15)
						.center();
				table.add(new Label("0", ls)).right().padRight(30);
				table.add(new Label("Newbie", ls)).center();
				table.add(new Label("", ls)).center();
			}
			
			if (!App.isLoggedIn()) {
				message.setText("You must login to Google Play for Leaderboard Support");
			}
		}
	};

	public static final String BoardId = "CgkI4-75m_EMEAIQBg";

	public Collection lb_collection = Collection.PUBLIC;

	private class InitView implements Runnable {
		@Override
		public void run() {
			TextButton button;

			final TextButtonStyle tbs = ui.getTbsSmall();

			ButtonGroup<TextButton> bgroup = new ButtonGroup<TextButton>();
			bgroup.setMaxCheckCount(1);
			bgroup.setMinCheckCount(1);

			container.row().fill(true, false).expand(true, false).top().center();
			
			button = new TextButton("BACK", tbs);
			container.add(button);
			button.addListener(exit);

			button = new TextButton("Show "+ts.next().getEngrish(), tbs);
			button.setChecked(false);
			container.add(button);
			final TextButton ts_button = button;

			button = new TextButton("Show "+lb_collection.next().getEnglish(), tbs);
			button.setChecked(true);
			container.add(button);
			bgroup.add(button);
			final TextButton lb_button = button;
			lb_button.addListener(new ClickListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y,
						int pointer, int button) {
					lb_collection = lb_collection.next();
					lb_button.setText("Show "+lb_collection.next().getEnglish());
					requestScores();
					return true;
				}
			});
			ts_button.addListener(new ClickListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y,
						int pointer, int button) {
					ts = ts.next();
					float width = ts_button.getLabel().getWidth();
					ts_button.setText("Show "+ts.next().getEngrish());
					ts_button.getLabel().setWidth(width);
					requestScores();
					return true;
				}
			});
			
			LabelStyle ls = ui.getLsSmall();
			message = new Label("...", ls);

			if (!App.isLoggedIn()) {
				button = new TextButton("Login to Google Play", tbs);
				message.setText("You must login to Google Play for Leaderboard Support");
			} else {
				button = new TextButton("Logout of Google Play", tbs);
			}
			final TextButton play_button = button;
			final UIDialog login = new UIDialog("Google Play Services", true, true, ui);
			login.text("Connecting to Google Play Services ...");
			login.button("DISMISS");
			
			final UIDialog[] error = new UIDialog[1];
			error[0]=ui.errorDialog(new Exception(""), null);
			play_button.addListener(new ClickListener(){				
				Callback<Void> success_in=new Callback<Void>() {							
					@Override
					public void success(Void result) {
						error[0].hide();
						login.hide();
						App.setLoggedIn(true);
						requestScores();
						play_button.setText("Logout of Google Play");
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
				Callback<Void> success_out=new Callback<Void>() {							
					@Override
					public void success(Void result) {
						error[0].hide();
						login.hide();
						App.setLoggedIn(false);
						requestScores();
						play_button.setText("Login to Google Play");
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
				public boolean touchDown(InputEvent event, float x, float y,
						int pointer, int button) {					
					if (App.isLoggedIn()) {
						App.services.logout(success_out);
					} else {
						login.show(stage);
						App.services.login(success_in);
					}
					return true;
				}
			});
			container.add(button).center().top().expandX().fillX();

			final int c = container.getCell(button).getColumn() + 1;

			container.row();
			message.setAlignment(Align.center);
			container.add(message).expandX().fillX().colspan(c).center();

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
		if (App.isLoggedIn()) {
			App.services.lb_getListFor(BoardId, lb_collection, ts,
					success_show_scores);
			message.setText("Loading ...");
		} else {
			Gdx.app.postRunnable(success_show_scores.with(new GameScores()));
		}
	}
}
