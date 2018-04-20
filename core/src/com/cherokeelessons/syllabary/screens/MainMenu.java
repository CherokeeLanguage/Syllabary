package com.cherokeelessons.syllabary.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.cherokeelessons.syllabary.one.App;
import com.cherokeelessons.syllabary.one.Fonts;


public class MainMenu extends ChildScreen {

	private Table container;
	private ClickListener showAbout=new ClickListener() {
		public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
			App.getGame().setScreen(new About(MainMenu.this));
			return true;
		};
	};

	public MainMenu() {
		super(null);
	}
	
	private ClickListener gameScreen = new ClickListener(){
		public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
			App.getGame().setScreen(new ChooseSession(MainMenu.this));
			return true;
		};
	};

	@Override
	public void show() {
		super.show();
		
		container = ui.getMenuTable();
		
		stage.addActor(container);
		
		TextButton button;
		
		LabelStyle lstyle = ui.getLs();
		lstyle.font=Fonts.XLarge.get();
		Label title = new Label("Cherokee Language\nSyllabary Practice", lstyle);
		title.setAlignment(Align.center);
		container.row();
		container.add(title).colspan(2).align(Align.center);
		
		TextButtonStyle bstyle=ui.getTbs();
		bstyle.font=Fonts.LLarge.get();
		
		container.row();
		
		button = new TextButton("New Game", bstyle);
		button.addCaptureListener(gameScreen);
		container.add(button);
		container.row();
		
		button = new TextButton("About", bstyle);
		button.addCaptureListener(showAbout);
		container.add(button);//.colspan(2);
		container.row();
		
		button = new TextButton("Quit", bstyle);
		button.addCaptureListener(exit);
		container.add(button);//.colspan(2);
		container.row();
		
		setDoBack(goodbye);
	}
	
	@Override
	public void hide() {
		super.hide();
		container.clear();
		stage.clear();
	}
	
	private Runnable goodbye = new Runnable() {		
		@Override
		public void run() {
			App.getGame().setScreen(new GoodBye(null));
			dispose();			
		}
	};
}
