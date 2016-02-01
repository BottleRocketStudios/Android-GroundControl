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

import android.text.TextUtils;
import android.util.Log;

import com.bottlerocketstudios.groundcontrol.AgentExecutor;
import com.bottlerocketstudios.groundcontrol.agent.AbstractAgent;
import com.bottlerocketstudios.groundcontrol.agent.Agent;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;
import com.bottlerocketstudios.groundcontrol.policy.AgentPolicy;
import com.bottlerocketstudios.groundcontrol.tether.AgentTether;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a convenient one-stop API for GroundControl which allows easier utilization of AgentPolicy,
 * AgentTether, and pulls all elements for managing UI listener state into one place instead of putting
 * that load on the UI classes.
 *
 * <p>
 *     A typical implementation in the UI will only need to call the {@link GroundControl#uiAgent(Object, Agent)}
 *     method to create the ExecutionBuilder and the {@link GroundControl#onDestroy} lifecycle method
 *     to avoid potential memory leaks and too-late delivery for listeners in dead UI.
 * </p>
 *
 * <p>
 *     A typical implementation on a background thread or other object with a non-UI lifecycle will be
 *     to call the {@link GroundControl#agent(Agent)} method to create the ExecutionBuilder.
 * </p>
 */
public class GroundControl {
    private static final String TAG = GroundControl.class.getSimpleName();

    private static final ConcurrentHashMap<String, ExecutionBuilderFactory> sExecutionBuilderFactoryMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, UiInformationContainer> sUiInformationContainerMap = new ConcurrentHashMap<>();

    private static String sDefaultAgentExecutorId = AgentExecutor.DEFAULT_AGENT_EXECUTOR_ID;
    private static boolean sDefaultAgentExecutorSet;

    /**
     * You can optionally supply a new ExecutionBuilderFactory for the specified AgentExecutor.
     */
    public static void setExecutionBuilderFactory(String agentExecutorId, ExecutionBuilderFactory executionBuilderFactory) {
        sExecutionBuilderFactoryMap.put(agentExecutorId, executionBuilderFactory);
    }

    private static ExecutionBuilderFactory getExecutionBuilderFactory(String agentExecutorId) {
        ExecutionBuilderFactory executionBuilderFactory = sExecutionBuilderFactoryMap.get(agentExecutorId);
        if (executionBuilderFactory == null) {
            Log.i(TAG, "Created a new default instance of the StandardExecutionBuilderFactory for AgentExecutorId " + agentExecutorId);
            executionBuilderFactory = createDefaultExecutionBuilderFactory(agentExecutorId);
        }
        return executionBuilderFactory;
    }

    /**
     * Guarantee that only one instance of the ExecutionBuilderFactory is built per agentExecutorId.
     */
    private static ExecutionBuilderFactory createDefaultExecutionBuilderFactory(String agentExecutorId) {
        synchronized (sExecutionBuilderFactoryMap) {
            ExecutionBuilderFactory executionBuilderFactory = sExecutionBuilderFactoryMap.get(agentExecutorId);
            if (executionBuilderFactory == null) {
                executionBuilderFactory = new StandardExecutionBuilderFactory(agentExecutorId);
                sExecutionBuilderFactoryMap.put(agentExecutorId, executionBuilderFactory);
            }
            return executionBuilderFactory;
        }
    }

    private static UiInformationContainer getUiInformationContainer(String agentExecutorId) {
        UiInformationContainer uiInformationContainer = sUiInformationContainerMap.get(agentExecutorId);
        if (uiInformationContainer == null) {
            Log.i(TAG, "Created a new default instance of the StandardExecutionBuilderFactory for AgentExecutorId " + agentExecutorId);
            uiInformationContainer = createDefaultUiInformationContainer(agentExecutorId);
        }
        return uiInformationContainer;
    }

    /**
     * Guarantee that only one instance of the UiInformationContainer is built per agentExecutorId.
     */
    private static UiInformationContainer createDefaultUiInformationContainer(String agentExecutorId) {
        synchronized (sUiInformationContainerMap) {
            UiInformationContainer uiInformationContainer = sUiInformationContainerMap.get(agentExecutorId);
            if (uiInformationContainer == null) {
                uiInformationContainer = new UiInformationContainer();
                sUiInformationContainerMap.put(agentExecutorId, uiInformationContainer);
            }
            return uiInformationContainer;
        }
    }

    /**
     * Register a new policy in the policy map for the default agent executor.
     *
     * @see GroundControl#setDefaultAgentExecutorId(String)
     */
    public static void registerPolicy(String policyIdentifier, AgentPolicy policy) {
        registerPolicy(sDefaultAgentExecutorId, policyIdentifier, policy);
    }

    /**
     * Register a new policy in the specified AgentExecutor.
     */
    public static void registerPolicy(String agentExecutorId, String policyIdentifier, AgentPolicy policy) {
        getExecutionBuilderFactory(agentExecutorId).registerPolicy(policyIdentifier, policy);
    }

    /**
     * Get the policy with the supplied id for the default agent executor.
     *
     * @see GroundControl#setDefaultAgentExecutorId(String)
     */
    public static AgentPolicy getPolicy(String policyIdentifier) {
        return getPolicy(sDefaultAgentExecutorId, policyIdentifier);
    }

    /**
     * Get the policy with the supplied id for the specified AgentExecutor.
     */
    public static AgentPolicy getPolicy(String agentExecutorId, String policyIdentifier) {
        return getExecutionBuilderFactory(agentExecutorId).getPolicy(policyIdentifier);
    }

    /**
     * Create an ExecutionBuilder for the supplied agent using the default agent executor. Used only for
     * non-UI implementations.
     *
     * <p><strong>Note: In most cases you should use bgAgent or uiAgent methods.</strong></p>
     *
     * @see GroundControl#setDefaultAgentExecutorId(String)
     */
    public static <ResultType, ProgressType> ExecutionBuilder<ResultType, ProgressType> agent(Agent<ResultType, ProgressType> agent) {
        return agent(sDefaultAgentExecutorId, agent);
    }

    /**
     * Create an ExecutionBuilder for the supplied agent on the specified AgentExecutor. Used only for
     * non-UI implementations.
     *
     * <p><strong>Note: In most cases you should use bgAgent or uiAgent methods.</strong></p>
     */
    public static <ResultType, ProgressType> ExecutionBuilder<ResultType, ProgressType> agent(String agentExecutorId, Agent<ResultType, ProgressType> agent) {
        return getExecutionBuilderFactory(agentExecutorId).createForAgent(agent);
    }

    /**
     * Create an ExecutionBuilder from inside of another Agent using the supplied AgentExecutor. Used only for
     * non-UI implementations. Supply the agentExecutor provided to the calling Agent with {@link Agent#setAgentExecutor(AgentExecutor)} method.
     * If your Agent extends {@link com.bottlerocketstudios.groundcontrol.agent.AbstractAgent} this is retained
     * for you so you can use {@link AbstractAgent#getAgentExecutor()}.
     */
    public static <ResultType, ProgressType> ExecutionBuilder<ResultType, ProgressType> bgAgent(AgentExecutor agentExecutor, Agent<ResultType, ProgressType> agent) {
        return agent(agentExecutor.getId(), agent);
    }

    /**
     * Create an ExecutionBuilder attached the uiObject for the supplied agent using the default AgentExecutor.
     * If you use this method you must also use {@link GroundControl#onDestroy(Object)}
     *
     * @see GroundControl#setDefaultAgentExecutorId(String)
     */
    public static <ResultType, ProgressType> ExecutionBuilder<ResultType, ProgressType> uiAgent(Object uiObject, Agent<ResultType, ProgressType> agent) {
        return uiAgent(sDefaultAgentExecutorId, uiObject, agent);
    }

    /**
     * Create an ExecutionBuilder attached the uiObject for the supplied agent on the specified AgentExecutor.
     * If you use this method you must also use {@link GroundControl#onDestroy(String, Object)}
     */
    public static <ResultType, ProgressType> ExecutionBuilder<ResultType, ProgressType> uiAgent(String agentExecutorId, Object uiObject, Agent<ResultType, ProgressType> agent) {
        return getExecutionBuilderFactory(agentExecutorId).createForAgent(agent).ui(uiObject);
    }

    /**
     * This method will reattach to a one-time operation or deliver its pre-existing result that was not acknowledged
     * by the last UI object which was waiting on it. You must call {@link GroundControl#onOneTimeCompletion}
     * in your listener when the one time operation has completed. This method uses the default AgentExecutor {@link GroundControl#setDefaultAgentExecutorId(String)}
     *
     * <p><strong>Note: This is typically placed in onCreate or onActivityCreated.</strong></p>
     *
     * @param uiObject              A Fragment or Activity. You must call {@link GroundControl#onDestroy} in your onDestroy.
     * @param oneTimeIdentifier     A unique string that identifies the operation you are resuming.
     *                              This must be unique across all potentially concurrent operations on the same AgentExecutor.
     * @param listener              A listener for the agent that is being reattached.
     * @param <ResultType>          Type of result delivered by the agent
     * @param <ProgressType>        Type of progress delivered by the agent
     * @return                      An ExecutionBuilder with the policy pre-populated from the
     *                              previous execution. In most cases, simply call .execute() on the builder.
     */
    public static <ResultType, ProgressType> AgentTether reattachToOneTime(Object uiObject, String oneTimeIdentifier, AgentListener<ResultType, ProgressType> listener) {
        return reattachToOneTime(sDefaultAgentExecutorId, uiObject, oneTimeIdentifier, listener);
    }

    /**
     * Reattach to one time execution using the specified AgentExecutor.
     * Full documentation {@link GroundControl#reattachToOneTime(Object, String, AgentListener)}
     */
    public static <ResultType, ProgressType> AgentTether reattachToOneTime(String agentExecutorId, Object uiObject, String oneTimeIdentifier, AgentListener<ResultType, ProgressType> listener) {
        OneTimeInformation oneTimeInformation = getUiInformationContainer(agentExecutorId).restoreOneTimeInformation(oneTimeIdentifier);
        if (oneTimeInformation != null) {
            return getExecutionBuilderFactory(agentExecutorId).createForReattach(uiObject, oneTimeIdentifier, oneTimeInformation.getAgentIdentifier(), listener, oneTimeInformation.getAgentPolicy()).execute();
        }
        return null;
    }

    /**
     * Must be called by {@link ExecutionBuilder#execute()} to update the UIInformationContainer with the latest tether for this agent/UI combination.
     */
    static void updateUiInformationContainer(String agentExecutorId, Object uiObject, AgentTether agentTether, AgentPolicy agentPolicy, String oneTimeId) {
        UiInformationContainer uiInformationContainer = getUiInformationContainer(agentExecutorId);
        if (!TextUtils.isEmpty(oneTimeId)) {
            uiInformationContainer.storeOneTimeInfo(oneTimeId, agentTether.getAgentIdentifier(), agentPolicy);
        }
        uiInformationContainer.storeTether(uiObject, agentTether);
    }

    /**
     * Always call this method when you are destroying your UI which will release all tethers to
     * avoid delivering results to listeners in dead UI components and prevent temporary listener leaks.
     * This method uses the default AgentExecutor {@link GroundControl#setDefaultAgentExecutorId(String)}
     *
     * @param uiObject The current Activity or Fragment.
     */
    public static void onDestroy(Object uiObject) {
        onDestroy(sDefaultAgentExecutorId, uiObject);
    }

    /**
     * Reattach to one time execution using the specified AgentExecutor.
     * Full documentation {@link GroundControl#onDestroy(Object)}
     */
    public static void onDestroy(String agentExecutorId, Object uiObject) {
        getUiInformationContainer(agentExecutorId).onDestroy(uiObject);
    }

    /**
     * You must call this method when completing a one-time operation. This prevents attempted re-attach
     * and redelivery of results when the result has already been consumed. This method uses the default
     * AgentExecutor {@link GroundControl#setDefaultAgentExecutorId(String)}
     */
    public static void onOneTimeCompletion(String oneTimeIdentifier) {
        onOneTimeCompletion(sDefaultAgentExecutorId, oneTimeIdentifier);
    }

    /**
     * Full documentation {@link GroundControl#onOneTimeCompletion(String)}
     */
    public static void onOneTimeCompletion(String agentExecutorId, String oneTimeIdentifier) {
        getUiInformationContainer(agentExecutorId).onOneTimeCompletion(oneTimeIdentifier);
    }

    /**
     * Determine the DefaultAgentExecutorId to be used on calls that do not specify an AgentExecutorId.
     */
    public static String getDefaultAgentExecutorId() {
        return sDefaultAgentExecutorId;
    }

    /**
     * Modify the DefaultAgentExecutorId so that all subsequent calls will use the corresponding AgentExecutor.
     * This must be called once and not modified later at runtime to avoid confusion in a multi-threaded
     * environment. An exception will be thrown if it is called more than once with different IDs. Calling
     * repeatedly with the same ID will not cause an exception, but may indicate a bad design. Use the
     * methods in this class which take an explicit AgentExecutorId parameter if you need to use more
     * than one AgentExecutor in the same application.
     */
    public static synchronized void setDefaultAgentExecutorId(String defaultAgentExecutorId) {
        if (sDefaultAgentExecutorSet && !TextUtils.equals(sDefaultAgentExecutorId, defaultAgentExecutorId)) {
            throw new IllegalStateException("The DefaultAgentExecutorId can only be set once. If you need more than one, specify that ID per method call.");
        }
        sDefaultAgentExecutorSet = true;
        sDefaultAgentExecutorId = defaultAgentExecutorId;
    }
}
