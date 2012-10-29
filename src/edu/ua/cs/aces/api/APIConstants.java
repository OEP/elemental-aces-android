package edu.ua.cs.aces.api;

/**
 * A class which defines values as constants as defined
 * by the Worddit API.
 * @author OEP
 *
 */
public class APIConstants {
	/** Constants for response codes returned by the Worddit server. */
	public static final int
		SUCCESS = 200,
		SUCCESS_CREATED = 201,
		SUCCESS_ACCEPTED = 202,
		ERROR_BAD_REQUEST = 400,
		ERROR_FORBIDDEN = 403,
		ERROR_NOT_FOUND = 404,
		ERROR_CONFLICT = 409;
	
	/** Constant keywords for payloads (arguments) required by the Worddit server */
	public static final String
		C2DM_ASSOCIATED = "regId_associated",
		USERNAME = "username",
		EMAIL = "email",
		PASSWORD = "password",
		CLIENT_TYPE = "client_type",
		NEW_PASSWORD = "newpassword",
		NICKNAME = "nickname",
		AVATAR = "avatar",
		IMAGE = "image",
		ID = "id",
		STATUS = "status",
		CURRENT_PLAYER = "current_player",
		PLAYERS = "players",
		LAST_MOVE = "last_move_utc",
		INVITATIONS = "invitations",
		RULES = "rules",
		REQUESTED_PLAYERS = "requested_players",
		ROW = "row",
		COLUMN = "column",
		DIRECTION = "direction",
		TILES = "tiles",
		MESSAGE = "message",
		DEVICE_ID = "device_id",
		C2DM_REGID = "regId",
		AUTHKEY = "auth_key",
		ELEMENT_CARD_ID = "elementCardId",
		MAGIC_CARD_ID = "magicCardId",
		AUTH_HEADER_NAME = "x-elementalaces-session";
	
	/** Constant values as defined by the server API */
	public static final String
		DOWN = "down",
		RIGHT = "right";
	
	/** Constant paths defined by the server API */
	public static final String
		PATH_USER_ADD = "/api/register",
		PATH_MOVE = "/api/game/%d/move/%d",
		PATH_ASSOCIATE_C2DM = "/api/c2dm/associate",
		PATH_USER_LOGIN = "/api/login",
		PATH_GET_GAMES = "/api/game_list",
		PATH_GET_GAME = "/api/game/%d",
		PATH_GAME_HAND = "/api/game/%d/hand",
		PATH_CHALLENGE = "/api/challenge";
}
