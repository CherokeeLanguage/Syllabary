package com.cherokeelessons.syllabary.one;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cherokeelessons.cards.SlotInfo;
import com.cherokeelessons.util.GooglePlayGameServices;

public class App {

	private static Color clearColor;

	public static void glClearColor() {
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b,
				clearColor.a);
	}

	private static final String TAG = "App";

	private static SyllabaryApp game;

	private static Preferences prefs;

	public static class Volume {
		public static float effects = 1f;
		public static float challenges = 1f;
	}

	public static Preferences getPrefs() {
		if (prefs == null) {
			prefs = Gdx.app.getPreferences(prefsName());
		}
		return prefs;
	}

	public static String prefsName() {
		return game != null ? getGame().getClass().getName() : App.class
				.getName();
	}

	public static SyllabaryApp getGame() {
		return game;
	}

	public static void setGame(SyllabaryApp game) {
		App.game = game;
	}

	public static FitViewport getFitViewport(Camera camera) {
		Rectangle surrounds = fittedSize();
		FitViewport fitViewport = new FitViewport(surrounds.width,
				surrounds.height, camera);
		fitViewport.update((int) surrounds.width, (int) surrounds.height, true);
		Gdx.app.log(TAG, "Camera Size: " + (int) surrounds.getWidth() + "x"
				+ (int) surrounds.getHeight());
		return fitViewport;
	}

	private final static Rectangle targetScreenSize = new Rectangle(0, 0, 1280,
			720);

	public static final String CredentialsFolder = ".config/CherokeeSyllabary/GooglePlayGameServices/";

	private static Rectangle fittedSize() {
		int h = Gdx.graphics.getHeight();
		int w = Gdx.graphics.getWidth();
		Rectangle surrounds = new Rectangle(0, 0, w, h);
		surrounds.fitOutside(targetScreenSize);
		return surrounds;
	}

	public static Color getClearColor() {
		return clearColor;
	}

	public static void setClearColor(Color clearColor) {
		App.clearColor = new Color(clearColor);
	}

	public static void log(Object source, String message) {
		Gdx.app.log(
				source != null ? source.getClass().getName() : App.class
						.getName(), message != null ? message : "");
	}

	public static void log(String message) {
		log(null, message);
	}

	public static void log(Object source, char character) {
		log(source, Character.toString(character));
	}
	
	public static FileHandle getFolder(int ix) {
		return getFolder(ix + "");
	}
	
	public static FileHandle getFolder(String child) {
		final FileHandle p0;
		String path0 = "CherokeeSyllabary/slots";
		p0 = Gdx.files.external(path0);
		p0.child(child).mkdirs();
		return p0.child(child);
	}
	
	private static Json _json;

	public static GooglePlayGameServices services;
	public static Json json() {
		if (_json!=null) {
			return _json;
		}
		_json = new Json();
		_json.setIgnoreUnknownFields(true);
		_json.setOutputType(OutputType.json);
		_json.setQuoteLongValues(true);
		_json.setTypeName("class");
		_json.setUsePrototypes(false);
		return _json;
	}
	
	public static void toJson(Object object, FileHandle file) {
		file.writeString(json().prettyPrint(object), false, "UTF-8");
	}
	
	public static <T> T fromJson(FileHandle file, Class<T> type) {
		return json().fromJson(type, file);
	}
	
	public static SlotInfo getSlotInfo(int ix) {
		FileHandle folder = getFolder(ix);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		FileHandle json_file = folder.child("info.json");
		if (!json_file.exists()) {
			SlotInfo info = new SlotInfo();
			info.slot=ix;
			toJson(info, json_file);
			return info;
		}
		SlotInfo fromJson = fromJson(json_file, SlotInfo.class);
		fromJson.slot=ix;
		return fromJson;
	}
	
	public static void saveSlotInfo(int ix, SlotInfo info) {
		FileHandle folder = getFolder(ix);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		toJson(info, folder.child("info.json"));
	}
	
	public static FileHandle getSlotInfoFileHandle(int ix) {
		return getFolder(ix).child("info.json");
	}
	
	public static interface PlatformTextInput {
		public void getTextInput(final TextInputListener listener,
				final String title, final String text, final String hint);
	}
}
