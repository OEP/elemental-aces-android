package edu.ua.cs.aces;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import edu.ua.cs.aces.adapters.CardListAdapter;
import edu.ua.cs.aces.api.APIConstants;
import edu.ua.cs.aces.api.APIException;
import edu.ua.cs.aces.api.Session;
import edu.ua.cs.aces.response.Card;
import edu.ua.cs.aces.response.Game;
import edu.ua.cs.aces.response.Move;
import edu.ua.cs.aces.response.User;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ListView;
import android.widget.TextView;

public class GameActivity extends Activity {
	
	public static final String TAG = GameActivity.class.getName();

	private Session mSession;
	
	private Game mGame;
	
	private Card mPlayerHand[];

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_game);
		setup();
	}

	private void setup() {
		Intent i = getIntent();
		mGame = i.getParcelableExtra(Constants.EXTRA_GAME);
		mSession = i.getParcelableExtra(Constants.EXTRA_SESSION);
		try {
			mSession = Session.makeSession(this);
		} catch (MalformedURLException e1) {
		}
		
		int gameId = i.getIntExtra(Constants.EXTRA_GAMEID, -1);

		if(mSession == null || (mGame == null && gameId == -1)) {
			// Somebody called this activity without knowing what they're doing.
			// We'll log the error and finish so nobody gets hurt.
			Log.e(TAG, "GameActivity launched with no Session or game reference (gameId or Game object.");
			this.finish();
			return;
		}
		
		if(mGame == null) {
			try {
				mGame = mSession.getGame(gameId);
			} catch (APIException e) {
				Constants.handleException(mSession, e, this);
				Log.e(TAG, "Can't fetch game. Impossible to use GameActivity.");
				this.finish();
				return;
			}
		}
		updateModel();
		updateView();
	}
	
	private void updateModel() {
		try {
			mPlayerHand = mSession.getHand(mGame.gameId);
		} catch (APIException e) {
			boolean ending = Constants.handleException(mSession, e, this);
			if(ending) return;
			
			mPlayerHand = new Card[mGame.inHand];
			Constants.showToast(this, e);
		}
	}
	
	private void updateView() {

		final Gallery ph = (Gallery) findViewById(R.id.game_player_cards);
		final Gallery oh = (Gallery) findViewById(R.id.game_opponent_cards);
		ph.setAdapter(new CardListAdapter(this, mPlayerHand));
		
		oh.setAdapter(new CardListAdapter(this, new Card[mGame.opponent.inHand]));
		
		ph.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
				CardListAdapter adapter = (CardListAdapter) ph.getAdapter();
				adapter.toggleBump(pos);
			}
		});
		
		final Button throwButton = (Button) findViewById(R.id.game_throw);
		
		CardListAdapter adapter = (CardListAdapter) ph.getAdapter();
		adapter.lockMove(mGame.playerCurrentMove);
		
		throwButton.setVisibility(!mGame.playerHasMove() ? View.VISIBLE : View.GONE);
		
		throwButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Gallery l = (Gallery) findViewById(R.id.game_player_cards);
				CardListAdapter adapter = (CardListAdapter) l.getAdapter();
				User u = mSession.getUserProfile();
				Move m = adapter.getMove(u.username, mGame);
				
				if(m == null) {
					Constants.showToast(GameActivity.this, R.string.msg_invalid_move);
					return;
				}
				
				// Move is acceptable!!!
				adapter.lockMove(m);
				throwButton.setVisibility(View.GONE);
				try {
					Game g = mSession.move(mGame.gameId, mGame.nextMove, m.elementCardId, m.magicCardId);
					mGame = g;
					updateModel();
					updateView();
				} catch (APIException e) {
					Constants.handleException(mSession, e, GameActivity.this);
					
					// If no AuthError, make the model reflect the move and update the view.
					if(!GameActivity.this.isFinishing()) {
						mGame.playerCurrentMove = m;
						updateView();
					}
				}
				
			}
		});
		
		// SOME DEBUG INFO
		TextView top = (TextView) findViewById(R.id.game_opponent_score);
		TextView bot = (TextView) findViewById(R.id.game_player_score);
		
		top.setText(mGame.toString());
		bot.setText("Has move: " + mGame.playerHasMove());
	}
}
