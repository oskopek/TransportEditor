if [ -n "$outputfile" ]; then
    cp "$tmpout" "$outputfile"
else
    cat "$tmpout"
fi

rm "$tmpout"
