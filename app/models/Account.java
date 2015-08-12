package models;

import fileauth.FileAuth;

public class Account {

	public final static Account NO_ACCOUNT = new Account("guest");
	public Long id;

	public String username;

	public Account(String username) {
		this.username = username;
	}

	public boolean isAdmin() {
		return FileAuth.contains("mediaDB", this.username);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(username);
		return sb.toString();
	}

	public static Account authenticate(String username, String password) {
		if (FileAuth.validate(username, password)) {
			Account account = new Account(username);
			return account.isAdmin() ? account : null;
		}
		return null;
	}
}
