package com.gamb1t.legacyforge;

public interface AuthenticationListener {
    void onLoginSuccess();
    void onLoginFailure(String errorMessage);
}
