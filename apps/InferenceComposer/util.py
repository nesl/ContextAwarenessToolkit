import numpy as np
import pandas as pd

import os

from sklearn import metrics
from sklearn.metrics import accuracy_score
from sklearn.metrics import precision_recall_fscore_support
from sklearn.metrics import precision_score
from sklearn.metrics import recall_score
from sklearn.metrics import classification_report
from sklearn2pmml import sklearn2pmml


def remove_nan(X, y):
    """
    Remove nan data from feature calculation
    """
    newX = []
    newY = []
    for i in range(0, len(X)):
        lst = X[i]
        lbl = y[i]
        flag = True
        for i in lst:
            if np.isnan(i):
                flag = False
                break
        if flag:
            newX.append(lst)
            newY.append(lbl)
    return np.array(newX), np.array(newY)


def show_accuracy(ground_truth, predicted):
    """
    Print statistics from classification results
    """
    acc_score = accuracy_score(ground_truth, predicted)
    prec_score = precision_score(ground_truth, predicted, average='weighted')
    recl_score = recall_score(ground_truth, predicted, average='weighted')
    print('--- Accuracy: ', acc_score)
    print('--- Precision: ', prec_score)
    print('--- Recall: ', recl_score)
    print('--- Classification report: ')
    print(precision_recall_fscore_support(ground_truth, predicted))
    print(classification_report(ground_truth, predicted))
    return acc_score, prec_score, recl_score


def export_model(name, classifier, mapper, path):
    """
    Export a classifier into PMML using the jpmml library
    """
    print('Exporting model ', name, ' to PMML...')
    sklearn2pmml(
        estimator=classifier, 
        mapper=mapper, 
        pmml=os.path.join(path, name + '.pmml')
    )


def show_label_distribution(data):
    """
    Print the distribution of labels in a data frame
    """
    stats = [0, 0, 0, 0]
    for index, row in data.iterrows():
        if int(row['label']) == 0:
            stats[0] = stats[0] + 1
        elif int(row['label']) == 1:
            stats[1] = stats[1] + 1
        elif int(row['label']) == 2:
            stats[2] = stats[2] + 1
        elif int(row['label']) == 3:
            stats[3] = stats[3] + 1
        else:
            print('Error: unknown label ' + str(row['label']))
    print(stats)
    print(sum(stats))