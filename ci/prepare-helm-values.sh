#!/bin/sh
set -eu

if [ "$#" -ne 2 ]; then
  echo "Usage: $0 SOURCE_FILE TARGET_FILE" >&2
  exit 2
fi

source_file=$1
target_file=$2
target_directory=${target_file%/*}

if [ "$target_directory" != "$target_file" ]; then
  mkdir -p "$target_directory"
fi

if [ -s "$source_file" ]; then
  cp "$source_file" "$target_file"
else
  printf '{}\n' > "$target_file"
fi
