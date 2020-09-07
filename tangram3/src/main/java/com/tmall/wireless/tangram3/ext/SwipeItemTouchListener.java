/*
 * MIT License
 *
 * Copyright (c) 2018 Alibaba Group
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.tmall.wireless.tangram3.ext;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.tmall.ultraviewpager.UltraViewPager;
import com.tmall.wireless.tangram3.core.adapter.GroupBasicAdapter;
import com.tmall.wireless.tangram3.dataparser.concrete.Card;
import com.tmall.wireless.tangram3.structure.card.SwipeCard;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Intercept touch event of RecyclerView binded to Tangram
 * <li>Steps: </li>
 * <li>Record the current cards in visible area of screen when recyclerView's state changes to idle. Start intercepting gesture event if there's a {@link SwipeCard}</li>
 * <li>During gesture interception, detect the horizontal and vertical swipe gesture and invoke callback</li>
 * Created by mikeafc on 17/2/16.
 */
public class SwipeItemTouchListener implements RecyclerView.OnItemTouchListener {
    private static final String TAG = "TangramEngine";

    private static final int ANIMATE_DURATION = 150;
    private static final int SWIPING_HOR = 1;
    private static final int SWIPING_VER = 2;
    private static final int SWIPING_NONE = -1;

    private GestureDetectorCompat mSwipeGestureDector;
    private List<View> mChildList;

    private float mDistanceX;
    private float mDistanceY;

    private MotionEvent lastMotionEvent;

    private Card currCard;
    private int currCardIdx;

    private int swipeType = SWIPING_NONE;

    private GroupBasicAdapter mGroupBasicAdapter;

    private VirtualLayoutManager layoutManager;

    private RecyclerView recyclerView;

    private WeakReference<SwipeCard> mSwipeCardRef;

    private PullFromEndListener pullFromEndListener;

    private int mActionEdge = 0;

    private boolean enableAnim = false;

    //Swipe card when scroll state is idle
    private boolean isOptMode;

    public SwipeItemTouchListener(Context context, GroupBasicAdapter groupBasicAdapter, RecyclerView recyclerView) {
        this.mGroupBasicAdapter = groupBasicAdapter;
        this.recyclerView = recyclerView;
        this.recyclerView.addOnScrollListener(scrollListener);
        this.layoutManager = (VirtualLayoutManager) recyclerView.getLayoutManager();
        mSwipeGestureDector = new GestureDetectorCompat(context, new SwipeGestureListener());
        mChildList = new ArrayList<>();
    }

    public void setActionEdge(int actionEdge) {
        this.mActionEdge = actionEdge;
    }

    public void setPullFromEndListener(PullFromEndListener listener) {
        pullFromEndListener = listener;
    }

    private boolean isReadyToPullFromEnd() {
        return pullFromEndListener != null && pullFromEndListener.isReadyToPull();
    }

    private boolean isSwiping() {
        return swipeType != SWIPING_NONE;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
        if ((recyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE) ||
                !isAttachedToWindow(recyclerView) || !hasAdapter(recyclerView)) {
            return false;
        }

        if (findFixedChildViewUnder(motionEvent) != null) {
            return false;
        }

        if (findScrollableChildViewUnder(motionEvent) != null) {
            return false;
        }

        mSwipeGestureDector.onTouchEvent(motionEvent);
        return isSwiping();
    }

    @Override
    public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

        mSwipeGestureDector.onTouchEvent(motionEvent);

        if (motionEvent.getAction() == MotionEvent.ACTION_UP ||
                motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {

            final boolean reachActionEdge = swipeType == SWIPING_HOR && (Math.abs(mDistanceX) > (mActionEdge > 0 ?
                    mActionEdge : recyclerView.getWidth() / 3));

            boolean reachTabEdge = false;
            if (mSwipeCardRef != null && mSwipeCardRef.get() != null && swipeType == SWIPING_HOR) {
                SwipeCard swipeCard = mSwipeCardRef.get();
                if (swipeCard.getCurrentIndex() == 0 && mDistanceX > 0
                        || (swipeCard.getCurrentIndex() == swipeCard.getTotalPage() - 1) && mDistanceX < 0) {
                    reachTabEdge = true;
                }
            }
            int direction = 1;
            if (swipeType == SWIPING_HOR) {
                direction = mDistanceX > 0 ? 1 : -1;
            } else if (swipeType == SWIPING_VER) {
                direction = mDistanceY > 0 ? 1 : -1;
            }
            resetViews(recyclerView, swipeType, reachActionEdge && !reachTabEdge, direction);

        }
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean b) {

    }

    private void resetViews(RecyclerView recyclerView, final int swipingType, final boolean reachActionEdge, final int direction) {
        if (enableAnim) {
            int contentWidth = recyclerView.getWidth();
            AnimatorSet animatorSet = new AnimatorSet();
            List<Animator> list = new ArrayList<>();
            String translation = "translationX";
            if (swipingType == SWIPING_VER) {
                translation = "translationY";
            }
            for (View view : mChildList) {
                ObjectAnimator animator;
                if (reachActionEdge) {
                    animator = ObjectAnimator.ofFloat(view, translation, contentWidth * direction)
                            .setDuration(ANIMATE_DURATION);
                    animator.setInterpolator(new AccelerateInterpolator());
                } else {
                    animator = ObjectAnimator.ofFloat(view, translation, 0)
                            .setDuration(ANIMATE_DURATION);
                    animator.setInterpolator(new DecelerateInterpolator());
                }
                list.add(animator);
            }
            animatorSet.playTogether(list);
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (swipingType == SWIPING_HOR && reachActionEdge) {
                        if (mSwipeCardRef != null && mSwipeCardRef.get() != null) {
                            SwipeCard swipeCard = mSwipeCardRef.get();
                            swipeCard.switchTo(swipeCard.getCurrentIndex() - direction);
                        }
                    }
                    mChildList.clear();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animatorSet.start();
        } else {
            if (swipingType == SWIPING_HOR && reachActionEdge) {
                if (mSwipeCardRef != null && mSwipeCardRef.get() != null) {
                    SwipeCard swipeCard = mSwipeCardRef.get();
                    swipeCard.switchTo(swipeCard.getCurrentIndex() - direction);
                }
            }
            mChildList.clear();
        }

        if (swipingType == SWIPING_VER) {
            if (pullFromEndListener != null) {
                if (mDistanceY < 0 && (mDistanceY < -pullFromEndListener.getPullEdge())) {
                    pullFromEndListener.onAction();
                } else {
                    pullFromEndListener.onReset();
                }
            }
        }

        swipeType = SWIPING_NONE;
    }

    private VirtualLayoutManager getLayoutManager() {
        return layoutManager;
    }

    private View findFixedChildViewUnder(MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        final List<View> fixedViews = getLayoutManager().getFixedViews();
        int count = fixedViews.size();
        for (int i = count - 1; i >= 0; --i) {
            View child = fixedViews.get(i);
            float translationX = child.getTranslationX();
            float translationY = child.getTranslationY();
            if (x >= (float) child.getLeft() + translationX && x <= (float) child.getRight() + translationX && y >= (float) child.getTop() + translationY && y <= (float) child.getBottom() + translationY) {
                return child;
            }
        }

        return null;
    }

    private View findScrollableChildViewUnder(MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        final int first = getLayoutManager().findFirstVisibleItemPosition();
        final int last = getLayoutManager().findLastVisibleItemPosition();
        for (int i = 0; i <= last - first; i++) {
            View child = getLayoutManager().getChildAt(i);
            if (child instanceof ViewGroup) {
                float translationX = child.getTranslationX();
                float translationY = child.getTranslationY();
                if (x >= (float) child.getLeft() + translationX
                        && x <= (float) child.getRight() + translationX
                        && y >= (float) child.getTop() + translationY
                        && y <= (float) child.getBottom() + translationY) {
                    if (findCanScrollView(child) != null) {
                        return child;
                    }
                }
            }
        }
        return null;
    }

    private View findCanScrollView(View v) {
        if (v instanceof ViewGroup) {
            ViewGroup target = (ViewGroup) v;
            if ((target instanceof UltraViewPager || target instanceof RecyclerView)
                    && target.getVisibility() == View.VISIBLE) {
                return target;
            } else {
                for (int i = 0; i < target.getChildCount(); ++i) {
                    View view = findCanScrollView(target.getChildAt(i));
                    if (view != null) {
                        return view;
                    }
                }
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * @param open true: swipe card when scroll state is idle
     */
    public void setOptMode(boolean open) {
        this.isOptMode = open;
    }

    private static boolean isAttachedToWindow(RecyclerView hostView) {
        if (Build.VERSION.SDK_INT >= 19) {
            return hostView.isAttachedToWindow();
        } else {
            return (hostView.getHandler() != null);
        }
    }

    private static boolean hasAdapter(RecyclerView hostView) {
        return (hostView.getAdapter() != null);
    }

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (RecyclerView.SCROLL_STATE_IDLE == newState
                    && recyclerView != null
                    && lastMotionEvent != null) {
                updateCurrCard();
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    };

    public void updateCurrCard() {
        if (recyclerView == null || lastMotionEvent == null) {
            return;
        }

        View childView = recyclerView.findChildViewUnder(lastMotionEvent.getX(), lastMotionEvent.getY());
        if (childView != null) {
            int position = layoutManager.getPosition(childView);
            currCardIdx = mGroupBasicAdapter.findCardIdxFor(position);
            List<Card> groups = mGroupBasicAdapter.getGroups();

            if (currCardIdx >= groups.size() || currCardIdx < 0) {
                Log.e(TAG, "onScroll: group size >= cardIdx");
                return;
            }

            currCard = groups.get(currCardIdx);
        }
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            if (e1 == null || e2 == null) {
                return false;
            }

            lastMotionEvent = e2;

            mDistanceX = e2.getX() - e1.getX();
            mDistanceY = e2.getY() - e1.getY();

            if (!isOptMode) {
                updateCurrCard();
            }

            if (recyclerView != null && currCard instanceof SwipeCard) {

                SwipeCard swipeCard = (SwipeCard) currCard;

                mSwipeCardRef = new WeakReference<SwipeCard>(swipeCard);

                if (!isSwiping()) {
                    if (Math.abs(distanceX) > Math.abs(distanceY)) {
                        swipeType = SWIPING_HOR;
                    } else if (pullFromEndListener != null
                            && Math.abs(distanceX) < Math.abs(distanceY)
                            && mDistanceY < 0
                            && isReadyToPullFromEnd()) {
                        swipeType = SWIPING_VER;
                    } else {
                        return false;
                    }
                }

                if (swipeType == SWIPING_HOR) {
                    for (int i = 0; i < recyclerView.getChildCount(); i++) {
                        View child = recyclerView.getChildAt(i);
                        int cidx = mGroupBasicAdapter.findCardIdxFor(layoutManager.getPosition(child));
                        if (cidx == currCardIdx) {
                            if (!mChildList.contains(child)) {
                                mChildList.add(child);
                            }
                            final int sign = mDistanceX > 0 ? 1 : -1;
                            if (enableAnim) {
                                child.setTranslationX((float) (sign * 10f * Math.sqrt(Math.abs(mDistanceX))));
                            }
                        }
                    }
                } else if (swipeType == SWIPING_VER && mDistanceY < 0) {
                    for (int i = 0; i < recyclerView.getChildCount(); i++) {
                        View child = recyclerView.getChildAt(i);
                        int cidx = mGroupBasicAdapter.findCardIdxFor(layoutManager.getPosition(child));
                        if (cidx == currCardIdx) {
                            if (!mChildList.contains(child)) {
                                mChildList.add(child);
                            }
                            final int sign = mDistanceY > 0 ? 1 : -1;
                            if (mDistanceY < -pullFromEndListener.getPullEdge()) {
                                pullFromEndListener.onReleaseToAction(mDistanceX, mDistanceY);
                            } else {
                                pullFromEndListener.onPull(mDistanceX, mDistanceY);
                            }
                            if (enableAnim) {
                                child.setTranslationY((float) (sign * 10f * Math.sqrt(Math.abs(mDistanceY))));
                            }
                        }
                    }
                } else {
                    return false;
                }

                return true;
            }

            return false;
        }
    }
}