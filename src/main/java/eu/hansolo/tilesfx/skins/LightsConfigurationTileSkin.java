/*
 * Copyright (c) 2017 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.tilesfx.skins;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.events.LocationEvent;
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.events.TileEvent.EventType;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Boat;
import eu.hansolo.tilesfx.tools.BoatPath;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.tools.Location;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

/**
 * Created by hansolo on 11.06.17.
 */
public class LightsConfigurationTileSkin extends TileSkin {

    private static final String HIRES_BOAT_PROPERTIES = "eu/hansolo/tilesfx/highres.properties";

    private Text title;
    private Boat boat;
    private StackPane boatContainer;
    private Group boatGroup;
    private EventHandler<MouseEvent> clickHandler;
    private List<BoatPath> boatPaths;
    private double boatMinX;
    private double boatMinY;
    private double boatMaxX;
    private double boatMaxY;
    private ObservableMap<Location, Circle> poiLocations;

    // ******************** Constructors **************************************
    public LightsConfigurationTileSkin(final Tile TILE) {

        super(TILE);
    }

    // ******************** Initialization ************************************
    @Override
    protected void initGraphics() {

        super.initGraphics();

        poiLocations = FXCollections.observableHashMap();

        boat = tile.getBoat();
        if (null == boat) {
            boat = Boat.SAILING;
        }

        clickHandler = event -> tile.fireTileEvent(
                new TileEvent(EventType.SELECTED_CHART_DATA, new ChartData(boat.getName(), boat.getValue(), boat.getColor())));

        boatPaths = getHiresBoatPaths().get(boat.name());

        boatMinX = Helper.MAP_WIDTH;
        boatMinY = Helper.MAP_HEIGHT;
        boatMaxX = 0;
        boatMaxY = 0;
        boatPaths.forEach(path -> {
            path.setFill(tile.getBarColor());
            boatMinX = Math.min(boatMinX, path.getBoundsInParent().getMinX());
            boatMinY = Math.min(boatMinY, path.getBoundsInParent().getMinY());
            boatMaxX = Math.max(boatMaxX, path.getBoundsInParent().getMaxX());
            boatMaxY = Math.max(boatMaxY, path.getBoundsInParent().getMaxY());
        });

        tile.getLightsList()
                .forEach(poi -> {
                    String tooltipText = new StringBuilder(poi.getName()).append("\n")
                            .append(poi.getInfo())
                            .toString();
                    Circle circle = new Circle(3, poi.getColor());
                    circle.setCenterX(poi.getLongitude());
                    circle.setCenterY(poi.getLatitude());
                    circle.setOnMousePressed(e -> poi.fireLocationEvent(new LocationEvent(poi)));
                    Tooltip.install(circle, new Tooltip(tooltipText));
                    poiLocations.put(poi, circle);
                });

        title = new Text(tile.getTitle());
        title.setFill(tile.getTitleColor());
        Helper.enableNode(title, !tile.getTitle().isEmpty());

        boatGroup = new Group();
        boatGroup.getChildren().setAll(boatPaths);

        boatContainer = new StackPane();
        boatContainer.setMinSize(size * 0.9, size * 0.795);
        boatContainer.setMaxSize(size * 0.9, size * 0.795);
        boatContainer.setPrefSize(size * 0.9, size * 0.795);
        boatContainer.getChildren().setAll(boatGroup);

        getPane().getChildren().addAll(boatContainer, title);
        getPane().getChildren().addAll(poiLocations.values());
    }

    @Override
    protected void registerListeners() {

        super.registerListeners();
        tile.addEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
    }

    // ******************** Methods *******************************************
    @Override
    protected void handleEvents(final String EVENT_TYPE) {

        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(title, !tile.getTitle().isEmpty());
            boatContainer.setMaxSize(size * 0.9, size * 0.795);
            boatContainer.setPrefSize(size * 0.9, size * 0.795);
        } else if ("RECALC".equals(EVENT_TYPE)) {
            boat = tile.getBoat();
            if (null == boat) {
                boat = Boat.SAILING;
            }
            boatPaths = getHiresBoatPaths().get(boat.name());
            boatPaths.forEach(path -> path.setFill(tile.getBarColor()));
            boatGroup.getChildren().setAll(boatPaths);
            title.setText(tile.getTitle());

            resize();
            redraw();
        }
    }

    @Override
    public void dispose() {

        tile.removeEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
        super.dispose();
    }

    // ******************** Resizing ******************************************

    @Override
    protected void resizeStaticText() {

        double maxWidth = width - size * 0.1;
        double fontSize = size * textSize.factor;

        title.setFont(Fonts.latoRegular(fontSize));
        if (title.getLayoutBounds().getWidth() > maxWidth) {
            Helper.adjustTextSize(title, maxWidth, fontSize);
        }
        switch (tile.getTitleAlignment()) {
            default:
            case LEFT:
                title.relocate(size * 0.05, size * 0.05);
                break;
            case CENTER:
                title.relocate((width - title.getLayoutBounds().getWidth()) * 0.5, size * 0.05);
                break;
            case RIGHT:
                title.relocate(width - (size * 0.05) - title.getLayoutBounds().getWidth(), size * 0.05);
                break;
        }

        title.setFont(Fonts.latoRegular(fontSize));
        if (title.getLayoutBounds().getWidth() > maxWidth) {
            Helper.adjustTextSize(title, maxWidth, fontSize);
        }
        switch (tile.getTitleAlignment()) {
            default:
            case LEFT:
                title.relocate(size * 0.05, size * 0.05);
                break;
            case CENTER:
                title.relocate((width - title.getLayoutBounds().getWidth()) * 0.5, size * 0.05);
                break;
            case RIGHT:
                title.relocate(width - (size * 0.05) - title.getLayoutBounds().getWidth(), size * 0.05);
                break;
        }
    }

    @Override
    protected void resize() {

        super.resize();
        width = tile.getWidth() - tile.getInsets().getLeft() - tile.getInsets().getRight();
        height = tile.getHeight() - tile.getInsets().getTop() - tile.getInsets().getBottom();
        size = width < height ? width : height;

        double containerWidth = contentBounds.getWidth();
        double containerHeight = contentBounds.getHeight();
        double containerSize = containerWidth < containerHeight ? containerWidth : containerHeight;

        double boatWidth = boatGroup.getLayoutBounds().getWidth();
        double boatHeight = boatGroup.getLayoutBounds().getHeight();
        double boatSize = boatWidth < boatHeight ? boatHeight : boatWidth; // max size

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            if (containerWidth > 0 && containerHeight > 0) {
                boatContainer.setMinSize(containerWidth, containerHeight);
                boatContainer.setMaxSize(containerWidth, containerHeight);
                boatContainer.setPrefSize(containerWidth, containerHeight);
                boatContainer.relocate(contentBounds.getX(), contentBounds.getY());
                double scaleFactor = containerSize / boatSize;
                boatGroup.setScaleX(scaleFactor);
                boatGroup.setScaleY(scaleFactor);
            }

            resizeStaticText();
        }
    }

    @Override
    protected void redraw() {

        super.redraw();
        title.setText(tile.getTitle());

        resizeDynamicText();
        resizeStaticText();

        boatPaths.forEach(path -> path.setFill(Helper.getColorWithOpacity(tile.getBarColor(), 0.5)));
    }

    private Properties hiresBoatProperties;
    private Map<String, List<BoatPath>> hiresBoatPaths;

    private final Map<String, List<BoatPath>> getHiresBoatPaths() {

        synchronized (Helper.class) {

            if (null == hiresBoatProperties) {

                hiresBoatProperties = Helper.readProperties(HIRES_BOAT_PROPERTIES);
            }
            if (null == hiresBoatPaths) {

                hiresBoatPaths = new ConcurrentHashMap<>();
                hiresBoatProperties.forEach((key, value) -> {
                    String name = key.toString();
                    List<BoatPath> pathList = new ArrayList<>();
                    for (String path : value.toString().split(";")) {
                        pathList.add(new BoatPath(name, path));
                    }
                    hiresBoatPaths.put(name, pathList);
                });
            }

            return hiresBoatPaths;
        }
    }
}
