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

package com.bottlerocketstudios.groundcontrol.listener;

/**
 * Runnable to deliver progress to AgentListener
 */
public class ListenerProgressRunnable<ProgressType> implements Runnable {

    private final AgentListener<?, ProgressType> mListener;
    private final ProgressType mProgress;
    private final String mAgentIdentifier;

    public ListenerProgressRunnable(String agentIdentifier, AgentListener<?, ProgressType> listener, ProgressType progress) {
        mAgentIdentifier = agentIdentifier;
        mListener = listener;
        mProgress = progress;
    }

    @Override
    public void run() {
        mListener.onProgress(mAgentIdentifier, mProgress);
    }
}
