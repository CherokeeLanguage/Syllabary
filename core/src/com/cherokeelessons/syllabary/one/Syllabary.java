package com.cherokeelessons.syllabary.one;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.cherokeelessons.syllabary.screens.Loading;

public class Syllabary extends Game {
	@Override
	public void create() {
		
		Gdx.input.setCatchBackKey(true);
		Gdx.input.setCatchMenuKey(true);
		
		App.setGame(this);
		App.setClearColor(Color.WHITE);
		
		setScreen(new Loading());
	}
}
