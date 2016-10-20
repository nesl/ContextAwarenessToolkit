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
from sklearn.metrics import precision_score
from sklearn.metrics import recall_score
from sklearn.metrics import classification_report
from sklearn.metrics import confusion_matrix
from sklearn import cross_validation
from sklearn.cross_validation import cross_val_score
from sklearn.cross_validation import KFold

def Classify(AbstractModule):
	def __init__(self):
		self.model_path = './'
		self.use_top_features = True
	    self.cv = False
	    self.model_prefix = ''
    	self.len_feature = 18
    	self.best_feature_index = (9, 3, 1, 10, 0, 5, 16)
    	self.clf_names = ['rf', 'dt5', 'dt8', 'gsvm', 'lsvm']

	def process(self, data):
	    # Separate feature and label
	    X = np.array(data[:,1:self.len_feature])
	    y = np.array(data[:,self.len_feature].astype(int))

	    # Select only top features
	    if self.use_top_features:
	        # KBest + TreeBased selection
	        X = np.array([itemgetter(self.best_feature_index)(i) for i in X])

	    # # Remove nan data
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
	    X = newX
	    y = newY

	    for clf_name in self.clf_names:
	        print('Classifying trace using trained ' + clf_name)
	        
	        # Load trained models
	        clf = joblib.load(
                os.path.join(
                    self.model_path, 
                    self.model_prefix, 
                    clf_name, '
                    .pkl'
                )
            )

	        # Classify data using trained model
	        yp = clf.predict(X)
	        print(accuracy_score(y, yp))
	        print(precision_score(y, yp, average='weighted'))
	        print(recall_score(y, yp, average='weighted'))
	        print(precision_recall_fscore_support(y, yp))

	        # Perform cross-validations
	        if self.cv:
	            print('10-fold CV...')
	            kf = KFold(len(X), n_folds=10, shuffle=False, random_state=None)
	            count = 1
	            avgacc = 0
	            avgprec = 0
	            avgrecl = 0

	            for train, test in kf:
	                print('Fold #' + str(count))
	                X_train = list(itemgetter(*train)(X))
	                y_train = list(itemgetter(*train)(y))
	                X_test = list(itemgetter(*test)(X))
	                y_test = list(itemgetter(*test)(y))

	                # Train the model using CV data
	                clf.fit(X_train, y_train)
	                yp = clf.predict(X_test)

	                # Calculate metrics for CV
	                avgacc = avgacc + clf.score(X_test, y_test)
	                avgprec = avgprec + precision_score(y_test, yp, average='weighted')
	                avgrecl = avgrecl + recall_score(y_test, yp, average='weighted')
	                count = count + 1

	            # Print metrics from CV data
	            print('cv avg acc. before smooth=' +  str(avgacc / 10.0))
	            print('cv avg prec. before smooth=' +  str(avgprec / 10.0))
	            print('cv avg recl. before smooth=' +  str(avgrecl / 10.0))