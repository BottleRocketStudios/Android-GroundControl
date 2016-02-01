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
 * Interface to clients of the InactivityCleanupRunnable
 */
public interface InactivityCleanupListener {
    /**
     * Return true if you currently have work in progress.
     */
    boolean isBusy();

    /**
     * Perform any cleanup actions required to stop working for now.
     */
    void enterIdleState();

    /**
     * Perform cleanup operations on an interval.
     */
    void performCleanup();
}
