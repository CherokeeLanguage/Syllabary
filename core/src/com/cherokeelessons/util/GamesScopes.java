package com.cherokeelessons.util;

/**
 * Available OAuth 2.0 scopes for use with the Google Play Game Services API.
 *
 * @since 1.4
 */
public class GamesScopes {

  /** View and manage its own configuration data in your Google Drive. */
  public static final String DRIVE_APPDATA = "https://www.googleapis.com/auth/drive.appdata";

  /** Share your Google+ profile information and view and manage your game activity. */
  public static final String GAMES = "https://www.googleapis.com/auth/games";

  /** Know the list of people in your circles, your age range, and language. */
  public static final String PLUS_LOGIN = "https://www.googleapis.com/auth/plus.login";

  /**
   * Returns an unmodifiable set that contains all scopes declared by this class.
   *
   * @since 1.16
   */
  public static java.util.Set<String> all() {
    java.util.Set<String> set = new java.util.HashSet<String>();
    set.add(DRIVE_APPDATA);
    set.add(GAMES);
    set.add(PLUS_LOGIN);
    return java.util.Collections.unmodifiableSet(set);
  }

  private GamesScopes() {
  }
}
