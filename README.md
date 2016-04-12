Ground Control - Android
============

### Purpose
This library provides a means to tackle the most common tasks faced on Android:

*   Keep non-UI work off of the UI thread.
*   Coalesce the same operation so that work only happens once and many can be notified.
*   Provide a means to easily create dependencies between operations. 
*   Callback on the UI thread or a background thread if so desired.
*   Continue work in the background through UI configuration changes without leaking the UI.
*   Deliver a temporarily cached result after UI rotation without re-requesting it. 
*   Deliver progress indication for long-running, partially complete operations.

### Sample Application
The sample application demonstrates some common usage for GroundControl. 

1. Product Corp wants an app based on existing data.  
1. There is a Configuration JSON file which provides a base URL and a path for the Version JSON file. 
1. The Version JSON file references the latest Region JSON. 
1. The Region JSON references three regional Product JSON files for the US, CA and DEFAULT. 
1. Users should be shown the correct region based on their physical location.

This yields the following dependency graph.		

	   +-------------+     +-------+     +------+
	   |Configuration| <---+Version| <---+Region+<-------+
	   +-------------+     +-------+     +------+        |
	          ^  ^                           |           |
	          |  +---------------------------+        +--------+
	          +---------------------------------------+Products|
	                                                  +--------+
	                                                     |
	   +--------+     +-------+     +------------+       |
	   |Location| <---+Geocode| <---+Country Code| <-----+
	   +--------+     +-------+     +------------+

* Version depends on Configuration. 
* Region depends on Version and Configuration. 
* Geocode depends on Location. 
* Country Code depends on Geocode.
* Products depends on Country Code, Configuration and Region. 

In the Sample app the operation to obtain each of these elements is created as an Agent. A consumer of these pieces of information could start any individual Agent and the Agent will resolve its dependencies before performing its own operation and delivering the result. Ground Control ensures that though many components require Configuration and try to obtain it directly by running the Configuration Agent, the simultaneous requests are coalesced to one operation and delivered many times. 

The goal in designing Agents is that the consumer of the information doesn't know anything about how it is obtained. MainActivity only knows the Agent that will give it the latest, local product list. If another screen requires this list, just reuse the Agent. If two fragments are on screen at once and both require the same information, those requests will be coalesced. 

### Components

*   GroundControl - A single point of contact to initiate all agent executions. 
    *   Manage tether retention and release for UI callback agents.
    *   Manage one-time execution for UI callback agents.
    *   Manage a repository for default AgentPolicy instances.
    *   Make building upon standard policies easier. 
    *   Helps put rails on common usage patterns. 
    *   If you call .uiAgent(this, agent) then you must call .onDestroy(this) where *this* is an instance of a UI element.
*   Agent - A unit of work that has some agency to determine what its blocking operations may be before delivering a result or progress indication.
    *   AbstractAgent handles some of the basic plumbing. Extend from it in most cases.
    *   DependencyHandlingAgent is useful if your Agent will depend on multiple other Agents completing before it can start.
    *   The interface defines several methods:
        *   getUniqueIdentifier() - The most important. Identifies a unique Agent globally. Typically YourAgentImplementation.class.getCanonicalName() [+ unique variables] will do fine. That ensures that the Agent is uniquely identified with the option of adding information about the specific task. For example: FetchArticleAgent.class.getCanonicalName() + String.valueOf(articleId). If you parameterize the unique identifier, keep it in a (final) member variable so that you don't do string concatenation over and over again. 
        *   cancel() - This method is called when the Agent has not delivered a result within the allowed time or all clients that requested work have told you to cancel. Wrap it up and notify of failure.
        *   getCancelTimeoutMs() - This is the amount of time in milliseconds to allow the agent to run before calling cancel() on it. This is separate from the AgentPolicy's policyTimeout, this should be a value large enough to complete under most circumstances. 
        *   getRunTimeoutMs() - This must be greater than the cancel timeout. This is called to set an absolute maximum amount of time for the run() operation to continue before its thread is interrupted.
        *   getMaximumTimeoutMs() - This must be greater than the run timeout. At this point the AgentExecutor has told it to cancel and tried to interrupt it. If it is still around, it will be dereferenced to (hopefully) free up resources. If you reach this timeout, you have really messed up. 
        *   setAgentListener() - This sets the listener to be called with progress or completion events. This is not the listener supplied when executing the Agent. This is a listener instance that will be used to route the result to the actual listener on the caller's thread of choice.
        *   setAgentExecutor() - This sets the instance of the AgentExecutor this agent is running on. It will be set before run and should be used for any further Agents spawned by this Agent via GroundControl.bgAgent(getAgentExecutor(), ...). This allows your Agent to be run on multiple AgentExecutors. 
        *   onProgressUpdateRequested() - A new listener has come along for this ongoing process, notify your listener of progress. 
        *   run() - Agent extends Runnable. This method will be called when it is time to do your operation. This will always be on a background thread pool. 
    *   If your Agent is a listener for another Agent and you specify parallelBackgroundCallback in the AgentPolicy, you will have parallelCallbackTimeoutMs to complete any work in your AgentListener callbacks and deliver a result or start another agent before being interrupted.
    *   If your Agent is a listener for another Agent and you specify a background looper to receive your callback, you block all other work on that looper until you finish your AgentListener callbacks and deliver a result or start another agent. Don't take forever.
*   AgentListener - Your callback interface. 
    *   onComplete will be called when the Agent has completed work and will deliver no further messages. 
    *   onProgress will be called when the Agent has updated progress to report. If the agent is build to respond to onProgressUpdateRequested, you should receive a quick progress update message for an already ongoing task.
*   AgentTether - A tether that will allow you to release or cancel a running operation. 
    *   Canceling an Agent via Tether that is in progress and has other interested listeners will not cause it to cancel unless all other Tethers cancel it. A cancel includes a release.
    *   Released Agents will continue until completion, but will not notify the associated listener. 
    *   If nothing has a strong reference to a tether, the cached data associated with it will be dereferenced after a defined, short interval. The GroundControl.uiAgent() and GroundControl.onDestroy() methods handle storing and releasing AgentTethers. 
*   AgentPolicy - A policy that is sent with the Agent indicating desired behavior specific to the requestor.
    *   If you omit the AgentPolicy in a request to AgentExecutor.runAgent, the default AgentPolicy will be used. If this has not been overridden, it will deliver on the UI thread and use sensible defaults.
    *   A policy will specify the following attributes
        *   callbackLooperId - Looper on which to call back your listener. This should be either the UI looper or the AgentExecutor's background looper. The background looper is useful for database transactions as they happen FIFO serially.
        *   parallelBackgroundCallback - Call all listeners (up to a limit) simultaneously on individual background threads. This precludes the use of callbackLooperId.
        *   parallelCallbackTimeoutMs - Time in milliseconds before the parallel callback operation's thread will be interrupted. A listening Agent should start a new Agent or deliver their result in this amount of time. 
        *   policyTimeoutMs - Time in milliseconds before the listener associated with this policy should be notified of failure.
        *   maxCacheAgeMs - Time in milliseconds during which to consider volatile in-memory cache to be valid before re-running an Agent. For example while a screen is still being displayed and rotated keep sending the same info for 2 minutes. Then refetch. If multiple policies are submitted for the same agent, the longest duration will be used. However, individual policies will use their individually specified time to determine a cache miss and may refetch data and cache it. If this value is 0, no in-memory caching will occur.
        *   bypassCache - Skip the cache for this request, always execute the agent. If maxCacheAgeMs > 0, the result will still be cached for future requests. 
        *   clearCache - Clear the cache with this request, includes bypass cache. If maxCacheAgeMs > 0, the result will still be cached for future requests. This is useful when you not only want to bypass the cache for this request, but also need to prevent future requests from using that old cached value while this operation is running. e.g. You know you have modified some data on the server and do not want to get the old data back again.
        *   jobPriority - The priority with which to dequeue this operation and its listeners. If multiple policies are supplied for the same Agent that is in queue for execution, the highest priority will override lower priorities.
    *   StandardAgentPolicyBuilder - Use this class to build new instances of an AgentPolicy. 
    *   AgentPolicies can be reused for multiple Agent executions, you should keep often used AgentPolicies around to reduce GC churn.
*  AgentExecutor - The workhorse of Agent scheduling and delivery. Most implementations do not need to interface directly with AgentExecutor, as this is handled by the GroundControl class.
    *   AgentExecutor keeps a thread-safe map of instances if you want to create your own, otherwise use getDefault();
    *   Highly compose-able using its builder to replace various components and set defaults. However, the default implementation should be fine for most applications. 
    *   Callbacks from Agents are guaranteed to be asynchronous even on immediate cache hit.
    *   The cache is designed for use with UI configuration change survival only. Use other means to cache information long-term. To this end the GroundControl ExecutionBuilder will forbid caching on background operations.  

### Usage
Check out the GroundControlSample project to get a working demonstration of how to use the library.

Add the jcenter repository and include the library in your project with the compile directive in your dependencies section of your build.gradle.

```gradle
repositories {
    ...
    jcenter()
}

...

dependencies {
    ...
    compile 'com.bottlerocketstudios:groundcontrol:1.1.3'
}
```

In rare cases where you need to pull a snapshot build to help troubleshoot the develop branch, snapshots are hosted by JFrog. **You should not ship a release using the snapshot library** as the actual binary referenced by snapshot is going to change with every build of the develop branch.

```gradle
repositories {
   ...
   jcenter()
   maven {
      url "https://oss.jfrog.org/artifactory/oss-snapshot-local"
   }
}
 
dependencies {
   ...
   compile 'com.bottlerocketstudios:groundcontrol:1.1.4-SNAPSHOT'
}
```

### Sample Usage
The samples below alternate between inline anonymous listeners and listeners defined as fields in the parent class. This is done to illustrate two (of many) different ways of doing it. Whatever fits your team is the best way to do it.

#### Normal Data Fetch and Display
This simple example illustrates the use of an Agent "MyAgent" that will return a Boolean result and deliver Float progress indication. The return type and the progress types are simple here for demonstration purposes.

```java
com/.../ui/MyFragment.java

public class MyFragment extends Fragment {        
    ...
    @Override
    onDestroy() {
        //This should be in your BaseFragment
        GroundControl.onDestroy(this);
    }
    ...
    @Override
    onStart() {
        startMyAgent();
    }
    
    private void startMyAgent() {
        mProgress.show();
        GroundControl.uiAgent(this, new MyAgent(getActivity()))
                .uiCallback(new AgentListener<Boolean, Float>() {
                    @Override
                    public void onCompletion(String agentIdentifier, Boolean success) {
                        mProgress.hide();
                        Toast.makeText(getActivity(), "Success: " + success, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onProgress(String agentIdentifier, Float progress) {
                        mProgress.setIndeterminate(false);
                        mProgress.setMax(100);
                        mProgress.setProgress(Math.round(progress * 100.0f));
                    }
                })
                .execute();
    }
}

com/.../agent/MyAgent.java

public class MyAgent extends AbstractAgent<Boolean, Float> {
    
    private final Context mContext;
    private float mProgress;
    private boolean mCancelled;

    public MyAgent(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public String getUniqueIdentifier() {
        return MyAgent.class.getCanonicalName();
    }

    @Override
    public void cancel() {
        mCancelled = true;
    }

    @Override
    public void onProgressUpdateRequested() {
        notifyProgress();
    }
    
    private notifyProgress() {
        getAgentListener().onProgress(getUniqueIdentifier(), mProgress);            
    }

    @Override
    public void run() {
        boolean success = false;
        ...
        while (!mCancelled) {
            //Do some time consuming iterative work then notify 50% complete. 
            mProgress = 0.5f;
            notifyProgress();
        }
        ...                
        //Work is over notify completion.                
        getAgentListener().onComplete(getUniqueIdentifier(), success && !mCancelled);
    }
}
```
        
#### One-time data post
This example uses the oneTime facility of GroundControl to manage reattach after configuration change for a long running one-time operation. This could be submission of authentication credentials, a form, a purchase, or other one-time write operation. File uploads should be done with a Service that has an ongoing Notification. 

```java
com/.../ui/MyLoginFragment.java

public class MyLoginFragment extends Fragment {        

    private static final String ONE_TIME_AUTHENTICATION = MyLoginFragment.class.getCanonicalName() + ".oneTimeAuthentication";
    ...
    @Override
    onDestroy() {
        //This should be in your BaseFragment
        GroundControl.onDestroy(this);
    }
    ...
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Reattach to the ongoing (or not) one-time operation hitting the cache if it was completed during rotation.
        GroundControl.reattachToOneTime(this, ONE_TIME_AUTHENTICATION, mLoginAgentListener);
    }
    
    private void startLogin(String username, String password) {
        mProgress.show();
        
        /*
         * Execute the login, forcing a bypass of cache and tagging the execution as
         * a one-time operation which can be reattached if the device is rotated. 
         */
        GroundControl.uiAgent(this, new MyAgent(getActivity(), username, password))
            .uiCallback(mLoginAgentListener)
            .oneTime(ONE_TIME_AUTHENTICATION)
            .bypassCache(true)
            .execute();
    }
    
    //FunctionalAgentListener has a built in empty onProgress implementation for convenience. 
    AgentListener<Boolean, Void> mLoginAgentListener = new FunctionalAgentListener<Boolean, Void>() {
        @Override
        public void onCompletion(String agentIdentifier, Boolean success) {
            //discard reference to this one time agent in-progress.
            GroundControl.onOneTimeCompletion(ONE_TIME_AUTHENTICATION);
            mProgress.hide();
            Toast.makeText(getActivity(), "Success: " + success, Toast.LENGTH_LONG).show();
        }
    };
}

com/.../agent/MyLoginAgent.java

public class MyLoginAgent extends AbstractAgent<Boolean, Void> {
    
    private final Context mContext;
    private final String mUsername;
    private final String mPassword;
    private boolean mCancelled;
    
    public MyLoginAgent(Context context, String username, String password) {
        mContext = context.getApplicationContext();
        mUsername = username;
        mPassword = password;
    }

    @Override
    public String getUniqueIdentifier() {
        return MyLoginAgent.class.getCanonicalName();
    }

    @Override
    public void cancel() {
        mCancelled = true;
    }

    @Override
    public void onProgressUpdateRequested() {}
    
    @Override
    public void run() {
        boolean success = false;
        ...
        success = LoginThing.doLogin(mUsername, mPassword);
        ...                
        //Work is over notify completion.                
        getAgentListener().onComplete(getUniqueIdentifier(), success && !mCancelled);
    }
}
```

#### Dependent Agent Execution
Agents can depend on other agents to do their work. Here work is handed off to another agent and we wait for that agent to complete and deliver a result before proceeding. If you have multiple dependencies, look at the Sample app usage of DependencyHandlingAgent.

**IMPORTANT** All execution paths must call call onComplete on the AgentListener or the Agent will timeout and eventually deliver a null result. 

```java
public class MyStoreFinderAgent extends AbstractAgent<StoreCollection, Void> {
    
    private final Context mContext;
    private boolean mCancelled;
                
    public MyStoreFinderAgent(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public String getUniqueIdentifier() {
        return MyStoreFinderAgent.class.getCanonicalName();
    }

    @Override
    public void cancel() {
        mCancelled = true;
    }

    @Override
    public void onProgressUpdateRequested() {}
    
    @Override
    public void run() {
        GroundControl.bgAgent(getAgentExecutor(), new LocationAgent())
            .bgParallelCallback(new FunctionalAgentListener<Location, Void> {
                @Override
                public void onCompletion(String agentIdentifier, Location location) {
                    StoreCollection storeCollection = null;
                    if (location != null) {
                        StoreCollection storeCollection = getStoreCollection(location);
                    }
                    getAgentListener().onCompletion(getUniqueIdentifier(), storeCollection);
                }
            })
            .execute();
    }
    
    private StoreCollection getStoreCollection(Location location) {
        //Whatever exchanges a location for a StoreCollection.
        return theStoreCollectionForLocation;
    }
    
}
```

#### Customizing Policy
It is now much easier to do a one-off modification of the policy before execution. This allows you to customize attributes of the policy without having to use a AgentPolicyBuilder instance to create a new policy. You must supply the policy explicitly or callback via uiCallback/bg*Callback beforehand as those will default to the correct baseline policy. Policies are immutable so the supplied policy itself cannot be modified, it will be the basis for a new instance. 

```java
//Executing a UI agent with a really fast do or die deadline of 2 seconds. 
//If we don't get a result by then, consider it a failure.
GroundControl.uiAgent(this, new MyAgent())
    .uiCallback(mMyListener)
    .timeout(TimeUnit.seconds.toMillis(2))
    .execute();
    
//Skipping the cache because we know the cached data is invalid. It is a builder
//so we can keep the same 2 second deadline addition if we wanted to. 
GroundControl.uiAgent(this, new MyAgent())
    .uiCallback(mMyListener)
    .timeout(TimeUnit.seconds.toMillis(2))
    .bypassCache(true)
    .execute();

//Supply an explicit policy then modify it to bypass cache and callback on the UI handler. 
GroundControl.uiAgent(this, new MyAgent())
    .policy(myFavoritePolicy)
    .uiCallback(mMyListener)
    .bypassCache(true)
    .execute();
```
            
#### Customizing Global Policies
GroundControl will automatically build sensible policies that will callback on the UI Looper, a background ThreadPool, or the AgentExecutor's background Looper. You may also supply per AgentExecutor overrides for these default policies. 

**IMPORTANT** These policies should be updated once in the onCreate of your application object. Updating the defaults at runtime will break expectation for other objects. See above to create one-time policies. 
        
```java
//Create the policies then register them
GroundControl.registerPolicy(AgentPolicyCache.POLICY_IDENTIFIER_UI, uiPolicy);  
GroundControl.registerPolicy(AgentPolicyCache.POLICY_IDENTIFIER_BG_SERIAL, bgSerialPolicy);    
GroundControl.registerPolicy(AgentPolicyCache.POLICY_IDENTIFIER_BG_PARALLEL, bgParallelPolicy);    

//Later these policies are automatically selected with these methods on the ExecutionBuilder
.uiPolicy()
.bgSerialPolicy()
.bgParallelPolicy()

//Or if no policy is explicitly selected and these methods are called on the ExecutionBuilder
.uiCallback()
.bgSerialCallback()
.bgParallelCallback()
```

You may also create your own policies that are not part of the defaults

```java
//Create a policy that always bypasses cache by default. Do this from the Application object.
public class MyGroundControlConfiguration {
	public static final String MY_POLICY_NAME = "myPolicy";

	public static void initialize() {
		AgentPolicy myPolicy = (new StandardAgentPolicyBuilder()).setBypassCache(true).build();
		GroundControl.registerPolicy(MY_POLICY_NAME, myPolicy);
	}
}

//Later use that policy by name anywhere in the app. 
GroundControl.uiAgent(this, new MyAgent())
	.policy(MyGroundControlConfiguration.MY_POLICY_NAME)
	.uiCallback(mMyListener)
	.execute();
```

#### Customizing AgentExecutor
The AgentExecutor can be customized quite a bit using the AgentExecutorBuilder. With it you can customize (or not) just about every aspect of the AgentExecutor. This is an advanced topic and almost anything can be achieved with the combination of AgentPolicy and custom Agent implementations. If you think you need to customize this, be sure that the AgentPolicy or some custom Agent would not work. 

```java
//Create a new Agent Executor, call this from your Application's onCreate
public class MyGroundControlConfiguration {
	public static final String MY_AGENT_EXECUTOR = "myAgentExecutor";

	public static void initialize() {
		//AgentTethers need to be built better for some reason.
		AgentExecutor myAgentExecutor = (new AgentExecutorBuilder())
				.setAgentTetherBuilder(new MyAgentTetherBuilder())
				.build();
		
		//Register the new AgentExecutor
		AgentExecutor.setInstance(MY_AGENT_EXECUTOR, myAgentExecutor);

		/*
		 * Make it so that this is the default for the GroundControl tool.
		 * You can call this method many times with the same agentExecutorId.
		 * However, if you call it again with a different id, it will throw
		 * an exception and reject the change because it can break expectations
		 * elsewhere in the app. 
		 */
		GroundControl.setDefaultAgentExecutorId(MY_AGENT_EXECUTOR);
	}
}
```

You may also have a one-off AgentExecutor for some really specific special purpose that has not yet been imagined. To that end, the GroundControl tool and everything really, is built to support as many AgentExecutors as the system will support, each identified by a unique String identifier. 

```java
//Expanding on the example above, if the GroundContorl.setDefaultAgentExecutorId 
//were not called and the default remained the typical default. You could use 
//the overrides on GroundControl to use that specific AgentExecutor.

GroundControl.uiAgent(MyGroundControlConfiguration.MY_AGENT_EXECUTOR,
						this, 
						new MyAgent())
			 .uiCallback(mMyListener)
			 .execute();
```

### Build
This project must be built with gradle. 

*   Version Numbering - The version name should end with "-SNAPSHOT" for non release builds. This will cause the resulting binary, source, and javadoc files to be uploaded to the snapshot repository in Maven as a snapshot build. Removing snapshot from the version name will publish the build on jcenter. If that version is already published, it will not overwrite it.
*   Execution - To build this libarary, associated tasks are dynamically generated by Android build tools in conjunction with Gradle. Example command for the production flavor of the release build type: 
    *   Build and upload: `./gradlew --refresh-dependencies clean uploadArchives`
    *   Build only: `./gradlew --refresh-dependencies clean jarRelease`
