package edu.ua.cs.aces;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

public class C2DMMessageReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.w("C2DM", "Message Receiver called");
		if ("com.google.android.c2dm.intent.RECEIVE".equals(action)) {
			Log.w("C2DM", "Received message");
			final String payload = intent.getStringExtra(Constants.EXTRA_GAMEID);
			Log.d("C2DM", "dmControl: payload = " + payload);
			// Send this to my application server
			
			if(payload != null && payload.matches("[0-9]+"))
			{
				notifyGame(context, Integer.parseInt(payload));
			}
		}
	}
	
	public void notifyGame(Context context, int gameId) {
		Resources r = context.getResources();
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		String msg = r.getString(R.string.msg_your_turn);
		Notification notification = new Notification(R.drawable.icon, msg, System.currentTimeMillis());
		// Hide the notification after its selected
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		String title = r.getString(R.string.app_name);
		String body = r.getString(R.string.msg_your_turn_long);
		
		Intent intent = new Intent(context, GameActivity.class);
		intent.putExtra(Constants.EXTRA_GAMEID, gameId);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,	intent, 0);
		notification.setLatestEventInfo(context, title, body, pendingIntent);
		notificationManager.notify(0, notification);
	}
}