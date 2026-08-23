/*
 * Promise API for async operations.
 */
package jdk.aprismate.async;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A Promise represents the eventual result of an asynchronous operation.
 * <p>
 * This is a wrapper around {@link CompletableFuture} with a more intuitive API
 * for chaining and error handling.
 * 
 * @param <T> the type of the value
 * @since 26.1
 */
public class Promise<T> {
    private final CompletableFuture<T> future;
    
    private Promise(CompletableFuture<T> future) {
        this.future = future;
    }
    
    /**
     * Creates a Promise from a CompletableFuture.
     */
    public static <T> Promise<T> fromCompletableFuture(CompletableFuture<T> future) {
        return new Promise<>(future);
    }
    
    /**
     * Creates a resolved Promise with the given value.
     */
    public static <T> Promise<T> resolve(T value) {
        return new Promise<>(CompletableFuture.completedFuture(value));
    }
    
    /**
     * Creates a rejected Promise with the given exception.
     */
    public static <T> Promise<T> reject(Throwable throwable) {
        return new Promise<>(CompletableFuture.failedFuture(throwable));
    }
    
    /**
     * Chains a success handler.
     */
    public <U> Promise<U> then(Function<? super T, ? extends U> fn) {
        return new Promise<>(future.thenApply(fn));
    }
    
    /**
     * Chains a success handler with void result.
     *
     * <p>Deliberately overloaded with {@link #then(Function)}.
     */
    @SuppressWarnings("overloads")
    public Promise<Void> then(Consumer<? super T> consumer) {
        return new Promise<>(future.thenAccept(consumer));
    }
    
    /**
     * Chains an error handler.
     */
    public Promise<T> catchError(Function<Throwable, ? extends T> fn) {
        return new Promise<>(future.exceptionally(fn));
    }
    
    /**
     * Chains an error handler with void result.
     *
     * <p>Deliberately overloaded with {@link #catchError(Function)}.
     */
    @SuppressWarnings("overloads")
    public Promise<T> catchError(Consumer<Throwable> consumer) {
        return new Promise<>(future.exceptionally(throwable -> {
            consumer.accept(throwable);
            return null;
        }));
    }
    
    /**
     * Returns the underlying CompletableFuture.
     */
    public CompletableFuture<T> toCompletableFuture() {
        return future;
    }
    
    /**
     * Blocks and gets the result.
     */
    public T get() throws Exception {
        return future.get();
    }
}
