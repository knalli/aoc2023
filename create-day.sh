#!/usr/bin/env bash

FMT_RED="\033[0;31m"
FMT_GREEN="\033[0;32m"
FMT_RESET="\033[0m"

function errecho() {
  echo >&2 -ne "${FMT_RED}"
  echo >&2 -n "$@"
  echo >&2 -e "${FMT_RESET}"
}

function okecho() {
  echo >&2 -ne "${FMT_GREEN}"
  echo >&2 -n "$@"
  echo >&2 -e "${FMT_RESET}"
}

function initDay() {
  local day
  local id
  day="$1"
  id=$(printf "%02d" "$day")
  if [ -r "src/main/java/de/knallisworld/aoc2023/day${id}/Day${id}.java" ]; then
    errecho "day file already exist"
    return 1
  fi
  mkdir -p "src/main/java/de/knallisworld/aoc2023/day${id}"
  cat <<EOF >"src/main/java/de/knallisworld/aoc2023/day${id}/Day${id}.java"
package de.knallisworld.aoc2023.day${id};

import lombok.extern.log4j.Log4j2;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;

@Log4j2
public class Day${id} {

	public static void main(String[] args) {
		printHeader(${day});
		printSolution(1, () -> "x");
		printSolution(2, () -> "x");
	}

}

EOF

  cat <<EOF >"src/main/java/de/knallisworld/aoc2023/day${id}/package-info.java"
@NonNullApi
package de.knallisworld.aoc2023.day${id};

import org.springframework.lang.NonNullApi;

EOF

}

if ! initDay "$1"; then
  errecho "Failed initializing day"
  exit 1
fi
