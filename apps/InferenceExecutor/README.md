## Inference Executor
Inference Executor is an Android app for actualizing an inference pipeline generated by Inference Composer and optimizing its execution across devices. The runtime loads a saved inference pipeline from a JSON file, including loading classifiers from PMMLs, and instantiates the pipeline using Java implementations of inference modules available in a module library. The runtime partitions modules in a pipeline and considers different devices as targets for the execution. In this work, we consider the specific case of executing an inference between a smartwatch and a smartphone.

### Design
- An `InferenceManager` (mobile or wear) runs on each device and is responsible for coordinating and managing the execution between devices. 
- On each device, `InferenceManager` invokes an `InferenceExecutor` to load an `InferencePipeline` from a JSON file, and to instantiate required `InferenceModule` based on loaded parameter as well as existing modules in the current library. 
- `InferenceExecutor` also serves as a runtime container for performing the actual sensing and inference workloads.
- A `CommunicationManager` coordinates the communication between devices, including but not limited to controlling module placements, managing sensor sampling, sending notifications, etc. 
- Specifically, a dedicated part called `WatchDog` monitors other devices for connectivity and availability to support runtime optimizations.

In the case of watch-phone coordination, the `MobileInferenceManager` running on a smartphone loads an `InferencePipeline` and splits it into a phone part and a watch part. It then uses the `CommunicationManager` to send the watch part to the `WearInferenceManager`. Both managers then coordinates the execution of this inference.

### Implementation
Inference Executor uses the Android Alarm and BroadcastReceiver for periodic inference execution. Inference pipelines are parsed using the JSON library of Java, and we use the [jpmml-evaluator](https://github.com/jpmml/jpmml-evaluator) open-source library to parse and evaluate the PMML classification models exported by Inference Composer. Finally, the communication between devices is achieved using Android’s Wear API, which opportunistically uses either Bluetooth LE of WiFi for data transmission.

Similar to Inference Composer, Inference Executor has a set of default inference modules in its Java module library. It also provides an interface for creating new modules.

To perform an inference in Inference Executor, app developers specify a pointer to the JSON pipeline file, the interval and duration of the current inference execution, and an optimization goal described next.

## API Example
See https://github.com/nesl/ContextAwarenessToolkit/blob/master/apps/InferenceExecutor/executor_mobile/src/main/java/edu/ucla/nesl/toolkit/executor/MainActivity.java
and 
https://github.com/nesl/ContextAwarenessToolkit/blob/master/apps/InferenceExecutor/executor_mobile/src/main/java/edu/ucla/nesl/toolkit/executor/MobileInferenceManager.java

### Runtime Optimization
Inference Executor supports the following optimization goal:
- Phone only: Always perform the inference on a smartphone.
- Watch only: Always perform the inference on a smartwatch.
- Maximize coverage: Start the entire inference on a smartwatch and use a phone to monitor the connectivity and significant motions on the watch. Upon the detection of watch not moving or disconnected, move the entire inference to phone. This approach maximizes the inference accuracy by using whichever device available at a time.
- Minimize transmission (work in progress): Start with a random module partition across the two devices. Monitor the amount of data transferred between devices. If the data rate is greater than a certain threshold, re-partition the inference pipeline and restart the inference execution.This approach minimizes resource usage by reducing the amount of remote data transfer.

