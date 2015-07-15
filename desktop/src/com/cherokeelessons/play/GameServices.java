package com.cherokeelessons.play;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.cherokeelessons.util.GooglePlayGameServices;
import com.cherokeelessons.util.GooglePlayGameServices.FileMetaList.FileMeta;
import com.cherokeelessons.util.GooglePlayGameServices.GameAchievements.GameAchievement;
import com.cherokeelessons.util.GooglePlayGameServices.GameScores.GameScore;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.Drive.Files.Insert;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.games.Games;
import com.google.api.services.games.Games.Achievements;
import com.google.api.services.games.Games.Scores;
import com.google.api.services.games.Games.Scores.Submit;
import com.google.api.services.games.GamesScopes;
import com.google.api.services.games.model.LeaderboardEntry;
import com.google.api.services.games.model.LeaderboardScores;
import com.google.api.services.games.model.PlayerAchievement;
import com.google.api.services.games.model.PlayerAchievementListResponse;
import com.google.api.services.games.model.PlayerLeaderboardScore;
import com.google.api.services.games.model.PlayerLeaderboardScoreListResponse;

public class GameServices implements GooglePlayGameServices {

	public static String APP_NAME = "ᏣᎳᎩ ᎦᏬᏂᎯᏍᏗ/1.0";
	public static interface PlatformInterface {
		public static final String USER = "user";
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
			if (flow==null) {
				return false;
			}
			return flow.loadCredential(PlatformInterface.USER)!=null;
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

		ArrayList<String> scopes = new ArrayList<String>();
		scopes.add(GamesScopes.DRIVE_APPDATA);
		scopes.add(GamesScopes.GAMES);
		scopes.add(GamesScopes.PLUS_LOGIN);

		GoogleAuthorizationCodeFlow.Builder builder = new GoogleAuthorizationCodeFlow.Builder(
				httpTransport, JSON_FACTORY, clientSecrets, scopes);
		builder.setScopes(scopes);
		return builder.setAccessType("offline")
				.setDataStoreFactory(dataStoreFactory).build();
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
					Gdx.app.log(this.getClass().getName(), "logout:init");
					init();
					Gdx.app.log(this.getClass().getName(), "logout:getflow");
					GoogleAuthorizationCodeFlow flow = getFlow();
					Gdx.app.log(this.getClass().getName(), "logout:flow#clear");
					flow.getCredentialDataStore().clear();
					Gdx.app.log(this.getClass().getName(), "credential=null");
					credential=null;
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

	@Override
	public void lb_submit(final String boardId, final long score,
			final String label, final Callback<Void> callback) {
		final Runnable runnable = new Runnable() {
			private final Runnable _self=this;
			@Override
			public void run() {
				try {
					Games g = _getGamesObject();
					Submit submit = g.scores().submit(boardId, score);
					String tag = URLEncoder.encode(label, "UTF-8");
					submit.setScoreTag(tag);
					submit.execute();
					postRunnable(callback.withNull());
				} catch (Exception e) {
					retry(_self);
				}
			} 
		};
		platform.runTask(runnable);
	}

	@Override
	public void lb_getScoresFor(final String boardId,
			final Callback<GameScores> callback) {
		final Runnable runnable = new Runnable() {
			private final Runnable _self=this;
			@Override
			public void run() {
				GameScores gscores = new GameScores();
				try {
					Games g = _getGamesObject();
					Scores.Get scores = g.scores().get("me", boardId,
							TimeSpan.ALL_TIME.name());
					scores.setMaxResults(30);
					PlayerLeaderboardScoreListResponse result = scores
							.execute();
					List<PlayerLeaderboardScore> list = result.getItems();
					for (PlayerLeaderboardScore e : list) {
						GameScore gs = new GameScore();
						gs.rank = "";
						gs.tag = URLDecoder.decode(e.getScoreTag(), "UTF-8");
						gs.value = e.getScoreString();
						gs.user = "";
						gscores.list.add(gs);
					}
					postRunnable(callback.with(gscores));
				} catch (Exception e) {
					retry(_self);
				}
			}
		};
		platform.runTask(runnable);
	}

	@Override
	public void lb_getListFor(final String boardId,
			final Collection collection, final TimeSpan ts,
			final Callback<GameScores> callback) {
		final Runnable runnable = new Runnable() {
			private final Runnable _self = this;
			@Override
			public void run() {
				GameScores gscores = new GameScores();
				try {
					Gdx.app.log(TAG,
							"Loading Leaderboard: " + collection.name() + " - "
									+ ts.name() + " - " + boardId);

					Games g = _getGamesObject();
					Scores.List scores = g.scores().list(boardId,
							collection.name(), ts.toString());
					scores.setMaxResults(30);
					LeaderboardScores result = scores.execute();
					List<LeaderboardEntry> list = result.getItems();
					if (list == null) {
						postRunnable(callback.with(gscores));
						return;
					}
					for (LeaderboardEntry e : list) {
						GameScore gs = new GameScore();
						gs.rank = e.getFormattedScoreRank();
						gs.tag = URLDecoder.decode(e.getScoreTag(), "UTF-8");
						gs.value = e.getFormattedScore();
						gs.user = e.getPlayer().getDisplayName();
						gs.imgUrl = e.getPlayer().getAvatarImageUrl();
						gscores.list.add(gs);
					}
					gscores.collection = collection;
					gscores.ts = ts;
					postRunnable(callback.with(gscores));
				} catch (Exception e) {
					retry(_self);
				}
			}
		};
		platform.runTask(runnable);
	}

	@Override
	public void lb_getListWindowFor(final String boardId,
			final Collection collection, final TimeSpan ts,
			final Callback<GameScores> callback) {
		final Runnable runnable = new Runnable() {
			private final Runnable _self = this;
			@Override
			public void run() {
				GameScores gscores = new GameScores();
				try {
					Games g = _getGamesObject();
					Scores.ListWindow scores = g.scores().listWindow(boardId,
							collection.name(), ts.name());
					scores.setMaxResults(5);
					LeaderboardScores result = scores.execute();
					List<LeaderboardEntry> list = result.getItems();
					for (LeaderboardEntry e : list) {
						GameScore gs = new GameScore();
						gs.rank = e.getFormattedScoreRank();
						gs.tag = URLDecoder.decode(e.getScoreTag(), "UTF-8");
						gs.value = e.getFormattedScore();
						gs.user = e.getPlayer().getDisplayName();
						gs.imgUrl = e.getPlayer().getAvatarImageUrl();
						gscores.list.add(gs);
					}
					gscores.collection = collection;
					gscores.ts = ts;
					postRunnable(callback.with(gscores));
				} catch (Exception e) {
					retry(_self);
				}
			}
		};
		platform.runTask(runnable);
	}

	private Games _getGamesObject() throws GeneralSecurityException,
			IOException {
		_login();
		Games.Builder b = new Games.Builder(httpTransport, JSON_FACTORY,
				credential);
		b.setApplicationName(APP_NAME);
		Games g = b.build();
		return g;
	}

	@Override
	public void ach_reveal(final String id, final Callback<Void> callback) {
		platform.runTask(new Runnable() {
			private final Runnable _self=this;
			@Override
			public void run() {
				try {
					Games g = _getGamesObject();
					Achievements ac = g.achievements();
					ac.reveal(id).execute();
					postRunnable(callback.withNull());
				} catch (Exception e) {
					retry(_self);
				}
			}
		});
	}

	@Override
	public void ach_unlocked(final String id, final Callback<Void> callback) {
		platform.runTask(new Runnable() {
			private final Runnable _self=this;
			@Override
			public void run() {
				try {
					Games g = _getGamesObject();
					Achievements ac = g.achievements();
					ac.unlock(id).execute();
					postRunnable(callback.withNull());
				} catch (Exception e) {
					retry(_self);
				}
			}
		});
	}

	@Override
	public void ach_list(final Callback<GameAchievements> callback) {
		final Runnable runnable = new Runnable() {
			private final Runnable _self=this;
			@Override
			public void run() {
				GameAchievements results = new GameAchievements();
				try {
					Games g = _getGamesObject();
					Achievements ac = g.achievements();
					PlayerAchievementListResponse response = ac.list("me")
							.execute();
					List<PlayerAchievement> list = response.getItems();
					for (PlayerAchievement pa : list) {
						GameAchievement a = new GameAchievement();
						a.id = pa.getId();
						a.state = pa.getAchievementState();
						results.list.add(a);
					}
					postRunnable(callback.with(results));
				} catch (Exception e) {
					retry(_self);
				}
			}
		};
		platform.runTask(runnable);
	}

	@Override
	public void drive_getFileByTitle(final String title,
			final Callback<String> callback) {
		Callback<FileMetaList> meta_cb = new Callback<FileMetaList>() {
			@Override
			public void success(final FileMetaList result) {
				platform.runTask(new Runnable() {
					private final Runnable _self=this;
					@Override
					public void run() {
						if (result.files.size() == 0) {
							postRunnable(callback.withNull());
							return;
						}
						try {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							MediaHttpDownloader downloader = new MediaHttpDownloader(
									httpTransport, credential);
							downloader.download(
									new GenericUrl(result.files.get(0).url),
									baos);
							String result = new String(baos.toByteArray(),
									"UTF-8").intern();
							postRunnable(callback.with(result));
						} catch (Exception e) {
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
					Gdx.app.log(TAG, file.title);
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
			private final Runnable _self=this;
			@Override
			public void run() {
				try {
					Drive drive = _getDriveObject();
					File meta = drive.files().get(id).execute();
					FileMeta fm = new FileMeta();
					fm.created = new Date(meta.getCreatedDate().getValue());
					fm.id = meta.getId();
					fm.isAppData = meta.getAppDataContents();
					fm.lastModified = new Date(meta.getModifiedDate()
							.getValue());
					fm.title = meta.getTitle();
					fm.url = meta.getDownloadUrl();
					postRunnable(callback.with(fm));
				} catch (Exception e) {
					retry(_self);
				} 
			}
		});
	}

	/**
	 * Lists all files in 'appfolder'
	 */
	@Override
	public void drive_list(final Callback<FileMetaList> callback) {
		platform.runTask(new Runnable() {
			private final Runnable _self=this;
			@Override
			public void run() {
				try {
					FileMetaList afs = new FileMetaList();
					Drive drive = _getDriveObject();

					Files.List request = drive.files().list();
					request.setQ("'appfolder' in parents");
					FileList fl = request.execute();
					List<File> items = fl.getItems();
					for (File item : items) {
						FileMeta af = new FileMeta();
						af.isAppData = item.getAppDataContents();
						af.created = new Date(item.getCreatedDate().getValue());
						af.id = item.getId();
						af.lastModified = new Date(item.getModifiedDate()
								.getValue());
						af.title = item.getTitle();
						af.url = item.getDownloadUrl();
						afs.files.add(af);
					}
					postRunnable(callback.with(afs));
				} catch (Exception e) {
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
			private final Runnable _self=this;
			@Override
			public void run() {
				try {
					Drive drive = _getDriveObject();
					drive.files().delete(id).execute();
					postRunnable(callback.withNull());
				} catch (Exception e) {
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
		Gdx.app.log(TAG, "drive_replace: " + title);
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
	 * Add file to 'appfolder'. Uses source file name as destination "title"
	 * 
	 * @param file
	 * @param callback
	 */
	@Override
	public void drive_put(final FileHandle file, final Callback<String> callback) {
		drive_put(file, file.file().getName(), file.file().getName(), callback);
	}

	/**
	 * Add file to 'appfolder'. Uses specified title and description for
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
			private final Runnable _self=this;
			@Override
			public void run() {
				try {
					Drive drive = _getDriveObject();
					File meta = new File();
					meta.setParents(Arrays.asList(new ParentReference()
							.setId("appfolder")));
					meta.setTitle(_title);
					FileContent content = new FileContent(
							"application/octet-stream", file.file());
					Insert insert = drive.files().insert(meta, content);
					File inserted = insert.execute();
					postRunnable(callback.with(inserted.getId()));
				} catch (Exception e) {
					retry(_self);
				}
			}
		});
	}

	@Override
	public void drive_getFileById(final String id,
			final Callback<String> callback) {
		platform.runTask(new Runnable() {
			private final Runnable _self=this;
			@Override
			public void run() {
				try {
					Drive drive = _getDriveObject();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					File meta = drive.files().get(id).execute();
					MediaHttpDownloader downloader = new MediaHttpDownloader(
							httpTransport, credential);
					downloader.download(new GenericUrl(meta.getDownloadUrl()),
							baos);
					String result = new String(baos.toByteArray(), "UTF-8")
							.intern();
					postRunnable(callback.with(result));
				} catch (Exception e) {
					retry(_self);
				}
			}
		});
	}

	@Override
	public void drive_getFileById(final String id, final FileHandle file,
			final Callback<Void> callback) {
		platform.runTask(new Runnable() {
			private final Runnable _self=this;
			@Override
			public void run() {
				try {
					Drive drive = _getDriveObject();
					FileOutputStream fos = new FileOutputStream(file.file());
					File meta = drive.files().get(id).execute();
					MediaHttpDownloader downloader = new MediaHttpDownloader(
							httpTransport, credential);
					downloader.download(new GenericUrl(meta.getDownloadUrl()),
							fos);
					postRunnable(callback.withNull());
				} catch (Exception e) {
					retry(_self);
				}
			}
		});
	}

	@Override
	public void drive_getFileByUrl(final String url, final FileHandle file,
			final Callback<Void> callback) {
		platform.runTask(new Runnable() {
			private final Runnable _self=this;
			@Override
			public void run() {
				try {
					FileOutputStream fos = new FileOutputStream(file.file());
					MediaHttpDownloader downloader = new MediaHttpDownloader(
							httpTransport, credential);
					downloader.download(new GenericUrl(url), fos);
					postRunnable(callback.withNull());
				} catch (Exception e) {
					retry(_self);
				}
			}
		});
	}

	@Override
	public void drive_getFileByUrl(final String url,
			final Callback<String> callback) {
		platform.runTask(new Runnable() {
			private final Runnable _self=this;
			@Override
			public void run() {
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					MediaHttpDownloader downloader = new MediaHttpDownloader(
							httpTransport, credential);
					downloader.download(new GenericUrl(url), baos);
					String result = new String(baos.toByteArray(), "UTF-8")
							.intern();
					postRunnable(callback.with(result));
				} catch (Exception e) {
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
