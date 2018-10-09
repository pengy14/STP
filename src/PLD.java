import java.util.Random;

public class PLD {

    public boolean isDrop() {
         Random random = new Random(Args.seed);
        float p = random.nextFloat();
        if (p > Args.pDrop)
            return true;
        return false;
    }

    public boolean isDuplicate() {
        Random random = new Random(Args.seed);
        float p = random.nextFloat();
        if (p > Args.pDuplicate)
            return true;
        return false;
    }

    public boolean isCorrupt() {
        Random random = new Random(Args.seed);
        float  p = random.nextFloat();
        if (p > Args.pCorrupt)
            return true;
        return false;
    }

    public boolean isOrder() {
        Random random = new Random(Args.seed);
        float p = random.nextFloat();
        if (p > Args.pOrder)
            return true;
        return false;
    }

    public boolean isDelay() {
        Random random = new Random(Args.seed);
        float p = random.nextFloat();
        if (p > Args.pDelay)
            return true;
        return false;
    }
}

