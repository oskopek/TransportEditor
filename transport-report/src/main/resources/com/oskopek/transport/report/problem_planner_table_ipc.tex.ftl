\begin{tabular}{l<#list planners as planner>r</#list>r}
\toprule
\textbf{\#}<#list planners as planner> & \textbf{${planner?replace("_", "\\_")}}</#list> & \textbf{BEST}\\
\midrule
<#list problems as problem>
\multicolumn{1}{l|}{${problem}}<#list planners as planner> & <#if (((status[planner][problem])!"") == "VALID")>{\footnotesize ${score[planner][problem]!"N/A"}} \textbf{${quality[planner][problem]}}<#elseif (((status[planner][problem])!"") == "INVALID")>inv.<#elseif (((status[planner][problem])!"") == "UNSOLVED")>uns.<#else>ERR</#if></#list> & \multicolumn{1}{|r}{${best[problem]!"--"}}\\
</#list>
\midrule
\textbf{total}<#list planners as planner> & \textbf{${total[planner]}}</#list> & \\
\bottomrule
\end{tabular}
