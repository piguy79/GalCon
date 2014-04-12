package com.railwaygames.solarsmash.screen.widget;

import java.util.Collection;
import java.util.Iterator;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.UISkin;

public class HighlightActorBar extends ActorBar {
	
	private Image highlightBar;
	private int actorToHighlight;
	private UISkin skin;

	public HighlightActorBar(float height, float width, int alignment,
			Collection<Actor> actors, float actorHeight, float actorWidth,
			float actorPadding, UISkin skin, int actorToHighlight) {
		super(height, width, alignment, actors, actorHeight, actorWidth, actorPadding);
		this.skin = skin;
		this.actorToHighlight = actorToHighlight;
		createHighlightBar();
	}
	
	
	private void createHighlightBar() {
		highlightBar = new Image(skin.getDrawable(Constants.UI.HIGHLIGHT_BAR));
		highlightBar.setWidth(actorWidth * 0.75f);
		highlightBar.setHeight(actorHeight * 0.075f);
		
		Actor lastActor = (Actor) actors.toArray()[actorToHighlight];
		
		final float actorOffset = actorWidth * 0.15f;
		highlightBar.setX(lastActor.getX() + actorOffset);
		highlightBar.setY(0);
		
		for(Iterator<Actor> iter = actors.iterator(); iter.hasNext();){
			final Actor actor = iter.next();
			actor.addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					highlightBar.addAction(Actions.moveTo(actor.getX() + actorOffset, highlightBar.getY(), 0.3f));
				}
			});
		}
		
		addActor(highlightBar);
	}


	public static class HighlightActorBarBuilder extends ActorBarBuilder {
		
		private UISkin skin;
		private int actorToHighlight;

		public HighlightActorBarBuilder(float height, float width, UISkin skin) {
			super(height, width);
			this.skin = skin;
		}
		
		@Override
		public HighlightActorBarBuilder actorSize(float height, float width) {
			super.actorSize(height, width);
			return this;
		}
		
		@Override
		public HighlightActorBarBuilder align(int alignment) {
			super.align(alignment);
			return this;
		}
		
		@Override
		public HighlightActorBarBuilder actorPadding(float padding) {
			super.actorPadding(padding);
			return this;
		}
		
		@Override
		public HighlightActorBarBuilder addActor(Actor actor) {
			super.addActor(actor);
			return this;
		}
		
		public HighlightActorBarBuilder actorToHighlight(int index){
			this.actorToHighlight = index;
			return this;
		}
		
		@Override
		public HighlightActorBar build(){
			return new HighlightActorBar(height, width, alignment, actors, actorHeight, actorWidth, actorPadding, skin, actorToHighlight);
		}
		
	}

}
