package hankyung.tossinvoice.domain.constant;

public enum CompanyType {
    INDIVIDUAL("개인"),
    CORPORATE("법인");

    private final String description;

    CompanyType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}