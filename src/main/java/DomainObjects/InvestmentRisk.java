package DomainObjects;

public enum InvestmentRisk {
    HIGH(4),
    MEDIUM(2),
    LOW(1);

    private final int value;
    InvestmentRisk(int value){
        this.value = value;
    }

    public int getValue(){return value;}
}
