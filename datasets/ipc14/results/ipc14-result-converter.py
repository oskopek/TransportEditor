#!/usr/bin/env python

import csv
import json

statusTable = {
        1: 'VALID',
        0: 'UNSOLVED',
    }

def transform_rows(rows):
    def parseScoreIfPossible(scoreStr):
        retval = None
        try:
            retval = int(scoreStr)
        except ValueError:
            return None
        if retval == 0:
            return None
        else:
            return retval

    json = []
    for row in rows:
        if not domain in row[2] or not track in row[0]:
            continue
        planner = row[1]
        if "ref" == planner:
            continue
        problem = "p" + str(row[3])
        status = statusTable[str(row[4])]

        score = parseScoreIfPossible(row[5])
        best = parseScoreIfPossible(row[6])
        quality = float(row[7])
        return {"runs": json}

def read_csv(inpfile):
    print("Reading file " + inpfile)
    rows = []
    with open(inpfile, 'r') as csvfile:
        reader = csv.reader(csvfile, delimiter=',', quotechar='"')
        for row in reader:
            if not row:
                continue
            row = [element for element in row if element]
            rows.append(row)
    return rows[:-1]

def to_json(domain, finished, score):
    planners = finished[0]
    if not score:
        planners = finished[0][:-1]
    problems = [row[0] for row in finished[1:]]

    finished_dict = {}
    best_score = {}
    score_dict = {}
    for row in finished[1:]:
        problem = row[0]
        best_score[problem] = None
        finished_dict[problem] = {}
        score_dict[problem] = {}
        row_end = len(row)
        if not score:
            row_end -= 1
            best_score[problem] = int(row[-1])
        for i in range(1, row_end):
            planner = planners[i-1]
            val = int(str(row[i])) # 0 or 1
            finished_dict[problem][planner] = val
            score_dict[problem][planner] = None
            if not score:
                score_dict[problem][planner] = val

    if score:
        for row in score[1:]:
            problem = row[0]
            best_score[problem] = int(row[-1])
            for i in range(1, len(row)-1):
                planner = planners[i-1]
                score_dict[problem][planner] = float(row[i])

    rows = []
    for problem in problems:
        for planner in planners:
            if planner in ["miplan", "nucelar", "planets", "dpmplan", "freelunch", "arvandherd", "bfs-f", "bifd", "fdss-2014", "cedalion", "rpt", "use", "yahsp3", "ibacop2"]: # skip low performing planners
                continue
            best = best_score[problem]
            status = statusTable[finished_dict[problem][planner]]
            quality = score_dict[problem][planner]
            if quality >= 0.01:
                cur_score = int(best/(quality)) # quality = best/score => score = best/quality
            else:
                cur_score = None
            jsonrow = {
                "domain": domain,
                "problem": problem,
                "planner": planner,
                "temporalPlanActions": [],
                "actions": [],
                "results": {
                    "score": cur_score,
                    "bestScore": best,
                    "exitStatus": status,
                    "startTimeMs": 0,
                    "endTimeMs": 0,
                    "durationMs": -1,
                    "quality": quality
                    }
                }
            rows.append(jsonrow)
    return {"runs": rows}

if __name__ == '__main__':

    to_filter = {'transport': {'seq-sat': True, 'seq-opt': False, #'seq-agl': True,
        'seq-mco': True}}

    inpfolder = 'csv'
    jsonfile_prefix = 'transport-strips-'
    jsonfile_suffix = '-ipc14-results.json'

    for domain in to_filter:
        for track in to_filter[domain]:
            has_score = to_filter[domain][track]
            finished_csv_rows = read_csv(inpfolder + '/' + domain + '-' + track + ".csv")
            score_csv_rows = []
            if has_score:
                score_csv_rows = read_csv(inpfolder + '/' + domain + '-' + track + "-score.csv")

            json_data = to_json(jsonfile_prefix[:-1], finished_csv_rows, score_csv_rows)
            json_text = json.dumps(json_data, indent=4, sort_keys=True)
            with open(jsonfile_prefix + track + jsonfile_suffix, 'w') as jsonfile:
                jsonfile.write(json_text)

