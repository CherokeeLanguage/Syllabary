package com.cherokeelessons.syllabary.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

public class LoadingScreen extends ChildScreen {

	private Texture[][] loading = new Texture[4][4];
	private Table img = new Table();

	public LoadingScreen() {
		super(null);
	}

	@Override
	public void show() {
		for (int ix = 0; ix < 8; ix++) {
			loading[ix % 4][ix / 4] = new Texture(
					Gdx.files.internal("img/loading/p_loading_" + ix + ".png"));
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
	public void render(float delta) {
		super.render(delta);
	}
}
