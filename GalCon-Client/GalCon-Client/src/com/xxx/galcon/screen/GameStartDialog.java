package com.xxx.galcon.screen;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.xxx.galcon.screen.event.GameStartEvent;
import com.xxx.galcon.screen.event.SocialGameStartEvent;
import com.xxx.galcon.screen.widget.CommonTextButton;
import com.xxx.galcon.screen.widget.OKCancelDialog;

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
		CommonTextButton randomPlay = new CommonTextButton(resources.skin, "Play a random opponent", height * 0.3f,
				width * 0.8f, resources.fontShader);
		randomPlay.setX((getWidth() / 2) - (randomPlay.getWidth() / 2));
		randomPlay.setY(getHeight() * 0.5f);

		randomPlay.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				fire(new GameStartEvent(selectedMapKey));
			}
		});

		addActor(randomPlay);

		CommonTextButton socialPlay = new CommonTextButton(resources.skin, "Play with a friend", height * 0.3f,
				width * 0.8f, resources.fontShader);
		socialPlay.setX((getWidth() / 2) - (socialPlay.getWidth() / 2));
		socialPlay.setY(getHeight() * 0.15f);

		socialPlay.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				fire(new SocialGameStartEvent(selectedMapKey));
			}
		});

		addActor(socialPlay);

	}

}