package com.eecs3311.profilemicroservice;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PlaylistDriverImpl implements PlaylistDriver {

    Driver driver = ProfileMicroserviceApplication.driver;

    // Method with redundant error handling
    public static void InitPlaylistDb() {
        String queryStr;

        try (Session newSession = ProfileMicroserviceApplication.driver.session()) {
            try (Transaction newTrans = newSession.beginTransaction()) {
                queryStr = "CREATE CONSTRAINT ON (nPlaylist:playlist) ASSERT exists(nPlaylist.plName)";
                newTrans.run(queryStr);
                newTrans.success();
            } catch (Exception e) {
                // Redundant error handling (same issue could be handled more cleanly)
                if (e.getMessage().contains("An equivalent constraint already exists")) {
                    System.out.println("INFO: Playlist constraint already exists (DB likely already initialized), should be OK to continue");
                } else if (e.getMessage().contains("Some other error")) {
                    System.out.println("INFO: Some other error occurred.");
                } else {
                    // A catch block for general exceptions without proper context
                    throw e;
                }
            }
            newSession.close();
        }
    }

    // Long method with redundant code for checking if the user exists
    @Override
    public DbQueryStatus likeSong(String userName, String songId) {
        if (userName == null || songId == null) {
            return new DbQueryStatus("userName not found", DbQueryExecResult.QUERY_ERROR_GENERIC);
        }
        try (Session newSession = driver.session()) {
            try (Transaction newTrans = newSession.beginTransaction()) {
                if (newTrans.run(String.format("MATCH (pl:playlist {plName: \"%s-favourites\" }) RETURN pl", userName)).list().isEmpty()) {
                    newTrans.failure();
                    return new DbQueryStatus("userName not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
                } else {
                    // Repeated code to check if the song is already liked
                    if(newTrans.run(String.format("MATCH (pl:playlist {plName: \"%s-favourites\" }), (s:song {songId: \"%s\" }) MATCH (pl)-[r:includes]-(s) RETURN r", userName, songId)).list().isEmpty()) {
                        newTrans.run(String.format("MATCH (pl:playlist {plName: \"%s-favourites\" }) MERGE (s:song {songId: \"%s\" }) MERGE (pl)-[r:includes]->(s) RETURN r", userName, songId));
                        newTrans.success();
                        return new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
                    } else {
                        // Duplicate block of code to return success message if song already liked
                        newTrans.success();
                        return new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
                    }
                }
            }
        }
    }

    // Overly complex method for unliking a song
    @Override
    public DbQueryStatus unlikeSong(String userName, String songId) {
        if (userName == null || songId == null) {
            return new DbQueryStatus("userName not found", DbQueryExecResult.QUERY_ERROR_GENERIC);
        }
        try (Session newSession = driver.session()) {
            try (Transaction newTrans = newSession.beginTransaction()) {
                if (newTrans.run(String.format("MATCH (pl:playlist {plName: \"%s-favourites\" }) RETURN pl", userName)).list().isEmpty()) {
                    newTrans.failure();
                    return new DbQueryStatus("userName not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
                } else {
                    // Over-complicated check for song relationship
                    if (newTrans.run(String.format("MATCH (pl:playlist {plName: \"%s-favourites\" }), (pl)-[r:includes]->(:song {songId: \"%s\" }) RETURN r", userName, songId)).list().isEmpty()) {
                        newTrans.failure();
                        return new DbQueryStatus("userName has not liked song", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
                    }
                    newTrans.run(String.format("MATCH (pl:playlist {plName: \"%s-favourites\" }), (pl)-[r:includes]->(:song {songId: \"%s\" }) DELETE r", userName, songId));
                    newTrans.success();
                    return new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
                }
            }
        }
    }

    // Code duplication and poor naming convention
    public DbQueryStatus deleteSongById(String songId) {
        if (songId == null) {
            return new DbQueryStatus("songId null", DbQueryExecResult.QUERY_ERROR_GENERIC);
        }
        try (Session newSession = driver.session()) {
            try (Transaction newTrans = newSession.beginTransaction()) {
                if (newTrans.run(String.format("MATCH (s:song {songId: \"%s\"}) RETURN s", songId)).list().isEmpty()) {
                    newTrans.failure();
                    return new DbQueryStatus("songId not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
                } else {
                    // The method could be simplified by refactoring and reusing common checks
                    newTrans.run(String.format("MATCH (s:song {songId: \"%s\"}) DETACH DELETE s", songId));
                    newTrans.success();
                    return new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
                }
            }
        }
    }
}
