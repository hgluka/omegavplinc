import glob
import pprint
import subprocess
from pathlib import Path
import regex
import os
import csv

class Example:
    def __init__(self, name, A, Bs):
        self.name = name
        self.A = A
        self.Bs = Bs
        self.B = ""
        self.timeout = 3600/2  # 30 seconds TODO: change to 3600 before running

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
            else:
                print("Union calculation for " + self.name + " wasn't successful.")
        except subprocess.TimeoutExpired:
            print("Example: {} timed out.".format(self.name))
        return successful

    def run_ultimate(self, output=False):
        env_with_java11 = {**os.environ, 'PATH': '/usr/lib/jvm/java-1.11.0-openjdk-amd64/bin:' + os.environ['PATH']}
        real_time = -1.0
        self_reported_time = -1.0
        subprocess.run(["sed", "-i", "s|BuchiCegarLoopAbstraction0 = (|A = (|", self.A])
        subprocess.run(["sed", "-i", "1s|automaton = (|B = (|", self.B])
        with open("../../Ultimate/run_example.ats", "w") as re:
            re.write("parseAutomata(\"../src/test/" + self.A + "\");\n")
            re.write("parseAutomata(\"../src/test/" + self.B + "\");\n")
            re.write("assert(buchiIsEmpty(buchiIntersect(A, buchiComplementFKV(B))));")
        try:
            rup = subprocess.run(["/bin/bash", "-c", "time -p ./AutomataScriptInterpreter.sh run_example.ats"], env=env_with_java11, timeout=self.timeout, cwd="../../Ultimate/", stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
            if output:
                with open("experiment_output/"+self.name+"_ultimate.txt", "w") as o:
                    o.write("STDOUT:\n")
                    o.write(rup.stdout)
                    o.write("\nSTDERR:\n")
                    o.write(rup.stderr)
            if not rup.returncode and "RESULT: Ultimate proved your program to be correct!" in rup.stdout:
                real_time = float(regex.search(r"real ([^\n]+)\n", rup.stderr).captures(1)[0])
                self_reported_time = sum(float(x)/1000 for x in regex.search("RUNTIME_TOTAL_MS=(\d+)}", rup.stdout).captures(1))
            elif not rup.returncode and "RESULT: Ultimate proved your program to be incorrect!" in rup.stdout:
                real_time = -float(regex.search(r"real ([^\n]+)\n", rup.stderr).captures(1)[0])
                self_reported_time = -sum(float(x)/1000 for x in regex.search("RUNTIME_TOTAL_MS=(\d+)}", rup.stdout).captures(1))
            else:
                real_time = -2.0
                self_reported_time = -2.0
        except subprocess.TimeoutExpired:
            pass
        return real_time, self_reported_time

    def run_omegaVPLinc(self, output=False):
        env_with_java17 = {**os.environ, 'PATH': '/usr/lib/jvm/java-1.17.0-openjdk-amd64/bin:' + os.environ['PATH']}
        real_time = -1.0
        self_reported_time = -1.0
        A_states = 0
        B_states = 0
        try:
            rup = subprocess.run(["/bin/bash", "-c", "time -p java -Xmx6g -jar build/libs/omegaVPLinc-1.0.jar src/test/" + self.A + " src/test/" + self.B], env=env_with_java17, timeout=self.timeout, cwd="../../", capture_output=True, universal_newlines=True)
            if output:
                with open("experiment_output/"+self.name+"_omegaVPLinc.txt", "w") as o:
                    o.write("STDOUT:\n")
                    o.write(rup.stdout)
                    o.write("\nSTDERR:\n")
                    o.write(rup.stderr)
            if not rup.returncode and "is a subset of" in rup.stdout:
                A_states = regex.findall(r"Automaton has (\d+) states.", rup.stdout)[0]
                B_states = regex.findall(r"Automaton has (\d+) states.", rup.stdout)[1]
                real_time = float(regex.search(r"real ([^\n]+)\n", rup.stderr).captures(1)[0])
                self_reported_time = float(regex.search(r"The check took (\d+) milliseconds.", rup.stdout).captures(1)[0])/1000
            elif not rup.returncode and "is not a subset of" in rup.stdout:
                A_states = regex.findall(r"Automaton has (\d+) states.", rup.stdout)[0]
                B_states = regex.findall(r"Automaton has (\d+) states.", rup.stdout)[1]

                real_time = -float(regex.search(r"real ([^\n]+)\n", rup.stderr).captures(1)[0])
                self_reported_time = -float(regex.search(r"The check took (\d+) milliseconds.", rup.stdout).captures(1)[0])/1000
            else:
                real_time = -2.0
                self_reported_time = -2.0
        except subprocess.TimeoutExpired:
            pass
        return A_states, B_states, real_time, self_reported_time

    def run_fadecider(self, output=False):
        real_time = -1.0
        self_reported_time = -1.0
        with open("fadecider_script.sh", "w") as es:
            es.write("#!/bin/bash\ncat " + self.A.replace("processed", "npvpa").replace(".ats", ".npvpa") + " | ../../fadecider/bin/fadecider -s npvpa -pc -pr -a " + self.B.replace("processed", "npvpa").replace(".ats", ".npvpa"))
        subprocess.run(["chmod", "+x", "fadecider_script.sh"])
        try:
            rup = subprocess.run(["/bin/bash", "-c", "time -p ./fadecider_script.sh"], timeout=self.timeout, capture_output=True, universal_newlines=True)
            if output:
                with open("experiment_output/" + self.name +  "_fadecider.txt", "w") as o:
                    o.write("STDOUT:\n")
                    o.write(rup.stdout)
                    o.write("\nSTDERR:\n")
                    o.write(rup.stderr)
            if not rup.returncode and "Automaton is subsumed." in rup.stdout:
                real_time = float(regex.search(r"real ([^\n]+)\n", rup.stderr).captures(1)[0])
                self_reported_time = float(regex.search(r"t = (\d+\.\d+) sec", rup.stdout).captures(1)[0])
            else:
                real_time = -2.0
                real_time = -2.0
        except subprocess.TimeoutExpired:
            pass
        return real_time, self_reported_time




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
for example in list(examples_nonempty.keys()):
    if not Path("resources/svcomp_examples_processed/" + example + "_Bunion.ats").is_file() and not Path("resources/svcomp_examples_notdone/" + example + "_Bunion.ats").is_file() and not Path("resources/svcomp_examples_notdone/" + example + "_Bunion.npvpa").is_file():
        print(str(count) + ": Running union calculation for " + example + ".")
        if examples_nonempty[example].bunion():
            finished_unions += 1
            if not Path("resources/svcomp_examples_processed/" + example + "_A.ats").is_file() and not Path("resources/svcomp_examples_processed/" + example + "_Bunion.ats").is_file():
                examples_nonempty[example].acopy()
            else:
                examples_nonempty[example].A = "resources/svcomp_examples_processed/" + example + "_A.ats"
        else:
            subprocess.run(["mv", examples_nonempty[example].A[0], "resources/svcomp_examples_timedout/"])
            for B in examples_nonempty[example].Bs:
                subprocess.run(["mv", B[0], "resources/svcomp_examples_timedout/"])
            print("Moved to different directory.")
            del examples_nonempty[example]
    else:
        examples_nonempty[example].B = "resources/svcomp_examples_processed/" + example + "_Bunion.ats"
        if not Path("resources/svcomp_examples_processed/" + example + "_A.ats").is_file() and not Path("resources/svcomp_examples_notdone/" + example + "_A.ats").is_file():
            examples_nonempty[example].acopy()
        else:
            examples_nonempty[example].A = "resources/svcomp_examples_processed/" + example + "_A.ats"
    count += 1

print("Total examples: {}".format(len(examples)))
print("Total examples without missing compoments: {}".format(len(examples_nonempty)))
print("Total computed unions: {}".format(finished_unions))

done_examples = []
with open("time_results_new.csv", "r") as results:
    reader = csv.DictReader(results)
    for row in reader:
        done_examples.append(row['example'])

for example in list(examples_nonempty.keys()):
    if example not in done_examples:
        del examples_nonempty[example]

print("Total examples: {}".format(len(examples)))
print("Total examples to be tested: {}".format(len(examples_nonempty)))

with open("time_omegaVPLinc+ultimate.csv", "w") as results:
    wr = csv.writer(results)
    # wr.writerow(["example","A_states", "B_states", "omegaVPLinc", "ultimate"])
    wr.writerow(["ultimate"])
    count = 1
    for example in list(examples_nonempty.keys())[:50]:
        print(example + ": running in omegaVPLinc.")
        A_states, B_states, ort, osrt = examples_nonempty[example].run_omegaVPLinc(output=True)
        print(example + ": finished in omegaVPLinc in " + str(ort) + " seconds.")
        urt = -3.0
        if count > 25:
            print(example + ": running in ultimate.")
            urt, usrt = examples_nonempty[example].run_ultimate(output=True)
            print(example + ": finished in ultimate in " + str(urt) + " seconds.")
        print(str(count) + " -  " + example + ": " + str(A_states) + "(A_states), " + str(B_states) + "(B_states), " + str(ort) + "(omegaVPLinc), " + str(urt) + "(ultimate)")  # + str(frt) + "(fadecider)")
        wr.writerow([example, A_states, B_states, ort, urt])
        count += 1
