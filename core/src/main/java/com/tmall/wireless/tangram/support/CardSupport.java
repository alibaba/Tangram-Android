/*
 * MIT License
 *
 * Copyright (c) 2017 Alibaba Group
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

package com.tmall.wireless.tangram.support;

import com.alibaba.android.vlayout.layout.FixAreaLayoutHelper;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.structure.entitycard.EntityCard;

import android.view.View;
import android.widget.ImageView;

/**
 * A helper class supports binding background to a card
 * <br />
 * Created by mikeafc on 16/9/16.
 */
public abstract class CardSupport {

    /**
     * the callback called when binding an image to card's background, this help you to do custom binding logic
     * @param layoutView an imageView instance
     * @param card the card has a background image
     */
    public abstract void onBindBackgroundView(View layoutView, Card card);

    /**
     * the callback called when unbinding an image to card's background, usually it is the time of the card scrolled out of screen. This help you to do custom unbinding logic     * @param layoutView an imageView instance
     * @param layoutView an imageView instance
     * @param card the card has a background image
     */
    public void onUnbindBackgroundView(View layoutView, Card card) {

    }

    /**
     * @param imageView
     * @param card
     */
    public void onBindBackgroundView(ImageView imageView, EntityCard card) {

    }

    /**
     * provide a appearAnimator for FixCard, sample animator is as follow:
     *
     * <pre>

     new FixViewAnimatorHelper() {
                 @Override
                 public ViewPropertyAnimator onGetFixViewAppearAnimator(View fixView) {
                     int height = fixView.getMeasuredHeight();
                     fixView.setTranslationY(-height);
                     return fixView.animate().translationYBy(height).alpha(1.0f).setDuration(500);
                 }

                 @Override
                 public ViewPropertyAnimator onGetFixViewDisappearAnimator(View fixView) {
                     int height = fixView.getMeasuredHeight();
                     return fixView.animate().translationYBy(-height).alpha(0.0f).setDuration(500);
                 }
             }

     * </pre>

     * @param card
     */
    public FixAreaLayoutHelper.FixViewAnimatorHelper onGetFixViewAppearAnimator(Card card) {
        return null;
    }

}
