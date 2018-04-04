package com.cherokeelessons.syllabary.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.cherokeelessons.cards.SlotInfo;
import com.cherokeelessons.syllabary.one.App;
import com.cherokeelessons.ui.SlotsDialogHandler;
import com.cherokeelessons.ui.UI.UIDialog;
import com.cherokeelessons.util.RandomName;

public class ChooseSession extends ChildScreen implements SlotsDialogHandler {

	public ChooseSession(Screen caller) {
		super(caller);
		container = ui.getMenuTable();
		stage.addActor(container);
	}

	private Runnable reload = new Runnable() {
		@Override
		public void run() {
			hide();
			show();
		}
	};

	private final Table container;

	@Override
	public void show() {
		super.show();
		final UIDialog slotsDialog = ui.getMainSlotDialog(this);
		TextButton back = new TextButton("BACK", ui.getTbs());
		back.addCaptureListener(exit);
		slotsDialog.button(back);
		slotsDialog.show(stage);
		stage.setScrollFocus(slotsDialog);
		stage.setKeyboardFocus(slotsDialog);
	}

	@Override
	public void hide() {
		super.hide();
	}

	@Override
	public void play(final int slot) {
		final Runnable play = new Runnable() {
			@Override
			public void run() {
				GameScreen screen = new GameScreen(caller, slot);
				screen.setStageCount(1);
				App.getGame().setScreen(screen);
				ChooseSession.this.dispose();				
			}
		};
		final SlotInfo info = App.getSlotInfo(slot);
		if (info.activeCards==0) {
			Runnable save = new Runnable() {
				@Override
				public void run() {
					App.saveSlotInfo(slot, info);
					Gdx.app.postRunnable(play);
				}
			};
			UIDialog dialog = ui.getSlotEditDialog(info, save);
			dialog.show(stage);
		} else {
			Gdx.app.postRunnable(play);
		}
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
		Runnable ifYes = new Runnable() {
			@Override
			public void run() {
				SlotInfo info = new SlotInfo();
				info.settings.name=RandomName.getRandomName();
				App.saveSlotInfo(slot, info);
				Gdx.app.postRunnable(reload);
			}
		};
		UIDialog dialog = ui.getYesNoDialog("Erase this session?", ifYes,
				reload);
		dialog.show(stage);
	}

	@Override
	public void reload() {
		Gdx.app.postRunnable(reload);
	}
}
