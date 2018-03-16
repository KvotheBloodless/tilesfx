package eu.hansolo.tilesfx.skins;

import static eu.hansolo.tilesfx.tools.Helper.enableNode;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import eu.hansolo.tilesfx.Section;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.events.ChartDataEventListener;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.tools.Location;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

/**
 * Created by hansolo on 03.03.17.
 */
public class WindInstrumentTileSkin extends TileSkin {

    private static final double ANGLE_RANGE = 360;
    private double size;
    private double chartSize;
    private Arc barBackground;
    private Arc bar;
    private Line separator;
    private Text titleText;
    private Text text;
    private Text windSpeedText;
    private Text windSpeedUnitText;
    private TextFlow windSpeedFlow;
    private Text valueText;
    private TextFlow valueUnitFlow;
    private double minValue;
    private double range;
    private double angleStep = 1;
    private boolean sectionsVisible;
    private List<Section> sections;
    private String formatString;
    private Locale locale;
    private StackPane graphicContainer;
    private ChangeListener graphicListener;
    private ListChangeListener<ChartData> chartDataListener;
    private ChartDataEventListener apparentAngleListener;
    private ChartDataEventListener northDirectionListener;
    private InvalidationListener currentValueListener;

    private Canvas overlayCanvas;
    private GraphicsContext overlayCtx;

    private Optional<Double> northAngle = Optional.empty();

    // ******************** Constructors **************************************
    public WindInstrumentTileSkin(Tile TILE) {

        super(TILE);

        setBar(TILE.getAngleRange());
    }

    // ******************** Initialization ************************************
    @Override
    protected void initGraphics() {

        super.initGraphics();

        if (tile.isAutoScale())
            tile.calcAutoScale();
        minValue = tile.getMinValue();
        range = tile.getRange();
        sectionsVisible = tile.getSectionsVisible();
        sections = tile.getSections();
        formatString = new StringBuilder("%.").append(Integer.toString(tile.getDecimals())).append("f").toString();
        locale = tile.getLocale();

        overlayCanvas = new Canvas(tile.getPrefWidth(), tile.getPrefHeight());
        overlayCtx = overlayCanvas.getGraphicsContext2D();

        graphicListener = (o, ov, nv) -> {
            if (nv != null) {
                graphicContainer.getChildren().setAll(tile.getGraphic());
            }
        };

        currentValueListener = o -> {

            windSpeedText.setText(String.format(locale, formatString, tile.getCurrentValue()));
        };

        apparentAngleListener = e -> {

            if (northAngle.isPresent()) {

                double calculatedCardinal = e.getData().getValue() - northAngle.get();
                if (calculatedCardinal < 0)
                    calculatedCardinal += 360;
                if (calculatedCardinal >= 360)
                    calculatedCardinal -= 360;

                valueText.setText(Location.CardinalDirection.from(calculatedCardinal).name());
            }

            redraw();
        };

        northDirectionListener = e -> {

            northAngle = Optional.of(e.getData().getValue());

            redraw();
        };

        chartDataListener = c -> {

            redraw();
        };

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getTextColor());
        enableNode(text, tile.isTextVisible());

        barBackground = new Arc(tile.getPrefWidth() * 0.5, tile.getPrefHeight() * 0.5, tile.getPrefWidth() * 0.468,
                tile.getPrefHeight() * 0.468, 90, 360);
        barBackground.setType(ArcType.OPEN);
        barBackground.setStroke(tile.getBarBackgroundColor());
        barBackground.setStrokeWidth(tile.getPrefWidth() * 0.1);
        barBackground.setStrokeLineCap(StrokeLineCap.BUTT);
        barBackground.setFill(null);

        bar = new Arc(tile.getPrefWidth() * 0.5, tile.getPrefHeight() * 0.5, tile.getPrefWidth() * 0.468,
                tile.getPrefHeight() * 0.468, 90, 0);
        bar.setType(ArcType.OPEN);
        bar.setStroke(tile.getBarColor());
        bar.setStrokeWidth(tile.getPrefWidth() * 0.1);
        bar.setStrokeLineCap(StrokeLineCap.BUTT);
        bar.setFill(null);

        separator = new Line(tile.getPrefWidth() * 0.5, 1, tile.getPrefWidth() * 0.5, 0.16667 * tile.getPrefHeight());
        separator.setStroke(tile.getBackgroundColor());
        separator.setFill(Color.TRANSPARENT);

        windSpeedText = new Text(String.format(locale, formatString, tile.getCurrentValue()));
        windSpeedText.setFont(Fonts.latoRegular(tile.getPrefWidth() * 0.27333));
        windSpeedText.setFill(tile.getValueColor());
        windSpeedText.setTextOrigin(VPos.CENTER);

        windSpeedUnitText = new Text(tile.getUnit());
        windSpeedUnitText.setFont(Fonts.latoLight(tile.getPrefWidth() * 0.08));
        windSpeedUnitText.setFill(tile.getUnitColor());

        windSpeedFlow = new TextFlow(windSpeedText, windSpeedUnitText);
        windSpeedFlow.setTextAlignment(TextAlignment.CENTER);

        valueText = new Text("N");
        valueText.setFont(Fonts.latoRegular(tile.getPrefWidth() * 0.27333));
        valueText.setFill(tile.getValueColor());
        valueText.setTextOrigin(VPos.CENTER);
        enableNode(valueText, tile.isValueVisible());

        valueUnitFlow = new TextFlow(valueText);
        valueUnitFlow.setTextAlignment(TextAlignment.CENTER);

        graphicContainer = new StackPane();
        graphicContainer.setMinSize(size * 0.9, tile.isTextVisible() ? size * 0.72 : size * 0.795);
        graphicContainer.setMaxSize(size * 0.9, tile.isTextVisible() ? size * 0.72 : size * 0.795);
        graphicContainer.setPrefSize(size * 0.9, tile.isTextVisible() ? size * 0.72 : size * 0.795);
        graphicContainer.getChildren().setAll(tile.getGraphic());

        getPane().getChildren().addAll(barBackground, bar, separator, titleText, text, graphicContainer,
                windSpeedFlow, valueUnitFlow, overlayCanvas);
    }

    @Override
    protected void registerListeners() {

        super.registerListeners();

        // First element is apparent wind
        tile.getChartData().stream().findFirst().ifPresent(chartData -> chartData.addChartDataEventListener(apparentAngleListener));

        // Second element is north cardinal
        tile.getChartData().stream().skip(1).findFirst().ifPresent(
                chartData -> chartData.addChartDataEventListener(northDirectionListener));

        tile.getChartData().addListener(chartDataListener);
        tile.currentValueProperty().addListener(currentValueListener);
        tile.graphicProperty().addListener(graphicListener);
    }

    // ******************** Methods *******************************************
    @Override
    protected void handleEvents(final String EVENT_TYPE) {

        super.handleEvents(EVENT_TYPE);
    }

    private void setBar(final double VALUE) {

        if (minValue > 0) {
            bar.setStartAngle((360 - (((minValue - VALUE) / 2) * angleStep)) + 90);
            bar.setLength((minValue - VALUE) * angleStep);
        } else {
            bar.setStartAngle((360 - (-VALUE / 2) * angleStep) + 90);
            bar.setLength(-VALUE * angleStep);
        }
        setBarColor(VALUE);
    }

    private void setBarColor(final double VALUE) {

        if (!sectionsVisible) {
            bar.setStroke(tile.getBarColor());
        } else {
            bar.setStroke(tile.getBarColor());
            for (Section section : sections) {
                if (section.contains(VALUE)) {
                    bar.setStroke(section.getColor());
                    break;
                }
            }
        }
    }

    @Override
    public void dispose() {

        tile.getChartData().removeListener(chartDataListener);
        tile.getChartData().forEach(chartData -> chartData.removeChartDataEventListener(apparentAngleListener));
        tile.currentValueProperty().removeListener(currentValueListener);
        tile.graphicProperty().removeListener(graphicListener);
        super.dispose();
    }

    // ******************** Resizing ******************************************
    @Override
    protected void resizeStaticText() {

        double maxWidth = width - size * 0.1;
        double fontSize = size * textSize.factor;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) {
            Helper.adjustTextSize(titleText, maxWidth, fontSize);
        }
        switch (tile.getTitleAlignment()) {
            default:
            case LEFT:
                titleText.relocate(size * 0.05, size * 0.05);
                break;
            case CENTER:
                titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.05);
                break;
            case RIGHT:
                titleText.relocate(width - (size * 0.05) - titleText.getLayoutBounds().getWidth(), size * 0.05);
                break;
        }

        text.setFont(Fonts.latoRegular(fontSize));
        if (text.getLayoutBounds().getWidth() > maxWidth) {
            Helper.adjustTextSize(text, maxWidth, fontSize);
        }
        switch (tile.getTextAlignment()) {
            default:
            case LEFT:
                text.setX(size * 0.05);
                break;
            case CENTER:
                text.setX((width - text.getLayoutBounds().getWidth()) * 0.5);
                break;
            case RIGHT:
                text.setX(width - (size * 0.05) - text.getLayoutBounds().getWidth());
                break;
        }
        text.setY(height - size * 0.05);
    }

    @Override
    protected void resizeDynamicText() {

        double maxWidth = windSpeedUnitText.isVisible() ? chartSize * 0.7 : chartSize * 0.8;
        double fontSize = graphicContainer.isVisible() ? chartSize * 0.15 : chartSize * 0.2;
        windSpeedText.setFont(Fonts.latoRegular(fontSize));
        if (windSpeedText.getLayoutBounds().getWidth() > maxWidth) {
            Helper.adjustTextSize(windSpeedText, maxWidth, fontSize);
        }

        fontSize = graphicContainer.isVisible() ? chartSize * 0.07 : chartSize * 0.08;
        windSpeedUnitText.setFont(Fonts.latoLight(fontSize));
        if (windSpeedUnitText.getLayoutBounds().getWidth() > maxWidth) {
            Helper.adjustTextSize(windSpeedUnitText, maxWidth, fontSize);
        }

        maxWidth = chartSize * 0.4;
        fontSize = graphicContainer.isVisible() ? chartSize * 0.075 : chartSize * 0.1;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) {
            Helper.adjustTextSize(valueText, maxWidth, fontSize);
        }
    }

    @Override
    protected void resize() {

        super.resize();
        width = tile.getWidth() - tile.getInsets().getLeft() - tile.getInsets().getRight();
        height = tile.getHeight() - tile.getInsets().getTop() - tile.getInsets().getBottom();
        size = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            double chartWidth = contentBounds.getWidth();
            double chartHeight = contentBounds.getHeight();
            chartSize = chartWidth < chartHeight ? chartWidth : chartHeight;

            double maxContainerSize = chartSize * 0.5;
            double containerWidth = maxContainerSize - size * 0.1;
            double containerHeight = tile.isTextVisible() ? height - maxContainerSize * 0.28 : height - maxContainerSize * 0.205;

            double radius = chartSize * 0.495 - contentBounds.getX();

            barBackground.setCenterX(contentCenterX);
            barBackground.setCenterY(contentCenterY);
            barBackground.setRadiusX(radius);
            barBackground.setRadiusY(radius);
            barBackground.setStrokeWidth(chartSize * 0.1);

            bar.setCenterX(contentCenterX);
            bar.setCenterY(contentCenterY);
            bar.setRadiusX(radius);
            bar.setRadiusY(radius);
            bar.setStrokeWidth(chartSize * 0.1);

            separator.setStartX(contentCenterX);
            separator.setStartY(contentCenterX - radius - chartSize * 0.05);
            separator.setEndX(contentCenterX);
            separator.setEndY(contentCenterX - radius + chartSize * 0.05);

            if (graphicContainer.isVisible() && containerWidth > 0 && containerHeight > 0) {
                graphicContainer.setMinSize(containerWidth, containerHeight);
                graphicContainer.setMaxSize(containerWidth, containerHeight);
                graphicContainer.setPrefSize(containerWidth, containerHeight);
                graphicContainer.relocate((width - containerWidth) * 0.5, (height - containerHeight) * 0.35);

                if (null != tile) {
                    Node graphic = tile.getGraphic();
                    if (tile.getGraphic() instanceof Shape) {
                        double graphicWidth = graphic.getBoundsInLocal().getWidth();
                        double graphicHeight = graphic.getBoundsInLocal().getHeight();

                        if (graphicWidth > containerWidth || graphicHeight > containerHeight) {
                            double scale;
                            if (graphicWidth - containerWidth > graphicHeight - containerHeight) {
                                scale = containerWidth / graphicWidth;
                            } else {
                                scale = containerHeight / graphicHeight;
                            }

                            graphic.setScaleX(scale);
                            graphic.setScaleY(scale);
                        }
                    } else if (tile.getGraphic() instanceof ImageView) {
                        ((ImageView) graphic).setFitWidth(containerWidth);
                        ((ImageView) graphic).setFitHeight(containerHeight);
                    }
                }
            }
            resizeStaticText();
            windSpeedFlow.setPrefWidth(width * 0.9);
            windSpeedFlow.relocate(width * 0.05,
                    graphicContainer.isVisible() ? bar.getCenterY() + chartSize * 0.12 : bar.getCenterY() - chartSize * 0.12);

            valueUnitFlow.setPrefWidth(width * 0.9);
            valueUnitFlow.relocate(width * 0.05,
                    graphicContainer.isVisible() ? bar.getCenterY() - chartSize * 0.32 : bar.getCenterY() + chartSize * 0.15);
        }
    }

    @Override
    protected void redraw() {

        super.redraw();
        locale = tile.getLocale();
        formatString = new StringBuilder("%.").append(Integer.toString(tile.getDecimals())).append("f").toString();
        sectionsVisible = tile.getSectionsVisible();

        barBackground.setStroke(tile.getBarBackgroundColor());
        setBarColor(tile.getCurrentValue());
        windSpeedText.setFill(tile.getValueColor());
        windSpeedUnitText.setFill(tile.getUnitColor());
        valueText.setFill(tile.getValueColor());
        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
        separator.setStroke(tile.getBackgroundColor());

        titleText.setText(tile.getTitle());
        text.setText(tile.getText());

        resizeStaticText();
        resizeDynamicText();

        // draw data
        overlayCanvas.setCache(false);

        drawOverlay();

        overlayCanvas.setCache(true);
        overlayCanvas.setCacheHint(CacheHint.QUALITY);

    }

    private void drawOverlay() {

        final double CENTER_X = 0.5 * size;
        final double CENTER_Y = CENTER_X;

        // clear the chartCanvas
        overlayCtx.clearRect(0, 0, size, size);

        tile.getChartData().forEach(c -> {

            overlayCtx.save();
            overlayCtx.setTextAlign(TextAlignment.CENTER);
            overlayCtx.setTextBaseline(VPos.CENTER);
            overlayCtx.translate(CENTER_X, CENTER_Y);

            overlayCtx.rotate(angleStep * c.getValue());

            c.getAnnotations().forEach(a -> {

                final Font font = a.getFont().orElse(Fonts.latoRegular(0.04 * size));

                final double offset = tile.getPrefWidth() * 0.380;

                overlayCtx.translate(0, -1 * offset);

                overlayCtx.setFont(font);
                overlayCtx.setFill(a.getFill().orElse(tile.getForegroundColor()));
                overlayCtx.fillText(a.getText().get(), 0, size * 0.02);

                overlayCtx.translate(0, offset);
            });

            overlayCtx.restore();
        });
    }
}
