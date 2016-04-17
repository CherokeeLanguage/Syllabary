package com.cherokeelessons.cards;

import com.cherokeelessons.syllabary.one.DisplayModeColors;
import com.cherokeelessons.syllabary.one.DisplayModeOldSyllabary;

public class Settings {
	public String name = "";
	public DisplayModeColors display = DisplayModeColors.Latin;
	public boolean muted = false;
	public boolean skipTraining = false;
	public float vol_challenges=1.0f;
	public float vol_effects=.3f;
	public boolean blackTiles=false;
	public DisplayModeOldSyllabary oldSyllabaryForms=DisplayModeOldSyllabary.ModernOnly;

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
			display = DisplayModeColors.Latin;
		}
	}
}