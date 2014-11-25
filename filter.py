# Imports
import sys
from glob import glob
import re

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

        m = re.match(regex, experiment)
        print(m.groups())
        name, compression, params = m.groups()

        if name not in sets:
            sets[name] = Dataset(name)

        if compression not in sets[name].compressions:
            sets[name].compressions[compression] = []

        data = Compression(compression, params)

        prop_file = open(p)
        for line in prop_file:
            if "=" not in line:
                continue

            prop, value = line[:-1].split("=")
            if prop in property_filter:
                print(prop, value)
                data.metrics[prop] = value

        prop_file.close()

        sets[name].compressions[compression].append(data)


if __name__ == "__main__":
    main(sys.argv[1:])
