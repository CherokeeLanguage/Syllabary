package com.cherokeelessons.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.cherokeelessons.syllabary.one.Fonts;
import com.cherokeelessons.syllabary.one.GameSound;

public class UI {
	
	public static final String PAUSED = "images/misc/paused.png";
	public static final String SKIN = "skins/holo/Holo-light-xhdpi.json";
	public static final String BG = "images/backgrounds/461223187.jpg";
	public static final String DISC = "images/misc/25cf_4.png";
	private final AssetManager manager;

	public UI(AssetManager manager) {
		this.manager=manager;
		manager.load(SKIN, Skin.class);
		manager.finishLoadingAsset(SKIN);
	}
	
	public Skin getSkin() {
		return manager.get(SKIN);
	}

	public TextButtonStyle getTbs() {
		TextButtonStyle tbs = new TextButtonStyle(getSkin().get(
				TextButtonStyle.class));
		tbs.font = Fonts.Medium.get();
		return tbs;
	}

	public WindowStyle getWs() {
		WindowStyle style = new WindowStyle(getSkin().get(WindowStyle.class));
		style.titleFont = Fonts.Large.get();
		return style;
	}

	public LabelStyle getLs() {
		LabelStyle style = new LabelStyle(getSkin().get(LabelStyle.class));
		style.font = Fonts.Medium.get();
		return style;
	}

	public ImageButtonStyle getIbs() {
		ImageButtonStyle style = new ImageButtonStyle(getSkin().get(
				ImageButtonStyle.class));
		return style;
	}

	public Table getMenuTable() {
		Table container = new Table();
		container.setFillParent(true);
		container.defaults().expand().fill();
		Texture t = getTextureFor(BG);
		TextureRegion tr = new TextureRegion(t);
		TiledDrawable background = new TiledDrawable(tr);
		Table btable = new Table();
		btable.setFillParent(true);
		btable.setBackground(background);
		btable.addAction(Actions.alpha(.5f));
		container.addActor(btable);
		return container;
	}

	public ScrollPaneStyle getSps() {
		ScrollPaneStyle style = new ScrollPaneStyle(getSkin().get(
				ScrollPaneStyle.class));
		return style;
	}

	public GameBoard getGameBoard(Stage stage, UI ui, GameSound gs) {
		GameBoard container = new GameBoard(stage, ui, gs);
		return container;
	}

	public ProgressBarStyle getPbs(Texture background, Texture foreground) {
		ProgressBarStyle style = new ProgressBarStyle();
		style.background = new TextureRegionDrawable(new TextureRegion(
				background));
		style.knobBefore = new TextureRegionDrawable(new TextureRegion(
				foreground));
		return style;
	}

	public ProgressBarStyle getPbsReversed(Texture background,
			Texture foreground) {
		ProgressBarStyle style = new ProgressBarStyle();
		style.background = new TextureRegionDrawable(new TextureRegion(
				background));
		style.knobAfter = new TextureRegionDrawable(new TextureRegion(
				foreground));
		return style;
	}

	public static class UIProgressBar extends WidgetGroup {
		private float value = 0f;
		private final Image b_img;
		private final Image f_img;
		private final float p_width;
		private final float p_height;

		UIProgressBar(Texture background, Texture foreground) {
			b_img = new Image(new TextureRegion(background));
			f_img = new Image(new TextureRegion(foreground));
			this.addActor(b_img);
			this.addActor(f_img);
			b_img.setScaling(Scaling.stretch);
			f_img.setScaling(Scaling.stretch);
			setValue(0f, 0f);
			p_width = Math.max(b_img.getWidth(), f_img.getWidth());
			p_height = Math.max(b_img.getHeight(), f_img.getHeight());
		}

		@Override
		public float getPrefHeight() {
			return p_height;
		}

		@Override
		public float getPrefWidth() {
			return p_width;
		}

		public float getValue() {
			return value;
		}

		public void setValue(float value, boolean animate, float interval) {
			this.value = value;
			for (Action a : f_img.getActions()) {
				if (a instanceof TemporalAction) {
					((TemporalAction) a).finish();
				}
			}
			f_img.addAction(Actions.scaleTo(value, 1f, animate ? interval : 0f,
					Interpolation.linear));
		}

		public void setValue(float value, float interval) {
			setValue(value, true, interval);
		}

		@Override
		protected void sizeChanged() {
			f_img.setWidth(getWidth());
			b_img.setWidth(getWidth());
			super.sizeChanged();
		}
	}

	public static double luminance(Color color) {
		return luminance(color.r, color.g, color.b) * color.a;
	}

	public static double luminance(double r, double g, double b) {
		if (r < .03928d)
			r = r / 12.92d;
		else
			r = Math.pow((r + .055d) / 1.055d, 2.4);
		if (g < .03928d)
			g = g / 12.92d;
		else
			g = Math.pow((g + .055d) / 1.055d, 2.4);
		if (b < .03928d)
			b = b / 12.92d;
		else
			b = Math.pow((b + .055d) / 1.055d, 2.4);
		return 0.2126d * r + .7152d * g + .0722d * b;
	}

	public Image getImageFor(String name) {
		return new Image(getTextureFor(name));
	}
	
	public Texture getTextureFor(String name) {
		TextureParameter tp = new TextureParameter();
		tp.magFilter = TextureFilter.Linear;
		tp.minFilter = TextureFilter.Linear;
		manager.load(name, Texture.class, tp);
		manager.finishLoadingAsset(name);
		return manager.get(name, Texture.class);
	}
}
