package com.example.scyalladb.utils.messages;


/**
 * The interface Error messages.
 */
public interface ErrorMessage {
    /**
     * The constant REQUEST_PARSING_FAILED.
     */
    String UNSUPPORTED_EXCEPTION = "Unsupported error !";
    /**
     * The constant RESPONSE_PARSING_FAILED.
     */
    String IO_EXCEPTION = "IO Error !";
    /**
     * The constant TOKEN_NOT_FOUND.
     */
    String TOKEN_NOT_FOUND = "Token not found!";
    /**
     * The constant AUTHENTICATED_USER_NOT_FOUND.
     */
    String AUTHENTICATED_USER_NOT_FOUND = "Email / Password is invalid";
    /**
     * The constant SESSION_NOT_FOUND.
     */
    String SESSION_NOT_FOUND = "Something Happened. Please login again to continue";
    /**
     * The constant NOT_RESPONDING.
     */
    String NOT_RESPONDING = "Server not responding  !";
    /**
     * The constant VALUE_NOT_FOUND_IN_SESSION.
     */
    String VALUE_NOT_FOUND_IN_SESSION = "Value not found in session!";
}