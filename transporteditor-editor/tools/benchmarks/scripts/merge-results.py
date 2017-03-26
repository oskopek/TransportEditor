#!/usr/bin/env python

import json
import sys

if __name__ == '__main__':

    if len(sys.argv) < 3:
        raise ValueError("Needs at least two args: [JsonResult]+ [TargetJson]")

    runs = []
    for i in range(1, len(sys.argv)-1):
        in_file = sys.argv[i]
        with open(in_file, 'r') as file:
            json_runs = json.loads(file.read())
            for run in json_runs["runs"]:
                runs.append(run)

    out_file = sys.argv[-1]
    runs = {"runs": runs}
    json_text = json.dumps(runs, indent=4, sort_keys=True)
    with open(out_file, 'w') as file:
        file.write(json_text)
