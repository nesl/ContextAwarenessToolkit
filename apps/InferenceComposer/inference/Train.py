import os
import numpy as np
import scipy as sp
import pandas as pd
from sklearn import tree
from sklearn.svm import SVC, LinearSVC
from sklearn.ensemble import ExtraTreesClassifier, RandomForestClassifier
from sklearn.externals import joblib
from sklearn import metrics
from sklearn.metrics import accuracy_score
from sklearn.metrics import precision_recall_fscore_support
from operator import itemgetter

def Train(AbstractModule):
    def __init__(self):
        self.use_top_features = True
        self.model_prefix = ''
        self.model_path = './'
        self.len_data = 85027
        self.len_feature = 18
        self.clfs = {
            'dt5': tree.DecisionTreeClassifier(max_depth=5), \
            'dt8': tree.DecisionTreeClassifier(max_depth=8), \
            'rf': RandomForestClassifier(n_estimators=64, max_features=6), \
            'lsvm': SVC(kernel='rbf', gamma=0.5) , \
            'gsvm': LinearSVC(C=0.01, penalty='l1', dual=False)
        }
        self.best_feature_index = (9, 3, 1, 10, 0, 5, 16)

    def process(self, data):      
        # Separate feature and label
        X = np.array(data[0:self.len_data, 1:self.len_feature])
        y = np.array(data[0:self.len_data, self.len_feature].astype(int))

        # Select only top features
        if self.use_top_features:
            # KBest + TreeBased selection
            X = np.array([itemgetter(self.best_feature_index)(i) for i in X])

        # Remove nan data
        newX = []
        newY = []
        for i in range(0, len(X)):
            lst = X[i]
            lbl = y[i]
            flag = True
            for i in lst:
                if np.isnan(i):
                    flag = False
                    print('Warning: nan detected in data.')
                    break
            if flag:
                newX.append(lst)
                newY.append(lbl)
        X = np.array(newX)
        y = np.array(newY)

        # Dump loaded data to file
        print(X.shape)
        print(y.shape)            

        # Train using different classifiers
        for clf_name, clf in self.clfs.iteritems():
            print('Training model using ' + clf_name)

            # Train the model and dump to file
            clf.fit(X, y)
            joblib.dump(
                clf, 
                os.path.join(
                    self.model_path, 
                    self.model_prefix, 
                    clf_name, '
                    .pkl'
                )
            )

            # Get initial performance number for this classifier
            yp = clf.predict(X)
            print(accuracy_score(y, yp))
            print(precision_recall_fscore_support(y, yp))