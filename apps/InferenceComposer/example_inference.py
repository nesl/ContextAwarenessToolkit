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

def compose_inference(filename=DATA_FILENAME):
	# Read sensor raw data from a file
	raw_data = pd.read_csv(filename, header=1)

	# Data preparation
	raw_data_columns = [
		ModuleBase.TIMESTAMP, 
		ModuleBase.ACCX, 
		ModuleBase.ACCY, 
		ModuleBase.ACCZ, 
		ModuleBase.LABEL
	]
	features = [
		Feature.MEAN,
		Feature.STD,
		Feature.VAR,
		Feature.MAD,
		Feature.RMS,
		Feature.MAG,
		Feature.RANGE,
		Feature.SKEW,
		Feature.KURT,
		Feature.QUA + '1',
		Feature.QUA + '2',
		Feature.QUA + '3',
		Feature.FFT + '1',
		Feature.FFT + '2',
		Feature.FFT + '3',
		Feature.FFT + '4',
		Feature.FFT + '5'
    ]

	# Pre-processing
	pre_processor = Preprocess.Preprocess(
		_window_size=5,
		_data_columns=raw_data_columns,
		_operators=[Preprocess.MOVING_AVG_SMOOTH]
	)
	processed_data = pre_processor.process(raw_data)
	print(processed_data.shape)

	# Feature calculation
	feature_calculator = Feature.Feature(
		_window_size=1,
		_data_columns=raw_data_columns,
		_features=features
	)
	feature_vector = feature_calculator.process(processed_data[:50000])
	print(feature_vector.shape)

	# Training with default classifiers
	classifiers = Classifier.Classifier(
		_feature_mapper=feature_calculator.get_mapper(),
		_save_model=False,
	    _model_path=MODEL_PATH,
	    _use_top_features=False,
	    _top_features=None,
	    _test_index=600, 
        _show_report=False,
        _cross_validation=False,
        _cv_fold=10
	)
	classifiers.add_default_classifiers()
	trained_classifiers = classifiers.process(feature_vector)
	print(len(trained_classifiers))

	# Export the inference pipeline to a JSON file
	with open(INF_FILENAME, 'w') as fout:
		fout.write(json.dumps(
			json.loads(export_inference(
				[pre_processor, feature_calculator, classifiers]
			)),
			sort_keys=True,
			indent=4,
			separators=(',', ': ')
		))


if __name__ == "__main__":
    compose_inference()
