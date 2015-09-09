package com.android.proximus.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.proximus.util.ParseConstants;
import com.android.proximus.R;
import com.android.proximus.adapter.UserAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;


public class ManageFriendsActivityFragment extends Fragment {

    private static final String LOG_TAG = ManageFriendsActivity.class.getSimpleName();

    protected List<ParseUser> mUsers;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;
    protected GridView mGridView;

    public ManageFriendsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.user_grid, container, false);

        mGridView = (GridView) rootView.findViewById(R.id.friendsGrid);
        mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
        TextView textView = (TextView) rootView.findViewById(android.R.id.empty);
        mGridView.setEmptyView(textView);

        mGridView.setOnItemClickListener(mOnItemClickListner);

        return rootView;
    }

    @Override
    public void onResume() {
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

                    if (mGridView.getAdapter() == null) {
                        UserAdapter adapter = new UserAdapter(getActivity(), mUsers);
                        mGridView.setAdapter(adapter);
                    } else {
                        ((UserAdapter) mGridView.getAdapter()).refill(mUsers);
                    }

                    addFriendsCheckMarks();

                } else {
                    Log.e(LOG_TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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

                                mGridView.setItemChecked(i, true);
                            }
                        }

                    }


                }else {
                    Log.e(LOG_TAG, e.getMessage());
                }


            }
        });
    }

    protected AdapterView.OnItemClickListener mOnItemClickListner = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            ImageView checkImageView = (ImageView) view.findViewById(R.id.userCheckView);
            if (mGridView.isItemChecked(position)) {

                mFriendsRelation.add(mUsers.get(position));
                checkImageView.setVisibility(View.VISIBLE);
            }
            else {

            mFriendsRelation.remove(mUsers.get(position));
            checkImageView.setVisibility(View.INVISIBLE);
            }

            mCurrentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(LOG_TAG, e.getMessage());
                    }

                }
            });
        }
    };
}
