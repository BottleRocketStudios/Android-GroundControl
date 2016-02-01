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

package com.bottlerocketstudios.groundcontrol.convenience;

import com.bottlerocketstudios.groundcontrol.policy.AgentPolicy;
import com.bottlerocketstudios.groundcontrol.tether.AgentTether;
import com.bottlerocketstudios.groundcontrol.tether.UIAgentTetherCollection;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Holder for UI related information for a single AgentExecutor.
 */
public class UiInformationContainer {
    private final WeakHashMap<Object, UIAgentTetherCollection> mUIAgentTetherCollectionMap = new WeakHashMap<>();
    private final Map<String, OneTimeInformation> mOneTimeInformationMap = new HashMap<>();

    public void storeTether(Object uiObject, AgentTether agentTether) {
        UIAgentTetherCollection uiAgentTetherCollection = mUIAgentTetherCollectionMap.get(uiObject);
        if (uiAgentTetherCollection == null) {
            uiAgentTetherCollection = new UIAgentTetherCollection();
            mUIAgentTetherCollectionMap.put(uiObject, uiAgentTetherCollection);
        }
        uiAgentTetherCollection.addTether(agentTether);
    }

    public void onDestroy(Object uiObject) {
        UIAgentTetherCollection uiAgentTetherCollection = mUIAgentTetherCollectionMap.get(uiObject);
        if (uiAgentTetherCollection != null) {
            uiAgentTetherCollection.destroy();
            mUIAgentTetherCollectionMap.remove(uiObject);
        }
    }

    public void storeOneTimeInfo(String oneTimeIdentifier, String agentIdentifier, AgentPolicy agentPolicy) {
        mOneTimeInformationMap.put(oneTimeIdentifier, new OneTimeInformation(agentIdentifier, agentPolicy));
    }

    public OneTimeInformation restoreOneTimeInformation(String oneTimeIdentifier) {
        return mOneTimeInformationMap.get(oneTimeIdentifier);
    }

    public void onOneTimeCompletion(String oneTimeIdentifier) {
        mOneTimeInformationMap.remove(oneTimeIdentifier);
    }
}
