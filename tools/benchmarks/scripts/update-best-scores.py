#!/usr/bin/env python

import json
import sys

def get_best_from_run(best_scores, run):
    problem = run["problem"]
    if problem not in best_scores:
        best_scores[problem] = None
    cur_best = best_scores[problem]
    if not cur_best:
        cur_best = float('inf')
    best_score = run["results"]["bestScore"]
    if best_score:
        best_score = float(best_score)
        cur_best = min(cur_best, best_score)
    score = run["results"]["score"]
    if score:
        score = float(score)
        cur_best = min(cur_best, score)
    if not score and not best_score and not best_scores[problem]:
        return None
    else:
        return cur_best


if __name__ == '__main__':

    if len(sys.argv) < 3:
        raise ValueError("Needs at least two args: [JsonResult]+ [TargetJson]")

    best_scores = {}
    for i in range(1, len(sys.argv)-1):
        in_file = sys.argv[i]
        with open(in_file, 'r') as file:
            json_runs = json.loads(file.read())
            for run in json_runs["runs"]:
                best_scores[run["problem"]] = get_best_from_run(best_scores, run)
        out_file = sys.argv[-1]
    with open(out_file, 'r') as file:
        runs = json.loads(file.read())
    for run in runs["runs"]:
        run["results"]["bestScore"] = best_scores[run["problem"]]
    json_text = json.dumps(runs, indent=4, sort_keys=True)
    with open(out_file, 'w') as file:
        file.write(json_text)
