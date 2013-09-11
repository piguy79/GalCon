package com.xxx.galcon;


import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.crashlytics.android.Crashlytics;
import com.jirbo.adcolony.AdColony;
import com.jirbo.adcolony.AdColonyVideoAd;
import com.jirbo.adcolony.AdColonyVideoListener;
import com.xxx.galcon.config.Configuration;
import com.xxx.galcon.http.SetConfigurationResultHandler;
import com.xxx.galcon.http.SocialAction;
import com.xxx.galcon.inappbilling.util.IabHelper;
import com.xxx.galcon.inappbilling.util.IabResult;
import com.xxx.galcon.inappbilling.util.Purchase;
import com.xxx.galcon.inappbilling.util.StoreResultCallback;
import com.xxx.galcon.model.Inventory;
import com.xxx.galcon.model.InventoryItem;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.service.PingService;

public class MainActivity extends AndroidApplication {
	public static final String LOG_NAME = "GalCon";


	protected String mDebugTag = "MainActivity";
	protected boolean mDebugLog = true;
	
	final static String APP_ID  = "appae5819628c4f43b5b7f9f9";
	final static String ZONE_ID = "vz592240fd26724b2a955912";
	
	private AndroidGameAction gameAction;
	
	IabHelper mHelper;

	public MainActivity() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Crashlytics.start(this);
		
	    setupAdColony();
	    setupInAppBilling();

		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
		cfg.useGL20 = true;

		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

		gameAction = new AndroidGameAction(this, connectivityManager);
		SocialAction socialGameAction = new AndroidSocialAction(this);

		Player player = new Player();
		player.name = UserInfo.getUser(getBaseContext());

		gameAction.findUserInformation(new SetOrPromptResultHandler(this, gameAction, player), player.name);
		
		Configuration config = new Configuration();
		gameAction.findConfigByType(new SetConfigurationResultHandler(config), "app");

		initialize(new GameLoop(player, gameAction, socialGameAction, config), cfg);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Intent intent = new Intent(this, PingService.class);
		startService(intent);
	}

	private void setupInAppBilling() {
		String base64EncodedPublicKey = "";
		
		// Create the helper, passing it our context and the public key to verify signatures with
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        
        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);
        
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }
            }
        });
		
	}

	private void setupAdColony() {
		AdColony.configure( this, "version:1.0,store:google", APP_ID, ZONE_ID );
	    
	    if ( !AdColony.isTablet() )
	    {
	      setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
	    }
	}
	
	void complain(String message) {
        alert("Error: " + message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        bld.create().show();
    }
	
	@Override
	protected void onPause(){
		super.onPause();
		AdColony.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		AdColony.resume( this );
	}


	public void displayAd(AdColonyVideoListener adListener) {
		AdColonyVideoAd ad = new AdColonyVideoAd();
        ad.show(adListener);
	}
	
	public void purchaseCoins(int numCoins){
	}
	
	public void loadInventory(StoreResultCallback<Inventory> callback){
		final Inventory stock = new Inventory();
		stock.inventory = new ArrayList<InventoryItem>(){
			{
				add(new InventoryItem("123", 0.99, "2 Coins", 2));
			}
		};
		callback.onResult(stock);
	}
	
	
}
