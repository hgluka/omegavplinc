package omegaVPLinc.automaton;

import java.util.*;

public class Context {
    private final LinkedList<Symbol> word;
    private final Map<State, Set<State>> ctx;
    private final Map<State, Set<State>> finalCtx;
    private final boolean withFinal;

    public Context() {
        this.word = null;
        this.ctx = null;
        this.finalCtx = null;
        this.withFinal = false;
    }

    public Context(LinkedList<Symbol> word, Map<State, Set<State>> ctx) {
        this.word = word;
        this.ctx = ctx;
        this.finalCtx = null;
        this.withFinal = false;
    }

    public Context(LinkedList<Symbol> word, Map<State, Set<State>> ctx, Map<State, Set<State>> finalCtx) {
        this.word = word;
        this.ctx = ctx;
        this.finalCtx = finalCtx;
        this.withFinal = true;
    }

    public static Context compose(Context e, Context d) {
        if (e.withFinal && d.withFinal) {
            Map<State, Set<State>> ctx = composeM(e.ctx, d.ctx);
            Map<State, Set<State>> finalCtx = union(composeM(e.ctx, d.finalCtx), composeM(e.finalCtx, d.ctx));
            return new Context(concatWord(e.word, d.word), ctx, finalCtx);
        } else {
            return new Context(concatWord(e.word, d.word), composeM(e.ctx, d.ctx));
        }
    }

    public static Context compose(Symbol c, Context e, Symbol r) {
        if (e.withFinal) {
            Map<State, Set<State>> ctx = cerM(c, e.ctx, r);
            Map<State, Set<State>> finalCtx = new HashMap<>();
            for (State pPrime : e.ctx.keySet()) {
                for (State qPrime : e.ctx.get(pPrime)) {
                    for (State p : pPrime.getCallPredecessors(c)) {
                        for (State q : qPrime.getReturnSuccessors(r, p.getName())) {
                            if ((e.finalCtx.containsKey(pPrime) && e.finalCtx.get(pPrime).contains(qPrime))
                                    || p.isFinal() || q.isFinal()) {
                                finalCtx.computeIfAbsent(p, k -> new HashSet<>()).add(q);
                            }
                        }
                    }
                }
            }
            return new Context(concatWord(c, e.word, r), ctx, finalCtx);
        } else {
            return new Context(concatWord(c, e.word, r), cerM(c, e.ctx, r));
        }
    }

    public static Set<Context> compose(Set<Context> E, Set<Context> D) {
        Set<Context> ED = new HashSet<>();
        if (E.isEmpty() || D.isEmpty())
            return ED;
        for (Context e : E) {
            for (Context d : D) {
                Context ed = compose(e, d);
                ED.add(ed);
            }
        }
        return ED;
    }

    public static Set<Context> compose(Symbol c, Set<Context> E, Symbol r) {
        Set<Context> cEr = new HashSet<>();
        for (Context e : E) {
            cEr.add(compose(c, e, r));
        }
        return cEr;
    }

    public static Map<State, Set<State>> composeM(Map<State, Set<State>> e, Map<State, Set<State>> d) {
        Map<State, Set<State>> ed = new HashMap<>();
        if (e.isEmpty() || d.isEmpty())
            return ed;
        for (Map.Entry<State, Set<State>> entryE : e.entrySet()) {
            for (State q : entryE.getValue()) {
                if (d.containsKey(q))
                    ed.computeIfAbsent(entryE.getKey(), k -> new HashSet<>()).addAll(d.get(q));
            }
        }
        return ed;
    }

    public static Map<State, Set<State>> cerM(Symbol c, Map<State, Set<State>> e, Symbol r) {
        Map<State, Set<State>> cer = new HashMap<>();
        for (State pPrime : e.keySet()) {
            for (State qPrime : e.get(pPrime)) {
                for (State p : pPrime.getCallPredecessors(c)) {
                    for (State q : qPrime.getReturnSuccessors(r, p.getName())) {
                        cer.computeIfAbsent(p, k -> new HashSet<>()).add(q);
                    }
                }
            }
        }
        return cer;
    }

    public static Map<State, Set<State>> union(Map<State, Set<State>> e, Map<State, Set<State>> d) {
        Map<State, Set<State>> union = new HashMap<>(e);
        for (Map.Entry<State, Set<State>> entry : d.entrySet()) {
            union.computeIfAbsent(entry.getKey(), k -> new HashSet<>()).addAll(entry.getValue());
        }
        return union;
    }

    private static boolean addTransitive(Map<State, Set<State>> m) {
        boolean added = false;
        for (State p1 : m.keySet()) {
            for (State q1 : new HashSet<>(m.get(p1))) {
                for (State q2 : new HashSet<>(m.getOrDefault(q1, new HashSet<>()))) {
                    if (!m.get(p1).contains(q2)) {
                        m.get(p1).add(q2);
                        added = true;
                    }
                }
            }
        }
        return added;
    }

    public static Map<State, Set<State>> transitiveClosure(Map<State, Set<State>> m) {
        Map<State, Set<State>> transitiveClosure = new HashMap<>(m);
        boolean added;
        do {
            added = addTransitive(transitiveClosure);
        } while (added);
        return transitiveClosure;
    }

    private static LinkedList<Symbol> concatWord(LinkedList<Symbol> w1, LinkedList<Symbol> w2) {
        LinkedList<Symbol> w = new LinkedList<>(w1);
        w.addAll(w2);
        return w;
    }

    private static LinkedList<Symbol> concatWord(Symbol c, LinkedList<Symbol> w1, Symbol r) {
        LinkedList<Symbol> w = new LinkedList<>(w1);
        w.addFirst(c);
        w.addLast(r);
        return w;
    }

    public LinkedList<Symbol> getWord() {
        return word;
    }

    public Map<State, Set<State>> getCtx() {
        return ctx;
    }

    public Map<State, Set<State>> getFinalCtx() {
        return finalCtx;
    }

    public boolean isWithFinal() {
        return withFinal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Context context = (Context) o;
        return Objects.equals(ctx, context.ctx) && Objects.equals(finalCtx, context.finalCtx);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, ctx, finalCtx, withFinal);
    }
}
