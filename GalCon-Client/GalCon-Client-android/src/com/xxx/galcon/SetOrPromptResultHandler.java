package com.xxx.galcon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.badlogic.gdx.Gdx;
import com.xxx.galcon.http.ConnectionException;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.SetPlayerResultHandler;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.HandleResponse;
import com.xxx.galcon.model.Player;

public class SetOrPromptResultHandler implements UIConnectionResultCallback<Player> {

	private Activity activity;
	private AlertDialog alertDialog;
	private GameAction gameAction;
	private Player player;

	public SetOrPromptResultHandler(Activity activity, GameAction gameAction, Player player) {
		this.activity = activity;
		this.gameAction = gameAction;
		this.player = player;
	}

	public void onConnectionResult(final Player playerFromServer) {
		if (playerFromServer.handle != null && !playerFromServer.handle.isEmpty()) {
			new SetPlayerResultHandler(player).onConnectionResult(playerFromServer);
		} else {
			activity.runOnUiThread(new Runnable() {
				public void run() {
					AlertDialog.Builder builder = new AlertDialog.Builder(activity);

					LayoutInflater inflater = activity.getLayoutInflater();
					final ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.create_username, null);
					final EditText userNameEditText = (EditText) viewGroup.findViewById(R.id.username);
					builder.setView(viewGroup);

					alertDialog = builder.create();
					alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, activity.getBaseContext().getResources()
							.getString(R.string.create), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							String handle = userNameEditText.getText().toString();
							try {
								gameAction.requestHandleForUserName(new NewHandleResultCallback(player, alertDialog),
										playerFromServer.name, handle);
							} catch (ConnectionException e) {
								// TODO Auto-generated catch block
							}
						}
					});
					alertDialog.show();
				}
			});
		}
	}

	public void onConnectionError(String msg) {
		// TODO Auto-generated method stub

	}

	private static class NewHandleResultCallback implements UIConnectionResultCallback<HandleResponse> {

		private AlertDialog dialog;
		private Player gamePlayer;

		public NewHandleResultCallback(Player gamePlayer, AlertDialog dialog) {
			this.dialog = dialog;
			this.gamePlayer = gamePlayer;
		}

		public void onConnectionResult(final HandleResponse result) {
			if (result.handleCreated) {
				new SetPlayerResultHandler(gamePlayer).onConnectionResult(result.player);
			} else {
				TextView textView = (TextView) dialog.findViewById(R.id.usernameErrorMsg);
				textView.setText(R.string.usernameTaken);
				dialog.show();
			}
		}

		public void onConnectionError(String msg) {
			TextView textView = (TextView) dialog.findViewById(R.id.usernameErrorMsg);
			textView.setText(R.string.usernameTaken);
			dialog.show();
		}
	}
}
