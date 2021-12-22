if __name__ == '__main__':
    import sys
    if len(sys.argv) < 5:
        sys.exit()
    with open(sys.argv[1]) as file1:
        G = {}
        for line in file1.readlines():
            p = line.strip().split()[0]
            q = line.strip().split()[1]
            try:
                G[p].add(q)
            except KeyError:
                G[p] = {q}
        with open(sys.argv[2]) as file2:
            T = [tuple(line.strip().split()) for line in file2.readlines()]
            new_return_transitions = set()
            for p in G:
                for q in G[p]:
                    for transition in T:
                        if transition[0] == q:
                            r = transition[3]
                            try:
                                for s in G[r]:
                                    new_return_transitions.add((p, transition[1], transition[2], s))
                            except KeyError:
                                continue
            print(len(new_return_transitions))
            with open("resources/opennwa_friedmann_examples/new_return_transitions.txt", 'w') as file3:
                for new_transition in new_return_transitions:
                    file3.write("(q" + new_transition[0] + " q" + new_transition[1] + " r" + new_transition[2] + " q" + new_transition[3] +")\n")
        with open(sys.argv[3]) as file4:
            T = [tuple(line.strip().split()) for line in file4.readlines()]
            new_call_transitions = set()
            for p in G:
                for q in G[p]:
                    for transition in T:
                        if transition[0] == q:
                            r = transition[2]
                            try:
                                for s in G[r]:
                                    new_call_transitions.add((p, transition[1], s))
                            except KeyError:
                                continue
            print(len(new_call_transitions))
            with open("resources/opennwa_friedmann_examples/new_call_transitions.txt", 'w') as file3:
                for new_transition in new_call_transitions:
                    file3.write("(q" + new_transition[0] + " c" + new_transition[1] + " q" + new_transition[2] +")\n")
        with open(sys.argv[4]) as file5:
            T = [tuple(line.strip().split()) for line in file5.readlines()]
            new_internal_transitions = set()
            for p in G:
                for q in G[p]:
                    for transition in T:
                        if transition[0] == q:
                            r = transition[2]
                            try:
                                for s in G[r]:
                                    new_internal_transitions.add((p, transition[1], s))
                            except KeyError:
                                continue
            print(len(new_internal_transitions))
            with open("resources/opennwa_friedmann_examples/new_internal_transitions.txt", 'w') as file3:
                for new_transition in new_internal_transitions:
                    file3.write("(q" + new_transition[0] + " a" + new_transition[1] + " q" + new_transition[2] +")\n")
