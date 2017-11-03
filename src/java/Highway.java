import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Michał Śliwa
 */
@WebServlet(urlPatterns = {"/Highway"})
public class Highway extends HttpServlet
{
    private static final long serialVersionUID = 1982457982715234598L;
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/html;charset=UTF-8");
        String dbString = "";
        //Check url parameters for jdbc connection string
        Enumeration<String> paramNames = request.getParameterNames();
        while(paramNames.hasMoreElements())
        {
            String paramValue = request.getParameter(paramNames.nextElement());
            if(paramValue.startsWith("jdbc"))
            {
                //if parameter line starts with jdbc, save it as connection string and break
                dbString = paramValue;
                break;
            }
        }
        //start if jdbc connection string was found
        if(!dbString.equalsIgnoreCase(""))
        {
            //split connection string to get database type
            String dbType = dbString.split("\\:")[1].toLowerCase();
            String output = "";
            try
            {
                //load appropriate driver based on connection string
                switch(dbType)
                {
                    case "sqlite":
                        Class.forName("org.sqlite.JDBC");                        
                        break;
                    case "oracle":
                        Class.forName("oracle.jdbc.OracleDriver");
                        break;
                    case "mysql":
                        Class.forName("com.mysql.jdbc.Driver");
                        break;
                    case "sqlserver":
                        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                        break;
                    case "postgresql":
                        Class.forName("org.postgresql.Driver");
                    default:
                        throw new ClassNotFoundException();
                }

                try(Connection conn = DriverManager.getConnection(dbString))
                {
                    output = "OK";
                }
                
                //Unload all used drivers
                Enumeration<Driver> drivers = DriverManager.getDrivers();
                while(drivers.hasMoreElements())
                {
                    Driver d = drivers.nextElement();
                    DriverManager.deregisterDriver(d);
                }
            
            }
            catch(SQLException | ClassNotFoundException ex)
            {
                output = ex.toString() + "<br/>Call Stack:<br/>";
                for(StackTraceElement el : ex.getStackTrace())
                    output += el.toString()+"<br/>";
            }
            finally
            {
                try (PrintWriter out = response.getWriter())
                {
                    out.println(output);
                }  
            }        
        }
        else
        {
            try (PrintWriter out = response.getWriter())
            {
                out.println("No database string provided");
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }// </editor-fold>

}
