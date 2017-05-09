#!/bin/bash
# Author: Ondrej Skopek <oskopek@oskopek.com>
# License: MIT <https://opensource.org/licenses/MIT>
#
# Run this script using no parameters, or a single parameter (the root directory you wish to convert)
# NOTE: This script might irreversibly damage your files. Please, back up your files.
# NOTE 2: If you find any bugs, please report/fix them at:
#                 https://gist.github.com/oskopek/cca1a553c612e24f8326e6bc0b5d84f7

set -e

root_dir="$1"
if [ -z "$root_dir" ]; then
root_dir="." # root directory to recursively convert
fi
whitelist="pdf jpg wav wave mp3 vob mp4 csv xml txt" # whitelist of all file endings
uninstall_file="sis-unconvert.sh.txt"
uninstall_file_win="sis-unconvert.bat.txt"

orig_dir="`pwd`"
cd "$root_dir"

files="`find . -not \( -name '\.git' -type d -prune \) -type f`"
for ending in $whitelist; do
    files="`echo "$files" | grep -v "\.$ending\$"`"
done

echo '#!/bin/bash' > "$uninstall_file"
chmod +x "$uninstall_file"
echo '@echo off' > "$uninstall_file_win"
chmod +x "$uninstall_file_win"
for file in $files; do
    mv -v "$file" "$file".txt
    echo mv -v "$file".txt "$file" >> "$uninstall_file"
    echo 'move /y' "$file".txt "$file" >> "$uninstall_file_win"
done

cd "$orig_dir"
