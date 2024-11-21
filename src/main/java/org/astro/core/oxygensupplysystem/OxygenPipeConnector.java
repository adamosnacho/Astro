package org.astro.core.oxygensupplysystem;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OxygenPipeConnector {
    public static final Map<OxygenPipeConnector, OxygenPipeConnector> renderedPipeConnections = new HashMap<>();

    private boolean powered = false;
    public boolean source = false;
    public float x;
    public float y;

    public final List<OxygenPipeConnector> connectedConnectors = new ArrayList<>();

    public OxygenPipeConnector(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Powers the current connector and propagates power to connected connectors.
     */
    public void power() {
        if (powered) return; // Prevent repeated powering
        powered = true;

        // Propagate power to all connected connectors
        for (OxygenPipeConnector connector : connectedConnectors) {
            connector.setPowered(true);
        }
    }

    /**
     * Unpowers the current connector and propagates the unpower action
     * unless the connector is a source.
     */
    public void unPower() {
        powered = false;

        // Propagate unpowering to all connected connectors unless they are sources
        for (OxygenPipeConnector connector : connectedConnectors) {
            if (!connector.source) {
                connector.setPowered(false);
            }
        }
    }

    /**
     * Sets the power status of the connector. If the connector is powered, it triggers the `power` method.
     * If unpowered, it triggers the `unPower` method.
     *
     * @param powered the new power state
     */
    public void setPowered(boolean powered) {
        if (this.powered != powered) {
            this.powered = powered;
            if (powered) {
                power();
            } else {
                unPower();
            }
        }
    }

    /**
     * Gets the power status of the connector.
     *
     * @return true if the connector is powered, false otherwise
     */
    public boolean getPowered() {
        return powered;
    }

    /**
     * Connects this connector to another connector.
     *
     * @param connector the connector to connect to
     */
    public void connect(OxygenPipeConnector connector) {
        if (!connectedConnectors.contains(connector) && connector != this) {
            connectedConnectors.add(connector);
            connector.connectedConnectors.add(this);

            // If either connector is powered, propagate power
            if (this.powered) {
                connector.setPowered(true);
            } else if (connector.powered) {
                this.setPowered(true);
            }
        }
    }

    /**
     * Disconnects this connector from another connector.
     *
     * @param connector the connector to disconnect from
     */
    public void disconnect(OxygenPipeConnector connector) {
        if (connectedConnectors.contains(connector)) {
            connectedConnectors.remove(connector);
            connector.connectedConnectors.remove(this);

            // Re-evaluate power propagation
            if (!this.source && !isConnectedToSource()) {
                this.setPowered(false);
            }
            if (!connector.source && !connector.isConnectedToSource()) {
                connector.setPowered(false);
            }
        }
    }

    /**
     * Renders a pipe from this o2 connector to another, specified one
     *
     * @param target the OxygenPipeConnector that the pipe should be drawn to
     * @param g the graphics canvas to draw to
     */
    public void renderPipe(Graphics g, OxygenPipeConnector target) {
        if (renderedPipeConnections.containsKey(this) && renderedPipeConnections.get(this) == target) return;
        if (renderedPipeConnections.containsKey(target) && renderedPipeConnections.get(target) == this) return;
        renderedPipeConnections.put(this, target);

        g.setLineWidth(10);
        g.setColor(powered ? new Color(255, 255, 255, 180) : new Color(255, 255, 255, 90));
        g.drawLine(target.x, target.y, x, y);
    }

    /**
     * Checks if this connector is connected to a source either directly or indirectly.
     *
     * @return true if connected to a source, false otherwise
     */
    private boolean isConnectedToSource() {
        return isConnectedToSource(new ArrayList<>());
    }

    /**
     * Recursive helper method to determine if connected to a source.
     *
     * @param visited the list of already-visited connectors to avoid cycles
     * @return true if connected to a source, false otherwise
     */
    private boolean isConnectedToSource(List<OxygenPipeConnector> visited) {
        if (source) return true;

        visited.add(this);
        for (OxygenPipeConnector connector : connectedConnectors) {
            if (!visited.contains(connector) && connector.isConnectedToSource(visited)) {
                return true;
            }
        }
        return false;
    }
}
