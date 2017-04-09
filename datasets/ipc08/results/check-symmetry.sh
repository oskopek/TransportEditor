for file in `ls ???.pddl`; do cat $file | grep -E '\(road-length.*' -o | grep -E ' [0-9]+\)' -o | grep -Eo '[0-9]+' | sed '$!N;s/\n/ /' | awk '$1!=$2 {print $0}' ; done
