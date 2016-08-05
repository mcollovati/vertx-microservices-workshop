package io.vertx.workshop.dashboard.ui;

import java.util.Optional;
import java.util.stream.Stream;

public enum Company {
    DIVINATOR("Divinator"), MACROHARD("MacroHard"), BLACKCOAT("Black Coat");

    private final String companyName;

    Company(String companyName) {
        this.companyName = companyName;
    }

    @Override
    public String toString() {
        return companyName;
    }

    public String toKey() {
        return name().toLowerCase();
    }

    public static Optional<Company> fromName(String name) {
        return Stream.of(Company.values()).filter( c -> c.companyName.equals(name)).findFirst();
    }

}
