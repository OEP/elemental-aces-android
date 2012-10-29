package edu.ua.cs.aces.api;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.util.Log;

/**
 * A class meant to help out on some of the more verbose
 * stuff when dealing with low-level HTTP connections.
 * @author OEP
 *
 */
public class HttpHelper {
	public static final String TAG = HttpHelper.class.getSimpleName();
	
	/**
	 * Create an <code>HttpURLConnection</code> object and make it post
	 * its parameters to the Worddit server.
	 * @param baseUrl The base URL of the Worddit server.
	 * @param path The path to make the POST to
	 * @param params URL-encoded parameters
	 * @param sessionId to send to the server
	 * @return an <code>HttpURLConnection</code> for the connection
	 * @throws IOException
	 */
	public static HttpURLConnection makePost(URL baseUrl, String path, String params, String sessionId)
	throws IOException {
		URL url = new URL(String.format("%s%s", baseUrl.toString(), path));
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		
		// Set cookie if it was given
		if(sessionId != null) {
			connection.setRequestProperty(APIConstants.AUTH_HEADER_NAME, sessionId);
		}
		
		connection.setRequestProperty("Content-Length", Integer.toString(params.getBytes().length));
		connection.setRequestProperty("Content-Language", "en-US");
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setDoOutput(true);
		
		
		// Send the request
		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		wr.writeBytes(params);
		wr.flush();
		wr.close();
		return connection;
	}
	
	/**
	 * Construct and return an <code>HttpURLConnection</code> object
	 * to use for a GET connection.
	 * @param baseUrl The base URL of the server
	 * @param path The path to make the GET call to.
	 * @param sessionId The cookie to send to the server (null if no cookie)
	 * @return an <code>HttpURLConnection</code> object representing the established connection
	 * @throws IOException if there was trouble establishing the connection
	 */
	public static HttpURLConnection makeGet(URL baseUrl, String path, String sessionId)
	throws IOException {
		URL url = new URL(String.format("%s%s", baseUrl.toString(), path));
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Content-Language", "en-US");
		
		Log.i("HttpHelper", "Header name " + sessionId);
		
		// Set cookie if it was given
		if(sessionId != null) {
			connection.setRequestProperty(APIConstants.AUTH_HEADER_NAME, sessionId);
		}
		
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setDoOutput(false);
		
		return connection;
	}
	
	/**
	 * Take an arbitrary list of arguments and return a URL-encoded
	 * String representation of the arguments.
	 * 		Note: This takes even-numbered lists of arguments
	 * @param args An alternating list of key/value payloads to URL-encode
	 * @return String representation of the key/value pairs, URL-encoded
	 * @throws UnsupportedEncodingException if "UTF-8" is not permissible
	 */
	public static String encodeParams(String ... args)
	throws UnsupportedEncodingException {
		if(args.length % 2 != 0) {
			throw new IllegalArgumentException("Must have a multiple of two arguments");
		}
		
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < args.length; i+=2) {
			buffer.append(URLEncoder.encode(args[i], "UTF-8"));
			buffer.append('=');
			buffer.append(URLEncoder.encode(args[i+1], "UTF-8"));
			buffer.append('&');
		}
		// Get rid of trailing '&'
		buffer.deleteCharAt(buffer.length() - 1);
		return buffer.toString();
	}
	
	/**
	 * Attempt to read the cookie provided by the Worddit server.
	 * This method fails silently if no cookies are provided.
	 * @param connection An HTTP connection to read the cookie from.
	 * @param name The name of the cookie to search for
	 * @return the contents of the cookie
	 */
	public static String readHeader(HttpURLConnection connection, String name) {
		Map<String,List<String>> headers = connection.getHeaderFields();
		List<String> values = null;
		
		for(int i = 1; i <= headers.size(); i++) {
			String headerName = connection.getHeaderFieldKey(i);
			if(headerName.equalsIgnoreCase(name)) {
				values = headers.get(headerName);
			}
		}
		
		if(values == null) {
			Log.w(TAG, String.format("Header '%s' not found.", name));
			return null;
		}
		
		StringBuffer buf = new StringBuffer();
		for(Iterator<String> iter = values.iterator(); iter.hasNext(); ) {
			String v = iter.next();
			buf.append(v);
		}
		
		String value = buf.toString();
		return value;
	}
}
