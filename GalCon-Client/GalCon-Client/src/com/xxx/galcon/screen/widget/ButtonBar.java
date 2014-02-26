package com.xxx.galcon.screen.widget;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Button;

public class ButtonBar extends Group {
	
	private List<Button> buttons;
	private float width;
	private float height;
	private float alignment;
	private float buttonWidth;
	private float buttonHeight;
	private float buttonPadding;
	
	
	public ButtonBar(float height, float width, int alignment, List<Button> buttons, float buttonHeight, float buttonWidth){
		this.height = height;
		this.width = width;
		this.alignment = alignment;
		this.buttons = buttons;
		this.buttonHeight = buttonHeight;
		this.buttonWidth = buttonWidth;
		this.buttonPadding = buttonWidth * 1.1f;
		
		setupButtonBar();
	}
	
	private void setupButtonBar() {
		setHeight(height);
		setWidth(width);
		
		
		float previousButtonStart = findButtonStartingPoint();
		for(Button button : buttons){
			button.setSize(buttonWidth, buttonHeight);
			button.setX(previousButtonStart);
			addActor(button);
			
			if(alignment == Align.RIGHT){
				previousButtonStart = previousButtonStart - buttonPadding;
			}else{
				previousButtonStart = previousButtonStart + buttonPadding;
			}
			
		}
		
	}

	private float findButtonStartingPoint() {
		if(alignment == Align.RIGHT){
			return getWidth() - buttonPadding;
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
	
	
	public static class ButtonBarBuilder {
		private float width;
		private float height;
		private int alignment;
		private List<Button> buttons;
		private float buttonWidth;
		private float buttonHeight;

		
		public ButtonBarBuilder(float height, float width){
			this.width = width;
			this.height = height;
			this.buttons = new ArrayList<Button>();
		}
		
		public ButtonBarBuilder addButton(Button button){
			buttons.add(button);
			return this;
		}
		
		public ButtonBarBuilder align(int alignment){
			this.alignment = alignment;
			return this;
		}
		
		public ButtonBarBuilder buttonSize(float height, float width){
			this.buttonHeight = height;
			this.buttonWidth = width;
			return this;
		}
		
		
		
		public ButtonBar build(){
			return new ButtonBar(height, width, alignment, buttons, buttonHeight, buttonWidth);
		}
	}

}
