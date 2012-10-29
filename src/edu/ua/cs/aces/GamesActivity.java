package edu.ua.cs.aces;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import edu.ua.cs.aces.adapters.GameListAdapter;
import edu.ua.cs.aces.api.APIException;
import edu.ua.cs.aces.api.Session;
import edu.ua.cs.aces.response.Game;

public class GamesActivity extends ListActivity {
	public static final String TAG = "GameList";
	
	protected Game[] mGames;
	protected Session mSession;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_games);

		Intent i = getIntent();
		mSession = (Session) i.getParcelableExtra(Constants.EXTRA_SESSION);
		
		// Start the C2DM service.
		Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
		registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0)); // boilerplate
		registrationIntent.putExtra("sender", Constants.C2DM_SENDER);
		startService(registrationIntent);
	}
	
	protected void onResume() {
		super.onResume();
		Button b = (Button) findViewById(R.id.games_logout_button);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mSession.forgetUser(GamesActivity.this);
				Intent i = new Intent(GamesActivity.this, LoginActivity.class);
				startActivity(i);
				GamesActivity.this.finish();
			}
		});
		
		setupList();
	}
	
	public Dialog onCreateDialog(int dialogId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = LayoutInflater.from(this);
		
		switch(dialogId) {
		case GamesActivity.DIALOG_CHALLENGE:
			final View v = inflater.inflate(R.layout.dialog_challenge,null);
			return builder.setTitle(R.string.app_name)
				.setMessage(R.string.msg_challenge)
				.setView(v)
				.setPositiveButton(R.string.label_challenge, new OnClickListener() {
					@Override
					public void onClick(DialogInterface di, int wut) {
						EditText et = (EditText) v.findViewById(R.id.dialog_challenge_username);
						String username = et.getText().toString();
						try {
							Game g = mSession.challenge(username);
							launchGame(g);
						} catch (APIException e) {
							Constants.showToast(GamesActivity.this, e.getMessage());
						}
					}
				})
				.setNegativeButton(R.string.label_cancel, null)
				.create();
		}
		
		return builder.setMessage("Invalid dialog ID!").setTitle(R.string.app_name).create();
	}

	private void setupList() {
		GameListAdapter adapter;
		setListAdapter(adapter = new GameListAdapter(this, mSession));
		adapter.repopulate();
	}
	
	protected void onListItemClick(ListView list, View v, int position, long id) {
		Game g = (Game) list.getItemAtPosition(position);
		launchGame(g);
	}
	
	protected void launchGame(Game g) {
		Intent i = new Intent(this, GameActivity.class);
		i.putExtra(Constants.EXTRA_GAME, g);
		i.putExtra(Constants.EXTRA_SESSION, mSession);
		startActivity(i);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.games_options_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.game_add:
			showDialog(GamesActivity.DIALOG_CHALLENGE);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public static final int
		DIALOG_CHALLENGE = 0xF173;
}
