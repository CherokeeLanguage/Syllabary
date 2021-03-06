package com.cherokeelessons.syllabary.one;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader.FreeTypeFontLoaderParameter;

public enum Fonts {

	Small(34), Medium(40), Large(56), LLarge(70), XLarge(94);

	public final int size;
	
	private Fonts(int size) {
		this.size = size;
	}

	public String fontName() {
		return this.name() + ".ttf";
	}
	
	public BitmapFont get() {
		return manager.get(fontName(), BitmapFont.class);
	}

	public static final String DIAMOND = "\u25c8";
	public static final String TRIANGLE_ASC = "\u25bc";
	public static final String TRIANGLE_DESC = "\u25b2";
	public static final String BACK_ARROW = "\u21a9";
	public static final String OVERLINE = "\u0305";
	public static final String UNDERDOT = "\u0323";
	public static final String DUNDERDOT = "\u0324";
	public static final String UNDERCIRCLE = "\u0325";
	public static final String UNDERLINE = "\u0332";
	public static final String DUNDERLINE = "\u0333";
	public static final String STHRU = "\u0336";
	public static final String UNDERX = "\u0353";
	public static final String UNDERCUBE = "\u033B";
	public static final String DSUNDERLINE = "\u0347";
	public static final String HEAVY_BALLOT_X = "\u2717";
	public static final String HEAVY_CHECK_MARK = "\u2713";
	public static final String LEFT_ARROW = "\u21e6";
	public static final String RIGHT_ARROW = "\u27a1";
	public static final String DOT = "•";
	public static final String RDQUOTE = "”";
	public static final String LDQUOTE = "“";
	public static final String SPECIALS;

	static {
		SPECIALS = DSUNDERLINE + DUNDERDOT + DUNDERLINE + OVERLINE + STHRU
				+ UNDERCIRCLE + UNDERCUBE + UNDERDOT + UNDERLINE + UNDERX
				+ BACK_ARROW + DIAMOND + TRIANGLE_ASC + TRIANGLE_DESC
				+ HEAVY_BALLOT_X + HEAVY_CHECK_MARK + LEFT_ARROW + RIGHT_ARROW
				+ DOT + LDQUOTE + RDQUOTE;
	}

	private static AssetManager manager;
	public static void dispose(){
		Fonts.manager.clear();
	}
	public static void init() {
		FileHandleResolver resolver = new InternalFileHandleResolver();
		Fonts.manager=new AssetManager();
		manager.setLoader(FreeTypeFontGenerator.class,
				new FreeTypeFontGeneratorLoader(resolver));
		manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(
				resolver));
		manager.setLoader(BitmapFont.class, ".otf", new FreetypeFontLoader(
				resolver));
		
		String defaultChars = FreeTypeFontGenerator.DEFAULT_CHARS;
		for (char c = 'Ꭰ'; c <= 'Ᏼ'; c++) {
			String valueOf = String.valueOf(c);
			if (!defaultChars.contains(valueOf)) {
				defaultChars += valueOf;
			}
		}
		for (char c : "ạẹịọụṿẠẸỊỌỤṾ¹²³⁴ɂ".toCharArray()) {
			String valueOf = String.valueOf(c);
			if (!defaultChars.contains(valueOf)) {
				defaultChars += valueOf;
			}
		}
		for (char c : SPECIALS.toCharArray()) {
			String valueOf = String.valueOf(c);
			if (!defaultChars.contains(valueOf)) {
				defaultChars += valueOf;
			}
		}
		for (Fonts font : values()) {			
			FreeTypeFontLoaderParameter fontp = new FreeTypeFontLoaderParameter();
			fontp.fontFileName = "fonts/CherokeeHandone.ttf";
			fontp.fontParameters.borderGamma=1.0f;
			fontp.fontParameters.borderStraight=false;
			fontp.fontParameters.characters = defaultChars;
			fontp.fontParameters.color=Color.WHITE;
			fontp.fontParameters.gamma=1.1f;
			fontp.fontParameters.kerning = true;
			fontp.fontParameters.magFilter = TextureFilter.Linear;
			fontp.fontParameters.minFilter = TextureFilter.Linear;
			fontp.fontParameters.size = font.size;
			fontp.fontParameters.spaceX=1;
			fontp.fontParameters.spaceY=1;
			manager.load(font.fontName(), BitmapFont.class, fontp);
		}
		return;
	}
	
	public static boolean isLoaded(){
		return manager.update();
	}
}
