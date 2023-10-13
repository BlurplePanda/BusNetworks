// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2023T2, Assignment 6
 * Name:
 * Username:
 * ID:
 */

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ecs100.*;

public class Town {

    private String name;
    private Set<Town> neighbours = new HashSet<Town>();
    public double x;
    public double y;

    public Town(String name) {
        this.name = name;
        x = Double.NEGATIVE_INFINITY; // shouldn't display if no lat/lon entered
        y = Double.NEGATIVE_INFINITY; // but also won't throw error this way :)
    }

    public Town(String name, double lat, double lon) {
        this.name = name;
        this.y = lat;
        this.x = lon;
    }

    public String getName() {
        return this.name;
    }

    public Set<Town> getNeighbours() {
        return Collections.unmodifiableSet(neighbours);
    }

    public void addNeighbour(Town node) {
        neighbours.add(node);
    }

    public String toString() {
        return name + " (" + neighbours.size() + " connections)";
    }

}
