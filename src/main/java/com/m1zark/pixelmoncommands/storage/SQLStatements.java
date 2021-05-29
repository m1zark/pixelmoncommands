package com.m1zark.pixelmoncommands.storage;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.m1zark.pixelmoncommands.WT.WTPokemon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class SQLStatements {
    private String mainTable;
    private String storageTable;

    public SQLStatements(String mainTable, String storageTable) {
        this.mainTable = mainTable;
        this.storageTable = storageTable;
    }

    public void createTables() {
        try(Connection connection = DataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + this.mainTable + "` (ID INTEGER NOT NULL AUTO_INCREMENT, Pokemon LONGTEXT, PRIMARY KEY(ID))")) {
                statement.executeUpdate();
            }
            try(PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + this.storageTable + "` (ID INTEGER NOT NULL AUTO_INCREMENT, PlayerUUID CHAR(36), Pokemon LONGTEXT, PRIMARY KEY(ID));")) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearTables() {
        try(Connection connection = DataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("TRUNCATE TABLE `" + this.mainTable + "`")) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void addStoragePokemon(UUID uuid, WTPokemon pkm) {
        Gson gson = new Gson();

        try(Connection connection = DataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + this.storageTable + "`(PlayerUUID, Pokemon) VALUES (?, ?)")) {
                statement.setString(1, uuid.toString());
                statement.setString(2, gson.toJson(pkm));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<WTPokemon> getStoragePokemon(UUID uuid) {
        Gson gson = new Gson();
        ArrayList<WTPokemon> listings = new ArrayList<>();

        try(Connection connection = DataSource.getConnection()) {
            try(ResultSet results = connection.prepareStatement("SELECT * FROM `" + this.storageTable + "` WHERE PlayerUUID='" + uuid + "'").executeQuery()) {
                while (results.next()) {
                    listings.add(gson.fromJson(results.getString("Pokemon"), WTPokemon.class));
                }
                return listings;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Lists.newArrayList();
        }
    }

    public LinkedHashMap<WTPokemon,String> getStorage() {
        Gson gson = new Gson();
        LinkedHashMap<WTPokemon,String> listings = new LinkedHashMap<>();

        try(Connection connection = DataSource.getConnection()) {
            try(ResultSet results = connection.prepareStatement("SELECT * FROM `" + this.storageTable + "`").executeQuery()) {
                while (results.next()) {
                    listings.put(gson.fromJson(results.getString("Pokemon"), WTPokemon.class), results.getString("PlayerUUID"));
                }
                return listings;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new LinkedHashMap<>();
        }
    }

    public void removeStoragePokemon(UUID uuid) {
        try(Connection connection = DataSource.getConnection()) {
            try(PreparedStatement query = connection.prepareStatement("DELETE FROM `" + this.storageTable + "` WHERE PlayerUUID='" + uuid + "'")) {
                query.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void addPokemon(WTPokemon pokemon) {
        Gson gson = new Gson();
        try(Connection connection = DataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + this.mainTable + "` (Pokemon) VALUES (?)")) {
                statement.setString(1, gson.toJson(pokemon));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<WTPokemon> getPool() {
        Gson gson = new Gson();
        ArrayList<WTPokemon> pool = new ArrayList<>();

        try(Connection connection = DataSource.getConnection()) {
            try (ResultSet logs = connection.prepareStatement("SELECT * FROM `" + this.mainTable + "`").executeQuery()) {
                int id = 1;
                while(logs.next()) {
                    WTPokemon pokemon = gson.fromJson(logs.getString("Pokemon"), WTPokemon.class);
                    pokemon.setId(id);
                    pool.add(pokemon);
                    id++;
                }
                return pool;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Lists.newArrayList();
        }
    }
}
