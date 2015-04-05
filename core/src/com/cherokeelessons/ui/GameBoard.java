package com.cherokeelessons.ui;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.cherokeelessons.cards.Card;
import com.cherokeelessons.syllabary.one.App;
import com.cherokeelessons.syllabary.one.GameSound;
import com.cherokeelessons.ui.UI.UIDialog;
import com.cherokeelessons.ui.UI.UIProgressBar;

public class GameBoard extends Table {
	public static final int height = 3;
	public static final int width = 4;
	private List<Cell<Image>> cell;
	private List<String> file;
	private Label challenge_latin;
	private Image challenge_pic;
	private Cell<Actor> leftBottomCell;

	private Cell<Actor> leftTopCell;
	private TextButton mainMenu;
	private TextButton mute;

	private TextButton pause;

	private Label lbl_score;

	private UIProgressBar remaining;
	private Texture p_fg;
	private Texture p_bg;
	private boolean paused;
	private final GameSound gs;
	private final UI ui;
	private boolean doElapsed=true;

	public boolean isPaused() {
		return paused;
	}
	
	public void setPaused(boolean paused) {
		this.paused = paused;
		if (paused) {
			blocks.setVisible(false);
			blocks.setTouchable(Touchable.disabled);
			overlay_pause=ui.loadImage(UI.PAUSED);
			overlay_pause.setPosition(blocks.getX(), blocks.getY());
			overlay_pause.setWidth(blocks.getWidth());
			overlay_pause.setHeight(blocks.getHeight());
			overlay_pause.setScaling(Scaling.none);
			getStage().addActor(overlay_pause);
		} else {
			blocks.setVisible(true);
			blocks.setTouchable(Touchable.childrenOnly);
			overlay_pause.remove();
			overlay_pause.clear();
		}
	}
	
	public void showNewLetter(Card card){
		this.setDoElapsed(false);
		UIDialog dialog = new UIDialog("A New Letter", ui) {
			@Override
			protected void result(Object object) {
				GameBoard.this.paused=false;
			}
		};
		dialog.show(getStage());
	}

	public static class Res {
		public static final String question = "images/misc/003f_4.png";
		public static final String bar = "images/misc/bar.png";
		public static final String p_bg = "images/misc/progress_bg.png";
		public static final String p_fg = "images/misc/progress_fg.png";
		public static final String disc = "images/misc/25cf_4.png";
	}


	private Table blocks;
	private Image overlay_pause;
	public GameBoard(Stage stage, UI ui, GameSound gs) {
		this.ui=ui;
		this.gs=gs;
		stage.addActor(this);

		setFillParent(true);
		defaults().expandX().fill();
		
		Texture t = ui.loadTexture(UI.BG);
		TextureRegion tr = new TextureRegion(t);
		TiledDrawable background = new TiledDrawable(tr);
		setBackground(background);

		LabelStyle ls = ui.getLs();
		TextButtonStyle tbs = ui.getTbs();

		blocks = new Table() {
			@Override
			public void act(float delta) {
				if (needsLayout()) {
					return;
				}
				super.act(delta);
			}
		};
		blocks.setTouchable(Touchable.childrenOnly);
		blocks.defaults().pad(8);
		cell = new ArrayList<>();
		file = new ArrayList<>();
		Image disc = ui.loadImage(Res.disc);
		for (int iy = 0; iy < height; iy++) {
			blocks.row();
			for (int ix = 0; ix < width; ix++) {
				Cell<Image> image_cell = blocks.add(disc);
				cell.add(image_cell);
				file.add(null);
			}
		}
		final Image topFillerBar = ui.loadImage(Res.bar);
		topFillerBar.setScaling(Scaling.fit);
		final Image bottomFillerBar = ui.loadImage(Res.bar);
		bottomFillerBar.setScaling(Scaling.fit);

		lbl_score = new Label("000000000", ls);
		challenge_latin = new Label("gwa", ui.getLsXLarge());
		challenge_pic = new Image(ui.loadTextureRegionDrawable(Res.question));
		challenge_pic.setScaling(Scaling.fit);
		mainMenu = new TextButton("BACK", tbs);
		mute = new TextButton("MUTE", tbs);
		pause = new TextButton("PAUSE", tbs);
		pause.addListener(new ClickListener(){
			@Override
			public boolean touchDown(InputEvent event, float x, float y,
					int pointer, int button) {
				setPaused(!isPaused());
				pause.setChecked(isPaused());
				return true;
			}
		});
		p_bg = ui.loadTexture(Res.p_bg);
		p_fg = ui.loadTexture(Res.p_fg);
		remaining = new UIProgressBar(p_bg, p_fg);
		remaining.setValue(0, false, 0f);
		row();
		final Table leftColumn = new Table() {
			@Override
			public void layout() {
				super.layout();
				float col_height = getHeight();
				float blocks_height = blocks.getHeight();
				if (blocks_height > 0) {
					if (col_height - blocks_height > 6 * 24) {
						leftTopCell.setActor(topFillerBar).center();
						leftBottomCell.setActor(bottomFillerBar).center();
					} else {
						leftTopCell.clearActor();
						leftBottomCell.clearActor();
					}
				}
			}
		};
		leftColumn.defaults().pad(0).space(0).center();
		add(leftColumn).fill().expand();
		leftColumn.row();
		leftTopCell = leftColumn.add(new Actor()).center().expandY();
		leftColumn.row();
		leftColumn.add(blocks).pad(15).expand().fill();
		leftColumn.row();
		leftBottomCell = leftColumn.add(new Actor()).center().expandY();

		Table rightColumn = new Table();
		rightColumn.defaults().pad(0).space(0).center();
		add(rightColumn).right();
		rightColumn.row();
		rightColumn.add(remaining).fillX().pad(10).padTop(15).padBottom(0);
		rightColumn.row();
		rightColumn.add(lbl_score);
		rightColumn.row();
		rightColumn.add().expandY();
		rightColumn.row();
		rightColumn.add(challenge_pic);
		rightColumn.row();
		rightColumn.add(challenge_latin);
		rightColumn.row();
		rightColumn.add().expandY();
		rightColumn.row();
		rightColumn.add(pause).fillX();
		rightColumn.row();
		rightColumn.add(mute).fillX();
		rightColumn.row();
		rightColumn.add(mainMenu).fillX();
		leftColumn.toFront();
	}

	public float getRemaining() {
		return remaining.getValue();
	}

	private float percent_prev = -1f;
	
	public void setRemaining(float percent, float interval) {
		setRemaining(percent, true, interval);
	}

	public void setRemaining(float percent, boolean animate, float interval) {
		if (percent > 1f)
			percent = 1f;
		if (percent < 0f)
			percent = 0f;
		percent = Math.round(percent);
		if (percent != percent_prev) {
			remaining.setValue(percent, animate, interval);
			percent_prev=percent;
		}
	}

	public String getChallenge_latin() {
		return challenge_latin.getText().toString().intern();
	}

	public void setChallenge_latin(String challenge_latin) {
		this.challenge_latin.setText(challenge_latin.intern());
	}

	private int score;

	public int getScore() {
		return score;
	}

	StringBuilder score_sb = new StringBuilder(9);

	public void resetScore() {
		score = 0;
		updateScoreDisplay(score);
	}

	public synchronized void addToScore(int amount) {
		score += amount;
		float delay = 0f;
		final boolean whip = amount < 0;
		do  {
			amount /= 7;
			Action u1 = null;
			final int tmp = amount;
			u1 = Actions.run(new Runnable() {
				@Override
				public void run() {
					if (whip) {
						gs.whip_pop();
					} else {
						gs.ding();
					}
					updateScoreDisplay(score - tmp);
				}
			});
			Action s = Actions.sequence(Actions.delay(delay), u1);
			addAction(s);
			delay += .2f;
		} while (amount != 0);
	}

	private void updateScoreDisplay(int score) {
		lbl_score.setLayoutEnabled(false);
		score_sb.setLength(0);
		score_sb.append(score);
		if (score < 0) {
			score_sb.setCharAt(0, '0');
		}
		while (score_sb.length() < 9) {
			score_sb.insert(0, '0');
		}
		if (score < 0) {
			score_sb.setCharAt(0, '-');
		}
		lbl_score.setText(score_sb.toString());
		lbl_score.layout();
	}

	public Image getImageAt(int x, int y) {
		return cell.get(getIndex(x, y)).getActor();
	}

	public Cell<Image> getCellAt(int x, int y) {
		return cell.get(getIndex(x, y));
	}

	public void setColorAt(int x, int y, Color color) {
		Image actor = cell.get(getIndex(x, y)).getActor();
		if (actor!=null) {
			actor.setColor(color);
		}
	}
	
	public void setImageAt(int x, int y, String name) {
		int i = getIndex(x, y);
		cell.get(i).clearActor();
		if (file.get(i)!=null) {
			ui.unloadTexture(file.get(i));
			file.set(i, null);
		}
		if (name==null) {
			return;
		}
		Image img = ui.loadImage(name);
		img.setScaling(Scaling.fit);
		cell.get(i).setActor(img);
	}

	private static int getIndex(int x, int y) {
		x %= width;
		y %= height;
		if (x < 0)
			x = width + x;
		if (y < 0)
			y = height + y;
		int i = x+y*width;
		return i;
	}
	
	public void clearImages(){
		for (int x=0; x<width; x++) {
			for (int y=0; y<height; y++) {
				setImageAt(x, y, (String)null);
			}
		}
	}

	public boolean isDoElapsed() {
		return doElapsed;
	}

	public void setDoElapsed(boolean doElapsed) {
		this.doElapsed = doElapsed;
	}

	/**
	 * @return
	 */
	public boolean isActive() {
		return active;
	}

	private boolean active=false;
	public void setActive(boolean b) {
		active=b;
	}

	private String challenge_img;
	public void setChallenge_img(String file) {
		if (challenge_img!=null) {
			ui.unloadTexture(challenge_img);
		}
		challenge_img=file;
		if (challenge_img!=null) {
			challenge_pic.setDrawable(ui.loadTextureRegionDrawable(file));
		} else {
			challenge_pic.setDrawable(null);
		}
	}
}