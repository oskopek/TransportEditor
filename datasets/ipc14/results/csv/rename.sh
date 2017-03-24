for file in `ls *.csv`; do
    newfile="$file"2
    cat $file | sed -E 's/"([0-9]+),([0-9]+)"/"\1.\2"/g' > $newfile
    mv $newfile $file
done
