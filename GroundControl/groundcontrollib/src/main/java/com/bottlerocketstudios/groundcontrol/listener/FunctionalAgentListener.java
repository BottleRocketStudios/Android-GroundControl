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
 * A listener that will act like a Functional Interface in that you only implement one method. Use this
 * when you do not care about progress events.
 */
public abstract class FunctionalAgentListener<ResultType, ProgressType> implements AgentListener<ResultType, ProgressType> {

    @Override
    public void onProgress(String agentIdentifier, ProgressType progress) {
        //This space left intentionally blank.
    }
}
