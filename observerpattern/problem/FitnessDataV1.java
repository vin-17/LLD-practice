package observerpattern.problem;

class LiveActivityDisplayNaive {
    public void showStats(int steps, int activeMinutes, int calories) {
        System.out.println("LiveActivityDisplayNaive: Displaying stats - Steps: " + steps +
            ", ActiveMins: " + activeMinutes + ", Calories: " + calories);
    }
}

class ProgressLoggerNaive {
    public void logDataPoint(int steps, int activeMinutes, int calories) {
        System.out.println("ProgressLoggerNaive: Logging data point - Steps: " + steps +
            ", ActiveMins: " + activeMinutes + ", Calories: " + calories);
    }
}

class NotificationServiceNaive {
    public void checkAndNotify(int steps) {
        if (steps >= 10000) {
            System.out.println("NotificationServiceNaive: Congratulations! You've reached your step goal!");
        }
    }

    public void resetDailyNotifications() {
        System.out.println("NotificationServiceNaive: Daily notifications reset.");
    }
}

class FitnessDataV1 {
    private int steps;
    private int activeMinutes;
    private int calories;

    // Direct, hardcoded references to all dependent modules
    private LiveActivityDisplayNaive liveDisplay = new LiveActivityDisplayNaive();
    private ProgressLoggerNaive progressLogger = new ProgressLoggerNaive();
    private NotificationServiceNaive notificationService = new NotificationServiceNaive();

    public void newFitnessDataPushed(int newSteps, int newActiveMinutes, int newCalories) {
        this.steps = newSteps;
        this.activeMinutes = newActiveMinutes;
        this.calories = newCalories;

        System.out.println("\nFitnessDataV1: New data received - Steps: " + steps +
            ", ActiveMins: " + activeMinutes + ", Calories: " + calories);

        // Manually notify each dependent module
        liveDisplay.showStats(steps, activeMinutes, calories);
        progressLogger.logDataPoint(steps, activeMinutes, calories);
        notificationService.checkAndNotify(steps);
    }

    public void dailyReset() {
        // Reset logic...
        if (notificationService != null) {
            notificationService.resetDailyNotifications();
        }
        System.out.println("FitnessDataV1 : Daily data reset.");
        newFitnessDataPushed(0, 0, 0); // Notify with reset state
    }
}


class FitnessAppNaiveClient {
    public static void main(String[] args) {
        FitnessDataV1 fitnessData = new FitnessDataV1();

        fitnessData.newFitnessDataPushed(500, 5, 20);
        fitnessData.newFitnessDataPushed(9800, 85, 350);
        fitnessData.newFitnessDataPushed(10100, 90, 380);
        fitnessData.dailyReset();
    }
}
