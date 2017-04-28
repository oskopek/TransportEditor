#!/usr/bin/env python3

import json

# Adapted from: http://stackoverflow.com/a/39493954/2713162

from collections import defaultdict
import pandas as pd
import matplotlib.patches as mpatches
import matplotlib.pyplot as plt

def convert_to_gantt(run):
    action_map = {"refuel": "Refuel", "drop": "Drop", "pick-up": "PickUp", "drive": "Drive"}
    def parse_to_frame(temporal_actions):
        whos = []
        actions = []
        begins = []
        ends = []
        for action in temporal_actions:
            action = action.strip()
            if action.startswith(';'):
                continue
            tokens = action.split(" ")
            begin = float(tokens[0][:-1])
            end = begin + float(tokens[-1][1:-1])
            name = action_map[tokens[1][1:]]
            who = tokens[2]
            where = tokens[3]
            whos.append(who)
            actions.append(name)
            begins.append(begin)
            ends.append(end)

            what = None
            if name in ["PickUp", "Drop"]:
                what = tokens[4][:-1]

                whos.append(what)
                actions.append(name)
                begins.append(begin)
                ends.append(end)
            #print(begin, end, name, who, where, what)

        return pd.DataFrame({
            'Object': whos,
            'Action': actions,
            'Start_Time': begins,
            'End_Time': ends
        })

    planner_name = run["planner"]
    problem_name = run["problem"]
    score = run["results"]["score"]
    score = "N/A" if score == None else score
    fig_name = "gantt_" + planner_name + "_" + problem_name
    print("Plotting: " + fig_name)

    temp_actions = run["temporalPlanActions"]
    if temp_actions == None or len(temp_actions) <= 0:
        print("Skipping...")
        return
    df = parse_to_frame(temp_actions)
    df = df[['Object', 'Action', 'Start_Time', 'End_Time']]


    chr_map = {'p' : 3, 'c': 2, 't': 1}
    def get_sort_key(t):
        char = chr_map[t[0]]
        index = int(t[t.rfind('-'):])
        sec_index = t[t.find('-'):t.rfind('-')]
        if sec_index != "":
            sec_index = int(sec_index)
        return (char, index, sec_index)

    names = sorted(df.Object.unique(), key=get_sort_key)
    nb_names = len(names)

    fig = plt.figure()
    ax = fig.add_subplot(111)

    bar_width = 0.8
    default_color = 'black'
    colors_dict = defaultdict(lambda: default_color, Drive='blue', PickUp='green', Drop='red', Refuel='violet')

    # Plot the events
    for index, name in enumerate(names):
        mask = df.Object == name
        start_dates = df.loc[mask].Start_Time
        end_dates = df.loc[mask].End_Time
        durations = end_dates - start_dates
        xranges = list(zip(start_dates, durations))
        ymin = index - bar_width / 2.0
        ywidth = bar_width
        yrange = (ymin, ywidth)
        facecolors = [colors_dict[action] for action in df.loc[mask].Action]
        ax.broken_barh(xranges, yrange, facecolors=facecolors, alpha=1.0, edgecolor="none")
        # you can set alpha to 0.6 to check if there are some overlaps

    # Shrink the x-axis
    box = ax.get_position()
    ax.set_position([box.x0, box.y0, box.width * 0.8, box.height])

    # Add the legend
    patches = [mpatches.Patch(color=color, label=key) for (key, color) in sorted(colors_dict.items())]
    plt.legend(handles=patches, bbox_to_anchor=(1, 0.5), loc='center left')

    # Format the x-ticks

    # Format the y-ticks
    ax.set_yticks(range(nb_names))
    ax.set_yticklabels(names)

    # Set the limits
    time_min = df.Start_Time.min()
    time_max = df.End_Time.max()
    ax.set_xlim(time_min, time_max)

    # Set the title
    ax.set_title(planner_name + ": " + problem_name + " (" + str("%.2f" % round(score, 2)) + ")")
    #plt.show()
    fig.savefig(fig_name + ".pdf", bbox_inches='tight')
    plt.close(fig)

if __name__ == "__main__":
    filename = "results-ipc.json"
    js = None
    with open(filename, 'r') as f:
        js = json.loads("".join(f.readlines()))
    for run in js["runs"]:
        convert_to_gantt(run)
