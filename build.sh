#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p out
find src/main/java test -name '*.java' > out/sources.txt
javac --release 11 -encoding UTF-8 -Xlint:all -d out @out/sources.txt
case "${1:-test}" in
  test) java -cp out giftbox.PartTests; java -cp out giftbox.ProjectTests ;;
  web) java -cp out giftbox.web.WebServer "${2:-8080}" web ;;
  demo) java -cp out giftbox.demo.Demo ;;
  *) echo 'Use: sh build.sh [test|web|demo] [port]'; exit 1 ;;
esac
