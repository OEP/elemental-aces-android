package edu.ua.cs.aces.response;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A class to represent a game that a player can join.
 * @author OEP
 *
 */
public class Game implements Parcelable {
	public int playerId;
	public String playerName;
	public int gameId;
	public int score;
	public int nextMove = 0;
	public int inHand;
	public int inDeck;
	public int inDiscard;
	
	public Game opponent;
	
	public Move playerCurrentMove;
	public Move playerPreviousMove;
	public Move opponentPreviousMove;
	
	/**
	 * Default constructor
	 */
	public Game() {
		
	}
	
	/**
	 * Construct a Game from a Parcel.
	 * @param in the parcel to construct from
	 */
	private Game(Parcel in) {
		playerId = in.readInt();
		playerName = in.readString();
		gameId = in.readInt();
		score = in.readInt();
		nextMove = in.readInt();
		inHand = in.readInt();
		inDeck = in.readInt();
		inDiscard = in.readInt();
		opponent = safeReadParcelable(in, Game.class.getClassLoader());
		playerCurrentMove = safeReadParcelable(in, Move.class.getClassLoader());
		playerPreviousMove = safeReadParcelable(in, Move.class.getClassLoader());
		opponentPreviousMove = safeReadParcelable(in, Move.class.getClassLoader());
	}
	

	public boolean playerHasMove() {
		return playerCurrentMove != null && !playerCurrentMove.isValid();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(playerId);
		dest.writeString(playerName);
		dest.writeInt(gameId);
		dest.writeInt(score);
		dest.writeInt(nextMove);
		dest.writeInt(inHand);
		dest.writeInt(inDeck);
		dest.writeInt(inDiscard);
		safeWriteParcelable(dest, opponent, flags);
		safeWriteParcelable(dest, playerCurrentMove, flags);
		safeWriteParcelable(dest, playerPreviousMove, flags);
		safeWriteParcelable(dest, opponentPreviousMove, flags);
	}
	
	private void safeWriteParcelable(Parcel dest, Parcelable p, int flags) {
		if(p != null) {
			dest.writeInt(1);
			dest.writeParcelable(p, flags);
		}
		else {
			dest.writeInt(0);
		}
	}
	
	private <T> T safeReadParcelable(Parcel in,  ClassLoader cl) {
		int val = in.readInt();
		if(val == 1) {
			return in.readParcelable(cl);
		}
		else {
			return null;
		}
	}
	
	public static final Parcelable.Creator<Game> CREATOR
	= new Parcelable.Creator<Game>() {
		@Override
		public Game createFromParcel(Parcel source) {
			return new Game(source);
		}

		@Override
		public Game[] newArray(int size) {
			return new Game[size];
		}
	};
	
	public String toString() {
		return String.format("Game (player=%s, opp=%s, gameId=%d, score=%d, nextMove=%d)",
				playerName, opponent.playerName, gameId, score, nextMove);
	}
	
}
