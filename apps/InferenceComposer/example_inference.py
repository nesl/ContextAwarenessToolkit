import pandas as pd
from inference.Preprocess import Preprocess
from inference.Feature import Feature
from inference.Classifier import Classifier

DATA_PATH = (
		'external_data/data_collection_new_2016/'
		'hl_classifier/labelled_acc/'
	)
FILENAME = DATA_PATH + 'all_data_acc_50hz.label.acc'
MODEL_PATH = (
		'external_data/data_collection_new_2016/'
		'hl_classifier/inference_composer_model_dump/'
	)


def compose_inference(filename=FILENAME):
	# Read sensor raw data from a file
	raw_data = pd.read_csv(filename, header=1)

	# Data preparation
	raw_data_columns = ['time', 'accx', 'accy', 'accz', 'label']
	features = [
            'mean', 'std', 'var', 'mad', 
            'rms', 'range', 'skew', 'kurt', 'mag', 
            'qua1', 'qua2', 'qua3', 'fft1', 'fft2',
            'fft3', 'fft4', 'fft5'
    ]

	# Pre-processing
	pre_processor = Preprocess(
		_window_size=5,
		_data_columns=raw_data_columns
	)
	processed_data = pre_processor.process(raw_data)
	print(processed_data.shape)

	# Feature calculation
	feature_calculator = Feature(
		_window_size=1,
		_data_columns=raw_data_columns,
		_features=features
	)
	feature_vector = feature_calculator.process(processed_data[:])
	print(feature_vector.shape)

	# Training with default classifiers
	classifier = Classifier(
		_save_model=True,
	    _model_path=MODEL_PATH,
	    _use_top_features=False,
	    _top_features=None,
	    _test_index=60000, 
        _show_report=True,
        _cross_validation=True,
        _cv_fold=10
	)
	classifier.add_default_classifiers()
	trained_classifiers = classifier.process(feature_vector)
	print(len(trained_classifiers))

if __name__ == "__main__":
    compose_inference()


def export_model():
	"""
	Placeholder for scikit model -> PMML -> Java/Android runtime

	Code taken from the example in https://github.com/jpmml/sklearn2pmml
	"""
	# #
	# # Step 1: feature engineering
	# #

	# from sklearn.datasets import load_iris
	# from sklearn.decomposition import PCA

	# from sklearn2pmml.decoration import ContinuousDomain

	# import pandas
	# import sklearn_pandas

	# iris = load_iris()

	# iris_df = pandas.concat((pandas.DataFrame(iris.data[:, :], columns = ["Sepal.Length", "Sepal.Width", "Petal.Length", "Petal.Width"]), pandas.DataFrame(iris.target, columns = ["Species"])), axis = 1)

	# iris_mapper = sklearn_pandas.DataFrameMapper([
	#     (["Sepal.Length", "Sepal.Width", "Petal.Length", "Petal.Width"], [ContinuousDomain(), PCA(n_components = 3)]),
	#     ("Species", None)
	# ])

	# iris = iris_mapper.fit_transform(iris_df)

	# #
	# # Step 2: training a logistic regression model
	# #

	# from sklearn.linear_model import LogisticRegressionCV

	# iris_X = iris[:, 0:3]
	# iris_y = iris[:, 3]

	# iris_classifier = LogisticRegressionCV()
	# iris_classifier.fit(iris_X, iris_y)

	# #
	# # Step 3: conversion to PMML
	# #

	# from sklearn2pmml import sklearn2pmml

	# sklearn2pmml(iris_classifier, iris_mapper, "LogisticRegressionIris.pmml", with_repr = True)