import glob
import pprint
import subprocess
from pathlib import Path

class Example:
    def __init__(self, name, A, Bs):
        self.name = name
        self.A = A
        self.Bs = Bs
        self.B = ""

    def __repr__(self):
        return str(len(self.Bs))

    def acopy(self):
        subprocess.run(["cp", self.A[0], "resources/svcomp_examples_processed/" + self.name + "_A.ats"])
        self.A = "resources/svcomp_examples_processed/" + self.name + "_A.ats"

    def bunion(self):
        successful = False
        with open("../../Ultimate/bunion.ats", "w") as bu:
            for bfile in self.Bs:
                bu.write("parseAutomata(\"../src/test/" + bfile[0] + "\");\n")
            bu.write("NestedWordAutomaton union0 = shrinkNwa(removeUnreachable("+self.Bs[0][1] + "));\n")
            for i in range(1, len(self.Bs)):
                bu.write("NestedWordAutomaton union"+ str(i) + " = shrinkNwa(union(union" + str(i-1) + ", " + self.Bs[i][1] + "));\n")
            bu.write("print(relabel(union"+str(len(self.Bs)-1)+"));\n")
        try:
            bp = subprocess.run(["./AutomataScriptInterpreter.sh", "bunion.ats"], timeout=60, cwd="../../Ultimate/", stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
            if not bp.returncode and "NestedWordAutomaton automaton = (\n" in bp.stdout:
                self.B = "resources/svcomp_examples_processed/" + self.name + "_Bunion.ats"
                with open("resources/svcomp_examples_processed/" + self.name + "_Bunion.ats", "w") as b:
                    b.write(bp.stdout[bp.stdout.find("NestedWordAutomaton"):bp.stdout.find("\n);\n")+4])
                    successful = True
        except subprocess.TimeoutExpired:
            print("Example: {} timed out.".format(self.name))
        return successful


examples = {}
for atsfile in glob.iglob('resources/svcomp_examples/*.ats'):
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

pp = pprint.PrettyPrinter(indent=4)
# pp.pprint(examples)
examples_nonempty = {key : examples[key] for key in examples if examples[key].A and examples[key].Bs}
finished_unions = 0
count = 0
for example in examples_nonempty:
    if not Path("resources/svcomp_examples_processed/" + example + "_A.ats").is_file():
        examples_nonempty[example].acopy()
    else:
        examples_nonempty[example].A = "resources/svcomp_examples_processed/" + example + "_A.ats"
    if not Path("resources/svcomp_example_processed/" + example + "_Bunion.ats").is_file():
        print(str(count) + ": Running union calculation for " + example + ".")
        if examples_nonempty[example].bunion():
            finished_unions += 1
    else:
        examples_nonempty[example].B = "resources/svcomp_examples_processed/" + example + "_Bunion.ats"
    count += 1
print("Total examples: {}".format(len(examples)))
print("Total examples without missing compoments: {}".format(len(examples_nonempty)))
print("Total computed unions: {}".format(finished_unions))

