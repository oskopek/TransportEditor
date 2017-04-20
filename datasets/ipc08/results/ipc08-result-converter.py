#!/usr/bin/env python

import csv
import json

statusTable = {
        'OK': 'VALID',
        'unsolved': 'UNSOLVED',
        'invalid': 'INVALID',
        'subopt': 'SUBOPT'
    }

def filter_rows(rows, domain, track):
    def parseScoreIfPossible(scoreStr):
        try:
            return int(scoreStr)
        except ValueError:
            return None

    json = []
    for row in rows:
        if not domain in row[2] or not track in row[0]:
            continue
        planner = row[1]
        if "ref" == planner:
            continue
        if planner in ["upwards", "dae1", "dae2", "c3"]: # filter out not well performing planners
            continue
        problem = "p" + str(row[3])
        status = statusTable[str(row[4])]

        score = parseScoreIfPossible(row[5])
        best = parseScoreIfPossible(row[6])
        quality = float(row[7])
        jsonrow = {
                "domain": domain,
                "problem": problem,
                "planner": planner,
                "temporalPlanActions": [],
                "actions": [],
                "results": {
                    "score": score,
                    "bestScore": best,
                    "exitStatus": status,
                    "startTimeMs": 0,
                    "endTimeMs": 0,
                    "durationMs": -1,
                    "quality": quality
                }
            }
        json.append(jsonrow)
    return {"runs": json}

if __name__ == '__main__':

    to_filter = {'transport-strips': ['seq-sat', 'seq-opt'], 'transport-numeric': ['tempo-sat', 'netben-opt']}

    inpfile = 'post-ipc2008-results.txt'
    jsonfile_suffix = '-ipc08-results.json'

    rows = []
    with open(inpfile, 'r') as csvfile:
        reader = csv.reader(csvfile, delimiter=' ')
        for row in reader:
            if not row:
                continue
            row = [element for element in row if element]
            rows.append(row)

    for domain in to_filter:
        for track in to_filter[domain]:
            json_data = filter_rows(rows, domain, track)
            json_text = json.dumps(json_data, indent=4, sort_keys=True)
            with open(domain + '-' + track + jsonfile_suffix, 'w') as jsonfile:
                jsonfile.write(json_text)

