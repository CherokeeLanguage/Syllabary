package com.cherokeelessons.syllabary.one;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.cherokeelessons.syllabary.one.App.PlatformTextInput;
import com.cherokeelessons.syllabary.screens.Loading;
import com.cherokeelessons.util.DreamLo;

public class SyllabaryApp extends Game {
	public PlatformTextInput pInput;

	@Override
	public void create() {
		Gdx.input.setCatchBackKey(true);
		Gdx.input.setCatchMenuKey(true);
		
		App.setGame(this);
		App.setClearColor(Color.WHITE);
		
		App.lb=new DreamLo(App.getPrefs());
		App.lb.registerWithDreamLoBoard();
		
		setScreen(new Loading());
	}
}
