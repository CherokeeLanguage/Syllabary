package com.cherokeelessons.syllabary.one;

public enum DisplayModeOldSyllabary {
	ModernOnly("Modern Syllabary Only"), OldOnly(
			"Old Syllabary Only"), Both("Both Old and Modern");
	private DisplayModeOldSyllabary(String english) {
		this.english = english.intern();
	}

	private String english;

	public String toString() {
		return english;
	};

	public static DisplayModeOldSyllabary getNext(DisplayModeOldSyllabary mode) {
		for (int ix = 0; ix < values().length - 1; ix++) {
			if (values()[ix].equals(mode)) {
				return values()[ix + 1];
			}
		}
		return values()[0];
	}
}