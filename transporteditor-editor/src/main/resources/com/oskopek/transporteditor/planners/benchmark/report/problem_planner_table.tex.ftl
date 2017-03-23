\begin{table}[htb]
\begin{tabular}{c|<#list planners as planner>|c</#list>}
\textbf{Problem}<#list planners as planner> & \textbf{${planner}}</#list>\\
\hline
\hline
<#list problems as problem>
${problem}<#list planners as planner> & ${elements[planner][problem]}</#list>\\
</#list>
\end{tabular}
\caption{Problem/planner table\TODO{Fix the caption, fix the label}}
\label{tab:results-TODO}
\end{table}
