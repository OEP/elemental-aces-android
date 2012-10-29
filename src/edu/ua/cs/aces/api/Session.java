package edu.ua.cs.aces.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;

import edu.ua.cs.aces.Constants;
import edu.ua.cs.aces.response.APIResponse;
import edu.ua.cs.aces.response.Card;
import edu.ua.cs.aces.response.ChatMessage;
import edu.ua.cs.aces.response.Game;
import edu.ua.cs.aces.response.GameBoard;
import edu.ua.cs.aces.response.Move;
import edu.ua.cs.aces.response.Profile;
import edu.ua.cs.aces.response.Tile;
import edu.ua.cs.aces.response.User;

public class Session implements Parcelable {
	/** Debug tag */
	public static final String TAG = "Session";
	
	/** The default API server */
	public static final String API_URL = "http://brujo.cs.ua.edu";
	
	/** The URL of the server we are working with */
	private URL mURL;
	
	/** Last HTTP response code from the Worddit server */
	private int mLastResponse;
	
	/** The logged-in user's profile */
	protected User mUser;
	
	/** This object can't be instantiated directly. */
	private Session() { }
	
	/** The Session object is restorable from a Parcel 
	 * @throws MalformedURLException */
	private Session (Parcel in) throws MalformedURLException {
		mURL = new URL( in.readString() );
		mUser = in.readParcelable(Profile.class.getClassLoader());
	}

	/**
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws IOException
	 */
	public User createAccount(String username, String password, Context ctx)
	throws APIException {
		APIResponse response = post(
				APIConstants.PATH_USER_ADD,
				APIConstants.USERNAME, username,
				APIConstants.PASSWORD, password);
		
		mUser = response.user;
		rememberUser(ctx);
		return response.user;
	}
	
	public User login(String username, String password, Context ctx) throws APIException {
		APIResponse response = post(
				APIConstants.PATH_USER_LOGIN,
				APIConstants.USERNAME, username,
				APIConstants.PASSWORD, password);
		
		mUser = response.user;
		rememberUser(ctx);
		return response.user;
	}
	
	public Game getGame(int gameId) throws APIException {
		APIResponse response = get(
				String.format(APIConstants.PATH_GET_GAME, gameId)
		);
		
		return response.game;
	}
	
	public Game[] getGames() throws APIException {
		APIResponse response = get(
			APIConstants.PATH_GET_GAMES
		);
		
		return response.games;
	}
	
	public Card[] getHand(int gameId) throws APIException {
		APIResponse response = get(
			String.format(APIConstants.PATH_GAME_HAND, gameId)
		);
		return response.playerHand;
	}
	
	public Game challenge(String username) throws APIException {
		APIResponse response = post(APIConstants.PATH_CHALLENGE,
			APIConstants.USERNAME, username
		);
		
		return response.game;
	}
	
	public User associateC2DM(String key) throws APIException {
		APIResponse response = post(APIConstants.PATH_ASSOCIATE_C2DM,
				APIConstants.C2DM_REGID, key
			);
		
		return response.user;
	}
	
	public Game move(int gameId, int moveNumber, int elementId, int magicId) throws APIException {
		APIResponse response = (magicId > 0) ? 
			post(
				String.format(APIConstants.PATH_MOVE, gameId, moveNumber),
				APIConstants.ELEMENT_CARD_ID, String.valueOf(elementId),
				APIConstants.MAGIC_CARD_ID, String.valueOf(magicId)
		)
		:
			post(
				String.format(APIConstants.PATH_MOVE, gameId, moveNumber),
				APIConstants.ELEMENT_CARD_ID, String.valueOf(elementId)
		);
		
		return response.game;
	}
	
	/**
	 * Returns the profile for the logged-in user.
	 * @return profile of logged-in user or null if nonexistent
	 */
	public User getUserProfile() {
		return mUser;
	}
	
	/**
	 * Returns the username this Session belongs to.
	 * @return null if not authenticated, or the username of the authenticated user
	 */
	public String getUsername() {
		return (this.isAuthenticated()) ? mUser.username : null;
	}
	
	/**
	 * Retrieve the last HTTP response code given by
	 * the Worddit server on the last API call.
	 * @return last HTTP response code
	 */
	public int getLastResponse() {
		return mLastResponse;
	}
	
	/**
	 * Check if this Session instance is authenticated.
	 * @return true if the cookie is set, false if it is not
	 */
	public boolean isAuthenticated() {
		return mUser != null;
	}
	
	/**
	 * Get the authentication cookie.
	 * @return cookie we're currently using, or null if there is none
	 */
	public String getAuthKey() {
		return (mUser != null) ? mUser.authKey : null;
	}
	
	/**
	 * Set the authentication cookie.
	 * @param username user this session belongs to
	 * @param authKey authentication key for user
	 */
	public void setAuthInfo(String username, String authKey) {
		if(username == null || authKey == null || username.length() == 0 || authKey.length() == 0) {
			mUser = null;
			return;
		}
		mUser = new User(username, authKey);
	}
	
	public String getURL() {
		Log.i(TAG, "String URL is: " + mURL.toExternalForm());
		return mURL.toString();
	}
	
	private String getSessionId()	{
		if(mUser == null) return null;
		return Session.makeAuthKey(mUser.username, mUser.authKey);
	}
	
	private APIResponse getResponse(HttpURLConnection conn) throws APIException {
		APIResponse r = castJson(conn, APIResponse.class);
		
		if(r.error != null) {
			throw new APIException(r.error);
		}
		return r;
	}
	
	public void forgetUser(Context ctx) {
		if(ctx == null) return;
		Constants.deletePref(ctx, APIConstants.USERNAME);
		Constants.deletePref(ctx, APIConstants.AUTHKEY);
	}
	
	private void rememberUser(Context ctx) {
		if(!isAuthenticated()) return;
		if(ctx == null) return;
		Constants.putPref(ctx, APIConstants.USERNAME, mUser.username);
		Constants.putPref(ctx, APIConstants.AUTHKEY, mUser.authKey);
	}
	
	
	
	/**
	 * Initiate an HTTP POST for the URL-encoded parameters passed.
	 * @param path to make the HTTP POST to
	 * @param params Arbitrary-length list of key/value pairs
	 * @return <code>HttpURLConnection</code> representing this connection.
	 * @throws APIException if there's big trouble
	 */
	private APIResponse post(String path, String ... params) throws APIException {
		try {
			Log.i(TAG, "POST " + path);
			String encodedArgs = HttpHelper.encodeParams(params);
			HttpURLConnection connection = HttpHelper.makePost(mURL, path, encodedArgs, getSessionId());
			mLastResponse = connection.getResponseCode();
			return getResponse(connection);
		}
		catch(IOException e) {
			wrapException(e);
			return null;
		}
	}
	
	/**
	 * This is a helper function to take an open <code>HttpURLConnection</code>, read the JSON
	 * payload it contains, and cast it as some object.
	 * This method also works by magic.
	 * @param <T> the object type to cast to
	 * @param connection to read JSON data from
	 * @param type of the object
	 * @return JSON cast as the specified object
	 * @throws APIException 
	 */
	private <T> T castJson(HttpURLConnection connection, Class<T> type)
	throws APIException
	 {
		try {
			BufferedReader reader =
				new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson gson = new Gson();
			return gson.fromJson(reader,type);
		}
		catch(IOException e) {
			wrapException(e);
			return null;
		}
	}
	
	private String collapse(List<String> strings, char delimeter) {
		StringBuffer output = new StringBuffer();
		for(Iterator<String> it = strings.iterator(); it.hasNext(); ) {
			String id = it.next();
			output.append(id);
			// Add a comma if needed
			if(it.hasNext()) output.append(delimeter);
		}
		return output.toString();
	}
	
	/*private <T> String collapse(T array[], char delimeter) {
		StringBuffer output = new StringBuffer();
		for(int i = 0; i < array.length; i++) {
			T bit = array[i];
			output.append(bit);
			if(i < array.length - 1) output.append(',');
		}
		return output.toString();
	}*/
	
	/**
	 * Initiate an HTTP GET for the given path.
	 * @param path to perform GET command to
	 * @return an <code>HttpURLConnection</code> representing the connection
	 * @throws APIException if there's big trouble
	 */
	private APIResponse get(String path) throws APIException {
		try {
			HttpURLConnection connection = HttpHelper.makeGet(mURL, path, getSessionId());
			Log.i(TAG, "GET " + path);
			mLastResponse = connection.getResponseCode();
			return getResponse(connection);
		}
		catch(IOException e) {
			wrapException(e);
			return null;
		}
	}
	
	private void wrapException(Exception e) throws APIException {
		String msg = e.getMessage();
		if(msg != null && msg.length() > 0) {
			throw new APIException(String.format("There was a connection error: %s", msg));
		}
		else {
			throw new APIException("There was a connection error. This can be due to intermittent network connectivity. Please check for network connectivity and try again.");
		}
	}
	
	/**
	 * Make a <code>Session</code> object from the hard-coded <code>API_URL</code>.
	 * @return Session object set up for the default URL.
	 * @throws MalformedURLException If the URL is not valid.
	 */
	public static Session makeSession() throws MalformedURLException {
		return Session.makeSession(API_URL);
	}
	
	public static Session makeSession(Context ctx) throws MalformedURLException {
		String username = Constants.getPref(ctx, APIConstants.USERNAME);
		String authKey = Constants.getPref(ctx, APIConstants.AUTHKEY);
		return makeSession(API_URL, username, authKey);
	}
	
	/**
	 * Create a session which uses a provided URL as the 
	 * game server location.
	 * @param url Which URL to use as the game server.
	 * @return Session object set up for the URL
	 * @throws MalformedURLException If the URL is invalid.
	 */
	public static Session makeSession(String url) throws MalformedURLException {
		return makeSession(url,null,null);
	}
	
	/**
	 * Create a session which uses a provided URL as the 
	 * game server location.
	 * @param url Which URL to use as the game server.
	 * @param authKey A authentication key to start using.
	 * @return Session object set up for the URL
	 * @throws MalformedURLException If the URL is invalid.
	 */
	public static Session makeSession(String url, String username, String authKey) throws MalformedURLException {
		Session s = new Session();
		s.mURL = new URL(url);
		s.setAuthInfo(username, authKey);
		return s;
	}
	
	/**
	 * Creates Session ID using username and authKey.
	 * @param username Username this session belongs to.
	 * @param key Authentication key
	 * @return A session id for the user.
	 */
	public static String makeAuthKey(String username, String key) {
		return String.format("%s:%s", key, username);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(getURL());
		out.writeParcelable(mUser, 0);		
	}
	
	/** Generates Session objects for the Parcelable subsystem */
	public static final Parcelable.Creator<Session> CREATOR
		= new Parcelable.Creator<Session>() {
			@Override
			public Session createFromParcel(Parcel in) {
				try {
					return new Session(in);
				} catch (MalformedURLException e) {
					e.printStackTrace();
					return null;
				}
			}

			@Override
			public Session[] newArray(int size) {
				return new Session[size];
			}
	};
}
