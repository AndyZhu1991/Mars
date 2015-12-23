package com.koolew.mars.statistics;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.tendcloud.tenddata.TCAgent;

/**
 * Created by jinchangzhu on 10/9/15.
 */
public class BaseLazyV4Fragment extends com.shizhefei.fragment.BaseFragment {

    protected boolean isNeedPageStatistics = true;

    protected boolean isLazyResumed = false;

    private boolean isInit = false;
    private Bundle savedInstanceState;
    public static final String INTENT_BOOLEAN_LAZYLOAD = "intent_boolean_lazyLoad";
    protected boolean isLazyLoad = true;
    private FrameLayout layout;

    @Deprecated
    protected final void onCreateView(Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            isLazyLoad = bundle.getBoolean(INTENT_BOOLEAN_LAZYLOAD, isLazyLoad);
        }
        if (isLazyLoad) {
            if (getUserVisibleHint() && !isInit) {
                isInit = true;
                this.savedInstanceState = savedInstanceState;
                onCreateViewLazy(savedInstanceState);
            } else {
                layout = new FrameLayout(getApplicationContext());
                layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT));
                View defaultView = createDefaultView();
                if (defaultView != null) {
                    layout.addView(defaultView);
                }
                super.setContentView(layout);
            }
        } else {
            isInit = true;
            onCreateViewLazy(savedInstanceState);
        }
    }

    protected View createDefaultView() {
        return null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && !isInit && getContentView() != null) {
            isInit = true;
            onCreateViewLazy(savedInstanceState);
            onResumeLazy();
        }
        if (isInit && getContentView() != null) {
            if (isVisibleToUser) {
                isStart = true;
                onFragmentStartLazy();
            } else {
                isStart = false;
                onFragmentStopLazy();
            }
        }

        // For page statistics
        if (isVisibleToUser && isLazyResumed) {
            onPageStart();
        }
        else if (!isVisibleToUser && isLazyResumed) {
            onPageEnd();
        }
    }

    @Deprecated
    @Override
    public final void onStart() {
        super.onStart();
        if (isInit && !isStart && getUserVisibleHint()) {
            isStart = true;
            onFragmentStartLazy();
        }
    }

    @Deprecated
    @Override
    public final void onStop() {
        super.onStop();
        if (isInit && isStart && getUserVisibleHint()) {
            isStart = false;
            onFragmentStopLazy();
        }
    }

    private boolean isStart = false;

    protected void onFragmentStartLazy() {

    }

    protected void onFragmentStopLazy() {

    }

    protected void onCreateViewLazy(Bundle savedInstanceState) {

    }

    protected void onResumeLazy() {
        isLazyResumed = true;
        if (getUserVisibleHint()) {
            onPageStart();
        }
    }

    protected void onPauseLazy() {
        isLazyResumed = false;
        if (getUserVisibleHint()) {
            onPageEnd();
        }
    }

    protected void onDestroyViewLazy() {

    }

    @Override
    public void setContentView(int layoutResID) {
        if (isLazyLoad && getContentView() != null && getContentView().getParent() != null) {
            layout.removeAllViews();
            View view = inflater.inflate(layoutResID, layout, false);
            layout.addView(view);
        } else {
            super.setContentView(layoutResID);
        }
    }

    @Override
    public void setContentView(View view) {
        if (isLazyLoad && getContentView() != null && getContentView().getParent() != null) {
            layout.removeAllViews();
            layout.addView(view);
        } else {
            super.setContentView(view);
        }
    }

    @Override
    @Deprecated
    public final void onResume() {
        super.onResume();
        if (isInit) {
            onResumeLazy();
        }
    }

    @Override
    @Deprecated
    public final void onPause() {
        super.onPause();
        if (isInit) {
            onPauseLazy();
        }
    }

    @Override
    @Deprecated
    public final void onDestroyView() {
        super.onDestroyView();
        if (isInit) {
            onDestroyViewLazy();
        }
        isInit = false;
    }

    private boolean isPageStarted = false;

    protected void onPageStart() {
        if (isNeedPageStatistics && !isPageStarted) {
            isPageStarted = true;
            TCAgent.onPageStart(getActivity(), getClass().getSimpleName());
        }
    }

    protected void onPageEnd() {
        if (isNeedPageStatistics && isPageStarted) {
            isPageStarted = false;
            TCAgent.onPageEnd(getActivity(), getClass().getSimpleName());
        }
    }
}
