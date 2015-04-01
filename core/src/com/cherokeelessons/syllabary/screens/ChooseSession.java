package com.cherokeelessons.syllabary.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cherokeelessons.ui.UI.UIDialog;

public class ChooseSession extends ChildScreen {

	public ChooseSession(Screen caller) {
		super(caller);
	}
	
	@Override
	public void show() {
		super.show();
		Table container = ui.getMenuTable();		
		stage.addActor(container);
		UIDialog slotsDialog = ui.getMainSlotDialog();
		slotsDialog.show(stage, null);
	}
	
}
