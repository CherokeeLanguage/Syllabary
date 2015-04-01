package com.cherokeelessons.cards;

public class Settings {
	public String name = "";
	public DisplayMode display = DisplayMode.Latin;
	public boolean muted = false;

	public Settings() {
	}

	public Settings(Settings settings) {
		this.display = settings.display;
		this.muted = settings.muted;
		this.name = settings.name;
	}

	public void validate() {
		if (display == null) {
			display = DisplayMode.Latin;
		}
	}
}