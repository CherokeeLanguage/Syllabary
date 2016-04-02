package com.cherokeelessons.play;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.cherokeelessons.util.GamesScopes;
import com.cherokeelessons.util.GooglePlayGameServices;
import com.cherokeelessons.util.GooglePlayGameServices.FileMetaList.FileMeta;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.Drive.Files.Create;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class GameServices implements GooglePlayGameServices {
	
	private static final String APP_DATA_FOLDER = "appDataFolder";

	private static void appendSourceClassName(StringBuilder sb, LogRecord record) {
		String sourceClassName = record.getSourceClassName();
		if (sourceClassName != null) {
			sb.append(" ");
			sb.append(sourceClassName);
		}
	}
	
	private static final Formatter f1 = new Formatter() {
		@Override
		public String format(LogRecord record) {
			StringBuilder sb = new StringBuilder(256);
			sb.append("[GameServices] ");
			sb.append(new java.util.Date(record.getMillis()));
			appendSourceClassName(sb, record);
			sb.append(", ");
			sb.append(record.getMessage());
			sb.append("\n");
			return sb.toString();
		}

	};
	
	private final static Logger log;
	static {
		ConsoleHandler handler = new ConsoleHandler();
		log = Logger.getLogger(GameServices.class.getName());
		handler.setFormatter(f1);
		log.setUseParentHandlers(false);
		log.addHandler(handler);
	}
	

	public static String APP_NAME = "CherokeeBoundPronouns/332095";

	public static interface PlatformInterface {
		public static final String USER = "cherokee-bound-pronouns-332095-restapi";

		public Credential getCredential(GoogleAuthorizationCodeFlow flow)
				throws IOException;

		public HttpTransport getTransport() throws GeneralSecurityException,
				IOException;

		public void runTask(Runnable runnable);
	}

	private FileHandle DATA_STORE_DIR;
	private Credential credential;
	private FileDataStoreFactory dataStoreFactory;
	private HttpTransport httpTransport;
	private FileHandle p0;
	private static final JacksonFactory JSON_FACTORY = JacksonFactory
			.getDefaultInstance();
	protected static final String TAG = "GameServices";

	private Boolean initdone = false;

	private static void postRunnable(Runnable runnable) {
		if (runnable == null) {
			Gdx.app.log(TAG, "NULL CALLBACK!");
			return;
		}
		Gdx.app.postRunnable(runnable);
	}

	private final PlatformInterface platform;
	private final String googlePlayServicesFolder;

	public GameServices(String credentialsFolder, PlatformInterface platform) {
		this.platform = platform;
		this.googlePlayServicesFolder = credentialsFolder;
	}

	private void init() throws GeneralSecurityException, IOException {
		synchronized (initdone) {
			if (initdone) {
				return;
			}
			if (Gdx.app.getType().equals(ApplicationType.Desktop)) {
				p0 = Gdx.files.external(googlePlayServicesFolder);
			} else {
				p0 = Gdx.files.local(googlePlayServicesFolder);
			}
			p0.mkdirs();
			DATA_STORE_DIR = p0;
			httpTransport = platform.getTransport();
			dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR.file());
			initdone = true;
		}
	}

	@Override
	public boolean isLoggedIn() {
		try {
			init();
			GoogleAuthorizationCodeFlow flow = getFlow();
			if (flow == null) {
				return false;
			}
			final Credential loadCredential = flow
					.loadCredential(PlatformInterface.USER);
			if (loadCredential == null) {
				return false;
			}
			try {
				return loadCredential.refreshToken();
			} catch (Exception e) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public GoogleAuthorizationCodeFlow getFlow() throws IOException {
		GoogleClientSecrets clientSecrets = null;
		String json = Gdx.files.internal("google.json").readString();
		
		StringReader sr = new StringReader(json);
		clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, sr);
		Set<String> scopes = new HashSet<>(GamesScopes.all());
		
		//games
		scopes.add("https://www.googleapis.com/auth/games");
		scopes.add("https://www.googleapis.com/auth/drive.appdata");
		scopes.add("https://www.googleapis.com/auth/plus.login");
		
		//profile
		scopes.add("https://www.googleapis.com/auth/plus.me");
		scopes.add("https://www.googleapis.com/auth/userinfo.profile");
		
		//v3 drive appdata
		scopes.add("https://www.googleapis.com/auth/drive.appfolder");
		
		GoogleAuthorizationCodeFlow.Builder builder = new GoogleAuthorizationCodeFlow.Builder(
				httpTransport, JSON_FACTORY, clientSecrets, scopes);
		builder.setApprovalPrompt("auto");
		builder.setAccessType("offline");
		builder.setDataStoreFactory(dataStoreFactory);
		GoogleAuthorizationCodeFlow flow = builder.build();
		return flow;
	}

	@Override
	public void login(final Callback<Void> callback) {
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					init();
					credential = authorize();
					postRunnable(callback.withNull());
				} catch (Exception e) {
					e.printStackTrace();
					postRunnable(callback.with(e));
				}
			}
		};
		platform.runTask(runnable);
	}

	private void _login() throws GeneralSecurityException, IOException {
		init();
		credential = authorize();
	}

	private Credential authorize() throws IOException {
		try {
			return platform.getCredential(getFlow());
		} catch (Exception e) {
			throw new IOException("Authorization Failure", e);
		}
	}

	@Override
	public void logout(final Callback<Void> success) {
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					init();
					GoogleAuthorizationCodeFlow flow = getFlow();
					flow.getCredentialDataStore().clear();
					credential = null;
					postRunnable(success.withNull());
				} catch (Exception e) {
					postRunnable(success.with(e));
				}
			}
		};
		platform.runTask(runnable);
	}

	private void retry(final Runnable r) {
		final Callback<Void> retry = new Callback<Void>() {
			@Override
			public void success(Void result) {
				platform.runTask(r);
			}
		};
		final Callback<Void> back_in = new Callback<Void>() {
			@Override
			public void success(Void result) {
				login(retry);
			}
		};
		logout(back_in);
	}

//	@Override
//	public void lb_submit(final String boardId, final long score,
//			final String label, final Callback<Void> callback) {
//		final Runnable runnable = new Runnable() {
//			private final Runnable _self = this;
//
//			@Override
//			public void run() {
//				try {
//					Games g = _getGamesObject();
//					Submit submit = g.scores().submit(boardId, score);
//					String tag = URLEncoder.encode(label, "UTF-8");
//					submit.setScoreTag(tag);
//					submit.setScore(score);
//					submit.execute();
//					postRunnable(callback.withNull());
//				} catch (Exception e) {
//					e.printStackTrace();
//					retry(_self);
//				}
//			}
//		};
//		platform.runTask(runnable);
//	}
//
//	@Override
//	public void lb_getScoresFor(final String boardId,
//			final Callback<GameScores> callback) {
//		final Runnable runnable = new Runnable() {
//			private final Runnable _self = this;
//
//			@Override
//			public void run() {
//				GameScores gscores = new GameScores();
//				try {
//					Games g = _getGamesObject();
//					Scores.Get scores = g.scores().get("me", boardId,
//							TimeSpan.ALL_TIME.name());
//					scores.setMaxResults(30);
//					PlayerLeaderboardScoreListResponse result = scores
//							.execute();
//					List<PlayerLeaderboardScore> list = result.getItems();
//					if (list != null)
//						for (PlayerLeaderboardScore e : list) {
//							GameScore gs = new GameScore();
//							gs.rank = "";
//							gs.tag = URLDecoder.decode(
//									StringUtils.defaultString(e.getScoreTag()),
//									"UTF-8");
//							gs.value = e.getScoreString();
//							gs.user = "";
//							gscores.list.add(gs);
//						}
//					postRunnable(callback.with(gscores));
//				} catch (Exception e) {
//					e.printStackTrace();
//					retry(_self);
//				}
//			}
//		};
//		platform.runTask(runnable);
//	}
//
//	@Override
//	public void lb_getListFor(final String boardId,
//			final Collection collection, final TimeSpan ts,
//			final Callback<GameScores> callback) {
//		final Runnable runnable = new Runnable() {
//			private final Runnable _self = this;
//
//			@Override
//			public void run() {
//				GameScores gscores = new GameScores();
//				try {
//					Games g = _getGamesObject();
//					Scores.List scores = g.scores().list(boardId,
//							collection.name(), ts.toString());
//					scores.setMaxResults(30);
//					LeaderboardScores result = scores.execute();
//					List<LeaderboardEntry> list = result.getItems();
//					if (list != null)
//						for (LeaderboardEntry e : list) {
//							GameScore gs = new GameScore();
//							gs.rank = StringUtils.defaultString(e
//									.getFormattedScoreRank());
//							try {
//								try {
//									gs.tag = URLDecoder.decode(StringUtils
//											.defaultString(e.getScoreTag()),
//											"UTF-8");
//								} catch (UnsupportedEncodingException e1) {
//									e1.printStackTrace();
//									gs.tag = "Unknown";
//								}
//								gs.value = StringUtils.defaultString(e
//										.getFormattedScore());
//								gs.user = StringUtils.defaultString(e
//										.getPlayer().getDisplayName());
//								gs.imgUrl = StringUtils.defaultString(e
//										.getPlayer().getAvatarImageUrl());
//							} catch (Exception e1) {
//								e1.printStackTrace();
//							}
//							gscores.list.add(gs);
//						}
//					gscores.collection = collection;
//					gscores.ts = ts;
//					postRunnable(callback.with(gscores));
//				} catch (Exception e) {
//					e.printStackTrace();
//					retry(_self);
//				}
//			}
//		};
//		platform.runTask(runnable);
//	}
//
//	@Override
//	public void lb_getListWindowFor(final String boardId,
//			final Collection collection, final TimeSpan ts,
//			final Callback<GameScores> callback) {
//		final Runnable runnable = new Runnable() {
//			private final Runnable _self = this;
//
//			@Override
//			public void run() {
//				GameScores gscores = new GameScores();
//				try {
//					Games g = _getGamesObject();
//					Scores.ListWindow scores = g.scores().listWindow(boardId,
//							collection.name(), ts.name());
//					scores.setMaxResults(5);
//					LeaderboardScores result = scores.execute();
//					List<LeaderboardEntry> list = result.getItems();
//					if (list != null)
//						for (LeaderboardEntry e : list) {
//							GameScore gs = new GameScore();
//							gs.rank = e.getFormattedScoreRank();
//							gs.tag = URLDecoder.decode(
//									StringUtils.defaultString(e.getScoreTag()),
//									"UTF-8");
//							gs.value = e.getFormattedScore();
//							gs.user = e.getPlayer().getDisplayName();
//							gs.imgUrl = e.getPlayer().getAvatarImageUrl();
//							gscores.list.add(gs);
//						}
//					gscores.collection = collection;
//					gscores.ts = ts;
//					postRunnable(callback.with(gscores));
//				} catch (Exception e) {
//					e.printStackTrace();
//					retry(_self);
//				}
//			}
//		};
//		platform.runTask(runnable);
//	}
//
//	private Games _getGamesObject() throws GeneralSecurityException,
//			IOException {
//		_login();
//		Games.Builder b = new Games.Builder(httpTransport, JSON_FACTORY,
//				credential);
//		b.setApplicationName(APP_NAME);
//		Games g = b.build();
//		return g;
//	}
//
//	@Override
//	public void ach_reveal(final String id, final Callback<Void> callback) {
//		platform.runTask(new Runnable() {
//			private final Runnable _self = this;
//
//			@Override
//			public void run() {
//				try {
//					Games g = _getGamesObject();
//					Achievements ac = g.achievements();
//					ac.reveal(id).execute();
//					postRunnable(callback.withNull());
//				} catch (Exception e) {
//					e.printStackTrace();
//					retry(_self);
//				}
//			}
//		});
//	}
//
//	@Override
//	public void ach_unlocked(final String id, final Callback<Void> callback) {
//		platform.runTask(new Runnable() {
//			private final Runnable _self = this;
//
//			@Override
//			public void run() {
//				try {
//					Games g = _getGamesObject();
//					Achievements ac = g.achievements();
//					ac.unlock(id).execute();
//					postRunnable(callback.withNull());
//				} catch (Exception e) {
//					e.printStackTrace();
//					retry(_self);
//				}
//			}
//		});
//	}
//
//	@Override
//	public void ach_list(final Callback<GameAchievements> callback) {
//		final Runnable runnable = new Runnable() {
//			private final Runnable _self = this;
//
//			@Override
//			public void run() {
//				GameAchievements results = new GameAchievements();
//				try {
//					Games g = _getGamesObject();
//					Achievements ac = g.achievements();
//					PlayerAchievementListResponse response = ac.list("me")
//							.execute();
//					List<PlayerAchievement> list = response.getItems();
//					if (list != null)
//						for (PlayerAchievement pa : list) {
//							GameAchievement a = new GameAchievement();
//							a.id = pa.getId();
//							a.state = pa.getAchievementState();
//							results.list.add(a);
//						}
//					postRunnable(callback.with(results));
//				} catch (Exception e) {
//					e.printStackTrace();
//					retry(_self);
//				}
//			}
//		};
//		platform.runTask(runnable);
//	}

	@Override
	public void drive_getFileByTitle(final String title,
			final Callback<String> callback) {
		Callback<FileMetaList> meta_cb = new Callback<FileMetaList>() {
			@Override
			public void success(final FileMetaList result) {
				platform.runTask(new Runnable() {
					private final Runnable _self = this;

					@Override
					public void run() {
						if (result.files.size() == 0) {
							postRunnable(callback.withNull());
							return;
						}
						try {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							Drive drive = _getDriveObject();
							drive.files().get(result.files.get(0).id).executeAndDownloadTo(baos);
							String result = new String(baos.toByteArray(),
									"UTF-8").intern();
							postRunnable(callback.with(result));
						} catch (Exception e) {
							e.printStackTrace();
							retry(_self);
						}
					}
				});
			}

			@Override
			public void error(Exception exception) {
				postRunnable(callback.with(exception));
			}
		};
		drive_getFileMetaByTitle(title, meta_cb);
	}

	/**
	 * Returns list of all matching files by title.
	 */
	@Override
	public void drive_getFileMetaByTitle(final String title,
			final Callback<FileMetaList> callback) {
		Callback<FileMetaList> findFile = new Callback<FileMetaList>() {
			@Override
			public void success(FileMetaList result) {
				Iterator<FileMeta> iter = result.files.iterator();
				while (iter.hasNext()) {
					FileMeta file = iter.next();
					if (!file.title.equals(title)) {
						iter.remove();
						continue;
					}
				}
				postRunnable(callback.with(result));
			}

			@Override
			public void error(Exception exception) {
				postRunnable(callback.with(exception));
			}
		};
		drive_list(findFile);
	}

	/**
	 * Returns meta for file by id
	 */
	@Override
	public void drive_getFileMetaById(final String id,
			final Callback<FileMeta> callback) {
		platform.runTask(new Runnable() {
			private final Runnable _self = this;

			@Override
			public void run() {
				try {
					Drive drive = _getDriveObject();
					File meta = drive.files().get(id).execute();
					FileMeta fm = new FileMeta();
					DateTime createdTime = meta.getCreatedTime();
					DateTime modifiedTime = meta.getModifiedTime();
					fm.created = new Date(createdTime!=null?createdTime.getValue():0);
					fm.lastModified = new Date(modifiedTime!=null?modifiedTime.getValue():0);
					fm.id = meta.getId();
					fm.title = meta.getName();
					fm.url = meta.getWebContentLink();
					postRunnable(callback.with(fm));
				} catch (Exception e) {
					e.printStackTrace();
					retry(_self);
				}
			}
		});
	}

	/**
	 * Lists all files in APP_DATA_FOLDER
	 */
	@Override
	public void drive_list(final Callback<FileMetaList> callback) {
		platform.runTask(new Runnable() {
			private final Runnable _self = this;

			@Override
			public void run() {
				try {
					FileMetaList afs = new FileMetaList();
					Drive drive = _getDriveObject();
					Files.List request = drive.files().list().setSpaces(APP_DATA_FOLDER);
					FileList fl = request.execute();
					List<File> items = fl.getFiles();
					if (items != null)
						for (File item : items) {
							FileMeta af = new FileMeta();
							DateTime createdTime = item.getCreatedTime();
							DateTime modifiedTime = item.getModifiedTime();
							af.created = new Date(createdTime!=null?createdTime.getValue():0);
							af.lastModified = new Date(modifiedTime!=null?modifiedTime.getValue():0);
							af.id = item.getId();
							af.title = item.getName();
							af.url = item.getWebContentLink();// getDownloadUrl();
							afs.files.add(af);
						}
					postRunnable(callback.with(afs));
				} catch (Exception e) {
					e.printStackTrace();
					retry(_self);
				}
			}
		});
	}

	/**
	 * Delete one file by id. Not reversible.
	 * 
	 * @param file
	 * @param title
	 * @param description
	 * @param callback
	 */
	@Override
	public void drive_deleteById(final String id, final Callback<Void> callback) {
		platform.runTask(new Runnable() {
			private final Runnable _self = this;

			@Override
			public void run() {
				try {
					Drive drive = _getDriveObject();
					drive.files().delete(id).execute();
					postRunnable(callback.withNull());
				} catch (Exception e) {
					e.printStackTrace();
					retry(_self);
				}
			}
		});
	}

	/**
	 * Delete all matching files by title. Not reversible.
	 */
	@Override
	public void drive_deleteByTitle(final String title,
			final Callback<Void> callback) {
		final Callback<Void> noop = new Callback<Void>() {
			@Override
			public void success(Void result) {
			}
		};
		Callback<FileMetaList> getTitles = new Callback<FileMetaList>() {
			@Override
			public void success(FileMetaList result) {
				if (result.files != null)
					for (FileMeta file : result.files) {
						if (file.title.equals(title)) {
							drive_deleteById(file.id, noop);
						}
					}
				postRunnable(callback.withNull());
			}
		};
		drive_list(getTitles);
	};

	@Override
	public void drive_replace(final FileHandle file,
			final Callback<String> callback) {
		drive_replace(file, file.file().getName(), file.file().getName(),
				callback);
	}

	@Override
	public void drive_replace(final FileHandle file, final String title,
			final String description, final Callback<String> callback) {
		final FileMetaList[] toDelete = new FileMetaList[1];
		final Callback<String> deleteOthers = new Callback<String>() {
			@Override
			public void success(String result) {
				for (FileMeta file : toDelete[0].files) {
					drive_deleteById(file.id, noop);
				}
				postRunnable(callback.with(result));
			}
		};
		final Callback<FileMetaList> delete_list_callback = new Callback<FileMetaList>() {
			@Override
			public void success(FileMetaList result) {
				toDelete[0] = result;
				Iterator<FileMeta> iter = result.files.iterator();
				while (iter.hasNext()) {
					FileMeta meta = iter.next();
					if (!meta.title.equals(title)) {
						iter.remove();
						continue;
					}
				}
				drive_put(file, title, description, deleteOthers);
			}
		};
		drive_list(delete_list_callback);
	}

	/**
	 * Add file to APP_DATA_FOLDER. Uses source file name as destination "title"
	 * 
	 * @param file
	 * @param callback
	 */
	@Override
	public void drive_put(final FileHandle file, final Callback<String> callback) {
		drive_put(file, file.file().getName(), file.file().getName(), callback);
	}

	/**
	 * Add file to APP_DATA_FOLDER. Uses specified title and description for
	 * metadata. Will not replace any previous files with the same title.
	 */
	@Override
	public void drive_put(final FileHandle file, final String title,
			final String description, final Callback<String> callback) {
		final String _title;
		if (title == null || title.trim().length() == 0) {
			_title = file.file().getName();
		} else {
			_title = title;
		}

		platform.runTask(new Runnable() {
			private final Runnable _self = this;

			@Override
			public void run() {
				try {
					Drive drive = _getDriveObject();
					File meta = new File();
					meta.setParents(Arrays.asList(APP_DATA_FOLDER));
					meta.setName(_title);
					FileContent content = new FileContent(
							"application/octet-stream", file.file());
					Create insert = drive.files().create(meta, content);
					File inserted = insert.execute();
					postRunnable(callback.with(inserted.getId()));
				} catch (Exception e) {
					e.printStackTrace();
					retry(_self);
				}
			}
		});
	}

	@Override
	public void drive_getFileById(final String id,
			final Callback<String> callback) {
		platform.runTask(new Runnable() {
			private final Runnable _self = this;

			@Override
			public void run() {
				try {
					Drive drive = _getDriveObject();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					drive.files().get(id).executeAndDownloadTo(baos);
					String result = new String(baos.toByteArray(), "UTF-8")
							.intern();
					postRunnable(callback.with(result));
				} catch (Exception e) {
					e.printStackTrace();
					retry(_self);
				}
			}
		});
	}

	@Override
	public void drive_getFileById(final String id, final FileHandle file,
			final Callback<Void> callback) {
		platform.runTask(new Runnable() {
			private final Runnable _self = this;

			@Override
			public void run() {
				try {
					Drive drive = _getDriveObject();
					FileOutputStream fos = new FileOutputStream(file.file());
					drive.files().get(id).executeMediaAndDownloadTo(fos);
					postRunnable(callback.withNull());
				} catch (Exception e) {
					e.printStackTrace();
					retry(_self);
				}
			}
		});
	}

	private Drive _getDriveObject() throws GeneralSecurityException,
			IOException {
		_login();
		Drive.Builder b = new Drive.Builder(httpTransport, JSON_FACTORY,
				credential);
		b.setApplicationName(APP_NAME);
		Drive drive = b.build();
		return drive;
	}
}
