package com.cherokeelessons.syllabary.screens;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.cherokeelessons.syllabary.one.App;
import com.cherokeelessons.syllabary.one.Fonts;
import com.cherokeelessons.syllabary.one.GameSound;

public class Loading extends ChildScreen {

	private Texture[][] loading = new Texture[4][4];
	private Table img = new Table();

	public Loading() {
		super(null);
	}

	@Override
	public void show() {
		for (int ix = 0; ix < 8; ix++) {
			loading[ix % 4][ix / 4] = ui.loadTexture("images/loading/p_loading_" + ix + ".png");
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
		manager.load(GameSound.STARTUP, Music.class);
		
		Fonts.init();
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
		super.dispose();
	}

	private boolean sounddone=false;
	private boolean startup=false;
	@Override
	public void act(float delta) {
		super.act(delta);
		if (!startup && manager.isLoaded(GameSound.STARTUP)){
			startup=true;
			Music m = manager.get(GameSound.STARTUP, Music.class);
			m.setLooping(false);
			m.setVolume(1f);
			m.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(Music music) {
					music.dispose();
					manager.unload(GameSound.STARTUP);
					sounddone=true;
				}
			});
			m.play();
			return;
		}
		if (!Fonts.isLoaded()) {
			return;
		}
		if (manager.update() && sounddone) {
			App.getGame().setScreen(new MainMenu());
			this.dispose();
		}
	}
}
