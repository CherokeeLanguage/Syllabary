package com.cherokeelessons.syllabary.one;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.badlogic.gdx.audio.Sound;
import com.cherokeelessons.syllabary.one.App.Volume;

public class GameSound {
	private final AssetManager manager;

	public GameSound(AssetManager manager) {
		this.manager = manager;
	}

	public static final String STARTUP = "music/startup.wav";
	private static final String DING = "sounds/effects/ding.wav";
	private static final String WHIP_POP = "sounds/effects/whip_pop.wav";
	private static final String ALARM = "sounds/effects/alarm.wav";
	private static final String BARK = "sounds/effects/bark.wav";
	private static final String BUZZER = "sounds/effects/buzzer2.wav";
	private static final String CASH = "sounds/effects/cash_out.wav";
	private static final String ERROR = "sounds/effects/dialogerror.wav";
	private static final String DINGDING = "sounds/effects/ding-ding-ding.wav";
	private static final String CLICK = "sounds/effects/menu-click.wav";
	private static final String[] BAD;
	static {
		BAD = new String[] {ALARM, BARK, ERROR };
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
		sound.play(App.Volume.effects);
	}
	
	public void pointsDeducted() {
		play(WHIP_POP);
	}

	public void dingding() {
		play(DINGDING);		
	}

	private String glyph_audio = null;
	public void playGlyph(final char answer, final Runnable whenDone) {
		if (glyph_audio!=null) {
			manager.unload(glyph_audio);
			glyph_audio=null;
		}
		glyph_audio = "sounds/glyphs/"+Integer.toHexString(answer)+".wav";
		manager.load(glyph_audio, Music.class);
		manager.finishLoadingAsset(glyph_audio);
		Music audio = manager.get(glyph_audio, Music.class);
		audio.setLooping(false);
		audio.setVolume(Volume.challenges);
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