import numpy as np

import json

from inference import ModuleBase

# Pre-defined preprocessing function name
MOVING_AVG_SMOOTH = 'moving_avg_smooth'

class Preprocess(ModuleBase.ModuleBase):
    module_type = ModuleBase.MOD_PREPROCESS

    def __init__(self, _operators, _window_size, _data_columns):
        self.operators = list(_operators)
        self.window_size = _window_size
        self.data_columns = list(_data_columns)

    def moving_average(self, interval, window_size):
        """
        Smooth data by applying a moving average window
        """
        window = np.ones(int(window_size)) / float(window_size)
        return np.convolve(interval, window, 'same')

    def moving_average_smooth(self, data):
        """
        Smooth a data frame
        """
        data.columns = self.data_columns
        if ModuleBase.TIMESTAMP in self.data_columns:
            self.data_columns.remove(ModuleBase.TIMESTAMP)
        if ModuleBase.LABEL in self.data_columns:
            self.data_columns.remove(ModuleBase.LABEL)
           
        # Smooth data using 5s moving average
        x = data.ix[:,1]
        y = data.ix[:,2]
        z = data.ix[:,3]
        x_av = self.moving_average(x, self.window_size)
        y_av = self.moving_average(y, self.window_size)
        z_av = self.moving_average(z, self.window_size)
        data.ix[:,1] = x_av
        data.ix[:,2] = y_av
        data.ix[:,3] = z_av
        return data

    def perform_preprocess(self, operator, data):
        if operator == MOVING_AVG_SMOOTH:
            return self.moving_average_smooth(data)
        else:
            raise ValueError('Error: unsupported pre-processing operation.')

    def process(self, data):
        """
        Perform all requested pre-processing operations
        """
        for operator in self.operators:
            data = self.perform_preprocess(operator, data)
        return data

    def export(self):
        """
        Export this module to json
        """
        result = {
            ModuleBase.WINDOW_SIZE: self.window_size,
            ModuleBase.DATA_COLUMN: self.data_columns,
            ModuleBase.OPERATOR: self.operators
        }
        return json.dumps(result)
