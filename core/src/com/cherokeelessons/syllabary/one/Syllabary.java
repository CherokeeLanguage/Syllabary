package com.cherokeelessons.syllabary.one;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;

public class Syllabary extends Game {
	@Override
	public void create() {
		App.setGame(this);
		App.setManager(new AssetManager());
		App.setClearColor(Color.WHITE);
	}
}
