package com.xxx.galcon;

import com.badlogic.gdx.tools.imagepacker.TexturePacker2;

public class GalConTexturePacker {

	public static final void main(String[] args) {
		TexturePacker2.process("images/gameBoard", "../GalCon-Client/GalCon-Client-android/assets/data/images",
				"gameBoard");
		TexturePacker2.process("images/levels", "../GalCon-Client/GalCon-Client-android/assets/data/images", "levels");
		TexturePacker2.process("images/levelSelection", "../GalCon-Client/GalCon-Client-android/assets/data/images",
				"levelSelection");
		TexturePacker2.process("images/menus", "../GalCon-Client/GalCon-Client-android/assets/data/images", "menus");
		TexturePacker2
				.process("images/planets", "../GalCon-Client/GalCon-Client-android/assets/data/images", "planets");
		TexturePacker2.process("images/social", "../GalCon-Client/GalCon-Client-android/assets/data/images", "social");
	}
}
