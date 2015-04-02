package com.cherokeelessons.syllabary.one;

import java.util.HashMap;
import java.util.Map;

import com.cherokeelessons.cards.Card;
import com.cherokeelessons.cards.Deck;

public class Syllabary {
	
	private static Map<String, String> hex2syllabary = null;
	private static Map<Character, String> tmap;
	private static Deck deck;
	
	public static Deck getDeck(){
		if (deck==null) {
			deck = new Deck();
			for(char syll = 'Ꭰ'; syll<='Ᏼ'; syll++) {
				Card card = new Card();
				card.answer=String.valueOf(syll);
				card.challenge=getLatinFor(syll);
				card.id=syll;
				deck.cards.add(card);
			}
		}
		return new Deck(deck);
	}

	public static String syllabary2hex(char syllabary) {
		return Integer.toHexString(syllabary).toLowerCase();
	}

	public static String hex2syllabary(String hex) {
		hex=hex.toLowerCase().intern();
		if (hex2syllabary == null) {
			hex2syllabary = new HashMap<String, String>();
			for (char ix = 'Ꭰ'; ix <= 'Ᏼ'; ix++) {
				String hexString = Integer.toHexString(ix).toLowerCase();
				hex2syllabary.put(hexString,String.valueOf(ix));
			}
		}
		String s = hex2syllabary.get(hex);
		return s!=null?s:"";
	}

	public static String getLatinFor(char syllabary) {
		if (tmap == null) {
			tmap = translationMaps();
		}
		return tmap.get(syllabary);
	}

	private static final String[] vowels = {"a", "e", "i", "o", "u", "v"};
	private static Map<Character, String> translationMaps() {
		int ix = 0;
		char letter;
		String prefix;
		char chrStart = 'Ꭰ';
		Map<Character, String> syllabary2latin = new HashMap<Character, String>();
		for (ix = 0; ix < vowels.length; ix++) {
			letter = (char) (chrStart + ix);
			syllabary2latin.put(letter, vowels[ix]);
		}
		syllabary2latin.put('Ꭶ', "ga");
		syllabary2latin.put('Ꭷ', "ka");
		prefix = "g";
		chrStart = 'Ꭸ';
		for (ix = 1; ix < vowels.length; ix++) {
			letter = (char) (chrStart + ix - 1);
			syllabary2latin.put(letter, prefix + vowels[ix]);
		}
		prefix = "h";
		chrStart = 'Ꭽ';
		for (ix = 0; ix < vowels.length; ix++) {
			letter = ((char) (chrStart + ix));
			syllabary2latin.put(letter, prefix + vowels[ix]);
		}
		prefix = "l";
		chrStart = 'Ꮃ';
		for (ix = 0; ix < vowels.length; ix++) {
			letter = ((char) (chrStart + ix));
			syllabary2latin.put(letter, prefix + vowels[ix]);
		}
		prefix = "m";
		chrStart = 'Ꮉ';
		for (ix = 0; ix < vowels.length-1; ix++) {
			letter = ((char) (chrStart + ix));
			syllabary2latin.put(letter, prefix + vowels[ix]);
		}
		syllabary2latin.put('Ꮎ', "na");
		syllabary2latin.put('Ꮏ', "hna");
		syllabary2latin.put('Ꮐ', "nah");
		prefix = "n";
		chrStart = 'Ꮑ';
		for (ix = 1; ix < vowels.length; ix++) {
			letter = ((char) (chrStart + ix - 1));
			syllabary2latin.put(letter, prefix + vowels[ix]);
		}
		prefix = "gw";
		chrStart = 'Ꮖ';
		for (ix = 0; ix < vowels.length; ix++) {
			letter = ((char) (chrStart + ix));
			syllabary2latin.put(letter, prefix + vowels[ix]);
		}
		syllabary2latin.put('Ꮜ', "sa");
		syllabary2latin.put('Ꮝ', "s");
		prefix = "s";
		chrStart = 'Ꮞ';
		for (ix = 1; ix < vowels.length; ix++) {
			letter = ((char) (chrStart + ix - 1));
			syllabary2latin.put(letter, prefix + vowels[ix]);
		}
		syllabary2latin.put('Ꮣ', "da");
		syllabary2latin.put('Ꮤ', "ta");
		syllabary2latin.put('Ꮥ', "de");
		syllabary2latin.put('Ꮦ', "te");
		syllabary2latin.put('Ꮧ', "di");
		syllabary2latin.put('Ꮨ', "ti");
		syllabary2latin.put('Ꮩ', "do");
		syllabary2latin.put('Ꮪ', "du");
		syllabary2latin.put('Ꮫ', "dv");
		syllabary2latin.put('Ꮬ', "dla");
		prefix = "hl";
		chrStart = 'Ꮭ';
		for (ix = 0; ix < vowels.length; ix++) {
			letter = ((char) (chrStart + ix));
			syllabary2latin.put(letter, prefix + vowels[ix]);
		}
		prefix = "j";
		chrStart = 'Ꮳ';
		for (ix = 0; ix < vowels.length; ix++) {
			letter = ((char) (chrStart + ix));
			syllabary2latin.put(letter, prefix + vowels[ix]);
		}
		prefix = "w";
		chrStart = 'Ꮹ';
		for (ix = 0; ix < vowels.length; ix++) {
			letter = ((char) (chrStart + ix));
			syllabary2latin.put(letter, prefix + vowels[ix]);
		}
		prefix = "y";
		chrStart = 'Ꮿ';
		for (ix = 0; ix < vowels.length; ix++) {
			letter = ((char) (chrStart + ix));
			syllabary2latin.put(letter, prefix + vowels[ix]);
		}
		App.log("Calculated " + syllabary2latin.size()
				+ " Syllabary to Latin mappings.");
		return syllabary2latin;
	}
}
