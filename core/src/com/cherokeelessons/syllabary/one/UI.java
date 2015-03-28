package com.cherokeelessons.syllabary.one;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Scaling;

public class UI {
	public static final String SKIN = "skins/holo/Holo-light-xhdpi.json";
	public static final String BG = "images/backgrounds/461223187.jpg";

	public static void load(AssetManager manager) {

		TextureParameter tp = new TextureParameter();
		tp.magFilter = TextureFilter.Linear;
		tp.minFilter = TextureFilter.Linear;

		manager.load(BG, Texture.class, tp);
		manager.load(SKIN, Skin.class);
	}

	public static Skin getSkin() {
		return App.getManager().get(SKIN);
	}

	public static TextButtonStyle getTbs() {
		TextButtonStyle tbs = new TextButtonStyle(getSkin().get(
				TextButtonStyle.class));
		tbs.font=Font.Medium.get();
		return tbs;
	}

	public static WindowStyle getWs() {
		WindowStyle style = new WindowStyle(getSkin().get(WindowStyle.class));
		style.titleFont=Font.Large.get();
		return style;
	}

	public static LabelStyle getLs() {
		LabelStyle style = new LabelStyle(getSkin().get(LabelStyle.class));
		style.font=Font.Medium.get();
		return style;
	}

	public static ImageButtonStyle getIbs() {
		ImageButtonStyle style = new ImageButtonStyle(getSkin().get(
				ImageButtonStyle.class));
		return style;
	}

	public static Table getMenuTable() {
		Table container = new Table();
		container.setFillParent(true);
		container.defaults().expand().fill();
		Texture t = App.getManager().get(BG, Texture.class);
		TextureRegion tr = new TextureRegion(t);
		TiledDrawable background = new TiledDrawable(tr);
		Table btable = new Table();
		btable.setFillParent(true);
		btable.setBackground(background);
		btable.addAction(Actions.alpha(.5f));
		container.addActor(btable);
		return container;
	}

	public static ScrollPaneStyle getSps() {
		ScrollPaneStyle style = new ScrollPaneStyle(getSkin().get(
				ScrollPaneStyle.class));
		return style;
	}

	public static GameBoard getGameBoard() {
		GameBoard container = new GameBoard();
		return container;
	}

	public static class GameBoard extends Table implements Disposable {
		public static final int width = 7;
		public static final int height = 5;
		private List<Cell<Image>> cell;
		private Cell<Actor> leftTopCell;
		private Cell<Actor> leftBottomCell;
		private Texture bar = null;

		public GameBoard() {
			setFillParent(true);
			setTransform(true);
			defaults().expandX().fill();
			
			Texture t = App.getManager().get(BG, Texture.class);
			TextureRegion tr = new TextureRegion(t);
			TiledDrawable background = new TiledDrawable(tr);
			Table btable = new Table();
			btable.setFillParent(true);
			btable.setBackground(background);
			btable.addAction(Actions.alpha(.5f));
			addActor(btable);

			LabelStyle ls = getLs();
			TextButtonStyle tbs = getTbs();

			final Table blocks = new Table();
			blocks.defaults().pad(8);
			cell = new ArrayList<>();
			Image i = new Image();
			for (int iy = 0; iy < height; iy++) {
				blocks.row();
				for (int ix = 0; ix < width; ix++) {
					cell.add(blocks.add(i).expand().fill());
				}
			}
			for (int iy = 0; iy < height; iy++) {
				for (int ix = 0; ix < width; ix++) {
					int letter = new Random().nextInt('Ᏼ' - 'Ꭰ') + 'Ꭰ';
					int font = new Random().nextInt(4);
					String glyph = Integer.toHexString(letter).toLowerCase();
					String path = "images/glyphs/" + glyph + "_" + font
							+ ".png";
					FileHandle file = Gdx.files.internal(path);
					Texture syl_text = new Texture(file);
					syl_text.setFilter(TextureFilter.Linear,
							TextureFilter.Linear);
					final Image img = new Image(syl_text);
					img.setScaling(Scaling.fit);
					img.setColor(new Color(new Random().nextFloat(),
							new Random().nextFloat(), new Random().nextFloat(),
							1f));
					img.addListener(new ClickListener() {
						@Override
						public boolean touchDown(InputEvent event, float x,
								float y, int pointer, int button) {
							img.setColor(new Color(new Random().nextFloat(),
									new Random().nextFloat(), new Random()
											.nextFloat(), 1f));
							App.log("wxh: "+cell.get(0).getActorWidth()+"x"+cell.get(0).getActorHeight());
							return true;
						}

					});
					setGlyph(ix, iy, img);
				}
			}

			challenge = new Label("GWA", ls);
			score = new Label("000000000", ls);
			mainMenu = new TextButton("Menu", tbs);
			mute = new TextButton("Mute", tbs);
			pause = new TextButton("Pause", tbs);

			row();
			final Table leftColumn = new Table() {
				@Override
				public void layout() {
					super.layout();
					float col_height = getHeight();
					float blocks_height = blocks.getHeight();
					if (blocks_height>0) {
						App.log(this, "column height: "+col_height);
						App.log(this, "blocks height: "+blocks_height);
						if (col_height-blocks_height>6*24) {
							if (bar==null) {
								bar = new Texture(Gdx.files.internal("images/misc/bar.png"));
								bar.setFilter(TextureFilter.Linear, TextureFilter.Linear);
								leftTopCell.setActor(new Image(bar)).center();
								leftBottomCell.setActor(new Image(bar)).center();
							}
						} else {
							leftTopCell.clearActor();
							leftBottomCell.clearActor();
							if (bar!=null) {
								bar.dispose();
								bar=null;
							}
						}
					}
				}				
			};
			add(leftColumn).fill().expand();
			leftColumn.defaults().pad(0).space(0).center();
			leftColumn.row();
			leftTopCell = leftColumn.add(new Actor()).center().expandY();
			leftColumn.row();
			leftColumn.add(blocks).pad(20);
			leftColumn.row();
			leftBottomCell = leftColumn.add(new Actor()).center().expandY();
			
			Table rightColumn = new Table();
			rightColumn.defaults().pad(0).space(0).center();
			add(rightColumn).right();
			
			rightColumn.row();
			rightColumn.add(score);
			rightColumn.row();
			rightColumn.add(challenge);
			rightColumn.row();
			rightColumn.add().expandY();
			rightColumn.row();
			rightColumn.add(mute).fill();
			rightColumn.row();
			rightColumn.add(pause).fill();
			rightColumn.row();
			rightColumn.add(mainMenu).fill();
		}
		
		public Label challenge;
		public Label score;
		public TextButton mainMenu;
		public TextButton mute;
		public TextButton pause;

		public Image getGlyph(int x, int y) {
			return cell.get(x + y * width).getActor();
		}

		public void setGlyph(int x, int y, Image img) {
			Cell<Image> c = cell.get(x + y * width);
			c.setActor(img);
		}

		@Override
		public void dispose() {
			if (bar!=null) {
				bar.dispose();
				bar=null;
			}
		}
	}

}
