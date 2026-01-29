package org.qrdlife.wikiconnect.mediawiki.client.Auth;

interface Auth {
    boolean login() throws Exception;
    void logout() throws Exception;
    boolean isLoggedIn() throws Exception;
    String getRUsername() throws Exception;
    String getUsername();
}