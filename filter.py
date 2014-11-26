# Imports
import sys
from glob import glob
import re
import os

class Dataset:
    def __init__(self, name):
        self.name = name
        self.compressions = {}

class Compression:
    def __init__(self, name, params=None):
        self.compression = name
        self.params = params
        self.metrics = {}

# Main function
def main(argv):
    # Constants
    types = ["none", "gaps", "interval", "copylist", "copyblocks", "copyflags"]
    regex = r"^(.*)-(" + "|".join(types) + r")(?:-(.*))?$"
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
    properties = glob(path + "/*.properties")
    for p in properties:
        experiment = p[len(path):-len(".properties")]
        prefix = path + "/" + experiment

        m = re.match(regex, experiment)
        print(m.groups())
        name, compression, params = m.groups()

        if name not in sets:
            sets[name] = Dataset(name)

        if compression not in sets[name].compressions:
            sets[name].compressions[compression] = {}

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

        print(data.metrics)
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
                print(m.groups(), size)
                name, compression, params = m.groups()
                sets[name].compressions[compression][params].metrics["size"] = size

        size_file.close()

if __name__ == "__main__":
    main(sys.argv[1:])
