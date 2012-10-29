package edu.ua.cs.aces;

import java.net.MalformedURLException;

import edu.ua.cs.aces.api.APIConstants;
import edu.ua.cs.aces.api.APIException;
import edu.ua.cs.aces.api.Session;
import edu.ua.cs.aces.response.APIError;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Constants {
	public static final String
		EXTRA_SESSION = "session",
		EXTRA_GAME = "game",
		EXTRA_GAMEID = "game_id",
		EXTRA_FRIENDID = "friend-id";
	
	public static final String
		C2DM_SENDER = "elementalaces@gmail.com";
	
	public static boolean handleException(Session s, APIException e, Activity act) {
		APIError err = e.getError();
		if(err.isAuthError()) {
			
			Intent i = new Intent(act, LoginActivity.class);
			act.startActivity(i);
			act.finish();
			
			if(s != null) {
				s.forgetUser(act);
			}
			
			return true;
		}
		else {
			Constants.showToast(act, e);
			return false;
		}
	}
	
	public static void showToast(Context ctx, int resId) {
		Resources r = ctx.getResources();
		showToast(ctx, r.getString(resId));
	}
	
	public static void showToast(Context ctx, String msg) {
		Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
	}
	
	public static void showToast(Context ctx, APIException e) {
		showToast(ctx, e.getMessage());
	}
	
	public static void putPref(Context ctx, String name, String value) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		Editor edit = prefs.edit();
		edit.putString(name, value);
		edit.commit();
	}
	
	public static void putBool(Context ctx, String name, boolean value) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		Editor edit = prefs.edit();
		edit.putBoolean(name, value);
		edit.commit();
	}
	
	public static String getPref(Context ctx, String name) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		return prefs.getString(name, "");
	}
	
	public static boolean getBool(Context ctx, String name, boolean defaultValue) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		return prefs.getBoolean(name, defaultValue);
	}
	
	public static void deletePref(Context ctx, String name) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		
		Editor e = prefs.edit();
		e.remove(name);
		e.commit();
	}
	
	public static void attemptC2DMAssociation(Context context) {
		attemptC2DMAssociation(context, Constants.getPref(context, APIConstants.C2DM_REGID));
	}

	public static void attemptC2DMAssociation(Context context, String registrationId) {
		if(context == null || registrationId == null || registrationId.length() == 0) return;
		
		if (Constants.getBool(context, APIConstants.C2DM_ASSOCIATED,false)) {
			return;
		}
		
		try {
			Session session = Session.makeSession(context);
			session.associateC2DM(registrationId);
			Constants.putPref(context, APIConstants.C2DM_REGID, registrationId);
			Constants.putBool(context, APIConstants.C2DM_ASSOCIATED, true);
		} catch (MalformedURLException e) {
			Constants.showToast(context, e.getMessage());
		} catch (APIException e) {
			Constants.showToast(context, e);
		}		
	}
}
