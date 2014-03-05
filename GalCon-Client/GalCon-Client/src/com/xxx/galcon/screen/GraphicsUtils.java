package com.xxx.galcon.screen;

import static com.badlogic.gdx.math.Interpolation.pow3;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

public class GraphicsUtils {

	public static float actionButtonSize = (Gdx.graphics.getHeight() * (0.1f * 0.88f));

	public static void hideAnimated(final Array<Actor> actors, boolean back, final Runnable runAtEnd) {
		if (actors.size == 0) {
			return;
		}

		int modifier = -1;
		if (back) {
			modifier = 1;
		}

		float delayInSeconds = 0.0f;
		float durationInSeconds = 0.9f;

		for (int i = 0; i < actors.size; ++i) {
			Actor actor = actors.get(i);
			if (actor != null) {
				actor.addAction(sequence(delay(delayInSeconds),
						moveTo(modifier * Gdx.graphics.getWidth(), actor.getY(), durationInSeconds, pow3)));
			}
		}

		actors.get(0).addAction(sequence(delay(delayInSeconds + durationInSeconds), run(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < actors.size; ++i) {
					Actor actor = actors.get(i);
					actor.remove();
				}
				runAtEnd.run();
			}
		})));
	}

	public static final void setCommonButtonSize(Actor actor) {
		actor.setHeight(actionButtonSize);
		actor.setWidth(actionButtonSize);
	}
}
