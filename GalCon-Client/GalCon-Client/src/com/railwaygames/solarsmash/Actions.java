package com.railwaygames.solarsmash;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;

public class Actions {
	public static Action highlightButtonClick() {
		return sequence(color(Color.GREEN), color(Color.WHITE, 0.2f));
	}
}
