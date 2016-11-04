import numpy as np

from inference.AbstractModule import AbstractModule

class Preprocess(AbstractModule):
    def __init__(self, _window_size, _data_columns):
        self.window_size = _window_size
        self.columns = _data_columns

    def moving_average(self, interval, window_size):
        """
        Smooth data by applying a moving average window
        """
        window = np.ones(int(window_size)) / float(window_size)
        return np.convolve(interval, window, 'same')

    def smooth(self, data):
        """
        Smooth a data frame
        """
        data.columns = self.columns
           
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

    def process(self, data):
        return self.smooth(data)