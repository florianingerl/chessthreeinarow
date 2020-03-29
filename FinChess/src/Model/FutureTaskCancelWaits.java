package Model;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;

public class FutureTaskCancelWaits<T> extends FutureTask<T> {

    private final Semaphore semaphore;

    private FutureTaskCancelWaits(final Callable<T> callable, final Semaphore semaphore) {
        super(new Callable<T>() {
            public T call() throws Exception {
                semaphore.acquire();
                try {
                    return callable.call();
                } finally {
                    semaphore.release();
                }
            }
        });
        this.semaphore = semaphore;
    }
    
    
    public FutureTaskCancelWaits(Callable<T> callable) {
        this(callable, new Semaphore(1));
    }
    
    public FutureTaskCancelWaits(Runnable runnable) {
        this(Executors.callable(runnable, (T) null));
    }


    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        // If the task was successfully cancelled, block here until call() returns
        if (super.cancel(mayInterruptIfRunning)) {
            try {
                semaphore.acquire();
                // All is well
                return true;
            } catch (InterruptedException e) {
                // Interrupted while waiting...
            } finally {
                semaphore.release();
            }
        }
        return false;
    }

   
}
