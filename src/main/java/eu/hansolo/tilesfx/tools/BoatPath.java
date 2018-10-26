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

package eu.hansolo.tilesfx.tools;

import javafx.scene.control.Tooltip;
import javafx.scene.shape.SVGPath;

/**
 * Created by hansolo on 21.12.16.
 */
public class BoatPath extends SVGPath {

    private String name;
    private Tooltip tooltip;

    // ******************** Constructors **************************************
    public BoatPath() {

        this("", null);
    }

    public BoatPath(final String NAME) {

        this(NAME, null);
    }

    public BoatPath(final String NAME, final String CONTENT) {

        super();
        name = NAME;
        if (null == CONTENT)
            return;
        setContent(CONTENT);
        tooltip = new Tooltip(CONTENT);
        Tooltip.install(BoatPath.this, tooltip);
    }

    // ******************** Methods *******************************************
    public String getName() {

        return name;
    }

    public void setName(final String NAME) {

        this.name = NAME;
    }

    public Tooltip getTooltip() {

        return tooltip;
    }

    public void setTooltip(final Tooltip TOOLTIP) {

        tooltip = TOOLTIP;
        Tooltip.install(BoatPath.this, tooltip);
    }

    @Override
    public String toString() {

        return new StringBuilder("{\n").append("  name   :\"")
                .append(name)
                .append("\"\n")
                .append("  tooltip:\"")
                .append(tooltip.getText())
                .append("\"\n")
                .append("  content:\"")
                .append(getContent())
                .append("\"\n")
                .append("}\n")
                .toString();
    }
}
