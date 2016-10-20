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