package com.cherokeelessons.cards;

public class Settings {
	public String name = "";
	public DisplayMode display = DisplayMode.Latin;
	public boolean muted = false;
	public boolean skipTraining = false;
	public float vol_challenges=1.0f;
	public float vol_effects=.3f;

	public Settings() {
	}

	public Settings(Settings settings) {
		this.display = settings.display;
		this.muted = settings.muted;
		this.name = settings.name;
		this.skipTraining=settings.skipTraining;
	}

	public void validate() {
		if (display == null) {
			display = DisplayMode.Latin;
		}
	}
}