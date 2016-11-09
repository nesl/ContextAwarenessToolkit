import pandas as pd
from inference.Preprocess import Preprocess
from inference.Feature import Feature
from inference.Classifier import Classifier
from util import export_model

DATA_PATH = (
		'external_data/data_collection_new_2016/'
		'hl_classifier/labelled_acc/'
	)
FILENAME = DATA_PATH + 'all_data_acc_50hz.label.acc'
MODEL_PATH = (
		'external_data/data_collection_new_2016/'
		'hl_classifier/inference_composer_model_dump/'
	)
MODEL_PMML_PATH = (
		'external_data/data_collection_new_2016/'
		'hl_classifier/inference_composer_model_pmml/'
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
	feature_vector = feature_calculator.process(processed_data[:50000])
	feature_mapper = feature_calculator.get_mapper()
	print(feature_vector.shape)

	# Training with default classifiers
	classifier = Classifier(
		_save_model=False,
	    _model_path=MODEL_PATH,
	    _use_top_features=False,
	    _top_features=None,
	    _test_index=600, 
        _show_report=True,
        _cross_validation=False,
        _cv_fold=10
	)
	classifier.add_default_classifiers()
	trained_classifiers = classifier.process(feature_vector)
	print(len(trained_classifiers))

	# Export models as PMML
	for name, classifier in trained_classifiers.items():
		export_model(
			name, 
			classifier, 
			feature_mapper, 
			MODEL_PMML_PATH
		)


if __name__ == "__main__":
    compose_inference()