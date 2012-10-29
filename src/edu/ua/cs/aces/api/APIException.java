package edu.ua.cs.aces.api;

import edu.ua.cs.aces.response.APIError;

public class APIException extends Exception {
	private static final long serialVersionUID = -2380983332176115409L;
	
	private APIError mError;
	
	private String mMessage;
	
	public APIException(APIError err) {
		super(err.message);
		mError = err;
	}
	
	public APIException(String msg) {
		super(msg);
		mMessage = msg;
	}
	
	public APIError getError() {
		return mError;
	}

	public String getMessage() {
		return (mError != null) ? mError.message : mMessage;
	}
}
