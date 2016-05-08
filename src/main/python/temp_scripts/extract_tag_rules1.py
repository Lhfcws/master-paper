
fp = open("content_mapping.txt", "r", encoding="utf-8")
fw = open("content_tag_rules.txt", "w", encoding="utf-8")

tag = ""
for line in fp.readlines():
    line = line.strip()
    if line.startswith("@"):
        tag = line[1:]
        continue
    elif len(line) > 0:
        fw.write(tag)
        fw.write("\t")
        fw.write(line)
        fw.write("\n")

fw.flush()
fp.close()
fw.close()
