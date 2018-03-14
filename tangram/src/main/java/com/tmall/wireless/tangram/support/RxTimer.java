package com.tmall.wireless.tangram.support;

import java.util.concurrent.TimeUnit;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import com.tmall.wireless.tangram.support.TimerSupport.OnTickListener;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

/**
 * Created by longerian on 2018/3/8.
 *
 * @author longerian
 * @date 2018/03/08
 */

public class RxTimer implements ITimer {

    private long mInterval;
    private TimerStatus mStatus;
    private boolean pause;

    private Observable<Long> mIntervalObservable;

    private ArrayMap<OnTickListener, Disposable> tickCache = new ArrayMap<>();

    public RxTimer(long interval) {
        this.mInterval = interval;
        this.mStatus = TimerStatus.Waiting;
        this.mIntervalObservable = Observable
            .interval(0, this.mInterval, TimeUnit.MILLISECONDS)
            .doOnSubscribe(new Consumer<Disposable>() {
                @Override
                public void accept(Disposable disposable) throws Exception {
                    mStatus = TimerStatus.Running;
                    Log.d("RxTimerSupportTest", "accept " + disposable + " status " + mStatus);
                }
            })
            .doOnDispose(new Action() {
                @Override
                public void run() throws Exception {
                    mStatus = TimerStatus.Paused;
                    Log.d("RxTimerSupportTest", "on dispose " + " status " + mStatus);
                }
            })
            .doOnTerminate(new Action() {
                @Override
                public void run() throws Exception {
                    mStatus = TimerStatus.Stopped;
                    Log.d("RxTimerSupportTest", "on terminate " + " status " + mStatus);
                }
            })
            .share();
    }

    public void register(final int interval, @NonNull final OnTickListener onTickListener, boolean intermediate) {
        Disposable disposable = this.mIntervalObservable.delaySubscription(intermediate ? 0 : interval * mInterval, TimeUnit.MILLISECONDS).skipWhile(

            new Predicate<Long>() {
                @Override
                public boolean test(Long aLong) throws Exception {
                    return pause;
                }
            }).observeOn(
            AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {

                private int count = 0;

                @Override
                public void accept(Long aLong) throws Exception {
                    if (count == 0) {
                        if (onTickListener != null) {
                            onTickListener.onTick();
                        }
                    }
                    count = (count + 1) % interval;
                }
        });
        tickCache.put(onTickListener, disposable);
    }

    public void unregister(OnTickListener onTickListener) {
        Disposable disposable = tickCache.get(onTickListener);
        if (disposable != null) {
            disposable.dispose();
            tickCache.remove(onTickListener);
        }
    }

    public boolean isRegistered(@NonNull OnTickListener onTickListener) {
        return tickCache.containsKey(onTickListener);
    }

    @Override
    public void start() {
        //no need
    }

    @Override
    public void start(boolean bySecond) {
        //no need
    }

    @Override
    public void pause() {
        pause = true;
        mStatus = TimerStatus.Paused;
        //TODO manage status in reactive
    }

    @Override
    public void restart() {
        pause = false;
        if (mStatus == TimerStatus.Paused) {
            mStatus = TimerStatus.Running;
        }
    }

    @Override
    public void stop() {
        for (int i = 0, size = tickCache.size(); i < size; i++) {
            Disposable disposable = tickCache.valueAt(i);
            disposable.dispose();
        }
        tickCache.clear();
    }

    @Override
    public void cancel() {
        //no need
    }

    @Override
    public TimerStatus getStatus() {
        return mStatus;
    }
}
