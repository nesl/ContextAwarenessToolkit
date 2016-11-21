## Data Collector
This is an Android app for helping developers collect a set of sensor data from users, including the ground truth label. 

### Usage
The basic workflow of Data Collector is described as follows:
- Data Collector provides a set of APIs for app developers to specify the set of sensors to be considered, including device types, sensor types, sensor configurations, as well as ground truth configurations.
- Target users then install Data Collector, which collects data based on the developer’s configuration. The app runs on the target user’s phone, continuously sampling sensor data and periodically querying ground truth labels (if necessary) until the desired amount of data has been collected.
- Finally, Data Collector outputs the data in a serialized or structured human-readable format, and developers can save the data for later use.

### Implementation
Data Collector is implemented as an Android app. It provides a `DataCollectionConfigurator` for developers to configure a data collection and then calls a background service `DataCollectionService` for sampling and saving sensor data as a `DataVector`. The ground truth label collection is implemented using the Android Alarm and BroadcastReceiver and periodically queries labels from users.

### API Example
```
// Configure type of sensor data to collect
DataCollectionConfigurator configurator =
    new DataCollectionConfigurator();
configurator.addSensorTypeToVector(
    DeviceType.ANDROID_PHONE,
    SensorType.ANDROID_ACCELEROMETER);
configurator.addSensorTypeToVector(
    DeviceType.ANDROID_PHONE,
    SensorType.ANDROID_GRAVITY);

// Configure label collection
LabelType labelType = new LabelType(
    "ground_truth",
    LabelDataType.NOMINAL);
    labelType.setInterval(10);
    labelType.addCandidateNominalValue("Activity A");
    labelType.addCandidateNominalValue("Activity B");
    labelType.addCandidateNominalValue("Activity C");

// Pass Configurator to DataCollectionService
DataCollectionService.configureDataCollection(
    DeviceType.ANDROID_PHONE,
    configurator.getDataVector());

// Start the data collection
DataCollectionService.startDataCollection();

// Stop the data collection and obtain the data as CSV
DataVector result = DataCollectionService.stopDataCollection();
result.dumpAsCSV("path_to_data.csv");
```
The above example uses Data Collector’s APIs to configure a data collection. Developers invoke the configurator and specify the types of sensor data to be collected. They then request labels of nominal type (strings) to be collected and specify the possible candidate strings. Finally, developers use the Data Collection Service to start the data collection, and write the collected data to a CSV file after stopping the collection.
