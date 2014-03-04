package com.xxx.galcon.screen.widget;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public class ScrollPaneHighlightReel extends ActorBar {
	
	private Map<Integer, Actor> actorsByKey;
	private Integer previousHightLight = -1;

	public ScrollPaneHighlightReel(float height, float width, int alignment,
			Map<Integer, Actor> actorsByKey, float buttonHeight, float buttonWidth, float actorPadding) {
		super(height, width, alignment, actorsByKey.values(), buttonHeight, buttonWidth, actorPadding);
		this.actorsByKey = actorsByKey;
	}
	
	
	public void highlight(int key) {
		final Actor actor = actorsByKey.get(key);
		
		if(actor == null){
			throw new IllegalArgumentException("Button with a key of " + key + " does not exist");
		}
		
		if(previousHightLight >= 0 && previousHightLight != key){
			final Actor previousScale = actorsByKey.get(previousHightLight);
			previousScale.addAction(Actions.parallel(Actions.sizeTo(actorWidth, actorHeight, 0.3f),
					Actions.color(Color.GRAY)));
		}
		
		if(previousHightLight != key){
			actor.addAction(Actions.parallel(Actions.sizeTo(actorWidth * 1.3f, actorHeight * 1.3f, 0.3f)
					, Actions.color(Color.WHITE)));
			previousHightLight = key;
		}
		
		
		
	}
	
	public static class ScrollPaneHighlightReelBuilder extends ActorBarBuilder {
		
		private Map<Integer, Actor> actorsByKey;

		public ScrollPaneHighlightReelBuilder(float height, float width) {
			super(height, width);
			actorsByKey = new HashMap<Integer, Actor>();
		}
		
		public ScrollPaneHighlightReelBuilder addActorWithKey(Integer key, Actor actor){
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
		public ScrollPaneHighlightReel build(){
			return new ScrollPaneHighlightReel(height, width, alignment, actorsByKey, actorHeight, actorWidth, actorPadding);
		}
		
	}

	
	
}
