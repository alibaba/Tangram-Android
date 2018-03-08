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

/**
 * Created by villadora on 15/8/31.
 */
public interface ITimer {
    /**
     * start timer
     */
    void start();

    /**
     * @param bySecond true, start timer with interval alignment; false, start timer immediately
     */
    void start(boolean bySecond);

    /**
     * pause timer
     */
    void pause();


    /**
     * restart timer
     */
    void restart();

    /**
     * stop timer
     */
    void stop();

    /**
     * cancel timer
     */
    void cancel();

    /**
     * @return timer status
     */
    TimerStatus getStatus();

    /**
     * Timer status
     */
    enum TimerStatus {

        Waiting(0, "Wating"),
        Running(1, "Running"),
        Paused(-1, "Paused"),
        Stopped(-2, "Stopped");

        private int code;
        private String desc;

        private TimerStatus(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

}
