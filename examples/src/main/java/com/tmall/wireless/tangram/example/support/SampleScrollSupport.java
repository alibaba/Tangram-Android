package com.tmall.wireless.tangram.example.support;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;

import java.util.ArrayList;
import java.util.List;

public class SampleScrollSupport {
    public interface IScrollListener {
        public void onScrollStateChanged(RecyclerView recyclerView, int newState);

        public void onScrolled(RecyclerView recyclerView, int dx, int dy);
    }

    private RecyclerView recyclerView;

    private List<IScrollListener> scrollListeners = new ArrayList<>();

    public SampleScrollSupport(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        recyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                for (IScrollListener listener : scrollListeners) {
                    listener.onScrollStateChanged(recyclerView, newState);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                for (IScrollListener listener : scrollListeners) {
                    listener.onScrolled(recyclerView, dx, dy);
                }
            }
        });
    }

    public void register(IScrollListener scrollListener) {
        if (!scrollListeners.contains(scrollListener)) {
            scrollListeners.add(scrollListener);
        }
    }
}
