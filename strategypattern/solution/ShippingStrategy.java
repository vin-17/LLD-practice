package strategypattern.solution;

interface ShippingStrategy {
    double calculateCost(Order order);
}

class FlatRateShippingStrategy implements ShippingStrategy {
    private double rate;

    public FlatRateShippingStrategy(double rate) {
        this.rate = rate;
    }

    @Override
    public double calculateCost(Order order) {
        System.out.println("Calculating with Flat Rate strategy ($" + rate + ")");
        return rate;
    }
}

class WeightBasedShippingStrategy implements ShippingStrategy {
    private double ratePerKg;

    public WeightBasedShippingStrategy(double ratePerKg) {
        this.ratePerKg = ratePerKg;
    }

    @Override
    public double calculateCost(Order order) {
        System.out.println("Calculating with Weight-Based strategy ($" + ratePerKg + " per kg)");
        return order.getTotalWeight() * ratePerKg;
    }
}

class DistanceBasedShippingStrategy implements ShippingStrategy {
    private double ratePerKm;

    public DistanceBasedShippingStrategy(double ratePerKm) {
        this.ratePerKm = ratePerKm;
    }

    @Override
    public double calculateCost(Order order) {
        System.out.println("Calculating with Distance-Based strategy for zone: " + order.getDestinationZone());
        return switch (order.getDestinationZone()) {
            case "ZoneA" -> ratePerKm * 5.0;
            case "ZoneB" -> ratePerKm * 7.0;
            default -> ratePerKm * 10.0;
        };
    }
}


// now we wanted to add a new strategy for third party API shipping, we can do so without modifying existing code.

class ThirdPartyApiShippingStrategy implements ShippingStrategy {
    private final double baseFee;

    public ThirdPartyApiShippingStrategy(double baseFee) {
        this.baseFee = baseFee;
    }

    @Override
    public double calculateCost(Order order) {
        System.out.println("Calculating with Third-Party API strategy.");
        // Simulate API call
        return baseFee + (order.getOrderValue());
    }
}