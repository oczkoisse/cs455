#!/usr/bin/env python

import csv
import numpy as np
import matplotlib.pyplot as plt

states = []
maleps = []
femaleps = []

with open('data.csv', 'r') as datafile:
    datareader = csv.reader(datafile, delimiter=',')
    for row in datareader:
        states.append(row[0].strip())
        maleps.append(float(row[1].strip()))
        femaleps.append(float(row[2].strip()))

maleps[39] = 0.0
femaleps[39] = 0.0

N = len(states)
ind = np.arange(1, N + 1) + .2 # the x locations for the groups
width = 0.2      # the width of the bars

fig, ax = plt.subplots()
rects1 = ax.bar(ind, maleps, width, color='g', label="Male separation %")

inter_space = 0.2

rects2 = ax.bar(ind + width + inter_space , femaleps, width, color='r', label="Female separation %")

ax.set_ylabel('Separation percentage')
ax.set_title('States')

ax.legend(loc='upper right')

ax.set_xticks(ind+width+inter_space)
ax.set_xticklabels(states)

fig.autofmt_xdate()
plt.show()
