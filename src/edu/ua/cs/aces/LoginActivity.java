package edu.ua.cs.aces;

import java.io.IOException;
import java.net.MalformedURLException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;

import edu.ua.cs.aces.R;

import edu.ua.cs.aces.api.APIException;
import edu.ua.cs.aces.api.Session;
import edu.ua.cs.aces.response.User;


public class LoginActivity extends Activity {
	/** Debug tag */
	public static final String TAG = "WordditHome";
	
	/** Convenient matter to change the default URL to use */
	private String URL = Session.API_URL;
	
	/** Session object to use */
	private Session mSession;
	
	/** Could we get a handle on the Window's progress bar */
	private boolean mWindowIndeterminate = false;
	
	/** Temporary variable for showing errors from API */
	private String mMessage;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		mWindowIndeterminate = requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_login);
		setup();
		
		if(icicle != null) {
			restoreFromBundle(icicle);
		}
	}
	
	/** Called when we create a dialog using showDialog(int) */
	public Dialog onCreateDialog(int id) {
		Resources r = getResources();
		
		// Catch if we're making a progress dialog
		// TODO: This could cause a problem if we use showDialog for String resource '1'
		if (id == DIALOG_WAIT) {
			ProgressDialog dlg = new ProgressDialog(this);
			dlg.setTitle(R.string.app_name);
			dlg.setMessage(r.getString(R.string.msg_communicating));
			return dlg;
		}
		else if(id == DIALOG_RAW) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			String msg = mMessage;
			
			return builder.setMessage(msg)
				.setTitle(R.string.app_name)
				.setCancelable(true)
				.setNeutralButton(R.string.label_ok, null)
				.create();
		}

		// Revert to the default behavior, which is treat 'id' as a String resource
		// and show a dialog displaying the corresponding message.
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String msg = "(null)";
		
		try { msg = r.getString(id); }
		catch(Exception e) { msg = String.format(r.getString(R.string.msg_not_found), id); }
		
		return builder.setMessage(msg)
			.setTitle(R.string.app_name)
			.setCancelable(true)
			.setNeutralButton(R.string.label_ok, null)
			.create();
	}
	
	private void showMessage(String msg) {
		mMessage = msg;
		showDialog(DIALOG_RAW);
	}
	
	/**
	 * Saves the state of this Activity.
	 */
	protected void onSaveInstanceState(Bundle icicle) {
		super.onSaveInstanceState(icicle);
		icicle.putInt(CONFIRM_STATE,
				findViewById(R.id.login_input_confirmpassword).getVisibility());
	}
	
	/**
	 * Restores the state of this Activity from a bundle.
	 * @param b the bundle to restore from.
	 */
	protected void restoreFromBundle(Bundle b) {
		// Tracking the visibility of the "confirm password" field
		// has been a problem with orientation changes. This fixes that.
		int foo = b.getInt(CONFIRM_STATE);
		findViewById(R.id.login_input_confirmpassword).setVisibility(foo);
	}
	
	private void setup() {
		// Force confirm password field to reflect default state of checkbox
		doNewChecked(null);
		
		// Setup listeners
		setupListeners();
		
		// Load up a session object
		setupSession();
	}
	
	/** Attempts to load the session */
	private void setupSession() {
		try {
			mSession = Session.makeSession(this);
			
			if(mSession.isAuthenticated()) {
				startGamesActivity();
			}
		} catch (MalformedURLException e) {
			showDialog(R.string.msg_setup_error);
		}
	}
	
	private void setupListeners() {
		findViewById(R.id.login_button_login)
			.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				doButtonClick(v);
			}
		});
		
		findViewById(R.id.login_check_new)
			.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				doNewChecked(v);
			}
		});
	}
	
	/** This checks the state of the check box and displays the "confirm" field if needed. */
	public void doNewChecked(View v) {
		CheckBox check = (CheckBox) findViewById(R.id.login_check_new);
		EditText confirm = (EditText) findViewById(R.id.login_input_confirmpassword);
		
		if(check.isChecked()) {
			confirm.setVisibility(View.VISIBLE);
		} else {
			confirm.setVisibility(View.GONE);
		}
	}
	
	/** This method is called if the "Login" button is clicked */
	public void doButtonClick(View v) {
		CheckBox check = (CheckBox) findViewById(R.id.login_check_new);
		if(check.isChecked()) {
			doCreate(v);
		}
		else {
			doLogin(v);
		}
	}
	
	/** Helper method to easily pull the email and password from the TextViews */
	public void doLogin(View v) {
		doLogin(getEmailField(), getPasswordField());
	}
	
	/** Helper method to get email, password, and confirm fields from TextViews */
	public void doCreate(View v) {
		String email = getEmailField(), password = getPasswordField(), confirm = getConfirmField();
		doCreate(email,password,confirm);
	}
	
	/** Manipulates UI widgets if we should be in a loading state */
	protected void setLoading(boolean state) {
		// Do this if the device supports having a progress indicator in the
		// activity's toolbar
		if(mWindowIndeterminate == true) {
			this.setProgressBarIndeterminate(true);
			this.setProgressBarIndeterminateVisibility(state);
		}
		
		// Otherwise, just show a boring progress dialog.
		else if(state == true) {
			showDialog(DIALOG_WAIT);
		}
		else if(state == false) {
			removeDialog(DIALOG_WAIT);
		}
	}
	
	protected void doLogin(String username, String password) {
		boolean fail = false;
		int msg = 0;
		
		if(username.length() == 0 || password.length() == 0) {
			msg = R.string.msg_required_fields_missing;
			fail = true;
		}
		
		if(fail) {
			showDialog(msg);
		}
		else {
			try {
				User u = mSession.login(username, password, this);
				startGamesActivity();
			} catch (APIException e) {
				showMessage(e.getMessage());
			}
		}
	}
	
	protected void doCreate(String username, String password, String confirm) {
		boolean fail = false;
		int msg = 0;
		
		if(username.length() == 0 || password.length() == 0 || confirm.length() == 0) {
			msg = R.string.msg_required_fields_missing;
			fail = true;
		}
		else if(password.equals(confirm) == false) {
			msg = R.string.msg_confirm_password;
			fail = true;
		}
		
		if(fail) {
			showDialog(msg);
		}
		else {
			User u;
			try {
				// TODO: Don't do this on the UI thread.
				u = mSession.createAccount(username, password, this);
				startGamesActivity();
			} catch (APIException e) {
				showMessage(e.getMessage());
			}
		}
	}
	
	protected void startGamesActivity() {
		Intent i = new Intent(this, GamesActivity.class);
		i.putExtra(Constants.EXTRA_SESSION, mSession);
		startActivity(i);
		this.finish();
	}
	
	protected String getEmailField() {
		EditText emailField = (EditText) this.findViewById(R.id.login_input_username);
		return emailField.getText().toString();
	}
	
	protected String getPasswordField() {
		EditText passwordField = (EditText) this.findViewById(R.id.login_input_password);
		return passwordField.getText().toString();
	}
	
	protected String getConfirmField() {
		EditText confirmField = (EditText) this.findViewById(R.id.login_input_confirmpassword);
		return confirmField.getText().toString();
	}

	/** Constant to represent the ProgressDialog */
	public static final int
		DIALOG_RAW = -1,
		DIALOG_WAIT = 1;
	
	public static final String
		CONFIRM_STATE = "confirm-state";

}