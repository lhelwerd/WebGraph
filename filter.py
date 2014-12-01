# Imports
from __future__ import print_function
import sys
from glob import glob
import re
import os
import os.path
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

def print_table(metric, metric_name, sets, types, printer):
    first = next(sets.itervalues())
    lengths = dict([(c, 1) for c in types.keys()])
    for compression, vals in first.compressions.iteritems():
        lengths[compression] = len(vals)

    columnspec = "|x{2cm}" + ("|l" * sum(lengths.itervalues())) + "|"
    print("""
\\begin{table}[!ht]
    \\centering
    {\\setlength{\\tabcolsep}{2pt}\\scriptsize \\begin{tabular}{""" + columnspec + """} \\hline
\multicolumn{1}{|r|}{Algorithm} & """ + " & ".join(["\\multicolumn{" + str(lengths[c]) + "}{c|}{" + types[c] + "}" for c in types.keys()]) + """ \\\\ \\hline""")

    print("\diag{.2em}{2cm}{Dataset}{Settings}", end="")

    for compression in types.iterkeys():
        if compression not in first.compressions:
            print(" &" * lengths[compression], end="")
        else:
            for params in first.compressions[compression].keys():
                print(" &", end="")
                if params is not None:
                    print(" \\multicolumn{1}{c|}{\\begin{tabular}[t]{@{}c@{}}" + "\\\\".join(["${} = {}$".format(params[i], "\\infty" if params[i+1] == "-" else params[i+1]) for i in range(0, (len(params)/2)*2, 2)]) + "\\end{tabular}}", end="")

    print(" \\\\ \\hline")

    for name in sets.keys():
        print("\\textsc{" + name + "}", end="")
        for compression in types.iterkeys():
            if compression not in sets[name].compressions:
                print(" &" * lengths[compression], end="")
            else:
                for p in sets[name].compressions[compression].itervalues():
                    print(" &", end="")
                    if metric in p.metrics and p.metrics[metric] is not None:
                        print(" " + printer(p, name), end="")
        print(" \\\\ \\hline")

    print("""    \\end{tabular}}
    \\caption{""" + metric_name + """}
    \\label{tab:""" + metric + """}
\\end{table}""")

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
        "avgbitsforresiduals", "compratio" # (overridden)
    ]

    # Input parameters
    path = argv[0] if len(argv) > 0 else "../experiments"

    # Data table to put intermediate representations in
    sets = OrderedDict([(name, Dataset(name)) for name in ["cnr-2000", "in-2004", "uk-2002", "twitter-2010", "uk-2007-02"]])

    # Read the experiments
    properties = sorted(glob(path + "/*.properties"))
    for p in properties:
        experiment = os.path.basename(p)[:-len(".properties")]
        if experiment[0] == "/":
            experiment = experiment[1:]

        prefix = os.path.dirname(p) + "/" + experiment

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

        try:
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
        except IOError:
            pass

        for speed in ["seq", "rand"]:
            try:
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
            except IOError:
                pass

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
            if len(parts) < 6:
                continue

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

    metrics = OrderedDict([
        ("storetime", "Time to compress from memory to the compressed format."),
        ("peakmem", "Peak memory usage during compression, in megabytes."),
        ("bitspernode", "The number of bits per node in the compressed format."),
        ("bitsperlink", "The number of bits per link in the compressed format."),
        ("size", "Total size of the compressed graph, in megabytes."),
        ("compratio", "Compression ratio compared with the uncompressed graph."),
        ("rand_ns/node", "Time to access a random node, in microseconds."),
        ("rand_ns/link", "Time to access a single link of a node, in microseconds."),
        ("seq_time", "Sequential access time, in seconds."),
        ("avgref", "Average reference chain length."),
    ])

    for metric, metric_name in metrics.iteritems():
        if metric == "avgref":
            # Only applicable on copy algorithms
            algos = OrderedDict([(k, v) for k, v in types.items() if k.startswith("copy")])
        elif metric == "compratio":
            algos = OrderedDict([(k, v) for k, v in types.items() if k != "none"])
        else:
            algos = types

        if metric == "peakmem":
            printer = lambda p, n: "{:.0f}".format(float(p.metrics[metric])/1024.0)
        elif metric == "bitspernode" or metric == "bitsperlink":
            printer = lambda p, n: "{:.0f}".format(float(p.metrics[metric]))
        elif metric == "size":
            printer = lambda p, n: "{:.0f}".format(float(p.metrics[metric])/(1024.0*1024.0))
        elif metric == "compratio":
            printer = lambda p, n: "{:.0f}\\%".format(100.0-100.0*float(p.metrics["size"])/float(sets[n].compressions["none"][None].metrics["size"]))
        elif metric == "rand_ns/node" or metric == "rand_ns/link":
            printer = lambda p, n: "{:.2f}".format(float(p.metrics[metric]) / 1000.0)
        else:
            printer = lambda p, n: str(p.metrics[metric])

        print_table(metric, metric_name, sets, algos, printer)

    print("""\\end{document}""")

if __name__ == "__main__":
    main(sys.argv[1:])
