package com.cherokeelessons.syllabary.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.cherokeelessons.syllabary.one.App;

public class Loading extends ChildScreen {

	private Texture[][] loading = new Texture[4][4];
	private Table img = new Table();

	public Loading() {
		super(null);
	}

	@Override
	public void show() {
		for (int ix = 0; ix < 8; ix++) {
			loading[ix % 4][ix / 4] = new Texture(
					Gdx.files.internal("images/loading/p_loading_" + ix + ".png"));
			loading[ix % 4][ix / 4].setFilter(TextureFilter.Linear, TextureFilter.Linear);
		}
		stage.clear();
		img.clear();
		img.defaults().pad(0).space(0);
		for (int y = 0; y < 2; y++) {
			img.row();
			for (int x = 0; x < 4; x++) {				
				Image part = new Image(new TextureRegionDrawable(new TextureRegion(loading[x][y])));
				part.setScaling(Scaling.fit);
				img.add(part);
			}
		}
		stage.addActor(img);
		img.setFillParent(true);
	}

	@Override
	public void hide() {
		super.hide();
		for (int ix = 0; ix < 8; ix++) {
			loading[ix % 4][ix / 4].dispose();
			loading[ix % 4][ix / 4] = null;
		}
		stage.clear();
		img.clear();
	}
	
	@Override
	public void dispose() {
		log("dispose");
		super.dispose();
	}

	boolean startup=false;
	@Override
	public void act(float delta) {
		super.act(delta);
		final AssetManager manager = App.getManager();
		if (!startup && manager.isLoaded(App.Sound.STARTUP)){
			startup=true;
			Music m = manager.get(App.Sound.STARTUP, Music.class);
			m.setLooping(false);
			m.setVolume(1f);
			m.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(Music music) {
					music.dispose();
					manager.unload(App.Sound.STARTUP);
				}
			});
			m.play();
			return;
		}
		if (manager.update()) {
			App.getGame().setScreen(new MainMenu());
		}
	}
}
