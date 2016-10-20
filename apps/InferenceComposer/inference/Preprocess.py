import numpy as np

class Preprocess(AbstractModule):
    def __init__(self):
        # Default window size: 1s
        self.window_size = 1

        # TODO: make columns configurable
        self.columns = ['time', 'accx', 'accy', 'accz', 'label']

    def moving_average(self, interval, window_size):
        """
        Use a window of +/-0.5 seconds
        """
        window = np.ones(int(window_size))/float(window_size)
        return np.convolve(interval, window, 'same')

    def smooth(self, data):
        """
        Smooth a data frame using a moving average window
        """
        data.columns = self.columns
           
        # Smooth data using 5s moving average
        x = data.ix[:,1]
        y = data.ix[:,2]
        z = data.ix[:,3]
        x_av = self.moving_average(x, 5)
        y_av = self.moving_average(y, 5)
        z_av = self.moving_average(z, 5)
        data.ix[:,1] = x_av
        data.ix[:,2] = y_av
        data.ix[:,3] = z_av
        return data

    def process(self, data):
        return self.smooth(data)