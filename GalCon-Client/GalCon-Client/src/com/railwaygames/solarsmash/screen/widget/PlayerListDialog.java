package com.railwaygames.solarsmash.screen.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.ExternalActionWrapper;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.config.ConfigResolver;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.Player;
import com.railwaygames.solarsmash.model.PlayerList;
import com.railwaygames.solarsmash.model.Point;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.overlay.DismissableOverlay;
import com.railwaygames.solarsmash.screen.overlay.Overlay;
import com.railwaygames.solarsmash.screen.overlay.TextOverlay;

public class PlayerListDialog extends Dialog {

	private OnClick onClick;

	public interface OnClick {
		public void onSuccess(Player player);

		public void onFail();
	}

	public PlayerListDialog(PlayerList playerList, String authId, String authProvider, Resources resources,
			float width, float height, Stage stage, OnClick onClick) {
		super(resources, width, height, stage);
		this.onClick = onClick;
		float margin = width * 0.05f;
		{
			ShaderLabel title = new ShaderLabel(resources.fontShader, "Choose a player", resources.skin,
					Constants.UI.DEFAULT_FONT, Color.WHITE);
			title.setBounds(margin, height * 0.9f, width - 2 * margin, height * 0.1f);
			addActor(title);
		}
		{
			ShaderLabel description = new ShaderLabel(resources.fontShader, "Your " + authProvider
					+ " account is already associated with another Solar Smash account. "
					+ "Please select the account you would like to keep.", resources.skin, Constants.UI.X_SMALL_FONT,
					Color.WHITE);
			description.setWrap(true);
			description.setBounds(margin, height * 0.6f, width - 2 * margin, height * 0.3f);
			addActor(description);
		}

		String session1 = playerList.players.get(0).sessionId;
		String session2 = playerList.players.get(1).sessionId;

		float y = height * 0.38f;
		for (Player player : playerList.players) {
			Button bg = new Button(resources.skin, Constants.UI.BASIC_BUTTON);
			bg.setX(margin);
			bg.setY(y);
			bg.setWidth(width - 2 * margin);
			bg.setHeight(height * 0.2f);
			addActor(bg);

			String deleteSession = player.sessionId.equals(session1) ? session2 : session1;
			bg.addListener(createClickListener(authId, authProvider, player.sessionId, deleteSession));

			y -= bg.getHeight() + height * 0.06f;

			{
				ShaderLabel label = new ShaderLabel(resources.fontShader, player.handle, resources.skin,
						Constants.UI.DEFAULT_FONT, Color.BLACK);
				label.setBounds(margin, bg.getHeight() * 0.6f, bg.getWidth() - 2 * margin, bg.getHeight() * 0.3f);
				label.setTouchable(Touchable.disabled);
				bg.addActor(label);
			}
			{
				ShaderLabel label = new ShaderLabel(resources.fontShader, "Rank: "
						+ ConfigResolver.getRankForXp(player.xp).level + " Wins: " + player.wins + " Losses: "
						+ player.losses, resources.skin, Constants.UI.X_SMALL_FONT, Color.BLACK);
				label.setBounds(margin, bg.getHeight() * 0.2f, bg.getWidth() - 2 * margin, bg.getHeight() * 0.3f);
				label.setTouchable(Touchable.disabled);
				bg.addActor(label);
			}
		}
	}

	private ClickListener createClickListener(final String authId, final String authProvider, final String keepSession,
			final String deleteSession) {
		return new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Overlay overlay = new Overlay(resources, 0.8f);

				float width = Gdx.graphics.getWidth();
				float height = Gdx.graphics.getHeight();

				WaitImageButton waitImage = new WaitImageButton(resources.skin);
				float buttonWidth = .25f * (float) width;
				waitImage.setWidth(buttonWidth);
				waitImage.setHeight(buttonWidth);
				waitImage.setX(width / 2 - buttonWidth / 2);
				waitImage.setY(height / 2 - buttonWidth / 2);
				overlay.addActor(waitImage);
				waitImage.start();
				getStage().addActor(overlay);

				Player currentUser = GameLoop.getUser();
				ExternalActionWrapper.addProviderToUserWithOverride(createCallback(overlay), currentUser.handle,
						authId, authProvider, keepSession, deleteSession);
			};
		};
	}

	private UIConnectionResultCallback<PlayerList> createCallback(final Overlay overlay) {
		return new UIConnectionResultCallback<PlayerList>() {

			@Override
			public void onConnectionResult(PlayerList result) {
				overlay.remove();
				PlayerListDialog.this.remove();

				onClick.onSuccess(result.players.get(0));
			}

			@Override
			public void onConnectionError(String msg) {
				overlay.remove();

				DismissableOverlay dOverlay = new DismissableOverlay(resources, new TextOverlay(
						"Could not associate user\n\nPlease try again", resources), new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						PlayerListDialog.this.hide();
						onClick.onFail();
					}
				});
				getStage().addActor(dOverlay);
			}
		};
	}

	public void show(Point point) {
		float duration = 0.4f;

		super.show(point, duration);
	}

	public void hide() {
		super.hide(0.3f);
	}
}
