package strategypattern.problem;

class ShippingCostCalculatorNaive {
    public double calculateShippingCost(Order order, String strategyType) {
        double cost = 0.0;

        if ("FLAT_RATE".equalsIgnoreCase(strategyType)) {
            System.out.println("Calculating with Flat Rate strategy.");
            cost = 10.0;

        } else if ("WEIGHT_BASED".equalsIgnoreCase(strategyType)) {
            System.out.println("Calculating with Weight-Based strategy.");
            cost = order.getTotalWeight() * 2.5;

        } else if ("DISTANCE_BASED".equalsIgnoreCase(strategyType)) {
            System.out.println("Calculating with Distance-Based strategy.");
            if ("ZoneA".equals(order.getDestinationZone())) {
                cost = 5.0;
            } else if ("ZoneB".equals(order.getDestinationZone())) {
                cost = 12.0;
            } else {
                cost = 20.0; // fallback
            }

        } else if ("THIRD_PARTY_API".equalsIgnoreCase(strategyType)) {
            System.out.println("Calculating with Third-Party API strategy.");
            // Simulated external call
            cost = 7.5 + (order.getOrderValue() * 0.02);

        } else {
            throw new IllegalArgumentException("Unknown shipping strategy: " + strategyType);
        }

        System.out.println("Calculated Shipping Cost: $" + cost);
        return cost;
    }
}

public class ECommerceAppV1 {
    public static void main(String[] args) {
        ShippingCostCalculatorNaive calculator = new ShippingCostCalculatorNaive();
        Order order1 = new Order();

        System.out.println("--- Order 1 ---");
        calculator.calculateShippingCost(order1, "FLAT_RATE");
        calculator.calculateShippingCost(order1, "WEIGHT_BASED");
        calculator.calculateShippingCost(order1, "DISTANCE_BASED");
        calculator.calculateShippingCost(order1, "THIRD_PARTY_API");

        // What if we want to try a new "PremiumZone" strategy?
        // We have to go modify this calculator class again...
    }
}
