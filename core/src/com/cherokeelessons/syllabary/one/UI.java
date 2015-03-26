package com.cherokeelessons.syllabary.one;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
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
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;

public class UI {
	public static final String SKIN = "skins/holo/Holo-light-xhdpi.json";
	public static final String BG = "images/backgrounds/461223187.jpg";

	public static void load(AssetManager manager){
		
		TextureParameter tp = new TextureParameter();
		tp.magFilter=TextureFilter.Linear;
		tp.minFilter=TextureFilter.Linear;
		
		manager.load(BG, Texture.class, tp);
		manager.load(SKIN, Skin.class);		
	}
	
	public static Skin getSkin() {
		return App.getManager().get(SKIN);
	}

	public static TextButtonStyle getTbs() {
		TextButtonStyle tbs = new TextButtonStyle(getSkin().get(TextButtonStyle.class));
		return tbs;
	}
	
	public static WindowStyle getWs() {
		WindowStyle style = new WindowStyle(getSkin().get(WindowStyle.class));
		return style;
	}
	
	public static LabelStyle getLs() {
		LabelStyle style = new LabelStyle(getSkin().get(LabelStyle.class));
		return style;
	}
	
	public static ImageButtonStyle getIbs() {
		ImageButtonStyle style = new ImageButtonStyle(getSkin().get(ImageButtonStyle.class));
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
		ScrollPaneStyle style = new ScrollPaneStyle(getSkin().get(ScrollPaneStyle.class));
		return style;
	}

	public static GameBoard getGameBoard() {
		GameBoard container = new GameBoard();
		return container;
	}
	
	public static class GameBoard extends Table {
		public static final int width=9;
		public static final int height=6;
		private List<Cell<Image>> cell;
		public GameBoard() {
			setFillParent(true);
			defaults().expand().fill();
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
			ImageButtonStyle ibs = getIbs();
			
			Table blocks = new Table();
			cell=new ArrayList<>();
			Image i = new Image();
			for (int iy=0; iy<height; iy++) {
				blocks.row();
				for (int ix=0; ix<width; ix++) {
					cell.add(blocks.add(i));
				}
			}
		}
		public Label challenge;
		public Label score;
		public TextButton mainMenu;
		public TextButton mute;
		public TextButton pause;
		public Image getGlyph(int x, int y){
			return cell.get(x+y*width).getActor();
		}
		public void setGlyph(int x, int y, Image img) {
			cell.get(x+y*width).setActor(img);
		}
	}
	
}
