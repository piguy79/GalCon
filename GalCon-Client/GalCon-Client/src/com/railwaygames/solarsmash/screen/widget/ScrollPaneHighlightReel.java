package com.railwaygames.solarsmash.screen.widget;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sizeTo;

import java.util.LinkedHashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ScrollPaneHighlightReel extends ActorBar {

	private static final Integer PREVIOUS = -1000;

	private Map<Integer, Actor> actorsByKey;
	private Integer previousHighlight = PREVIOUS;

	public ScrollPaneHighlightReel(float height, float width, int alignment, Map<Integer, Actor> actorsByKey,
			float buttonHeight, float buttonWidth, float actorPadding) {
		super(height, width, alignment, actorsByKey.values(), buttonHeight, buttonWidth, actorPadding);
		this.actorsByKey = actorsByKey;
	}

	public void highlight(int key) {
		final Actor actor = actorsByKey.get(key);

		if (actor == null) {
			throw new IllegalArgumentException("Button with a key of " + key + " does not exist");
		}

		if (previousHighlight > PREVIOUS && previousHighlight != key) {
			final Actor previousScale = actorsByKey.get(previousHighlight);
			previousScale.addAction(parallel(sizeTo(actorWidth, actorHeight, 0.3f), color(Color.GRAY)));
		}

		if (previousHighlight != key) {
			actor.addAction(parallel(sizeTo(actorWidth * 1.3f, actorHeight * 1.3f, 0.3f), color(Color.WHITE)));
			previousHighlight = key;
		}
	}

	public static class ScrollPaneHighlightReelBuilder extends ActorBarBuilder {

		private LinkedHashMap<Integer, Actor> actorsByKey;

		public ScrollPaneHighlightReelBuilder(float height, float width) {
			super(height, width);
			actorsByKey = new LinkedHashMap<Integer, Actor>();
		}

		public ScrollPaneHighlightReelBuilder addActorWithKey(Integer key, Actor actor) {
			actorsByKey.put(key, actor);
			return this;
		}

		@Override
		public ScrollPaneHighlightReelBuilder actorSize(float height, float width) {
			super.actorSize(height, width);
			return this;
		}

		@Override
		public ScrollPaneHighlightReelBuilder align(int alignment) {
			super.align(alignment);
			return this;
		}

		@Override
		public ScrollPaneHighlightReelBuilder actorPadding(float padding) {
			super.actorPadding(padding);
			return this;
		}

		@Override
		public ScrollPaneHighlightReel build() {
			return new ScrollPaneHighlightReel(height, width, alignment, actorsByKey, actorHeight, actorWidth,
					actorPadding);
		}
	}
}
