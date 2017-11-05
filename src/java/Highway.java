import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
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
        //check url parameters for jdbc connection string
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
                        break;
                    default:
                        throw new ClassNotFoundException();
                }
                //connect to database
                try(Connection conn = DriverManager.getConnection(dbString))
                {
                    //create new instance of HighwayPlan
                    HighwayPlan plan = new HighwayPlan();
                    //creata a statement and select data from database
                    Statement st = conn.createStatement();
                    ResultSet result = st.executeQuery("SELECT * FROM HDATA ORDER BY ID ASC");
                    while(result.next())
                    {
                        //insert selected data to HighwayPlan
                        plan.AddPair(result.getInt("i"), result.getInt("j"));
                    }
                    //set result to output variable
                    output = String.valueOf(plan.CanBuildRoads());
                }
                
                //unload all used drivers
                Enumeration<Driver> drivers = DriverManager.getDrivers();
                while(drivers.hasMoreElements())
                {
                    Driver d = drivers.nextElement();
                    DriverManager.deregisterDriver(d);
                }
            
            }
            catch(SQLException | ClassNotFoundException ex)
            {
                //set exception to output variable
                output = ex.toString() + "<br/>Call Stack:<br/>";
                for(StackTraceElement el : ex.getStackTrace())
                    output += el.toString()+"<br/>";
            }
            finally
            {
                //print the output variable
                try (PrintWriter out = response.getWriter())
                {
                    out.println(output);
                }  
            }        
        }
        else
        {
            //no database string detected, inform user 
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

    /**
     * Represents a plan to build highways between cities
     * @author Michał Śliwa
     */
    class HighwayPlan
    {
        //ArrayList holding pairs of cities to build between
        private final ArrayList<CityPair> highways;
        
        /**
         * Default constructor
         */
        public HighwayPlan()
        {
            highways = new ArrayList<>();
        }
        
        /**
         * Adds a new CityPair to the plan
         * 
         * @param i number of first city
         * @param j number of second city
         */
        public void AddPair(int i, int j)
        {
            highways.add(new CityPair(i, j));
        }
        
        /**
         * Check if non-intersecting web of roads can be built
         * @return 1 if it can be built, otherwise 0
         */
        public int CanBuildRoads()
        {
            //sort city pairs ascending by number of first city 
            //then by number of second city
            Collections.sort(highways);
            
            //collection for highways built in north
            ArrayList<CityPair> NorthBuiltRoads = new ArrayList<>();
            //for every pair of cities in highways
            for (int i=0; i<highways.size(); i++)
            {
                Boolean canBuildPair = true;
                //get current pair of cities
                CityPair currentPair = highways.get(i);
                for (CityPair p : NorthBuiltRoads)
                {
                    //check if highway can be built without intersecting
                    //if not, break
                    if(currentPair.j > p.j && currentPair.i > p.i && currentPair.i < p.j)
                    {
                        canBuildPair = false;
                        break;
                    }
                }
                //if highway can be built, add it to northern list
                if (canBuildPair)
                {
                    NorthBuiltRoads.add(currentPair);
                }
            }
            //remove from highways all roads that are on northern list
            for (CityPair p : NorthBuiltRoads)
            {
                highways.remove(p);
            }
            
            //collection for highways built in south
            ArrayList<CityPair> SouthBuiltRoads = new ArrayList<>();
            //for every pair of cities remaining in highways
            for (int i=0; i<highways.size(); i++)
            {
                //get current pair of cities
                CityPair currentPair = highways.get(i);
                //check if highway can be built without intersecting
                //if not, return 0
                for (CityPair p : SouthBuiltRoads)
                {
                    if(currentPair.j > p.j && currentPair.i > p.i && currentPair.i < p.j)
                    {
                        return 0;
                    }
                }
                //if highway can be built, add it to southern list
                SouthBuiltRoads.add(currentPair);
            }
            //if all roads can be built, return 1
            return 1;
        }
    }
    
    /**
     * Represents a pair of cities
     * @author Michał Śliwa
     */
    class CityPair implements Comparable<CityPair>
    {
        //number of first city
        public int i;
        //number of second city
        public int j;
        
        /**
         * Constructor
         * @param _i number of first city
         * @param _j number of second city
         */
        public CityPair(int _i, int _j)
        {
            this.i = _i;
            this.j = _j;
        }

        /**
         * Overriden comparator for CityPair, compares first city number and
         * if identical, second city number
         * @param o other instace of CityPair
         * @return
         */
        @Override
        public int compareTo(CityPair o)
        {
            int iVal = this.i - o.i;
            if(iVal != 0)
                return iVal;
            else
                return this.j - o.j;
        }
    }
}
