import numpy as np
import scipy as sp
import pandas as pd

import math

from inference.AbstractModule import AbstractModule

TIMESTAMP = 'time'
LABEL = 'label'

# Pre-defined feature name
MEAN = 'mean'
STD = 'std'
VAR = 'var'
MAD = 'mad'
RMS = 'rms'
RANGE = 'range'
SKEW = 'skew'
KURT = 'kurt'
MAG = 'mag'

# Pre-defined feature name - configurable
# Append a parameter after the name
QUA = 'qua'
FFT = 'fft'

ACCX = 'accx'
ACCY = 'accy'
ACCZ = 'accz'
ACC = [ACCX, ACCY, ACCZ]


class Feature(AbstractModule):
    def __init__(self, _window_size, _data_columns, _features):
        self.window_size = _window_size
        self.data_columns = _data_columns
        self.features = _features

    def energy(self, column):
        """
        Calculate FFT energy
        """
        F = sp.fft(column)
        return np.multiply(F, sp.conj(F))

    def rms(self, x):
        """
        Calculate RMS
        """
        return np.sqrt(x.dot(x)/x.size)

    def calculate_feature(self, name, sample):
        """
        Perform feature calculation based on the requested name
        """
        if name == TIMESTAMP or name == LABEL:
            return None

        if name == MEAN:
            return sample.mean().mean()
        elif name == STD:
            return sample.std().mean()
        elif name == VAR:
            return sample.var().mean()
        elif name == MAD:
            return sample.mad().mean()
        elif name == SKEW:
            return sample.skew().mean()
        elif name == KURT:
            return sample.kurt().mean()
        elif name == RANGE:
            return (sample.max() - sample.min()).mean()
        elif name == RMS:
            axis_rms = []
            for axis in ACC:
                axis_rms.append(self.rms(sample[axis]))
            return np.mean(axis_rms)
        elif name == MAG:
            temp_mag = 0
            for axis in ACC:
                temp_mag += sample[axis]**2
            return np.mean(np.sqrt(temp_mag))
        elif name.startswith(QUA):
            percentile = int(name[3:]) * 0.25
            return sample.quantile(percentile).mean()
        elif name.startswith(FFT):
            temp_dft = 0
            for axis in ACC: 
                temp_dft += self.energy(sample[axis])
            freq = int(name[3:])
            return np.mean(temp_dft[freq + 1])
        else:
            raise ValueError('Invalid requested feature name: ' + name)

        return feature


    def process(self, data):
        """
        Calculate specified features from a data frame
        """
        # Prepare the data vector
        data.columns = self.data_columns
        if TIMESTAMP in self.data_columns:
            self.data_columns.remove(TIMESTAMP)
        if LABEL in self.data_columns:
            self.data_columns.remove(LABEL)

        # Prepare the feature vector
        feature_columns = list(self.features)
        feature_columns.insert(0, TIMESTAMP)
        feature_columns.append(LABEL)
        feature_vector = pd.DataFrame(columns=feature_columns)

        # number of samples processed
        count = 0

        # number of feature vectors processed
        idx = 0

        # Calculate features over time
        while count < len(data):
            # Group data by window_size and get data
            cur_time = math.floor(data.iloc[count, 0])
            sample = data[(data[TIMESTAMP] >= cur_time) & (
                data[TIMESTAMP] < cur_time + self.window_size)]
            sample_data = sample[self.data_columns]

            # Create feature vector for this sample and set timestamp
            current_feature = []
            current_feature.append(int(cur_time))

            # Calculate each requested feature
            for feature_name in self.features:
                current_feature.append(
                    self.calculate_feature(feature_name, sample_data)
                )

            # Calculate ground truth label (majority in this second)
            label = sample[LABEL].value_counts().idxmax()
            current_feature.append(int(label))
            
            # Append the current feature vector to the entire matrix
            feature_vector.loc[idx] = current_feature

            # Update timestamp and index
            count = count + len(sample)
            idx = idx + 1

        return feature_vector