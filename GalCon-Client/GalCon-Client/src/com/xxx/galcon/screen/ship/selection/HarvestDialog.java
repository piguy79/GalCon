package com.xxx.galcon.screen.ship.selection;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.xxx.galcon.Constants;
import com.xxx.galcon.config.ConfigConstants;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.screen.Resources;
import com.xxx.galcon.screen.widget.OKCancelDialog;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class HarvestDialog extends OKCancelDialog {
	private ShaderLabel confirmText;

	public HarvestDialog(GameBoard gameBoard, Resources resources, float width, float height, Stage stage) {
		super(resources, width, height, stage, OKCancelDialog.Type.OK_CANCEL);

		String harvestRounds = gameBoard.gameConfig.getValue(ConfigConstants.HARVEST_ROUNDS);

		confirmText = new ShaderLabel(resources.fontShader, "Harvest will take\n" + harvestRounds
				+ " rounds\n\nContinue?", resources.skin, Constants.UI.BASIC_BUTTON_TEXT);
		confirmText.setAlignment(Align.center);
		confirmText.setY(getHeight() * 0.6f);
		confirmText.setWidth(getWidth());
		confirmText.setColor(Color.CLEAR);

		addActor(confirmText);
	}
}
