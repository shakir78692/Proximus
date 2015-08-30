package com.android.proximus;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class ManageFriendsActivity extends ListActivity {

    private static final String LOG_TAG = ManageFriendsActivity.class.getSimpleName();

    protected List<ParseUser> mUsers;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_friends);

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

    }

    @Override
    protected void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.orderByAscending(ParseConstants.KEY_USERNAME);
        query.setLimit(1000);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null) {
                    //Success
                    mUsers = users;
                    String[] userNames = new String[mUsers.size()];
                    int i = 0;
                    for (ParseUser user : mUsers) {
                        userNames[i] = user.getUsername();
                        i++;
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(ManageFriendsActivity.this, android.R.layout.simple_list_item_checked, userNames);
                    setListAdapter(adapter);

                    addFriendsCheckMarks();

                } else {
                    Log.e(LOG_TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(ManageFriendsActivity.this);
                    builder.setTitle(R.string.error_title)
                            .setMessage(e.getMessage())
                            .setPositiveButton(android.R.string.ok, null);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    private void addFriendsCheckMarks() {

        mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {

                if (e == null){
                    //List returned success - look for a match

                    for (int i =0; i < mUsers.size(); i++){

                        ParseUser user = mUsers.get(i);

                        for (ParseUser friend : friends){

                            if (user.getObjectId().equals(friend.getObjectId())){

                                getListView().setItemChecked(i,true);
                            }
                        }

                    }


                }else {
                    Log.e(LOG_TAG, e.getMessage());
                }


            }
        });
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (getListView().isItemChecked(position)) {

            mFriendsRelation.add(mUsers.get(position));

        }
        else {

            mFriendsRelation.remove(mUsers.get(position));

        }

        mCurrentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(LOG_TAG, e.getMessage());
                } else {

                }
            }
        });
    }
}