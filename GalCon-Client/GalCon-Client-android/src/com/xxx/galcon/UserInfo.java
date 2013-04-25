package com.xxx.galcon;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

public class UserInfo {

	private static String user;

	public synchronized static String getUser(Context context) {
		if (user == null) {
			AccountManager am = AccountManager.get(context);
			Account[] accounts = am.getAccountsByType("com.google");

			if (accounts == null || accounts.length == 0) {
				return "testUser";
			}

			user = accounts[0].name;
		}
		return user;
	}
}
