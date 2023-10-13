// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2023T2, Assignment 6
 * Name: Amy Booth
 * Username: boothamy
 * ID: 300653766
 */

import ecs100.*;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.util.List;

public class BusNetworks {

    /**
     * Map of towns, indexed by their names
     */
    private Map<String, Town> busNetwork = new HashMap<String, Town>();

    /**
     * CORE
     * Loads a network of towns from a file.
     * Constructs a Set of Town objects in the busNetwork field
     * Each town has a name and a set of neighbouring towns
     * First line of file contains the names of all the towns.
     * Remaining lines have pairs of names of towns that are connected.
     */
    public void loadNetwork(String filename) {
        try {
            busNetwork.clear();
            UI.clearText();
            List<String> lines = Files.readAllLines(Path.of(filename));
            String firstLine = lines.remove(0);
            Scanner names = new Scanner(firstLine);
            // if it's a lat long file
            if (names.hasNextInt()) {
                int numLines = names.nextInt();
                for (int i = 0; i < numLines; i++) { //go through each singular town info
                    Scanner sc = new Scanner(lines.get(i));
                    String name = sc.next();
                    double lat = sc.nextDouble();
                    double lon = sc.nextDouble();
                    busNetwork.put(name, new Town(name, lat, lon));
                }
                lines = lines.subList(numLines, lines.size()); //remove so the next iteration works
                convertXY();
            }
            // if it's not a lat long file
            else {
                while (names.hasNext()) {
                    String name = names.next();
                    busNetwork.put(name, new Town(name));
                }
            }

            //do this for either type of file
            for (String line : lines) {
                Scanner sc = new Scanner(line);
                Town town1 = busNetwork.get(sc.next());
                Town town2 = busNetwork.get(sc.next());
                town1.addNeighbour(town2);
                town2.addNeighbour(town1);
            }

            UI.setColor(Color.black);
            displayNetwork();

            UI.println("Loaded " + busNetwork.size() + " towns:");

        } catch (IOException e) {
            throw new RuntimeException("Loading data.txt failed" + e);
        }
    }

    /**
     * CORE
     * Print all the towns and their neighbours:
     * Each line starts with the name of the town, followed by
     * the names of all its immediate neighbours,
     */
    public void printNetwork() {
        UI.println("The current network: \n====================");
        for (Town town : busNetwork.values()) {
            String print = town.getName() + "->";
            for (Town neighbour : town.getNeighbours()) {
                print += " " + neighbour.getName();
            }
            UI.println(print);
        }

    }

    /**
     * COMPLETION
     * Return a set of all the nodes that are connected to the given node.
     * Traverse the network from this node in the standard way, using a
     * visited set, and then return the visited set
     */
    public Set<Town> findAllConnected(Town town) {
        Set<Town> connected = new HashSet<>();
        findConnected(town, connected);
        return connected;
    }

    public void findConnected(Town town, Set<Town> connected) {
        if (connected.contains(town)) {
            return;
        }
        connected.add(town);
        for (Town neighbour : town.getNeighbours()) {
            findConnected(neighbour, connected); //yum recursion
        }
    }

    /**
     * COMPLETION
     * Print all the towns that are reachable through the network from
     * the town with the given name.
     * Note, do not include the town itself in the list.
     */
    public void printReachable(String name) {
        Town town = busNetwork.get(name);
        if (town == null) {
            UI.println(name + " is not a recognised town");
        } else {
            UI.println("\nFrom " + town.getName() + " you can get to:");
            Set<Town> reachable = findAllConnected(town);
            reachable.remove(town); // remove the town itself from print list
            for (Town t : reachable) {
                UI.println(t);
            }
        }

    }

    /**
     * COMPLETION
     * Print all the connected sets of towns in the busNetwork
     * Each line of the output should be the names of the towns in a connected set
     * Works through busNetwork, using findAllConnected on each town that hasn't
     * yet been printed out.
     */
    public void printConnectedGroups() {
        UI.println("Groups of Connected Towns: \n================");
        int groupNum = 1;
        List<Town> toCheck = new ArrayList<>(busNetwork.values());
        while (!toCheck.isEmpty()) {
            Set<Town> found = findAllConnected(toCheck.get(0));
            toCheck.removeAll(found); // otherwise groups will be printed more than once
            UI.printf("Group %d:", groupNum);
            for (Town town : found) {
                UI.print(" " + town.getName());
            }
            UI.println();
            groupNum++;
        }
    }

    /**
     * Calculate and assign each town's x and y graphic values
     * Based on the overall space and maximums/minimums
     * Transform them so that they fit in the space correctly
     */
    public void convertXY() {
        // visual space bounds
        double maxX = 500;
        double maxY = 500;
        double minX = 50;
        double minY = 50;

        // max/min latitudes and longitudes (need for calculation)
        double maxLat = Double.NEGATIVE_INFINITY;
        double maxLon = Double.NEGATIVE_INFINITY;
        double minLat = Double.POSITIVE_INFINITY;
        double minLon = Double.POSITIVE_INFINITY;
        for (Town town : busNetwork.values()) {
            if (Math.abs(town.y) > maxLat) {
                maxLat = Math.abs(town.y);
            }
            if (Math.abs(town.y) < minLat) {
                minLat = Math.abs(town.y);
            }
            if (town.x > maxLon) {
                maxLon = town.x;
            }
            if (town.x < minLon) {
                minLon = town.x;
            }
        }

        // maths :)
        double shiftLat = (minY * maxLat - minLat * maxY) / (minY - maxY);
        double shiftLon = (minX * maxLon - minLon * maxX) / (minX - maxX);
        double scaleLat = (maxY - minY) / (maxLat - minLat);
        double scaleLon = (maxX - minX) / (maxLon - minLon);

        // change the x and y to correct graphical/visual values
        for (Town town : busNetwork.values()) {
            double lat = Math.abs(town.y);
            double lon = town.x;
            town.y = scaleLat * (lat - shiftLat);
            town.x = scaleLon * (lon - shiftLon);
        }
    }

    /**
     * Display one town, its neighbours, and the connections between
     *
     * @param town   The town at the centre of the immediate neighbourhood
     * @param radius The size of the dot to draw where the town is
     */
    public void displayTown(Town town, double radius) {
        UI.fillOval(town.x - radius, town.y - radius, 2 * radius, 2 * radius);
        for (Town neighbour : town.getNeighbours()) {
            UI.drawLine(town.x, town.y, neighbour.x, neighbour.y);
            UI.fillOval(neighbour.x - radius, neighbour.y - radius, 2 * radius, 2 * radius);
        }
    }

    /**
     * Display all the towns and connections
     */
    public void displayNetwork() {
        UI.clearGraphics();
        UI.setColor(Color.black);
        UI.setLineWidth(1);
        for (Town town : busNetwork.values()) {
            displayTown(town, 5);
        }
    }

    /**
     * Mouse listener - records mouse action
     * Highlights specific town neighbourhood if mouse released on a town
     *
     * @param action The mouse action - "pressed", "released", or "clicked"
     * @param x      The x coordinate of the mouse when action occurs
     * @param y      The y coordinate of the mouse when action occurs
     */
    public void doMouse(String action, double x, double y) {
        if (action.equals("released")) {
            for (Town town : busNetwork.values()) {
                if (Math.abs(town.x - x) <= 5 && Math.abs(town.y - y) <= 5) { //check if on a town
                    displayNetwork();
                    // draw red and bigger to highlight/emphasise
                    UI.setColor(Color.red);
                    UI.setLineWidth(3);
                    displayTown(town, 7);
                }
            }
        }
    }

    /**
     * Set up the GUI (buttons and mouse)
     */
    public void setupGUI() {
        UI.setMouseListener(this::doMouse);
        UI.addButton("Load", () -> {
            loadNetwork(UIFileChooser.open());
        });
        UI.addButton("Print Network", this::printNetwork);
        UI.addTextField("Reachable from", this::printReachable);
        UI.addButton("All Connected Groups", this::printConnectedGroups);
        UI.addButton("Clear text", UI::clearText);
        UI.addButton("Quit", UI::quit);
        UI.setWindowSize(1100, 700);
        UI.setDivider(0.5);
        loadNetwork("data-small.txt");
    }

    // Main
    public static void main(String[] arguments) {
        BusNetworks bnw = new BusNetworks();
        bnw.setupGUI();
    }

}
