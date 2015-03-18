package com.cherokeelessons.syllabary.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cherokeelessons.syllabary.one.App;

public class ChildScreen implements Screen, InputProcessor {
	
	public void log(){
		Gdx.app.log(this.getClass().getSimpleName(), " ");
	}
	
	public void log(String message) {
		Gdx.app.log(this.getClass().getSimpleName(), message);
	}
	
	protected final Screen caller;
	protected final Stage stage;
	protected final InputMultiplexer multi;
	
	protected final ClickListener exit = new ClickListener() {
		public boolean touchDown(InputEvent event, float x, float y,
				int pointer, int button) {
			doBack.run();;
			return true;
		};
	};
	
	protected Runnable doBack = new Runnable(){
		public void run() {
		};
	};
	
	protected Runnable doMenu = new Runnable(){
		public void run() {
		};
	};
	
	public ChildScreen(Screen caller) {		
		this.caller=caller;
		this.multi=new InputMultiplexer();
		stage = new Stage();
		stage.setViewport(App.getFitViewport(stage.getCamera()));
	}
	
	@Override
	public void show() {		
		multi.addProcessor(this);
		multi.addProcessor(stage);
		Gdx.input.setInputProcessor(multi);
	}
	
	public void act(float delta){
		stage.act(delta);
	}

	@Override
	public void render(float delta) {
		act(delta);
		draw(delta);
	}
	
	public void draw(float delta){
		App.glClearColor();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		stage.setViewport(App.getFitViewport(stage.getCamera()));
		stage.getViewport().update(width, height);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public void dispose() {
		stage.dispose();
		App.log(this, "dispose()");
	}

	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
		case Keys.BACK:
		case Keys.ESCAPE:
			App.log(this, "<<BACK>>");
			if (doBack!=null) {
				Gdx.app.postRunnable(doBack);
				return true;
			}
		case Keys.MENU:
		case Keys.F1:
			App.log(this, "<<MENU>>");
			if (doMenu!=null) {
				Gdx.app.postRunnable(doMenu);
				return true;
			}
		default:
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

}
