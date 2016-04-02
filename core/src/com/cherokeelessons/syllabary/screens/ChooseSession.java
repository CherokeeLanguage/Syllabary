package com.cherokeelessons.syllabary.screens;

import java.util.Random;

import org.apache.commons.lang3.text.WordUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.cherokeelessons.cards.SlotInfo;
import com.cherokeelessons.syllabary.one.App;
import com.cherokeelessons.ui.SlotsDialogHandler;
import com.cherokeelessons.ui.UI.UIDialog;
import com.cherokeelessons.util.GooglePlayGameServices.Callback;
import com.cherokeelessons.util.GooglePlayGameServices.FileMetaList;

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
				App.saveSlotInfo(slot, info);
				Gdx.app.postRunnable(reload);
			}
		};
		UIDialog dialog = ui.getYesNoDialog("Erase this session?", ifYes,
				reload);
		dialog.show(stage);
	}

	private enum YesNo {
		Yes, No;
	}

	@Override
	public void sync(final int slot, final Runnable whenDone) {
		if (!App.services.isLoggedIn()) {
			UIDialog logind = new UIDialog("Sync Service", true, true,
					ui) {
				protected void result(Object object) {
					if (!object.equals(YesNo.Yes)){
						if (whenDone!=null) {
							Gdx.app.log(this.getClass().getSimpleName(), "Do not login chosen. Cancel...");
							Gdx.app.postRunnable(whenDone);
						}
						return;
					}
					if (object.equals(YesNo.Yes)) {
						Callback<Void> cb_login = new Callback<Void>() {
							@Override
							public void success(Void result) {
								if (App.services.isLoggedIn()) {
									ChooseSession.this.sync(slot, whenDone);
									return;
								}
								Gdx.app.postRunnable(whenDone);
							}

							@Override
							public void error(Exception exception) {
								UIDialog error = new UIDialog("ERROR!", true,
										true, ui);
								error.text(WordUtils.wrap(
										exception.getMessage(), 60, "\n", true));
								error.button("OK");
								error.show(stage);
								Gdx.app.postRunnable(whenDone);
							}
						};
						App.services.login(cb_login);
					}
				};
			};
			logind.text("Device sync support requires that\n"
					+ "you to login.\n"
					+ "Would you like to login now?");
			logind.button("YES", YesNo.Yes);
			logind.button("NO", YesNo.No);
			logind.show(stage);
			return;
		}
		SlotInfo info = App.getSlotInfo(slot);
		if (info.signature == null || info.signature.length() == 0) {
			String s1 = Long.toString(System.currentTimeMillis(),
					Character.MAX_RADIX);
			String s2 = Integer.toString(
					new Random().nextInt(Integer.MAX_VALUE),
					Character.MAX_RADIX);
			info.signature = s1 + "-" + s2;
			App.saveSlotInfo(slot, info);
		}
		final Callback<Void> cb_sync_check = new Callback<Void>() {
			@Override
			public void success(Void result) {
				FileHandle tmp = App.getFolder(slot).child("tmp.json");
				FileHandle info = App.getFolder(slot).child("info.json");
				SlotInfo si_tmp = App.json().fromJson(SlotInfo.class, tmp);
				SlotInfo si_info = App.getSlotInfo(slot);
				if (si_info.lastrun == 0 && tmp.exists()) {
					tmp.copyTo(info);
					Gdx.app.postRunnable(whenDone);
					return;
				}
				if (!si_info.signature.equals(si_tmp.signature)) {
					doResolveConflict(slot, si_tmp, si_info, whenDone);
					return;
				}
				if (si_info.activeCards > si_tmp.activeCards) {
					upload(slot, whenDone);
					return;
				}
				if (si_info.activeCards < si_tmp.activeCards) {
					tmp.copyTo(info);
					Gdx.app.postRunnable(whenDone);
					return;
				}
				if (si_info.lastrun > si_tmp.lastrun) {
					upload(slot, whenDone);
					return;
				}
				if (si_info.lastrun < si_tmp.lastrun) {
					tmp.copyTo(info);
					Gdx.app.postRunnable(whenDone);
					return;
				}
				App.log(this, "Already synced.");
				Gdx.app.postRunnable(whenDone);
			}

			@Override
			public void error(Exception exception) {
				UIDialog error = new UIDialog("ERROR!", true, true, ui);
				error.text(WordUtils.wrap(exception.getMessage(), 60, "\n",
						true));
				error.button("OK");
				error.show(stage);
				Gdx.app.postRunnable(whenDone);
			}
		};
		final Callback<FileMetaList> cb_sync_meta = new Callback<FileMetaList>() {
			@Override
			public void success(FileMetaList result) {
				if (result.files.size() == 0) {
					Gdx.app.log("GoogleSyncUI", "No active deck in cloud... ");
					upload(slot, null);
					Gdx.app.postRunnable(whenDone);
					return;
				}
				App.services.drive_getFileById(result.files.get(0).id, App
						.getFolder(slot).child("tmp.json"), cb_sync_check);
			}

			@Override
			public void error(Exception exception) {
				UIDialog error = new UIDialog("ERROR!", true, true, ui);
				error.text(WordUtils.wrap(exception.getMessage(), 60, "\n",
						true));
				error.button("OK");
				error.show(stage);
				Gdx.app.postRunnable(whenDone);
			}
		};
		FileHandle slotInfoFileHandle = App.getSlotInfoFileHandle(slot);
		String name = slotInfoFileHandle.name();
		String title = slot + "-" + name;
		App.services.drive_getFileMetaByTitle(title, cb_sync_meta);
	}

	private void upload(final int slot, final Runnable whenDone) {
		String title = slot + "-" + App.getSlotInfoFileHandle(slot).name();
		Callback<String> cb_upload_done = new Callback<String>() {
			@Override
			public void success(String result) {
				if (whenDone != null) {
					Gdx.app.postRunnable(whenDone);
				}
			}

			@Override
			public void error(Exception exception) {
				UIDialog error = new UIDialog("ERROR!", true, true, ui);
				error.text(WordUtils.wrap(exception.getMessage(), 60, "\n",
						true));
				error.button("OK");
				error.show(stage);
				Gdx.app.postRunnable(whenDone);
			}
		};
		App.services.drive_replace(App.getSlotInfoFileHandle(slot), title,
				title, cb_upload_done);
	}

	private void doResolveConflict(final int slot, final SlotInfo cloud_info,
			final SlotInfo device_info, final Runnable whenDone) {
		final String download = "CLOUD COPY";
		final String upload = "DEVICE COPY";
		final String cancel = "CANCEL";

		final UIDialog notice = new UIDialog("CONFLICT DETECTED", true, true,
				ui) {
			@Override
			protected void result(Object object) {
				if (upload.equals(object)) {
					upload(slot, whenDone);
					return;
				}
				if (download.equals(object)) {
					FileHandle tmp = App.getFolder(slot).child("tmp.json");
					FileHandle info = App.getFolder(slot).child("info.json");
					tmp.copyTo(info);
					if (whenDone != null) {
						Gdx.app.postRunnable(whenDone);
					}
					return;
				}
				if (whenDone != null) {
					Gdx.app.postRunnable(whenDone);
				}
			}
		};
		notice.getTitleLabel().setAlignment(Align.center);

		Table content = notice.getContentTable();
		content.row();
		content.add(new Label("Please choose which copy to keep:", ui.getLs()))
				.colspan(2).center();
		content.row();
		content.add(new Label("CLOUD COPY:", ui.getLs()));

		StringBuilder sb = new StringBuilder();
		sb.append(cloud_info.level);
		sb.append(" ");
		sb.append((cloud_info.settings.name == null || cloud_info.settings.name
				.length() == 0) ? "ᎤᏲᏒ ᏥᏍᏕᏥ!" : cloud_info.settings.name);
		sb.append("\n");
		sb.append(cloud_info.activeCards);
		sb.append(" letters: ");
		sb.append(cloud_info.shortTerm);
		sb.append(" short, ");
		sb.append(cloud_info.mediumTerm);
		sb.append(" medium, ");
		sb.append(cloud_info.longTerm);
		sb.append(" long");

		TextButton textb = new TextButton(sb.toString(), ui.getTbs());
		content.add(textb).expandX().fill();
		textb.addListener(new ClickListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y,
					int pointer, int button) {
				notice.hide();
				FileHandle tmp = App.getFolder(slot).child("tmp.json");
				FileHandle info = App.getFolder(slot).child("info.json");
				tmp.copyTo(info);
				Gdx.app.postRunnable(whenDone);
				return true;
			}
		});

		content.row();
		content.add(new Label("DEVICE COPY:", ui.getLs()));

		sb.setLength(0);
		sb.append(device_info.level);
		sb.append(" ");
		sb.append((device_info.settings.name == null || device_info.settings.name
				.length() == 0) ? "ᎤᏲᏒ ᏥᏍᏕᏥ!" : device_info.settings.name);
		sb.append("\n");
		sb.append(device_info.activeCards);
		sb.append(" letters: ");
		sb.append(device_info.shortTerm);
		sb.append(" short, ");
		sb.append(device_info.mediumTerm);
		sb.append(" medium, ");
		sb.append(device_info.longTerm);
		sb.append(" long");

		textb = new TextButton(sb.toString(), ui.getTbs());
		textb.addListener(new ClickListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y,
					int pointer, int button) {
				notice.hide();
				upload(slot, whenDone);
				return true;
			}
		});
		content.add(textb).expandX().fill();

		notice.button(new TextButton(upload, ui.getTbs()), upload);
		notice.button(new TextButton(download, ui.getTbs()), download);
		notice.button(new TextButton(cancel, ui.getTbs()), cancel);
		notice.show(stage);
	}

	@Override
	public void reload() {
		Gdx.app.postRunnable(reload);
	}
}
