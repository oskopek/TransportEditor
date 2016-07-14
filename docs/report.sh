#!/bin/bash

sprint="`cat CURRENT_SPRINT`"
out=/dev/stdout
startdate="`date --date='last week' "+%d. %m. %Y"`"
enddate="`date "+%d. %m. %Y"`"
line="------------------------------------------------"

echo "<!DOCTYPE html>
<meta charset="UTF-8">
<html>
    <head>
            <title>$title</title>
            <style>
                pre {
                    white-space: pre-wrap;
                    background: lightgray
                }
            </style>
    </head>
    <body>"
echo -e "    <h1>Bachelor thesis status report ($startdate - $enddate)</h1>\n\n    <h2>Sprint #$sprint</h2>"
echo "    <hr>"
echo "<h2>My comments on this weeks work items</h2>"
echo "    <pre><code>"
cat diary/sprint"$sprint".adoc
echo "    </code></pre>"
echo "    <h2>What I did this week (git commits) and GitHub issues</h2>"
echo "    <pre><code>"
did this week --TransportEditor --github
echo "    </code></pre>"
echo "</body>"
echo "</html>"

