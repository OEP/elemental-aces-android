package edu.ua.cs.aces;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import edu.ua.cs.aces.api.APIConstants;
import edu.ua.cs.aces.api.APIException;
import edu.ua.cs.aces.api.Session;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.util.Log;

public class C2DMRegistrationReceiver extends BroadcastReceiver {

	@TargetApi(3)
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.w("C2DM", "Registration Receiver called");
		if ("com.google.android.c2dm.intent.REGISTRATION".equals(action)) {
			Log.w("C2DM", "Received registration ID");
			final String registrationId = intent
					.getStringExtra("registration_id");
			String error = intent.getStringExtra("error");
			
			int length = (registrationId != null) ? registrationId.length() : 0;

			Log.d("C2DM", "dmControl: registrationId = " + registrationId + ", error = " + error);
			Log.d("C2DM", "dmControl: registration length = " + length);
			
			saveRegistrationId(context, registrationId);
			
			Constants.attemptC2DMAssociation(context, registrationId);
		}
	}

	private void saveRegistrationId(Context context, String registrationId) {
		if(registrationId == null) return;
		String oldId = Constants.getPref(context, APIConstants.C2DM_REGID);
		
		if(oldId.compareTo(registrationId) != 0) {
			Constants.putPref(context, APIConstants.C2DM_REGID, registrationId);
			Constants.putBool(context, APIConstants.C2DM_ASSOCIATED, false);
		}
	}

	public void createNotification(Context context, String registrationId) {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.icon,
				"Registration successful", System.currentTimeMillis());
		// Hide the notification after its selected
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		Intent intent = new Intent(context, GameActivity.class);
		intent.putExtra("registration_id", registrationId);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, 0);
		notification.setLatestEventInfo(context, "Registration",
				"Successfully registered", pendingIntent);
		notificationManager.notify(0, notification);
	}
}