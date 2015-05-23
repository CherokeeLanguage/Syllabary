package com.cherokeelessons.syllabary.one.android;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formUri = "http://www.cherokeelessons.com/acra.php")
public class AndroidApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		ACRA.init(this);
	}
}
