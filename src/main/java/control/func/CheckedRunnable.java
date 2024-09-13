package control.func;

@FunctionalInterface
public interface CheckedRunnable {
    void run() throws Throwable;
}
