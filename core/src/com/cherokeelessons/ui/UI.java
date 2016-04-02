package com.cherokeelessons.ui;

import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.cherokeelessons.cards.DisplayMode;
import com.cherokeelessons.cards.SlotInfo;
import com.cherokeelessons.syllabary.one.App;
import com.cherokeelessons.syllabary.one.Fonts;
import com.cherokeelessons.syllabary.one.GameSound;
import com.cherokeelessons.util.GooglePlayGameServices.Callback;

public class UI {

	public static final String PAUSED = "images/misc/paused.png";
	public static final String SKIN = "skins/holo/Holo-light-xhdpi.json";
	public static final String BG = "images/backgrounds/461223187_50.jpg";
	public static final String DISC = "images/misc/25cf_4.png";
	public static final String WHITE = "images/misc/white-dot.png";
	public static final String BLACK = "images/misc/black-dot.png";
	public static final String DIM = "images/misc/dim.png";

	public static final String IMG_SETTINGS = "images/misc/gear.png";
	public static final String IMG_ERASE = "images/misc/trash.png";
	public static final String IMG_SYNC = "images/misc/refresh.png";
	public static final String HEAVYX = "images/misc/2718_5.png";
	public static final String CHECKMARK = "images/misc/2714_5.png";

	public static final String DIALOG9 = "images/ninepatch/dialog-9patch.png";

	private final AssetManager manager;
	private Callback<Void> noop = new Callback<Void>() {
		@Override
		public void success(Void result) {
		}
	};

	public UI(AssetManager manager) {
		this.manager = manager;
		manager.load(SKIN, Skin.class);
		manager.finishLoadingAsset(SKIN);
	}

	public Skin getSkin() {
		return manager.get(SKIN);
	}

	public TextButtonStyle getTbsLarge() {
		TextButtonStyle tbs = new TextButtonStyle(getSkin().get(TextButtonStyle.class));
		tbs.font = Fonts.Large.get();
		return tbs;
	}

	public TextButtonStyle getTbs() {
		TextButtonStyle tbs = new TextButtonStyle(getSkin().get(TextButtonStyle.class));
		tbs.font = Fonts.Medium.get();
		return tbs;
	}

	public TextButtonStyle getTbsSmall() {
		TextButtonStyle tbs = new TextButtonStyle(getSkin().get(TextButtonStyle.class));
		tbs.font = Fonts.Small.get();
		return tbs;
	}

	public NinePatch getDialog9() {
		Texture t9 = loadTexture(DIALOG9);
		int left = 40;
		int right = 40;
		int top = 40;
		int bottom = 40;
		NinePatch p9 = new NinePatch(t9, left, right, top, bottom);
		return p9;
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

	public LabelStyle getLsLarge() {
		LabelStyle style = new LabelStyle(getSkin().get(LabelStyle.class));
		style.font = Fonts.Large.get();
		return style;
	}

	public ImageButtonStyle getIbs() {
		ImageButtonStyle style = new ImageButtonStyle(getSkin().get(ImageButtonStyle.class));
		return style;
	}

	public Table getMenuTable() {
		Table container = new Table();
		container.setFillParent(true);
		container.defaults().expand().fill();
		Texture t = loadTexture(BG);
		TextureRegion tr = new TextureRegion(t);
		TiledDrawable background = new TiledDrawable(tr);
		container.setBackground(background);
		return container;
	}

	public ScrollPaneStyle getSps() {
		ScrollPaneStyle style = new ScrollPaneStyle(getSkin().get(ScrollPaneStyle.class));
		return style;
	}

	public GameBoard getGameBoard(Stage stage, UI ui, GameSound gs) {
		GameBoard container = new GameBoard(stage, ui, gs);
		return container;
	}

	public ProgressBarStyle getPbs(Texture background, Texture foreground) {
		ProgressBarStyle style = new ProgressBarStyle();
		style.background = new TextureRegionDrawable(new TextureRegion(background));
		style.knobBefore = new TextureRegionDrawable(new TextureRegion(foreground));
		return style;
	}

	public ProgressBarStyle getPbsReversed(Texture background, Texture foreground) {
		ProgressBarStyle style = new ProgressBarStyle();
		style.background = new TextureRegionDrawable(new TextureRegion(background));
		style.knobAfter = new TextureRegionDrawable(new TextureRegion(foreground));
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
			f_img.addAction(Actions.scaleTo(value, 1f, animate ? interval : 0f, Interpolation.linear));
		}

		public void setValue(float value, float interval) {
			setValue(value, true, interval);
		}

		@Override
		protected void sizeChanged() {
			super.sizeChanged();
			f_img.setWidth(getWidth());
			b_img.setWidth(getWidth());
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

	public Image loadImage(String name) {
		return new Image(loadTexture(name));
	}

	public TextureRegionDrawable loadTextureRegionDrawable(String name) {
		return new TextureRegionDrawable(new TextureRegion(loadTexture(name)));
	}

	public Texture loadTexture(String name) {
		TextureParameter tp = new TextureParameter();
		tp.magFilter = TextureFilter.Linear;
		tp.minFilter = TextureFilter.Linear;
		manager.load(name, Texture.class, tp);
		manager.finishLoadingAsset(name);
		return manager.get(name, Texture.class);
	}

	public void unloadTexture(String name) {
		manager.unload(name);
	}

	/*
	 * Customized window style for dialogs that has a "dimmed" stage background.
	 * Really needs to use a nine-path for the direct background to have proper
	 * borders.
	 */
	public WindowStyle getDialogStyle(boolean dim) {
		WindowStyle ws = getWs();
		ws.titleFont = Fonts.Large.get();
		TiledDrawable td = new TiledDrawable(new TextureRegion(loadTexture(BG)));
		td.setMinHeight(0);
		td.setMinWidth(0);
		td.setTopHeight(ws.titleFont.getCapHeight() + 20);
		ws.background = td;
		if (dim) {
			ws.stageBackground = new TextureRegionDrawable(new TextureRegion(loadTexture(DIM)));
		}
		return ws;
	}

	public WindowStyle getDialogStyle9(boolean dim) {
		NinePatch dialog9 = getDialog9();
		NinePatchDrawable background = new NinePatchDrawable(dialog9);
		WindowStyle ws = getWs();
		ws.titleFont = Fonts.Large.get();
		background.setTopHeight(ws.titleFont.getCapHeight() + 48);
		ws.background = background;
		if (dim) {
			ws.stageBackground = new TextureRegionDrawable(new TextureRegion(loadTexture(DIM)));
		}
		return ws;
	}

	public static class UIDialog extends Dialog {
		private final UI ui;

		public UIDialog(String title, UI ui) {
			this(title, false, false, ui);
			this.getTitleLabel().setAlignment(Align.center);
		}

		public UIDialog(String title, boolean dim, boolean nine, UI ui) {
			super(title, nine ? ui.getDialogStyle9(dim) : ui.getDialogStyle(dim));
			this.getTitleLabel().setAlignment(Align.center);
			this.ui = ui;
		}

		public UIDialog textCenter(String text) {
			Label lbl = new Label(text, ui.getLs());
			lbl.setAlignment(Align.center);
			text(lbl);
			return this;
		}

		@Override
		public UIDialog text(String text) {
			Label lbl = new Label(text, ui.getLs());
			text(lbl);
			return this;
		}

		@Override
		public UIDialog button(String text) {
			TextButton tb = new TextButton(text, ui.getTbs());
			button(tb);
			return this;
		}

		@Override
		public UIDialog button(String text, Object object) {
			TextButton tb = new TextButton(text, ui.getTbs());
			button(tb, object);
			return this;
		}

		@Override
		public UIDialog show(Stage stage, Action action) {
			super.show(stage, action);
			setPosition(Math.round((stage.getWidth() - getWidth()) / 2),
					Math.round((stage.getHeight() - getHeight()) / 2));
			return this;
		}
	}

	public UIDialog getMainSlotDialog(final SlotsDialogHandler handler) {
		final UIDialog dialog = new UIDialog("Select Session", this);
		dialog.setModal(true);
		dialog.setFillParent(true);

		Table slots = new Table();
		ScrollPane scroll = new ScrollPane(slots, getSps());
		scroll.setFadeScrollBars(false);
		scroll.setColor(Color.DARK_GRAY);

		dialog.getContentTable().add(scroll).expand().fill();

		for (int ix = 0; ix < 4; ix++) {
			SlotInfo info;
			info = App.getSlotInfo(ix);
			info.validate();
			if (!info.isUpdatedVersion()) {
				info.recalculateStats();
				if (info.activeCards > 0) {
					App.lb.lb_submit(ix + "", info.activeCards, info.lastScore,
							info.level.getEnglish() + "!!!" + info.settings.name, noop);
				}
				App.saveSlotInfo(ix, info);
			}
			String displayName = info.settings.name;
			boolean isBlank = false;
			if (StringUtils.isBlank(displayName) && info.activeCards == 0) {
				displayName = "** BLANK **";
				isBlank = true;
			}
			StringBuilder sb = new StringBuilder();
			sb.append(info.level.getEnglish());
			sb.append(" ");
			sb.append(StringUtils.isBlank(displayName) ? "ᎤᏲᏒ ᏥᏍᏕᏥ!" : displayName);
			sb.append("\n");
			sb.append(info.activeCards);
			sb.append(" letters: ");
			sb.append(info.shortTerm);
			sb.append(" short, ");
			sb.append(info.mediumTerm);
			sb.append(" medium, ");
			sb.append(info.longTerm);
			sb.append(" long");
			TextButtonStyle tbs = getTbs();
			TextButton textb = new TextButton(sb.toString(), tbs);
			Image icon = new Image(loadTexture("images/levels/" + info.level.getLevel() + "-75.png"));
			slots.row();
			slots.add(icon).pad(5).left();
			slots.add(textb).pad(0).expand().fill().left();
			Table editControls = new Table();
			editControls.center();
			editControls.defaults().pad(10);
			ImageButton editb = getImageButton(IMG_SETTINGS);
			ImageButton deleteb = getImageButton(IMG_ERASE);
			ImageButton syncb = getImageButton(IMG_SYNC);
			editControls.add(editb).center();
			editControls.add(deleteb).center();
			editControls.add(syncb).center();
			slots.add(editControls);
			if (isBlank) {
				editb.setDisabled(true);
				editb.setTouchable(Touchable.disabled);
				editb.getImage().setColor(Color.CLEAR);
				deleteb.setDisabled(true);
				deleteb.setTouchable(Touchable.disabled);
				deleteb.getImage().setColor(Color.CLEAR);
			}
			final int slot = ix;
			textb.addListener(new ClickListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					handler.play(slot);
					return true;
				}
			});
			editb.addListener(new ClickListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					handler.edit(slot);
					return true;
				}
			});
			deleteb.addListener(new ClickListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					handler.erase(slot);
					return true;
				}
			});
			syncb.addListener(new ClickListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					final UIDialog busy = new UIDialog("Sync Service", true, true, UI.this);
					busy.text("Device sync in progress...");
					busy.button("HIDE");
					Stage stage = dialog.getStage();
					if (stage != null) {
						busy.show(stage);
					}
					final Runnable nobusy = new Runnable() {
						@Override
						public void run() {
							Gdx.app.log(this.getClass().getSimpleName(), "busy#hide");
							busy.hide();
							handler.reload();
						}
					};
					handler.sync(slot, nobusy);
					return true;
				}
			});
		}
		return dialog;
	}

	public ImageButton getImageButton(String texture) {
		Texture textureFor = loadTexture(texture);
		TextureRegion region = new TextureRegion(textureFor);
		TextureRegionDrawable imageUp = new TextureRegionDrawable(region);
		ImageButton imageButton = new ImageButton(imageUp);
		imageButton.getImage().setScaling(Scaling.fit);
		imageButton.getImage().setColor(Color.DARK_GRAY);
		return imageButton;
	}

	public TextFieldStyle getTfs() {
		TextFieldStyle tfs = new TextFieldStyle(getSkin().get(TextFieldStyle.class));
		tfs.font = Fonts.Medium.get();
		return tfs;
	}

	public UIDialog getSlotEditDialog(final SlotInfo info) {
		return getSlotEditDialog(info, null);
	}

	public UIDialog getSlotEditDialog(final SlotInfo info, final Runnable whenDone) {

		App.log(this, "getSlotEditDialog: " + info.slot);

		TextButtonStyle tbs = getTbs();
		TextFieldStyle tfs = getTfs();

		info.validate();
		if (info.lastrun != 0) {
			info.settings.name = (StringUtils.isBlank(info.settings.name)) ? "ᏐᏈᎵ ᏂᏧᏙᎥᎾ" : info.settings.name;
		}

		final TextField name = new TextField(info.settings.name, tfs);
		name.setDisabled(true);
		name.setTouchable(Touchable.enabled);
		name.addListener(new ClickListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				name.setTouchable(Touchable.disabled);
				TextInputListener listener = new TextInputListener() {
					@Override
					public void input(String text) {
						name.setText(text);
						name.setTouchable(Touchable.enabled);
					}

					@Override
					public void canceled() {
						name.setTouchable(Touchable.enabled);
					}
				};
				if (App.getGame().pInput == null) {
					Gdx.input.getTextInput(listener, "Profile Name?", name.getText(), "");
				} else {
					App.getGame().pInput.getTextInput(listener, "Profile Name?", name.getText(), "");
				}
				return true;
			}
		});

		final TextButton mode = new TextButton(info.settings.display.toString(), tbs);
		mode.addListener(new ClickListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				info.settings.display = DisplayMode.getNext(info.settings.display);
				mode.setText(info.settings.display.toString());
				return true;
			}
		});

		final TextButton muted = new TextButton(info.settings.muted ? "Yes" : "No", tbs);
		muted.addListener(new ClickListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				info.settings.muted = !info.settings.muted;
				muted.setText(info.settings.muted ? "Yes" : "No");
				return true;
			}
		});

		final TextButton training = new TextButton(info.settings.skipTraining ? "Yes" : "No", tbs);
		training.addListener(new ClickListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				info.settings.skipTraining = !info.settings.skipTraining;
				training.setText(info.settings.skipTraining ? "Yes" : "No");
				return true;
			}
		});

		final TextButton blackLetters = new TextButton(info.settings.blackTiles ? "Yes" : "No", tbs);
		blackLetters.addListener(new ClickListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				info.settings.blackTiles = !info.settings.blackTiles;
				blackLetters.setText(info.settings.blackTiles ? "Yes" : "No");
				return true;
			}
		});

		final TextButton ok = new TextButton("OK", tbs);
		final TextButton cancel = new TextButton("CANCEL", tbs);

		UIDialog dialog = new UIDialog("Settings", this) {
			protected void result(Object object) {
				if (ok.equals(object)) {
					if (whenDone != null) {
						info.settings.name = name.getText();
						Gdx.app.postRunnable(whenDone);
					}
				}
				this.clear();
			};

			@Override
			public Dialog show(Stage stage) {
				super.show(stage);
				stage.setKeyboardFocus(name);
				stage.setScrollFocus(name);
				name.setCursorPosition(name.getText().length());
				return this;
			}
		};

		LabelStyle ls = getLs();
		final Table contentTable = dialog.getContentTable();
		dialog.setFillParent(true);
		contentTable.clearChildren();

		contentTable.row();
		Label lbl_name = new Label("Name: ", ls);
		contentTable.add(lbl_name).left().fillX();
		contentTable.add(name).expand().fillX().left();

		contentTable.row();
		Label lbl_display = new Label("Display: ", ls);
		contentTable.add(lbl_display).left().fillX();
		contentTable.add(mode).expand().fillX().left();

		contentTable.row();
		Label lbl_blackTiles = new Label("Show only solid black letters?", ls);
		contentTable.add(lbl_blackTiles).left().fillX();
		contentTable.add(blackLetters).expand().fillX().left();

		contentTable.row();
		Label lbl_training = new Label("Skip new letter training?", ls);
		contentTable.add(lbl_training).left().fillX();
		contentTable.add(training).expand().fillX().left();

		contentTable.row();
		Label lbl_mute_effects = new Label("Mute Sound Effects? ", ls);
		contentTable.add(lbl_mute_effects).left().fillX();
		contentTable.add(muted).expand().fillX().left();
		contentTable.row();

		dialog.button(ok, ok);
		dialog.button(cancel, cancel);

		return dialog;
	}

	public UIDialog getYesNoDialog(String msg, final Runnable ifYes, final Runnable ifNo) {
		TextButton yes = new TextButton("Yes", getTbs());
		TextButton no = new TextButton("No", getTbs());
		msg = WordUtils.wrap(msg, 60, "\n", true);
		Label msglabel = new Label(msg, getLs());
		UIDialog dialog = new UIDialog("Please Choose", true, true, this) {
			@Override
			protected void result(Object object) {
				if (object == null) {
					return;
				}
				if (object.equals(ifYes)) {
					Gdx.app.postRunnable(ifYes);
					return;
				}
				if (object.equals(ifNo)) {
					Gdx.app.postRunnable(ifNo);
					return;
				}
			}
		};
		dialog.text(msglabel);
		dialog.button(yes, ifYes);
		dialog.button(no, ifNo);
		return dialog;
	}

	public UIDialog getReadyDialog(final Runnable whenDone) {
		TextButton Yes = new TextButton("OK", getTbs());
		UIDialog dialog = new UIDialog("", this) {
			@Override
			protected void result(Object object) {
				if (whenDone != null) {
					Gdx.app.postRunnable(whenDone);
				}
			}
		};
		dialog.getStyle().stageBackground = new TextureRegionDrawable(new TextureRegion(loadTexture(DIM)));
		dialog.getStyle().background.setTopHeight(0f);
		dialog.setStyle(dialog.getStyle());
		dialog.text(new Label("Begin!", getLsLarge()));
		dialog.button(Yes);
		return dialog;
	}

	public static Color randomBrightColor() {
		Random r = new Random();
		Color img_color;
		do {
			img_color = new Color(r.nextFloat(), r.nextFloat(), r.nextFloat(), 1f);
		} while (UI.luminance(img_color) < .4);
		return img_color;
	}

	public LabelStyle getLsXLarge() {
		LabelStyle style = new LabelStyle(getSkin().get(LabelStyle.class));
		style.font = Fonts.XLarge.get();
		return style;
	}

	public LabelStyle getLsSmall() {
		LabelStyle style = new LabelStyle(getSkin().get(LabelStyle.class));
		style.font = Fonts.Small.get();
		return style;
	}

	public UIDialog errorDialog(final Exception e, final Runnable done) {
		UIDialog error = new UIDialog("Sync Service", true, true, this) {
			@Override
			protected void result(Object object) {
				if (done != null) {
					Gdx.app.postRunnable(done);
				}
			}
		};
		error.button("OK");
		String msgtxt = e.getMessage();
		msgtxt = WordUtils.wrap(msgtxt, 45, "\n", true);
		error.textCenter(msgtxt);
		error.setKeepWithinStage(true);
		return error;
	}
}
