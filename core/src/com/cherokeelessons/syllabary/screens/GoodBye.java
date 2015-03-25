package com.cherokeelessons.syllabary.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;

public class GoodBye extends ChildScreen {
	
	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
		case Keys.BACK:
		case Keys.ESCAPE:
			return true;
		default:
		}
		return super.keyDown(keycode);
	}

	public GoodBye(Screen caller) {
		super(caller);
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(null);		
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.app.exit();
	}
}
