package com.railwaygames.solarsmash.screen.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.railwaygames.solarsmash.model.Move;
import com.railwaygames.solarsmash.model.Point;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.event.CancelDialogEvent;
import com.railwaygames.solarsmash.screen.event.MoveEvent;
import com.railwaygames.solarsmash.screen.event.SliderUpdateEvent;
import com.railwaygames.solarsmash.screen.widget.ActionButton;

public class ShipSelectionHud extends Group {

	private Slider slider;
	private Resources resources;
	private ActionButton okButton;

	private int max;
	private int shipsToSend = 0;
	private Move move;

	public ShipSelectionHud(Move move, int shipsOnPlanet, Resources resources) {
		this.resources = resources;
		this.setWidth(Gdx.graphics.getWidth());
		this.setHeight(Gdx.graphics.getHeight() * 0.1f);
		this.move = move;

		this.max = shipsOnPlanet + move.shipsToMove;
		this.shipsToSend = move.shipsToMove;

		createBackground();
		addOkButton();
		addCancelButton();
		addSlider();

		this.slider.setValue(shipsToSend);
	}

	private void createBackground() {
		AtlasRegion bgRegion = resources.gameBoardAtlas.findRegion("player_hud");
		Image backGround = new Image(new TextureRegionDrawable(bgRegion));
		backGround.setWidth(getWidth());
		backGround.setHeight(getHeight());
		addActor(backGround);
	}

	private void addCancelButton() {
		final ActionButton cancelButton = new ActionButton(resources.skin, "cancelButton", new Point(10,
				getHeight() * 0.1f));
		cancelButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				fire(new CancelDialogEvent());
			}
		});

		addActor(cancelButton);
	}

	private void addOkButton() {
		okButton = new ActionButton(resources.skin, "okButton", new Point(0, getHeight() * 0.1f));
		okButton.setX(getWidth() - okButton.getWidth() - 10);
		okButton.setDisabled(shipsToSend == 0);
		okButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(!okButton.isDisabled()){
					int oldShipsToMove = move.shipsToMove;
					move.shipsToMove = shipsToSend;
					fire(new MoveEvent(oldShipsToMove, move));	
				}
			}
		});

		addActor(okButton);
	}

	private void addSlider() {
		Drawable sliderBg = new TextureRegionDrawable(resources.menuAtlas.findRegion("slider_bg"));
		Drawable sliderKnob = new TextureRegionDrawable(resources.gameBoardAtlas.findRegion("ship"));

		sliderBg.setMinHeight(getHeight() * 0.2f);
		sliderBg.setMinWidth(getWidth() * 0.6f);
		sliderKnob.setMinHeight(getHeight() * 0.5f);
		sliderKnob.setMinWidth(getWidth() * 0.12f);
		resources.skin.add("default-horizontal", new SliderStyle(sliderBg, sliderKnob));

		slider = new Slider(0, max, 1, false, resources.skin);
		slider.setWidth(getWidth() * 0.6f);
		slider.setHeight(getHeight() * 0.8f);
		slider.setY(getHeight() * 0.05f);
		slider.setX(getWidth() * 0.2f);

		slider.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				shipsToSend = (int) slider.getValue();
				okButton.setDisabled(shipsToSend == 0);
				fire(new SliderUpdateEvent(shipsToSend));
			}
		});

		addActor(slider);
	}
}
