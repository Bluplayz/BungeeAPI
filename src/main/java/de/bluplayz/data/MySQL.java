package de.bluplayz.data;

import de.bluplayz.BungeeAPI;
import de.bluplayz.api.LocaleAPI;

import java.sql.*;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MySQL {

    // Connection data
    private static String host = "localhost";
    private static int port = 3306;
    private static String database = "";
    private static String username = "";
    private static String password = "";

    // Prepared Statements
    /*
    public PreparedStatement insertX;
    public PreparedStatement deleteX;
    public PreparedStatement selectX;
    public PreparedStatement selectAllX;
    */

    private Connection con = null;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public MySQL() {
        super();

        host = BungeeAPI.getInstance().getConfig().getString( "data.mysql.host" );
        port = BungeeAPI.getInstance().getConfig().getInt( "data.mysql.port" );
        database = BungeeAPI.getInstance().getConfig().getString( "data.mysql.database" );
        username = BungeeAPI.getInstance().getConfig().getString( "data.mysql.username" );
        password = BungeeAPI.getInstance().getConfig().getString( "data.mysql.password" );

        try {
            // Load driver
            Class.forName( "com.mysql.jdbc.Driver" );

            connect();
            if ( isConnected() ) {
                LocaleAPI.log( "data_mysql_connected_successfully" );
                createTables();
                prepareStatements();
            }
        } catch ( ClassNotFoundException e ) {
            LocaleAPI.log( "data_mysql_driver_not_found" );
        } catch ( SQLException e ) {
            e.printStackTrace();
        }
    }

    /**
     * check if is connected
     *
     * @return true if is connected otherwise false
     */
    private boolean isConnected() {
        return con != null;
    }

    /**
     * connect using the connection data
     */
    private void connect() {
        try {
            // Connect to database
            con = DriverManager.getConnection( "jdbc:mysql://" + host + ":" + port + "/" + database + "?user=" + username + "&password=" + password + "&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false&maxReconnects=5" );
        } catch ( SQLException e ) {
            System.out.println( "Connection failed" );
            System.out.println( "SQLException: " + e.getMessage() );
            System.out.println( "Are your connection data correct?" );
            con = null;
        }
    }

    /**
     * execute query statement
     *
     * @param statement the statement to update
     * @return resultset from the query
     */
    public ResultSet query( PreparedStatement statement ) {
        ResultSet resultSet = null;
        if ( isConnected() ) {
            try {
                resultSet = statement.executeQuery();
            } catch ( SQLException e ) {
                e.printStackTrace();
            }
        }

        return resultSet;
    }

    /**
     * execute update statement
     *
     * @param statement the statement to update
     */
    public void update( PreparedStatement statement ) {
        if ( isConnected() ) {
            try {
                statement.executeUpdate();
            } catch ( SQLException e ) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Executes a query asynchronously
     *
     * @param statement The statement to execute
     */
    public void asyncUpdate( PreparedStatement statement ) {
        executorService.execute( () -> update( statement ) );
    }

    /**
     * Executes a query asynchronously
     *
     * @param statement The statement to execute
     * @param consumer  The consumer that handles the {@link ResultSet}
     */
    public void asyncQuery( PreparedStatement statement, Consumer<ResultSet> consumer ) {
        executorService.execute( () -> consumer.accept( query( statement ) ) );
    }

    /**
     * get Data in HashMap
     * HashMap<id, HashMap<RowKey, RowValue>>
     */
    public HashMap<Integer, HashMap<String, String>> getData( PreparedStatement statement ) {
        HashMap<Integer, HashMap<String, String>> data = new HashMap<>();

        HashMap<String, String> row = new HashMap<>();
        ResultSet rs = null;
        if ( isConnected() ) {
            try {
                rs = statement.executeQuery();

                ResultSetMetaData metadata = rs.getMetaData();
                int numberOfColumns = metadata.getColumnCount();
                metadata.getColumnName( 1 );
                String key;

                int id = 0;
                while ( rs.next() ) {
                    int i = 1;
                    while ( i <= numberOfColumns ) {
                        String value = rs.getString( i );
                        key = metadata.getColumnName( i );
                        i++;

                        row.put( key, value );
                    }
                    data.put( id, row );
                    row = new HashMap<>();
                    id++;
                }

            } catch ( SQLException e ) {
                //connect();
                System.err.println( e.getMessage() );
            }
        }

        return data;
    }

    /**
     * prepare statements
     *
     * @throws SQLException
     */
    private void prepareStatements() throws SQLException {
        /*
        selectAllX = con.prepareStatement( "SELECT * FROM X;" );
        selectX = con.prepareStatement( "SELECT * FROM X WHERE sender=?;" );
        insertX = con.prepareStatement( "INSERT INTO X(sender, target, reason, time, time_string) VALUES(?, ?, ?, ?, ?);" );
        deleteX = con.prepareStatement( "DELETE FROM X WHERE sender=?;" );
         */
    }

    /**
     * create default talbes
     */
    private void createTables() {
        /*
        String createReportsTable = "CREATE TABLE IF NOT EXISTS X" +
                "(" +

                "  id INT NOT NULL AUTO_INCREMENT," +
                "  sender VARCHAR(32) default 'uuid from the player here'," +
                "  target VARCHAR(32) default 'uuid from the player here'," +
                "  reason VARCHAR(128) default 'reason'," +
                "  time BIGINT default '-1'," +
                "  time_string VARCHAR(128) default 'time'," +

                "  PRIMARY KEY (id)" +
                ");";

        try {
            Statement statement = con.createStatement();
            statement.execute( createReportsTable );
            statement.close();
        } catch ( SQLException e ) {
            e.printStackTrace();
        }
        */
    }
}
