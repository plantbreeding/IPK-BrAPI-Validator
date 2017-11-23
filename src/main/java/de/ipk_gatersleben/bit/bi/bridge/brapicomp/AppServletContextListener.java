package de.ipk_gatersleben.bit.bi.bridge.brapicomp;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Endpoint;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.TestReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.DataSourceManager;
import io.restassured.RestAssured;

/**
 * Initializes tables, Daos and others.
 */
@WebListener
public class AppServletContextListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        try {
            DataSourceManager.closeConnectionSource();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent e) {
        Config.init();
        createDatabaseConnection(e.getServletContext());
        createTables();
        buildDaos();
        if (Config.get("proxy") != null) {
            RestAssured.proxy(Config.get("proxy"), Integer.parseInt(Config.get("proxyport")));
        }


    }

    private static void createDatabaseConnection(ServletContext servletContext) {
        String path = Config.get("dbpath");
        String databaseUrl = "jdbc:h2:" + path + "brapivalDB";
        ConnectionSource connectionSource;
        try {
            connectionSource = new JdbcPooledConnectionSource(databaseUrl);
            ((JdbcPooledConnectionSource) connectionSource).setUsername(Config.get("dbuser"));
            ((JdbcPooledConnectionSource) connectionSource).setPassword(Config.get("dbpass"));
            DataSourceManager.setConnectionSource(connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTables() {
        try {
            DataSourceManager.createTable(Endpoint.class);
            DataSourceManager.createTable(TestReport.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void buildDaos() {
        try {
            DataSourceManager.addDao(Endpoint.class);
            DataSourceManager.addDao(TestReport.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
