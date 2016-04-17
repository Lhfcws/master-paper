# encoding: utf-8

import os

fp = open("../../resources/usr_grp.txt", "r", encoding="utf-8")
fw = open("../../resources/content_tag_rules.txt", "w", encoding="utf-8")


def filter_single_char(ls):
    kws = []
    for word in ls:
        if len(word) > 1:
            kws.append(word)
    return kws


def write_tag_rules(tag, kws):
    for kw in kws:
        fw.write(tag + "\t" + kw + "\t1\n")


start = 0
for line in fp.readlines():
    line = line.strip()
    if len(line) == 0:
        continue
    if start == 1:
        ls = line.split("|")
        if len(ls) != 2:
            continue
        tag = ls[0]
        kws = ls[1].split("$")
        kws = filter_single_char(kws)
        write_tag_rules(tag, kws)

    if line.startswith("@MODEL"):
        start = 1

fp.close()
fw.close()
