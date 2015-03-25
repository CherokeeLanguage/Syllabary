package com.cherokeelessons.syllabary.one;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.cherokeelessons.syllabary.screens.LoadingScreen;

public class Syllabary extends Game {
	@Override
	public void create() {
		
		Gdx.input.setCatchBackKey(true);
		Gdx.input.setCatchMenuKey(true);
		
		App.setGame(this);
		App.setClearColor(Color.WHITE);
		
		AssetManager manager = new AssetManager();
		App.setManager(manager);
		manager.load(App.Sound.STARTUP, Music.class);
		UI.load(manager);
		Font.addFonts(manager);
		
		setScreen(new LoadingScreen());
	}
}
