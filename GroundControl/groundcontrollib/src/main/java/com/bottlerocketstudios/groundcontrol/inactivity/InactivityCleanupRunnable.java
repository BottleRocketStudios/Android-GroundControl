/*
 * Copyright (c) 2016. Bottle Rocket LLC
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bottlerocketstudios.groundcontrol.inactivity;

/**
 * Self-posting Runnable that will perform callbacks to a listener to cause it to enter a prolonged
 * idle state or to cleanup stale information.
 */
public interface InactivityCleanupRunnable extends Runnable {

    /**
     * Store a reference to the provided listener for future notifications.
     */
    void setListener(InactivityCleanupListener inactivityCleanupListener);

    /**
     * Stop running updates.
     */
    void stop();

    /**
     * Restart the idle timeout for the associated listener.
     */
    void restartTimer();

    /**
     * Start running cleanup on high speed interval
     */
    void enterHighSpeedMode();

    /**
     * Start running cleanup on normal interval
     */
    void exitHighSpeedMode();

    /**
     * Return true if high speed processing is underway.
     */
    boolean isHighSpeedMode();
}
