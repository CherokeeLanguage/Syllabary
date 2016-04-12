package com.cherokeelessons.syllabary.one.android;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.cherokeelessons.play.GameServices;
import com.cherokeelessons.play.Platform;
import com.cherokeelessons.syllabary.one.App;
import com.cherokeelessons.syllabary.one.SyllabaryApp;

import android.os.Bundle;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
//		config.hideStatusBar=true;
//		config.useImmersiveMode=true;
//		config.useAccelerometer=false;
//		config.useCompass=false;
//		config.useWakelock=false;
		config.maxSimultaneousSounds=8;
//		config.resolutionStrategy= new RatioResolutionStrategy(1280f,720f);
		initialize(new SyllabaryApp(), config);
		Platform.application=this;
		App.services=new GameServices(App.CredentialsFolder, new Platform());
	}
}
