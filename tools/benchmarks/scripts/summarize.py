#!/usr/bin/env python

import json
import sys

def get_quality_from_run(run):
    problem = run["problem"]
    quality = 0.0
    score = run["results"]["score"]
    best_score = run["results"]["bestScore"]
    if best_score and score:
        quality = float(best_score)/float(score)
    #print(run["problem"], run["planner"], quality)
    return quality

if __name__ == '__main__':
    if len(sys.argv) < 1:
        raise ValueError("Needs at least one arg: [JsonResult]+")

    qualities = {}
    first=True
    for in_file in sys.argv:
        if first:
            first=False
            continue
        with open(in_file, 'r') as file:
            json_runs = json.loads(file.read())
            for run in json_runs["runs"]:
                if run["planner"] not in qualities:
                    qualities[run["planner"]] = []
                qualities[run["planner"]].append(get_quality_from_run(run))
    for quality in qualities:
        lst = qualities[quality]
        #print("DEBUG-SIZE", sum(lst))
        #print("DEBUG-LEN", len(lst))
        print(quality, sum(lst)/len(lst))
