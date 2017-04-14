\begin{tabular}{|l|<#list planners as planner>r</#list>|r|}
\hline
\textbf{Problem}<#list planners as planner> & \textbf{${planner?replace("_", "\\_")}}</#list> & \textbf{BEST}\\
\hline
<#list problems as problem>
${problem}<#list planners as planner> & <#if (((status[planner][problem])!"") == "VALID")>{\footnotesize ${score[planner][problem]!"N/A"}} \textbf{${quality[planner][problem]}}<#elseif (((status[planner][problem])!"") == "INVALID")>inv.<#elseif (((status[planner][problem])!"") == "UNSOLVED")>uns.<#else>ERR</#if></#list> & ${best[problem]!"--"}\\
</#list>
\hline
\textbf{total}<#list planners as planner> & \textbf{${total[planner]}}</#list> & \\
\hline
\end{tabular}
