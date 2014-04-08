package com.railwaygames.solarsmash.screen.ship.selection;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.config.ConfigConstants;
import com.railwaygames.solarsmash.model.GameBoard;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.widget.OKCancelDialog;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;

public class HarvestDialog extends OKCancelDialog {
	private ShaderLabel confirmText;

	public HarvestDialog(GameBoard gameBoard, Resources resources, float width, float height, Stage stage) {
		super(resources, width, height, stage, OKCancelDialog.Type.OK_CANCEL);

		String harvestRounds = gameBoard.gameConfig.getValue(ConfigConstants.HARVEST_ROUNDS);

		confirmText = new ShaderLabel(resources.fontShader, "Harvest bonus will last\nfor " + harvestRounds
				+ " rounds and result\nin the moon's destruction\n\nContinue?", resources.skin, Constants.UI.DEFAULT_FONT);
		confirmText.setAlignment(Align.center);
		confirmText.setY(getHeight() * 0.2f);
		confirmText.setX(0);
		confirmText.setWidth(getWidth());
		confirmText.setColor(Color.WHITE);

		addActor(confirmText);
	}
}
