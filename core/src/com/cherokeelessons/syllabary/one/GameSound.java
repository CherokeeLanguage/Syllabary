package com.cherokeelessons.syllabary.one;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.badlogic.gdx.audio.Sound;

public class GameSound {
	private final AssetManager manager;

	public GameSound(AssetManager manager) {
		this.manager = manager;
	}

	public static final String STARTUP = "music/startup.mp3";
	private static final String DING = "sounds/effects/ding.mp3";
	private static final String WHIP_POP = "sounds/effects/whip_pop.mp3";
	private static final String ALARM = "sounds/effects/alarm.mp3";
	private static final String BARK = "sounds/effects/bark.mp3";
	private static final String BUZZER = "sounds/effects/buzzer2.mp3";
	private static final String CASH = "sounds/effects/cash_out.mp3";
	private static final String ERROR = "sounds/effects/dialogerror.mp3";
	private static final String DINGDING = "sounds/effects/ding-ding-ding.mp3";
	private static final String[] BAD;
	static {
		BAD = new String[] {ALARM, BARK, ERROR, BUZZER };
	}
	
	public void pointsAdded() {
		play(DING);
	}

	public void badSound() {
		String snd = BAD[new Random().nextInt(BAD.length)];
		play(snd);
	}

	private void play(String sound_file) {
		manager.load(sound_file, Sound.class);
		manager.finishLoadingAsset(sound_file);
		Sound sound = manager.get(sound_file, Sound.class);
		sound.play(App.Volume.effectsMute?0f:App.Volume.effects);
	}
	
	public void pointsDeducted() {
		play(WHIP_POP);
	}

	public void dingding() {
		play(DINGDING);		
	}

	private Music audio = null;
	private String glyph_audio = null;
	public void playGlyph(final char answer, final Runnable whenDone) {
		if (audio!=null) {
			audio.stop();
			audio=null;
			if (glyph_audio!=null) {
				manager.unload(glyph_audio);
			}
		}
		glyph_audio = "sounds/glyphs/"+Integer.toHexString(answer)+".mp3";
		manager.load(glyph_audio, Music.class);
		manager.finishLoadingAsset(glyph_audio);
		audio = manager.get(glyph_audio, Music.class);
		audio.setLooping(false);
		audio.setVolume(Math.max(App.Volume.challenges, 0f));
		audio.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(Music music) {
				if (whenDone!=null) {
					Gdx.app.postRunnable(whenDone);
				}
			}
		});
		audio.play();
	}

	public void cashOut() {
		play(CASH);		
	}
}