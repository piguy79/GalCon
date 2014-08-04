package com.railwaygames.solarsmash.screen;

import static com.badlogic.gdx.math.Interpolation.pow3;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveByAction;
import com.badlogic.gdx.scenes.scene2d.actions.ParallelAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.UISkin;
import com.railwaygames.solarsmash.screen.widget.CommonCoinButton;

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

		float delay = delayInSeconds + durationInSeconds;
		runDelayAndRemoveActors(actors, runAtEnd, delay);
	}

	private static void runDelayAndRemoveActors(final Array<Actor> actors,
			final Runnable runAtEnd, float delay) {
		actors.get(0).addAction(sequence(delay(delay), run(new Runnable() {
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
	
	public static void fadeOut(final Array<Actor> actors, final Runnable runAtEnd, final float delay){
		if(actors.size == 0){
			return;
		}
		
		for(Actor actor : actors){
			actor.addAction(Actions.fadeOut(delay, pow3));
		}
		
		runDelayAndRemoveActors(actors, runAtEnd, delay);
	}

	public static final void setCommonButtonSize(Actor actor) {
		actor.setHeight(actionButtonSize);
		actor.setWidth(actionButtonSize);
	}
	
	public static ParallelAction arcMovement(float duration, float distanceUp, float distanceDown){
		MoveByAction moveAcross = Actions.moveBy(Gdx.graphics.getWidth() * 0.25f, 0, duration, Interpolation.linear);
		
		MoveByAction moveUp = Actions.moveBy(0, distanceUp, duration * 0.25f, Interpolation.circleOut);
		MoveByAction moveDown = Actions.moveBy(0, -distanceDown, duration * 0.75f, Interpolation.pow5);
		SequenceAction yMovements = Actions.sequence(moveUp, moveDown);
		
		return Actions.parallel(moveAcross, yMovements);
	}
	

}
