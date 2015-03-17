package com.cherokeelessons.syllabary.one;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader.FreeTypeFontLoaderParameter;

public enum Font {

	Small(36), Medium(42), Large(58), LLarge(62), XLarge(72);

	public final int size;

	private Font(int size) {
		this.size = size;
	}

	public String fontName() {
		return this.name() + ".ttf";
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
	private static final String SPECIALS;

	static {
		SPECIALS = DSUNDERLINE + DUNDERDOT + DUNDERLINE + OVERLINE + STHRU
				+ UNDERCIRCLE + UNDERCUBE + UNDERDOT + UNDERLINE + UNDERX
				+ BACK_ARROW + DIAMOND + TRIANGLE_ASC + TRIANGLE_DESC
				+ HEAVY_BALLOT_X + HEAVY_CHECK_MARK + LEFT_ARROW + RIGHT_ARROW
				+ DOT + LDQUOTE + RDQUOTE;
	}

	public static void addFonts(AssetManager manager) {
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
		for (Font font : values()) {			
			FreeTypeFontLoaderParameter param = new FreeTypeFontLoaderParameter();
			param.fontFileName = "otf/FreeSerif.otf";
			param.fontParameters.characters = defaultChars;
			param.fontParameters.kerning = true;
			param.fontParameters.size = font.size;
			param.fontParameters.magFilter = TextureFilter.Linear;
			param.fontParameters.minFilter = TextureFilter.Linear;
			manager.load(font.fontName(), BitmapFont.class, param);
		}
		return;
	}
}
