# Pre-defined module and parameter names
MOD_PREPROCESS = 'mod_preprocess'
MOD_FEATURE = 'mod_feature'
MOD_CLASSIFIER = 'mod_classifier'

DATA_COLUMN = 'data_column'
WINDOW_SIZE = 'window_size'
OPERATOR = 'operator'
FEATURE = 'feature'
CLASSIFIER = 'classifier'
CLASSIFIER_NAME = 'classifier_name'
CLASSIFIER_PMML = 'classifier_pmml'

# String constants
TIMESTAMP = 'time'
LABEL = 'label'

ACCX = 'accx'
ACCY = 'accy'
ACCZ = 'accz'
ACC = [ACCX, ACCY, ACCZ]


class ModuleBase:
	module_type = None

	def process(self, data):
		"""
		Perform operations on a data vector (pandas.DataFrame)
		"""
		raise NotImplementedError('Not implemented!')

	def export(self):
		"""
		Export the module to a JSON format
		"""
		raise NotImplementedError('Not implemented!')
