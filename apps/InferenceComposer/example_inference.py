import pandas as pd

import json

from inference import ModuleBase
from inference import Preprocess
from inference import Feature
from inference import Classifier
from util import export_inference

DATA_PATH = 'external_data/inference_composer/data/'
DATA_FILENAME = DATA_PATH + 'all_data_acc_50hz.label.acc'
MODEL_PATH = 'external_data/inference_composer/model/'
INF_FILENAME = MODEL_PATH + 'inference_pipeline.json'


def compose_inference_helper(raw_data, data_columns, features):
	"""
	Helper function to construct an inference pipeline
	"""
	# Pre-processing
	pre_processor = Preprocess.Preprocess(
		_window_size=1,
		_data_columns=data_columns,
		_operators=[Preprocess.MOVING_AVG_SMOOTH]
	)
	processed_data = pre_processor.process(raw_data)

	# Feature calculation
	feature_calculator = Feature.Feature(
		_window_size=1,
		_data_columns=data_columns,
		_features=features
	)
	feature_vector = feature_calculator.process(processed_data[0:50000])

	# Training with default classifiers
	classifiers = Classifier.Classifier(
		_feature_mapper=feature_calculator.get_mapper(),
		_save_model=False,
	    _model_path=MODEL_PATH,
	    _use_top_features=False,
	    _top_features=None,
	    _test_index=600, 
        _cross_validation=False,
        _cv_fold=10
	)
	classifiers.add_default_classifiers()
	classifiers.process(feature_vector)

	# Return JSON results
	return export_inference([pre_processor, feature_calculator, classifiers])


def compose_inference(filename=DATA_FILENAME):
	# Read sensor raw data from a file
	phone_data = pd.read_csv(filename, header=1)
	# Use dummy wear data for now
	wear_data = pd.read_csv(filename, header=1)

	# Data preparation
	data_columns = [
		ModuleBase.TIMESTAMP, 
		ModuleBase.ACCX, 
		ModuleBase.ACCY, 
		ModuleBase.ACCZ, 
		ModuleBase.LABEL
	]
	phone_features = [
		Feature.MEAN,
		Feature.STD,
		Feature.MAD,
		Feature.RMS,
		Feature.RANGE,
		Feature.SKEW,
		Feature.QUA + '1',
		Feature.FFT + '3',
		Feature.FFT + '4',
		Feature.FFT + '5'
	]
	wear_features = [
		Feature.MEAN,
		Feature.VAR,
		Feature.STD,
		Feature.MAG,
		Feature.KURT,
		Feature.QUA + '2',
		Feature.QUA + '3',
		Feature.FFT + '1',
		Feature.FFT + '2',
		Feature.FFT + '3'
	]

	# Result json for output
	result = {
		ModuleBase.ANDROID_WEAR: json.loads(compose_inference_helper(
    		wear_data,
    		data_columns,
    		wear_features
    	)),
    	ModuleBase.ANDROID_PHONE: json.loads(compose_inference_helper(
    		phone_data,
    		data_columns,
    		phone_features
    	))
	}

	# Export the inference pipeline to a JSON file
	with open(INF_FILENAME, 'w') as fout:
		fout.write(json.dumps(
			result,
			sort_keys=True,
			indent=4,
			separators=(',', ': ')
		))


if __name__ == "__main__":
    compose_inference()
