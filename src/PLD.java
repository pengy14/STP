import java.util.Random;

public class PLD {
    private static Random random = new Random(Args.seed);
    private static float p = random.nextFloat();

    public static boolean isDrop() {
        if (p > Args.pDrop)
            return true;
        return false;
    }

    public static boolean isDuplicate() {
        if (p > Args.pDuplicate)
            return true;
        return false;
    }

    public static boolean isCorrupt() {
        if (p > Args.pCorrupt)
            return true;
        return false;
    }

    public static boolean isOrder() {
        if (p > Args.pOrder)
            return true;
        return false;
    }

    public static boolean isDelay(){
        if (p > Args.pDelay)
            return true;
        return false;
    }
}

