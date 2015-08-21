package com.koolew.mars;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.koolew.mars.statistics.BaseActivity;


public class VideoPrivacyActivity extends BaseActivity {

    public static final int AUTHORITY_PUBLIC = 0;
    public static final int AUTHORITY_FRIEND_ONLY = 1;

    private int mAuthority;

    private ImageView mPublicImage;
    private ImageView mFriendOnlyImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_privacy);

        initViews();

        setAuthority(AUTHORITY_PUBLIC);
    }

    private void initViews() {
        mPublicImage = (ImageView) findViewById(R.id.public_check);
        mFriendOnlyImage = (ImageView) findViewById(R.id.only_friend_check);
    }

    private void setAuthority(int authority) {
        mAuthority = authority;
        if (authority == AUTHORITY_PUBLIC) {
            mPublicImage.setVisibility(View.VISIBLE);
            mFriendOnlyImage.setVisibility(View.INVISIBLE);
        }
        else if (authority == AUTHORITY_FRIEND_ONLY) {
            mPublicImage.setVisibility(View.INVISIBLE);
            mFriendOnlyImage.setVisibility(View.VISIBLE);
        }
    }

    public void onPublicClick(View v) {
        setAuthority(AUTHORITY_PUBLIC);
    }

    public void onFriendOnlyClick(View v) {
        setAuthority(AUTHORITY_FRIEND_ONLY);
    }

    @Override
    public void onBackPressed() {
        setResult(mAuthority);
        super.onBackPressed();
    }
}
