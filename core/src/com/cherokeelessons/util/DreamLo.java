package com.cherokeelessons.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.Preferences;
import com.cherokeelessons.util.GooglePlayGameServices.Callback;
import com.cherokeelessons.util.GooglePlayGameServices.GameScores;
import com.cherokeelessons.util.GooglePlayGameServices.GameScores.GameScore;

public class DreamLo {
	private static final String DREAMLO_USERID = "dreamlo-userid";
	private static final String writeUrl = "http://dreamlo.com/lb/" + "4uW2ngUc8Emwk5g_xFfaAgUcKtt6mnjkWQdHKWotck-g";
	private static final String readUrl = "http://dreamlo.com/lb/" + "56ff2c056e51bc0d04a13bbb";
	/**
	 * boardId = "animal-slot#-timstamp-random";
	 */
	private final Preferences prefs;
	
	@SuppressWarnings("deprecation")
	private static String encode(String string) {
		try {
			return URLEncoder.encode(string, "UTF-8").replaceAll("\\+", "%20");
		} catch (UnsupportedEncodingException e) {
			return URLEncoder.encode(string).replaceAll("\\+", "%20");
		}
	}

	public DreamLo(Preferences prefs) {
		this.prefs = prefs;
	}

	public boolean registerWithDreamLoBoard() {
		if (prefs.getString(DREAMLO_USERID, "").length() == 0) {
			if (!registeredListenerPending) {
				Gdx.app.log("DreamLo", "registeredWithBoard: false");
				registeredListenerPending = true;
				Gdx.app.postRunnable(registerWithBoard);
			}
			return false;
		}
		return true;
	}

	private static boolean registeredListenerPending = false;
	private HttpResponseListener registeredListener = new HttpResponseListener() {
		@Override
		public void handleHttpResponse(HttpResponse httpResponse) {
			Gdx.app.log("Register result:", httpResponse.getResultAsString());
			registeredListenerPending = false;
		}

		@Override
		public void failed(Throwable t) {
			Gdx.app.log("Register result:", t.getMessage());
			registeredListenerPending = false;
		}

		@Override
		public void cancelled() {
			Gdx.app.log("Register result:", "ABORTED");
			registeredListenerPending = false;
		}
	};

	private Runnable registerWithBoard = new Runnable() {
		@Override
		public void run() {
			Gdx.app.log("DreamLo", "registerWithBoard");
			HttpRequest httpRequest = new HttpRequest("GET");
			httpRequest.setUrl(readUrl + "/pipe");
			Gdx.app.log("DreamLo", "URL: '" + httpRequest.getUrl() + "'");
			HttpResponseListener httpResponseListener = new HttpResponseListener() {
				@Override
				public void handleHttpResponse(HttpResponse httpResponse) {
					Gdx.app.log("DreamLo", "registerWithBoard-init");
					String str_scores = httpResponse.getResultAsString();
					String[] scores = str_scores.split("\n");
					Random r = new Random();
					int id = 0;
					tryagain: while (true) {
						id = r.nextInt(Integer.MAX_VALUE) + 1;
						for (String score : scores) {
							if (score.contains(id + "-")) {
								continue tryagain;
							}
						}
						break tryagain;
					}
					HttpRequest httpRequest = new HttpRequest("GET");
					httpRequest.setTimeOut(10000);
					httpRequest.setUrl(writeUrl + "/add/" + id + "-0/0/0/"+encode("ᎢᏤ ᏴᏫ!!!ᎩᎶ ᎢᏤ"));
					Gdx.app.log("Register url:", httpRequest.getUrl());
					Gdx.net.sendHttpRequest(httpRequest, registeredListener);
					prefs.putString(DREAMLO_USERID, id + "");
					prefs.flush();
				}

				@Override
				public void failed(Throwable t) {
					Gdx.app.log("DreamLo", "registerWithBoard: ", t);
				}

				@Override
				public void cancelled() {
					Gdx.app.log("DreamLo", "registerWithBoard: TIMED OUT");
				}
			};
			Gdx.net.sendHttpRequest(httpRequest, httpResponseListener);
		}
	};

	public void lb_getScores(final Callback<GameScores> callback) {
		if (!registerWithDreamLoBoard()) {
			Gdx.app.postRunnable(new Runnable() {
				public void run() {
					DreamLo.this.lb_getScores(callback);
				}
			});
			return;
		}
		HttpRequest httpRequest = new HttpRequest("GET");
		httpRequest.setTimeOut(10000);
		httpRequest.setUrl(readUrl + "/pipe");
		Gdx.net.sendHttpRequest(httpRequest, new HttpResponseListener() {
			@Override
			public void handleHttpResponse(HttpResponse httpResponse) {
				String myId = prefs.getString(DREAMLO_USERID, "") + "-";
				List<String> records = new ArrayList<>(Arrays.asList(httpResponse.getResultAsString().split("\n")));
				final GameScores gss = new GameScores();
				gss.list = new ArrayList<>();
				for (String score_record : records) {
					if (score_record == null || score_record.length() == 0) {
						continue;
					}
					String[] s = score_record.split("\\|");
					if (s == null || s.length < 4) {
						continue;
					}
					/*
					 * 0: username 1: score 2: time 3: tag 4: date 5: index
					 */
					GameScore gs = new GameScore();
					gs.score = StringUtils.defaultString(s[2]).trim();
					gs.score=StringUtils.reverse(gs.score).replaceAll("(\\d{3})", "$1,");
					gs.score=StringUtils.reverse(gs.score);
					gs.score=StringUtils.strip(gs.score, ",");
					String label = StringUtils.defaultString(s[3]).trim();
					gs.tag = StringUtils.substringBefore(label, "!!!");
					String decoded_other_name = StringUtils.substringAfter(label, "!!!");
					String dreamLoId=StringUtils.defaultString(s[0]).trim();
					gs.user = dreamLoId;
					if (gs.user.startsWith(myId)) {
						gs.user = decoded_other_name;
					} else {
						if (!decoded_other_name.matches(".*?[a-zA-Z].*?")) {
							gs.user = StringUtils.left(decoded_other_name,14)+" #"+dreamLoId;
						}
					}
					gs.user = StringUtils.left(gs.user, 17);
					gs.activeCards = StringUtils.defaultString(s[1]).trim();
					gs.activeCards=StringUtils.reverse(gs.activeCards).replaceAll("(\\d{3})", "$1,");
					gs.activeCards=StringUtils.reverse(gs.activeCards);
					gs.activeCards=StringUtils.strip(gs.activeCards, ",");
					gss.list.add(gs);
				}
				Comparator<GameScore> descending = new Comparator<GooglePlayGameServices.GameScores.GameScore>() {
					@Override
					public int compare(GameScore o1, GameScore o2) {
						if (o1 == o2) {
							return 0;
						}
						if (o2 == null) {
							return -1;
						}
						if (o1 == null) {
							return 1;
						}
						if (StringUtils.isBlank(o1.tag)!=StringUtils.isBlank(o2.tag)) {
							return StringUtils.isBlank(o1.tag)?1:-1;
						}
						long v1;
						long v2;
						try {
							v1 = Long.valueOf(o1.activeCards.replace(",", ""));
						} catch (NumberFormatException e) {
							v1 = 0;
						}
						try {
							v2 = Long.valueOf(o2.activeCards.replace(",", ""));
						} catch (NumberFormatException e) {
							v2 = 0;
						}
						if (v1!=v2) {
							return v1 < v2 ? 1 : -1;
						}
						try {
							v1 = Long.valueOf(o1.score.replace(",", ""));
						} catch (NumberFormatException e) {
							v1 = 0;
						}
						try {
							v2 = Long.valueOf(o2.score.replace(",", ""));
						} catch (NumberFormatException e) {
							v2 = 0;
						}
						return v1 < v2 ? 1 : v1 > v2 ? -1 : 0;
					}
				};
				Collections.sort(gss.list, descending);
				for (int ix = 0; ix < gss.list.size(); ix++) {
					GameScore tmp = gss.list.get(ix);
					switch (ix + 1) {
					case 1:
						tmp.rank = "1st";
						break;
					case 2:
						tmp.rank = "2nd";
						break;
					case 3:
						tmp.rank = "3rd";
						break;
					default:
						tmp.rank = (ix + 1) + "th";
						break;
					}
				}
				Gdx.app.postRunnable(callback.with(gss));
			}

			@Override
			public void failed(Throwable t) {
				Gdx.app.log("DreamLo", "lb_getScoresFor:", t);
				Gdx.app.postRunnable(callback.with(new RuntimeException(t)));
			}

			@Override
			public void cancelled() {
				Gdx.app.log("DreamLo", "lb_getScoresFor: timed out");
				Gdx.app.postRunnable(callback.with(new RuntimeException("TIMED OUT")));
			}
		});
	}

	public void lb_submit(final String boardId, final long cards, final long score, final String label, final Callback<Void> callback) {
		if (!registerWithDreamLoBoard()) {
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run() {
					DreamLo.this.lb_submit(boardId, cards, score, label, callback);
				}
			});
			return;
		}
		HttpRequest httpRequest = new HttpRequest("GET");
		httpRequest.setTimeOut(10000);
		String url = writeUrl + "/add/" + prefs.getString(DREAMLO_USERID, "") + "-" + boardId + "/" + cards + "/"+score+"/"
				+ encode(label);
		httpRequest.setUrl(url);
		Gdx.net.sendHttpRequest(httpRequest, new HttpResponseListener() {
			@Override
			public void handleHttpResponse(HttpResponse httpResponse) {
				Gdx.app.postRunnable(callback.withNull());
			}

			@Override
			public void failed(Throwable t) {
				Gdx.app.log("DreamLo", "lb_submit", t);
				Gdx.app.postRunnable(callback.with(new RuntimeException(t)));
			}

			@Override
			public void cancelled() {
				Gdx.app.log("DreamLo", "lb_submit: timed out");
				Gdx.app.postRunnable(callback.with(new RuntimeException("TIMED OUT")));
			}
		});
	}
}