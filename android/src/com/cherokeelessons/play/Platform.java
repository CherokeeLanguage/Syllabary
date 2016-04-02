package com.cherokeelessons.play;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.cherokeelessons.play.GameServices.PlatformInterface;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.HttpTransport;

@SuppressLint("DefaultLocale")
public class Platform implements PlatformInterface {

	private static final String TAG = "AndroidGameServices";
	public static AndroidApplication application;

	private static class AndroidCodeReceiver implements VerificationCodeReceiver {
		private static final String TAG = "AndroidCodeReceiver";
		private String code = null;

		@Override
		public String getRedirectUri() throws IOException {
			return "urn:ietf:wg:oauth:2.0:oob:auto";
		}

		private long timeout;

		@Override
		public String waitForCode() throws IOException {
			Gdx.app.log(TAG, "waitForCode");
			timeout = 1000l * 60l * 10l;
			while (code == null && timeout > 0l) {
				try {
					Thread.sleep(250l);
					timeout -= 250l;
					continue;
				} catch (InterruptedException e) {
					Gdx.app.log(TAG, "InterruptedException");
					return code;
				}
			}
			Gdx.app.log(TAG, "waitForCode:" + code);
			return code;
		}

		@Override
		public void stop() {
			Gdx.app.log(TAG, "stop");
			timeout = 0l;
		}

	}

	final AndroidCodeReceiver codeReceiver;

	public Platform() {
		codeReceiver = new AndroidCodeReceiver();
	}
	
	public static boolean isEmpty(CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	public static String substringAfter(String str, String separator) {
		if (isEmpty(str)) {
			return str;
		}
		if (separator == null) {
			return "";
		}
		int pos = str.indexOf(separator);
		if (pos == -1) {
			return "";
		}
		return str.substring(pos + separator.length());
	}

	public static boolean isBlank(CharSequence cs) {
		int strLen;
		if (cs == null || (strLen = cs.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if (Character.isWhitespace(cs.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}

	public static String substringBefore(String str, String separator) {
		if (isEmpty(str) || separator == null) {
			return str;
		}
		if (separator.length() == 0) {
			return "";
		}
		int pos = str.indexOf(separator);
		if (pos == -1) {
			return str;
		}
		return str.substring(0, pos);
	}

	private String mostRecentError="Unknown Error - Try Again";
	
	@Override
	public Credential getCredential(GoogleAuthorizationCodeFlow flow)
			throws IOException {
		return new AuthorizationCodeInstalledApp(flow, codeReceiver) {

			@Override
			public Credential authorize(final String userId) throws IOException {
				final AuthorizationCodeFlow flow = this.getFlow();
				Credential credential = flow.loadCredential(userId);
				if (credential != null
						&& (credential.getRefreshToken() != null || credential
								.getExpiresInSeconds() > 60)) {
					return credential;
				}
				// open in webview
				codeReceiver.code=null;
				mostRecentError="Unknown Error - Try Again";
				Gdx.app.log(TAG, "Opening OAUTH Webview");
				final String redirectUri = codeReceiver.getRedirectUri();
				AuthorizationCodeRequestUrl authorizationUrl = flow
						.newAuthorizationUrl().setRedirectUri(redirectUri);
				login(authorizationUrl.build());
				Gdx.app.log(TAG, "Waiting For Authorization Code");
				String waitForCode = codeReceiver.waitForCode();
				if (waitForCode == null) {
					throw new IOException(mostRecentError);
				}
				TokenResponse response = flow.newTokenRequest(waitForCode)
						.setRedirectUri(redirectUri).execute();
				return flow.createAndStoreCredential(response, userId);
			}
		}.authorize(PlatformInterface.USER);
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void login(final String url) {
		Gdx.app.log(TAG, "webViewLogin");
		application.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final Builder alert = new AlertDialog.Builder(application);
				alert.setTitle("Google Play Services");
				alert.setNegativeButton("CLOSE",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								mostRecentError="User Canceled Authentication Dialog";
							}
						});
				final WebView webView = new WebView(application) {
					@Override
					public boolean onCheckIsTextEditor() {
						return true;
					}
				};
				webView.setMinimumHeight(600);
				webView.setMinimumWidth(1024);
				webView.setInitialScale(150);
				alert.setView(webView);
				alert.setCancelable(true);
				final AlertDialog adialog = alert.show();
				final OnDismissListener listener = new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						webView.loadUrl("about:blank");
						codeReceiver.stop();
					}
				};
				adialog.setOnDismissListener(listener);
				webView.loadUrl("about:blank");
				
				application.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						WebSettings settings = webView.getSettings();
						settings.setBuiltInZoomControls(false);
						settings.setDefaultTextEncodingName("UTF-8");
						settings.setJavaScriptEnabled(true);
						settings.setJavaScriptCanOpenWindowsAutomatically(true);
						settings.setLoadsImagesAutomatically(true);
						settings.setSaveFormData(true);
						settings.setUseWideViewPort(false);

						settings.setFixedFontFamily("FreeMono");
						settings.setSansSerifFontFamily("FreeSans");
						settings.setSerifFontFamily("FreeSerif");
						settings.setStandardFontFamily("FreeSerif");
						settings.setLoadWithOverviewMode(true);

						webView.requestFocus(View.FOCUS_DOWN);
						webView.setOnTouchListener(new View.OnTouchListener() {
							@SuppressLint("ClickableViewAccessibility")
							@Override
							public boolean onTouch(View v, MotionEvent event) {
								switch (event.getAction()) {
								case MotionEvent.ACTION_DOWN:
								case MotionEvent.ACTION_UP:
									if (!v.hasFocus()) {
										v.requestFocus();
									}
									break;
								}
								return false;
							}
						});

						webView.setWebViewClient(new WebViewClient() {
							@Override
							public void onPageFinished(WebView view, String url) {
								String title = StringUtils.defaultString(view
										.getTitle());
								if (title.toLowerCase().contains("denied")) {
									adialog.dismiss();
									codeReceiver.stop();
									mostRecentError=title;
									return;
								}
								if (title.toLowerCase().contains("code=")) { // OAuth2ClientCredentials.OAUTH_CALLBACK_URL))
									webView.loadUrl("about:blank");
									String code = StringUtils.substringAfter(
											title, "code=");
									if (code.contains("&")) {
										code = StringUtils.substringBefore(
												code, "&");
									}
									if (StringUtils.isBlank(code)) {
										mostRecentError="Did not receive a code.";										
										adialog.dismiss();
										codeReceiver.stop();
										return;
									}
									codeReceiver.code=code;
									adialog.dismiss();
								}
							}

							@Override
							public void onReceivedError(WebView view,
									int errorCode, String description,
									String failingUrl) {
								super.onReceivedError(view, errorCode,
										description, failingUrl);
								mostRecentError=description;
							}

							@Override
							public void onReceivedSslError(WebView view,
									SslErrorHandler handler, SslError error) {
								super.onReceivedSslError(view, handler, error);
								mostRecentError=error.toString();
							}
						});
						webView.loadUrl(url);
					}
				});
			}
		});
	}

	@Override
	public void runTask(final Runnable runnable) {
		application.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						runnable.run();
						return null;
					}
				}.execute();
			}
		});
	}

	@Override
	public HttpTransport getTransport() {
		return AndroidHttp.newCompatibleTransport();
	}
}
