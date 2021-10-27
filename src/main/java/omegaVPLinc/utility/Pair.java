package omegaVPLinc.utility;

public record Pair<T, U>(T fst, U snd) {
    public static <T, U> Pair<T, U> of(T fst, U snd) {
        return new Pair<>(fst, snd);
    }
}