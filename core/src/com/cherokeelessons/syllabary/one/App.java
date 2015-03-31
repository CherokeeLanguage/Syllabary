package com.cherokeelessons.syllabary.one;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cherokeelessons.util.GooglePlayGameServices;

public class App {

	public static GooglePlayGameServices gpgs;

	private static Color clearColor;

	public static void glClearColor() {
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b,
				clearColor.a);
	}

	private static final String TAG = "App";

	private static Syllabary game;

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

	public static Syllabary getGame() {
		return game;
	}

	public static void setGame(Syllabary game) {
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
}
