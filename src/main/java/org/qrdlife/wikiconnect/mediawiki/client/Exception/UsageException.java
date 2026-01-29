package org.qrdlife.wikiconnect.mediawiki.client.Exception;

/**
 * Exception thrown when the MediaWiki API returns an error or a "failed"
 * result.
 */
public class UsageException extends Exception {
	/** The error code returned by the API. */
	private final String code;
	/** The full result reference or command status. */
	private final String result;

	/**
	 * Constructs a new UsageException.
	 *
	 * @param code    The error code.
	 * @param message The error message or description.
	 * @param result  The result context or raw response snippet.
	 */
	public UsageException(String code, String message, String result) {
		super(message);
		this.code = code;
		this.result = result;
	}

	/**
	 * Gets the error code.
	 *
	 * @return The error code.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Gets the result details.
	 *
	 * @return The result details.
	 */
	public String getResult() {
		return result;
	}

	@Override
	public String getMessage() {
		return super.getMessage() + (code != null ? " [Code: " + code + "]" : "")
				+ (result != null ? " [Result: " + result + "]" : "");
	}
}
