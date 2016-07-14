all: thesis.pdf

# LaTeX must be run multiple times to get references right
thesis.pdf: thesis.tex $(wildcard *.tex) bibliography.bib
	pdflatex $<
	bibtex thesis
	pdflatex $<
	pdflatex $<

clean:
	rm -f *.log *.dvi *.aux *.toc *.lof *.lot *.out *.bbl *.blg
	rm -f thesis.pdf
