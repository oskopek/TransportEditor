all: TransportEditor-user-manual.pdf TransportEditor-dev-manual.pdf

TransportEditor-user-manual.pdf: TransportEditor-user-manual.tex
	pdflatex $<
	bibtex TransportEditor-user-manual
	pdflatex $<
	pdflatex $<
	pdflatex $<

TransportEditor-dev-manual.pdf: TransportEditor-dev-manual.tex
	pdflatex $<
	bibtex TransportEditor-dev-manual
	pdflatex $<
	pdflatex $<
	pdflatex $<
	
clean:
	rm -f *.log *.dvi *.aux *.toc *.lof *.lot *.out *.bbl *.blg *.xmpi *.pdf
