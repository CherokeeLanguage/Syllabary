package com.cherokeelessons.syllabary.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.cherokeelessons.cards.SlotInfo;
import com.cherokeelessons.syllabary.one.App;
import com.cherokeelessons.ui.SlotsDialogHandler;
import com.cherokeelessons.ui.UI.UIDialog;

public class ChooseSession extends ChildScreen implements SlotsDialogHandler {

	public ChooseSession(Screen caller) {
		super(caller);
	}
	
	private Runnable reload= new Runnable() {
		@Override
		public void run() {
			hide();
			show();
		}
	};
	
	private Table container;
	
	@Override
	public void show() {
		super.show();
		container = ui.getMenuTable();		
		stage.addActor(container);
		final UIDialog slotsDialog = ui.getMainSlotDialog(this);
		RunnableAction focus = Actions.run(new Runnable() {
			@Override
			public void run() {
				stage.setScrollFocus(slotsDialog);
				stage.setKeyboardFocus(slotsDialog);
			}
		});

		TextButton back = new TextButton("BACK", ui.getTbs());
		back.addCaptureListener(exit);
		slotsDialog.button(back);
		slotsDialog.show(stage).addAction(focus);
	}
	
	@Override
	public void hide() {
		super.hide();
		container.clear();
		container.remove();
	}

	@Override
	public void play(final int slot) {
		GameScreen screen = new GameScreen(caller, slot);
		screen.setStageCount(1);
		App.getGame().setScreen(screen);
		this.dispose();
	}

	@Override
	public void edit(final int slot) {
		final SlotInfo info = App.getSlotInfo(slot);
		Runnable save = new Runnable() {
			@Override
			public void run() {
				App.saveSlotInfo(slot, info);
				Gdx.app.postRunnable(reload);
			}
		};
		UIDialog dialog = ui.getSlotEditDialog(info, save);
		dialog.show(stage);
	}

	@Override
	public void erase(final int slot) {
		Runnable ifYes = new Runnable(){
			@Override
			public void run() {
				SlotInfo info = new SlotInfo();
				App.saveSlotInfo(slot, info);
				Gdx.app.postRunnable(reload);
			}
		};
		UIDialog dialog = ui.getYesNoDialog("Erase this session?", ifYes, reload);
		dialog.show(stage);
	}

	@Override
	public void sync(int slot) {
		
	}
}
