package com.cherokeelessons.syllabary.one;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.cherokeelessons.syllabary.screens.LoadingScreen;

public class Syllabary extends Game {
	@Override
	public void create() {
		App.setGame(this);
		App.setClearColor(Color.WHITE);
		
		AssetManager manager = new AssetManager();
		App.setManager(manager);
		manager.load(App.Sound.STARTUP, Music.class);
		manager.load(UI.SKIN, Skin.class);
		Font.addFonts(manager);
		
		setScreen(new LoadingScreen());
	}
}
