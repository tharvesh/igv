/*
 * Copyright (c) 2007-2012 The Broad Institute, Inc.
 * SOFTWARE COPYRIGHT NOTICE
 * This software and its documentation are the copyright of the Broad Institute, Inc. All rights are reserved.
 *
 * This software is supplied without any warranty or guaranteed support whatsoever. The Broad Institute is not responsible for its use, misuse, or functionality.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 */

package org.broad.igv.dev.db;

import org.apache.log4j.Logger;
import org.broad.igv.Globals;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.util.MessageUtils;
import org.broad.igv.util.LoginDialog;
import org.broad.igv.util.ResourceLocator;

import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.*;

/**
 * Class for prototyping database connections.  Prototype only -- hardcoded for mysql,  connects to single database,
 * keeps single connection, etc.
 *
 * @author Jim Robinson
 * @date 10/31/11
 */
public class DBManager {

    private static Logger log = Logger.getLogger(DBManager.class);

    static Map<String, Connection> connectionPool =
            Collections.synchronizedMap(new HashMap<String, Connection>());

    private static Map<String, String> driverMap;

    static {
        driverMap = new HashMap<String, String>(2);
        driverMap.put("mysql", "com.mysql.jdbc.Driver");
        driverMap.put("sqlite", "org.sqlite.JDBC");
    }

    public static Connection getConnection(ResourceLocator locator) {
        String url = locator.getPath();
        if (connectionPool.containsKey(url)) {
            Connection conn = connectionPool.get(url);
            try {
                if (conn == null || conn.isClosed()) {
                    connectionPool.remove(url);
                } else {
                    return conn;
                }
            } catch (SQLException e) {
                log.error("Bad connection", e);
                connectionPool.remove(url);
            }
        }


        // No valid connections
        Connection conn = connect(locator);
        if (conn != null) {
            connectionPool.put(url, conn);
            log.info("Connection pool size: " + connectionPool.size());
        }
        return conn;

    }


    public static void closeConnection(ResourceLocator locator) {
        String url = locator.getPath();
        if (connectionPool.containsKey(url)) {
            Connection conn = connectionPool.get(url);
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                    connectionPool.remove(url);
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private static String getSubprotocol(String url) {
        String[] tokens = url.split(":");
        return tokens[1];
    }

    private static Connection connect(ResourceLocator locator) {
        createDriver(getSubprotocol(locator.getPath()));
        try {
            return DriverManager.getConnection(locator.getPath(),
                    locator.getUsername(), locator.getPassword());
        } catch (SQLException e) {
            int errorCode = e.getErrorCode();
            if (errorCode == 1044 || errorCode == 1045) {
                String resource = locator.getPath();

                Frame parent = Globals.isHeadless() ? null : IGV.getMainFrame();
                LoginDialog dlg = new LoginDialog(parent, false, resource, false);
                dlg.setVisible(true);
                if (dlg.isCanceled()) {
                    throw new RuntimeException("Must login to access" + resource);
                }
                locator.setUsername(dlg.getUsername());
                locator.setPassword(new String(dlg.getPassword()));
                return connect(locator);

            } else {
                MessageUtils.showErrorMessage("<html>Error connecting to database: <br>" + e.getMessage(), e);
                return null;
            }

        }
    }

    public static void shutdown() {
        for (Connection conn : connectionPool.values()) {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {

                }
            }
        }
        connectionPool.clear();
    }

    public static java.lang.Class<?> createDriver(String subprotocol) {
        String driver = driverMap.get(subprotocol);
        try {
            return Class.forName(driver);
        } catch (ClassNotFoundException e) {
            log.error("Unable to create driver for " + subprotocol, e);
            throw new IllegalArgumentException(e);
        }
    }


    /**
     * Creates a connection URL, and loads the driver class necessary for the protocol
     *
     * @param subprotocol
     * @param host
     * @param db
     * @param port
     * @return
     */
    public static String createConnectionURL(String subprotocol, String host, String db, String port) {
        createDriver(subprotocol);

        //If the host is a local file, don't want the leading "//"
        if (!(new File(host)).exists()) {
            host = "//" + host;
        }
        String url = "jdbc:" + subprotocol + ":" + host;
        if (port != null && !port.equals("")) {
            try {
                int iPort = Integer.parseInt(port);
                if (iPort >= 0) {
                    url += ":" + iPort;
                }
            } catch (NumberFormatException e) {
                log.error("Invalid port: " + port, e);
            }
        }
        if (db != null) {
            url += "/" + db;
        }

        return url;
    }

    /**
     * Close the specified resources
     *
     * @param rs
     * @param st
     * @param conn
     */
    static void closeResources(ResultSet rs, Statement st, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("Error closing sql connection", e);
            }
        }

    }

    public static String[] lineToArray(ResultSet rs, DBReader.ColumnMap columnMap) throws SQLException {
        int colCount = columnMap.getMaxFileColNum() - columnMap.getMinFileColNum() + 1;
        String[] tokens = new String[colCount];

        for (int cc = columnMap.getMinFileColNum(); cc < columnMap.getMaxFileColNum(); cc++) {
            int sqlCol = columnMap.getDBColumn(cc);
            tokens[cc] = getStringFromResultSet(rs, sqlCol);
        }
        return tokens;
    }

    /**
     * Convert a the current line to an array of strings
     *
     * @param rs
     * @param startColIndex 1-based start column index (lower columns are ignored)
     * @param endColIndex   1-based, inclusive end column index (columns afterwards are ignored)
     * @return
     * @throws SQLException
     */
    public static String[] lineToArray(ResultSet rs, int startColIndex, int endColIndex) throws SQLException {
        int colCount = Math.min(rs.getMetaData().getColumnCount(), endColIndex) - startColIndex + 1;
        String[] tokens = new String[colCount];
        for (int cc = 0; cc < colCount; cc++) {
            int sqlCol = cc + startColIndex;
            tokens[cc] = getStringFromResultSet(rs, sqlCol);
        }
        return tokens;
    }


    /**
     * Get the value at column {@code sqlCol} in the current row as a string.
     * <p/>
     * Have to parse blobs specially, otherwise we get the pointer as a string
     *
     * @param rs
     * @param sqlCol 1-indexed column number
     * @return
     * @throws SQLException
     */
    private static String getStringFromResultSet(ResultSet rs, int sqlCol) throws SQLException {
        String s;
        int type = rs.getMetaData().getColumnType(sqlCol);

        if (blobTypes.contains(type)) {
            Blob b = rs.getBlob(sqlCol);
            s = new String(b.getBytes(1l, (int) b.length()));
        } else {
            s = rs.getString(sqlCol);
        }
        return s;
    }

    private static final Set<Integer> blobTypes;

    static {
        int[] blobtypes = {Types.BINARY, Types.BLOB, Types.VARBINARY, Types.LONGVARBINARY};
        blobTypes = new HashSet<Integer>(blobtypes.length);
        for (int bt : blobtypes) {
            blobTypes.add(bt);
        }
    }

}
