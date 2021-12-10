if __name__ == '__main__':
    import sys, traceback
    if len(sys.argv) < 3:
        sys.exit()
    parser_state = "ALPHABET"
    states = []
    call_alphabet = []
    internal_alphabet = []
    return_alphabet = []
    call_transitions = []
    internal_transitions = []
    return_transitions = []
    initial_state = ""
    final_states = []
    try:
        with open(sys.argv[1]) as file:
            lines = file.readlines()
            for line in lines[1::]:
                if line == "alphabet;\n":
                    parser_state = "ALPHABET"
                    continue
                elif line == "states;\n":
                    parser_state = "STATES"
                    continue
                elif line == "stack;\n":
                    parser_state = "STACK"
                    continue
                elif line == "transitions;\n":
                    parser_state = "TRANSITIONS"
                    continue
                elif parser_state == "ALPHABET":
                    if line.split(" ")[0].isnumeric():
                        if line.split(" ")[1] == "<":
                            call_alphabet.append("c" + line.split(" ")[0])
                        elif line.split(" ")[1] == ">":
                            return_alphabet.append("r" + line.split(" ")[0])
                        else:
                            internal_alphabet.append("a" + line.split(" ")[0])
                elif parser_state == "STATES":
                    if line.split(" ")[0].isnumeric():
                        states.append("q" + line.split(" ")[0])
                        if line.split(" ")[1] == "2":
                            final_states.append("q" + line.split(" ")[0])
                elif parser_state == "STACK":
                    if not line.split(" ")[0].isnumeric():
                        print(line)
                        initial_state = "q" + line.split(" ")[1][:-2]
                elif parser_state == "TRANSITIONS":
                    if line == "transitions;\n":
                        continue
                    if len(line.split(" ")) == 3 and line.split(" ")[2][0] == "(":
                        call_transitions.append(("q" + line.split(" ")[0], "c" + line.split(" ")[1], "q" + line.split(" ")[2][1:].split(",")[0]))
                    elif len(line.split(" ")) == 3 and line.split(" ")[2][0] != "(":
                        internal_transitions.append(("q" + line.split(" ")[0], "a" + line.split(" ")[1], "q" + line.split(" ")[2][:-2]))
                    elif len(line.split(" ")) == 4:
                        return_transitions.append(("q" + line.split(" ")[0], "q" + line.split(" ")[1], "r" + line.split(" ")[2], "q" + line.split(" ")[3][:-2]))
    except Exception as e:
        traceback.print_exc()
        print(sys.argv[1])
        sys.exit()
    print("=====================================")
    print(states)
    print(call_alphabet)
    print(internal_alphabet)
    print(return_alphabet)
    print(initial_state)
    print(final_states)

    with open("resources/" + sys.argv[2], "w") as file:
        file.write("NestedWordAutomaton nwa = (\n")
        file.write("\tcallAlphabet = {" + " ".join(call_alphabet) + "},\n")
        file.write("\tinternalAlphabet = {" + " ".join(internal_alphabet) + "},\n")
        file.write("\treturnAlphabet = {" + " ".join(return_alphabet) + "},\n")
        file.write("\tstates = {" + " ".join(states) + "},\n")
        file.write("\tinitialStates = {" + initial_state + "},\n")
        file.write("\tfinalStates = {" + " ".join(final_states) + "},\n")
        file.write("\tcallTransitions = {\n")
        for tr in call_transitions:
            file.write("\t\t(" + " ".join(tr) + ")\n")
        file.write("\t},\n")
        file.write("\tinternalTransitions = {\n")
        for tr in internal_transitions:
            file.write("\t\t(" + " ".join(tr) + ")\n")
        file.write("\t},\n")
        file.write("\treturnTransitions = {\n")
        for tr in return_transitions:
            file.write("\t\t(" + " ".join(tr) + ")\n")
        file.write("\t}\n")
        file.write(");\n")
