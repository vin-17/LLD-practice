package strategypattern.solution;

class Order {
    private double totalWeight;
    private String destinationZone;
    private double orderValue;

    public Order() {
        this.totalWeight = 5.0; // in kg
        this.destinationZone = "ZoneA"; // Example zone
        this.orderValue = 100.0; // in dollars
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public String getDestinationZone() {
        return destinationZone;
    }

    public double getOrderValue() {
        return orderValue;
    }
}