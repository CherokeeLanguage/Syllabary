package com.cherokeelessons.syllabary.screens;

import com.badlogic.gdx.Screen;
import com.cherokeelessons.syllabary.one.UI;
import com.cherokeelessons.syllabary.one.UI.GameBoard;

public class GameScreen extends ChildScreen {
	public GameScreen(Screen caller) {
		super(caller);
	}
	
	@Override
	public void show() {
		super.show();
		GameBoard gameboard = UI.getGameBoard();
		stage.addActor(gameboard);
	}

	@Override
	public void hide() {
		super.hide();
	}
}

/*
 * [challenge] <=> [score]
 * blocks
 * [main] [mute] [pause]
 */