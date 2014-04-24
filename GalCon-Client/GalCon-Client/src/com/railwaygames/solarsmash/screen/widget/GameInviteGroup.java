package com.railwaygames.solarsmash.screen.widget;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.UIConnectionWrapper;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.BaseResult;
import com.railwaygames.solarsmash.model.GameBoard;
import com.railwaygames.solarsmash.model.GameQueueItem;
import com.railwaygames.solarsmash.model.Map;
import com.railwaygames.solarsmash.model.Point;
import com.railwaygames.solarsmash.model.Size;
import com.railwaygames.solarsmash.screen.GraphicsUtils;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.event.AcceptInviteEvent;
import com.railwaygames.solarsmash.screen.event.DeclineInviteEvent;
import com.railwaygames.solarsmash.screen.overlay.LoadingOverlay;
import com.railwaygames.solarsmash.screen.overlay.Overlay;

public class GameInviteGroup extends Group {

	private Resources resources;
	private float width;
	private float height;
	private GameQueueItem item;
	private String mapTitle;

	public GameInviteGroup(Resources resources, GameQueueItem item, Size size, Map map) {
		this.resources = resources;
		this.width = size.width;
		this.height = size.height;
		this.item = item;

		if (map == null) {
			mapTitle = "Application update required to play this map";
		} else {
			mapTitle = map.title;
		}

		createVsLabel();
		createPlayerLabel();
		createLevelLabel();
		createDeclineButton();

		if (map != null) {
			createAcceptButton();
		}
	}

	private void createVsLabel() {
		ShaderLabel vsLabel = new ShaderLabel(resources.fontShader, "vs ", resources.skin, Constants.UI.DEFAULT_FONT);
		vsLabel.setAlignment(Align.center);
		vsLabel.setWidth(width);
		vsLabel.setY(height * 0.6f);

		addActor(vsLabel);
	}

	private void createPlayerLabel() {
		ShaderLabel playerLabel = new ShaderLabel(resources.fontShader, item.requester.handle, resources.skin,
				Constants.UI.DEFAULT_FONT);
		playerLabel.setAlignment(Align.center);
		playerLabel.setWidth(width);
		playerLabel.setY(height * 0.35f);

		addActor(playerLabel);
	}

	private void createLevelLabel() {
		final ShaderLabel levelLabel = new ShaderLabel(resources.fontShader, " Map: " + mapTitle, resources.skin,
				Constants.UI.DEFAULT_FONT);
		levelLabel.setAlignment(Align.center);
		levelLabel.setWidth(width);
		levelLabel.setY(height * 0.1f);

		addActor(levelLabel);
	}

	private void createDeclineButton() {
		float centerY = (height / 2) - (GraphicsUtils.actionButtonSize / 2);
		ActionButton declineButton = new ActionButton(resources.skin, "cancelButton", new Point(
				GraphicsUtils.actionButtonSize / 2, centerY));
		declineButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				final Overlay loadingOverlay = new LoadingOverlay(resources);
				getParent().getStage().addActor(loadingOverlay);
				UIConnectionWrapper.declineInvite(new UIConnectionResultCallback<BaseResult>() {
					@Override
					public void onConnectionResult(BaseResult result) {
						loadingOverlay.remove();
						fire(new DeclineInviteEvent(true));
					}

					@Override
					public void onConnectionError(String msg) {
						loadingOverlay.remove();
						fire(new DeclineInviteEvent(false));
					}
				}, item.game.id, GameLoop.USER.handle);
			}
		});
		addActor(declineButton);
	}

	public void createAcceptButton() {
		float centerY = (height / 2) - (GraphicsUtils.actionButtonSize / 2);
		ActionButton okButton = new ActionButton(resources.skin, "okButton", new Point(width
				- (GraphicsUtils.actionButtonSize * 1.5f), centerY));
		okButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				final Overlay loadingOverlay = new LoadingOverlay(resources);
				getParent().getStage().addActor(loadingOverlay);
				UIConnectionWrapper.acceptInvite(new UIConnectionResultCallback<GameBoard>() {
					public void onConnectionResult(GameBoard result) {
						loadingOverlay.remove();
						fire(new AcceptInviteEvent(true, result));
					};

					@Override
					public void onConnectionError(String msg) {
						loadingOverlay.remove();
						fire(new AcceptInviteEvent(false, msg));
					}
				}, item.game.id, GameLoop.USER.handle);
			}
		});
		addActor(okButton);
	}
}
