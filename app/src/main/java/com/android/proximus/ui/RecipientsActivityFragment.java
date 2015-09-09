package com.android.proximus.ui;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.proximus.util.ParseConstants;
import com.android.proximus.R;
import com.android.proximus.adapter.UserAdapter;
import com.android.proximus.util.FileHelper;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class RecipientsActivityFragment extends Fragment {

    private static final String LOG_TAG = RecipientsActivityFragment.class.getSimpleName();

    protected List<ParseUser> mFriends;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;
    protected MenuItem mSendMenuItem;
    protected Uri mMediaUri;
    protected String mFileType;
    protected GridView mGridView;

    public RecipientsActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
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

        mMediaUri = getActivity().getIntent().getData();
        mFileType = getActivity().getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {

                if (e == null) {
                    mFriends = friends;

                    String[] userNames = new String[mFriends.size()];
                    int i = 0;
                    for (ParseUser friend : mFriends) {
                        userNames[i] = friend.getUsername();
                        i++;
                    }

                    if (mGridView.getAdapter() == null) {
                        UserAdapter adapter = new UserAdapter(getActivity(), mFriends);
                        mGridView.setAdapter(adapter);
                    } else {
                        ((UserAdapter) mGridView.getAdapter()).refill(mFriends);
                    }

                } else {
                    Log.e(LOG_TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.error_title)
                            .setMessage(e.getMessage())
                            .setPositiveButton(android.R.string.ok, null);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_recipients_frag, menu);

        mSendMenuItem = menu.getItem(0);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_send){

            ParseObject message = createMessage();
            if (message == null){

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.general_error)
                    .setMessage(R.string.file_selected_error)
                    .setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();

            }
            else {
                send(message);
                getActivity().finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    protected void send(ParseObject message) {
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

                if(e == null) {

                    Log.i("SENT", "Message sent successfully!");
                    sendPushNotification();

                }else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.general_error)
                            .setMessage(R.string.error_sending_try_again)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    protected void sendPushNotification() {

        ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
        query.whereContainedIn(ParseConstants.KEY_USER_ID, getRecipientIds());

        ParsePush push = new ParsePush();
        push.setQuery(query);
        push.setMessage("You have a new message from " + mCurrentUser.getUsername() + "!");
        push.sendInBackground();
    }

    protected ParseObject createMessage(){

        ParseObject message = new ParseObject(ParseConstants.CLASS_MEASSAGES);

        message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientIds());
        message.put(ParseConstants.KEY_FILE_TYPE, mFileType);

        byte[] fileBytes = FileHelper.getByteArrayFromFile(getActivity(), mMediaUri);

        if (fileBytes == null){

            return null;
        }
        else {

            if(mFileType.equals(ParseConstants.TYPE_IMAGE)){
                fileBytes = FileHelper.reduceImageForUpload(fileBytes);
            }
            String fileName = FileHelper.getFileName(getActivity(),mMediaUri,mFileType);
            ParseFile file = new ParseFile(fileName,fileBytes);

            message.put(ParseConstants.KEY_FILE, file);

            return message;
        }
    }

    protected ArrayList<String> getRecipientIds() {

        ArrayList<String> recipientIds = new ArrayList<String>();
        for (int i = 0; i < mGridView.getCount(); i++){
            if (mGridView.isItemChecked(i)){
                recipientIds.add(mFriends.get(i).getObjectId());
            }
        }
        return recipientIds;
    }

    protected AdapterView.OnItemClickListener mOnItemClickListner = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (mGridView.getCheckedItemCount() > 0) {
                mSendMenuItem.setVisible(true);
            }else {
                mSendMenuItem.setVisible(false);
            }
            ImageView checkImageView = (ImageView) view.findViewById(R.id.userCheckView);
            if (mGridView.isItemChecked(position)) {

                checkImageView.setVisibility(View.VISIBLE);
            }
            else {

                checkImageView.setVisibility(View.INVISIBLE);
            }

        }
    };
}
