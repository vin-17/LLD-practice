package factorypattern.problem;


// Logistics Interface
interface LogisticsV1 {
    void send();
}

// Class implementing the Logistics Interface
class Road implements LogisticsV1 {
    @Override
    public void send() {
        System.out.println("Sending by road logic");
    }
}

// Class implementing the Logistics Interface
class Air implements LogisticsV1 {
    @Override
    public void send() {
        System.out.println("Sending by air logic");
    }
}

// Class implementing Logistics Service
class LogisticsService {
    public void send(String mode) {
        if (mode.equals("Air")) {
            LogisticsV1 logistics = new Air();
            logistics.send();
        } else if (mode.equals("Road")) {
            LogisticsV1 logistics = new Road();
            logistics.send();
        }
    }
}

// Driver code
class Main {
    public static void main(String[] args) {
        LogisticsService service = new LogisticsService();
        service.send("Air");
        service.send("Road");
    }
}
