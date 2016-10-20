import inference as inf

"""
Placeholder for scikit model -> PMML -> Java/Android runtime

Code taken from the example in https://github.com/jpmml/sklearn2pmml
"""

#
# Step 1: feature engineering
#

from sklearn.datasets import load_iris
from sklearn.decomposition import PCA

from sklearn2pmml.decoration import ContinuousDomain

import pandas
import sklearn_pandas

iris = load_iris()

iris_df = pandas.concat((pandas.DataFrame(iris.data[:, :], columns = ["Sepal.Length", "Sepal.Width", "Petal.Length", "Petal.Width"]), pandas.DataFrame(iris.target, columns = ["Species"])), axis = 1)

iris_mapper = sklearn_pandas.DataFrameMapper([
    (["Sepal.Length", "Sepal.Width", "Petal.Length", "Petal.Width"], [ContinuousDomain(), PCA(n_components = 3)]),
    ("Species", None)
])

iris = iris_mapper.fit_transform(iris_df)

#
# Step 2: training a logistic regression model
#

from sklearn.linear_model import LogisticRegressionCV

iris_X = iris[:, 0:3]
iris_y = iris[:, 3]

iris_classifier = LogisticRegressionCV()
iris_classifier.fit(iris_X, iris_y)

#
# Step 3: conversion to PMML
#

from sklearn2pmml import sklearn2pmml

sklearn2pmml(iris_classifier, iris_mapper, "LogisticRegressionIris.pmml", with_repr = True)