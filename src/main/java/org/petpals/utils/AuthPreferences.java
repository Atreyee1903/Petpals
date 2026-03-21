package org.petpals.utils;

import org.petpals.db.UserDAO;
import org.petpals.model.User;

import java.time.Duration;
import java.time.Instant;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class AuthPreferences {

  private static final String PREF_NODE_NAME = "org/petpals";
  private static final String PREF_KEY_USERNAME = "last_username";
  private static final String PREF_KEY_LOGIN_TIME = "last_login_time";
  private static final Duration LOGIN_TIMEOUT = Duration.ofHours(1);

  private static Preferences getPrefs() {
    return Preferences.userRoot().node(PREF_NODE_NAME);
  }

  public static void saveLoginSession(String username) {
    Preferences prefs = getPrefs();
    prefs.put(PREF_KEY_USERNAME, username);
    prefs.putLong(PREF_KEY_LOGIN_TIME, Instant.now().toEpochMilli());
    try {
      prefs.flush();
      System.out.println("Saved login preferences for user: " + username);
    } catch (BackingStoreException e) {
      System.err.println("Error saving login preferences: " + e.getMessage());
    }
  }

  public static User checkRecentLogin() {
    Preferences prefs = getPrefs();
    String username = prefs.get(PREF_KEY_USERNAME, null);
    long lastLoginMillis = prefs.getLong(PREF_KEY_LOGIN_TIME, 0);

    if (username != null && lastLoginMillis > 0) {
      Instant lastLoginTime = Instant.ofEpochMilli(lastLoginMillis);
      Instant expiryTime = lastLoginTime.plus(LOGIN_TIMEOUT);

      if (Instant.now().isBefore(expiryTime)) {
        System.out.println("Found recent login session for user: " + username);
        UserDAO userDAO = new UserDAO();
        User user = userDAO.findUserByUsername(username);
        if (user != null) {
          System.out.println("Successfully retrieved user details for recent session.");
          return user;
        } else {
          System.err.println("Could not find user details for recent session username: " + username + ". Clearing prefs.");
          clearLoginSession();
        }
      } else {
        System.out.println("Recent login session expired. Clearing prefs.");
        clearLoginSession();
      }
    }
    return null;
  }

  public static void clearLoginSession() {
    Preferences prefs = getPrefs();
    prefs.remove(PREF_KEY_USERNAME);
    prefs.remove(PREF_KEY_LOGIN_TIME);
    try {
      prefs.flush();
      System.out.println("Cleared login preferences.");
    } catch (BackingStoreException e) {
      System.err.println("Error clearing login preferences: " + e.getMessage());
    }
  }
}
