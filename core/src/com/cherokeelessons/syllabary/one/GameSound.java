package com.cherokeelessons.syllabary.one;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;

public class GameSound {
	private final AssetManager manager;
	public GameSound(AssetManager manager) {
		this.manager=manager;
	}
	public static final String STARTUP = "music/startup.wav";
	public static final String DING = "sounds/effects/ding.wav";
	private static final String WHIP_POP = "sounds/effects/whip_pop.wav";
	private static Sound ding = null;
	public void ding() {
		if (ding == null) {
			manager.load(DING, Sound.class);
			manager.finishLoadingAsset(DING);
			ding = manager.get(DING, Sound.class);
		}
		ding.play(App.Volume.effects);
	}
	private Sound whip_pop;
	public void whip_pop() {
		App.log("whip_pop");
		if (whip_pop == null) {
			manager.load(WHIP_POP, Sound.class);
			manager.finishLoadingAsset(WHIP_POP);
			whip_pop = manager.get(WHIP_POP, Sound.class);
		}
		whip_pop.play(App.Volume.effects);
	}
}