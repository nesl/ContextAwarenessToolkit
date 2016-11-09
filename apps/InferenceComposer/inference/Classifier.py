import os
import numpy as np
from sklearn.tree import DecisionTreeClassifier
from sklearn.svm import SVC
from sklearn.ensemble import RandomForestClassifier
from sklearn.externals import joblib
from sklearn.cross_validation import KFold
from operator import itemgetter

from inference.AbstractModule import AbstractModule
from inference import Feature
from util import remove_nan, show_accuracy


class Classifier(AbstractModule):
    def __init__(
            self,
            _save_model=False,
            _model_path=None,
            _use_top_features=False,
            _top_features=None,
            _test_index=None, 
            _show_report=False,
            _cross_validation=False,
            _cv_fold=10):
        self.save_model = _save_model
        self.model_path = _model_path
        self.use_top_features = _use_top_features
        self.top_features = _top_features
        self.test_index = _test_index
        self.show_report = _show_report
        self.cross_validation = _cross_validation
        self.cv_fold = _cv_fold

        self.classifiers = {}


    def add_default_classifiers(self):
        """
        Use the default set of classifiers for training,
        i.e. automatic mode.
        """
        self.classifiers = {
            'dt_depth5': DecisionTreeClassifier(max_depth=5), \
            'dt_depth8': DecisionTreeClassifier(max_depth=8), \
            'rf_64x6': RandomForestClassifier(n_estimators=64, max_features=6), \
            'svm_rbf': SVC(kernel='rbf', gamma=0.5) , \
            'svm_linear': SVC(kernel='linear', C=0.01)
        }


    def add_classifier(self, clf_name, clf):
        """
        Manually add custom classifiers for training,
        i.e. advanced mode.
        """
        self.classifiers[clf_name] = clf


    def prepare_data(self, data):
        """
        Separate features from labels and remove NaN in a data frame
        """
        # Separate feature and label
        len_feature = len(data.columns) - 2
        print(len_feature)
        X = np.array(data.ix[:, 1:len_feature+1]).astype(float)
        y = np.array(data.ix[:, len_feature+1]).astype(int)

        # Select only top features
        if self.use_top_features:
            X = np.array([itemgetter(self.top_features)(i) for i in X])

        # Remove NAN in feature vectors
        X, y = remove_nan(X, y)

        # Return results
        return X, y


    def run_cross_validation(self, classifier, X, y, fold=10, show_report=False):
        """
        Perform cross-validation and returns result
        """
        print('-- ', fold, '-fold cross validation:')
        kf = KFold(
            len(X), 
            n_folds=fold, 
            shuffle=False, 
            random_state=None
        )
        count = 1
        avgacc = 0
        avgprec = 0
        avgrecl = 0

        for train, test in kf:
            print('--- Fold #' + str(count))
            X_train = list(itemgetter(*train)(X))
            y_train = list(itemgetter(*train)(y))
            X_test = list(itemgetter(*test)(X))
            y_test = list(itemgetter(*test)(y))

            # Train the model using CV data
            classifier.fit(X_train, y_train)
            yp = classifier.predict(X_test)

            # Calculate metrics for CV
            acc, prec, recl = show_accuracy(y_test, yp)
            avgacc += acc
            avgprec += prec
            avgrecl += recl
            count += 1

        # Print metrics from CV data
        if show_report:
            print('---Avg accuracy: ', avgacc / 10.0)
            print('---Avg precision: ', avgprec / 10.0)
            print('---Avg recall: ', avgrecl / 10.0)


    def process(self, data):
        """
        Train a set of classifiers on a feature vector
        """
        # Check that data has timestamp and label
        if (Feature.LABEL not in data.columns or
                Feature.TIMESTAMP not in data.columns):
            raise ValueError('Error: invalid feature vector format.')

        # Check that we have a set of classifiers to train
        if len(self.classifiers) == 0:
            raise ValueError('Error: no valid classifier specified.')

        X_train, y_train = self.prepare_data(data.ix[0:self.test_index, :])

        # Train using different classifiers
        for name, classifier in self.classifiers.items():
            print('*** Training model with ' + name + ' ***')

            # Train the model
            classifier.fit(X_train, y_train)

            # Display training performance for this classifier
            if self.show_report:
                print('- Training performance: ')
                show_accuracy(y_train, classifier.predict(X_train))

                # Run classifier on test data
                if self.test_index is not None:
                    print('- Testing performance: ')
                    X_test, y_test = self.prepare_data(
                        data.ix[self.test_index:, :]
                    )
                    show_accuracy(y_test, classifier.predict(X_test))

            # Perform cross validation
            if self.cross_validation:
                self.run_cross_validation(
                    classifier,
                    X_train, 
                    y_train, 
                    self.cv_fold, 
                    self.show_report
                )

            # Dump the model to a file if necessary
            if self.save_model:
                joblib.dump(
                    classifier, 
                    os.path.join(self.model_path, name + '.pkl')
                )

        # Return the list of trained classifiers
        return self.classifiers