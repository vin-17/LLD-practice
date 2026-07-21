package singletonpattern;

//not thread safe
class LazySingleton {
    private static LazySingleton instance;

    // Private constructor prevents creating objects from outside the class
    private LazySingleton() {}

    // Global access point to get the Singleton instance
    public static LazySingleton getInstance() {
        if (instance == null) {
            instance = new LazySingleton();
        }
    
        return instance;
    }
}


class ThreadSafeSingleton {
    private static ThreadSafeSingleton instance;

    private ThreadSafeSingleton() {}

    public static synchronized ThreadSafeSingleton getInstance() {
        if (instance == null) {
            instance = new ThreadSafeSingleton();
        }

        return instance;
    }
}
// in this implementation, the getInstance() method is synchronized
// which ensures that only one thread can access it at a time. 
// This prevents multiple threads from creating separate instances of the Singleton class.
// but performance can be an issue due to the overhead of acquiring a lock every time the method is called.



class DoubleCheckedSingleton {
    // Holds the single shared instance (requires safe publication)
	// volatile prevents reordering of instructions in: instance = new Singleton()
	// since it involves three steps: allocate memory, call constructor, assign reference
    private static volatile DoubleCheckedSingleton instance;

    // Private constructor prevents external instantiation
    private DoubleCheckedSingleton() {}

    // Global access point to get the Singleton instance
    public static DoubleCheckedSingleton getInstance() {
        
		// Fast path: first check without locking
        if (instance == null) {
            // Lock only when the instance might need to be created
            synchronized (DoubleCheckedSingleton.class) {
                // Second check inside the lock (prevents double creation)
                if (instance == null) {
                    instance = new DoubleCheckedSingleton();
                }
            }
        }

        // Return the shared instance (existing or newly created)
        return instance;
    }
}


class EagerSingleton {
    // Holds the single shared instance (created immediately at class load time)
    private static final EagerSingleton instance = new EagerSingleton();

    // Private constructor prevents creating objects from outside the class
    private EagerSingleton() {}

    // Global access point to get the Singleton instance
    public static EagerSingleton getInstance() {
        // Return the already-created shared instance
        return instance;
    }
}
