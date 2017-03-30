\begin{table}[htb]
\begin{tabular}{c|<#list planners as planner>|c</#list>}
\textbf{Problem}<#list planners as planner> & \textbf{${planner?replace("_", "\\_")}}</#list>\\
\hline
\hline
<#list problems as problem>
${problem}<#list planners as planner> & <#if (((status[planner][problem])!"") == "VALID")>${elements[planner][problem]!"N/A"}<#elseif (((status[planner][problem])!"") == "INVALID")>inv.<#elseif (((status[planner][problem])!"") == "UNSOLVED")>uns.<#else>ERR</#if></#list>\\
</#list>
\end{tabular}
\caption{Problem/planner table\TODO{Fix the caption, fix the label}}
\label{tab:results-TODO}
\end{table}
