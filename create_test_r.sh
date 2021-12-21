#!/bin/bash
# encoding=utf-8

echo "start generate r_strings.xml"
file_name="r_strings.xml"

echo "<?xml version=\"1.0\" encoding=\"utf-8\"?>" >>$file_name
echo "<resources>" >>$file_name

for ((i = 1; i <= 65536; i++)); do
  echo "  <string name=\"public_r_$i\">TEST-$i</string>" >>$file_name
done

echo "</resources>" >>$file_name
echo "generate r_strings.xml success!"
