//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.uma.jmetal.util.chartcontainer;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontUtils;

/**
 * Class for configuring and displaying a XChart.
 *
 * @author Jorge Rodriguez Ordonez
 */

public class ChartContainer {
    private Map<String, XYChart> charts;
    private XYChart frontChart;
    private XYChart varChart;
    private SwingWrapper<XYChart> sw;
    private String name;
    private int delay;
    private int objective1;
    private int objective2;
    private int variable1;
    private int variable2;
    private Map<String, List<Integer>> iterations;
    private Map<String, List<Double>> indicatorValues;

    public ChartContainer(String name) {
        this(name, 0);
    }

    public ChartContainer(String name, int delay) {
        this.name = name;
        this.delay = delay;
        this.charts = new HashMap<String, XYChart>();
        this.iterations = new HashMap<String, List<Integer>>();
        this.indicatorValues = new HashMap<String, List<Double>>();
    }

    public void SetFrontChart(int objective1, int objective2) throws FileNotFoundException {
        this.SetFrontChart(objective1, objective2, null);
    }

    public void SetFrontChart(int objective1, int objective2, String referenceFront) throws FileNotFoundException {
        this.objective1 = objective1;
        this.objective2 = objective2;
        this.frontChart = new XYChartBuilder().xAxisTitle("Objective " + this.objective1)
                .yAxisTitle("Objective " + this.objective2).build();
        this.frontChart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter).setMarkerSize(5);

        if (referenceFront != null) {
            this.DisplayReferenceFront(referenceFront);
        }

        double[] xData = new double[] { 0 };
        double[] yData = new double[] { 0 };
        XYSeries frontChartSeries = this.frontChart.addSeries(this.name, xData, yData);
        frontChartSeries.setMarkerColor(Color.blue);

        this.charts.put(this.name, this.frontChart);
    }

    public void SetVarChart(int variable1, int variable2) {
        this.variable1 = variable1;
        this.variable2 = variable2;
        this.varChart = new XYChartBuilder().xAxisTitle("Variable " + this.variable1)
                .yAxisTitle("Variable " + this.variable2).build();
        this.varChart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter).setMarkerSize(5);

        double[] xData = new double[] { 0 };
        double[] yData = new double[] { 0 };

        XYSeries varChartSeries = this.varChart.addSeries(this.name, xData, yData);
        varChartSeries.setMarkerColor(Color.blue);

        this.charts.put(this.name + "_VAR", this.varChart);
    }

    public void InitChart() {
        this.sw = new SwingWrapper<XYChart>(new ArrayList<XYChart>(this.charts.values()));
        this.sw.displayChartMatrix(this.name);
    }

    public void UpdateFrontCharts(List<DoubleSolution> solutionList) {
        double[] xData;
        double[] yData;

        if (this.frontChart != null) {
            xData = this.getSolutionsForObjective(solutionList, this.objective1);
            yData = this.getSolutionsForObjective(solutionList, this.objective2);
            this.frontChart.updateXYSeries(this.name, xData, yData, null);
        }

        if (this.varChart != null) {
            xData = this.getVariableValues(solutionList, this.variable1);
            yData = this.getVariableValues(solutionList, this.variable2);
            this.varChart.updateXYSeries(this.name, xData, yData, null);
        }
    }

    public void RefreshCharts() {
        this.RefreshCharts(this.delay);
    }

    public void RefreshCharts(int delay) {
        if (delay > 0) {
            try {
                TimeUnit.MILLISECONDS.sleep(delay);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        this.Repaint();
    }

    public void AddIndicatorChart(String indicator) {
        XYChart indicatorChart = new XYChartBuilder().xAxisTitle("n").yAxisTitle(indicator).build();
        indicatorChart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter).setMarkerSize(5);

        List<Integer> indicatorIterations = new ArrayList<Integer>();
        indicatorIterations.add(0);
        List<Double> indicatorValues = new ArrayList<Double>();
        indicatorValues.add(0.0);

        XYSeries indicatorSeries = indicatorChart.addSeries(this.name, indicatorIterations, indicatorValues);
        indicatorSeries.setMarkerColor(Color.blue);

        this.iterations.put(indicator, indicatorIterations);
        this.indicatorValues.put(indicator, indicatorValues);
        this.charts.put(indicator, indicatorChart);
    }

    public void RemoveIndicator(String indicator) {
        this.iterations.remove(indicator);
        this.indicatorValues.remove(indicator);
        this.charts.remove(indicator);
    }

    public void UpdateIndicatorChart(String indicator, Double value) {
        this.indicatorValues.get(indicator).add(value);
        this.iterations.get(indicator).add(this.indicatorValues.get(indicator).size());

        this.charts.get(indicator).updateXYSeries(this.name, this.iterations.get(indicator),
                this.indicatorValues.get(indicator), null);
    }

    public void Repaint() {
        try {
            for (int i = 0; i < this.charts.values().size(); i++) {
                this.sw.repaintChart(i);
            }
        } catch (IndexOutOfBoundsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void DisplayFront(String name, String fileName, int objective1, int objective2)
            throws FileNotFoundException {
        ArrayFront front = new ArrayFront(fileName);
        double[][] data = FrontUtils.convertFrontToArray(front);
        double[] xData = getObjectiveValues(data, objective1);
        double[] yData = getObjectiveValues(data, objective2);
        XYSeries referenceFront = this.frontChart.addSeries(name, xData, yData);
        referenceFront.setMarkerColor(Color.red);
    }

    private void DisplayReferenceFront(String fileName) throws FileNotFoundException {
        this.DisplayReferenceFront(fileName, this.objective1, this.objective2);
    }

    private void DisplayReferenceFront(String fileName, int objective1, int objective2) throws FileNotFoundException {
        this.DisplayFront("Reference Front", fileName, objective1, objective2);
    }

    private double[] getObjectiveValues(double[][] data, int obj) {
        double[] values = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            values[i] = data[i][obj];
        }
        return values;
    }

    private double[] getSolutionsForObjective(List<DoubleSolution> solutionList, int objective) {
        double[] result = new double[solutionList.size()];
        for (int i = 0; i < solutionList.size(); i++) {
            result[i] = solutionList.get(i).getObjective(objective);
        }
        return result;
    }

    private double[] getVariableValues(List<DoubleSolution> solutionList, int variable) {
        double[] result = new double[solutionList.size()];
        for (int i = 0; i < solutionList.size(); i++) {
            result[i] = solutionList.get(i).getVariableValue(variable);
        }
        return result;
    }

    public void SaveChart(String fileName, BitmapFormat format) throws IOException {
        for (String chart : this.charts.keySet()) {
            BitmapEncoder.saveBitmap(this.charts.get(chart), fileName + "_" + chart, format);
        }
    }

    public String getName() {
        return this.name;
    }

    public ChartContainer setName(String name) {
        this.name = name;
        return this;
    }

    public int getDelay() {
        return this.delay;
    }

    public ChartContainer setDelay(int delay) {
        this.delay = delay;
        return this;
    }

    public XYChart getFrontChart() {
        return this.frontChart;
    }

    public XYChart getVarChart() {
        return this.varChart;
    }

    public XYChart getChart(String chartName) {
        return this.charts.get(chartName);
    }
}
