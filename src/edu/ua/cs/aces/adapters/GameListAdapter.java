package edu.ua.cs.aces.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.ua.cs.aces.Constants;
import edu.ua.cs.aces.R;
import edu.ua.cs.aces.api.APIException;
import edu.ua.cs.aces.api.Session;
import edu.ua.cs.aces.response.Game;

public class GameListAdapter extends SessionListAdapter {
	protected Game[] mGames;
	@SuppressWarnings("unused")
	private int mStatusField, mNextPlayerField, mLastMoveField; 
	
	public GameListAdapter(Context ctx, Session session) {
		this(ctx,session,0,R.id.item_game_nextup,R.id.item_game_lastplay);
	}
	
	public GameListAdapter(Context ctx, Session session,
			int statusField, int nextPlayerField, int lastMoveField) {
		super(ctx, session);
		mStatusField = statusField;
		mNextPlayerField = nextPlayerField;
		mLastMoveField = lastMoveField;
	}
	
	public int getItemCount() {
		return (mGames == null) ? 0 : mGames.length;
	}

	public Game getItem(int n) {
		return mGames[n];
	}

	public long getItemId(int n) {
		if(isFetching()) {
			if(n == 0) return 1;
			return 0;
		}
		else if(mGames == null) {
			return 0;
		}
		
		Game g = mGames[n];
		return g.gameId; 
	}
	
	protected View getLoadingView() {
		return mInflater.inflate(R.layout.item_loadingitem, null);
	}
	
	protected void fetchData() {
		try {
			mGames = mSession.getGames();
			onDataFetched(true);
		} catch (APIException e) {
			Constants.showToast(mContext, e);
			onDataFetched(false);
		}
	}
	
	@Override
	protected void onFetchComplete(boolean result) {
		if(!result) {
			// TODO: Some sort of error message.
		}
	}
	
	@Override
	protected View getItemLoadingView(int position, View convertView, ViewGroup parent) {
		return convertView;
	}

	@Override
	protected View getItemView(int position, View convertView, ViewGroup parent) {
		View gameItem;

		// TODO: Case where mGames == null or mGames.length == 0 ?
		
		// Replace if convertView is null or it's still using the loading view
		if(convertView == null) {
			gameItem = mInflater.inflate(R.layout.item_gameitem, null);
		} else {
			gameItem = convertView;
		}
		
		Game gameForView = mGames[position];
		TextView main = (TextView) gameItem.findViewById(mNextPlayerField);
		TextView subtext = (TextView) gameItem.findViewById(mLastMoveField);
		
		Resources r = mContext.getResources();
		main.setText(String.format(r.getString(R.string.label_opponent),
				gameForView.opponent.playerName));
		
		// Label that goes under the main thing. Display the score.
		String subLabel = mContext.getString(R.string.label_score);
		subtext.setText(String.format(subLabel, gameForView.score));
		
		return gameItem;
	}



}
