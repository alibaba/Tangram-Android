package com.tmall.wireless.tangram;

import java.util.Map;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.util.ArrayMap;
import android.test.AndroidTestCase;
import com.tmall.wireless.tangram.core.service.ServiceManager;
import com.tmall.wireless.tangram.support.BannerSupport;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

/**
 * Created by longerian on 2018/3/13.
 *
 * @author longerian
 * @date 2018/03/13
 */

@RunWith(AndroidJUnit4.class)
public class RxBannerSupportTest {

    @Rule public final ActivityTestRule<BannerTestActivity> activityRule =
        new ActivityTestRule<>(BannerTestActivity.class);

    @Rule
    public final UiThreadTestRule uiThread = new UiThreadTestRule();

    private final Context context = InstrumentationRegistry.getContext();

    private BannerSupport mBannerSupport = new BannerSupport();

    private final ServiceManager mServiceManager = new ServiceManager() {
        private Map<Class<?>, Object> mServices = new ArrayMap<>();

        @Override
        public <T> void register(Class<T> type, T service) {
            mServices.put(type, type.cast(service));
        }

        @Override
        public <T> T getService(Class<T> type) {
            Object service = mServices.get(type);
            if (service == null) {
                return null;
            }
            return type.cast(service);
        }
    };

    @Before
    public void setUp() {
        BannerTestActivity activity = activityRule.getActivity();
        mServiceManager.register(BannerSupport.class, mBannerSupport);
    }



}
