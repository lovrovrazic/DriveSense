# Driving behaviour recognition project
## Tilen Babič


Dataset was obtained from the following article: [How Smartphone Accelerometers Reveal Aggressive Driving Behavior?--The Key Is the Representation](https://ieeexplore.ieee.org/document/8764567)   
[Dataset link](https://www.accelerometer.xyz/datasets/)

### Models 
- extracted data: preprocessed data from dataset
- android: tensorflow lite CNN model, portable to the Android platform
- models: CNN, LSTM, Autoencoder + Random Forest

### Efficiency
- data_events: extracted events from our own recordings (normal and aggressive)
- efficiency_thresholds: distributions for each event type with calculated normal and aggresive thresholds
