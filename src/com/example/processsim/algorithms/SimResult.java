package com.example.processsim.algorithms;

import java.awt.Color;
import java.util.List;
import java.util.Map;

/**
 * Holds the result of a scheduling simulation.
 */
public class SimResult {
    public List<GanttEntry> timeline;
    public String text;
    public Map<String, Color> colorMap;
}
