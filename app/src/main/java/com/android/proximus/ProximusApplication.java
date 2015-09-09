package com.android.proximus;

import android.app.Application;

import com.android.proximus.util.ParseConstants;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

/**
 * Created by Yasin on 8/28/2015.
 */
public class ProximusApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "4VkzHMxd6dxctY8ohXMKoQ8Jh8F6Sc7KkyY3Tq4e", "GKB4cweC7PvR4n6dGwl0d22S1JPFbcnLYgwZuCGY");
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    public static void updateParseInstallation(ParseUser user) {

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put(ParseConstants.KEY_USER_ID, user.getObjectId());
        installation.saveInBackground();
    }
}
