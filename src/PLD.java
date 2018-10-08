import java.util.Random;

public class PLD {
    private Random random = new Random(Args.seed);
    float p;

    public PLD() {
        this.p = random.nextFloat();
    }

    public boolean isDrop() {
        if (p > Args.pDrop)
            return true;
        return false;
    }

    public boolean isDuplicate() {
        if (p > Args.pDuplicate)
            return true;
        return false;
    }

    public boolean isCorrupt() {
        if (p > Args.pCorrupt)
            return true;
        return false;
    }

    public boolean isOrder() {
        if (p > Args.pOrder)
            return true;
        return false;
    }

    public boolean isDelay() {
        if (p > Args.pDelay)
            return true;
        return false;
    }
}

