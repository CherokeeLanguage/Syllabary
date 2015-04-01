package com.cherokeelessons.cards;

public enum DisplayMode {
	Latin("Show Latin"), None(
			"Hide Latin");
	private DisplayMode(String english) {
		this.english = english.intern();
	}

	private String english;

	public String toString() {
		return english;
	};

	public static DisplayMode getNext(DisplayMode mode) {
		for (int ix = 0; ix < values().length - 1; ix++) {
			if (values()[ix].equals(mode)) {
				return values()[ix + 1];
			}
		}
		return values()[0];
	}
}