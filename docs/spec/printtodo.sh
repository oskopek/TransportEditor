#!/bin/bash
doc="tex/spec.tex"

cat "$doc" | grep -E '\\TODO .*' | tr -d '{}\\' | sed -E 's/^/    * /'
