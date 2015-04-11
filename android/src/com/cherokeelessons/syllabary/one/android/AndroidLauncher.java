package com.cherokeelessons.syllabary.one.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.cherokeelessons.play.GameServices;
import com.cherokeelessons.syllabary.one.App;
import com.cherokeelessons.syllabary.one.SyllabaryApp;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new SyllabaryApp(), config);
		App.services=new GameServices(new Platform());
	}
}
