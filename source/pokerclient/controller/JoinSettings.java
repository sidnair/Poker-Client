package pokerclient.controller;

import java.io.Serializable;

public class JoinSettings implements Serializable {
	
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = 8346477677754029096L;
	public String name;
	public String avatarPath;
	
	public JoinSettings(String name, String avatarPath) {
		this.name = name;
		this.avatarPath = avatarPath;
	}

	public String getName() {
		return name;
	}
	
	public String getAvatarPath() {
		return avatarPath;
	}
	
}
