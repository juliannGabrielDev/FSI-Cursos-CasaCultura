
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author drlias
 */
public class ConexionHR {

    String url; //URL al archivo mysql.php del servidor

    public ConexionHR(String url) {
        this.url = url;
    }

    //=========================================================================
    // <editor-fold defaultstate="collapsed" desc="Funciones Solicitudes a BD">   
    //--------------------------------------------------------------------------
    //-------- Muestra el resultado de una consulta en una JTable
    //--------------------------------------------------------------------------
    public int entablar(String consulta, JTable malla) {

        int correcta = 0;

        String resultado = peticionHttpPost(url, consulta);

        if (resultado != null) {

            String[] lineas = resultado.split("->");

            DefaultTableModel modelo = (DefaultTableModel) malla.getModel();
            //limpiar tabla
            modelo.setColumnCount(0);
            modelo.setRowCount(0);

            //agregar las columnas del resultado con su nombre
            String cols[] = lineas[0].split(",");
            for (String col : cols) {
                modelo.addColumn(col.toUpperCase());
            }

            //agregar renglones con los datos
            for (int k = 1; k < lineas.length; k++) {
                String ren[] = lineas[k].split(",");
                modelo.addRow(ren);
            }

            correcta = 1;
        }

        return correcta;

    }

    //--------------------------------------------------------------------------
    //-------- muestra los datos de un campo en un combobox
    //--------------------------------------------------------------------------
    public int seleccionar(String consulta, JComboBox box) {
        int correcta = 1;
        ArrayList<String> datos = new ArrayList();
        String[] lineas = null;

        try {
            String resultado = peticionHttpPost(url, consulta);
            if (resultado != null) {
                lineas = resultado.split("->");
                for (int k = 1; k < lineas.length; k++) {
                    datos.add(lineas[k]);
                }

                DefaultComboBoxModel cbm = new DefaultComboBoxModel(datos.toArray());
                box.setModel(cbm);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            correcta = 0;
        }
        return correcta;
    }

    //--------------------------------------------------------------------------
    //-------- regresa el valor de una consulta de un solo dato
    //-------- si el resultado es la cadena vacia, significa que la consulta ha
    //-------- regresado una tabla vacia o el código SQL es incorrecto
    //--------------------------------------------------------------------------
    public String obtenerDato(String consulta) {

        String dato = "";

        try {
            String resultado = peticionHttpPost(url, consulta);
            if (resultado != null) {
                String[] lineas = resultado.split("->");
                if (lineas.length > 1) {
                    dato = lineas[1];
                }

            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());;
        }

        return dato;
    }

    //--------------------------------------------------------------------------
    //-------- regresa el valor de un campo que coincida con un dato dado
    //-------- supone que el campo regresado por la consulta es único
    //--------------------------------------------------------------------------
    public String buscarDato(String tabla, String columnaDeseada, String campoBuscado, String valorBuscado) {
        String consulta = "SELECT " + columnaDeseada + " FROM " + tabla
                + " WHERE " + campoBuscado + " = '" + valorBuscado + "'";

        String dato = "";

        try {
            String resultado = peticionHttpPost(url, consulta);
            if (resultado != null) {
                String[] lineas = resultado.split("->");
                if (lineas.length > 1) {
                    dato = lineas[1];
                }

            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());;
        }

        return dato;
    }

    //--------------------------------------------------------------------------
    //-------- regresa una lista con los registros del resultado de la consulta
    //---------cada registro de la lista es otra lista que contiene los datos del registro
    //--------------------------------------------------------------------------
    public ArrayList<ArrayList<String>> consultar(String consulta) {
        ArrayList<ArrayList<String>> datos = new ArrayList();

        String resultado = peticionHttpPost(url, consulta);

        if (resultado != null) {

            String[] lineas = resultado.split("->");

            for (int idx = 1; idx < lineas.length; idx++) {
                String linea = lineas[idx];
                ArrayList<String> renglon = new ArrayList();
                String[] valores = linea.split(",");
                for (Object elem : valores) {
                    renglon.add(elem.toString());
                }
                datos.add(renglon);
            }

        }

        return datos;
    }

    // </editor-fold>
    //=========================================================================
    // <editor-fold defaultstate="collapsed" desc="Funciones PDF">   
    //-------------------------------------------------
    //Crea un archivo PDF en base a una consulta dada
    //-------------------------------------------------
    public int crearPDF(String titulo, String encabezado, String consulta, float[] anchos, String nombreReporte) {
        int correcta = 0;

        String resultado = peticionHttpPost(url, consulta);
        
        System.out.println("DEBUG PHP RESPUESTA: " + resultado);

        if (resultado != null) {
            Document doc = new Document();

            String[] lineas = resultado.split("->");

            try {
                PdfWriter.getInstance(doc, new FileOutputStream(nombreReporte + ".pdf"));
                doc.open();

                //Titulo
                Image image = Image.getInstance("logo.png"); //For adding image in the pdf.
                image.scaleAbsolute(60, 80);
                image.setAbsolutePosition(490f, 750f);
                doc.add(image);
                doc.add(new Paragraph(titulo,
                        FontFactory.getFont(FontFactory.TIMES_BOLD, 18, Font.BOLD, BaseColor.BLACK))
                );
                doc.add(new Paragraph(obtenerFechaHoraE()));
                doc.add(Chunk.NEWLINE);
                doc.add(new Paragraph(" - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - "));
                doc.add(Chunk.NEWLINE);
                doc.add(Chunk.NEWLINE);

                String cols[] = lineas[0].split(",");
                int numCols = cols.length;

                PdfPTable tbl = new PdfPTable(numCols);

                tbl.setTotalWidth(550f);
                tbl.setLockedWidth(true);

                tbl.setHorizontalAlignment(1);
                tbl.setWidths(anchos);
                tbl.setHeaderRows(2);

                //Encabezado de la tabla
                PdfPCell cell = new PdfPCell(new Phrase(encabezado));
                cell.setColspan(numCols);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(new BaseColor(51, 153, 255));
                tbl.addCell(cell);

                //Encabezados de las columnas
                for (String col : cols) {
                    PdfPCell cel = new PdfPCell(new Phrase(col.toUpperCase()));
                    cel.setBackgroundColor(new BaseColor(153, 204, 255));
                    tbl.addCell(cel);
                }

                //Agregar los registros
                for (int k = 1; k < lineas.length; k++) {
                    String ren[] = lineas[k].split(",");
                    for (String valor : ren) {
                        tbl.addCell(valor);
                    }
                }

                doc.add(tbl);
                correcta = 1;

            } catch (Exception ex) {
                System.out.println("Error al generar: " + ex.getMessage());
                correcta = 0;
            }

            doc.close();
        }

        return correcta;
    }

    //-----------------------------------------------
    //Visualizar un archivo pdf
    //-----------------------------------------------
    public void visualizarPDF(String archivo) {
        try {
            File path = new File(archivo + ".pdf");
            Desktop.getDesktop().open(path);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // </editor-fold> 
    //=========================================================================
    // <editor-fold defaultstate="collapsed" desc="Funciones de fechas">   
    //========================================================================
    //--------------------------------------------------------------------------
    //-------- obtener la fechay hora del sistema
    //--------------------------------------------------------------------------
    public String obtenerFechaHora() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);
        int mSecond = c.get(Calendar.SECOND);
        int mMili = c.get(Calendar.MILLISECOND);

        return year + "-" + month + "-" + day + " " + mHour + ":" + mMinute + ":" + mSecond + "." + mMili;
    }

    //--------------------------------------------------------------------------
    //-------- convertir fecha util a fecha sql
    //--------------------------------------------------------------------------
    public String toSQL(java.util.Date fecha) {
        String dia = fecha.getDate() + "";
        String mes = fecha.getMonth() + "";
        String year = fecha.getYear() + "";
        return year + "-" + mes + "-" + dia;
    }
    
    //--------------------------------------------------------------------------
    //-------- convertir fecha sql a fecha util
    //--------------------------------------------------------------------------
    public java.util.Date toDate(String fecha) {
        String partes[] = fecha.split("-");
        int yy = Integer.parseInt(partes[0]);
        int mm = Integer.parseInt(partes[1]);
        int dd = Integer.parseInt(partes[2]);
        
        return new java.util.Date(yy, mm, dd);
    }

    //========================================================================
    //--------------------------------------------------------------------------
    //-------- obtener la fechay hora del sistema
    //--------------------------------------------------------------------------
    public String obtenerFechaHoraE() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);
        int mSecond = c.get(Calendar.SECOND);
        int mMili = c.get(Calendar.MILLISECOND);

        return day + "/" + month + "/" + year + " " + mHour + ":" + mMinute + ":" + mSecond;
    }

    //--------------------------------------------------------------------------
    //-------- obtener la fecha del sistema
    //--------------------------------------------------------------------------
    public String obtenerFecha() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);

        return year + "-" + month + "-" + day;
    }

    //--------------------------------------------------------------------------
    //-------- obtener la hora del sistema
    //--------------------------------------------------------------------------
    public String obtenerHora() {
        Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);
        int mSecond = c.get(Calendar.SECOND);
        int mMili = c.get(Calendar.MILLISECOND);

        return mHour + ":" + mMinute + ":" + mSecond + "." + mMili;
    }

    // </editor-fold> 
    //=========================================================================
    // <editor-fold defaultstate="collapsed" desc="Funciones para peticiones">   
    public String peticionHttpPost(String url_visitar, String query) {
        try {

            URL urlv = new URL(url_visitar);

            Map<String, Object> params = new LinkedHashMap();
            params.put("key", "secret");
            params.put("query", query);

            StringBuilder postdata = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postdata.length() != 0) {
                    postdata.append("&");
                }
                postdata.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postdata.append("=");
                postdata.append(URLEncoder.encode(param.getValue().toString(), "UTF-8"));
            }

            byte[] postbytes = postdata.toString().getBytes("UTF-8");

            HttpURLConnection con = (HttpURLConnection) urlv.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Content-Length", "" + postbytes.length);
            con.setDoOutput(true);
            con.getOutputStream().write(postbytes);

            //------------
            BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String linea;
            StringBuilder resultado = new StringBuilder();
            while ((linea = rd.readLine()) != null) {
                if (resultado.length() != 0) {
                    resultado.append("->");
                }
                resultado.append(linea);
            }
            rd.close();
            return resultado + "";

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }

    }

    //-----------------------
    public static String peticionHttpGet(String url) throws Exception {
        // Esto es lo que vamos a devolver
        StringBuilder resultado = new StringBuilder();
        // Crear un objeto de tipo URL
        URL urlv = new URL(url);

        // Abrir la conexión e indicar que será de tipo GET
        HttpURLConnection conexion = (HttpURLConnection) urlv.openConnection();
        conexion.setRequestMethod("GET");
        // Búferes para leer
        BufferedReader rd = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
        String linea;
        // Mientras el BufferedReader se pueda leer, agregar contenido a resultado
        while ((linea = rd.readLine()) != null) {
            resultado.append(linea);
        }
        // Cerrar el BufferedReader
        rd.close();
        // Regresar resultado, pero como cadena, no como StringBuilder
        return resultado.toString();
    }
    // </editor-fold> 

    //=========================================================================
    //-------------------------------------------------------
    //---Obtiene los nombres de las columnas de una tabla
    //-------------------------------------------------------
    public String[] columnas(String tabla) {
        String consulta = "SELECT * FROM " + tabla;
        String[] columnas = null;
        try {
            String resultado = peticionHttpPost(url, consulta);
            if (resultado != null) {
                String[] lineas = resultado.split("->");
                columnas = lineas[0].split(",");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return columnas;
    }

    //-------------------------------------------------------
    //---Insertar un registro en una tabla
    //-------------------------------------------------------
    public int insertar(String tabla, String[] valores) {
        int correcta = 0;

        String sql = "INSERT INTO " + tabla + " VALUES(";
        for (int k = 0; k < valores.length; k++) {
            if (valores[k].equals("")) {
                sql += "NULL";
            } else {
                sql += "'" + valores[k] + "'";
            }
            if (k < valores.length - 1) {
                sql += ",";
            }
        }
        sql += ")";

        System.out.println("INSERTAR: " + sql);

        try {
            String resultado = peticionHttpPost(url, sql);
            if (resultado.contains("AFFECTED ROWS")) {
                correcta = 1;
            }
            System.out.println("Resultado: " + resultado);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return correcta;
    }

    //-------------------------------------------------------
    //---Actualizar un registro en una tabla
    //-------------------------------------------------------
    public int actualizar(String tabla, String[] valores) {
        int correcta = 0;

        String[] cols = columnas(tabla);

        String sql = "UPDATE " + tabla + " SET ";
        for (int k = 1; k < valores.length; k++) {
            sql += cols[k] + " = '" + valores[k] + "'";
            if (k < valores.length - 1) {
                sql += ",";
            }
        }
        //sql += " WHERE " + cols[0] + " = " + valores[0];
        sql += " WHERE " + cols[0] + " = '" + valores[0] + "'";

        System.out.println("ACTUALIZAR: " + sql);

        try {
            String resultado = peticionHttpPost(url, sql);
            if (resultado.contains("AFFECTED ROWS")) {
                correcta = 1;
            }
            System.out.println("Resultado: " + resultado);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return correcta;
    }

    //-------------------------------------------------------
    //---Borra el registro en una tabla
    //-------------------------------------------------------
    public int borrar(String tabla, String[] valores) {
        int correcta = 0;

        String[] cols = columnas(tabla);

        //String sql = "DELETE FROM " + tabla
        //        + " WHERE " + cols[0] + " = " + valores[0];
        String sql = "DELETE FROM " + tabla
           + " WHERE " + cols[0] + " = '" + valores[0] + "'";

        try {
            String resultado = peticionHttpPost(url, sql);
            if (resultado.contains("AFFECTED ROWS")) {
                correcta = 1;
            }
            System.out.println("Resultado: " + resultado);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return correcta;
    }

}
