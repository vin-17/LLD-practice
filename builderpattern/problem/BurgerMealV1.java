package builderpattern.problem;
import java.util.*;

// Represents a customizable Burger Meal
class BurgerMeal {
    // Mandatory components
    private String bun;
    private String patty;

    // Optional components
    private String sides;
    private List<String> toppings;
    private boolean cheese;

    // Constructor trying to handle all combinations
    public BurgerMeal(String bun, String patty, String sides, List<String> toppings, boolean cheese) {
        this.bun = bun;
        this.patty = patty;
        this.sides = sides;
        this.toppings = toppings;
        this.cheese = cheese;
    }
}

// telescopiing constructor anti pattern:
// class BurgerMeal {
//     public BurgerMeal(String bun, String patty) { ... }
//     public BurgerMeal(String bun, String patty, boolean cheese) { ... }
//     public BurgerMeal(String bun, String patty, boolean cheese, String side) { ... }
//     public BurgerMeal(String bun, String patty, boolean cheese, String side, String drink) { ... }
// }

class Main {
    public static void main(String[] args) {
        // Constructing the object with only required details
        BurgerMeal burgerMeal = new BurgerMeal("wheat", "veg", null, null, false);
    }
}
