import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleEdge;

public class RadarChart extends JFrame {
	public static List sortByValue(final Map map) {
		List<String> list = new ArrayList();
		list.addAll(map.keySet());
		Collections.sort(list,new Comparator() {
			public int compare(Object o1,Object o2) {
				Object v1 = map.get(o1);
				Object v2 = map.get(o2);
				
				return ((Comparable) v2).compareTo(v1);
			}
		});

		Collections.reverse(list); // 주석시 오름차순

		return list;
	}
	
	
	
    public DefaultCategoryDataset dataset;
    public SpiderWebPlot plot;
    public RadarChart(String title, String input, HashMap<String, Double> result, HashMap<String, Double> result2) {
        //super(title);
        
    	
    	
    	
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
		//Set set = result.keySet();
		//Iterator iterator = set.iterator();
        Iterator iterator1 = sortByValue(result).iterator();
        Iterator iterator2 = sortByValue(result2).iterator();
		while(iterator1.hasNext()) {
			String key = (String)iterator1.next();
			dataset.addValue(result.get(key), title.substring(9), key);
		}
		
		while(iterator2.hasNext()) {
			String key = (String)iterator2.next();
			dataset.addValue(result2.get(key), "Input", key);
		}
		
        SpiderWebPlot plot = new SpiderWebPlot(dataset);
        plot.setStartAngle(54);
        plot.setInteriorGap(0.40);
        plot.setToolTipGenerator(new StandardCategoryToolTipGenerator());
        JFreeChart chart = new JFreeChart("", TextTitle.DEFAULT_FONT, plot, false);
        LegendTitle legend = new LegendTitle(plot);
        legend.setPosition(RectangleEdge.BOTTOM);
        ChartUtilities.applyCurrentTheme(chart);
        chart.addSubtitle(legend);
        ChartPanel chartPanel = new ChartPanel(chart);
        this.plot = (SpiderWebPlot) chartPanel.getChart().getPlot();
        this.dataset = (DefaultCategoryDataset) plot.getDataset();
        
        chartPanel.setPreferredSize(new Dimension(500, 270));
        //setContentPane(chartPanel);
        
       //JFrame f = new JFrame(title);
        setTitle(title);
        //setContentPane(chartPanel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        add(chartPanel);
        pack();
        setVisible(true);
        
        }
  }