package edu.ua.cs.aces.response;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Scanner;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * A class to represent a move in the game's history.
 * @author OEP
 *
 */
public class Move implements Parcelable {
	public int elementCardId = -1;
	public int magicCardId = -1;
	
	public Move(int elementId) {
		this(elementId, -1);
	}
	
	public Move(int elementId, int magicId) {
		this.elementCardId = elementId;
		this.magicCardId = magicId;
	}
	
	private Move(Parcel in) {
		this.elementCardId = in.readInt();
		this.magicCardId = in.readInt();
	}
	
	public boolean isValid() {
		return elementCardId > 0 || magicCardId > 0;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeInt(this.elementCardId);
		parcel.writeInt(this.magicCardId);
	}
	
	public static final Parcelable.Creator<Move> CREATOR
	= new Parcelable.Creator<Move>() {
		@Override
		public Move createFromParcel(Parcel source) {
			return new Move(source);
		}

		@Override
		public Move[] newArray(int size) {
			return new Move[size];
		}
	};
	
}
