import java.util.concurrent.*;
import java.util.*;

class SharedResource {
    private Queue<Integer> queue = new LinkedList<>();
    private final int CAPACITY = 5;
    private final Object lock = new Object();

    // Producer method
    public void produce(int value) throws InterruptedException {
        synchronized (lock) {
            while (queue.size() == CAPACITY) {
                System.out.println("Queue full, producer waiting...");
                lock.wait();
            }
            queue.add(value);
            System.out.println("Produced: " + value);
            lock.notifyAll(); // Notify consumers
        }
    }

    // Consumer method
    public int consume() throws InterruptedException {
        synchronized (lock) {
            while (queue.isEmpty()) {
                System.out.println("Queue empty, consumer waiting...");
                lock.wait();
            }
            int val = queue.poll();
            System.out.println("Consumed: " + val);
            lock.notifyAll(); // Notify producers
            return val;
        }
    }
}

// Producer Thread
class Producer extends Thread {
    private SharedResource resource;

    public Producer(SharedResource resource) {
        this.resource = resource;
    }

    public void run() {
        Random random = new Random();
        try {
            while (true) {
                int value = random.nextInt(100);
                resource.produce(value);
                Thread.sleep(500); // Simulate time delay
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// Consumer Thread
class Consumer extends Thread {
    private SharedResource resource;

    public Consumer(SharedResource resource) {
        this.resource = resource;
    }

    public void run() {
        try {
            while (true) {
                resource.consume();
                Thread.sleep(1000); // Simulate time delay
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// Deadlock Example
class Deadlock {
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();

    public void method1() {
        synchronized (lock1) {
            System.out.println("Thread-1: Holding lock1...");
            try { Thread.sleep(50); } catch (InterruptedException e) {}

            synchronized (lock2) {
                System.out.println("Thread-1: Holding lock1 & lock2...");
            }
        }
    }

    public void method2() {
        synchronized (lock2) {
            System.out.println("Thread-2: Holding lock2...");
            try { Thread.sleep(50); } catch (InterruptedException e) {}

            synchronized (lock1) {
                System.out.println("Thread-2: Holding lock2 & lock1...");
            }
        }
    }
}

// Callable & Future Example
class FactorialTask implements Callable<Long> {
    private int num;

    public FactorialTask(int num) {
        this.num = num;
    }

    public Long call() throws Exception {
        long result = 1;
        for (int i = 2; i <= num; i++) {
            result *= i;
        }
        return result;
    }
}

public class MultithreadingExample {
    public static void main(String[] args) {
        SharedResource resource = new SharedResource();
        Producer producer = new Producer(resource);
        Consumer consumer = new Consumer(resource);

        producer.start();
        consumer.start();

        // Deadlock demonstration
        Deadlock deadlock = new Deadlock();
        Thread t1 = new Thread(deadlock::method1);
        Thread t2 = new Thread(deadlock::method2);

        t1.start();
        t2.start();

        // Callable & Future Example
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Long> future = executor.submit(new FactorialTask(10));

        try {
            System.out.println("Factorial Result: " + future.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        executor.shutdown();
    }
}
