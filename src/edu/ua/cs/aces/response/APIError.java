package edu.ua.cs.aces.response;

public class APIError {
	public String message;
	public String type;
	
	public boolean isAuthError() {
		return type != null && type.equals(AUTH_ERROR);
	}
	
	public static final String
		AUTH_ERROR = "AuthError";
		
}
