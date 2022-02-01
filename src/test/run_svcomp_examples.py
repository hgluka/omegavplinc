import glob
import pprint
import subprocess

class Example:
    def __init__(self, name, A, Bs):
        self.name = name
        self.A = A
        self.Bs = Bs
        self.B = ""

    def __repr__(self):
        return str(len(self.Bs))

    def bunion(self):
        with open("bunion.ats", "w") as bu:
            for bfile in self.Bs:
                bu.write("parseAutomata(\"../src/test/" + bfile[0] + "\");\n")
            bu.write("union0 = "+self.Bs[0][1] + ";\n")
            for i in range(1, len(self.Bs)):
                bu.write("union"+ str(i) + " = union(union" + str(i-1) + ", " + self.Bs[i][1] + ");\n")
            bu.write("print(union"+str(len(self.Bs)-2)+");\n")




examples = {}

for atsfile in glob.iglob('resources/sv-comp-examples/*.ats'):
    prefix = atsfile[:atsfile.rfind("_")].split("/")[2]
    suffix = atsfile[atsfile.rfind("_")+1:]
    if prefix not in examples:
        A = ()
        Bs = []
        if "BuchiCegarLoopAbstraction" in suffix:
            A = (atsfile, suffix.split(".")[0])
        else:
            Bs = [(atsfile, suffix.split(".")[0])]
        examples[prefix] = Example(prefix, A, Bs)
    else:
        if "BuchiCegarLoopAbstraction" in suffix:
            examples[prefix].A = (atsfile, suffix.split(".")[0])
        else:
            examples[prefix].Bs.append((atsfile, suffix.split(".")[0]))
    # subprocess.run(["sed", "-i", "5s|nwa|"+suffix.split(".")[0]+"|", atsfile], stdout=subprocess.PIPE, universal_newlines=True)

pp = pprint.PrettyPrinter(indent=4)
#pp.pprint(examples)
examples_nonempty = {key : examples[key] for key in examples if examples[key].A and examples[key].Bs}
print(len(examples))
print(len(examples_nonempty))
examples_nonempty["Ackermann01-1.c"].bunion()
