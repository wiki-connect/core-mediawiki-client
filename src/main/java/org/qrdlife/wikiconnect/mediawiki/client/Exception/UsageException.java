package org.qrdlife.wikiconnect.mediawiki.client.Exception;

public class UsageException extends Exception {
	private final String code;
	private final String result;

	public UsageException(String code, String message, String result) {
		super(message);
		this.code = code;
		this.result = result;
	}

	public String getCode() {
		return code;
	}

	public String getResult() {
		return result;
	}

	@Override
	public String getMessage() {
		return super.getMessage() + (code != null ? " [Code: " + code + "]" : "")
				+ (result != null ? " [Result: " + result + "]" : "");
	}
}

