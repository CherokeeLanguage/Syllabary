package com.cherokeelessons.syllabary.one;

public enum DisplayModeColors {
	Latin("Show Latin"), None(
			"Hide Latin");
	private DisplayModeColors(String english) {
		this.english = english.intern();
	}

	private String english;

	public String toString() {
		return english;
	};

	public static DisplayModeColors getNext(DisplayModeColors mode) {
		for (int ix = 0; ix < values().length - 1; ix++) {
			if (values()[ix].equals(mode)) {
				return values()[ix + 1];
			}
		}
		return values()[0];
	}
}