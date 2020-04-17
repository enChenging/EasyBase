package com.release.easybase.base;

import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import com.release.easybase.R;
import com.release.easybase.constance.BConstants;
import com.release.easybase.event.NetworkChangeEvent;
import com.release.easybase.receiver.NetworkChangeReceiver;
import com.release.easybase.utils.AppManager;
import com.release.easybase.utils.KeyBoardUtils;
import com.release.easybase.utils.SPUtil;
import com.release.easybase.utils.StatusBarUtil;
import com.release.easybase.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;

/**
 * @author Mr.release
 * @create 2019/3/22
 * @Describe
 */
public abstract class BaseActivity<T extends Presenter> extends AppCompatActivity implements
        UiInterfaceAct {

    protected static String TAG;

    protected T mPresenter;

    protected NetworkChangeReceiver mNetworkChangeReceiver;

    public boolean useEventBus() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        TAG = this.getClass().getSimpleName();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(getLayoutId());

        if (useEventBus())
            EventBus.getDefault().register(this);

        initView();

        initListener();

        AppManager.addActivity(this);

        startNet();
    }

    @Override
    public void initView() {

    }

    @Override
    public void initListener() {
    }

    @Override
    public void startNet() {

    }

    public void doReConnected() {
        startNet();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initReceiver();
        initThemeColor();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNetworkChangeReceiver != null) {
            unregisterReceiver(mNetworkChangeReceiver);
            mNetworkChangeReceiver = null;
        }
    }

    private void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        mNetworkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(mNetworkChangeReceiver, filter);
    }

    protected void initThemeColor() {
        StatusBarUtil.setColorNoTranslucent(this, getResources().getColor(R.color.theme_color));

        //ActionBar颜色
        if (this.getSupportActionBar() != null) {
            this.getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.theme_color));
        }

        //底部带返回键手机的导航栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Color.BLACK);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            View currentFocus = getCurrentFocus();
            // 如果不是落在EditText区域，则需要关闭输入法
            if (KeyBoardUtils.isHideKeyboard(currentFocus, ev)) {
                KeyBoardUtils.hideKeyboard(this, currentFocus);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.hold, R.anim.slide_right_exit);
    }


    @Override
    protected void onDestroy() {
        if (useEventBus())
            EventBus.getDefault().unregister(this);
        AppManager.removeActivity(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkChangeEvent(NetworkChangeEvent event) {
        SPUtil.setData(BConstants.HAS_NETWORK_KEY, event.isConnected());
        checkNetwork(event.isConnected());
    }

    private void checkNetwork(boolean isConnected) {
        if (isConnected) {
            doReConnected();
        } else {
            ToastUtils.show("请检查网络");
        }
    }
}
