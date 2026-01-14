package com.bibliodesk.model;

public enum Profile {
    ADMIN("ADMIN"),
    BIBLIOTECARIO("BIBLIOTECÁRIO"),
    LEITOR("LEITOR");

    private final String label;

    Profile(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
