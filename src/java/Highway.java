import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
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
        Enumeration<String> paramNames = request.getParameterNames();
        while(paramNames.hasMoreElements())
        {
            String paramValue = request.getParameter(paramNames.nextElement());
            if(paramValue.startsWith("jdbc"))
            {
                dbString = paramValue;
                break;
            }
        }
        if(!dbString.equalsIgnoreCase(""))
        {
            
            String output = "";
            try
            {
                Class.forName("org.sqlite.JDBC");
                try(Connection conn = DriverManager.getConnection(dbString))
                {
                    
                }
            
            }
            catch(SQLException | ClassNotFoundException ex)
            {
                output = ex.toString();
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
