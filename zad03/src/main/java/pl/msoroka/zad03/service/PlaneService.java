package pl.msoroka.zad03.service;

import pl.msoroka.zad03.domain.Plane;

import java.sql.*;
import java.util.Date;
import java.util.List;

public class PlaneService {
    private final String URL = "jdbc:hsqldb:hsql://localhost/workdb";
    private final Connection connection;
    private final Statement statement;
    private boolean tableExists = false;
    private PreparedStatement createPlanesTablePStmt;
    private PreparedStatement insertPlanePStmt;
    private PreparedStatement updatePlanePStmt;
    private PreparedStatement readAllPlanesPStmt;
    private PreparedStatement deleteAllPlanesPStmt;
    private PreparedStatement searchPlaneByNamePStmt;
    private PreparedStatement deletePlanePStmt;

    public PlaneService() throws SQLException {
        connection = DriverManager.getConnection(URL);
        statement = connection.createStatement();

        createPlanesTablePStmt = connection.prepareStatement("CREATE TABLE Plane (id bigint GENERATED BY DEFAULT AS IDENTITY, producer VARCHAR(30) UNIQUE, capacity INT, produceDate DATE, combustion DOUBLE)");

        ResultSet rs = connection.getMetaData().getTables(null, null, null, null);

        while (rs.next()) {
            if ("Plane".equalsIgnoreCase(rs.getString("table_name"))) {
                tableExists = true;

                break;
            }
        }

        if (!tableExists) {
            createPlanesTablePStmt.executeUpdate();
        }

        insertPlanePStmt = connection.prepareStatement("INSERT INTO Plane(producer, capacity, produceDate, combustion) VALUES(?, ?, ?, ?)");
        deleteAllPlanesPStmt = connection.prepareStatement("DELETE FROM Plane");
        searchPlaneByNamePStmt = connection.prepareStatement("SELECT * FROM Plane WHERE producer=?");
        readAllPlanesPStmt = connection.prepareStatement("SELECT * FROM Plane");
        deletePlanePStmt = connection.prepareStatement("DELETE FROM Plane WHERE producer=?");
    }

    public void addPlane(Plane plane) throws SQLException {
        insertPlanePStmt.setString(1, plane.getProducer());
        insertPlanePStmt.setInt(2, plane.getCapacity());
        insertPlanePStmt.setDate(3, plane.getProduceDate());
        insertPlanePStmt.setDouble(4, plane.getCombustion());

        insertPlanePStmt.executeUpdate();
    }

    public void showAllPlanes() throws SQLException {
        ResultSet rs = readAllPlanesPStmt.executeQuery();

        while (rs.next()) {
            System.out.println("Producer: " + rs.getString("producer") + "\nProduction date: " + rs.getDate("produceDate") + "\nCapacity: " + rs.getInt("capacity") + "\nAverage combustion: " + rs.getDouble("combustion") + "\n");
        }
    }

    public void searchPlaneByName(String producer) throws SQLException {
        searchPlaneByNamePStmt.setString(1, producer);

        ResultSet rs = searchPlaneByNamePStmt.executeQuery();
        while (rs.next()) {
            System.out.println("Producer: " + rs.getString("producer") + "\nProduction date: " + rs.getDate("produceDate") + "\nCapacity: " + rs.getInt("capacity") + "\nAverage combustion: " + rs.getDouble("combustion") + "\n");
        }
    }

    public void updatePlane(String producer, int capacity, Date produceDate, double combustion) throws SQLException {
        updatePlanePStmt = connection.prepareStatement("UPDATE Plane SET producer=?, capacity=?, produceDate=?, combustion=? WHERE producer=?");
        searchPlaneByNamePStmt.setString(1, producer);

        ResultSet rs = searchPlaneByNamePStmt.executeQuery();

        if (rs.next() == false) {
            System.out.println("Nie znaleziono samolotu do edycji");
        } else {
            updatePlanePStmt.setString(1, producer);
            updatePlanePStmt.setInt(2, capacity);
            updatePlanePStmt.setDate(3, (java.sql.Date) produceDate);
            updatePlanePStmt.setDouble(4, combustion);
            updatePlanePStmt.setString(5, producer);

            updatePlanePStmt.executeUpdate();
        }
    }

    public void removePlanes() throws SQLException {
        deleteAllPlanesPStmt.executeUpdate();
    }

    public void removePlane(String producer) throws SQLException {
        deletePlanePStmt.setString(1, producer);

        deletePlanePStmt.executeUpdate();
    }

    public boolean addPlanes(List<Plane> planes) {

        try {
            connection.setAutoCommit(false);

            for (Plane plane : planes) {
                insertPlanePStmt.setString(1, plane.getProducer());
                insertPlanePStmt.setInt(2, plane.getCapacity());
                insertPlanePStmt.setDate(3, plane.getProduceDate());
                insertPlanePStmt.setDouble(4, plane.getCombustion());

                insertPlanePStmt.executeUpdate();
            }

            connection.commit();

            return true;
        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {

                e1.printStackTrace();
            }
        }

        return false;
    }
}
