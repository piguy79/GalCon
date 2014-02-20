package com.xxx.galcon.screen.event;

import com.badlogic.gdx.scenes.scene2d.Event;

public class SliderUpdateEvent extends Event {

	public int value;

	public SliderUpdateEvent(int value) {
		this.value = value;
	}
}
