package edu.ua.cs.aces.response;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
	public String username;
	public String authKey;
	
	/**
	 * Construct a Profile from a Parcel.
	 * @param in the parcel to construct from
	 */
	private User(Parcel in) {
		username = in.readString();
		authKey = in.readString();
	}

	public User(String username, String authKey) {
		this.username = username;
		this.authKey = authKey;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(username);
		dest.writeString(authKey);
	}

	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
		@Override
		public User createFromParcel(Parcel source) {
			return new User(source);
		}

		@Override
		public User[] newArray(int size) {
			return new User[size];
		}
	};
}
