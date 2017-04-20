#!/usr/bin/env python

import csv
import json

statusTable = {
        2: 'VALID',
        1: 'INVALID',
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
    return rows

def to_json(domain, finished, score):
    problems = set()
    planners = set()

    finished_dict = {}
    best_score = {}
    score_dict = {}
    time_dict = {}
    for row in finished[1:]:
        planner = row[0]
        planners.add(planner)
        problem = int(row[2])
        problems.add(problem)
        finished_status = int(row[3]) + int(row[4])

        best_score[problem] = None
        if not problem in finished_dict:
            finished_dict[problem] = {}
        finished_dict[problem][planner] = finished_status
        score_dict[problem] = {}
        time_dict[problem] = {}
    if score:
        for row in score[1:]:
            planner = row[0]
            problem = int(row[2])
            if len(row) == 5:
                timesols = [int(s)*1000 for s in row[3].split(",")]
                values = [float(v) for v in row[4].split(",")]
                max_val = min(list(enumerate(values)), key=lambda elem: elem[1])
                max_time = timesols[max_val[0]]
                score_dict[problem][planner] = max_val[1]
                time_dict[problem][planner] = max_time

    for problem in problems:
        best_score[problem] = min([score_dict[problem][planner] for planner in score_dict[problem]])

    rows = []
    for problem in problems:
        for planner in planners:
            if planner in ["acoplan", "acoplan2", "cpt4", "lprpgp", "madagascar", "madagascar-p", "popf2", "satplanlm-c", "sharaabi"]: # skip irrelevant planners
                continue
            best = best_score[problem]
            status = statusTable[finished_dict[problem][planner]]
            if planner not in score_dict[problem]:
                cur_score = None
                quality = 0.0
                time = 0
            else:
                cur_score = score_dict[problem][planner]
                quality = best/cur_score
                time = time_dict[problem][planner]
            jsonrow = {
                "domain": domain,
                "problem": 'p{:02d}'.format(problem + 1),
                "planner": planner,
                "temporalPlanActions": [],
                "actions": [],
                "results": {
                    "score": cur_score,
                    "bestScore": best,
                    "exitStatus": status,
                    "startTimeMs": 0,
                    "endTimeMs": time,
                    "durationMs": -1 if time == 0 else time,
                    "quality": quality
                    }
                }
            rows.append(jsonrow)
    return {"runs": rows}

if __name__ == '__main__':

    to_filter = {'transport': {'seq-sat': True}} #, 'seq-opt': True, 'seq-mco': True}}

    inpfolder = 'csv'
    jsonfile_prefix = 'transport-strips-'
    jsonfile_suffix = '-ipc11-results.json'

    for domain in to_filter:
        for track in to_filter[domain]:
            has_score = to_filter[domain][track]
            finished_csv_rows = read_csv(inpfolder + '/' + domain + '-' + track + ".csv")
            score_csv_rows = []
            if has_score:
                score_csv_rows = read_csv(inpfolder + '/' + domain + '-' + track + "-time-score.csv")

            json_data = to_json(jsonfile_prefix[:-1], finished_csv_rows, score_csv_rows)
            json_text = json.dumps(json_data, indent=4, sort_keys=True)
            with open(jsonfile_prefix + track + jsonfile_suffix, 'w') as jsonfile:
                jsonfile.write(json_text)

