package com.koolew.mars;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.statistics.BaseV4FragmentActivity;
import com.koolew.mars.view.TitleBarView;

public class TitleFragmentActivity extends BaseV4FragmentActivity {

    public static final String KEY_FRAGMENT_CLASS = "fragment_class";

    protected TitleBarView mTitleBar;
    protected BaseTitleFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_fragment);

        mTitleBar = (TitleBarView) findViewById(R.id.title_bar);

        Bundle extras = getIntent().getExtras();
        Class<BaseTitleFragment> fragmentClass = (Class) extras.getSerializable(KEY_FRAGMENT_CLASS);
        try {
            mFragment = fragmentClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, mFragment);
        fragmentTransaction.commit();

        mTitleBar.setBackgroundColor(mFragment.getTitleBarColor(this));
        mTitleBar.setTitle(mFragment.getTitle(this));
    }


    public abstract static class BaseTitleFragment<A extends LoadMoreAdapter>
            extends RecyclerListFragmentMould<A> {

        public abstract int getTitleBarColor(Context context);

        public abstract String getTitle(Context context);
    }

    public static void launchFragment(Context context, Class fragmentClass) {
        Intent intent = new Intent(context, TitleFragmentActivity.class);
        intent.putExtra(KEY_FRAGMENT_CLASS, fragmentClass);
        context.startActivity(intent);
    }

    public static void launchFragment(Context context, Class fragmentClass,
                                      Bundle extras) {
        Intent intent = new Intent(context, TitleFragmentActivity.class);
        intent.putExtra(KEY_FRAGMENT_CLASS, fragmentClass);
        intent.putExtras(extras);
        context.startActivity(intent);
    }
}
