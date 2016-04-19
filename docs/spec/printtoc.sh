#!/bin/bash
doc="tex/spec.tex"

cat "$doc" | grep -E '\\[a-z]*section' | sed -E 's/sub/    /g' | sed -E 's/section//' | grep -Eo '\\[ ]*\{[^}]*\}' | tr -d '{}\\' | sed -E 's/^/    /'
