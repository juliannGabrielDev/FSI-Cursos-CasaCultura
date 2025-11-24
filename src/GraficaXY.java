
import java.util.ArrayList;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author drlias
 */
public class GraficaXY {

    JFreeChart chart;
    ChartPanel chartPanel;

    //-------------------------------------------------------------------------------------------------
    public GraficaXY(String titulo, String tituloEjeX, String tituloEjeY, ArrayList<String> titulosSeries, ArrayList<ArrayList<String>> valores) {
        chart = ChartFactory.createBarChart(
                titulo, tituloEjeX, tituloEjeY,
                crearDataset(titulosSeries, valores),
                PlotOrientation.VERTICAL,
                true, true, true);

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        chartPanel = new ChartPanel(chart);
    }

    //------------------------------
    private DefaultCategoryDataset crearDataset(ArrayList<String> nombres, ArrayList<ArrayList<String>> valores) {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int col = 0; col < nombres.size(); col++) {
            String serie = nombres.get(col);
            for (int r = 0; r < valores.size(); r++) {
                ArrayList<String> rr = valores.get(r);
                String etiqueta = rr.get(0);
                Double valor = Double.valueOf(rr.get(col + 1));
                dataset.addValue(valor, serie, etiqueta);
            }
        }

        return dataset;
    }

}
