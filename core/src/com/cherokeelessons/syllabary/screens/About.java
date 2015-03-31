package com.cherokeelessons.syllabary.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.cherokeelessons.syllabary.one.Fonts;

public class About extends ChildScreen {

	private ScrollPane scroll;
	
	private Table table;
	private Table container;
	
	public About(Screen screen) {
		super(screen);		
	}
	
	@Override
	public void show() {
		super.show();
		container = ui.getMenuTable();
		stage.addActor(container);
		
		LabelStyle ls = new LabelStyle(ui.getLs());
		ls.font=Fonts.Medium.get();
		
		container.row();
		TextButtonStyle bls=ui.getTbs();
		bls.font=Fonts.Medium.get();
		TextButton back = new TextButton("BACK", bls);
		container.add(back).left().fill(false, false).expand(false, false);
		back.addListener(exit);
		
		table = new Table();
		
		scroll = new ScrollPane(table,ui.getSps());
		scroll.setColor(Color.DARK_GRAY);
		scroll.setFadeScrollBars(false);
		scroll.setSmoothScrolling(true);
		
		String text = Gdx.files.internal("text/about.txt").readString("UTF-8");		
		text+="\n\n";
		text+="===========\n";
		text+="CHANGELOG\n";
		text+="===========\n";
		text+="\n\n";
		
		text += Gdx.files.internal("text/changelog.txt").readString("UTF-8");

		Label label = new Label(text, ls);
		label.setWrap(true);
		label.setAlignment(Align.center);
		
		table.row();
		table.add(label).expand().fill().left().padLeft(20).padRight(20);
		
		container.row();
		container.add(scroll).expand().fill();
		stage.setKeyboardFocus(scroll);
		stage.setScrollFocus(scroll);
	}
}
