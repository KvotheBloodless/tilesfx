package eu.hansolo.tilesfx.tools;

import eu.hansolo.tilesfx.ValueObject;
import javafx.scene.paint.Color;

public enum Boat {

    SAILING("Sailing");

    private ValueObject valueObject;
    private double value;
    private Color color;
    private String displayName;

    // ******************** Constructors **************************************
    Boat(final String DISPLAY_NAME) {

        valueObject = null;
        value = 0;
        color = null;
        displayName = DISPLAY_NAME;
    }

    // ******************** Methods *******************************************
    public String getName() {

        return name();
    }

    public ValueObject getValueObject() {

        return valueObject;
    }

    public void setValueObject(final ValueObject VALUE) {

        valueObject = VALUE;
    }

    public double getValue() {

        return value;
    }

    public void setValue(final double VALUE) {

        value = VALUE;
    }

    public Color getColor() {

        return color;
    }

    public void setColor(final Color COLOR) {

        color = COLOR;
    }

    public String getDisplayName() {

        return displayName;
    }
}