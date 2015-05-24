package com.cherokeelessons.syllabary.one.android;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender.Method;

import android.app.Application;
import android.util.Log;

@ReportsCrashes(formUri = "http://www.cherokeelessons.com/acra.php", sendReportsAtShutdown = true, sendReportsInDevMode = true, disableSSLCertValidation = true, httpMethod = Method.POST, mode = ReportingInteractionMode.SILENT)
public class AndroidApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		ACRA.init(this);
		Log.d(this.getClass().getName(), "ACRA.init#done");
	}
}
