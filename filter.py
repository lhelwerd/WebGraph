# Imports
from __future__ import print_function
import sys
from glob import glob
import re
import os
from collections import OrderedDict

class Dataset:
    def __init__(self, name):
        self.name = name
        self.compressions = OrderedDict()

class Compression:
    def __init__(self, name, params=None):
        self.compression = name
        self.params = params
        self.metrics = {}

# Main function
def main(argv):
    # Constants
    types = OrderedDict([
        ("none", "None"),
        ("gaps", "Gaps"),
        ("interval", "Interval"),
        ("copylist", "Copy list"),
        ("copyblocks", "Copy blocks"),
        ("copyflags", "Copy flags")
    ])
    regex = r"^(.*)-(" + "|".join(types.keys()) + r")(?:-(.*))?$"
    property_filter = [
        "bitsforblocks", "residualarcs", "avgref", "avgbitsforoutdegrees",
        "avgbitsforblocks", "bitsperlink", "bitsforresiduals",
        "bitsforreferences", "avgbitsforreferences", "nodes", "bitspernode",
        "arcs", "bitsforoutdegrees", "avgbitsforintervals",
        "avgbitsforresiduals"
    ]

    # Input parameters
    path = argv[0] if len(argv) > 0 else "../experiments"

    # Data table to put intermediate representations in
    sets = {}

    # Read the experiments
    properties = sorted(glob(path + "/*.properties"))
    for p in properties:
        experiment = p[len(path):-len(".properties")]
        prefix = path + "/" + experiment

        m = re.match(regex, experiment)
        name, compression, params = m.groups()

        if name not in sets:
            sets[name] = Dataset(name)

        if compression not in sets[name].compressions:
            sets[name].compressions[compression] = OrderedDict()

        data = Compression(compression, params)

        prop_file = open(p)
        for line in prop_file:
            if "=" not in line:
                continue

            prop, value = line[:-1].split("=")
            if prop in property_filter:
                data.metrics[prop] = value

        prop_file.close()

        stats_file = open(prefix + ".stats")
        for line in stats_file:
            if " - " in line:
                info = line[:-1].split(" - ")[1]
                if info == "Loading graph...":
                    key = "loadtime"
                elif info == "Storing...":
                    key = "storetime"
                elif info.startswith("Elapsed: "):
                    time = info[len("Elapsed: "):info.find(" [")]
                    data.metrics[key] = time
            elif line.startswith("mem\t"):
                value = line[len("mem\t"):-len("/4 kB\n")]
                data.metrics["peakmem"] = int(value)/4

        stats_file.close()

        for speed in ["seq", "rand"]:
            speed_file = open(prefix + "." + speed + "speedtest")
            for line in speed_file:
                if line.startswith("Time: "):
                    parts = line.split(" ")
                    for p in range(0, len(parts), 2):
                        key = parts[p].lower()[:-1]
                        value = parts[p+1]
                        if not value[-1].isdigit():
                            value = value[:-1]
                        data.metrics[speed + "_" + key] = value
            speed_file.close()

        try:
            st = os.stat(prefix + ".graph")
            data.metrics["size"] = st.st_size
        except OSError:
            data.metrics["size"] = None

        sets[name].compressions[compression][params] = data

    sizes = glob(path + "/*.sizes")
    for s in sizes:
        size_file = open(s)
        for line in size_file:
            parts = line[:-1].split()
            size = int(parts[5])
            experiment = parts[-1]
            if experiment.endswith(".graph"):
                m = re.match(regex, experiment[:-len(".graph")])
                name, compression, params = m.groups()
                sets[name].compressions[compression][params].metrics["size"] = size

        size_file.close()

    print("""\\documentclass{article}
\\usepackage{array}
\\newcolumntype{x}[1]{>{\\arraybackslash}p{#1}}
\\usepackage{tikz}
\\newcommand\diag[4]{%
  \\multicolumn{1}{|p{#2}|}{\\vskip.5\\baselineskip\\vskip-\\tabcolsep\\hskip-\\tabcolsep
    $\\vcenter{\\begin{tikzpicture}[baseline=0,anchor=south west,inner sep=#1]
    \\path[use as bounding box] (0,0) rectangle (#2+2\\tabcolsep,\\baselineskip);
    \\node[minimum width={#2+2\\tabcolsep-\\pgflinewidth},
        minimum height=2\\baselineskip+\\extrarowheight-\\pgflinewidth] (box) {};
    \\draw[line cap=round] (box.north west) -- (box.south east);
    \\node[anchor=south west] at (box.south west) {#3};
    \\node[anchor=north east] at (box.north east) {#4};
\\end{tikzpicture}}$\hskip-\\tabcolsep}%
}
\\begin{document}""")

    metric = "storetime"
    metric_name = "Time to compress from memory to the compressed format."

    first = next(sets.itervalues())
    lengths = dict([(c, 1) for c in types.keys()])
    for compression, vals in first.compressions.iteritems():
        lengths[compression] = len(vals)

    columnspec = "|x{2.5cm}" + ("|l" * sum(lengths.itervalues())) + "|"
    print("""
\\begin{table}[h]
    \\centering
    {\\setlength{\\tabcolsep}{2pt}\\footnotesize \\begin{tabular}{""" + columnspec + """} \\hline
\multicolumn{1}{|r|}{Algorithm} & """ + " & ".join(["\multicolumn{" + str(lengths[c]) + "}{c|}{" + types[c] + "}" for c in types.keys()]) + """ \\\\ \\hline""")

    print("\diag{.2em}{2.5cm}{Dataset}{Parameters}", end="")

    for compression in types.iterkeys():
        if compression not in first.compressions:
            print(" &" * lengths[compression], end="")
        else:
            for params in first.compressions[compression].keys():
                print(" &", end="")
                if params is not None:
                    print(" \\begin{tabular}[t]{@{}c@{}}" + "\\\\".join(["${} = {}$".format(params[i], "\\infty" if params[i+1] == "-" else params[i+1]) for i in range(0, (len(params)/2)*2, 2)]) + "\\end{tabular}", end="")

    print(" \\\\ \\hline")

    for name in sets.keys():
        print(name, end="")
        for compression in types.iterkeys():
            if compression not in sets[name].compressions:
                print(" &" * lengths[compression], end="")
            else:
                for p in sets[name].compressions[compression].itervalues():
                    print(" &", end="")
                    if metric in p.metrics:
                        print(" " + p.metrics[metric], end="")
        print(" \\\\ \\hline")

    print("""
    \\end{tabular}}
    \\caption{""" + metric_name + """}
    \\label{fig:""" + metric + """}
\\end{table}""")

    print("""\\end{document}""")


if __name__ == "__main__":
    main(sys.argv[1:])
