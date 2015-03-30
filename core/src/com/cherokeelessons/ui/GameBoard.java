package com.cherokeelessons.ui;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Scaling;
import com.cherokeelessons.ui.UI.UIProgressBar;

public class GameBoard extends Table implements Disposable {
	public static final int height = 5;
	public static final int width = 7;
	private Texture bar = null;
	private List<Cell<Image>> cell;
	private List<String> glyph;
	private Label challenge_latin;
	private Image challenge_pic;
	private Cell<Image> challenge;
	private Cell<Actor> leftBottomCell;

	private Cell<Actor> leftTopCell;
	private TextButton mainMenu;
	private TextButton mute;

	private TextButton pause;

	private Texture question = null;
	private Label score;

	private UIProgressBar remaining;
	private Texture p_fg;
	private Texture p_bg;
	private final AssetManager manager;
	
	public static class Res {
		public static final String question = "images/misc/003f_4.png";
		public static final String bar = "images/misc/bar.png";
		public static final String p_bg = "images/misc/progress_bg.png";
		public static final String p_fg = "images/misc/progress_fg.png";
	}
	
	private void loadAssets(){
		TextureParameter tp = new TextureParameter();
		tp.magFilter=TextureFilter.Linear;
		tp.minFilter=TextureFilter.Linear;
		manager.load(Res.question, Texture.class, tp);
		manager.load(Res.bar, Texture.class, tp);
		manager.load(Res.p_bg, Texture.class, tp);
		manager.load(Res.p_fg, Texture.class, tp);
	}
	
	private void unloadAssets(){
		manager.unload(Res.question);
		manager.unload(Res.bar);
		manager.unload(Res.p_bg);
		manager.unload(Res.p_fg);
	}

	
	public GameBoard(Stage stage, AssetManager assetManager) {
		this.manager = assetManager;
		stage.addActor(this);
		loadAssets();
		manager.finishLoadingAsset(Res.question);
		question = manager.get(Res.question, Texture.class);		

		setFillParent(true);

		defaults().expandX().fill();

		Texture t = manager.get(UI.BG, Texture.class);
		TextureRegion tr = new TextureRegion(t);
		TiledDrawable background = new TiledDrawable(tr);
		Table btable = new Table();
		btable.setFillParent(true);
		btable.setBackground(background);
		btable.addAction(Actions.alpha(.5f));
		addActor(btable);

		LabelStyle ls = UI.getLs();
		TextButtonStyle tbs = UI.getTbs();

		final Table blocks = new Table() {
			@Override
			public void act(float delta) {
				if (needsLayout()) {
					return;
				}
				super.act(delta);
			}
		};
		blocks.defaults().pad(8);
		cell = new ArrayList<>();
		glyph = new ArrayList<>();
		Image i = new Image();
		for (int iy = 0; iy < height; iy++) {
			blocks.row();
			for (int ix = 0; ix < width; ix++) {
				Cell<Image> image_cell = blocks.add(i).expand().fill();
				cell.add(image_cell);
				glyph.add("");
			}
		}
		manager.finishLoadingAsset(Res.bar);
		bar = manager.get(Res.bar, Texture.class);
		final Image topFillerBar = new Image(bar);
		topFillerBar.setScaling(Scaling.fit);
		final Image bottomFillerBar = new Image(bar);
		bottomFillerBar.setScaling(Scaling.fit);

		score = new Label("000000000", ls);
		challenge_latin = new Label("GWA", ls);
		challenge_pic = new Image(question);
		challenge_pic.setScaling(Scaling.fit);
		mainMenu = new TextButton("Menu", tbs);
		mute = new TextButton("Mute", tbs);
		pause = new TextButton("Pause", tbs);
		manager.finishLoadingAsset(Res.p_bg);
		manager.finishLoadingAsset(Res.p_fg);
		p_bg = manager.get(Res.p_bg);
		p_fg = manager.get(Res.p_fg);
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
		leftColumn.add(blocks).pad(15);
		leftColumn.row();
		leftBottomCell = leftColumn.add(new Actor()).center().expandY();

		Table rightColumn = new Table();
		rightColumn.defaults().pad(0).space(0).center();
		add(rightColumn).right();
		rightColumn.row();
		rightColumn.add(remaining).fillX().pad(10).padTop(15).padBottom(0);
		rightColumn.row();
		rightColumn.add(score);
		rightColumn.row();
		rightColumn.add().expandY();
		rightColumn.row();
		rightColumn.add(challenge_latin);
		rightColumn.row();
		challenge = rightColumn.add(challenge_pic);
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
	
	public void setRemaining(float percent, float interval) {
		setRemaining(percent, true, interval);
	}
	
	public void setRemaining(float percent, boolean animate, float interval) {
		if (percent>1f) percent=1f;
		if (percent<0f) percent=0f;
		remaining.setValue(percent, animate, interval);
	}

	@Override
	public void dispose() {
		unloadAssets();
	}

	@Override
	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}

	public String getChallenge_latin() {
		return challenge_latin.getText().toString().intern();
	}

	public Image getChallenge_image() {
		return challenge.getActor();
	}
	
	public String getGlyphAt(int x, int y){
		return null;
	}
	
	public void setGlyphAt(int x, int y){
		
	}

	public Image getImageAt(int x, int y) {
		x %= width;
		y %= height;
		if (x < 0)
			x = width + x;
		if (y < 0)
			y = height + y;
		return cell.get(x + y * width).getActor();
	}
	
	public Cell<Image> getCellAt(int x, int y) {
		x %= width;
		y %= height;
		if (x < 0)
			x = width + x;
		if (y < 0)
			y = height + y;
		return cell.get(x + y * width);
	}
	
	public void setImageAt(int x, int y, Image img) {
		x %= width;
		y %= height;
		if (x < 0)
			x = width + x;
		if (y < 0)
			y = height + y;
		cell.get(x + y * width).setActor(img);
	}
	
	public void setChallenge_latin(String challenge_latin) {
		this.challenge_latin.setText(challenge_latin.intern());
	}
}