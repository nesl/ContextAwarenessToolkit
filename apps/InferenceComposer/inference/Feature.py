import numpy as np
import scipy as sp
import pandas as pd

def Feature(AbstractModule):
    def __init__(self):
        # TODO: make data columns and features configurable
        self.data_columns = ['time', 'accx', 'accy', 'accz', 'label']
        self.features = ([
            'time', 'mean', 'std', 'var', 'mad', 
            'rms', 'range', 'skew', 'kurt', 'mag',
            'qua1', 'qua2', 'qua3', 'fft1', 'fft2', 
            'fft3', 'fft4', 'fft5', 'label'
        ])

    def energy(self, column):
        """
        Calculate FFT energy
        """
        F = sp.fft(column)
        return np.multiply(F, sp.conj(F))

    def rms(x):
        """
        Calculate RMS
        """
        return np.sqrt(x.dot(x)/x.size)

    def process(self, data):
        """
        Calculate specified features from a data frame
        """
        data.columns = self.data_columns
        features = pd.DataFrame(columns=self.features)
        # number of samples processed
        count = 0
        # number of feature vectors processed
        idx = 0
        total_sec = 0

        # Calculate features over time
        while count < len(data):
            if count % 100 == 0:
                print('Processing ' + str(count) + '/' + str(len(data)))
            cur_time = math.floor(data.iloc[count, 0])
            sample = data[(data['time'] >= cur_time) & (
                data['time'] < cur_time + window_size)]

            # Set timestamp
            f_time = cur_time
            
            # Calculate features
            accs = sample[['accx', 'accy', 'accz']]
            f_mean = accs.mean().mean()
            f_std = accs.std().mean()
            f_var = accs.var().mean()
            f_mad = accs.mad().mean()
            f_rms = np.mean(
                [rms(accs['accx']), rms(accs['accy']), rms(accs['accz'])])
            f_range = (accs.max() - accs.min()).mean()
            f_skew = accs.skew().mean()
            f_kurt = accs.kurt().mean()
            f_mag = np.mean(
                np.sqrt(accs['accx']**2 + accs['accy']**2 + accs['accz']**2))
            f_qua1 = accs.quantile(0.25).mean()
            f_qua2 = accs.quantile(0.5).mean()
            f_qua3 = accs.quantile(0.75).mean()
            t_dft = energy(accs['accx'])
            f_fft = np.zeros(5)
            for i in range(0, min([5, len(t_dft) - 1])):
                f_fft[i] = np.mean(t_dft[i + 1])

            # Calculate ground truth label (majority in this second)
            label = sample['label'].value_counts().idxmax()

            # Create feature vector for this sample
            feature = []
            feature.append(int(f_time))
            feature.append(f_mean)
            feature.append(f_std)
            feature.append(f_var)
            feature.append(f_mad)
            feature.append(f_rms)
            feature.append(f_range)
            feature.append(f_skew)
            feature.append(f_kurt)
            feature.append(f_mag)
            feature.append(f_qua1)
            feature.append(f_qua2)
            feature.append(f_qua3)
            for i in range(0, 5):
                feature.append(f_fft[i])
            feature.append(int(label))
            
            # Append the current feature vector to the matrix
            features.loc[idx] = feature

            # Update time and count
            count = count + len(sample)
            idx = idx + 1
        
       return features