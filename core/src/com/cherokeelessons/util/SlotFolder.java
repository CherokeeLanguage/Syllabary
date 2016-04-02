package com.cherokeelessons.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.cherokeelessons.syllabary.one.App;

public class SlotFolder {
	
	public static final String base = "CherokeeSyllabary";
	
	public static void migrate(){
		Preferences prefs = App.getPrefs();
		String key = "migrate-"+base;
		if (prefs.getBoolean(key, false)){
			return;
		}
		FileHandle lpath = Gdx.files.local(base);
		if (lpath.exists()&&!lpath.isDirectory()) {
			lpath.deleteDirectory();
		}
		if (lpath.child("slots").child("0").isDirectory()) {
			prefs.remove(key);
			prefs.putBoolean(key, true);
			prefs.flush();
			return;
		}
		FileHandle epath = Gdx.files.external(base);
		if (epath.isDirectory()) {
			epath.moveTo(lpath);
		}
		prefs.remove(key);
		prefs.putBoolean(key, true);
		prefs.flush();
	}
	
	public static FileHandle getFolder(String child) {
		final FileHandle p0;
		String path0 = base + "/slots";
		switch (Gdx.app.getType()) {
		case Android:
			p0 = Gdx.files.local(path0);
			break;
		case Applet:
			p0 = Gdx.files.external(path0);
			break;
		case Desktop:
			p0 = Gdx.files.external(path0);
			break;
		case HeadlessDesktop:
			p0 = Gdx.files.external(path0);
			break;
		case WebGL:
			p0 = Gdx.files.external(path0);
			break;
		case iOS:
			p0 = Gdx.files.local(path0);
			break;
		default:
			p0 = Gdx.files.external(path0);
		}
		p0.child(child).mkdirs();
		return p0.child(child);
	}
	
	public static FileHandle getDeckSlot() {
		return getFolder("deck");
	}
}
