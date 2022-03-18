"""run_examples.py

Usage:
  run_examples.py --run [-n NUM] [--omegaVPLinc|--ultimate] [-i INPUT_DIR] [-o CSV_FILE] [-s SKIP_FILE]
  run_examples.py --unions [-i INPUT_DIR] [-s SKIP_FILE]
  run_examples.py --wordcheck CSV_FILE
  run_examples.py -h | --help

Options:
  -h --help                             Show this screen.
  -r --run                              Run the examples.
  -n NUM                                Specify number of examples to run. [default: 50]
  --omegaVPLinc                         Run examples only with omegaVPLinc.
  --ultimate                            Run examples only with Ultimate.
  -o CSV_FILE                           Specify output csv_file.
  -u --unions                           Calculate the unions.
  -i INPUT_DIR                          Specify input directory. [default: ./resources/svcomp_examples_processed]
  -s SKIP_FILE                          Specify file with examples to skip.
  -w CSV_FILE --wordcheck CSV_FILE      Check if word counter-examples
"""
import glob
import subprocess
import psutil
from pathlib import Path
import regex
import os
import csv
import string
import random
from docopt import docopt

class Example:
    def __init__(self, name, A, Bs, B):
        self.name = name
        self.A = A
        self.Bs = Bs
        self.B = B
        self.timeout = 3600/2 # 3600/2  # 30 seconds

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

    def run_wordcheck(self, output=False):
        env_with_java11 = {**os.environ, 'PATH': '/usr/lib/jvm/java-1.11.0-openjdk-amd64/bin:' + os.environ['PATH']}
        env_with_java17 = {**os.environ, 'PATH': '/usr/lib/jvm/java-1.17.0-openjdk-amd64/bin:' + os.environ['PATH']}
        lasso_word_str = ""
        try:
            rup = subprocess.run(["/bin/bash", "-c", "java -Xmx6g -jar build/libs/omegaVPLinc-1.0.jar --words src/test/" + self.A + " src/test/" + self.B], env=env_with_java17, timeout=self.timeout, cwd="../../", capture_output=True, universal_newlines=True)
            if output:
                with open("experiment_output/"+self.name+"_wordcheck_omegaVPLinc.txt", "w") as o:
                    o.write("STDOUT:\n")
                    o.write(rup.stdout)
                    o.write("\nSTDERR:\n")
                    o.write(rup.stderr)
            if not rup.returncode and "is not a subset of" in rup.stdout:
                lasso_word_str = regex.findall(r"Lasso word: (\[.+\])\n", rup.stdout)[0]
        except subprocess.TimeoutExpired:
            for proc in psutil.process_iter():
                if "java -Xmx6g -jar build/libs/omegaVPLinc-1.0.jar --words src/test/" + self.A + " src/test/" + self.B == " ".join(proc.cmdline()):
                    proc.terminate()
        with open("../../Ultimate/run_wordcheck.ats", "w") as rwc:
            rwc.write("parseAutomata(\"../src/test/" + self.A + "\");\n")
            rwc.write("parseAutomata(\"../src/test/" + self.B + "\");\n")
            rwc.write("NestedLassoWord word = " + lasso_word_str + ";\n")
            rwc.write("assert(buchiAccepts(A, word));\n")
            rwc.write("assert(!buchiAccepts(B, word));")
        try:
            rup = subprocess.run(["/bin/bash", "-c", "./AutomataScriptInterpreter.sh run_wordcheck.ats"], env=env_with_java11, timeout=self.timeout, cwd="../../Ultimate/", stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
            if output:
                with open("experiment_output/"+self.name+"_wordcheck_ultimate.txt", "w") as o:
                    o.write("STDOUT:\n")
                    o.write(rup.stdout)
                    o.write("\nSTDERR:\n")
                    o.write(rup.stderr)
            if not rup.returncode and "RESULT: Ultimate proved your program to be correct!" in rup.stdout:
                return True
            else:
                return False
        except subprocess.TimeoutExpired:
            for proc in psutil.process_iter():
                if "run_wordcheck.ats" in proc.cmdline():
                    proc.terminate()
            return False

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
            for proc in psutil.process_iter():
                if "run_example.ats" in proc.cmdline():
                    proc.terminate()
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
            for proc in psutil.process_iter():
                if "java -Xmx6g -jar build/libs/omegaVPLinc-1.0.jar src/test/" + self.A + " src/test/" + self.B == " ".join(proc.cmdline()):
                    proc.terminate()

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



def load_all_examples():
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
            examples[prefix] = Example(prefix, A, Bs, "")
        else:
            if "BuchiCegarLoopAbstraction" in suffix:
                examples[prefix].A = (atsfile, suffix.split(".")[0])
            else:
                examples[prefix].Bs.append((atsfile, suffix.split(".")[0]))
    return {key : examples[key] for key in examples if examples[key].A and examples[key].Bs}

def load_processed_examples(directory, skip_file = None):
    examples = {}
    to_skip = []
    if skip_file is not None:
        with open(skip_file, "r") as sf:
            for line in sf.readlines():
                if "," in line:
                    to_skip.append(line.split(",")[0])
                else:
                    to_skip.append(line.strip())
    if (directory[-1] == '/'):
        file_pattern = directory + "*.ats"
    else:
        file_pattern = directory + "/*.ats"
    for atsfile in glob.iglob(file_pattern):
        prefix = atsfile[:atsfile.rfind("_")].split("/")[2]
        suffix = atsfile[atsfile.rfind("_")+1:]
        if prefix in to_skip:
            continue
        if prefix not in examples:
            A = ""
            B = ""
            if suffix == "A.ats":
                A = atsfile
            elif suffix == "Bunion.ats":
                B = atsfile
            examples[prefix] = Example(prefix, A, [], B)
        else:
            if suffix == "A.ats":
                examples[prefix].A = atsfile
            elif suffix == "Bunion.ats":
                examples[prefix].B = atsfile
    return examples

def load_csv_examples(csv_file):
    processed = load_processed_examples('resources/svcomp_examples_processed')
    examples = {**processed, **load_processed_examples('resources/svcomp_examples_notdone')}
    keep = []
    with open(csv_file, "r") as cf:
        for line in cf.readlines()[1:]:
            if line.split(',')[4] not in ["-1.0", "-2.0"] and float(line.split(',')[4]) < 0:
                keep.append(line.split(',')[0])
    final = []
    for example in list(examples.keys()):
        if example in keep:
            final.append(examples[example])
    return final

def calculate_unions(examples):
    finished_unions = 0
    count = 0
    for example in list(examples.keys()):
        if not Path("resources/svcomp_examples_processed/" + example + "_Bunion.ats").is_file() and not Path("resources/svcomp_examples_notdone/" + example + "_Bunion.ats").is_file() and not Path("resources/svcomp_examples_notdone/" + example + "_Bunion.npvpa").is_file():
            print(str(count) + ": Running union calculation for " + example + ".")
            if examples[example].bunion():
                finished_unions += 1
                if not Path("resources/svcomp_examples_processed/" + example + "_A.ats").is_file() and not Path("resources/svcomp_examples_processed/" + example + "_Bunion.ats").is_file():
                    examples[example].acopy()
                else:
                    examples[example].A = "resources/svcomp_examples_processed/" + example + "_A.ats"
            else:
                subprocess.run(["mv", examples[example].A[0], "resources/svcomp_examples_timedout/"])
                for B in examples[example].Bs:
                    subprocess.run(["mv", B[0], "resources/svcomp_examples_timedout/"])
                print("Moved to different directory.")
                del examples[example]
        else:
            examples[example].B = "resources/svcomp_examples_processed/" + example + "_Bunion.ats"
            if not Path("resources/svcomp_examples_processed/" + example + "_A.ats").is_file() and not Path("resources/svcomp_examples_notdone/" + example + "_A.ats").is_file():
                examples[example].acopy()
            else:
                examples[example].A = "resources/svcomp_examples_processed/" + example + "_A.ats"
        count += 1

def run_processed(examples, file, num, omegavplinc, ultimate):
    with open(file, "w") as results:
        wr = csv.writer(results)
        wr.writerow(["example","A_states", "B_states", "omegaVPLinc", "ultimate"])
        count = 1
        for example in list(examples.keys())[:num]:
            if (omegavplinc):
                print(example + ": running in omegaVPLinc.")
                A_states, B_states, ort, osrt = examples[example].run_omegaVPLinc(output=True)
                print(example + ": finished in omegaVPLinc in " + str(ort) + " seconds.")
            else:
                A_states, B_states, ort, osrt = 0, 0, 0, 0
            if (ultimate):
                print(example + ": running in ultimate.")
                urt, usrt = examples[example].run_ultimate(output=True)
                print(example + ": finished in ultimate in " + str(urt) + " seconds.")
            else:
                urt, usrt = 0, 0
            print(str(count) + " -  " + example + ": " + str(A_states) + "(A_states), " + str(B_states) + "(B_states), " + str(ort) + "(omegaVPLinc), " + str(urt) + "(ultimate)")  # + str(frt) + "(fadecider)")
            wr.writerow([example, A_states, B_states, ort, urt])
            count += 1

if __name__=='__main__':
    arguments = docopt(__doc__, version='run_examples.py 1.0')
    def random_string(length):
        letters = string.ascii_lowercase
        return ''.join(random.choice(letters) for i in range(length))
    csv_file = "time_omegaVPLinc+ultimate_" + random_string(3) + ".csv"
    if arguments['-o'] is not None:
        csv_file = arguments['-o']
    skip_file = arguments['-s']
    if arguments['--run']:
        input_dir = 'resources/svcomp_examples_notdone'
        if arguments['-i'] is not None:
            input_dir = arguments['-i']
        print("Running examples and writing to: " + csv_file +".")
        examples = load_processed_examples(input_dir, skip_file)
        num = 50
        if arguments['-n'] is not None:
            num = int(arguments['-n'])
        ultimate = not arguments['--omegaVPLinc']
        omegavplinc = not arguments['--ultimate']
        run_processed(examples, csv_file, num, omegavplinc, ultimate)
        print("Written to: " + csv_file + ".")
    elif arguments['--unions']:
        print("Running union calculations.")
        examples = load_all_examples()
        calculate_unions(examples)
        print("Union calculations done.")
    elif arguments['--wordcheck']:
        print("Checking if counterexamples are correct.")
        examples = load_csv_examples(arguments['--wordcheck'])
        incorrect = []
        for example in examples:
            if not example.run_wordcheck():
                incorrect.append(example.name)
        if len(incorrect) == 0:
            print("All correct.")
        else:
            with open('wordcheck_incorrect_' + random_string(3) + ".txt", "w") as fout:
                for example in incorrect:
                    fout.write(example + "\n")

