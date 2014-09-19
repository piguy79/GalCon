package com.railwaygames.solarsmash.screen.overlay;

import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.railwaygames.solarsmash.screen.Resources;

public class ProgressTextOverlay extends TextOverlay {

	private Timer timer;
	private String end = "";

	public ProgressTextOverlay(String text, Resources resources) {
		super(text, resources);

		timer = new Timer();
		timer.scheduleTask(new Task() {
			@Override
			public void run() {
				String text = getOriginalText();
				if (end.length() < 3) {
					end += ".";
				} else {
					end = "";
				}
				getLabel().setText(text + end);
			}
		}, 0.33f, 0.33f);
	}

	@Override
	public boolean remove() {
		if (timer != null) {
			timer.clear();
			timer = null;
		}
		return super.remove();
	}
}
