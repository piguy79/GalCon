package com.railwaygames.solarsmash.screen;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.railwaygames.solarsmash.screen.event.GameStartEvent;
import com.railwaygames.solarsmash.screen.event.PracticeStartEvent;
import com.railwaygames.solarsmash.screen.event.SocialGameStartEvent;
import com.railwaygames.solarsmash.screen.widget.CommonTextButton;
import com.railwaygames.solarsmash.screen.widget.OKCancelDialog;

public class GameStartDialog extends OKCancelDialog {

	private float height;
	private float width;
	private int selectedMapKey;

	public GameStartDialog(Resources resources, float width, float height, Stage stage, int selectedMapKey) {
		super(resources, width, height, stage, Type.CANCEL);
		this.height = height;
		this.width = width;
		this.selectedMapKey = selectedMapKey;

		create();
	}

	private void create() {
		CommonTextButton practiceButton = new CommonTextButton(resources.skin, "Play a battle simulation", height * 0.27f,
				width * 0.8f, resources.fontShader);
		practiceButton.setX((getWidth() / 2) - (practiceButton.getWidth() / 2));
		practiceButton.setY(getHeight() * 0.65f);

		practiceButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				fire(new PracticeStartEvent(selectedMapKey));
			}
		});

		addActor(practiceButton);
		
		CommonTextButton randomPlay = new CommonTextButton(resources.skin, "Play a random opponent", height * 0.27f,
				width * 0.8f, resources.fontShader);
		randomPlay.setX((getWidth() / 2) - (randomPlay.getWidth() / 2));
		randomPlay.setY(getHeight() * 0.35f);

		randomPlay.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				fire(new GameStartEvent(selectedMapKey));
			}
		});

		addActor(randomPlay);

		CommonTextButton socialPlay = new CommonTextButton(resources.skin, "Play with a friend", height * 0.27f,
				width * 0.8f, resources.fontShader);
		socialPlay.setX((getWidth() / 2) - (socialPlay.getWidth() / 2));
		socialPlay.setY(getHeight() * 0.05f);

		socialPlay.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				fire(new SocialGameStartEvent(selectedMapKey));
			}
		});

		addActor(socialPlay);
	}
}
