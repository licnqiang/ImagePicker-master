/*******************************************************************************
 * Copyright 2011-2014 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.nostra13.universalimageloader.utils;

import com.nostra13.universalimageloader.core.ImageLoader;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

import java.util.logging.Logger;

/**
 * L
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.6.4
 */
public final class L {

    private static final String LOG_FORMAT = "%1$s\n%2$s";
    private static volatile boolean writeDebugLogs = false;
    private static volatile boolean writeLogs = true;

    private L() {
    }

    /**
     * Enables logger (if {@link #disableLogging()} was called before)
     *
     * @deprecated Use {@link #writeLogs(boolean) writeLogs(true)} instead
     */
    @Deprecated
    public static void enableLogging() {
        writeLogs(true);
    }

    /**
     * Disables logger, no logs will be passed to LogCat, all log methods will do nothing
     *
     * @deprecated Use {@link #writeLogs(boolean) writeLogs(false)} instead
     */
    @Deprecated
    public static void disableLogging() {
        writeLogs(false);
    }

    /**
     * Enables/disables detail logging of {@link ImageLoader} work.
     * Consider {@link L#disableLogging()} to disable
     * ImageLoader logging completely (even error logs)<br />
     * Debug logs are disabled by default.
     *
     * @param writeDebugLogs write debug logs
     */
    public static void writeDebugLogs(boolean writeDebugLogs) {
        L.writeDebugLogs = writeDebugLogs;
    }

    /**
     * Enables/disables logging of {@link ImageLoader} completely (even error logs).
     *
     * @param writeLogs write logs
     */
    public static void writeLogs(boolean writeLogs) {
        L.writeLogs = writeLogs;
    }

    /**
     * D *
     *
     * @param message message
     * @param args    args
     */
    public static void d(String message, Object... args) {
        if (writeDebugLogs) {
            log(HiLog.DEBUG, null, message, args);
        }
    }

    /**
     * log
     *
     * @param message message
     * @param args    args
     */
    public static void i(String message, Object... args) {
        log(HiLog.INFO, null, message, args);
    }

    /**
     * W *
     *
     * @param message message
     * @param args    args
     */
    public static void w(String message, Object... args) {
        log(HiLog.WARN, null, message, args);
    }

    /**
     * E *
     *
     * @param ex ex
     */
    public static void e(Throwable ex) {
        log(HiLog.ERROR, ex, null);
    }

    /**
     * E *
     *
     * @param message message
     * @param args    args
     */
    public static void e(String message, Object... args) {
        log(HiLog.ERROR, null, message, args);
    }

    /**
     * E *
     *
     * @param ex      ex
     * @param message message
     * @param args    args
     */
    public static void e(Throwable ex, String message, Object... args) {
        log(HiLog.ERROR, ex, message, args);


    }

    private static void log(int priority, Throwable ex, String message, Object... args) {
        if (!writeLogs) return;
        if (args.length > 0) {
            message = String.format(message, args);
        }

        String log;
        if (ex == null) {
            log = message;
        } else {
            String logMessage = message == null ? ex.getMessage() : message;
            String logBody = HiLog.getStackTrace(ex);
            log = String.format(LOG_FORMAT, logMessage, logBody);
        }
        HiLogLabel label = new HiLogLabel(priority, 0x00201, "MY_TAG");
        HiLog.info(label, ImageLoader.TAG, log);
    }

    /**
     * Info *
     *
     * @param msg msg
     */
    public static void info(String msg) {
        Logger.getGlobal().info("jwen====" + msg);
    }

}