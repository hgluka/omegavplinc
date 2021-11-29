if __name__ == '__main__':
    import sys
    if len(sys.argv) < 3:
        exit()
    states = []
    alphabet = []
    transitions = []
    initial_state = ""
    final_states = []
    with open(sys.argv[1]) as file:
        lines = file.readlines()
        initial_state = "q" + str(lines[0][1:-2])
        for line in lines[1::]:
            if line[0] == '[':
                if line[-1] == '\n':
                    q = "q" + str(line[1:-2])
                else:
                    q = "q" + str(line[1:-1])
                if q not in states:
                    states.append(q)
                if q not in final_states:
                    final_states.append(q)
            else:
                a = line.split(",")[0]
                if a not in alphabet:
                    alphabet.append(a)
                q1 = "q" + line.split(",")[1].split("->")[0][1:-1]
                q2 = "q" + line.split(",")[1].split("->")[1][1:-2]
                if q1 not in states:
                    states.append(q1)
                if q2 not in states:
                    states.append(q2)
                transitions.append((q1, a, q2))
    print("=====================================")
    print(states)
    print(alphabet)
    print(initial_state)
    print(final_states)

    with open("resources/" + sys.argv[2], "w") as file:
        file.write("NestedWordAutomaton nwa = (\n")
        file.write("\tcallAlphabet = {},\n")
        file.write("\tinternalAlphabet = {" + " ".join(alphabet) + "},\n")
        file.write("\treturnAlphabet = {},\n")
        file.write("\tstates = {" + " ".join(states) + "},\n")
        file.write("\tinitialStates = {" + initial_state + "},\n")
        file.write("\tfinalStates = {" + " ".join(final_states) + "},\n")
        file.write("\tcallTransitions = {\n\t},\n")
        file.write("\tinternalTransitions = {\n")
        for tr in transitions:
            file.write("\t\t(" + " ".join(tr) + ")\n")
        file.write("\t},\n")
        file.write("\treturnTransitions = {\n\t},\n")
        file.write(");\n")