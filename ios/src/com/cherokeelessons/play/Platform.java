package com.cherokeelessons.play;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.robovm.apple.dispatch.DispatchQueue;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.foundation.NSURLRequest;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIInterfaceOrientation;
import org.robovm.apple.uikit.UIInterfaceOrientationMask;
import org.robovm.apple.uikit.UIScreen;
import org.robovm.apple.uikit.UIViewController;
import org.robovm.apple.uikit.UIWebView;
import org.robovm.apple.uikit.UIWebViewDelegateAdapter;
import org.robovm.apple.uikit.UIWindow;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.cherokeelessons.play.GameServices.PlatformInterface;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

public class Platform implements PlatformInterface {

	private static class iOSCodeReceiver implements VerificationCodeReceiver {
		private static final String TAG = "iOSCodeReceiver";
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

	private final iOSCodeReceiver codeReceiver;

	public Platform() {
		codeReceiver=new iOSCodeReceiver();
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

	private class WebViewDelegate extends UIWebViewDelegateAdapter {

		@Override
		public void didStartLoad(UIWebView webView) {
			UIApplication.getSharedApplication()
					.setNetworkActivityIndicatorVisible(true);
			Gdx.app.log("iOSGameServices", "didStartLoad: "
					+ webView.getRequest().getURL().getAbsoluteString());
		}

		@Override
		public void didFinishLoad(UIWebView webView) {
			UIApplication.getSharedApplication()
					.setNetworkActivityIndicatorVisible(false);
			String title = webView.evaluateJavaScript("document.title");
			Gdx.app.log("iOSGameServices", "didFinishLoad: " + title);
			if (title.toLowerCase().contains("error")) {
				mostRecentError=title;
				dismissViewControllerFor(webView);
				return;
			}
			if (title.contains("code=")) {
				String code = substringAfter(title, "code=");
				if (code.contains("&")) {
					code = substringBefore(code, "&");
				}
				if (isBlank(code)) {
					mostRecentError="Code Missing";
					dismissViewControllerFor(webView);
					return;
				}
				codeReceiver.code=code;
				dismissViewControllerFor(webView);
			}
		}

		private void dismissViewControllerFor(final UIWebView webView) {
			webView.stopLoading();
			webView.loadRequest(new NSURLRequest(new NSURL("about:blank")));
			UIWindow window = webView.getWindow();
			UIViewController child = window.getRootViewController();
			webView.removeFromSuperview();
			window.removeFromSuperview();
			child.dismissViewController(true, new Runnable() {				
				@Override
				public void run() {
					UIViewController mvc = getMainViewController();
					mvc.becomeFirstResponder();
					Gdx.app.log("iOSGameServices",
							"mvc.getView().getWindow().makeKeyAndVisible()");
					mvc.getView().getWindow().makeKeyAndVisible();
				}
			});
			codeReceiver.stop();
			Gdx.app.log("dismissViewController#completion", "done");
		}

		@Override
		public void didFailLoad(UIWebView webView, NSError error) {
			mostRecentError="Internet Connection Error";
			dismissViewControllerFor(webView);
			UIApplication.getSharedApplication()
					.setNetworkActivityIndicatorVisible(false);
		}
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
				Gdx.app.log("iOSGameServices", "Opening OAUTH Webview");
				final String redirectUri = codeReceiver.getRedirectUri();
				AuthorizationCodeRequestUrl authorizationUrl = flow
						.newAuthorizationUrl().setRedirectUri(redirectUri);
				login(authorizationUrl.build());
				Gdx.app.log("iOSGameServices", "Waiting For Authorization Code");
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

	private void login(final String url) {
		Gdx.app.log("Platform", "NSURL");
		NSURL nsurl = new NSURL(url);
		webview(nsurl);
	}

	private UIViewController getViewController() {

		return new UIViewController() {
			@Override
			public boolean shouldAutorotate() {
				return true;
			}
			
			@Override
			public boolean canBecomeFirstResponder() {
				return true;
			}

			@Override
			public UIInterfaceOrientationMask getSupportedInterfaceOrientations() {
				long mask = 0;
				mask |= ((1 << UIInterfaceOrientation.LandscapeLeft.value()) | (1 << UIInterfaceOrientation.LandscapeRight
						.value()));
				return new UIInterfaceOrientationMask(mask);
			}

			@Override
			public void viewDidLoad() {
				super.viewDidLoad();
				becomeFirstResponder();
			}
		};
	}

	public void webview(final NSURL url) {
		runUITask(new Runnable() {
			@Override
			public void run() {
				UIViewController child = getViewController();
				child.setTitle("Google Play Services");
				UIWindow window = new UIWindow(UIScreen.getMainScreen()
						.getBounds());
				window.setRootViewController(child);

				NSURLRequest request = new NSURLRequest(url);
				final UIWebView wv = new UIWebView(child.getView().getBounds());
				window.addSubview(wv);
				wv.setScalesPageToFit(true);
				wv.setDelegate(new WebViewDelegate());
				wv.loadRequest(request);
				window.makeKeyAndVisible();

				Gdx.app.log(tag,  "getMainViewController");
				UIViewController uiViewController = getMainViewController();
				Gdx.app.log(tag,  "presentViewController");
				uiViewController.presentViewController(child, true, null);
			}

		});
	}

	private UIViewController getMainViewController() {
		IOSApplication app = (IOSApplication) Gdx.app;
		UIViewController uiViewController = app.getUIViewController();
		return uiViewController;
	}

	@Override
	public void runTask(final Runnable runnable) {
		new Thread(runnable).start();
	}

	private void runUITask(final Runnable runnable) {
		DispatchQueue.getMainQueue().async(runnable);
	}

	@Override
	public HttpTransport getTransport() throws GeneralSecurityException {
		return new NetHttpTransport.Builder().build();
	}
	
	private static final String tag = "iOSGameServices";
}
