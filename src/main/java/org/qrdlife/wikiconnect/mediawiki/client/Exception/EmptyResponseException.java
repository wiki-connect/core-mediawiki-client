package org.qrdlife.wikiconnect.mediawiki.client.Exception;

/**
 * Exception thrown when the API returns an empty or null response.
 */
public class EmptyResponseException extends Exception {

	/**
	 * Constructs a new EmptyResponseException with the specified detail message.
	 *
	 * @param message The detail message.
	 */
	public EmptyResponseException(String message) {
		super(message);
	}
}
