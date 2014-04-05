package com.railwaygames.solarsmash.screen.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Button;

public class ActorBar extends Group {
	
	protected Collection<Actor> actors;
	protected float width;
	protected float height;
	protected float alignment;
	protected float actorWidth;
	protected float actorHeight;
	protected float actorPadding;
	
	
	public ActorBar(float height, float width, int alignment, Collection<Actor> actors, float actorHeight, float actorWidth, float actorPadding){
		this.height = height;
		this.width = width;
		this.alignment = alignment;
		this.actors = actors;
		this.actorHeight = actorHeight;
		this.actorWidth = actorWidth;
		this.actorPadding = actorPadding;
		
		setupButtonBar();
	}
	
	private void setupButtonBar() {
		setHeight(height);
		setWidth(width);
		
		
		float previousActorStart = findActorStartingPoint();
		for(Actor actor : actors){
			actor.setSize(actorWidth, actorHeight);
			actor.setX(previousActorStart);
			addActor(actor);
			
			if(alignment == Align.RIGHT){
				previousActorStart = previousActorStart - (actorWidth + actorPadding);
			}else{
				previousActorStart = previousActorStart + (actorWidth + actorPadding);
			}
			
		}
		
	}

	private float findActorStartingPoint() {
		if(alignment == Align.RIGHT){
			return getWidth() - (actorWidth + actorPadding);
		}else if (alignment == Align.LEFT){
			return 0;
		}
		else{
			throw new IllegalArgumentException("ButtonBar does not support the alignment: " + alignment);
		}
	}

	public static class Align{
		public static final int RIGHT = 0;
		public static final int LEFT = 1;
	}
	
	
	public static class ActorBarBuilder {
		protected float width;
		protected float height;
		protected int alignment;
		protected Collection<Actor> actors;
		protected float actorWidth;
		protected float actorHeight;
		protected float actorPadding;

		
		public ActorBarBuilder(float height, float width){
			this.width = width;
			this.height = height;
			this.actors = new ArrayList<Actor>();
		}
		
		public ActorBarBuilder addActor(Actor actor){
			actors.add(actor);
			return this;
		}
		
		public ActorBarBuilder align(int alignment){
			this.alignment = alignment;
			return this;
		}
		
		public ActorBarBuilder actorSize(float height, float width){
			this.actorHeight = height;
			this.actorWidth = width;
			return this;
		}
		
		public ActorBarBuilder actorPadding(float padding){
			this.actorPadding = padding;
			return this;
		}
		
		
		
		public ActorBar build(){
			return new ActorBar(height, width, alignment, actors, actorHeight, actorWidth, actorPadding);
		}
	}

}
