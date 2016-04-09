#!/usr/bin/env python3
import networkx as nx
import sys
import os
from math import sqrt

try:
        import matplotlib.pyplot as plt
except:
        raise

G=nx.DiGraph()
infile=sys.argv[1]
lines = []
with open(infile, 'r') as f:
    lines = f.readlines()

for line in lines:
    if "road-length" in line:
        line = line.lstrip().rstrip().replace("ity", "").replace("oc", "")
        tokens = "".join(filter(lambda c: c not in "()=-", line)).split()
        assert len(tokens) == 4
        a = tokens[1]
        b = tokens[2]
        cost = int(tokens[3])
        G.add_node(a)
        G.add_node(b)
        G.add_edge(a, b, weight=cost, label=cost)

ncoef = 1
abspos = dict()
for i in range(0, len(lines)):
    line = lines[i].lstrip().rstrip()
    if not "->" in line:
        continue
    nline = lines[i+1].lstrip().rstrip().replace("ity", "").replace("oc", "")
    ptokens = "".join(filter(lambda c: c not in "()=-", nline)).split()
    a = ptokens[1]
    b = ptokens[2]

    ctokens = "".join(filter(lambda c: c not in ";<->", line)).split()
    atokens = ctokens[0].split(",")
    btokens = ctokens[1].split(",")
    acoords = (ncoef*int(atokens[0]), ncoef*int(atokens[1]))
    bcoords = (ncoef*int(btokens[0]), ncoef*int(btokens[1]))
    abspos[a] = acoords
    abspos[b] = bcoords

coef = 2*1/sqrt(len(G.nodes()))
pos=nx.spring_layout(G, k=coef, iterations=750) # positions for all nodes
#pos=abspos

# nodes
nx.draw_networkx_nodes(G,pos,node_size=350, linewidths=0.5, node_color='r')
# edges
nx.draw_networkx_edges(G,pos, width=0.5)
# labels
nx.draw_networkx_labels(G,pos,font_size=7)
elabels = dict([((u, v), d["label"]) for (u, v, d) in G.edges(data=True)])
#lpos=0.4
lpos=0.5
lalpha=0.75
nx.draw_networkx_edge_labels(G,pos,edge_labels=elabels, font_size=5, label_pos=lpos,
        bbox=dict(boxstyle="square,pad=0", lw=0, fc="w", alpha=lalpha))

plt.axis("off")
plt.savefig(os.path.basename(infile) + ".png", dpi=400, bbox_inches='tight') # save as png
plt.savefig(os.path.basename(infile) + ".svg", dpi=400, bbox_inches='tight', format="svg")
#plt.show() # display
