# Ground Control Changelog #

*   1.1.4 - Create single method call to disable cache on UI AgentPolicy.
*	1.1.3 - Open Source Release
*   1.1.1 - Bugfix
    *   Fix temporarily leaking most recent AgentListener.
*   1.1.0 - GroundControl static API
    *   Create a single point of contact with the AgentExecutor, AgentPolicy and UI lifecycle helper components.
*   1.0.0 - First Release
    *   Allow developer to specify arbitrary background thread priority when composing system.
    *   Ensure that background loopers run at developer specified thread priority. 
    *   Ensure HandlerCache is threadsafe.
*   0.9.9 - RC 3
    *   Set thread prioritization to match AsyncTask background operation priority.
*   0.9.8 - RC 2
    *   Add ability to clear a previously cached value for current and all future requests.
*   0.9.7 - RC 1
    *   Fix special case where reentrant code modified a synchronized set during iteration. Create unit test for same.
    *   Fix thread-local cache issue with unit test.
    *   Enforce background callbacks bypass cache.
*   0.9.6 - Beta 4
    *   Range check on AbstractAgent
    *   Concurrent dequeue prevention
*   0.9.5 - Beta 3
    *   Fixed API < 19 issue not reported by IDE.
    *   Clean up warnings and minor issues.
    *   Synchronized several iterations over lists/keys.
*   0.9.4 - Beta 2
    *   Fixed another concurrency issue 
    *   Code clean up
*   0.9.3 - Beta 1
    *   Fixed some concurrency issues
    *   Added easy ability to reattach to an ongoing one-time operation with UIOneTimeAgentHelper
    *   Improved throughput in high volume situations
    *   Created unit tests to spam from multiple simultaneous threads
    *   Sample application works for upload and display of Imgur images
*   0.9.2 - Alpha 3
    *   AgentPolicyBuilder did not return builder instance if setting parallelBackgroundTimeoutMs
    *   Sample application has more Imgur operations
*   0.9.1 - Alpha 2
    *   The AgentExecutor an Agent is running on is provided to the Agent to use if spawning other Agents
    *   Sample application adding OAuth2.0 demonstration (Imgur API)
*   0.9.0 - Alpha 1
    *   Initial internal release
    *   Sample app supports complicated Coca-Cola Freestyle brand fetch and extract operation
