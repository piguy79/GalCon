package com.xxx.galcon.social;

import com.xxx.galcon.http.AuthenticationListener;

public interface Authorizer {

	public void signIn(AuthenticationListener listener);

	public void getToken(AuthenticationListener listener);

}
