package edu.ua.cs.aces.adapters;

import edu.ua.cs.aces.R;
import edu.ua.cs.aces.response.Card;
import edu.ua.cs.aces.response.Game;
import edu.ua.cs.aces.response.Move;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class CardListAdapter extends ArrayAdapter<Card> {

	private boolean mBumpable = true;
	
	public CardListAdapter(Context ctx, Card cards[]) {
		super(ctx, R.layout.item_card, R.id.item_card_name, cards);
	}
	
	public void setBumpable(boolean b) {
		mBumpable = b;
	}

	public void toggleBump(int pos) {
		if(!mBumpable) return;
		Card c = getItem(pos);
		if (c == null)
			return;
		c.toggleBumped();
		notifyDataSetChanged();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);

		Card c = getItem(position);

		if (c != Card.EMPTY_CARD) {
			int bg = (c.isBumped()) ? R.drawable.card_selected
					: R.drawable.card_faceup;
			v.setBackgroundResource(bg);
		}

		return v;
	}

	public Card getItem(int pos) {
		Card c = super.getItem(pos);
		if (c == null)
			return Card.EMPTY_CARD;
		return c;
	}

	public long getItemId(int i) {
		Card c = getItem(i);
		if (c == null) {
			return -1;
		}
		return c.id;
	}
	
	public void lockMove(Move m) {
		if(m == null || !m.isValid()) return;
		
		setBumpable(false);
		for(int i = 0; i < this.getCount(); i++) {
			Card c = getItem(i);
			
			if(c.id == m.elementCardId || c.id == m.magicCardId) {
				c.setBumped(true);
			}
		}
		notifyDataSetChanged();
	}
	
	/**
	 * Shortcut for the lazy.
	 * @param username username of the one who moves
	 * @param game game object this move is for
	 * @return null if invalid move, or move object player specified
	 */
	public Move getMove(String username, Game game) {
		return getMove(username, game.gameId, game.nextMove);
	}

	/**
	 * Constructs an object representing a move specified by the player.
	 * 
	 * @return null if an invalid move is specified, or a move object
	 * the player has specified
	 */
	public Move getMove(String username, int gameId, int moveId) {
		int elementId = -1, magicId = -1;
		for(int i = 0; i < this.getCount(); i++) {
			Card c = getItem(i);
			
			if(c != null && c.isBumped()) {
				String type = (c.cardType == null) ? Card.TYPE_ELEMENT : c.cardType; 
				
				if(type.compareTo(Card.TYPE_ELEMENT) == 0 && elementId <= -1) {
					elementId = c.id;
				}
				else if(type.compareTo(Card.TYPE_MAGIC) == 0 && magicId <= -1) {
					magicId = c.id;
				}
				else if((type.compareTo(Card.TYPE_ELEMENT) == 0 && elementId >= 0) ||
						(type.compareTo(Card.TYPE_MAGIC) == 0 && magicId >= 0)) {
					return null;
				}
			}
		}
		
		if(elementId <= -1) {
			return null;
		}
		return new Move(elementId, magicId);
	}
}
