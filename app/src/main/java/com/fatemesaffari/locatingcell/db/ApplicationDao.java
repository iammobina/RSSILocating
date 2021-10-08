package com.fatemesaffari.locatingcell.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.fatemesaffari.locatingcell.model.Parameters;

import java.util.List;

@Dao
public interface ApplicationDao {

    @Query("SELECT * FROM Parameter")
    List<Parameters> getAll();

    @Query("DELETE FROM Parameter")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Parameters parametrs);
}