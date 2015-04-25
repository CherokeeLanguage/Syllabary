package com.cherokeelessons.play;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.cherokeelessons.play.GameServices.PlatformInterface;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;

public class Platform implements PlatformInterface {

	public Platform() {
	}

	@Override
	public Credential getCredential(GoogleAuthorizationCodeFlow flow)
			throws IOException {
		LocalServerReceiver jettyServlet = new LocalServerReceiver();
		return new AuthorizationCodeInstalledApp(flow, jettyServlet)
				.authorize(USER);
	}

	@Override
	public HttpTransport getTransport() throws GeneralSecurityException, IOException {
		return GoogleNetHttpTransport.newTrustedTransport();
	}

	@Override
	public void runTask(Runnable runnable) {
		new Thread(runnable).start();		
	}
}
